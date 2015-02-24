/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Prepare;


import java.io.FileOutputStream;
import java.util.HashSet;
import java.io.File;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;


/**
 *
 * @author PG
 */
public class PaperRepopulation {

    
    String conceptOntURI;
    String paperOntURI;
    
    String conceptOntLocation;
    
    String paperOntPriorLocation;
    String paperOntPostLocation;
    
    OntModel paperModel;
    
    OntClass category;
    OntClass individual;
    
    DatatypeProperty characterises;
       
    ObjectProperty concerns;
    ObjectProperty isConcernedIn;
    ObjectProperty categorySubsumes;
    ObjectProperty categoryInstantiates;
    
    PaperRepopulation (String concept, String paper, String conceptLoc, String paperPreLoc, String paperPostLoc)
    {
        conceptOntURI=concept;
        paperOntURI=paper;
        
        conceptOntLocation=conceptLoc;
        paperOntPriorLocation=paperPreLoc;
        paperOntPostLocation=paperPostLoc;
    }
    
    public static void main(String[] args) {
        // TODO code application logic here

        PaperRepopulation newRepopulation = new PaperRepopulation(   //
                                                        "http://www.l3g.pl/ontologies/OntoBeef/Conceptualisation.owl", //
                                                        "http://www.l3g.pl/ontologies/OntoBeef/Papers.owl", //
                                                        "data/ontologies/Conceptualisation.owl",//
                                                        "data/ontologies/Papers_Populated.owl", //
                                                        "data/ontologies/Papers_Ready.owl");
        
        newRepopulation.loadOntologies();
        //System.out.println("Ontologies loaded");
        newRepopulation.addPapersToReifiedClasses();
        newRepopulation.addPapersToReifiedIndividuals();
        //System.out.println("Papers added");
        newRepopulation.inheritPapersForIndividuals();
        newRepopulation.inheritPapersForCategories();
        //System.out.println("Papers inherited");
        newRepopulation.saveOntology();
        //System.out.println("Ontologies saved");
        
        OntologyFix newFix = new OntologyFix(   "data/ontologies/Conceptualisation.owl",//
                                                "data/ontologies/Conceptualisation.owl",//
                                                "http://www.l3g.pl/ontologies/OntoBeef/Conceptualisation.owl"        
                                        );
        newFix.loadOntology();
        //System.out.println("Conceptualisation ontology re-loaded to be fixed");
        newFix.removeAnnotations();
        newFix.saveOntology(); 
        
        
        //newRepopulation.paperModel.close();
    }

public void loadOntologies ()
{
    this.paperModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_RDFS_INF);
    paperModel.setDynamicImports(true);
    this.paperModel.read("file:"+this.paperOntPriorLocation, paperOntURI, "N-TRIPLE");
    paperModel.loadImports();
    
    //System.out.println(paperModel.listClasses().toSet());
    
    this.individual = paperModel.getOntClass(conceptOntURI+"#"+"Individual");
    this.category = paperModel.getOntClass(conceptOntURI+"#"+"Category");
    this.concerns = paperModel.getObjectProperty(paperOntURI+"#"+"concerns");
    this.isConcernedIn = paperModel.getObjectProperty(paperOntURI+"#"+"isConcernedIn");
    this.characterises = paperModel.getDatatypeProperty(conceptOntURI+"#"+"characterisesPaper");
    this.categorySubsumes = paperModel.getObjectProperty(conceptOntURI+"#"+"categorySubsumes");
    this.categoryInstantiates = paperModel.getObjectProperty(conceptOntURI+"#"+"categoryInstantiates");
    
    this.paperModel.setStrictMode(false);
    
}



public void addPapersToReifiedClasses()
{
    //System.out.println(this.paperModel.listIndividuals(this.category).toSet());
    
    for (Individual classReification : this.paperModel.listIndividuals(this.category).toSet())
    {
        //System.out.println(classReification.listProperties().toSet());
        if (classReification.hasProperty(this.characterises))
        {
            //System.out.println(classReification);
            for (RDFNode paperId : classReification.listPropertyValues(this.characterises).toSet())
            {
                Individual paper = this.paperModel.getIndividual(this.paperOntURI+"#"+"Paper_"+paperId.asLiteral().toString());
                //System.out.println(paper);
                paper.addProperty(this.concerns, classReification);
                classReification.addProperty(this.isConcernedIn, paper);
                
                classReification.removeProperty(this.characterises, paperId);
                
                //System.out.println("Adding "+classReification.getURI()+" to "+paper.getURI());
            }
                
        }
    }
}

public void addPapersToReifiedIndividuals()
{
    for (Individual individualReification : this.paperModel.listIndividuals(this.individual).toSet())
    {
        if (individualReification.hasProperty(this.characterises))
        {
            for (RDFNode paperId : individualReification.listPropertyValues(this.characterises).toSet())
            {
                Individual paper = paperModel.getIndividual(paperOntURI+"#"+"Paper_"+paperId.asLiteral().toString());
                paper.addProperty(concerns, individualReification);
                individualReification.addProperty(this.isConcernedIn, paper);
                
                individualReification.removeProperty(this.characterises, paperId);
            }
        }
    }
}

public void inheritPapersForCategories()
{
    for (Individual classReification : this.paperModel.listIndividuals(this.category).toSet())
    {
        if (classReification.hasProperty(this.categorySubsumes))
        {
            HashSet<RDFNode> inheritedPaperSet = new HashSet<RDFNode>();
            
            inheritedPaperSet=inheritPapersForCategory(inheritedPaperSet, classReification, this.categorySubsumes);
            
            for (RDFNode subsumedPaperNode : inheritedPaperSet)
            {
                Individual subsumedPaper= this.paperModel.getIndividual(subsumedPaperNode.asResource().getURI());
                        
                //System.out.println(classReification.getURI());
                //System.out.println(subsumedPaperNode.asResource().getURI());
                        
                classReification.addProperty(this.isConcernedIn, subsumedPaperNode);
                subsumedPaper.addProperty(this.concerns, classReification);
            }
        }
    }
}

public void inheritPapersForIndividuals()
{
    for (Individual classReification : this.paperModel.listIndividuals(this.category).toSet())
    {
        if (classReification.hasProperty(this.categoryInstantiates))
        {
            HashSet<RDFNode> inheritedPaperSet = new HashSet<RDFNode>();
            
            inheritedPaperSet=inheritPapersForCategory(inheritedPaperSet, classReification, this.categoryInstantiates);
            
            for (RDFNode subsumedPaperNode : inheritedPaperSet)
            {
                Individual subsumedPaper= this.paperModel.getIndividual(subsumedPaperNode.asResource().getURI());
                        
                //System.out.println(classReification.getURI());
                //System.out.println(subsumedPaperNode.asResource().getURI());
                        
                classReification.addProperty(this.isConcernedIn, subsumedPaperNode);
                subsumedPaper.addProperty(this.concerns, classReification);
            }
        }
    }
}

public HashSet<RDFNode> inheritPapersForCategory(HashSet<RDFNode> initialSet, Individual category, OntProperty relation)
{
    if (category.hasProperty(this.isConcernedIn))
        initialSet.addAll(category.listPropertyValues(this.isConcernedIn).toSet());
    
    if (category.hasProperty(relation))
    {
        for (RDFNode subsumedClassNode : category.listPropertyValues(relation).toSet())
        {
            Individual subsumedClassInd = paperModel.getIndividual(subsumedClassNode.asResource().getURI());
            
            initialSet.addAll(inheritPapersForCategory(initialSet, subsumedClassInd, relation));
        }
    }
    
    return initialSet;
}

public void saveOntology()
{
    try {
            File newFile = new File (this.paperOntPostLocation);
            try {newFile.createNewFile();} catch (java.io.IOException e) {e.printStackTrace();}
        
            FileOutputStream writerPaper = new FileOutputStream(newFile);
            this.paperModel.write(writerPaper, "N-TRIPLE");
            
            try {
                    //writerConcept.close();
                    writerPaper.close();
                } catch (java.io.IOException ex) {ex.printStackTrace();}

                
            //this.conceptModel.close();
            this.paperModel.close();
                    
        } catch (java.io.FileNotFoundException ex) {ex.printStackTrace();}    
}
    
}












