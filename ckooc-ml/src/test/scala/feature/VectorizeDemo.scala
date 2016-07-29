package feature

import ml.feature.VectorizerUtils
import nlp.segment.SegmentUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Administrator on 2016/7/27.
  */
object VectorizeDemo {
  def main(args: Array[String]) {
    Logger.getRootLogger.setLevel(Level.WARN)

    val conf = new SparkConf().setAppName("Vectorize").setMaster("local[4]")
    val sc = new SparkContext(conf)

    val args = Array("ckooc-ml/data/preprocess/train", "ckooc-ml/models/vectorize")

    val inPath = args(0)
    val outPath = args(1)
    val splitSize = 48

    val preUtils = SegmentUtils("ckooc-ml/src/main/resources/segment.properties")

    val vectorizer = new VectorizerUtils()
      .setMinDocFreq(2)
      .setToTFIDF(true)
      .setVocabSize(5000)

    val data = preUtils.getText(sc, inPath, splitSize).map { line =>
      val tokens = line.split("|")
      val id = tokens(0).toLong
      val values = tokens(1).split("\\s+")
      (id, values.toSeq)
    }

    //中文分词后数据向量化
    val (_, cvModel, idf) = vectorizer.vectorize(data)

    vectorizer.save(outPath, cvModel, idf)

    sc.stop()
  }
}
