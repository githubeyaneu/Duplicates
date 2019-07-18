package eu.eyan.duplicate

import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import eu.eyan.util.swing.JFramePlus.JFramePlusImplicit
import eu.eyan.util.swing.JListPlus.JListImplicit
import eu.eyan.util.swing.JPanelWithFrameLayout
import javax.swing.JFrame

import scala.collection.JavaConversions._

import eu.eyan.util.swing.JListPlus.JListImplicit
import eu.eyan.util.awt.ComponentPlus.ComponentPlusImplicit
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.swing.JFramePlus
import eu.eyan.util.swing.JFramePlus.JFramePlusImplicit
import eu.eyan.util.swing.JPanelWithFrameLayout
import eu.eyan.util.swing.JTablePlus.JTableImplicit
import javax.swing.JFrame
import javax.swing.JList
import eu.eyan.util.swing.SwingPlus
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit

object Duplicator extends App {
  type Duplicate = List[Fil]

  val indexPanel = new JPanelWithFrameLayout
  indexPanel.addLabel("Index location")
  val indexLocation = indexPanel.newColumn.addTextField("""c:\tmp\idx""", 10)
  val readIndexButton = indexPanel.newColumn.addButton("  Read index  ").onAction(readIndices)

  indexPanel.newRow("f:p")
  val indexList = indexPanel.addList[String]

  // TODO: why does not work? indexList.asInstanceOf[JList[String]].onSelectionChangedEvent( e=> {deleteIndexButton.setEnabled(indexList.getSelectedIndices.nonEmpty) } )
  indexList.getSelectionModel.addListSelectionListener(SwingPlus.onValueChanged(e => {deleteIndexButton.setEnabled(indexList.getSelectedIndices.nonEmpty)}))
  
  val addRemovePanel = indexPanel.newColumn.addPanelWithFormLayout()

  val deleteIndexButton = addRemovePanel.addButton("Delete").onAction({ println("Delete: " + indexList.getSelectedValuesList.mkString); indexList.getSelectedValuesList.foreach(_.asFile.delete); readIndices }).disabled
  val locationToIndex = addRemovePanel.newRow.addTextField("""i:\videos""", 30)
  addRemovePanel.newRow.addButton("Create Index").onAction(Index.create(locationToIndex.getText, indexLocation.getText, readIndices))

  val panel = new JPanelWithFrameLayout()
  panel.newColumn("f:p:g")
  panel.addSeparatorWithTitle("Indices")
  panel.newRow.add(indexPanel)

  panel.newRow.addSeparatorWithTitle("Duplicates")
  val table = panel.newRowFPG.addTable[Duplicate].withColumns("Name", "Size", "Count")
  table.withValueGetter((duplicate, column) => column match {
    case "Count" => duplicate.size.toString
    case "Name"  => duplicate(0).file.getName
    case "Size"  => duplicate(0).sizeReadable
    case _       => "n.a."
  })
  val duplicates = panel.newRow.addList[String]
  table onSelectionChanged { () => showDuplicates }

  def showDuplicates = {
    val selectedDuplicate = table.getValues(table.convertRowIndexToModel(table.getSelectedRow))
    val files = selectedDuplicate.map(fil => fil.file.toString)
    duplicates.withValues(files)
  }

  def readIndices = {
    val indices = indexLocation.getText.asFile.listFiles.map(_.getAbsolutePath).toList
    indexList.withValues(indices)

    val fils = indices.map(Index.load(_)).flatten.toList
    table.withValues(findDuplicates(fils))
  }

  def findDuplicates(fils: List[Fil]) = {
    val bigs = fils.filter(_.size > 100 * 1000 * 1000)
    val groups = bigs.groupBy(f => (f.file.getName + f.size + f.hash)).values
    val duplicates = groups.filter(1 < _.size).toList.sortWith((l, r) => l(0).size < r(0).size)
    for (duplicate <- duplicates) {
      println(duplicate(0).size)
      println(duplicate.map(_.file).mkString("  ", "\r\n  ", ""))
    }
    duplicates
  }

  new JFrame().title("Duplicator").component(panel).packAndSetVisible.positionToCenter
}