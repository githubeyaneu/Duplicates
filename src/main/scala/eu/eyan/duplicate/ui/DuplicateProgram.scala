package eu.eyan.duplicate.ui

import org.jdesktop.swingx.JXFrame
import javax.swing.JFrame
import java.awt.Component
import java.awt.Toolkit
import java.awt.Frame
import javax.swing.JPanel
import com.jgoodies.forms.layout.FormLayout
import javax.swing.JLabel
import com.jgoodies.forms.factories.CC
import javax.swing.JTextField
import javax.swing.JButton
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import java.io.File
import eu.eyan.duplicate.treereader.FileTreeReader
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.io.ObjectInputStream
import java.io.FileInputStream
import eu.eyan.duplicate.treereader.Dir
import eu.eyan.duplicate.util.JPanelWithFrameLayout
import eu.eyan.duplicate.util.AwtHelper._
import javax.swing.JTextArea
import javax.swing.JScrollPane
import eu.eyan.duplicate.treereader.Fil

object DuplicateProgram extends App {
  val treeReader = new FileTreeReader()

  val readPanel = new JPanelWithFrameLayout()
  val readFromPathButton = readPanel.addButton("Create index:")
  val readFromPathPath = readPanel.addTextField("Z:\\_REND_\\zene")
  val writeToFileButton = readPanel.addButton("Index to file:")
  val writeToFilePath = readPanel.addTextField("Z:\\index")
  val readFromFileButton = readPanel.addButton("Index from file:")
  val readFromFilePath = readPanel.addTextField("Z:\\index")

  val statusPanel = new JPanelWithFrameLayout()
  val statusFiles = statusPanel.addLabel("Nr. of files:")
  val statusFilesNr = statusPanel.addLabel("-")
  val statusDirs = statusPanel.addLabel("Nr. of dirs:")
  val statusDirsNr = statusPanel.addLabel("-")
  val statusSize = statusPanel.addLabel("Size:")
  val statusSizeNr = statusPanel.addLabel("-")

  val messagePanel = new JPanelWithFrameLayout()
  val messageLabelTitle = messagePanel.addLabel("Message: ")
  val messageLabel = messagePanel.addLabel("")

  val listCommandsPanel = new JPanelWithFrameLayout()
  val countSizes = listCommandsPanel.addButton("List count of sizes")

  val textArea = new JTextArea("...", 15,15)

  val mainPanel = new JPanelWithFrameLayout()
  mainPanel.add(readPanel)
  mainPanel.newRow().add(statusPanel)
  mainPanel.newRow().add(messagePanel)
  mainPanel.newRow().add(listCommandsPanel)
  mainPanel.newRow().add(new JScrollPane(textArea))
  
  
  val frame = new JXFrame()
  frame.setLayout(new FormLayout("3dlu,p,3dlu","3dlu,p,3dlu"))
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  frame.setTitle("Duplicates");
  frame.setName("duplicates_frame")
  frame.add(mainPanel, CC.xy(2,2))
  frame.setVisible(true)
  frame.pack()
  positionToCenter(frame)

  readFromPathButton.addAction(e => startReadingFromPath(readFromPathPath.getText())  )
  writeToFileButton.addAction( e =>  writeToFile(writeToFilePath.getText())  )
  readFromFileButton.addAction(e => readFromFile(readFromFilePath.getText())  )
  countSizes.addAction(e => textArea.setText(formatSameSizes(treeReader.countSizes()))  )
  
  def startReadingFromPath(path: String) = {
    val root = new File(path)
    if (!root.exists()) messageLabel.setText("Does not exist: " + path)
    else {
      readFromPathButton.setEnabled(true)
      treeReader.statusListener = (fd: Tuple2[Long, Long]) => { statusFilesNr.setText(fd._1 + ""); statusDirsNr.setText(fd._2 + "") }
      treeReader.endListener = () => { readFromPathButton.setEnabled(true) }
      treeReader.start(root)
    }
  }

  def writeToFile(path: String) = {
    val os = new ObjectOutputStream(new FileOutputStream(path))
    os.writeObject(treeReader.root)
    os.close()
  }
  
  def readFromFile(path: String) = {
    val is = new ObjectInputStream(new FileInputStream(path))
    val obj = is.readObject()
    is.close()
    treeReader.root = obj.asInstanceOf[Dir]
    statusFilesNr.setText(treeReader.root.nrOfFiles + "")
    statusDirsNr.setText(treeReader.root.nrOfDirs + "")
    statusSizeNr.setText(treeReader.root.size+ "")
  }

  def formatSameSizes(sameSizesSortedBySize: List[(Long, List[Fil])] ) = {
    val duplicateCount = sameSizesSortedBySize.filter(_._1>1000000).map(cl=>(cl._1, cl._2.map( f => new File(f.path).getName()).mkString("\r\n  ")))
    println("found")
    println("found "+duplicateCount.size)
    println(duplicateCount.map(cl => cl._1 +"\r\n  "+cl._2).mkString("\r\n"))
    duplicateCount.map(cl => cl._1 +"\r\n  "+cl._2).mkString("\r\n")
  }
}