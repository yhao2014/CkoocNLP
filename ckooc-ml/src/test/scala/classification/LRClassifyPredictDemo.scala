package classification

import ml.classification.LRClassifyUtils
import ml.feature.VectorizerUtils
import nlp.segment.SegmentUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Administrator on 2016/7/27.
  */
object LRClassifyPredictDemo {
  def main(args: Array[String]) {
    Logger.getRootLogger.setLevel(Level.WARN)

    val conf = new SparkConf().setAppName("LRClassifyPredict").setMaster("local[4]")
    val sc = new SparkContext(conf)

    val args = Array("ckooc-ml/models/vectorize", "ckooc-ml/models/classification/lr")

    val data = "据美国Scout网站报道：美空军F-35战机飞行员称有信心驾驶这款联合攻击机迎战任何敌机，包括俄罗斯和中国的第五代隐形战机。美军官员也认为，F-35联合攻击机可以利用传感器、武器系统和计算机技术来摧毁俄罗斯和中国的第五代隐形战机。\n亚利桑那州卢克空军基地第56战斗机联队中校马特·海登称，“我如果能在战术环境中驾驶F-35战机，就不会还对其他战机再生好感。驾驶那款战机执行任何任务都能让我感到舒适，信心倍增。”而且，多位F-35战机飞行员一直明确表示，这款多功能战机在实战中可胜过其他任何机型。\n海登曾明确指出，他并未在模拟任务中驾驶F-35战机对抗过俄罗斯正在开发的“苏霍伊”T-50 PAK FA或中国的歼-31等第五代隐形战机。虽然他本人并不知道中俄第五代战机的所有技术和能力，但是在声明中却毫不含糊地表示对F-35充满了信心。"
    val vecModelPath = args(0)
    val lrModelPath = args(1)

    val classNum = 6


    //--- 分词
    val preUtils = SegmentUtils("ckooc-ml/src/main/resources/segment.properties")
    val lrUtils = new LRClassifyUtils(classNum)

    val textRDD = sc.parallelize(Seq(data)).map(line => (-1L, line))
    val splitedRDD = preUtils.run(textRDD)


    //--- 向量化
    val minDocFreq = 2 //最小文档频率阀值
    val toTFIDF = true //是否将TF转化为TF-IDF
    val vocabSize = 5000 //词汇表大小

    val vectorizer = new VectorizerUtils()
      .setMinDocFreq(minDocFreq)
      .setToTFIDF(toTFIDF)
      .setVocabSize(vocabSize)

    val (cvModel, idf) = vectorizer.load(vecModelPath)
    val vectorizedRDD = vectorizer.vectorize(splitedRDD, cvModel, idf)


    //--- 测试LR分类
    val (model, categoryMap) = lrUtils.load(sc, lrModelPath)
    val classify = lrUtils.predict(vectorizedRDD, model)

    println(classify)
    println(categoryMap.get(classify.toLong).get)

    sc.stop()
  }
}
