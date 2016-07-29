package classification

import ml.classification.SVMClassifyUtils
import ml.feature.VectorizerUtils
import nlp.segment.SegmentUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.classification.SVMModel
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Administrator on 2016/7/28.
  */
object SVMClassifyTrainDemo {
  def main(args: Array[String]) {
    Logger.getRootLogger.setLevel(Level.INFO)

    val conf = new SparkConf().setAppName("SVMClassifyTrain").setMaster("local[4]")
    val sc = new SparkContext(conf)

    //    val args = Array("ckooc-ml/data/news/train", "ckooc-ml/models/vectorize", "ckooc-ml/models/classification/svm")
    val args = Array("E:/test/chinaNews_2_train", "E:/test/models/vecModels", "E:/test/models/svmModel")

    val dataPath = args(0)
    val vecModelPath = args(1)
    val svmModelPath = args(2)


    //--- 分词
    val preUtils = SegmentUtils("ckooc-ml/src/main/resources/segment.properties")
    val svmUtils = new SVMClassifyUtils()

    val (trainData, categoryMap) = svmUtils.getFromDic(sc, dataPath)
    val splitedRDD = preUtils.run(trainData)


    //--- 向量化
    val minDocFreq = 2 //最小文档频率阀值
    val toTFIDF = true //是否将TF转化为TF-IDF
    val vocabSize = 15000 //词汇表大小

    val vectorizer = new VectorizerUtils()
      .setMinDocFreq(minDocFreq)
      .setToTFIDF(toTFIDF)
      .setVocabSize(vocabSize)

    val (vectorizedRDD, cvModel, idf) = vectorizer.vectorize(splitedRDD)
    vectorizer.save(vecModelPath, cvModel, idf)

    val numIterations = 50

    //--- 训练模型
    val model: SVMModel = svmUtils.train(vectorizedRDD, numIterations)
    svmUtils.save(sc, model, categoryMap, svmModelPath)

    sc.stop()
  }
}
