package config.paramconf

import config.Conf

import scala.collection.mutable

/**
  * 分类训练/测试使用参数
  *
  * Created by yhao on 2017/3/7.
  */
class ClassParams extends Serializable {
  val kvMap: mutable.LinkedHashMap[String, String] = Conf.loadConf("../conf/classification.properties")

  val maxIteration: Int = kvMap.getOrElse("maxIteration", "80").toInt    //模型最大迭代次数
  val regParam: Double = kvMap.getOrElse("regParam", "0.1").toDouble   //正则化项参数
  val elasticNetParam: Double = kvMap.getOrElse("elasticNetParam", "0.0").toDouble   //L1范式比例, L1/(L1 + L2)
  val converTol: Double = kvMap.getOrElse("converTol", "1E-6").toDouble    //模型收敛阈值

  val minInfoGain: Double = kvMap.getOrElse("minInfoGain", "0.0").toDouble    //最小信息增益阈值
  val maxDepth: Int = kvMap.getOrElse("maxDepth", "10").toInt    //决策树最大深度

  val LRModelPath: String = kvMap.getOrElse("LRModelPath", "models/classification/news-class-LR-model")    //LR模型保存路径
  val DTModelPath: String = kvMap.getOrElse("DTModelPath", "models/classification/news-class-DT-model")   //决策树模型保存路径
}
