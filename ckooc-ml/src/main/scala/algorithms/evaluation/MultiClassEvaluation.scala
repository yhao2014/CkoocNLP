package algorithms.evaluation

import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.rdd.RDD

/**
  * 结果评估
  *
  * Created by yhao on 2017/3/9.
  */
object MultiClassEvaluation extends Serializable {

  /**
    * 多分类结果评估
    *
    * @param data 分类结果
    * @return (准确率, 召回率, F1)
    */
  def multiClassEvaluate(data: RDD[(Double, Double)]): (Double, Double, Double) = {
    val metrics = new MulticlassMetrics(data)
    val weightedPrecision = metrics.weightedPrecision
    val weightedRecall = metrics.weightedRecall
    val f1 = metrics.weightedFMeasure

    (weightedPrecision, weightedRecall, f1)
  }
}
