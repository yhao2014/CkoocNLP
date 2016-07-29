package clustering

import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter}

import ml.clustering.lda.LDAUtils
import ml.feature.VectorizerUtils
import nlp.PreProcessUtils
import nlp.segment.SegmentUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Administrator on 2016/7/26.
  */
object LDATestDemo {
  def main(args: Array[String]) {
    Logger.getRootLogger.setLevel(Level.WARN)

    val conf = new SparkConf().setAppName("LDATest").setMaster("local[4]")
    val sc = new SparkContext(conf)

    val args = Array("ckooc-ml/data/news/test", "ckooc-ml/models/vectorize", "ckooc-ml/models/lda", "ckooc-ml/data/ldaResult")

    val datainPath = args(0)
    val vecModelPath = args(1)
    val ldaModelPath = args(2)
    val resultPath = args(3)

    val blockSize = 48
    val minDocSize = 2
    val vocabSize = 5000
    val toTFIDF = true

    val preUtils = new PreProcessUtils()
      .setBlockSize(blockSize)
      .setMinDocFreq(minDocSize)
      .setVocabSize(vocabSize)
      .setToTFIDF(toTFIDF)

    val (testRDD, cvModel) = preUtils.run(sc, datainPath, vecModelPath, "test")


    //-- 加载LDA模型
    val k = 10 //主题个数
    val analysisType = "em" //参数估计算法
    val maxIterations = 20 //迭代次数

    val ldaUtils = new LDAUtils()
      .setK(k)
      .setAlgorithm(analysisType)
      .setMaxIterations(maxIterations)

    val ldaModel = ldaUtils.load(sc, ldaModelPath)
    val (docTopics, topicWords) = ldaUtils.predict(testRDD, ldaModel, cvModel, sorted = true)


    //--- 输出结果
    println("文档-主题分布：")
    docTopics.take(5).foreach(doc => {
      val docTopicsArray = doc._2.map(topic => topic._1 + ":" + topic._2)
      println(doc._1 + ": [" + docTopicsArray.mkString(",") + "]")
    })

    println("主题-词：")
    topicWords.take(3).zipWithIndex.foreach(topic => {
      println("Topic: " + topic._2)
      topic._1.foreach(word => {
        println(word._1 + "\t" + word._2)
      })
      println()
    })

    //保存结果
    saveReasult(docTopics, topicWords, resultPath)

    sc.stop()
  }


  /**
    * 保存预测结果
    *
    * @param docTopics  文档-主题分布
    * @param topicWords 主题-词
    * @param outFile    输出路径
    */
  def saveReasult(docTopics: RDD[(Long, Array[(Double, Int)])], topicWords: Array[Array[(String, Double)]], outFile: String): Unit = {
    val bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile + File.separator + "docTopics.txt")))
    val bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile + File.separator + "topicWords.txt")))

    docTopics.collect().foreach(doc => {
      val docTopicsArray = doc._2.map(topic => topic._1 + ":" + topic._2)
      bw1.write(doc._1 + ": [" + docTopicsArray.mkString(",") + "]\n")
    })

    topicWords.zipWithIndex.foreach(topic => {
      bw2.write("\n\nTopic: " + topic._2 + "\n")
      topic._1.foreach(word => {
        bw2.write(word._1 + "\t" + word._2 + "\n")
      })
      println()
    })

    bw1.close()
    bw2.close()
  }
}
