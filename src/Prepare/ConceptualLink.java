/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prepare;


import java.util.ArrayList;
import java.util.HashSet;
import com.hp.hpl.jena.ontology.*;
import java.io.*;


/**
 *
 * @author PG
 */
public class ConceptualLink implements Serializable {
    
    int depth;
    ArrayList<LinkType> linkTypeList;
    Individual beginCategory;
    HashSet<String> endListOfPaperURI;
    
    ConceptualLink (Individual c, ArrayList<LinkType> l)
    {
        beginCategory=c;
        linkTypeList=l;
    }
    
    HashSet<String> createEndList(OntModel model, ArrayList<LinkType> linkTypeList, Expander expInst)
    {
        HashSet<String> endListOfPaperURIForLink= new HashSet<String>();
        HashSet<Individual> endListIndividualSet = new HashSet<Individual>();
        
        endListIndividualSet.add(this.beginCategory);
        
        //Individual beginCategory = model.getIndividual(Test.conceptOntURI+"#"+beginString);
        
        for (LinkType link : linkTypeList)
        {
            HashSet<Individual> newCategoriesSet = new HashSet<Individual>();
            
            for (Individual category : endListIndividualSet)
            {
                //System.out.println(category);
                //System.out.println(link.type);
                //System.out.println("***");
                if (category.hasProperty(link.type))
                {
                    Individual newCategory = model.getIndividual(category.getPropertyResourceValue(link.type).getURI());
                    if (expInst.categoryToPaperURIMap.containsKey(newCategory)) 
                    {
                        endListOfPaperURIForLink.addAll(expInst.categoryToPaperURIMap.get(newCategory));
                    }
                    newCategoriesSet.add(newCategory);
                }
            }
            
            endListIndividualSet.addAll(newCategoriesSet);
        }
        
        return endListOfPaperURIForLink;
    }
    
}
