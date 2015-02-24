/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prepare;


import java.util.ArrayList;
import java.sql.*;
import java.util.regex.*;
import java.util.HashSet;
import java.util.HashMap;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import java.io.*;


/**
 *
 * @author PG
 */
public class Expander implements Serializable {
    
    public static morfologik.stemming.PolishStemmer stemmer = new morfologik.stemming.PolishStemmer();
    public static final String databaseConnectionData = "jdbc:mysql://mysql1.hekko.net.pl/garbacz_stoplista?useUnicode=yes&characterEncoding=UTF-8";
    
    public static final HashMap<String, String> locationNameMap = new HashMap<String, String>();
    
    HashSet<String> lang; 
    //boolean includePartial = true;
    
    String expanderName;
    
    HashSet<String> stopList = new HashSet<String>();
    
    String paperOntLocation;
    
    String conceptOntURI;
    String paperOntURI;
    
    OntModel paperModel;
    
    Property concerns;
    OntClass paperClass;
    
    OntClass categoryClass;
    OntClass individualClass;
    
    HashMap<Individual, HashSet<String>> categoryToPaperURIMap;
    HashMap<String, HashSet<Individual>> paperURIToCategoryMap;
    HashMap<String, HashSet<Individual>> keywordToCategoryMap;
    public HashMap<String, HashSet<String>> keywordToPaperURIMap;
    HashMap<Individual, HashSet<String>> categoryToKeywordMap;
    
    public HashSet<ConceptualLink> extenstionList = new HashSet<ConceptualLink>();
    
    public ArrayList<LinkType> expandList;
    
    public ArrayList<String> expandNameList;
    
    public String serialisationLocation;
    
    public Search.Search newSearch;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
    locationNameMap.put("categorySubsumes", "s");
    locationNameMap.put("inverse_of_categorySubsumes", "invs");
    locationNameMap.put("categoryInstantiates", "i");
    locationNameMap.put("inverse_of_categoryInstantiates", "invi");
    locationNameMap.put("isRelatedToByRestriction", "r");
    locationNameMap.put("inverse_of_isRelatedToByRestriction", "invr");
    
       
    String expanderListAbbreviation = args[0];
    ArrayList<String> expandListName = new ArrayList<String>();
    
    for (int i=0;i<expanderListAbbreviation.length();i++)
    {
       String exAbbr = String.valueOf(expanderListAbbreviation.charAt(i));
       if (exAbbr.equals("i")) expandListName.add("inverse_of_categorySubsumes");
       else
           if (exAbbr.equals("r")) expandListName.add("isRelatedToByRestriction");
           else
               if (exAbbr.equals("b")) 
               {
                   if (expanderListAbbreviation.length() == 1) break;
                   else
                   {
                       System.err.println("BAD EXPANDER SPEC AT "+i+" equal to"+exAbbr);
                       System.exit(i);
                   }
               }
               else
               {
                    System.err.println("BAD EXPANDER SPEC AT "+i+" equal to"+exAbbr);
                    System.exit(i);
               }
    }
    
    
    
    Expander newExpander = new Expander();
    
    if (expanderListAbbreviation.equals("b")) newExpander.expanderName="Basic";
    else newExpander.expanderName=expanderListAbbreviation;
    newExpander.newSearch = new Search.Search();
    
    newExpander.conceptOntURI="http://www.l3g.pl/ontologies/OntoBeef/Conceptualisation.owl";
    newExpander.paperOntURI= "http://www.l3g.pl/ontologies/OntoBeef/Papers.owl";
    newExpander.paperOntLocation= "data/ontologies/Papers_Ready.owl";
    newExpander.expandNameList = expandListName;
    newExpander.lang = new HashSet<String>();
    
    Pattern langAbbrPattern = Pattern.compile("(.*?)@");
    Matcher langAbbrMatch = langAbbrPattern.matcher(args[1]);
    while (langAbbrMatch.find())
    {
        newExpander.lang.add(langAbbrMatch.group(1));
    }
   
    //newExpander.lang.add("pl");
    //newExpander.lang.add("en");
    
    newExpander.categoryToKeywordMap=new HashMap<Individual, HashSet<String>>();
    newExpander.categoryToPaperURIMap= new HashMap<Individual, HashSet<String>>();
    newExpander.keywordToCategoryMap= new HashMap<String, HashSet<Individual>>();
    newExpander.paperURIToCategoryMap=new HashMap<String, HashSet<Individual>>();
    newExpander.keywordToPaperURIMap=new HashMap<String, HashSet<String>>();
    
           
    newExpander.loadOntologies();
    //System.out.println("Ontologies loaded");
    newExpander.stopList = newExpander.loadStopList();
    //System.out.println("Stoplist loaded");
    newExpander.setStoredLinks();
    //System.out.println("Links loaded");
    newExpander.expandLinks();
    //System.out.println("Links expanded");
    //System.out.println(newExpander.keywordToPaperURIMap.keySet().size());
    //newExpander.defineSerialisationLocation();
    newExpander.serialisationLocation="data/expanders/"+newExpander.expanderName+".exp";
    newExpander.serializeExpander();
}
    
    
    public void loadOntologies ()
    {
        this.paperModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_RDFS_INF);
        paperModel.setDynamicImports(true);
        this.paperModel.read("file:"+this.paperOntLocation, paperOntURI, "N-TRIPLE");
        paperModel.loadImports();
    
        this.concerns = this.paperModel.getProperty(paperOntURI+"#"+"concerns");
        this.paperClass = this.paperModel.getOntClass(paperOntURI+"#"+"Article");
        this.categoryClass = this.paperModel.getOntClass(conceptOntURI+"#"+"Category");
        this.individualClass=this.paperModel.getOntClass(conceptOntURI+"#"+"Individual");
        
    }
    
    public HashSet<String> loadStopList()
    {
        HashSet<String> stopList = new HashSet<String>();
        
        try {Class.forName("com.mysql.jdbc.Driver");} catch (ClassNotFoundException ex) {ex.printStackTrace();}

        try {
        
            Connection conn = DriverManager.getConnection(this.databaseConnectionData, "garbacz_garbacz", "sagan1");
        
            java.sql.Statement findStopWord = conn.createStatement();
            ResultSet rsStopWord = findStopWord.executeQuery("SELECT Content FROM StopList");
            while (rsStopWord.next())
            {
                stopList.add(rsStopWord.getString("Content").trim());
            }
        
            } catch (java.sql.SQLException ex) {ex.printStackTrace();}
        
        return stopList;
    
    }
    
    public void setStoredLinks()
    {

//******************************************************************************
//OD SŁÓW KLUCZOWYCH DO KATEGORII I Z POWROTEM
//******************************************************************************
        
        HashSet<Individual> categorySet = new HashSet<Individual>();
        categorySet.addAll(this.paperModel.listIndividuals(this.categoryClass).toSet());
        categorySet.addAll(this.paperModel.listIndividuals(this.individualClass).toSet());
        
        for (Individual category : categorySet)
        {
            if (!findLabels(category).isEmpty())
            {
                
                for (RDFNode label : findLabels(category))
                {
                    String labelValue = label.asLiteral().getString();
                    
                    if (labelValue.length()<1)
                    {
                        System.err.println("Category "+category.getURI()+" has empty label "+label);
                        System.exit(1);
                    }
                    
                    if (!this.stopList.contains(labelValue))
                    {
                        if (this.keywordToCategoryMap.containsKey(labelValue))
                        {
                            this.keywordToCategoryMap.get(labelValue).add(category);
                        }
                        else
                        {
                            HashSet<Individual> categoryListForKeyword = new HashSet<Individual>();
                            categoryListForKeyword.add(category);
                                
                            this.keywordToCategoryMap.put(labelValue, categoryListForKeyword);
                        }
                    
                        if (this.categoryToKeywordMap.containsKey(category))
                        {
                            this.categoryToKeywordMap.get(category).add(labelValue);
                        }
                        else
                        {
                            HashSet<String> keywordList = new HashSet<String>();
                            keywordList.add(labelValue);
                            this.categoryToKeywordMap.put(category, keywordList);
                        }
                            
                     if (labelValue.contains(" "))
                        {
                            ArrayList<String> labelItemList = new ArrayList<String>(this.newSearch.findStemList(labelValue));
                                
                            if (labelItemList.size()>0)
                            {
                                String stemmedLabel = this.newSearch.concatenateList(labelItemList);
                                
                                if (this.keywordToCategoryMap.containsKey(stemmedLabel))
                                {
                                    this.keywordToCategoryMap.get(stemmedLabel).add(category);
                                }
                                else
                                {
                                    HashSet<Individual> categoryListForKeyword = new HashSet<Individual>();
                                    categoryListForKeyword.add(category);
                                
                                    this.keywordToCategoryMap.put(stemmedLabel, categoryListForKeyword);
                                }
                            
                                if (this.categoryToKeywordMap.containsKey(category))
                                {
                                    this.categoryToKeywordMap.get(category).add(stemmedLabel);
                                }
                                else
                                {
                                    HashSet<String> keywordList = new HashSet<String>();
                                    keywordList.add(stemmedLabel);
                                
                                    this.categoryToKeywordMap.put(category, keywordList);
                                }
                            }
                        }
                    }
                 }
             }
             else
            {
                System.err.println("Category "+category.getURI()+" has no labels");
                System.exit(1);
            }
        }
        
        
//******************************************************************************
//OD URI ARTYKUŁÓW DO KATEGORII
//******************************************************************************
        
        for (Individual paper : this.paperModel.listIndividuals(this.paperClass).toSet())
        {
            String paperURI = paper.getURI();
            
            HashSet<Individual> categoryList = new HashSet<Individual>();
            
            if (paper.hasProperty(this.concerns))
            {
                for (RDFNode categoryNode : paper.listPropertyValues(this.concerns).toSet())
                {
                    Individual category = this.paperModel.getIndividual(categoryNode.asResource().getURI());
                    if (!stopList.contains(category.getLocalName())) categoryList.add(category);
                }
            }
            
            this.paperURIToCategoryMap.put(paperURI, categoryList);
            
        }
       
//******************************************************************************
//OD KATEGORII DO URI ARTYKUŁÓW
//******************************************************************************
        
        for (String paperURI : this.paperURIToCategoryMap.keySet())
        {
            for (Individual category : this.paperURIToCategoryMap.get(paperURI))
            {
                if (this.categoryToPaperURIMap.containsKey(category))
                {
                    this.categoryToPaperURIMap.get(category).add(paperURI);
                }
                else
                {
                    HashSet<String> paperURIList = new HashSet<String>();
                    paperURIList.add(paperURI);
                    
                    this.categoryToPaperURIMap.put(category, paperURIList);
                }    
            }
        }
        
//******************************************************************************
//OD SŁÓW KLUCZOWYCH DO URI ARTYKUŁÓW
//******************************************************************************       
        
        for (String keyword : this.keywordToCategoryMap.keySet())
        {
            for (Individual category : this.keywordToCategoryMap.get(keyword))
            {
                if (this.categoryToPaperURIMap.containsKey(category))
                {
                    if (this.keywordToPaperURIMap.containsKey(keyword))
                    {
                        this.keywordToPaperURIMap.get(keyword).addAll(this.categoryToPaperURIMap.get(category));
                    }
                    else
                    {
                        HashSet<String> paperURIList = new HashSet<String>();
                        paperURIList.addAll(this.categoryToPaperURIMap.get(category));
                    
                        this.keywordToPaperURIMap.put(keyword, paperURIList);
                       
                    }
                }
            }
        }
    }
    
    public void expandLinks()
    {
        ArrayList<LinkType> newexpandList = new ArrayList<LinkType>();
        
        for (String expandName :  this.expandNameList)
        {
            LinkType newLink = new LinkType(1, this.paperModel.getObjectProperty(this.conceptOntURI+"#"+expandName));
            newexpandList.add(newLink);
        }
        
        this.expandList = newexpandList;
        
        int count=0;
        
        for (OntResource category : this.categoryClass.listInstances(false).toSet())
        {
            //System.out.println("Category"+category);
            if (!this.stopList.contains(category.getLocalName()))
            {
                ConceptualLink newLink = new ConceptualLink((Individual)category, this.expandList);
                newLink.endListOfPaperURI=newLink.createEndList(this.paperModel, this.expandList, this);
         
                this.extenstionList.add(newLink);
            }
            
        }
      
        count=0;
        
        for (ConceptualLink link :  this.extenstionList)
        {
            if (!link.endListOfPaperURI.isEmpty()) count++; //System.out.println(link.beginCategory.getLocalName()+" > "+link.endListOfPaperURI);
            
            if (this.categoryToKeywordMap.containsKey(link.beginCategory))
            {
                for (String keyword : this.categoryToKeywordMap.get(link.beginCategory))
                {
                    if (this.keywordToPaperURIMap.containsKey(keyword)) 
                    {
                        this.keywordToPaperURIMap.get(keyword).addAll(link.endListOfPaperURI);
                    }
                    else
                    {
                        //System.out.println("No map to paper for extension of keyword"+keyword);
                        
                        if (!link.endListOfPaperURI.isEmpty())
                        {
                            HashSet<String> extendedPaperURIList = new HashSet<String>();
                            extendedPaperURIList.addAll(link.endListOfPaperURI);
                            this.keywordToPaperURIMap.put(keyword, extendedPaperURIList);
                        }
                    }
                }
            }
            else
            {
                System.out.println("No map to keyword for category"+link.beginCategory.getLocalName());
                
                for (String keyword : this.categoryToKeywordMap.get(link.beginCategory))
                {
                    HashSet<String> extendedPaperURIList = new HashSet<String>();
                    extendedPaperURIList.addAll(link.endListOfPaperURI);
                    this.keywordToPaperURIMap.put(keyword, extendedPaperURIList);
                }
            }
        }
        
        //System.out.println(count);
    }
    
    public void serializeExpander ()
    {
        Pattern uriPattern = Pattern.compile("#Paper_(\\d*?)\\z");
        
        SerializableExpander serialExpander = new SerializableExpander();
        HashMap<String, HashSet<String>> keywordToPaperIdMap = new HashMap<String, HashSet<String>>();
        
        
        for (String keyword : this.keywordToPaperURIMap.keySet())
        {
            HashSet<String> paperIdSet = new HashSet<String>();
            
            for (String paperURI : this.keywordToPaperURIMap.get(keyword))
            {
                String paperId = null;
                Matcher uriMatch = uriPattern.matcher(paperURI);
                while (uriMatch.find())
                {
                    paperId = uriMatch.group(1);
                }
                
                paperIdSet.add(paperId);
            }
            
            keywordToPaperIdMap.put(keyword, paperIdSet);
        }
        
        serialExpander.keywordToPaperIdMap=keywordToPaperIdMap;
        serialExpander.serialisationLocation=this.serialisationLocation;
        
        File newFile = new File (this.serialisationLocation);
        try {newFile.createNewFile();} catch (java.io.IOException e) {e.printStackTrace();}
        
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try
        {
            fos = new FileOutputStream(newFile);
            out = new ObjectOutputStream(fos);
            out.writeObject(serialExpander);
            out.close();
        }
        catch(IOException ex) {ex.printStackTrace();}
    }
    
    public void defineSerialisationLocation ()
    {
        this.serialisationLocation="data/expanders/Expansion_";
        
        for (String linkTypeName : this.expandNameList)
        {
            this.serialisationLocation=this.serialisationLocation+"-"+this.locationNameMap.get(linkTypeName);
        }
        
        this.serialisationLocation=this.serialisationLocation+".exp";
    }
    
    public Expander loadExpander(String location)
    {
        //HashMap<String, HashSet<String>> newMap = new HashMap<String, HashSet<String>>();
        
        SerializableExpander newSerialExpander = null;
        
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try
        {
            fis = new FileInputStream(location);
            in = new ObjectInputStream(fis);
            try {
                newSerialExpander = (SerializableExpander)in.readObject();
                
                //newMap = newExpander.keywordToPaperIdMap;
            } catch (java.lang.ClassNotFoundException ex) {ex.printStackTrace();}
            
            in.close();
        }
        
        catch(IOException ex) {ex.printStackTrace();}
        
        Expander newExpander = new Expander();
        newExpander.keywordToPaperURIMap = newSerialExpander.keywordToPaperIdMap;
        newExpander.serialisationLocation = newSerialExpander.serialisationLocation;
        
        return newExpander;
    }
    
    public HashSet<RDFNode> findLabels(Individual category)
    {
        HashSet<RDFNode> listOfLabels = new HashSet<RDFNode>();
              
        for (String language : this.lang)
        {
            for (RDFNode label : category.listLabels(language).toSet()) 
            {
                listOfLabels.add(label);
            }
        }
               
        return listOfLabels;
    }
    
}
