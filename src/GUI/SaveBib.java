
package GUI;

import java.io.*;
import javax.swing.*;

public class SaveBib extends JFrame {
    
    JFileChooser fc;
    BibFilter bibFilter;

    public SaveBib()
    {
        bibFilter = new BibFilter();
        initComponents();
        
        int returnVal = fc.showSaveDialog(null);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
           
            File selectedFile = fc.getSelectedFile();
            //System.out.print(file);
            
            File file = null;
            
            if (!selectedFile.getName().endsWith(".bib"))
            {
                if (!selectedFile.getName().contains("."))
                file = new File(selectedFile.getAbsolutePath()+".bib");
                else
                {
                    file = new File (selectedFile.getAbsolutePath().replaceAll("\\..*?\\z", ".bib"));
                }
            }
            else file=selectedFile;
            
            try
            {
                //System.out.println(file);
                file.createNewFile();
                
                //PrintWriter out = new PrintWriter(file);
                
                String bibContent = "";
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Configure.Configure.bibEncoding));
                
                for (Prepare.Paper answer : OntoBeefApplication.search.answerSet)
                {
                    if (answer.bib != null && !bibContent.contains(answer.bib))
                    {
                        //System.out.println(answer.bib);
                        out.write(answer.bib);
                        bibContent=bibContent+answer.bib;
                    }
                }
                
                out.flush();
                out.close();
                
                this.dispose();
                
            } catch (java.io.IOException ex) {ex.printStackTrace();} //catch (java.io.FileNotFoundException ex) {ex.printStackTrace();}
            
        }
        
        if (returnVal == JFileChooser.CANCEL_OPTION)
        {
            this.dispose();
        }
    }
    
    private void initComponents() {
        
        
        
        fc = new JFileChooser();
        fc.setFileFilter(bibFilter);
        fc.setCurrentDirectory(new File("bibliografie/"));
        fc.setApproveButtonText("Zapisz");
        
        this.add(fc);
        
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();
        
    }
    
    
}