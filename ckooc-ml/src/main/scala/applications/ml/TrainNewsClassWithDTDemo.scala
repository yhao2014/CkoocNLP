package applications.ml

import algorithms.nlp.Preprocessor
import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.feature._
import org.apache.spark.sql.{DataFrame, SparkSession}
import param.ClassParams

/**
  * Created by yhao on 2017/3/15.
  */
object TrainNewsClassWithDTDemo {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.WARN)

    val spark = SparkSession
      .builder
      .master("local[2]")
      .appName("train news multi class with DT")
      .getOrCreate()

    val filePath = "ckooc-ml/data/classnews/train"
    val modelPath = "ckooc-ml/models/news-class-DT-model"

    val data = clean(filePath, spark)
    val preprocessor = new Preprocessor
    val pipeline = preprocessor.preprocess(data)

    // DT模型训练
    val params = new ClassParams
    val dtClassifier = new DecisionTreeClassifier()
      .setMinInfoGain(params.minInfoGain)
      .setMaxDepth(params.maxDepth)    //目前Spark只支持最大30层深度
      .setLabelCol("indexedLabel")
      .setFeaturesCol("features")

    val indexModel = pipeline.getStages(1).asInstanceOf[StringIndexerModel]
    //索引标签化
    val labelConverter = new IndexToString()
      .setLabels(indexModel.labels)
      .setInputCol(dtClassifier.getPredictionCol)
      .setOutputCol("predictedLabel")

    val stages = pipeline.getStages ++ Array(dtClassifier, labelConverter)
    pipeline.setStages(stages)

    val model = pipeline.fit(data)
    model.write.overwrite().save(modelPath)

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
