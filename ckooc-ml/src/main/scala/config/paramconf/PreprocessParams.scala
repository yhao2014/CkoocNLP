package config.paramconf

import config.Conf

import scala.collection.mutable

/**
  * 预处理过程使用参数
  *
  * Created by yhao on 2017/3/8.
  */
class PreprocessParams extends Serializable  {
  val kvMap: mutable.LinkedHashMap[String, String] = Conf.loadConf("../conf/preprocess.properties")

  val fanjian: String = kvMap.getOrElse("fanjian", "f2j")   //繁简转换
  val quanban: String = kvMap.getOrElse("quanban", "q2b")   //全半角转换
  val minLineLen: Int = kvMap.getOrElse("minLineLen", "1").toInt    //最短行长度

  val handleInvalid: String = kvMap.getOrElse("handleInvalid", "error")   //如何处理非法行

  val segmentType: String = kvMap.getOrElse("segType", "StandardSegment")    //分词方式
  val delNum: Boolean = kvMap.getOrElse("delNum", "false").toBoolean     //是否去除数字
  val delEn: Boolean = kvMap.getOrElse("delEn", "false").toBoolean    //是否去除英语单词
  val addNature: Boolean = kvMap.getOrElse("addNature", "false").toBoolean   //是否添加词性
  val natureFilter: Boolean = kvMap.getOrElse("natureFilter", "false").toBoolean    //是否按词性过滤
  val minTermLen: Int = kvMap.getOrElse("minTermLen", "1").toInt      //最小词长度
  val minTermNum: Int = kvMap.getOrElse("minTermNum", "3").toInt      //行最小词数
  val minTF: Double = kvMap.getOrElse("minTF", "1").toDouble    //最小词频
  val vocabSize: Int = kvMap.getOrElse("vocabSize", "10000").toInt   //特征词汇表大小
  val minDocFreq: Int = kvMap.getOrElse("minDocFreq", "1").toInt    //最小文档频率

  val stopwordFilePath: String = kvMap.getOrElse("stopwordFilePath", "ckooc-ml/dictionaries/hanlp/data/dictionary/stopwords.txt")    //停用词表路径
}
