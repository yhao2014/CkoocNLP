package applications.analysis

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}

import functions.segment.Segmenter
import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

/**
  * Created by Administrator on 2017/3/23.
  */
object StarsAnalysisDemo {
  def main(args: Array[String]) {
    Logger.getLogger("org").setLevel(Level.WARN)

    val spark = SparkSession
      .builder
      .master("local[2]")
      .appName("Stars Analysis Demo")
      .getOrCreate()

    val filePath = "E:/data/chinaNews/entertainment.txt"


    // 加载数据，并保留年份和内容字段，并对内容字段进行过滤
    import spark.implicits._
    val data = spark.sparkContext.textFile(filePath).flatMap { line =>
      val tokens: Array[String] = line.split("\u00ef")
      if (tokens.length > 3) {
        var year: String = tokens(2).split("-")(0)
        if (tokens(2).contains("年")) year = tokens(2).split("年")(0)

        var content = tokens(3)
        if (content.length > 22 && content.substring(0, 20).contains("日电")) {
          content = content.substring(content.indexOf("日电") + 2, content.length).trim
        }

        if (content.startsWith("(")) content = content.substring(content.indexOf(")") + 1, content.length)
        if (content.length > 20 && content.substring(content.length - 20, content.length).contains("记者")) {
          content = content.substring(0, content.lastIndexOf("记者")).trim
        }

        Some(year, content)
      } else None
    }.toDF("year", "content")

    // 分词，去除长度为1的词，每个词保留词性
    val segmenter = new Segmenter()
      .isAddNature(true)
      .isDelEn(true)
      .isDelNum(true)
      .setMinTermLen(2)
      .setMinTermNum(5)
      .setSegType("StandardSegment")
      .setInputCol("content")
      .setOutputCol("segmented")
    val segDF: DataFrame = segmenter.transform(data)
    segDF.cache()

    val segRDD: RDD[(Int, Seq[String])] = segDF.select("year", "segmented").rdd.map { case Row(year: String, terms: Seq[String]) =>
      (Integer.parseInt(year), terms)
    }

    val result: Array[String] = segRDD.map(line => line._1.toString + "\u00ef" + line._2.mkString(",")).collect()
    val writer: BufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("E:/entertainment_seg.txt")))
    result.foreach(line => writer.write(line + "\n"))
    writer.close()

    // 统计2016出现在新闻中最多的明星
    val stars2016 = segRDD.filter(_._1 == 2016)
      .flatMap { case (year: Int, termStr: Seq[String]) =>
        val person = termStr.map(term => (term.split("/")(0), term.split("/")(1))).filter(_._2.equals("nr")).map(term => (term._1, 1L))
        person
      }
      .reduceByKey(_ + _)
      .sortBy(_._2, ascending = false)

    segDF.unpersist()

    stars2016.take(100).foreach(println)

    spark.stop()
  }
}
