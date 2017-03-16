package applications.ml

import algorithms.classification.MultiClassEvaluations
import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

/**
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

    val args = Array("LR")
    val modelType = args(0)

    val filePath = "ckooc-ml/data/classnews/predict"
    val modelPath = "ckooc-ml/models/news-class-" + modelType + "-model"   //加载模型

    val data = clean(filePath, spark)

    val model = PipelineModel.load(modelPath)
    val predictions = model.transform(data)

    //=== 模型评估
    val resultRDD = predictions.select("prediction", "indexedLabel").rdd.map { case Row(prediction: Double, label: Double) => (prediction, label) }
    val (precision, recall, f1) = MultiClassEvaluations.multiClassEvaluate(resultRDD)
    println("\n\n========= 评估结果 ==========")
    println(s"\n加权准确率：$precision")
    println(s"加权召回率：$recall")
    println(s"F1值：$f1")

//    predictions.select("label", "predictedLabel", "content").show(100, truncate = false)

    spark.stop()
  }


  /**
    * 清洗步骤, 可根据具体数据结构和业务场景的不同进行重写. 注意: 输出必须要有标签字段"label"
    *
    * @param filePath 数据路径
    * @param spark    SparkSession
    * @return 清洗后的数据, 包含字段: "label", "title", "time", "content"
    */
  def clean(filePath: String, spark: SparkSession): DataFrame = {
    import spark.implicits._
    val textDF = spark.sparkContext.textFile(filePath).flatMap { line =>
      val fields = line.split("\u00EF")

      if (fields.length > 3) {
        val categoryLine = fields(0)
        val categories = categoryLine.split("\\|")
        val category = categories.last

        var label = "其他"
        if (category.contains("文化")) label = "文化"
        else if (category.contains("财经")) label = "财经"
        else if (category.contains("军事")) label = "军事"
        else if (category.contains("体育")) label = "体育"
        else if (category.contains("娱乐")) label = "娱乐"
        else if (category.contains("社会")) label = "社会"
        else {}

        val title = fields(1)
        val time = fields(2)
        val content = fields(3)
        if (!label.equals("其他")) Some(label, title, time, content) else None
      } else None
    }.toDF("label", "title", "time", "content")

    textDF
  }
}
