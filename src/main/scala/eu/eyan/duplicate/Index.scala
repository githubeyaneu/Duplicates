package eu.eyan.duplicate

import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.io.FilePlus
import java.io.FileWriter
import java.io.BufferedWriter
import java.io.File
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import scala.util.Try
import eu.eyan.util.io.FilePlus.FilePlusImplicit

case class Fil(file: File, isFile: Boolean, size: Long, hash: String) {
	def sizeReadable =
			if (size >= 1000 * 1000 * 1000) (size / 1000 / 1000 / 1000) + " GB"
			else if (size >= 1000 * 1000) (size / 1000 / 1000) + " MB"
			else if (size >= 1000) (size / 1000) + " kB"
			else size.toString
}
object Fil{
	def lineToFil(line: String) = {
		val lineValues = Try{line.split(";").toList}.getOrElse(List("", "0", "false", "x"))
		
		val file = Try{new File(lineValues(0))}.getOrElse({println("Wrong file in: "+lineValues(0)+" "+lineValues); null})
		val isFile = Try{lineValues(2).toBoolean}.getOrElse({println("Wrong isFile in: "+lineValues(2)+" "+lineValues); false})
		val size = Try{lineValues(1).toLong}.getOrElse({println("Wrong length in: "+lineValues(1)+" "+lineValues); 0L})
		val hash = Try{lineValues(3)}.getOrElse({println("Wrong hash in: "+lineValues(3)+" "++lineValues); ""})
		Fil(file, isFile, size, hash)
	}
	def _try[A](tri: => A, catc: => A, finall: => Unit = {}):A = try{tri} catch {case t:Throwable => catc}
}

object Index {
  def create(locationToIndex: String, locationToStore: String, finished: => Unit) = {
    if (locationToIndex.asFile.isDirectory) {
      val tree = locationToIndex.asFile.fileTreeWithItself
      val bw = new BufferedWriter(new FileWriter(locationToStore + "\\" + locationToIndex.toSafeFileName))
      tree.foreach { f => println(f); bw.write(f + ";" + f.length + ";" + f.isFile + ";" + f.hashSimple + "\r\n") }
      bw.close
    }

    finished
  }

  def load(indexLocation: String) = indexLocation.linesFromFile map Fil.lineToFil

}