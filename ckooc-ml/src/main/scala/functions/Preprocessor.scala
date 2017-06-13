package functions

import config.paramconf.PreprocessParams
import functions.clean.Cleaner
import functions.segment.Segmenter
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{CountVectorizer, IDF, StopWordsRemover, StringIndexer}
import org.apache.spark.sql.DataFrame

/**
  * Created by yhao on 2017/3/15.
  */
class Preprocessor extends Serializable {

  /**
    * 预处理过程，包括数据清洗、标签索引化、分词、去除停用词、向量化、转换tf-idf
    *
    * @param data 输入数据，注意必须包含content列，作为预处理的对象
    * @return pipeline
    */
  def preprocess(data: DataFrame): Pipeline = {
    val spark = data.sparkSession
    val params = new PreprocessParams

    val indexModel = new StringIndexer()
      .setHandleInvalid(params.handleInvalid)
      .setInputCol("label")
      .setOutputCol("indexedLabel")
      .fit(data)

    val cleaner = new Cleaner()
      .setFanJian(params.fanjian)
      .setQuanBan(params.quanban)
      .setMinLineLen(params.minLineLen)
      .setInputCol("content")
      .setOutputCol("cleand")

    val segmenter = new Segmenter()
      .isAddNature(params.addNature)
      .isDelEn(params.delEn)
      .isDelNum(params.delNum)
      .isNatureFilter(params.natureFilter)
      .setMinTermLen(params.minTermLen)
      .setMinTermNum(params.minTermNum)
      .setSegType(params.segmentType)
      .setInputCol(cleaner.getOutputCol)
      .setOutputCol("segmented")

    val stopwords = spark.sparkContext.textFile(params.stopwordFilePath).collect()
    val remover = new StopWordsRemover()
      .setStopWords(stopwords)
      .setInputCol(segmenter.getOutputCol)
      .setOutputCol("removed")

    val vectorizer = new CountVectorizer()
      .setMinTF(params.minTF)
      .setVocabSize(params.vocabSize)
      .setInputCol(remover.getOutputCol)
      .setOutputCol("vectorized")

    val idf = new IDF()
      .setMinDocFreq(params.minDocFreq)
      .setInputCol(vectorizer.getOutputCol)
      .setOutputCol("features")

    val stages = Array(cleaner, indexModel, segmenter, remover, vectorizer, idf)
    new Pipeline().setStages(stages)
  }
}
