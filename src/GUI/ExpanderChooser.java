/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

/**
 *
 * @author PG
 */
public class ExpanderChooser extends javax.swing.JFileChooser{
    
    javax.swing.JFrame frame;
    
    ExpanderChooser (javax.swing.JFrame f)
    {
        frame=f;
    }
    
    public void approveSelection()
    {
        OntoBeefApplication.expanderModel=OntoBeefApplication.retrieveExpanderModel(this.getSelectedFile().getPath(), OntoBeefApplication.retrievedTFIDF);
        OntoBeefApplication.activateExpander(OntoBeefApplication.GUI);
        this.frame.dispose();
    }
    
    public void cancelSelection()
    {
        this.frame.dispose();
    }
    
}
