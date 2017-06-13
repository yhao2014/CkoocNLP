package algorithms.graph.common

/**
  * Created by Administrator on 2017/4/5.
  */
class Node(
            private var id: String = "",
            private var weight: Double = 1.0,
            private var flag: Boolean = true,
            private var num: Int = 1,
            private var low: Int = 1,
            private var parent: String = "",
            private var children: List[String] = Nil) extends Serializable {

  /** @group setParam */
  def setId(value: String): this.type = {
    this.id = value
    this
  }

  /** @group setParam */
  def setWeight(value: Double): this.type = {
    this.weight = value
    this
  }

  /** @group setParam */
  def setFlag(value: Boolean): this.type = {
    this.flag = value
    this
  }

  /** @group setParam */
  def setNum(value: Int): this.type = {
    require(value > 0, "节点序号必须大于0")
    this.num = value
    this
  }

  /** @group setParam */
  def setLow(value: Int): this.type = {
    require(value > 0, "最早祖先节点序号必须大于0")
    this.low = value
    this
  }

  /** @group setParam */
  def setParent(value: String): this.type = {
    this.parent = value
    this
  }

  /** @group setParam */
  def setChildren(value: List[String]): this.type = {
    this.children = value
    this
  }


  /** @group getParam */
  def getId: String = this.id

  /** @group getParam */
  def getWeight: Double = this.weight

  /** @group getParam */
  def getFlag: Boolean = this.flag

  /** @group getParam */
  def getNum: Int = this.num

  /** @group getParam */
  def getLow: Int = this.low

  /** @group getParam */
  def getParent: String = this.parent

  /** @group getParam */
  def getChildren: List[String] = this.children
}
