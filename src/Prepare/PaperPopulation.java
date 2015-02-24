/*
 
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Prepare;

import java.sql.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import java.io.*;
import java.util.regex.*;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.ArrayList;
import org.apache.commons.net.ftp.*;

/**
 *
 * @author PG
 */
public class PaperPopulation {
    
    static String paperOntURI = "http://www.l3g.pl/ontologies/OntoBeef/Papers.owl";
    static String dataOntURI = "http://www.l3g.pl/ontologies/OntoBeef/Science.owl";
    static String paperOntPopulatedLocation = "data/ontologies/Papers_Populated.owl";
    static String paperOntPopulatedURI = "http://www.l3g.pl/ontologies/OntoBeef/Papers_Populated.owl";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        int paperCount=0;
        int titleCount=0;
        int abstractCount=0;
        int keywordCount=0;
        int biblioCount=0;
        int problemCount=0;
        int introCount=0;
        int discussionCount=0;
        int thesisCount=0;
        int methodCount=0;
        int definitionCount=0;
        int dataCount=0;
        int conclusionCount=0;
        int authorCount=0;
        int issueCount=0;
        int collectionCount=0;
        
        
        Connection connDB = null;
                            
        

        OntModel paperModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        paperModel.read(paperOntURI, null, null);

        OntClass articleClass = paperModel.getOntClass(paperOntURI+"#"+"Article");
        OntClass titleClass = paperModel.getOntClass(paperOntURI+"#"+"Title");
        OntClass abstractClass = paperModel.getOntClass(paperOntURI+"#"+"Abstract");
        OntClass biblioClass = paperModel.getOntClass(paperOntURI+"#"+"Bibliography");
        OntClass discussionClass = paperModel.getOntClass(paperOntURI+"#"+"Discussion_of_Results");
        OntClass introClass = paperModel.getOntClass(paperOntURI+"#"+"Introduction");
        OntClass keywordClass = paperModel.getOntClass(paperOntURI+"#"+"Keyword");
        OntClass thesisClass = paperModel.getOntClass(paperOntURI+"#"+"Thesis");
        OntClass definitionClass = paperModel.getOntClass(paperOntURI+"#"+"Definition");
        OntClass conclusionClass = paperModel.getOntClass(paperOntURI+"#"+"Conclusion");
        OntClass authorClass = paperModel.getOntClass(paperOntURI+"#"+"Individual_Agent");
        OntClass issueClass = paperModel.getOntClass(paperOntURI+"#"+"Journal_Issue");
        OntClass collectionClass = paperModel.getOntClass(paperOntURI+"#"+"Collection");
        
        OntClass methodClass = paperModel.getOntClass(dataOntURI+"#"+"Scientific_Method");
        OntClass problemClass = paperModel.getOntClass(dataOntURI+"#"+"Scientific_Problem");
        OntClass dataClass = paperModel.getOntClass(dataOntURI+"#"+"Data_Sample");
        

        Property concerns = paperModel.getObjectProperty(paperOntURI+"#"+"concerns");
        Property based = paperModel.getObjectProperty(paperOntURI+"#"+"isBasedOn");
        Property hasPart = paperModel.getProperty(paperOntURI+"#"+"textHasPart");
        Property isPart = paperModel.getProperty(paperOntURI+"#"+"isPartOfText");
        Property solves = paperModel.getObjectProperty(paperOntURI+"#"+"solves");
        Property uses = paperModel.getObjectProperty(paperOntURI+"#"+"uses");

        Property hasContent = paperModel.getProperty(paperOntURI+"#"+"hasContent");
        Property isSummarised = paperModel.getProperty(paperOntURI+"#"+"isSummarisedAs");
        Property isBasedOn = paperModel.getProperty(paperOntURI+"#"+"isBasedOn");
        Property isAuthored = paperModel.getProperty(paperOntURI+"#"+"isAuthoredBy");
        Property authorsRelation = paperModel.getProperty(paperOntURI+"#"+"authors");
        Property isId = paperModel.getProperty(paperOntURI+"#"+"isIdentifiedBy");
        Property hasISSN = paperModel.getProperty(paperOntURI+"#"+"hasISSN");
        Property hasISBN = paperModel.getProperty(paperOntURI+"#"+"hasISBN");
        Property isYear = paperModel.getProperty(paperOntURI+"#"+"isPublishedIn");
        Property isVolume = paperModel.getProperty(paperOntURI+"#"+"hasVolumeNumber");
        Property pageRangeProperty = paperModel.getProperty(paperOntURI+"#"+"hasPageRange");
        Property hasBib = paperModel.getProperty(paperOntURI+"#"+"hasBibTex");
        //System.out.println(hasBib);
        //System.exit(1);

        Pattern authorColonPattern = Pattern.compile("(.*?),");
        Pattern authorSemiColonPattern = Pattern.compile("(.*?);");
        Pattern authorAndPattern = Pattern.compile("(.*?) and (.*?)\\z", Pattern.DOTALL);
        
        Pattern journalPattern = Pattern.compile("(IS\\wN) ([\\d\\-]*?) (.*?)\\z", Pattern.DOTALL);
        
        
        HashMap<String, String> langMap = new HashMap<String, String>();
        HashMap<String, Integer> keywordFreq = new HashMap<String, Integer>();
        
        HashMap<String, Integer> authorMap = new HashMap<String, Integer>();
        HashMap<String, Integer> issnMap = new HashMap<String, Integer>();
        HashMap<String, Integer> isbnMap = new HashMap<String, Integer>();

        try {Class.forName("com.mysql.jdbc.Driver");} catch (ClassNotFoundException ex) {ex.printStackTrace();}
        
        langMap.put("angielski", "en");
        langMap.put("niemiecki", "de");
        langMap.put("francuski", "fr");
        langMap.put("polski", "pl");
        langMap.put("hiszpański", "es");

        try {
                connDB = DriverManager.getConnection("jdbc:mysql://localhost:8889/OntoBeef?useUnicode=yes&characterEncoding=UTF-8", "root", "root");

                java.sql.Statement findData = connDB.createStatement();

                ResultSet findDataRS = findData.executeQuery("SELECT ID, Artykuł, Tytuł_tłumaczenie, Słowa_kluczowe, Abstrakt_oryginalny, Abstrakt_tłumaczenie, Autorzy, Bibliografia, Cel_badania_i_zakres, Czasopismo, Definicje_zagadnień_i_pojęć, Dyskusja, Język, Materiał_badawczy, Metodyka_badawcza, Tezy_dowiedzione, Tezy_obalone, Wnioski, Wstęp, Wyniki_badań, Rok_wydania, Numer_wydania, Od_strony, Do_strony, BibTex FROM Papers");
                while (findDataRS.next())
                {
                    String title = findDataRS.getString("Artykuł");
                    String titleTransl = findDataRS.getString("Tytuł_tłumaczenie");
                    String keywords = findDataRS.getString("Słowa_kluczowe");
                    String abstractOrg = findDataRS.getString("Abstrakt_oryginalny");
                    String abstractTransl = findDataRS.getString("Abstrakt_tłumaczenie");
                    String authors = findDataRS.getString("Autorzy");
                    String bibliography = findDataRS.getString("Bibliografia");
                    String problem = findDataRS.getString("Cel_badania_i_zakres");
                    String journal = findDataRS.getString("Czasopismo");
                    String definitions = findDataRS.getString("Definicje_zagadnień_i_pojęć");
                    String discussion = findDataRS.getString("Dyskusja");
                    String language = findDataRS.getString("Język");
                    String data = findDataRS.getString("Materiał_badawczy");
                    String theses = findDataRS.getString("Tezy_dowiedzione");
                    String method = findDataRS.getString("Metodyka_badawcza");
                    String thesesRejected = findDataRS.getString("Tezy_obalone");
                    String conclusions = findDataRS.getString("Wnioski");
                    String introduction = findDataRS.getString("Wstęp");
                    String results = findDataRS.getString("Wyniki_badań");
                    String paperId = findDataRS.getString("ID");
                    String publicationYear = findDataRS.getString("Rok_wydania");
                    String publicationNumber = findDataRS.getString("Numer_wydania");
                    String pageRange = findDataRS.getString("Od_strony")+"-"+findDataRS.getString("Do_strony");
                    String bib = findDataRS.getString("BibTex");
                    //System.out.println("Found "+bib);

                    paperCount++;
                    Individual newPaper = articleClass.createIndividual(paperOntURI+"#"+"Paper_"+paperCount);
                    
                    //System.out.println(title);
                    

                    if (title != null)
                    {
                        title = removeInvalidChars(title).trim();
                        
                        boolean foundLang = false;
                        
                        if (language != null)
                        {
                            language=language.toLowerCase();
                            
                            for (String lang : langMap.keySet())
                            {
                                if (language.contains(lang)) 
                                {
                                    language = langMap.get(lang);
                                    foundLang=true;
                                    break;
                                }
                            }
                            
                            if (!foundLang) language="other";
                            
                            
                        } else language="null";

                        Individual newTitle = titleClass.createIndividual(paperOntURI+"#"+"Title_"+paperId);
                        newTitle.addProperty(hasContent, paperModel.createLiteral(title, true));
                        newPaper.addProperty(hasPart, newTitle);
                        
                        if (titleTransl != null)
                        {
                            newTitle.addProperty(isSummarised, titleTransl.trim(), "pl");
                        }
                        
                        if (abstractOrg != null)
                        {
                            abstractOrg = removeInvalidChars(abstractOrg).trim();

                            abstractCount++;
                            Individual newAbstract = abstractClass.createIndividual(paperOntURI+"#"+"Abstract_"+abstractCount);
                            newAbstract.addProperty(hasContent, paperModel.createLiteral(abstractOrg, true));
                            newPaper.addProperty(hasPart, newAbstract);
                            
                            if (abstractTransl != null)
                            {
                                abstractTransl=removeInvalidChars(abstractTransl).trim();
                                newAbstract.addProperty(isSummarised, abstractTransl, "pl");
                            }
                            
                        }

                        if (keywords != null)
                        {
                            keywords = removeInvalidChars(keywords).trim();

                            keywordCount++;
                            Individual newKeywords = keywordClass.createIndividual(paperOntURI+"#"+"Keywords_"+keywordCount);
                            newKeywords.addProperty(hasContent, paperModel.createLiteral(keywords, true));
                            newPaper.addProperty(hasPart, newKeywords);
                        }
                        
                        if (bibliography != null)
                        {
                            bibliography = removeInvalidChars(bibliography).trim();

                            biblioCount++;
                            Individual newBiblio = biblioClass.createIndividual(paperOntURI+"#"+"Bibliography_"+biblioCount);
                            newBiblio.addProperty(hasContent, bibliography);
                            newPaper.addProperty(hasPart, newBiblio);
                        }
                        
                        if (problem != null)
                        {
                            problem = removeInvalidChars(problem).trim();

                            problemCount++;
                            Individual newProblem = problemClass.createIndividual(paperOntURI+"#"+"Problem_"+problemCount);
                            newProblem.addProperty(isSummarised, paperModel.createLiteral(problem, true));
                            newPaper.addProperty(solves, newProblem);
                           
                        }
                        
                        if (introduction != null)
                        {
                            introduction = removeInvalidChars(introduction).trim();

                            introCount++;
                            Individual newIntro = introClass.createIndividual(paperOntURI+"#"+"Introduction_"+introCount);
                            newIntro.addProperty(hasContent, paperModel.createLiteral(introduction, true));
                            newPaper.addProperty(hasPart, newIntro);
                        }
                        
                        if (definitions != null)
                        {
                              definitions = removeInvalidChars(definitions).trim();

                              definitionCount++;
                              Individual newDefinition = definitionClass.createIndividual(paperOntURI+"#"+"Definition_"+definitionCount);
                              //newDefinition.addProperty(hasContent, definitions);
                              newDefinition.addProperty(isSummarised, paperModel.createLiteral(definitions, true));
                              newPaper.addProperty(hasPart, newDefinition);
                        }
                        
                        if (theses+thesesRejected != null)
                        {
                            String thesesCollated = "";
                            if (theses != null) thesesCollated=thesesCollated+removeInvalidChars(theses).trim();
                            if (thesesRejected != null) thesesCollated=thesesCollated+removeInvalidChars(thesesRejected).trim();

                            thesisCount++;
                            Individual newThesis = thesisClass.createIndividual(paperOntURI+"#"+"Thesis_"+thesisCount);
                            newThesis.addProperty(isSummarised, paperModel.createLiteral(thesesCollated, true));
                            newPaper.addProperty(hasPart, newThesis);
                        }
                        
                        if (discussion != null)
                        {
                            discussion = removeInvalidChars(discussion).trim();

                            discussionCount++;
                            Individual newDiscussion = discussionClass.createIndividual(paperOntURI+"#"+"Discussion_"+discussionCount);
                            newDiscussion.addProperty(isSummarised, paperModel.createLiteral(discussion, true));
                            newPaper.addProperty(hasPart, newDiscussion);
                        }
                        
                        if (data != null)
                        {
                            data = removeInvalidChars(data).trim();

                            dataCount++;
                            Individual newData = dataClass.createIndividual(paperOntURI+"#"+"Data_"+dataCount);
                            newData.addProperty(isSummarised, paperModel.createLiteral(data, true));
                            newPaper.addProperty(isBasedOn, newData);
                        }
                        
                        if (method != null)
                        {
                            method = removeInvalidChars(method).trim();

                            methodCount++;
                            Individual newMethod = methodClass.createIndividual(paperOntURI+"#"+"Method_"+methodCount);
                            newMethod.addProperty(isSummarised, paperModel.createLiteral(method, true));
                            newPaper.addProperty(uses, newMethod);
                        }
                        
                        if (conclusions != null)
                        {
                            conclusions = removeInvalidChars(conclusions).trim();

                            conclusionCount++;
                            Individual newConclusion = conclusionClass.createIndividual(paperOntURI+"#"+"Conclusion_"+conclusionCount);
                            newConclusion.addProperty(isSummarised, paperModel.createLiteral(conclusions, true));
                            newPaper.addProperty(hasPart, newConclusion);
                        }
                        
                        if (authors != null)
                        {
                            authors = removeInvalidChars(authors).trim();
                            
                            Matcher authorMatch = null;
                            
                            boolean authorSepFound = false;
                            
                            if (authors.contains(";")) 
                            {
                                if (!authors.endsWith(";")) authors=authors+";";
                                authorMatch = authorSemiColonPattern.matcher(authors);
                                authorSepFound=true;
                            }
                            else
                                if (authors.contains(",")) 
                                {
                                    if (!authors.endsWith(",")) authors=authors+",";
                                    authorMatch = authorColonPattern.matcher(authors);
                                    authorSepFound=true;
                                }
                            
                            if (authorSepFound) 
                            while (authorMatch.find())
                            {
                                String author = authorMatch.group(1).trim();
                                
                                if (author.contains("and"))
                                {
                                    Matcher andAuthorMatch = authorAndPattern.matcher(author);
                                    while (andAuthorMatch.find())
                                    {
                                        ArrayList rsList1 = new ArrayList(processIndividual(authorClass, andAuthorMatch.group(1).trim(), "Author", authorMap, authorCount, paperModel));
                                        Individual rsAuthor1 = (Individual) rsList1.get(0);
                                        rsAuthor1.addProperty(isId, andAuthorMatch.group(1).trim());
                                        newPaper.addProperty(isAuthored, rsAuthor1);
                                        rsAuthor1.addProperty(authorsRelation, newPaper);
                                        authorCount=authorCount+(Integer)rsList1.get(1);
                                        
                                        ArrayList rsList2 = new ArrayList(processIndividual(authorClass, andAuthorMatch.group(2).trim(), "Author", authorMap, authorCount, paperModel));
                                        Individual rsAuthor2 = (Individual) rsList2.get(0);
                                        rsAuthor2.addProperty(isId, andAuthorMatch.group(1).trim());
                                        newPaper.addProperty(isAuthored, rsAuthor2);
                                        rsAuthor2.addProperty(authorsRelation, newPaper);
                                        authorCount=authorCount+(Integer)rsList2.get(1);
                                        
                                    }
                                } else 
                                {
                                    ArrayList rsList = new ArrayList(processIndividual(authorClass, author, "Author", authorMap, authorCount, paperModel));
                                    Individual rsAuthor = (Individual) rsList.get(0);
                                    rsAuthor.addProperty(isId, author);
                                    newPaper.addProperty(isAuthored, rsAuthor);
                                    rsAuthor.addProperty(authorsRelation, newPaper);
                                    authorCount=authorCount+(Integer)rsList.get(1);
                                    
                                }
                            }
                            else 
                            {
                                ArrayList rsList = new ArrayList(processIndividual(authorClass, authors, "Author", authorMap, authorCount, paperModel));
                                Individual rsAuthor = (Individual) rsList.get(0);
                                rsAuthor.addProperty(isId, authors);
                                newPaper.addProperty(isAuthored, rsAuthor);
                                rsAuthor.addProperty(authorsRelation, newPaper);
                                authorCount=authorCount+(Integer)rsList.get(1);
                            }
                                
                        }
                        
                        if (journal != null)
                        {
                            //System.out.println(journal);
                            Matcher journalMatch = journalPattern.matcher(journal);
                            while (journalMatch.find())
                            {
                                String journalType = journalMatch.group(1);
                                String journalTypeNumber = journalMatch.group(2);
                                
                                if (journalType.equals("ISSN")) 
                                {
                                    ArrayList rsList = new ArrayList(processIndividual(issueClass, journalTypeNumber, "Journal_Issue", issnMap, issueCount, paperModel));
                                    Individual rsContainer = (Individual) rsList.get(0);
                                    
                                    issueCount=issueCount+(Integer)rsList.get(1);
                                    rsContainer.addProperty(hasContent, journalMatch.group(3));
                                    rsContainer.addProperty(hasISSN, journalTypeNumber);
                                    newPaper.addProperty(isPart, rsContainer);
                                }
                                
                                if (journalType.equals("ISBN")) 
                                {
                                    ArrayList rsList = new ArrayList(processIndividual(collectionClass, journalTypeNumber, "Collection", isbnMap, collectionCount, paperModel));
                                    Individual rsContainer = (Individual) rsList.get(0);
                                    
                                    collectionCount=collectionCount+(Integer)rsList.get(1);
                                    rsContainer.addProperty(hasContent, journalMatch.group(3));
                                    rsContainer.addProperty(hasISBN, journalTypeNumber);
                                    newPaper.addProperty(isPart, rsContainer);
                                    
                                }
                            }
                             
                        }
                        
                        if (publicationYear != null)
                        {
                            newPaper.addProperty(isYear, publicationYear);
                            
                            if (publicationNumber != null)
                            {
                                newPaper.addProperty(isVolume, publicationNumber);
                                
                                if (pageRange != null)
                                {
                                    newPaper.addProperty(pageRangeProperty, pageRange);
                                }
                            }
                        }
                        
                        if (bib != null)
                        {
                            if (!bib.equals("NULL"))
                            {
                                newPaper.addProperty(hasBib, bib);
                            //System.out.println("Adding "+bib);
                            }
                        }
                        
                        
                    } else System.out.println("No title for: "+paperId);
                }

                connDB.close();
        

            } catch (java.sql.SQLException ex) {ex.printStackTrace();}

        
        //upLoadOntology(paperModel);
    
        saveOntology(paperModel);
    }

   static void saveOntology(OntModel model)
   {
       try {
                    //System.getProperty("usr.dir").;
                    File newFile = new File (paperOntPopulatedLocation);
                    try {newFile.createNewFile();} catch (java.io.IOException e) {e.printStackTrace();}
                    FileOutputStream writer = new FileOutputStream(newFile);
                    model.write(writer, "N-TRIPLE");
                    try {
                            writer.close();
                        } catch (java.io.IOException ex) {ex.printStackTrace();}

                    model.close();
                    
        } catch (java.io.FileNotFoundException ex) {ex.printStackTrace();}
   }
    
   static void upLoadOntology (OntModel model)
   {
       FTPClient client = new FTPClient();
       
       try {
           client.connect("s4.hekko.pl");
           client.login("garbacz", "sagan1");
           client.setFileType(FTP.BINARY_FILE_TYPE);
           
           try {
                OutputStream writer = client.storeFileStream("/domains/l3g.pl/public_html/OntoBeef/Papers_populated.owl");
                model.write(writer, "N-TRIPLE");
                try {
                            writer.close();
                    } catch (java.io.IOException ex) {ex.printStackTrace();}

                
                model.close();
                    
                } catch (java.io.FileNotFoundException ex) {ex.printStackTrace();}
             
           client.completePendingCommand();
           client.logout();
           client.disconnect();
           
       } catch (java.net.SocketException e) {e.printStackTrace();} catch (java.io.IOException f) {f.printStackTrace();}
   }
    public static String removeInvalidChars (String inputString)
    {
        if (inputString.contains(String.valueOf((char)0))) inputString=inputString.replaceAll((String.valueOf((char)0)), "");
        if (inputString.contains(String.valueOf((char)1))) inputString=inputString.replaceAll((String.valueOf((char)1)), "");
        if (inputString.contains(String.valueOf((char)2))) inputString=inputString.replaceAll((String.valueOf((char)2)), "");
        if (inputString.contains(String.valueOf((char)3))) inputString=inputString.replaceAll((String.valueOf((char)3)), "");
        if (inputString.contains(String.valueOf((char)4))) inputString=inputString.replaceAll((String.valueOf((char)4)), "");
        if (inputString.contains(String.valueOf((char)5))) inputString=inputString.replaceAll((String.valueOf((char)5)), "");
        if (inputString.contains(String.valueOf((char)6))) inputString=inputString.replaceAll((String.valueOf((char)6)), "");
        
        if (inputString.contains("<")) inputString=inputString.replaceAll("<", "&lt;");
        if (inputString.contains(">")) inputString=inputString.replaceAll(">", "&gt;");
        if (inputString.contains("\"")) inputString=inputString.replaceAll("\"", "&quot;");
        if (inputString.contains("'")) inputString=inputString.replaceAll("'", "&apos;");
        if (inputString.contains("&")) inputString=inputString.replaceAll("&", "&amp;");

        return inputString;
    }
    
    public static ArrayList processIndividual (OntClass inputClass, String name, String type, HashMap<String, Integer> map, int count, OntModel paperModel)
    {
        ArrayList result = new ArrayList();
        int newObject = 0;
        
        Individual object = null;
        
        if (!map.containsKey(name))
        {
            count++;
            object = inputClass.createIndividual(paperOntURI+"#"+type+"_"+count);
            map.put(name, count);
            newObject++;
        }
        else
        {
            object = paperModel.getIndividual(paperOntURI+"#"+type+"_"+map.get(name));
        }
        
        result.add(object);
        result.add(newObject);
        
        return result;
                                
    }

}
