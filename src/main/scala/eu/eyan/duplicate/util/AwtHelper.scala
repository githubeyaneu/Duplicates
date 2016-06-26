package eu.eyan.duplicate.util

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.Component
import java.awt.Toolkit
import javax.swing.SwingWorker

object AwtHelper {

  def newActionListener(action: ActionEvent => Unit) = new ActionListener() { override def actionPerformed(e: ActionEvent) = action(e) }

  def positionToCenter(component: Component) = {
    val screenSize = Toolkit.getDefaultToolkit().getScreenSize()
    val width = component.getSize().width
    val height = component.getSize().height
    component.setSize(width, height)
    component.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2)
  }

  def runInWorker(work: () => Unit, doAtDone: () => Unit) = {
    new SwingWorker[Void, Void]() {
      override def doInBackground() = {work.apply(); null}
      override def done() = doAtDone.apply()
    }.execute()
  }
}