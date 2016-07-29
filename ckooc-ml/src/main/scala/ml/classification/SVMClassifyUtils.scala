package ml.classification

import java.io.{FileInputStream, InputStreamReader, _}

import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS, SVMModel, SVMWithSGD}
import org.apache.spark.mllib.evaluation.{BinaryClassificationMetrics, MulticlassMetrics}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

import scala.collection.mutable

/**
  * Created by Administrator on 2016/7/28.
  */
class SVMClassifyUtils extends Serializable {
  /**
    * 从目录获取带分类数据，目录下每个文件名即为对应分类
    *
    * @param sc     SparkContext
    * @param inPath 输入文件目录
    * @return
    */
  def getFromDic(sc: SparkContext, inPath: String): (RDD[(Long, String)], mutable.HashMap[Long, String]) = {
    val dir = new File(inPath)
    val fileList = dir.listFiles()

    val categoryMap = new mutable.HashMap[Long, String]()
    var id = 0L

    var fileName = fileList.head.getName
    if (fileName.contains(".")) {
      fileName = fileName.substring(0, fileName.indexOf("."))
    }
    categoryMap.put(id, fileName)

    var textRDD: RDD[(Long, String)] = sc.textFile(fileList.head.getAbsolutePath).map(line => (0L, line))

    for (file <- fileList.tail) {
      if (fileName.contains(".")) {
        fileName = file.getName.substring(0, fileName.indexOf("."))
      }
      id += 1
      val tempID = id
      val tempRDD = sc.textFile(file.getAbsolutePath).map(line => (tempID, line))
      textRDD ++= tempRDD
      categoryMap.put(id, fileName)
    }

    (textRDD, categoryMap)
  }


  def getFromDic(sc: SparkContext, inPath: String, categoryMap: mutable.HashMap[Long, String]): RDD[(Long, String)] = {
    val tempMap = categoryMap.toArray.map(_.swap).toMap

    val dir = new File(inPath)
    var fileList = dir.listFiles()

    var id = 0L

    val fileName = fileList.head.getName
    if (fileName.contains(".")) {
      fileList = fileList.sortBy(file => tempMap.get(file.getName.substring(0, file.getName.indexOf("."))).get)
    }

    var textRDD: RDD[(Long, String)] = sc.textFile(fileList.head.getAbsolutePath).map(line => (0L, line))

    for (file <- fileList.tail) {
      id += 1
      val tempID = id
      val tempRDD = sc.textFile(file.getAbsolutePath).map(line => (tempID, line))
      textRDD ++= tempRDD
    }

    textRDD
  }


  /**
    * 训练，返回LR模型，向量模型cvModel以及类别映射Map
    *
    * @param tokensLP LabeledPoint数据
    * @return LRModel
    */
  def train(tokensLP: RDD[LabeledPoint], numIterations: Int): SVMModel = {
    tokensLP.persist()
    //训练
    val model = SVMWithSGD.train(tokensLP, numIterations)
    tokensLP.unpersist()

    model
  }


  /**
    * 测试模型准确度
    *
    * @param tokensLP LabeledPoint数据
    * @param model  LRModel
    */
  def test(tokensLP: RDD[LabeledPoint], model: SVMModel) = {
    tokensLP.persist()

    //预测，返回预测标签和原有标签
    val predictionAndLabels = tokensLP.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }
    tokensLP.unpersist()

    val metrics = new BinaryClassificationMetrics(predictionAndLabels)

    val auROC = metrics.areaUnderROC()

    println("Area under ROC = " + auROC)
  }


  /**
    * 单文本预测，输入一段文本预测其类别
    *
    * @param tokensLP LabeledPoint数据
    * @param model  LRModel
    * @return 预测类别ID
    */
  def predict(tokensLP: RDD[LabeledPoint], model: SVMModel): Double = {
    val predictionAndLabels = tokensLP.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }

    predictionAndLabels.collect()(0)._1
  }


  /**
    * 保存LR分类模型
    * @param sc SparkContext
    * @param model  LR模型
    * @param categoryMap  分类映射map
    * @param modelPath  模型保存路径
    */
  def save(
            sc: SparkContext,
            model: SVMModel,
            categoryMap: mutable.HashMap[Long, String],
            modelPath: String): Unit = {

    model.save(sc, modelPath + File.separator + "model")

    val bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(modelPath + File.separator + "categoryMap")))
    categoryMap.foreach { case (id, category) => bw.write(id + " " + category + "\n") }
    bw.close()
  }


  /**
    * 加载LR分类模型和类别映射map
    * @param sc SparkContext
    * @param modelPath  模型保存路径
    * @return (LRModel, categoryMap)
    */
  def load(
            sc: SparkContext,
            modelPath: String): (SVMModel, mutable.HashMap[Long, String]) = {

    val model = SVMModel.load(sc, modelPath + File.separator + "model")

    val br = new BufferedReader(new InputStreamReader(new FileInputStream(modelPath + File.separator + "categoryMap")))
    val categoryMap = new mutable.HashMap[Long, String]()

    var text = br.readLine()
    while (text != null) {
      val tokens = text.split(" ")
      categoryMap.put(tokens(0).toLong, tokens(1))
      text = br.readLine()
    }

    br.close()

    (model, categoryMap)
  }
}
