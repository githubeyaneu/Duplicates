package eu.eyan.duplicate

import java.io.File

import eu.eyan.util.awt.MultiField
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import eu.eyan.util.swing.JComponentPlus.JComponentImplicit
import eu.eyan.util.swing.JFramePlus.JFramePlusImplicit
import eu.eyan.util.swing.JTextAreaPlus.JTextAreaImplicit
import eu.eyan.util.swing.JTextFieldPlus.JTextFieldPlusImplicit
import eu.eyan.util.swing.{JPanelWithFrameLayout, MultiFieldJTextField}
import javax.swing.JFrame

import scala.collection.mutable

class TextFieldWithCheckBox(size: Int) extends JPanelWithFrameLayout {
  withSeparators
  val textField = addTextField("", size)
  val include = newColumn.addCheckBox("Include")
  val checkBox = newColumn.addCheckBox("Allow to delete")

  def onKeyReleased(action: => Unit) = {
    textField.onKeyReleased(action)
    this
  }
}

class MultiFieldJTextFieldWithCheckbox(columnName: String, columns: Int = 0) extends MultiField[(String, Boolean, Boolean), TextFieldWithCheckBox](columnName) {
  protected def createEditor(fieldEdited: TextFieldWithCheckBox => Unit) = {
    val editor = new TextFieldWithCheckBox(columns)
    editor.onKeyReleased(fieldEdited(editor))
  }

  protected def getValue(editor: TextFieldWithCheckBox) = {
    val text = editor.textField.getText.trim
    if (text.isEmpty) None else Some((text, editor.checkBox.isSelected, editor.include.isSelected))
  }

  protected def stringToValue(string: String): (String, Boolean, Boolean) = (string, false, false)

  protected def setValueInEditor(editor: TextFieldWithCheckBox)(value: (String, Boolean, Boolean)): Unit = editor.textField.setText(value._1)

  protected def valueToString(value: (String, Boolean, Boolean)): String = value._1

  def getTexts = getValues.map(_._1)
}

object DuplicateDelete extends App {
  val panel = new JPanelWithFrameLayout().withBorders.withSeparators
  panel.newColumn.newColumnFPG
  val N = "\n"
  panel.addSeparatorWithTitle("Directories to search")
  //  val dirs = panel.newRow.addTextFieldMulti("dirsToSearch", 30, List()).rememberValueInRegistry("dirsToSearch")
  val dirs = new MultiFieldJTextFieldWithCheckbox("dirsToSearch", 30)
  dirs.rememberValueInRegistry("dirsToSearch")
  panel.newRow.add(dirs)

  val multi = new MultiFieldJTextField("name", 30)

  val findPanel = new JPanelWithFrameLayout().withSeparators
  findPanel.newColumnFPG.addButton("Find duplicates").onAction_disableEnable(findDuplicates(false))
  val fullHash = findPanel.newColumn.addCheckBox("Full hash")
  panel.newRow.add(findPanel)


  val deletePanel = new JPanelWithFrameLayout().withSeparators
  val allowDelete = deletePanel.newRow.addCheckBox("Enable").tooltipText("Enable \"Delete duplicates\" button")
  deletePanel.newColumnFPG.addButton("Delete duplicates").onAction_disableEnable(findDuplicates(true)).enabledDependsOn(allowDelete)
  panel.newRow.add(deletePanel)

  val progress = panel.newRow.addProgressBar(0, 1, "%dMB")

  val logs = panel.newRowFPGForTextArea.span(1).addTextArea()

  val frame = new JFrame()
    .title("Handle duplicates")
    .component(panel)
    .onCloseExit
    .packAndSetVisible
    .size(800, 1000)
    .positionToCenter

  dirs.onChanged(() => frame.size(frame.getWidth + 1, frame.getHeight + 1))

  private def findDuplicates(withDelete: Boolean) = {
    allowDelete.setSelected(false)
    val deletablePaths = dirs.getValues.filter(_._2).map(_._1)
    val dirPaths = dirs.getValues.filter(_._3).map(_._1).toStream
    val filesStreams = dirPaths.flatMap(_.asDir.fileTreeWithItself.filter(_.isFile))
    var fileCt = 0L
    progress.setFormat("%d files")
    progress.setMaximum(Int.MaxValue)
    val files = filesStreams.collect { case file =>
      fileCt += 1
      if (fileCt % 100 == 0) progress.valueChanged(fileCt.toInt)
      file
    }.toList.distinct
    progress.valueChanged(fileCt.toInt)

    val fileGroupsByLength = files.groupBy(_.length())

    val filesSingle = fileGroupsByLength.filter(_._2.size == 1).values.flatten.toList

    val filesMultiGroups = fileGroupsByLength.filter(_._2.size != 1).values.toList.sortWith((l1, l2) => l1.head.length < l2.head.length)

    val sumMB = (files.map(_.length).sum / 1000 / 1000).toInt
    val sumSingleMB = (filesSingle.map(_.length).sum / 1000 / 1000).toInt
    val sumMultiMB = (filesMultiGroups.flatten.map(_.length).sum / 1000 / 1000).toInt

    logs.setText("")
    logs.appendLater("Files found: " + files.size + ", Size: " + sumMB + N)
    logs.appendLater("Single: " + filesSingle.size + ", Size single: " + (sumSingleMB / 1000) + "GB" + N)
    logs.appendLater("Multi : " + filesMultiGroups.flatten.size + " (" + filesMultiGroups.size + " groups), Size multi:" + (sumMultiMB / 1000) + "GB" + N)

    progress.setFormat("%dMB")
    progress.setMaximum(sumMultiMB)
    progress.valueChanged(0)

    var sum = 0L

    def updateProgress(readBytes: Long) = {
      sum += readBytes
      progress.valueChanged((sum / 1000 / 1000).toInt)
    }

    var remainingFilesCt = 0
    var deleteCt = 0

    val dirsContainingDuplicates = mutable.Set[File]()

    filesMultiGroups.foreach { group =>
      val hashGroups = group.groupBy(f => if (fullHash.isSelected) f.hashFull(updateProgress) else f.hashFast(updateProgress))


      hashGroups.foreach { hashFiles =>
        if (hashFiles._2.size < 2) logs.appendLater(".")
        else  {
          logs.appendLater(N + N)
          val hash = hashFiles._1
          logs.appendLater(hash + N)

          val files = hashFiles._2.sortBy(f => f.getName.length)
          files.map(_.getParentFile).foreach(dirsContainingDuplicates.add)
          //          logs.append(files.map(file => ("File", file.length, file.getAbsolutePath)).mkString(N)+N)

          val filesToDeleteCandidates = files.filter(fileToDelete => deletablePaths.exists(fileToDelete.getAbsolutePath.contains(_)))
          //          logs.append(filesToDeleteCandidates.map(file => ("filesToDeleteCandidates", file.length, file.getAbsolutePath)).mkString(N)+N)
          val filesToKeepCandidates = files.filter(file => !filesToDeleteCandidates.contains(file))
          //          logs.append(filesToKeepCandidates.map(file => ("filesToKeepCandidates", file.length, file.getAbsolutePath)).mkString(N)+N)

          val filesToKeep = if (filesToKeepCandidates.isEmpty) List(filesToDeleteCandidates.head) else filesToKeepCandidates
          remainingFilesCt += filesToKeep.size
          val filesToDelete = if (filesToKeepCandidates.isEmpty) filesToDeleteCandidates.tail else filesToDeleteCandidates


          logs.appendLater(filesToKeep.map(file => ("Keep", file.length, file.getAbsolutePath)).mkString(N) + N)
          logs.appendLater(filesToDelete.map(file => ("Delete", file.length, file.getAbsolutePath)).mkString(N) + N)
          deleteCt += filesToDelete.size

          if (withDelete) filesToDelete.foreach(_.delete)
        }
      }
    }

    logs.appendLater(N + N + "dirsContainingDuplicates: " + N + dirsContainingDuplicates.toList.sorted.mkString(N))
    logs.appendLater(N + N + "DeleteCt: " + deleteCt)
    logs.appendLater(N + N + "Expected: " + (filesSingle.size + remainingFilesCt))
  }
}