/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Prepare;


import java.io.FileOutputStream;
import java.io.File;
import java.util.HashMap;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;


/**
 *
 * @author PG
 */
public class DomainReification {

    
    String domainOntURI;
    String conceptOntURI;
    
    String domainOntLocation;
    String conceptOntPriorLocation;
    String conceptOntPostLocation;
    
    OntModel domainModel;
    OntModel conceptModel;
    
    OntClass category;
    OntClass relation;
    OntClass individual;
        
    ObjectProperty categorySubsumes;
    ObjectProperty instatiates;
    ObjectProperty categoryHasDomain;
    ObjectProperty categoryHasRange;
    ObjectProperty isRelatedTo;
    
    DatatypeProperty characterises;
    
    AnnotationProperty hasDomainCodeOld;
    AnnotationProperty hasDomainCodeNew;
    
    AnnotationProperty forPaper;
    
    AnnotationProperty properLabelDomain;
    AnnotationProperty properLabelConcept;
    
    DomainReification (String domain, String concept, String domainLoc, String conceptPreLoc, String conceptPostLoc)
    {
        domainOntURI=domain;
        conceptOntURI=concept;
        
        domainOntLocation=domainLoc;
        conceptOntPriorLocation=conceptPreLoc;
        conceptOntPostLocation=conceptPostLoc;
    
    }
    
    public static void main(String[] args) {
        // TODO code application logic here

        HashMap<OntClass, Individual> classToIndMap = new HashMap<OntClass, Individual>();
        HashMap<Property, Individual> propertyToIndMap = new HashMap<Property, Individual>();
        HashMap<Individual, Individual> individualToIndMap = new HashMap<Individual, Individual>();
        
        DomainReification newReification = new DomainReification(   "http://www.l3g.pl/ontologies/OntoBeef/Domain.owl", //
                                                        "http://www.l3g.pl/ontologies/OntoBeef/Conceptualisation.owl", //
                                                        "http://www.l3g.pl/ontologies/OntoBeef/Domain_last.owl", //
                                                        "data/ontologies/Initialisation/Conceptualisation.owl",//
                                                        "data/ontologies/Conceptualisation.owl");
        
        newReification.loadOntologies();
        //System.out.println("Ontologies loaded");
        classToIndMap=newReification.reifyClasses(classToIndMap);
        propertyToIndMap=newReification.reifyProperties(propertyToIndMap);
        //System.out.println("Category reification complete");
        individualToIndMap=newReification.reifyIndividuals(individualToIndMap, classToIndMap);
        
        newReification.reifyDomainAndRange(classToIndMap, propertyToIndMap);
        newReification.reifyRestrictions(classToIndMap);
        //System.out.println("Reification complete");
        newReification.subsumeClasses(classToIndMap);
        newReification.subsumeProperties(propertyToIndMap);
        //System.out.println("Subsumptiom complete");
        newReification.saveOntology();
    }

public void loadOntologies ()
{
    this.domainModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
    this.domainModel.setDynamicImports(true);
    this.domainModel.read(domainOntLocation, domainOntURI, null);
    this.domainModel.loadImports();
    
    this.conceptModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    this.conceptModel.read("file:"+conceptOntPriorLocation, conceptOntURI, null);
        
    this.category = conceptModel.getOntClass(conceptOntURI+"#"+"Unary_Category");
    this.relation = conceptModel.getOntClass(conceptOntURI+"#"+"Relation");
    this.individual = conceptModel.getOntClass(conceptOntURI+"#"+"Individual");
        
    this.categorySubsumes = conceptModel.getObjectProperty(conceptOntURI+"#"+"categorySubsumes");
    this.instatiates = conceptModel.getObjectProperty(conceptOntURI+"#"+"categoryInstantiates");
    this.categoryHasDomain = conceptModel.getObjectProperty(conceptOntURI+"#"+"categoryHasDomain");
    this.categoryHasRange = conceptModel.getObjectProperty(conceptOntURI+"#"+"categoryHasRange");
    this.isRelatedTo = conceptModel.getObjectProperty(conceptOntURI+"#"+"isRelatedToByRestriction");
    
    this.hasDomainCodeOld = domainModel.getAnnotationProperty(domainOntURI+"#"+"hasDomainCode");
    this.hasDomainCodeNew = conceptModel.createAnnotationProperty(conceptOntURI+"#"+"hasDomainCode");
    this.forPaper = domainModel.getAnnotationProperty(domainOntURI+"#"+"characterisesPaper");
       
    this.characterises = conceptModel.getDatatypeProperty(conceptOntURI+"#"+"characterisesPaper");
    
    this.domainModel.setStrictMode(false);
    this.conceptModel.setStrictMode(false);
    
    this.properLabelDomain = domainModel.getAnnotationProperty("http://www.l3g.pl/ontologies/OntoBeef/Domain.owl#proper_label");
    this.properLabelConcept = conceptModel.createAnnotationProperty("http://www.w3.org/2000/01/rdf-schema#label");
    
}

public HashMap<OntClass, Individual> reifyClasses (HashMap<OntClass, Individual> map)
{
    int unionCount = 0;
    int interCount = 0;
    
    for (OntClass domainClass : this.domainModel.listClasses().toSet())
    {
        String reificationName = "";
        boolean doCreateClass = false;
        
        if (domainClass.isAnon())
        {
            if (domainClass.isUnionClass())
            {
                reificationName=this.conceptOntURI+"#"+"UnionOf_"+unionCount;
                doCreateClass = true;
                unionCount++;
            }
            
            if (domainClass.isIntersectionClass())
            {
                reificationName=this.conceptOntURI+"#"+"Intersection_"+interCount;
                doCreateClass = true;
                interCount++;
            }
        }
        
        else 
        {
            reificationName = this.conceptOntURI+"#"+domainClass.getLocalName().toLowerCase();
            doCreateClass = true;
        }
        
        if (doCreateClass)
        {
            Individual classReification = this.conceptModel.createIndividual(reificationName, this.category);
            
            if (domainClass.hasProperty(this.properLabelDomain))
            {
                for (RDFNode label : domainClass.listPropertyValues(this.properLabelDomain).toSet())
                {
                    //classReification.addProperty(this.properLabelConcept, label);
                    classReification.addLabel(label.asLiteral());
                }
            }
            
            for (RDFNode label : domainClass.listLabels(null).toList())
            {
                classReification.addLabel(label.asLiteral());
            }
            
            if (domainClass.hasProperty(this.hasDomainCodeOld)) classReification.addProperty(this.hasDomainCodeNew, domainClass.getPropertyValue(this.hasDomainCodeOld).asLiteral());
            
            if (domainClass.hasProperty(this.forPaper)) 
                for (RDFNode paperId : domainClass.listPropertyValues(forPaper).toSet())
                {
                    classReification.addProperty(this.characterises, paperId);
                }
        
                map.put(domainClass, classReification);
        }
        
    }
    
    return map;
}

public HashMap<Property, Individual> reifyProperties (HashMap<Property, Individual> map)
{
    for (ObjectProperty domainProperty : this.domainModel.listObjectProperties().toSet())
    {
        Individual propertyReification = this.conceptModel.createIndividual(this.conceptOntURI+"#"+domainProperty.getLocalName().toLowerCase(), this.relation);
            
        for (RDFNode label : domainProperty.listLabels(null).toList())
        {
            propertyReification.addLabel(label.asLiteral());
        }
        
        if (domainProperty.hasProperty(this.properLabelDomain))
        {
            for (RDFNode label : domainProperty.listPropertyValues(this.properLabelDomain).toSet())
            {
                propertyReification.addLabel(label.asLiteral());
            }
        }
            
        if (domainProperty.hasProperty(this.hasDomainCodeOld)) domainProperty.addProperty(this.hasDomainCodeNew, domainProperty.getPropertyValue(this.hasDomainCodeOld).asLiteral());
            
        if (domainProperty.hasProperty(this.forPaper)) 
            for (RDFNode paperId : domainProperty.listPropertyValues(forPaper).toSet())
            {
                propertyReification.addProperty(this.characterises, paperId);
            }
        
        map.put(domainProperty, propertyReification);
    }
    
    return map;
}

public HashMap<Individual, Individual> reifyIndividuals (HashMap<Individual, Individual> mapInd, HashMap<OntClass, Individual> mapClass)
{
    for (Individual domainIndividual : this.domainModel.listIndividuals().toSet())
    {
        Individual individualReification = this.conceptModel.createIndividual(this.conceptOntURI+"#"+domainIndividual.getLocalName().toLowerCase(), this.individual);
            
        for (RDFNode label : domainIndividual.listLabels(null).toList())
        {
            individualReification.addLabel(label.asLiteral());
        }
        
        if (domainIndividual.hasProperty(this.properLabelDomain))
        {
            for (RDFNode label : domainIndividual.listPropertyValues(this.properLabelDomain).toSet())
            {
                individualReification.addLabel(label.asLiteral());
            }
        }
        
        if (domainIndividual.listOntClasses(true) != null)
        {
            for (OntClass domainClassForInd : domainIndividual.listOntClasses(true).toSet())
            {
                if (mapClass.containsKey(domainClassForInd)) individualReification.addProperty(instatiates, mapClass.get(domainClassForInd));
            }
        }
            
        if (domainIndividual.hasProperty(hasDomainCodeOld)) individualReification.addProperty(hasDomainCodeNew, domainIndividual.getPropertyValue(hasDomainCodeOld).asLiteral());
            
        if (domainIndividual.hasProperty(this.forPaper)) 
            for (RDFNode paperId : domainIndividual.listPropertyValues(forPaper).toSet())
            {
                individualReification.addProperty(this.characterises, paperId);
            }
            
        mapInd.put(domainIndividual, individualReification);
    }
    
    return mapInd;
}

public void subsumeClasses (HashMap<OntClass, Individual> map)
{
    for (OntClass domainClass : domainModel.listNamedClasses().toSet())
    {
        if (domainClass.hasSubClass())
        {
            for (OntClass subClass : domainClass.listSubClasses(false).toSet())
            {
                map.get(domainClass).addProperty(categorySubsumes, map.get(subClass));
            }
        }
    }    
}

public void subsumeProperties (HashMap<Property, Individual> map)
{
    for (ObjectProperty domainProperty : this.domainModel.listObjectProperties().toSet())
    {
        if (domainProperty.listSubProperties() != null)
        {
            for (OntProperty subProperty : domainProperty.listSubProperties(false).toSet())
            {
                map.get(domainProperty).addProperty(categorySubsumes, map.get(subProperty));
            }
        }
    } 
}

public void reifyRestrictions (HashMap<OntClass, Individual> map)
{
    for (Restriction restrictionClass : domainModel.listRestrictions().toSet())
    {
        if (restrictionClass.isSomeValuesFromRestriction())
        {
            OntClass relatedClass = (OntClass) restrictionClass.asSomeValuesFromRestriction().getSomeValuesFrom();
                
            for (OntClass subClass : restrictionClass.listSubClasses().toSet())
            {
                if (map.containsKey(subClass) && map.containsKey(relatedClass)) map.get(subClass).addProperty(isRelatedTo, map.get(relatedClass));
            }
        }
                        
        if (restrictionClass.isAllValuesFromRestriction())
        {
            OntClass relatedClass = (OntClass) restrictionClass.asAllValuesFromRestriction().getAllValuesFrom();
                
            for (OntClass subClass : restrictionClass.listSubClasses().toSet())
            {
                    if (map.containsKey(subClass) && map.containsKey(relatedClass)) map.get(subClass).addProperty(isRelatedTo, map.get(relatedClass));
            }
        }
    }
}

public void reifyDomainAndRange (HashMap<OntClass, Individual> mapClass, HashMap<Property, Individual> mapProp)
{
    for (ObjectProperty domainProperty : domainModel.listObjectProperties().toSet())
    {
        if (domainProperty.getDomain() != null)
         {
             //System.out.println(domainProperty.getURI());
             //System.out.println(domainProperty.getDomain().getURI());
             
             mapProp.get(domainProperty).addProperty(categoryHasDomain, mapClass.get(domainProperty.getDomain().asClass()));
        }
            
        if (domainProperty.getRange() != null)
        {
            mapProp.get(domainProperty).addProperty(categoryHasRange, mapClass.get(domainProperty.getRange().asClass()));
        }
    }
        
}

public void saveOntology()
{
    try {
            File newFile = new File (this.conceptOntPostLocation);
            try {newFile.createNewFile();} catch (java.io.IOException e) {e.printStackTrace();}
                    
            FileOutputStream writerConcept = new FileOutputStream(newFile);
            this.conceptModel.write(writerConcept);
            
            try {
                    writerConcept.close();
                    
                } catch (java.io.IOException ex) {ex.printStackTrace();}

                
            this.domainModel.close();
            this.conceptModel.close();
                    
        } catch (java.io.FileNotFoundException ex) {ex.printStackTrace();}    
}
    
}












