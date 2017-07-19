package graph


import algorithms.graph.{Dijkstra, NodeRank, Tarjan}
import algorithms.graph.common.{Edge, Node}
import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import org.junit.Test

import scala.io.Source

/**
  * Created by yhao on 2017/6/13.
  */
class RelativeNetTest {

  val startId: String = "v0001"
  val graphDataPath: String = "E:/projects/CkoocNLP/ckooc-ml/data/json/graph1.json"
  val json: String = Source.fromFile(graphDataPath, "utf-8").mkString

  /**
    * Dijkstra单源最短路径算法测试
    */
  @Test
  def testDijkstra(): Unit = {
    val (nodeList, edgeList) = parseJson(json)

    val dijkstra: Dijkstra = new Dijkstra(nodeList, edgeList)
    val paths: List[(List[String], Double)] = dijkstra.run(startId)

    paths.sortBy(_._1.last).foreach{case (path: List[String], dist: Double) =>
      val pathStr = path.toArray.mkString(" --> ")
      println(dist + "   \t" + pathStr)
    }
  }

  /**
    * NodeRank关系图结点重要度计算算法测试
    */
  @Test
  def testNodeRank(): Unit = {
    val (nodeList, edgeList) = parseJson(json)

    val rank: NodeRank = new NodeRank(nodeList, edgeList)
    val scores: List[(String, Double)] = rank.run()
    scores.foreach(record => println(record._1 + "\t" + record._2))
  }


  /**
    * Tarjan关系图割点计算算法测试
    */
  @Test
  def testTarjan(): Unit = {
    val (nodeList, edgeList) = parseJson(json)

    val tarjan: Tarjan = new Tarjan(nodeList, edgeList)
    tarjan.init()
    tarjan.run(startId)

    println("割点: " + tarjan.getArcNodes)

    tarjan.nodeMap.foreach{record =>
      val node: String = record._1
      val num: Int = record._2.getNum
      val low: Int = record._2.getLow
      println(List(node, num, low).mkString("\t"))
    }
  }


  /** 解析JSON数据 */
  def parseJson(json: String): (List[Node], List[Edge]) = {
    val edges: JSONArray = JSON.parseObject(json).getJSONArray("edges")
    val nodes: JSONArray = JSON.parseObject(json).getJSONArray("nodes")

    var nodeList: List[Node] = List()
    var edgeList: List[Edge] = List()

    for(i <- 0 until edges.size()) {
      val edge: JSONObject = edges.getJSONObject(i)
      val edgeId: String = edge.getString("id")
      val srcId: String = edge.getString("src")
      val tarId: String = edge.getString("tar")
      val edgeWeight: Double = edge.getDouble("weight")

      edgeList = edgeList.:+(new Edge(edgeId, srcId, tarId, edgeWeight))
    }

    for(i <- 0 until nodes.size()) {
      val node: JSONObject = nodes.getJSONObject(i)
      val nodeId: String = node.getString("id")
      val nodeWeight: Double = node.getDouble("weight")

      nodeList = nodeList.:+(new Node(nodeId, nodeWeight))
    }

    (nodeList, edgeList)
  }
}
