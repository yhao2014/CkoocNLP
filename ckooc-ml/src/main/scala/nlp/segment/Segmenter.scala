package nlp.segment

import com.hankcs.hanlp.seg.Segment
import com.hankcs.hanlp.seg.common.Term
import com.hankcs.hanlp.tokenizer.{IndexTokenizer, NLPTokenizer, SpeedTokenizer, StandardTokenizer}
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.{BooleanParam, IntParam, Param, ParamMap}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset}
import param.{HasInputCol, HasOutputCol}
import util.MySchemaUtils

import scala.collection.JavaConversions._

/**
  * 基于HanLP的分词
  *
  * Created by yhao on 2017/2/17.
  */
class Segmenter(val uid: String) extends Transformer with HasInputCol with HasOutputCol with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("clean"))

  /** @group setParam */
  def setInputCol(value: String): this.type = set(inputCol, value)

  /** @group setParam */
  def setOutputCol(value: String): this.type = set(outputCol, value)

  //英文字符正则
  private val enExpr = "[A-Za-z]+"
  //数值正则，可以匹配203,2.23,2/12
  private val numExpr = "\\d+(\\.\\d+)?(\\/\\d+)?"

  /**
    * 分词方式, 可选输入有: "StandardSegment", "NLPSegment", "IndexSegment", "SpeedSegment", "NShortSegment", "CRFSegment".
    * 默认"StandardSegment"分词方式
    * @group param
    */
  val segType: Param[String] = new Param[String](this, "segType", "set the type of segment")

  /** @group setParam */
  def setSegType(value: String): this.type = {
    val segTypeSet = Set("StandardSegment", "NLPSegment", "IndexSegment", "SpeedSegment", "NShortSegment", "CRFSegment")
    require(segTypeSet.contains(value), "分词方式错误, 请输入正确的分词方式!")
    set(segType, value)
  }

  /** @group getParam */
  def getSegType: String = $(segType)


  /**
    * 是否添加词性, 使用"/"进行分割, 默认不添加
    * @group param
    */
  val addNature: BooleanParam = new BooleanParam(this, "addNature", "add the nature of word")

  /** @group setParam */
  def isAddNature(value: Boolean): this.type = set(addNature, value)


  /**
    * 是否删除数字, 默认不删除
    * @group param
    */
  val delNum: BooleanParam = new BooleanParam(this, "delNum", "is delete number")

  /** @group setParam */
  def isDelNum(value: Boolean): this.type = set(delNum, value)


  /**
    * 是否删除英语单词, 默认不删除
    * @group param
    */
  val delEn: BooleanParam = new BooleanParam(this, "delEn", "is delete english")

  /** @group setParam */
  def isDelEn(value: Boolean): this.type = set(delEn, value)


  /**
    * 最小词长度, 大于等于1, 默认为2以去除长度过短的词
    * @group param
    */
  val minTermLen: IntParam = new IntParam(this, "minTermLen", "minimum word length (>= 1)")

  /** @group setParam */
  def setMinTermLen(value: Int): this.type = {
    require(value >= 0, "最短词长度必须大于等于0")
    set(minTermLen, value)
  }

  /** @group getParam */
  def getMinTermLen: Int = $(minTermLen)


  /**
    * 最小词数, 大于等于1, 默认为1
    * @group param
    */
  val minTermNum: IntParam = new IntParam(this, "minTermNum", "minimum word number (>= 1)")

  /** @group setParam */
  def setMinTermNum(value: Int): this.type = {
    require(value >= 0, "最小词数必须大于等于0")
    set(minTermNum, value)
  }

  /** @group getParam */
  def getMinTermNum: Int = $(minTermNum)


  /**
    * 设置默认参数
    */
  setDefault(segType -> "StandardSegment", addNature -> false,
    delNum -> false, delEn -> false, minTermLen -> 2, minTermNum -> 1)

  /**
    * 输出列数据类型
    */
  def outputDataType: DataType = new ArrayType(StringType, true)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val outputSchema = transformSchema(dataset.schema, logging = true)

    var segmenter: Segment = null
    getSegType match {
      case "NShortSegment" =>
        segmenter = new MyNShortSegment()
      case "CRFSegment" =>
        segmenter = new MyCRFSegment()
      case _ =>
    }

    def segmentFunc = udf{line: String =>
      var terms: Seq[Term] = Seq()

      getSegType match {
        case "StandardSegment" => terms = StandardTokenizer.segment(line)
        case "NLPSegment" => terms = NLPTokenizer.segment(line)
        case "IndexSegment" => terms = IndexTokenizer.segment(line)
        case "SpeedSegment" => terms = SpeedTokenizer.segment(line)
        case "NShortSegment" => terms = segmenter.seg(line)
        case "CRFSegment" => terms = segmenter.seg(line)
        case _ =>
          println("分词类型错误！")
          System.exit(1)
      }

      val termSeq = terms.flatMap { term =>
        val word = term.word.trim
        val nature = term.nature

        if ($(delNum) && word.matches(numExpr)) None      //去除数字
        else if ($(delEn) && word.matches(enExpr)) None   //去除英文
        else if (word.length < getMinTermLen) None            //去除过短的词
        else if ($(addNature)) Some(word + "/" + nature)
        else Some(word)
      }

      termSeq
    }

    val metadata = outputSchema($(outputCol)).metadata
    dataset.select(col("*"), segmentFunc(col($(inputCol))).as($(outputCol), metadata)).filter{record =>
      val outputIndex = record.fieldIndex($(outputCol))
      val tokens = record.getList(outputIndex)

      tokens.nonEmpty && tokens.size() > getMinTermNum
    }
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    val inputType = schema($(inputCol)).dataType
    require(inputType.typeName.equals(StringType.typeName),
      s"Input type must be StringType but got $inputType.")
    MySchemaUtils.appendColumn(schema, $(outputCol), outputDataType, nullable = true)
  }
}


object Segmenter extends DefaultParamsReadable[Segmenter] {
  override def load(path: String): Segmenter = super.load(path)
}
