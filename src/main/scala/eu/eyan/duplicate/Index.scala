package eu.eyan.duplicate

import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.io.FilePlus
import java.io.FileWriter
import java.io.BufferedWriter
import java.io.File

case class Fil(file: File, isFile: Boolean, size: Long){
  def sizeReadable = 
    if (size >= 1000*1000*1000) (size/1000/1000/1000)+" GB"
    else if (size >= 1000*1000) (size/1000/1000)+" MB"
    else if (size >= 1000) (size/1000)+" kB"
    else size.toString
}

object Index {

  def create(locationToIndex: String, locationToStore: String, finished: () => Unit) = {
    if (locationToIndex.asFile.isDirectory) {
      val tree = FilePlus.fileTrees(locationToIndex)
      val bw = new BufferedWriter(new FileWriter(locationToStore + "\\" + locationToIndex.toSafeFileName))
      tree.foreach { f => println(f); bw.write(f + ";" + f.length + ";" + f.isFile + "\r\n") }
      bw.close
    }

    finished()
  }

  def load(indexLocation: String) = indexLocation.linesFromFile map lineToFil

  private def lineToFil(line: String) = {
    val line3 = line.split(";")
    Fil(new File(line3(0)), line3(2).toBoolean, line3(1).toLong)
  }
}