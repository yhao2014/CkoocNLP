package ml.classification

import nlp.clean.Cleaner
import nlp.segment.Segmenter
import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{CountVectorizer, IndexToString, StringIndexer}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by yhao on 2017/3/14.
  */
object TrainNewsMultiClassWithLR extends Serializable {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.WARN)

    val spark = SparkSession
      .builder
      .master("local[2]")
      .appName("train news multi class with LR")
      .getOrCreate()

    val filePath = "ckooc-ml/data/classnews/train"
    val modelPath = "ckooc-ml/models/news-multi-class-model"

    val data = clean(filePath, spark)

    val cleaner = new Cleaner()
      .setFanJan("f2j")
      .setQuanBan("q2b")
      .setMinLineLen(15)
      .setInputCol("content")
      .setOutputCol("cleand")

    val indexer = new StringIndexer()
      .setInputCol("label")
      .setOutputCol("indexedLabel")
      .fit(data)

    val segmenter = new Segmenter()
      .isAddNature(false)
      .isDelEn(true)
      .isDelNum(true)
      .setMinTermLen(2)
      .setMinTermNum(10)
      .setSegType("StandardSegment")
      .setInputCol(cleaner.getOutputCol)
      .setOutputCol("segmented")

    val vectorizer = new CountVectorizer()
      .setMinTF(3)
      .setVocabSize(15000)
      .setInputCol(segmenter.getOutputCol)
      .setOutputCol("features")

    val logisticRegression = new LogisticRegression()
      .setTol(1E-6)
      .setMaxIter(100)
      .setRegParam(0.2)
      .setElasticNetParam(0.05)
      .setLabelCol(indexer.getOutputCol)
      .setFeaturesCol(vectorizer.getOutputCol)

    val labelConverter = new IndexToString()
      .setInputCol(logisticRegression.getPredictionCol)
      .setOutputCol("predictedLabel")
      .setLabels(indexer.labels)

    val stages = Array(cleaner, indexer, segmenter, vectorizer, logisticRegression, labelConverter)
    val pipeline = new Pipeline().setStages(stages)
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
