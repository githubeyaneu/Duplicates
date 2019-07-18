package eu.eyan.duplicate

import eu.eyan.util.swing.JFramePlus.JFramePlusImplicit
import javax.swing.JFrame
import eu.eyan.util.swing.JPanelWithFrameLayout
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import eu.eyan.util.swing.SwingPlus

object DuplicateDelete extends App {

  val panel = new JPanelWithFrameLayout().withBorders.withSeparators
  panel.newColumn.newColumnFPG
  panel.addSeparatorWithTitle("Directories to search")
  val dirs = panel.newRow.addTextFieldMulti("dirsToSearch", 30, List()).rememberValueInRegistry("dirsToSearch")
  panel.newRow.addButton("Find duplicates").onAction_disableEnable(findDuplicates(false))
  panel.newRow.addButton("Delete duplicates").onAction_disableEnable(findDuplicates(true))
  val progress = panel.newRow.addProgressBar(0, 1, "%dMB")
  val logs = panel.newRow("f:1px:g").span(1).addTextArea()

  val frame = new JFrame()
    .title("Handle duplicates")
    .component(panel)
    .onCloseExit
    .packAndSetVisible
    .size(800, 600)
    .positionToCenter

  dirs.onChanged(() => frame.size(frame.getWidth + 1, frame.getHeight + 1))

  private def findDuplicates(withDelete: Boolean): Unit = {
    val files = dirs.getValues.flatMap(_.asDir.fileTreeWithItself.filter(_.isFile).toList).distinct
    val fileGroupsByLength = files.groupBy(_.length())

    val filesSingle = fileGroupsByLength.filter(_._2.size == 1).values.flatten.toList

    val filesMultiGroups = fileGroupsByLength.filter(_._2.size != 1).values.toList

    val sumMB = (files.map(_.length).sum / 1000 / 1000).toInt
    val sumSingleMB = (filesSingle.map(_.length).sum / 1000 / 1000).toInt
    val sumMultiMB = (filesMultiGroups.flatten.map(_.length).sum / 1000 / 1000).toInt

    SwingPlus.invokeLater {
      logs.setText("")
      logs.append("Files found: " + files.size + ", Size: " + sumMB + "\n")
      logs.append("Single: " + filesSingle.size + ", Size single: " + (sumSingleMB / 1000) + "GB\n")
      logs.append("Multi : " + filesMultiGroups.flatten.size + " (" + filesMultiGroups.size + " groups), Size multi:" + (sumMultiMB / 1000) + "GB\n")

      progress.setMaximum(sumMultiMB)
      progress.setNewValue(0)
    }

    var sum = 0
    var remainingFilesCt = 0
    filesMultiGroups.foreach { group =>
      val hashGroups = group.groupBy(f => {
        f.hashFull(readBytes => SwingPlus.invokeLater {

          sum += readBytes
          progress.setNewValue(sum / 1000 / 1000)
        })
      })

      hashGroups.foreach { hashFiles =>
        SwingPlus.invokeLater {
          logs.append("\n\n")
          val hash = hashFiles._1
          val files = hashFiles._2.sortBy(f => f.getName.length)
          val fileToKeep = files.head
          remainingFilesCt += 1
          val filesToDelete = files.tail

          logs.append(hash + "\n")
          logs.append(("Keep", fileToKeep.length, fileToKeep.getAbsolutePath) + "\n")
          logs.append(filesToDelete.map(file => ("Delete", file.length, file.getAbsolutePath)).mkString("\n"))
          if (withDelete) filesToDelete.foreach(_.delete)
        }
      }
    }

    SwingPlus.invokeLater { logs.append("\n\nExpected: " + (filesSingle.size + remainingFilesCt)) }

  }
}