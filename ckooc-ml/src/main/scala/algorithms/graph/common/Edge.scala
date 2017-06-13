package algorithms.graph.common

/**
  * Created by Administrator on 2017/4/5.
  */
class Edge(
            private var id: String,
            private var src: String,
            private var tar: String,
            private var weight: Double = 1.0) extends Serializable {

  /** @group setParam */
  def setId(value: String): this.type = {
    this.id = value
    this
  }

  /** @group setParam */
  def setSrc(value: String): this.type = {
    this.src = value
    this
  }

  /** @group setParam */
  def setTar(value: String): this.type = {
    this.tar = value
    this
  }

  /** @group setParam */
  def setWeight(value: Double): this.type = {
    require(value > 0.0, "权重需大于0！")
    this.weight = value
    this
  }

  /** @group getParam */
  def getId: String = this.id

  /** @group getParam */
  def getSrc: String = this.src

  /** @group getParam */
  def getTar: String = this.tar

  /** @group getParam */
  def getWeight: Double = this.weight
}
