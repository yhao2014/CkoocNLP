package db.utils

import java.io.Serializable
import java.sql.{Connection, DriverManager, PreparedStatement}


/**
  * Created by Administrator on 2016/7/14.
  */
class MySQLUtils(
                private var url: String,
                private var name: String,
                private var user: String,
                private var password: String
                ) extends Serializable {

  def this() = this(url = "jdbc:mysql://127.0.0.1:3306/test", name = "com.mysql.jdbc.Driver", user = "mysql", password = "Mysql123!")


  def setURL(url: String): this.type = {
    this.url = url
    this
  }


  def setName(name: String): this.type = {
    this.name = name
    this
  }


  def setUser(user: String): this.type = {
    this.user = user
    this
  }


  def setPasswd(passwd: String): this.type = {
    this.password = passwd
    this
  }


  def getURL: String = this.url

  def getName: String = this.name

  def getUser: String = this.user

  def getPasswd: String = this.password

  var conn: Connection = null


  def init(): Connection ={
    Class.forName(name)
    conn = DriverManager.getConnection(url, user, password)
    conn
  }


  def insert(conn: Connection, table: String, fields: Array[String], values: Array[String]) = {
    if (fields.length != values.length) {
      println("字段和数值个数不匹配！")
      sys.exit(1)
    }

    val randArray = new Array[String](values.length)
    for (i <- values.indices) {
      randArray(i) = "?"
    }

    val sql = "insert into " + table + "(" + fields.mkString(",") + ") values (" + randArray.mkString(",") + ")"

    val preStmt: PreparedStatement = conn.prepareStatement(sql)
    for (i <- values.indices) {
      preStmt.setString(i + 1, values(i))
    }

    preStmt.execute()
    preStmt.close()
  }


  def close(): Unit ={
    conn.close()
  }
}

object MySQLUtil {
  def main(args: Array[String]): Unit = {
    val mysqlUtils = new MySQLUtils()
      .setURL("jdbc:mysql://192.168.10.121:3306/spider")


    mysqlUtils.close()

    println()
  }
}
