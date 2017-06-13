package applications.mining

import algorithms.evaluation.MultiClassEvaluation
import config.paramconf.ClassParams
import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.{Row, SparkSession}

/**
  * 新闻多分类模型测试
  *
  * Created by yhao on 2017/3/15.
  */
object PredictNewsClassDemo extends Serializable {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.WARN)

    val spark = SparkSession
      .builder
      .master("local[2]")
      .appName("predict news multi class demo")
      .getOrCreate()

    val args = Array("ckooc-ml/data/classnews/predict", "lr")
    val filePath = args(0)
    val modelType = args(1)

    var modelPath = ""
    val params = new ClassParams

    modelType match {
      case "lr" => modelPath = params.LRModelPath
      case "dt" => modelPath = params.DTModelPath
      case _ =>
        println("模型类型错误！")
        System.exit(1)
    }

    import spark.implicits._
    val data = spark.sparkContext.textFile(filePath).flatMap { line =>
      val tokens: Array[String] = line.split("\u00ef")
      if (tokens.length > 3) Some((tokens(0), tokens(1), tokens(2), tokens(3))) else None
    }.toDF("label", "title", "time", "content")
    data.persist()

    //加载模型，进行数据转换
    val model = PipelineModel.load(modelPath)
    val predictions = model.transform(data)

    //=== 模型评估
    val resultRDD = predictions.select("prediction", "indexedLabel").rdd.map { case Row(prediction: Double, label: Double) => (prediction, label) }
    val (precision, recall, f1) = MultiClassEvaluation.multiClassEvaluate(resultRDD)
    println("\n\n========= 评估结果 ==========")
    println(s"\n加权准确率：$precision")
    println(s"加权召回率：$recall")
    println(s"F1值：$f1")

    //    predictions.select("label", "predictedLabel", "content").show(100, truncate = false)
    data.unpersist()

    spark.stop()
  }
}
