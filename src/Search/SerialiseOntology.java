/*
 
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Search;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import java.util.HashMap;
import java.io.*;

/**
 *
 * @author PG
 */
public class SerialiseOntology {
    
    String paperOntURI = "http://www.l3g.pl/ontologies/OntoBeef/Papers.owl";
    String paperOntLocation = "file:data/ontologies/Papers_Ready.owl";
    String dataOntURI = "http://www.l3g.pl/ontologies/OntoBeef/Science.owl";
    
    OntModel model;
    
    OntClass articleClass;
    OntClass titleClass;
    OntClass abstractClass;
    OntClass biblioClass;
    OntClass discussionClass;
    OntClass introClass;
    OntClass keywordClass;
    OntClass thesisClass;
    OntClass definitionClass;
    OntClass conclusionClass;
    OntClass authorClass;
    OntClass issueClass;
    OntClass collectionClass;
    OntClass containerClassJournal;
    OntClass journalClass;
    
        
    OntClass methodClass;
    OntClass problemClass;
    OntClass dataClass;
        
    Property concerns;
    Property based;
    Property hasPart;
    Property isPart;
    Property solves;
    Property uses;

    Property hasContent;
    Property isSummarised;
    Property isBasedOn;
    Property isAuthored;
    Property authorsRelation;
    Property isId;
    Property hasISSN;
    Property hasISBN;
    Property isYear;
    Property isVolume;
    Property pageRangeProperty;
    Property hasBib;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        SerialiseOntology newOnt = new SerialiseOntology();
        
        newOnt.serialiseOntology("data/OntologySerialisation.java");
    }

    public void loadOntology()
    {
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.setDynamicImports(true);
        model.read(paperOntLocation, paperOntURI, "N-TRIPLE");

        articleClass = model.getOntClass(paperOntURI+"#"+"Article");
        titleClass = model.getOntClass(paperOntURI+"#"+"Title");
        abstractClass = model.getOntClass(paperOntURI+"#"+"Abstract");
        biblioClass = model.getOntClass(paperOntURI+"#"+"Bibliography");
        discussionClass = model.getOntClass(paperOntURI+"#"+"Discussion_of_Results");
        introClass = model.getOntClass(paperOntURI+"#"+"Introduction");
        keywordClass = model.getOntClass(paperOntURI+"#"+"Keyword");
        thesisClass = model.getOntClass(paperOntURI+"#"+"Thesis");
        definitionClass = model.getOntClass(paperOntURI+"#"+"Definition");
        conclusionClass = model.getOntClass(paperOntURI+"#"+"Conclusion");
        authorClass = model.getOntClass(paperOntURI+"#"+"Individual_Agent");
        issueClass = model.getOntClass(paperOntURI+"#"+"Journal_Issue");
        collectionClass = model.getOntClass(paperOntURI+"#"+"Collection");
        journalClass = model.getOntClass(paperOntURI+"#"+"Journal_Issue");
        
        
        methodClass = model.getOntClass(dataOntURI+"#"+"Scientific_Method");
        problemClass = model.getOntClass(dataOntURI+"#"+"Scientific_Problem");
        dataClass = model.getOntClass(dataOntURI+"#"+"Data_Sample");
        

        concerns = model.getObjectProperty(paperOntURI+"#"+"concerns");
        based = model.getObjectProperty(paperOntURI+"#"+"isBasedOn");
        hasPart = model.getProperty(paperOntURI+"#"+"textHasPart");
        isPart = model.getProperty(paperOntURI+"#"+"isPartOfText");
        solves = model.getObjectProperty(paperOntURI+"#"+"solves");
        uses = model.getObjectProperty(paperOntURI+"#"+"uses");

        hasContent = model.getProperty(paperOntURI+"#"+"hasContent");
        isSummarised = model.getProperty(paperOntURI+"#"+"isSummarisedAs");
        isBasedOn = model.getProperty(paperOntURI+"#"+"isBasedOn");
        isAuthored = model.getProperty(paperOntURI+"#"+"isAuthoredBy");
        authorsRelation = model.getProperty(paperOntURI+"#"+"authors");
        isId = model.getProperty(paperOntURI+"#"+"isIdentifiedBy");
        hasISSN = model.getProperty(paperOntURI+"#"+"hasISSN");
        hasISBN = model.getProperty(paperOntURI+"#"+"hasISBN");
        isYear = model.getProperty(paperOntURI+"#"+"isPublishedIn");
        isVolume = model.getProperty(paperOntURI+"#"+"hasVolumeNumber");
        pageRangeProperty = model.getProperty(paperOntURI+"#"+"hasPageRange");
        hasBib = model.getProperty(paperOntURI+"#"+"hasBibTex");
    }
    
    

    public Prepare.Paper appendPaperDetails (Prepare.Paper paper)
    {
        Individual paperInd = this.model.getIndividual(paperOntURI+"#"+"Paper_"+paper.paperId);
        
        if (paperInd.hasProperty(hasPart))
        {
            for (RDFNode part : paperInd.listPropertyValues(hasPart).toSet())
            {
                Individual partInd = this.model.getIndividual(part.asResource().getURI());
                
                if (partInd.hasOntClass(titleClass))
                {
                    paper.paperTitle=partInd.getPropertyValue(hasContent).asLiteral().getString();
                }
                
                if (partInd.hasOntClass(abstractClass))
                {
                    if (partInd.hasProperty(hasContent)) paper.paperAbstract=partInd.getPropertyValue(hasContent).asLiteral().getString();
                    if (partInd.hasProperty(isSummarised)) paper.paperAbstractTranslation=partInd.getPropertyValue(isSummarised).asLiteral().getString();
                    
                }
                
                if (partInd.hasOntClass(thesisClass))
                {
                    paper.theses=partInd.getPropertyValue(isSummarised).asLiteral().getString();
                }
                
                if (partInd.hasOntClass(keywordClass))
                {
                    paper.keyword=partInd.getPropertyValue(hasContent).asLiteral().getString();
                }
            }
            
            
        }
        
        String paperAuthor="";
        if (paperInd.hasProperty(isAuthored))
        {
            for (RDFNode part : paperInd.listPropertyValues(isAuthored).toSet())
            {
                Individual partInd = this.model.getIndividual(part.asResource().getURI());
                
                if (partInd.hasOntClass(authorClass))
                {
                    //System.out.println(partInd.getURI());
                    
                    paperAuthor=paperAuthor+" "+partInd.getPropertyValue(isId).asLiteral().getString();
                    //break;
                }
            }
        }
        paper.paperAuthor=paperAuthor.trim();
        
        if (paperInd.hasProperty(isPart))
        {
            for (RDFNode part : paperInd.listPropertyValues(isPart).toSet())
            {
                Individual partInd = this.model.getIndividual(part.asResource().getURI());
                
                //System.out.println(partInd.getURI());
                
                if (partInd.hasOntClass(journalClass, true) || partInd.hasOntClass(collectionClass, true))
                {
                    //System.out.println(partInd.getURI());
                    
                    paper.publication=partInd.getPropertyValue(hasContent).asLiteral().getString();
                }
            }
        }
        
        if (paperInd.hasProperty(isYear))
        {
            paper.year=paperInd.getPropertyValue(isYear).asLiteral().getString();
        }
        
        if (paperInd.hasProperty(isVolume))
        {
            paper.volume=paperInd.getPropertyValue(isVolume).asLiteral().getString();
        }
        
        if (paperInd.hasProperty(pageRangeProperty))
        {
            paper.pages=paperInd.getPropertyValue(pageRangeProperty).asLiteral().getString();
        }
        
        if (paperInd.hasProperty(hasBib))
        {
            paper.bib=paperInd.getPropertyValue(hasBib).asLiteral().getString();
        }
        
        return paper;
    }
    
    public void serialiseOntology(String location)
    {
        loadOntology();
        //System.out.println("Ontology loaded");
        HashMap<Integer, Prepare.Paper> paperMap = new HashMap<Integer, Prepare.Paper>();
        
        for (Individual paperInd : this.model.listIndividuals(this.articleClass).toSet())
        {
            Prepare.Paper newPaper = new Prepare.Paper();
            newPaper.paperId=Integer.parseInt(paperInd.getURI().substring(paperInd.getURI().lastIndexOf("_")+1));
            //System.out.println(paperInd.getURI()+ " vs. "+newPaper.paperId);
            newPaper=this.appendPaperDetails(newPaper);
            
            paperMap.put(newPaper.paperId, newPaper);
        }
        
        SerializableOntology newSerializableOnt = new SerializableOntology();
        newSerializableOnt.paperMap=paperMap;
        
        //System.out.println("Serialisation ready");
        
        File newFile = new File (location);
        try {newFile.createNewFile();} catch (java.io.IOException e) {e.printStackTrace();}
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try
        {
            fos = new FileOutputStream(newFile);
            out = new ObjectOutputStream(fos);
            out.writeObject(newSerializableOnt);
            out.close();
        }
        catch(IOException ex) {ex.printStackTrace();}
        
        //System.out.println("Serialisation saved");
        
        this.model.close();
    }
    
    public SerializableOntology retrieveOntology(String serialisationLocation)
    {
        SerializableOntology retrievedOntology = null;
        
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try
        {
            fis = new FileInputStream(serialisationLocation);
            in = new ObjectInputStream(fis);
            try {
                    retrievedOntology = (SerializableOntology)in.readObject();
            } catch (java.lang.ClassNotFoundException ex) {ex.printStackTrace();}
            
            in.close();
        }
        
        catch(IOException ex) {ex.printStackTrace();}
        
        return retrievedOntology;
    }
    
    
}
