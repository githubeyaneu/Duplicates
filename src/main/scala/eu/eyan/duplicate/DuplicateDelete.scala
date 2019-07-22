package eu.eyan.duplicate

import eu.eyan.util.awt.MultiField
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import eu.eyan.util.swing.JFramePlus.JFramePlusImplicit
import eu.eyan.util.swing.JTextFieldPlus.JTextFieldPlusImplicit
import eu.eyan.util.swing.{JPanelWithFrameLayout, MultiFieldJTextField, SwingPlus}
import javax.swing.JFrame

class TextFieldWithCheckBox(size: Int) extends JPanelWithFrameLayout {
  withSeparators
  val textField = addTextField("", size)
  val checkBox = newColumn.addCheckBox("Allow to delete")

  def onKeyReleased(action: => Unit) = {
    textField.onKeyReleased(action); this
  }
}

class MultiFieldJTextFieldWithCheckbox(columnName: String, columns: Int = 0) extends MultiField[(String, Boolean), TextFieldWithCheckBox](columnName) {
  protected def createEditor(fieldEdited: TextFieldWithCheckBox => Unit) = {
    val editor = new TextFieldWithCheckBox(columns)
    editor.onKeyReleased(fieldEdited(editor))
  }

  protected def getValue(editor: TextFieldWithCheckBox) = {
    val text = editor.textField.getText.trim
    if (text.isEmpty) None else Some((text, editor.checkBox.isSelected))
  }

  protected def stringToValue(string: String): (String, Boolean) = (string, false)

  protected def setValueInEditor(editor: TextFieldWithCheckBox)(value: (String, Boolean)): Unit = editor.textField.setText(value._1)

  protected def valueToString(value: (String, Boolean)): String = value._1

  def getTexts = getValues.map(_._1)
}

object DuplicateDelete extends App {
  val panel = new JPanelWithFrameLayout().withBorders.withSeparators
  panel.newColumn.newColumnFPG
  panel.addSeparatorWithTitle("Directories to search")
  //  val dirs = panel.newRow.addTextFieldMulti("dirsToSearch", 30, List()).rememberValueInRegistry("dirsToSearch")
  val dirs = new MultiFieldJTextFieldWithCheckbox("dirsToSearch", 30)
  dirs.rememberValueInRegistry("dirsToSearch")
  panel.newRow.add(dirs)

  val multi = new MultiFieldJTextField("name", 30)

  val fullHash = panel.newRow.addCheckBox("Full hash", false)
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

  private def findDuplicates(withDelete: Boolean) = {
    val deletablePaths = if (withDelete) dirs.getValues.filter(_._2).map(_._1) else List()
    val files = dirs.getTexts.flatMap(_.asDir.fileTreeWithItself.filter(_.isFile).toList).distinct
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

    var sum = 0L

    def updateProgress(readBytes: Long) = SwingPlus.invokeLater {
      sum += readBytes; progress.setNewValue((sum / 1000 / 1000).toInt)
    }

    var remainingFilesCt = 0
    filesMultiGroups.foreach { group =>
      val hashGroups = group.groupBy(f => if(fullHash.isSelected) f.hashFull(updateProgress) else f.hashFast(updateProgress) )

      hashGroups.foreach { hashFiles =>
        SwingPlus.invokeLater {
          logs.append("\n\n")
          val hash = hashFiles._1
          logs.append(hash + "\n")

          val files = hashFiles._2.sortBy(f => f.getName.length)
          logs.append(files.map(file => ("File", file.length, file.getName)).mkString("\n") + "\n")

          val filesToDeleteCandidates = files.filter(fileToDelete => deletablePaths.exists(deletablePath => fileToDelete.getAbsolutePath.contains(deletablePath)))
          logs.append(filesToDeleteCandidates.map(file => ("filesToDeleteCandidates", file.length, file.getName)).mkString("\n") + "\n")
          val filesToKeepCandidates = files.filter(file => !filesToDeleteCandidates.contains(file))
          logs.append(filesToKeepCandidates.map(file => ("filesToKeepCandidates", file.length, file.getName)).mkString("\n") + "\n")

          val filesToKeep = if (filesToKeepCandidates.isEmpty) List(filesToDeleteCandidates.head) else filesToKeepCandidates
          remainingFilesCt += filesToKeep.size
          val filesToDelete = if (filesToKeepCandidates.isEmpty) filesToDeleteCandidates.tail else filesToDeleteCandidates

          logs.append(filesToKeep.map(file => ("Keep", file.length, file.getName)).mkString("\n") + "\n")
          logs.append(filesToDelete.map(file => ("Delete", file.length, file.getName)).mkString("\n") + "\n")

          if (withDelete) filesToDelete.foreach(_.delete)

        }
      }
    }

    SwingPlus.invokeLater {
      logs.append("\n\nExpected: " + (filesSingle.size + remainingFilesCt))
    }

  }
}