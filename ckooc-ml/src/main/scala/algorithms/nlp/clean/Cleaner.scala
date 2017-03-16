package algorithms.nlp.clean

import algorithms.nlp.clean.chinese.BCConvert
import com.hankcs.hanlp.HanLP
import config.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.{IntParam, Param, ParamMap}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.{StringType, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}
import util.MySchemaUtils


/**
  * Created by yhao on 2017/3/14.
  */
class Cleaner(val uid: String) extends Transformer with HasInputCol with HasOutputCol with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("clean"))

  /** @group setParam */
  def setInputCol(value: String): this.type = set(inputCol, value)

  /** @group setParam */
  def setOutputCol(value: String): this.type = set(outputCol, value)


  /**
    * 繁简转换, 可选输入有: 'f2j'(繁体转换为简体), 'j2f'(简体转换为繁体)
    * @group param
    */
  val fanjan: Param[String] = new Param[String](this, "fanjan", "words from fan to jan or from jan to fan")

  /** @group setParam */
  def setFanJian(value: String): this.type = {
    val fanjianSet = Set("f2j", "j2f")
    require(fanjianSet.contains(value), "繁简参数错误！")
    set(fanjan, value)
  }

  /** @group getParam */
  def getFanJian: String = $(fanjan)


  /**
    * 是否全角转半角, 可选输入有: 'q2b'(全角转半角), 'b2q'(半角转全角)
    * @group param
    */
  val quanban: Param[String] = new Param[String](this, "q2b", "from SBC case to DBC case")

  /** @group setParam */
  def setQuanBan(value: String): this.type = {
    val quanbanSet = Set("q2b", "b2q")
    require(quanbanSet.contains(value), "全半角参数错误!")
    set(quanban, value)
  }

  /** @group getParam */
  def getQuanBan: String = $(quanban)


  /**
    * 最短行长度，大于等于0，默认1以便返回非空字符串
    * @group param
    */
  val minLineLen: IntParam = new IntParam(this, "minLineLen", "minimum line length (>= 0)")

  /** @group setParam */
  def setMinLineLen(value: Int): this.type = {
    require(value >= 0, "最短行长度必须大于等于0")
    set(minLineLen, value)
  }

  /** @group getParam */
  def getMinLineLen: Int = $(minLineLen)

  /**
    * 设置默认参数
    */
  setDefault(fanjan -> "f2j", quanban -> "q2b", minLineLen -> 1)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val outputSchema = transformSchema(dataset.schema, logging = true)

    val cleanFunc = udf {line: String =>
      var cleaned = ""
      getFanJian match {
        case "f2j" => cleaned = HanLP.convertToSimplifiedChinese(line)
        case "j2f" => cleaned = HanLP.convertToTraditionalChinese(line)
        case _ => cleaned = line
      }

      getQuanBan match {
        case "q2b" => cleaned = BCConvert.qj2bj(cleaned)
        case "b2q" => cleaned = BCConvert.bj2qj(cleaned)
        case _ => cleaned = cleaned
      }

      cleaned
    }

    val metadata = outputSchema($(outputCol)).metadata
    dataset.select(col("*"), cleanFunc(col($(inputCol))).as($(outputCol), metadata)).filter{record =>
      val outputIndex = record.fieldIndex($(outputCol))
      record.getString(outputIndex).length >= getMinLineLen
    }
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    val inputType = schema($(inputCol)).dataType
    require(inputType.typeName.equals(StringType.typeName),
      s"Input type must be StringType but got $inputType.")
    MySchemaUtils.appendColumn(schema, $(outputCol), inputType, schema($(inputCol)).nullable)
  }
}


object Cleaner extends DefaultParamsReadable[Cleaner] {
  override def load(path: String): Cleaner = super.load(path)
}
