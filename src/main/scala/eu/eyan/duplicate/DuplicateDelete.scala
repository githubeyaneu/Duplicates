package eu.eyan.duplicate

import eu.eyan.util.awt.MultiField
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import eu.eyan.util.swing.JFramePlus.JFramePlusImplicit
import eu.eyan.util.swing.JTextFieldPlus.JTextFieldPlusImplicit
import eu.eyan.util.swing.JToggleButtonPlus.JToggleButtonImplicit
import eu.eyan.util.swing.{JCheckBoxPlus, JPanelWithFrameLayout, MultiFieldJTextField, SwingPlus}
import javax.swing.JFrame
import sun.plugin.com.Dispatch

class TextFieldWithCheckBox(size: Int) extends JPanelWithFrameLayout {
  withSeparators
  val textField = addTextField("", size)
  val include = newColumn.addCheckBox("Include")
  val checkBox = newColumn.addCheckBox("Allow to delete")
  def onKeyReleased(action: => Unit) = {textField.onKeyReleased(action); this}
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
  panel.addSeparatorWithTitle("Directories to search")
  //  val dirs = panel.newRow.addTextFieldMulti("dirsToSearch", 30, List()).rememberValueInRegistry("dirsToSearch")
  val dirs = new MultiFieldJTextFieldWithCheckbox("dirsToSearch", 30)
  dirs.rememberValueInRegistry("dirsToSearch")
  panel.newRow.add(dirs)

  val multi = new MultiFieldJTextField("name", 30)

  panel.newRow.addButton("Find duplicates").onAction_disableEnable(findDuplicates(false))

  val allowDelete = panel.newRow.addCheckBox("Enable \"Delete duplicates\" button")
  panel.newRow.addButton("Delete duplicates").onAction_disableEnable(findDuplicates(true)).enabledDependsOn(allowDelete)
  val progress = panel.newRow.addProgressBar(0, 1, "%dMB")
  val logs = panel.newRow("f:1px:g").span(1).addTextArea()

  val frame = new JFrame()
    .title("Handle duplicates")
    .component(panel)
    .onCloseExit
    .packAndSetVisible
    .size(800, 1000)
    .positionToCenter

  dirs.onChanged(() => frame.size(frame.getWidth + 1, frame.getHeight + 1))

  private def findDuplicates(withDelete: Boolean) = {
    SwingPlus.invokeLater { allowDelete.setSelected(false)}
    val deletablePaths = dirs.getValues.filter(_._2).map(_._1)
    val dirPaths = dirs.getValues.filter(_._3).map(_._1).toStream
    val filesStreams = dirPaths.flatMap(_.asDir.fileTreeWithItself.filter(_.isFile))
    var fileCt = 0L
    progress.setFormat("%d files")
    progress.setMaximum(Int.MaxValue)
    val files = filesStreams.collect{ case file =>
      fileCt += 1

      if(fileCt%100==0) SwingPlus.invokeLater { progress.setNewValue(fileCt.toInt)}
      file
    }.toList.distinct
    SwingPlus.invokeLater { progress.setNewValue(fileCt.toInt)}
    val fileGroupsByLength = files.groupBy(_.length())

    val filesSingle = fileGroupsByLength.filter(_._2.size == 1).values.flatten.toList

    val filesMultiGroups = fileGroupsByLength.filter(_._2.size != 1).values.toList.sortWith((l1,l2)=> l1.head.length < l2.head.length)

    val sumMB = (files.map(_.length).sum / 1000 / 1000).toInt
    val sumSingleMB = (filesSingle.map(_.length).sum / 1000 / 1000).toInt
    val sumMultiMB = (filesMultiGroups.flatten.map(_.length).sum / 1000 / 1000).toInt

    SwingPlus.invokeLater {
      logs.setText("")
      logs.append("Files found: " + files.size + ", Size: " + sumMB + "\n")
      logs.append("Single: " + filesSingle.size + ", Size single: " + (sumSingleMB / 1000) + "GB\n")
      logs.append("Multi : " + filesMultiGroups.flatten.size + " (" + filesMultiGroups.size + " groups), Size multi:" + (sumMultiMB / 1000) + "GB\n")

      progress.setFormat("%dMB")
      progress.setMaximum(sumMultiMB)
      progress.setNewValue(0)
    }

    var sum = 0L
    var remainingFilesCt = 0
    var deleteCt = 0
    filesMultiGroups.foreach { group =>
      val hashGroups = group.groupBy(f => {
        f.hashFull(readBytes => SwingPlus.invokeLater {
          sum += readBytes
          progress.setNewValue((sum / 1000 / 1000).toInt)
        })
      })


      hashGroups.foreach { hashFiles =>
        if(hashFiles._2.size <2) SwingPlus.invokeLater { logs.append(".") }
        else
        SwingPlus.invokeLater {
          logs.append("\n\n")
          val hash = hashFiles._1
          logs.append(hash + "\n")
          
          val files = hashFiles._2.sortBy(f => f.getName.length)
//          logs.append(files.map(file => ("File", file.length, file.getAbsolutePath)).mkString("\n")+"\n")
          
          val filesToDeleteCandidates = files.filter(fileToDelete => deletablePaths.exists(deletablePath => {
//            print(".")
//            logs.append("deletablePath: "+deletablePath+"\n")
//            logs.append("fileToDelete.getAbsolutePath: "+fileToDelete.getAbsolutePath+"\n")
            fileToDelete.getAbsolutePath.contains(deletablePath)}))
//          logs.append(filesToDeleteCandidates.map(file => ("filesToDeleteCandidates", file.length, file.getAbsolutePath)).mkString("\n")+"\n")
          val filesToKeepCandidates = files.filter(file => !filesToDeleteCandidates.contains(file))
//          logs.append(filesToKeepCandidates.map(file => ("filesToKeepCandidates", file.length, file.getAbsolutePath)).mkString("\n")+"\n")

          val filesToKeep = if(filesToKeepCandidates.isEmpty) List(filesToDeleteCandidates.head) else filesToKeepCandidates
          remainingFilesCt += filesToKeep.size
          val filesToDelete = if(filesToKeepCandidates.isEmpty) filesToDeleteCandidates.tail else filesToDeleteCandidates 


          logs.append(filesToKeep.map(file => ("Keep", file.length, file.getAbsolutePath)).mkString("\n")+"\n")
          logs.append(filesToDelete.map(file => ("Delete", file.length, file.getAbsolutePath)).mkString("\n")+"\n")
          deleteCt += filesToDelete.size
          
          if (withDelete) filesToDelete.foreach(_.delete)
          
        }
      }
    }

    SwingPlus.invokeLater {
      logs.append("\n\nDeleteCt: " + deleteCt)
      logs.append("\n\nExpected: " + (filesSingle.size + remainingFilesCt))
    }

  }
}