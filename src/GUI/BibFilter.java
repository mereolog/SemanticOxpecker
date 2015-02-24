/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author PG
 */
public class BibFilter extends FileFilter{
    
    public  boolean accept(File f)
    {
        boolean isBib = false;
        String fileExt = f.getName().substring(f.getName().lastIndexOf(".")+1);
        
        if (fileExt != null)
        {
            if (fileExt.equals("bib"))
            {
                isBib = true;
                //System.out.println(isExpander);
            }
        }
         
        return isBib;
    }

public String getDescription()
{
    
    return "Tylko pliki bib";
}
    
}
