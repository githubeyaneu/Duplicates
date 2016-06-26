package eu.eyan.duplicate.util

import javax.swing.JButton
import java.awt.event.ActionEvent

class JButtonPlus(text:String) extends JButton(text) {
  def addAction(action: ActionEvent => Unit) = {
    this.addActionListener( AwtHelper.newActionListener( e => {
      setEnabled(false)
      AwtHelper.runInWorker(() => action.apply(e), () => setEnabled(true))
    } )) 
  }
}