package applications.mining

import config.paramconf.ClassParams
import functions.Preprocessor
import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.feature._
import org.apache.spark.sql.SparkSession

/**
  * 基于DT的新闻多分类模型训练
  *
  * Created by yhao on 2017/3/15.
  */
object TrainNewsClassWithDTDemo {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.WARN)

    val spark = SparkSession
      .builder
      .master("local[2]")
      .appName("train news with DT Demo")
      .getOrCreate()

    val args = Array("ckooc-ml/data/classnews/train")
    val filePath = args(0)

    import spark.implicits._
    val data = spark.sparkContext.textFile(filePath).flatMap { line =>
      val tokens: Array[String] = line.split("\u00ef")
      if (tokens.length > 3) Some((tokens(0), tokens(1), tokens(2), tokens(3))) else None
    }.toDF("label", "title", "time", "content")
    data.persist()

    val preprocessor = new Preprocessor
    val pipeline = preprocessor.preprocess(data)

    // DT模型训练
    val params = new ClassParams
    val dtClassifier = new DecisionTreeClassifier()
      .setMinInfoGain(params.minInfoGain)
      .setMaxDepth(params.maxDepth) //目前Spark只支持最大30层深度
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
    model.write.overwrite().save(params.DTModelPath)

    data.unpersist()
    spark.stop()
  }
}
