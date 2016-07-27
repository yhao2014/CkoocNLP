package db

import java.io.{BufferedReader, FileInputStream, InputStreamReader}
import java.text.SimpleDateFormat

import db.utils.MySQLUtils

/**
  * Created by Administrator on 2016/7/20.
  */
object FileToMySQLDemo {
  def main(args: Array[String]) {
    val inPath = "E:/data/chinaNews/gn.txt"
    val seg = "\u00ef"

    val br = new BufferedReader(new InputStreamReader(new FileInputStream(inPath)))

    val mysqlUtils = new MySQLUtils()
      .setURL("jdbc:mysql://192.168.10.121:3306/spider?useUnicode=true&characterEncoding=UTF-8")

    val table = "cn_news_gn_tb"
    val fields = Array("category", "title", "start_date", "content")

    val conn = mysqlUtils.init()

    var count = 0
    var line = br.readLine()
    while (line != null) {
      count += 1

      val tokens = line.split(seg)
      if (tokens.length == 4) {
        val category = tokens(0)
        val title = tokens(1)
        val date = tokens(2)

        val toDate = new SimpleDateFormat("yyyy年MM月dd日 HH:mm")
        val toString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val formattedDate = toString.format(toDate.parse(date))

        val content = tokens(3)

        val values = Array(category, title, formattedDate, content)

        mysqlUtils.insert(conn, table, fields, values)

        if (count % 10000 == 0) {
          println("已插入：" + count / 10000 + " 万条...")
        }


        line = br.readLine()
      }
    }

    mysqlUtils.close()
    br.close()
  }

}
