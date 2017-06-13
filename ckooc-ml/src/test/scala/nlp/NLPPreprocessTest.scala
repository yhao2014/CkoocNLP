package nlp

import com.hankcs.hanlp.utility.Predefine
import functions.clean.Cleaner
import functions.segment.Segmenter
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.junit.Test

import scala.reflect.io.File

/**
  * Created by yhao on 2017/6/13.
  */
class NLPPreprocessTest {
  Logger.getLogger("org").setLevel(Level.WARN)



  /**
    * 数据清洗器测试
    */
  @Test
  def testCleaner(): Unit = {
    val spark = SparkSession
      .builder
      .master("local[2]")
      .appName("Clean Demo")
      .getOrCreate()

    val text = Seq((0, "用筆記簿型電腦寫程式HelloWorld"), (1, "用电脑书写程式可以自动进行繁简转换"), (2, "还可以进行全半角转换，１２４５ａｄｆ"))
    val sentenceData = spark.createDataFrame(text).toDF("id", "sentence")

    val cleaner = new Cleaner()
      .setFanJian("f2j")
      .setQuanBan("q2b")
      .setInputCol("sentence")
      .setOutputCol("cleaned")

    val transformed = cleaner.transform(sentenceData)
    transformed.show(false)

    spark.stop()
  }



  /**
    *分词器测试
    */
  @Test
  def testSegmenter(): Unit = {
    val spark = SparkSession
      .builder
      .master("local[2]")
      .appName("Segment Demo")
      .getOrCreate()

    val text = Seq(
      (0, "这段文本是用来做分词测试的！This text is for test!"),
      (1, "江州市长江大桥参加长江大桥通车仪式"),
      (2, "他邀请了不少于10个明星，有：范冰冰、赵薇、周杰伦等，还有20几位商业大佬")
    )
    val sentenceData = spark.createDataFrame(text).toDF("id", "sentence")

    // 设置HanLP配置文件路径, 默认位于classpath路径中
    val path = this.getClass.getClassLoader.getResource("").getPath
    Predefine.HANLP_PROPERTIES_PATH = path + File.separator + "hanlp.properties"

    val segmenter = new Segmenter()
      .isDelEn(true)
      .isDelNum(true)
      .isAddNature(true)
      .setSegType("StandardSegment")
      .setMinTermLen(2)
      .setMinTermNum(3)
      .setInputCol("sentence")
      .setOutputCol("segmented")

    segmenter.transform(sentenceData).show(false)

    spark.stop()
  }
}
