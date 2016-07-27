package classification

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}

import ml.classification.LRClassifyUtils
import ml.feature.Vectorizer
import nlp.preprocess.PreProcessUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Administrator on 2016/7/27.
  */
object LRClassifyTrainDemo {
  def main(args: Array[String]) {
    Logger.getRootLogger.setLevel(Level.WARN)

    val conf = new SparkConf().setAppName("LRClassifyTrain").setMaster("local[4]")
    val sc = new SparkContext(conf)

    val args = Array("ckooc-ml/data/news/train", "ckooc-ml/models/vectorize", "ckooc-ml/models/classification/lr")

    val dataPath = args(0)
    val vecModelPath = args(1)
    val lrModelPath = args(2)

    val classNum = 6


    //--- 分词
    val preUtils = PreProcessUtils("ckooc-ml/src/main/resources/preprocess.properties")
    val lrUtils = new LRClassifyUtils(classNum)

    val (trainData, categoryMap) = lrUtils.getFromDic(sc, dataPath)
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


    //--- 训练模型
    val model: LogisticRegressionModel = lrUtils.train(vectorizedRDD)
    lrUtils.save(sc, model, categoryMap, lrModelPath)

    sc.stop()
  }
}
