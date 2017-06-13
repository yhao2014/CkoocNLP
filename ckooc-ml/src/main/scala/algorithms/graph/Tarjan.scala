package algorithms.graph

import algorithms.graph.common.{Edge, Node}

import scala.collection.mutable

/**
  * Tarjan算法
  * 功能：(无向图)割点查找，目前只支持单联通图割点查找
  * 流程：
  * 1. 初始化：
  *     ·根据edgeList添加各节点的children
  *     ·初始化遍历次序号计数器
  * 2. 设置当前节点的次序号，最早祖先次序号low = num = count；设置基准节点node的flag = false，表示已访问；count自增
  * 3. 遍历node的children，当前孩子为child：
  *     ·如果孩子未访问(flag为true)：
  *         ·node的子树个数 subTreeNum += 1
  *         ·child的父节点设为node
  *         ·以child为新基准节点递归第2,3步
  *         ·判定：1)如果当前基准节点为根节点，并且子树个数 > 1，该节点为割点；2)如果为非根节点，且low[child] > num[node]，则该节点为割点
  *         ·更新node的low = min(low[node], low[child])
  *     ·如果孩子已访问：
  *         ·更新node的low = min(low[node], num[child])
  *
  * Created by yhao on 2017/4/12.
  */
class Tarjan(val nodeList: List[Node],
             val edgeList: List[Edge],
             val isDirected: Boolean = false) extends Serializable {

  val nodeMap = new mutable.HashMap[String, Node]()   //节点

  private var count = 1   //遍历次序号计数器
  private var arcNodes: List[String] = Nil

  def getArcNodes: List[String] = this.arcNodes
  def geNodes: List[Node] = this.nodeMap.values.toList

  def run(start: String): Unit = {
    val starNode: Node = nodeMap(start)
    var subTreeNum: Int = 0   //当前节点子树个数

    starNode.setNum(count)
    starNode.setLow(starNode.getNum)
    starNode.setFlag(false)
    count += 1

    for (child <- starNode.getChildren) {
      val childNode: Node = nodeMap(child)

      if (childNode.getFlag) {
        subTreeNum += 1
        childNode.setParent(start)
        run(child)
        if (starNode.getParent.isEmpty && subTreeNum > 1 && !arcNodes.contains(start)) arcNodes = arcNodes.:+(start)
        if (childNode.getLow >= starNode.getNum && starNode.getNum != 1 && !arcNodes.contains(start)) arcNodes = arcNodes.:+(start)

        starNode.setLow(Math.min(starNode.getLow, childNode.getLow))
      } else {
        if (starNode.getParent.nonEmpty && !starNode.getParent.equals(child)) {
          starNode.setLow(Math.min(starNode.getLow, childNode.getNum))
        }
      }
    }
  }


  /**
    * 初始化
    */
  def init(): Unit = {
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

    count = 1   //初始化计数器
  }
}