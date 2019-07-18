package eu.eyan.duplicate

import eu.eyan.util.io.FilePlus
import java.io.File
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import java.io.FileWriter
import java.io.BufferedWriter

object Extension extends App {

  //writeFilesToTxt
  
//  def files = readFiles.toStream.filter { _.isFile }
//  def extensions = files map { f=> f.file.extension } 
//  def distinct = extensions.distinct 
//  
//  println(distinct.sorted.mkString(", "))
//  
//  println(files.filter(_.file.extension.startsWith(".GIF")).mkString("\r\n"))
//
//  def readFiles() = {
//    """C:\tmp\IKLvid.txt""".linesFromFile map { l => {val f3 = l.split(";");Fil(new File(f3(0)), f3(2).toBoolean, f3(1).toLong, new File(f3(3)).hash) } }
//  }
//  
//  def writeFilesToTxt = {
//    val tree = FilePlus.fileTrees("""I:\videos""", """K:\NO_BACKUP\videos""", """L:\NO_BACKUP\videos""")
//    val bw = new BufferedWriter(new FileWriter("""C:\tmp\IKLvid.txt"""))
//    tree.foreach { f => println(f); bw.write(f + ";" + f.length+";"+f.isFile+"\r\n") }
//    bw.close
//  }
}