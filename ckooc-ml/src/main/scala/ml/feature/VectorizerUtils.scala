package ml.feature

import java.io._

import org.apache.spark.ml.feature.{CountVectorizer, CountVectorizerModel}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext}

import scala.reflect.io.File

/**
  * Created by Administrator on 2016/7/25.
  */
class VectorizerUtils(
                       private var minDocFreq: Int,
                       private var vocabSize: Int,
                       private var toTFIDF: Boolean
                     ) extends Serializable {

  def this() = this(minDocFreq = 1, vocabSize = 5000, toTFIDF = true)

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

  def getMinDocFreq: Int = this.minDocFreq

  def getVocabSize: Int = this.vocabSize

  def getToTFIDF: Boolean = this.toTFIDF


  /**
    * 生成向量模型
    *
    * @param df        DataFrame
    * @param vocabSize 词汇表大小
    * @return 向量模型
    */
  def genCvModel(df: DataFrame, vocabSize: Int): CountVectorizerModel = {
    val cvModel = new CountVectorizer()
      .setInputCol("tokens")
      .setOutputCol("features")
      .setVocabSize(vocabSize)
      .fit(df)

    cvModel
  }


  /**
    * 将词频数据转化为特征LabeledPoint
    *
    * @param df      DataFrame数据
    * @param cvModel 向量模型
    * @return LabeledPoint
    */
  def toTFLP(df: DataFrame, cvModel: CountVectorizerModel): RDD[LabeledPoint] = {
    val documents = cvModel.transform(df)
      .select("id", "features")
      .map { case Row(id: Long, features: Vector) => LabeledPoint(id, features) }

    documents
  }


  /**
    * 根据特征向量生成tf-idf模型
    *
    * @param documents 文档LabeledPoint
    * @return IDFModel
    */
  def genIDFModel(documents: RDD[LabeledPoint]): IDFModel = {
    val features = documents.map(_.features)

    val idf = new IDF(minDocFreq)
    idf.fit(features)
  }


  /**
    * 将词频LabeledPoint转化为TF-IDF的LabeledPoint
    *
    * @param documents 词频LabeledPoint
    * @param idfModel  TF-IDF模型
    * @return TF-IDF的LabeledPoint
    */
  def toTFIDFLP(documents: RDD[LabeledPoint], idfModel: IDFModel): RDD[LabeledPoint] = {
    val ids = documents.map(_.label)
    val allFeatures = documents.map(_.features)

    val tfidf = idfModel.transform(allFeatures)

    val tfidfDocs = ids.zip(tfidf).map { case (id, features) => LabeledPoint(id, features) }

    tfidfDocs
  }


  /**
    * 已分词中文数据向量化
    *
    * @param data 已分词数据
    * @return
    */
  def vectorize(data: RDD[(Long, scala.Seq[String])]): (RDD[LabeledPoint], CountVectorizerModel, Vector) = {
    val sc = data.context
    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    val tokenDF = data.toDF("id", "tokens")

    var startTime = System.nanoTime()

    //生成cvModel
    val cvModel = genCvModel(tokenDF, vocabSize)
    val cvTime = (System.nanoTime() - startTime) / 1e9
    startTime = System.nanoTime()
    println(s"生成cvModel完成！\n\t 耗时: $cvTime sec\n")

    //转化为LabeledPoint
    var tokensLP = toTFLP(tokenDF, cvModel)
    val lpTime = (System.nanoTime() - startTime) / 1e9
    startTime = System.nanoTime()
    println(s"转化LabeledPoint完成！\n\t 耗时: $lpTime sec\n")

    //转换为TFIDF
    var idfModel: IDFModel = null
    if (toTFIDF) {
      //生成idfModel
      idfModel = genIDFModel(tokensLP)
      tokensLP = toTFIDFLP(tokensLP, idfModel)
    }
    val idfTime = (System.nanoTime() - startTime) / 1e9
    println(s"转化TFIDF完成！\n\t 耗时: $idfTime sec\n")

    (tokensLP, cvModel, idfModel.idf)
  }


  def vectorize(data: RDD[(Long, scala.Seq[String])], cvModel: CountVectorizerModel, idf: Vector): RDD[LabeledPoint] = {
    val sc = data.context
    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    val tokenDF = data.toDF("id", "tokens")

    //转化为LabeledPoint
    var tokensLP = toTFLP(tokenDF, cvModel)

    if (toTFIDF) {
      val idfModel = new IDFModel(idf)
      tokensLP = toTFIDFLP(tokensLP, idfModel)
    }

    tokensLP
  }


  /**
    * 保存cvModel向量模型和idf向量
    *
    * @param modelPath 保存路径
    * @param cvModel   cvModel
    * @param idf       idf向量
    */
  def save(modelPath: String, cvModel: CountVectorizerModel, idf: Vector): Unit = {
    val bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(modelPath + File.separator + "IDF")))
    bw.write(idf.toArray.mkString(","))
    cvModel.save(modelPath + File.separator + "cvModel")

    bw.close()
  }


  /**
    * 加载cvModel向量模型和idf向量
    *
    * @param modelPath 模型保存路径
    * @return (cvModel, idf)
    */
  def load(modelPath: String): (CountVectorizerModel, Vector) = {
    val br = new BufferedReader(new InputStreamReader(new FileInputStream(modelPath + File.separator + "IDF")))
    val idfArray = br.readLine().split(",").map(_.toDouble)
    val idf = Vectors.dense(idfArray)
    val cvModel = CountVectorizerModel.load(modelPath + File.separator + "cvModel")

    br.close()
    (cvModel, idf)
  }
}
