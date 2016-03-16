/**
  * Created by james on 14/03/16.
  */

import java.io._
import java.net.URL

import scala.util.Try
import scala.xml._
import org.apache.commons.io.{FilenameUtils, FileUtils}

//scrapes images from nik.bot.ru with particular flags, then downloads them into an output folder.
object ChanScraper {


  def main(args: Array[String]): Unit = {
    val boards = "4chan_s,reddit,7chan_wp,8chan__w,iichan_w,8chan_wg,dbrchan_w,2ch_wp"
    val images = for (day <- 1 until 28; month <- 1 to args(0).toInt; str: String = "%02d.%02d.%04d".format(month, day, args(1).toInt))
      yield "%02d.%02d.%04d".format(month, day, 2015) -> dailyScraper(str, boards)


    images.foreach((tuple: (String, List[String])) => {
      val dir = new File(s"${args(2)}/nikbot/images/${tuple._1}")
      dir.mkdirs()

      val csvFile = new File(s"${dir.getAbsolutePath}/${tuple._1}.csv")
      val bw = new BufferedWriter(new FileWriter(csvFile, true))

      tuple._2.foreach((link: String) => {
        bw.write(s"$link,")
        bw.newLine()
      })
      bw.close()

      tuple._2.foreach((link: String) => {
        val file = new File(s"${dir.getAbsolutePath}/${FilenameUtils.getName(link)}")
        Try(FileUtils.copyURLToFile(new URL(link), file, 3000, 3000))
      })
    })
  }

  def dailyScraper(date: String, boards:String): List[String]  = {
    val url = s"https://nik.bot.nu/rss.fu?req=mode:rss%20board:" +
      s"$boards%" +
      s"20safe:adult%" +
      s"20sort:datedesc%" +
      s"20date:$date"
    val doc = XML.load(url)

    (doc \ "channel" \ "item" ).map((item: (Node)) => {
      val content = (item \ "content").head
      (content \ "@url").text
    }).toList
  }
}