/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prepare;

import java.util.Comparator;
import java.io.*;
//import java.util.

/**
 *
 * @author PG
 */
public class Paper implements Comparator,Serializable {
    
    public int paperId;
    public double paperRelevance;
    
    public String paperTitle;
    
    public String paperAuthor;
    
    public String publication;
    public String year;
    public String pages;
    public String volume;
    
    public String paperAbstract;
    public String paperAbstractTranslation;
    
    public String theses;
    
    public String keyword;
    
    public String bib;
    
    public Paper (int id, double rel)
    {
        paperId=id;
        paperRelevance = rel;
    }
    
    public Paper ()
    {
        
    }
    
    public int compare (Object object1, Object object2)
    {
        int comparison=0;
        
        Paper paper1 = (Paper)object1;
        Paper paper2 = (Paper)object2;
        
        if (paper1.paperRelevance>paper2.paperRelevance) comparison =-1;
        if (paper1.paperRelevance<paper2.paperRelevance) comparison =1;
        if (paper1.paperRelevance==paper2.paperRelevance) 
        {
            if (paper1.paperId<paper2.paperId) comparison=-1;
            if (paper1.paperId>paper2.paperId) comparison=1;
            if (paper1.paperId==paper2.paperId) comparison=0;
        }
        
        return comparison;
    }
    
}
