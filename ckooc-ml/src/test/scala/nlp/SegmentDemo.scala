package nlp

import algorithms.nlp.segment.Segmenter
import com.hankcs.hanlp.utility.Predefine
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

/**
  * Created by yhao on 2017/3/14.
  */
object SegmentDemo extends Serializable{
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.WARN)

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
    Predefine.HANLP_PROPERTIES_PATH = "E:/projects/ML/CkoocNLP/src/main/resources/hanlp.properties"

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
