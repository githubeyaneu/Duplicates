package eu.eyan.duplicate.treereader

import java.io.File

class Fil(val path: String, val size: Long) extends Serializable
class Dir(val path: String,
          val size: Long,
          val files: List[Fil],
          val dirs: List[Dir],
          val nrOfDirs: Long,
          val nrOfFiles: Long) extends Serializable

class FileTreeReader {
  var statusListener: Tuple2[Long, Long] => Unit = null
  var endListener: () => Unit = null
  var root: Dir = null

  def countSizes() = {
    val files = allFiles(root)
    val filesGroupedBySize = files.groupBy(f => f.size)
    val sameSizes = filesGroupedBySize.filter(_._2.size>1).toList
    val sameSizesSortedBySize = filesGroupedBySize.filter(_._2.size>1).toList.sortBy(_._1).reverse
    
    sameSizesSortedBySize
  }
  

  def allFiles(dir: Dir):List[Fil] = dir.files ++ dir.dirs.map( allFiles(_) ).flatten
  
  def start(rootDir: File) = {
    root = read(rootDir)
    statusListener.apply((root.nrOfFiles, root.nrOfDirs))
    endListener()
  }

  def read(dir: File): Dir = {
//    println("Read " + dir)
    val fildirs = dir.listFiles()
    if (fildirs == null) new Dir(dir.getAbsolutePath(), 0, List(), List(), 0, 0)
    else {
      val dirs = fildirs.filter(f => f.isDirectory()).map(d => read(d)).toList
      val files = fildirs.filter(f => f.isFile()).map(f => new Fil(f.getAbsolutePath(), f.length())).toList
      val size = dirs.map(_.size).sum + files.map(_.size).sum
      val nrOfDirs = dirs.size + dirs.map(_.nrOfDirs).sum
      val nrOfFiles = files.size + dirs.map(_.nrOfFiles).sum

      val ndir = new Dir(dir.getAbsolutePath(), size, files, dirs, nrOfDirs, nrOfFiles)

//      statusListener.apply((nrOfFiles, nrOfDirs))
//      println(ndir.path + " " + ndir.size)

      ndir
    }
  }

}