package clustering

import ml.clustering.lda.LDAUtils
import ml.feature.VectorizerUtils
import nlp.PreProcessUtils
import nlp.segment.SegmentUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.clustering.LDAModel
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Administrator on 2016/7/26.
  */
object LDATrainDemo {
  def main(args: Array[String]) {
    Logger.getRootLogger.setLevel(Level.WARN)

    val conf = new SparkConf().setAppName("LDATrain").setMaster("local[4]")
    val sc = new SparkContext(conf)

    val args = Array("ckooc-ml/data/news/train", "ckooc-ml/models/vectorize", "ckooc-ml/models/lda")

    val dataPath = args(0)
    val vecModelPath = args(1)
    val ldaModelPath = args(2)

    val blockSize = 48
    val minDocSize = 2
    val vocabSize = 5000
    val toTFIDF = true

    val preUtils = new PreProcessUtils()
      .setBlockSize(blockSize)
      .setMinDocFreq(minDocSize)
      .setVocabSize(vocabSize)
      .setToTFIDF(toTFIDF)

    val trainRDD = preUtils.run(sc, dataPath, vecModelPath, "train")._1

    //-- LDA训练
    val k = 10 //主题个数
    val analysisType = "em" //参数估计算法
    val maxIterations = 20 //训练迭代次数

    val ldaUtils = new LDAUtils()
      .setK(k)
      .setAlgorithm(analysisType)
      .setMaxIterations(maxIterations)

    val ldaModel: LDAModel = ldaUtils.train(trainRDD)
    ldaUtils.save(sc, ldaModelPath, ldaModel)

    sc.stop()
  }
}
