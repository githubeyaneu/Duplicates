package eu.eyan.duplicate.util

import javax.swing.JPanel
import com.jgoodies.forms.layout.FormLayout
import com.jgoodies.forms.factories.CC
import javax.swing.JButton
import com.jgoodies.forms.layout.ColumnSpec
import com.jgoodies.forms.layout.RowSpec
import javax.swing.JTextField
import javax.swing.JLabel
import java.awt.Component

class JPanelWithFrameLayout extends JPanel {
  val frameLayout = new FormLayout("", "p")
  this.setLayout(frameLayout)
  var column = 0
  var row = 1

  def newColumnSeparator() = {
	  frameLayout.appendColumn(ColumnSpec.decode("3dlu"))
	  column += 1
  }
  def newRowSeparator() = {
    frameLayout.appendRow(RowSpec.decode("3dlu"))
    row +=1
  }
  
  def newColumn() = {
    if (column != 0) newColumnSeparator()
    frameLayout.appendColumn(ColumnSpec.decode("p"))
    column += 1
    this
  }

  def newRow() = {
    newRowSeparator()
    frameLayout.appendRow(RowSpec.decode("p"))
    row += 1
    this
  }

  def addButton(text: String) = {
    newColumn()
    val button = new JButtonPlus(text)
    this.add(button, CC.xy(column, row))
    button
  }

  def addTextField(text: String, size: Int = 15) = {
    newColumn()
    val tf = new JTextField(text, size)
    this.add(tf, CC.xy(column, row))
    tf
  }
  
  def addLabel(text: String) = {
	  newColumn()
	  val label = new JLabel(text)
	  this.add(label, CC.xy(column, row))
	  label
  }

  override def add(comp: Component) = {
    if(column == 0) newColumn()
    this.add(comp, CC.xy(column, row))
    comp
  }

}