package applications.mining

import config.paramconf.ClassParams
import functions.Preprocessor
import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature._
import org.apache.spark.sql.SparkSession

/**
  * 基于LR的新闻多分类模型训练
  *
  * Created by yhao on 2017/3/14.
  */
object TrainNewsClassWithLRDemo extends Serializable {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.WARN)

    val spark = SparkSession
      .builder
      .master("local[2]")
      .appName("train news with LR Demo")
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

    //LR模型训练
    val params = new ClassParams
    val logisticRegression = new LogisticRegression()
      .setTol(params.converTol)
      .setMaxIter(params.maxIteration)
      .setRegParam(params.regParam)
      .setElasticNetParam(params.elasticNetParam)
      .setLabelCol("indexedLabel")
      .setFeaturesCol("features")

    val indexModel = pipeline.getStages(1).asInstanceOf[StringIndexerModel]
    //索引标签化
    val labelConverter = new IndexToString()
      .setLabels(indexModel.labels)
      .setInputCol(logisticRegression.getPredictionCol)
      .setOutputCol("predictedLabel")

    val stages = pipeline.getStages ++ Array(logisticRegression, labelConverter)
    pipeline.setStages(stages)

    val model = pipeline.fit(data)
    model.write.overwrite().save(params.LRModelPath)

    data.unpersist()
    spark.stop()
  }
}
