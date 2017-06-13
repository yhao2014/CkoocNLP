package algorithms.graph

import algorithms.graph.common.{Edge, Node}
import com.alibaba.fastjson.{JSONObject, JSON, JSONArray}

import scala.collection.{Set, mutable}
import scala.io.Source
import scala.util.control.Breaks._

/**
  * Created by Administrator on 2017/4/13.
  */
class NodeRank(val nodeList: List[Node],
               val edgeList: List[Edge],
               val isDirected: Boolean = false) extends Serializable {

  private val nodeMap = new mutable.HashMap[String, Node]()
  //节点
  private final val MAX_ITR = 200
  //最大迭代次数
  private final val d = 0.85
  //阻尼系数，一般设为0.85
  private final val MIN_DIFF = 1e-3 //迭代停止的最小误差

  def run(): List[(String, Double)] = {
    this.init()

    var scores: mutable.HashMap[String, Double] = new mutable.HashMap[String, Double]()

    for (_ <- 0 until MAX_ITR) {
      var max_diff = 0.0
      val newScores: mutable.HashMap[String, Double] = new mutable.HashMap[String, Double]()

      val nodes: Set[String] = nodeMap.keySet
      for (node <- nodes) {
        newScores.put(node, 1 - d)

        val children: List[String] = nodeMap(node).getChildren
        for (child <- children if !node.equals(child) && nodeMap(child).getChildren.nonEmpty) {
          val weight = nodeMap(child).getWeight * 10
          var nodeScore: Double = newScores(node)
          if (scores.get(child).nonEmpty) nodeScore += 1.0 * d / weight * scores(child)
          else nodeScore += 1.0 * d / weight * 0
          newScores.put(node, nodeScore)
        }

        if (scores.get(node).nonEmpty) max_diff = Math.max(max_diff, Math.abs(newScores(node) - scores(node)))
        else max_diff = Math.max(max_diff, newScores(node) - 0)
      }

      scores = newScores
      breakable {if (max_diff < MIN_DIFF) break()}
    }

    //标准化
    val scoreList = scores.toList.sortWith((record1, record2) => record1._2 > record2._2)
    val head = scoreList.head
    scoreList.map(record => (record._1, record._2 / head._2))
  }

  /**
    * 初始化
    */
  private def init() = {
    //添加节点children，将节点flag初始化，并将节点添加到nodeMap
    for (edge <- edgeList) {
      val src = edge.getSrc
      val tar = edge.getTar

      val srcNode: Node = nodeMap.getOrElse(src, new Node())
      val tarNode: Node = nodeMap.getOrElse(tar, new Node())

      if (!isDirected) {
        val tarChildren: List[String] = tarNode.getChildren
        tarNode.setChildren(tarChildren.:+(src)).setFlag(true)
        nodeMap.put(tar, tarNode)
      }

      val srcChildren: List[String] = srcNode.getChildren
      srcNode.setChildren(srcChildren.:+(tar)).setFlag(true)
      nodeMap.put(src, srcNode)
    }

    //添加孤立节点到nodeMap
    for (node <- nodeList) {
      if (nodeMap.get(node.getId).isEmpty) {
        node.setFlag(true)
        nodeMap.put(node.getId, node)
      }
    }
  }
}