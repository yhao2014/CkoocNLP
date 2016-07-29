package nlp

import ml.feature.VectorizerUtils
import nlp.segment.SegmentUtils
import org.apache.spark.SparkContext
import org.apache.spark.ml.feature.CountVectorizerModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
  * Created by Administrator on 2016/7/29.
  */
class PreProcessUtils(
                       private var blockSize: Int,
                       private var minDocFreq: Int,
                       private var toTFIDF: Boolean,
                       private var vocabSize: Int) {

  def this() = this(blockSize = 48, minDocFreq = 2, toTFIDF = true, vocabSize = 5000)

  def setBlockSize(blockSize: Int): this.type = {
    require(blockSize > 0, "切分块大小必须大于0")
    this.blockSize = blockSize
    this
  }

  def setMinDocFreq(minDocFreq: Int): this.type = {
    require(minDocFreq > 0, "最小文档频率必须大于0")
    this.minDocFreq = minDocFreq
    this
  }

  def setVocabSize(vocabSize: Int): this.type = {
    require(vocabSize > 1000, "词汇表大小不小于1000")
    this.vocabSize = vocabSize
    this
  }

  def setToTFIDF(toTFIDF: Boolean): this.type = {
    this.toTFIDF = toTFIDF
    this
  }

  def getBlockSize: Int = this.blockSize

  def getMinDocFreq: Int = this.minDocFreq

  def getVocabSize: Int = this.vocabSize

  def getToTFIDF: Boolean = this.toTFIDF


  /**
    * 预处理运行函数，主要进行分词等数据清洗和向量化
    *
    * @param sc SparkContext
    * @param dataInPath 数据输入路径
    * @param vecModelPath  向量模型, idf数组 路径
    * @param mode 运行模式（train/test）, 如果是train模式，vecModelPath为保存路径；如果是test模式，vecModelPath为加载路径
    * @return 预处理后的数据
    */
  def run(sc: SparkContext, dataInPath: String, vecModelPath: String, mode: String): (RDD[(Long, Vector)], CountVectorizerModel) = {
    //--- 分词
    val preUtils = SegmentUtils("ckooc-ml/src/main/resources/segment.properties")
    val trainData = preUtils.getText(sc, dataInPath, blockSize).zipWithIndex().map(_.swap)
    val splitedRDD = preUtils.run(trainData)

    //--- 向量化
    val vectorizer = new VectorizerUtils()
      .setMinDocFreq(minDocFreq)
      .setToTFIDF(toTFIDF)
      .setVocabSize(vocabSize)

    var resultRDD: RDD[(Long, Vector)] = null
    var cvModel: CountVectorizerModel = null

    mode match {
      case "train" =>
        val vectorize: (RDD[LabeledPoint], CountVectorizerModel, Vector) = vectorizer.vectorize(splitedRDD)
        val vectorizedRDD = vectorize._1
        cvModel = vectorize._2
        val idf = vectorize._3

        resultRDD = vectorizedRDD.map(line => (line.label.toLong, line.features))
        vectorizer.save(vecModelPath, cvModel, idf)

      case "test" =>
        val loaded: (CountVectorizerModel, Vector) = vectorizer.load(vecModelPath)
        cvModel = loaded._1
        val idf = loaded._2

        val vectorizedRDD = vectorizer.vectorize(splitedRDD, cvModel, idf)
        resultRDD = vectorizedRDD.map(line => (line.label.toLong, line.features))

      case _ =>
        println("处理模式不正确，请输入：train/test")
        sys.exit(1)
    }

    (resultRDD, cvModel)
  }
}
