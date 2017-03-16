package nlp

import algorithms.nlp.clean.Cleaner
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

/**
  * Created by yhao on 2017/3/14.
  */
object CleanDemo extends Serializable {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.WARN)

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
}
