package algorithms.graph

import algorithms.graph.common.{Edge, Node}

import scala.collection.{Set, mutable}

/**
  * Dijkstra算法
  * 功能：(无向图/有向图)单源最短路径查找
  * 流程：
  * 1. 初始化：
  *     ·划分已找到最短路径的点集U（点的flag设为false）和未找到最短路径的点集V（flag设为true）
  *     ·设置源节点flag = false，设置基准节点为源节点
  *     ·根据边信息，初始化源节点到各节点路径长度pathDist: Map[(String, Int)]，无直接连接的pathDist=Infinity，key为目标节点target
  *     ·根据边信息，初始化源节点到各节点的路径path: Map[(String, List[String])]，key为目标节点target
  * 2. 根据边从V中根据dist选择路径长度最短的节点node作为新的基准节点，设置node.flag = false，即已找到到node的最短路径
  * 3. 如果pathDist(node) + edge(node, target).weight < pathDist(target), 更新pathDist和path：
  *     ·pathDist(target) = pathDist(node) + edge(node, target).weight
  *     ·path(target) = path(node) + target
  * 4. 如果遍历次数小于节点数 - 1（不算根节点），重复2、3步
  *
  * Created by Administrator on 2017/4/5.
  */
class Dijkstra(
              val nodeList: List[Node],
              val edgeList: List[Edge],
              val isDirected: Boolean = false
              ) extends Serializable {

  private val path = new mutable.HashMap[String, List[String]]()    //存储路径
  private val pathDist = new mutable.HashMap[String, Double]()    //存储路径距离
  private val nodeMap = new mutable.HashMap[String, Node]()   //节点
  private val edgeMap = new mutable.HashMap[String, Edge]()   //边

  /**
    * 算法主逻辑
    * @param curr  基准节点
    * @return List[(最短路径, 路径长度)]
    */
  def run(curr: String): List[(List[String], Double)] = {

    this.init(curr: String)    //初始化

    val nodeSet: Set[String] = path.keySet
    var count = nodeSet.size
    while(count > 0) {
      var maxDist = Double.PositiveInfinity
      var nearest = ""
      for (key <- nodeSet) {
        if (nodeMap.get(key).nonEmpty && nodeMap(key).getFlag && pathDist(key) < maxDist) {
          maxDist = pathDist(key)
          nearest = key
        }
      }
      if (nearest.nonEmpty) update(nearest)
      count -= 1
    }

    var result: List[(List[String], Double)] = Nil
    for (key <- nodeSet) {
      val p: List[String] = path(key)
      val dist: Double = pathDist(key)
      result = result.:+((p, dist))
    }
    result
  }


  /**
    * 更新路径
    * 如果从源节点到node节点的路径长度pathDist(node) + node节点到目标节点的长度edge(node, target).weight < 从源节点到
    * target节点的路径长度pathDist(target)，则：
    *
    * 1. 更新target路径长度: pathDist(target) = pathDist(node) + edge(node, target).weight
    * 2. 更新从源节点到tartget节点路径: path(target) = path(node) + target
    *
    * @param node 当前基准节点
    */
  private def update(node: String): Unit = {
    nodeMap(node).setFlag(false)    //将当前基准节点标记记为false，即后续搜索不再考虑该节点，表明到该节点的最短路径已找到

    val keySet: Set[String] = path.keySet
    for (key <- keySet if !key.equals(node) && nodeMap(key).getFlag) {
      val maybeEdge: Option[Edge] = edgeMap.get(node + key)
      if (maybeEdge.nonEmpty) {
        val edgeWeight: Double = maybeEdge.get.getWeight
        if (edgeWeight + pathDist(node) < pathDist(key)) {
          pathDist.put(key, edgeWeight + pathDist(node))
          val newPath = path(node).:+(key)
          path.put(key, newPath)
        }
      }
    }

  }


  /** 初始化
    * 1. 初始化路径Map，每个key表示目的节点，List为从源节点到目的节点路径，初始化List(源节点, 目的节点)
    * 2. 初始化路径长度Map，每个key表示目的节点，value表示当前从源节点到该目的节点的cost，不可达时cost=Inf
    */
  private def init(start: String) = {

    //添加节点Map映射
    for (node <- nodeList) {
      if (nodeMap.get(node.getId).isEmpty) {
        node.setFlag(true)
        nodeMap.put(node.getId, node)
      }
    }

    //添加边Map映射
    for (edge <- edgeList) {
      val src = edge.getSrc
      val tar = edge.getTar
      if (isDirected) {
        edgeMap.put(src + tar, edge)
      } else {
        edgeMap.put(src + tar, edge)
        edgeMap.put(tar + src, edge)
      }
    }

    nodeMap(start).setFlag(false)   //将源节点标记记为false，即后续搜索不再考虑该节点

    val nodeIds: Set[String] = nodeList.map(_.getId).toSet
    for (nodeId <- nodeIds if !nodeId.equals(start)) {
      path.put(nodeId, List(start, nodeId))   //添加源节点和目标节点初始路径，该初始路径仅包含两个节点

      var dist = Double.PositiveInfinity
      val maybeEdge: Option[Edge] = edgeMap.get(start + nodeId)   //获取与源节点相连的边
      if (maybeEdge.nonEmpty) dist = maybeEdge.get.getWeight
      pathDist.put(nodeId, dist)    //初始化初始路径距离，仅与源节点直接相连的节点有值，其他为无穷
    }
  }
}