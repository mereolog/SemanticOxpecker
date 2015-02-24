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
public class ExpanderFilter extends FileFilter {
    
public  boolean accept(File f)
    {
        boolean isExpander = false;
        String fileExt = f.getName().substring(f.getName().lastIndexOf(".")+1);
        
        if (fileExt != null)
        {
            if (fileExt.equals("exp"))
            {
                isExpander = true;
                //System.out.println(isExpander);
            }
        }
         
        return isExpander;
    }

public String getDescription()
{
    
    return "Tylko ekspandery";
}
    
}
