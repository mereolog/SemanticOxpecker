/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prepare;



import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import java.io.*;

/**
 *
 * @author PG
 */
public class OntologyFix {
    
    
    String conceptOntPreLocation;
    String conceptOntPostLocation;
    String conceptOntURI;
    
    public OntModel conceptModel;
    
    OntClass categoryClass;
    OntClass individualClass;
    
    DatatypeProperty characterises;
    
    String lang; 
    
    boolean includePartial = true;
    
    public OntologyFix(String pre, String post, String uri)
    {
        conceptOntPreLocation=pre;
        conceptOntPostLocation=post;
        conceptOntURI=uri;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    
        
    OntologyFix newFix = new OntologyFix(   "data/ontologies/Conceptualisation.owl",//
                                            "data/ontologies/Conceptualisation.owl",//
                                            "http://www.l3g.pl/ontologies/OntoBeef/Conceptualisation.owl"        
                                        );
    
    newFix.loadOntology();
    //System.out.println("Conceptualisation ontology loaded");
    newFix.lang="pl";
        
    newFix.fixLabels();
    //System.out.println("Labels fixed");
    newFix.defineInverseProperties();
    newFix.addInverseProperties();
    //System.out.println("Inversion finished");
    
    //newFix.removeAnnotations();
    
    newFix.saveOntology(); 
    
}
  
    
    public void loadOntology()
    {
        this.conceptModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_RDFS_INF);
        this.conceptModel.read("file:"+this.conceptOntPreLocation, null, null);
        
        this.categoryClass = this.conceptModel.getOntClass(this.conceptOntURI+"#"+"Category");
        this.individualClass = this.conceptModel.getOntClass(this.conceptOntURI+"#"+"Individual");
        this.characterises = conceptModel.getDatatypeProperty(conceptOntURI+"#"+"characterisesPaper");
    }
    
    public void fixLabels ()
    {
        for (OntResource categoryResource : this.categoryClass.listInstances(false).toSet())
        {
            Individual category = this.conceptModel.getIndividual(categoryResource.getURI());
        
            if (category.listLabels(this.lang).toSet().isEmpty())
            {
                category.addLabel(category.getLocalName(), this.lang);
            }
        }
        
        for (OntResource individualResource : this.individualClass.listInstances(false).toSet())
        {
            Individual individual = this.conceptModel.getIndividual(individualResource.getURI());
        
            if (individual.listLabels(this.lang).toSet().isEmpty())
            {
                individual.addLabel(individual.getLocalName(), this.lang);
            }
        }  
    }
    
    public void defineInverseProperties ()
    {
        for (OntProperty property : this.conceptModel.listAllOntProperties().toSet())
        {
            if (!property.hasInverse())
            {
                ObjectProperty inverseProperty = this.conceptModel.createObjectProperty(this.conceptOntURI+"#"+"inverse_of_"+property.getLocalName());
                
                property.addInverseOf(inverseProperty);
                inverseProperty.addInverseOf(property);
                
                if (!property.listLabels(this.lang).toSet().isEmpty())
                {
                    for (RDFNode label : property.listLabels(this.lang).toSet())
                    {
                        inverseProperty.addLabel(label.asLiteral());
                    }
                }
            }
        }
    }
    
    public void addInverseProperties()
    {
        for (OntResource categoryResource : this.categoryClass.listInstances(false).toSet())
        {
            Individual category = this.conceptModel.getIndividual(categoryResource.getURI());
            
            if (!category.listProperties().toSet().isEmpty())
            {
                for (com.hp.hpl.jena.rdf.model.Statement statement : category.listProperties().toSet())
                {
                    ObjectProperty property = this.conceptModel.getObjectProperty(statement.getPredicate().getURI());
                    
                    if (statement.getObject().isURIResource()) 
                    {
                        Resource object = statement.getObject().asResource();
                    
                        if (this.conceptModel.getIndividual(object.getURI()) != null)
                        {
                            Individual objectIndividual = this.conceptModel.getIndividual(object.getURI());
                        
                            if (objectIndividual.hasOntClass(this.categoryClass))
                            {
                                Property inverseProperty = property.getInverseOf();
                                object.addProperty(inverseProperty, category);
                            }
                        }
                    }
                }
            }
        }   
    }
    
    public void removeAnnotations ()
    {
        for (Individual classReification : this.conceptModel.listIndividuals(this.categoryClass).toSet())
        {
            classReification.removeAll(this.characterises);
        }
    

        for (Individual individualReification : this.conceptModel.listIndividuals(this.individualClass).toSet())
        {
            individualReification.removeAll(this.characterises);
        }    
        
    }
    
    
    public void saveOntology ()
    {
        try {
                File newFile = new File (this.conceptOntPostLocation);
                try {newFile.createNewFile();} catch (java.io.IOException e) {e.printStackTrace();}
            
                FileOutputStream writer = new FileOutputStream(this.conceptOntPostLocation);
                this.conceptModel.write(writer, null, null);
            
                try {
                    writer.close();
                    } catch (java.io.IOException ex) {ex.printStackTrace();}

                this.conceptModel.close();
                    
            } catch (java.io.FileNotFoundException ex) {ex.printStackTrace();}
    }
}
