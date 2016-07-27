package clustering

import ml.clustering.lda.LDAUtils
import ml.feature.Vectorizer
import nlp.preprocess.PreProcessUtils
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


    //--- 分词
    val preUtils = PreProcessUtils("ckooc-ml/src/main/resources/preprocess.properties")
    val splitSize = 48    //数据切分大小（MB）
    val trainData = preUtils.getText(sc, dataPath, splitSize).zipWithIndex().map(_.swap)
    val splitedRDD = preUtils.run(trainData)


    //--- 向量化
    val minDocFreq = 2    //最小文档频率阀值
    val toTFIDF = true    //是否将TF转化为TF-IDF
    val vocabSize = 5000    //词汇表大小

    val vectorizer = new Vectorizer()
      .setMinDocFreq(minDocFreq)
      .setToTFIDF(toTFIDF)
      .setVocabSize(vocabSize)

    val (vectorizedRDD, cvModel, idf) = vectorizer.vectorize(splitedRDD)
    vectorizer.save(vecModelPath, cvModel, idf)

    val trainRDD = vectorizedRDD.map(line => (line.label.toLong, line.features))


    //-- LDA训练
    val k = 10    //主题个数
    val analysisType = "em"   //参数估计算法
    val maxIterations = 20    //迭代次数

    val ldaUtils = new LDAUtils()
      .setK(k)
      .setAlgorithm(analysisType)
      .setMaxIterations(maxIterations)

    val ldaModel: LDAModel = ldaUtils.train(trainRDD)
    ldaUtils.save(sc, ldaModelPath, ldaModel)

    sc.stop()
  }
}
