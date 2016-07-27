import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter}

import nlp.preprocess.PreProcessUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Administrator on 2016/7/25.
  */
object PreProcessDemo {
  def main(args: Array[String]) {
    Logger.getRootLogger.setLevel(Level.WARN)

    val conf = new SparkConf().setAppName("PreProcess").setMaster("local[4]")
    val sc = new SparkContext(conf)

    val preUtils = PreProcessUtils("ckooc-ml/src/main/resources/preprocess.properties")

    val args = Array("ckooc-ml/data/news/train", "ckooc-ml/data/preprocess/train")
//    val args = Array("ckooc-ml/data/news/test", "ckooc-ml/data/preprocess/test")

    val inPath = args(0)
    val outPath = args(1)

    val splitSize = 48

    val files = new File(inPath)

    if (files.isDirectory) {
      val fileList = files.listFiles()
      for (file <- fileList) {
        val filePath = file.getAbsolutePath
        val fileName = file.getName

        val data = preUtils.getText(sc, filePath, splitSize).zipWithIndex().map(_.swap)

        //分词等预处理，得到分词后的结果
        val splitedRDD = preUtils.run(data)

        //--本地测试使用：写入本地文件
        val result = splitedRDD.map(words => words._1 + "|" + words._2.mkString(" ")).collect()
        val bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outPath + File.separator + fileName)))
        for (line <- result) {
          bw.write(line + "\n")
        }
        bw.close()
      }
    } else {
      val data = preUtils.getText(sc, inPath, splitSize).zipWithIndex().map(_.swap)

      //分词等预处理，得到分词后的结果
      val splitedRDD = preUtils.run(data)

      //--本地测试使用：写入本地文件
      val result = splitedRDD.map(words => words._1 + "|" + words._2.mkString(" ")).collect()
      val bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outPath)))
      for (line <- result) {
        bw.write(line + "\n")
      }
      bw.close()
    }

    sc.stop()
  }
}
