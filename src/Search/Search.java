/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Search;


import Prepare.TFIDF;
import Prepare.Expander;
import Prepare.Paper;
import java.util.ArrayList;
import java.util.regex.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author PG
 */
public class Search {
    
    public String preloadedMapLocation;
    
    public int maxLengthOfComplexKeyword;
    
    public HashMap<String, HashSet<String>> keywordIdToPaperIdMap;
    
    public String question;
    
    public ArrayList<String> questionStemmedList;
    
    public HashSet<ArrayList<String>> questionIdentifiedList;
    
    public TreeSet<Paper> answerSet;
    
    public HashMap<ArrayList<String>, Double> similarityMeasure;
    
    public double tresholdSimilarity;
    public double maximalSimilarity;
    
    public Search (HashMap<ArrayList<String>, Double> simil, HashMap<String, HashSet<String>> map)
    {
        similarityMeasure = simil;
        keywordIdToPaperIdMap = map;
    }
    
    public Search (Search oldSearch)
    {
        maxLengthOfComplexKeyword=oldSearch.maxLengthOfComplexKeyword;
        keywordIdToPaperIdMap=oldSearch.keywordIdToPaperIdMap;
        similarityMeasure=oldSearch.similarityMeasure;
        tresholdSimilarity=oldSearch.tresholdSimilarity;
        maximalSimilarity=oldSearch.maximalSimilarity;
    }
    
    public Search () {}
    
    public static void  main(String[] args) {
    
}
    
    public void addTFIDF(TFIDF tfidf)
    {
        //TFIDF newTFIDF = new TFIDF();
        //TFIDF retrievedTFIDF = newTFIDF.retrieveTFIDF(location);
    
        this.similarityMeasure.putAll(tfidf.similarityMeasure);
        this.keywordIdToPaperIdMap.putAll(tfidf.wordToPaperIdMap);
        this.tresholdSimilarity=tfidf.tresholdSimilarity;
        this.maximalSimilarity=tfidf.maximalSimilarity;
        
        //return retrievedTFIDF;
    }
    
    public void addExpander(TFIDF tfidf, Expander expander)
    {
        this.keywordIdToPaperIdMap.putAll(expander.keywordToPaperURIMap);
        
        for (String keyword : expander.keywordToPaperURIMap.keySet())
        {
            for (String paperId : expander.keywordToPaperURIMap.get(keyword))
            {
                ArrayList<String> tempCouple = new ArrayList<String>();
                tempCouple.add(keyword);
                tempCouple.add(paperId);
                
                this.similarityMeasure.put(tempCouple, tfidf.maximalSimilarity);
            }
        }
        this.tresholdSimilarity=tfidf.tresholdSimilarity;
        this.maximalSimilarity=tfidf.maximalSimilarity;
    }
    
   
    public TreeSet<Paper> findPaperListByIdentity(HashSet<String> interpretation, SerializableOntology ontology)
    {
        
        HashSet<String> foundPaperIdList = new HashSet<String>();
        //System.out.println("I interpreted it as "+interpretation);
        
        boolean firstKeyword=true;
        
        for (String foundCategory : interpretation)
        {
                HashSet<String> singlePaperIdList = new HashSet<String>(keywordIdToPaperIdMap.get(foundCategory));
            
                if (firstKeyword)
                {
                    foundPaperIdList.addAll(singlePaperIdList);
                }
                else foundPaperIdList.retainAll(singlePaperIdList);
            
                firstKeyword=false;
        }
        
        TreeSet<Paper> foundPaperSet = new TreeSet<Paper>(new Paper());
        
        for (String paperId : foundPaperIdList)
        {
            Paper newFoundPaper = ontology.paperMap.get(Integer.parseInt(paperId));
            newFoundPaper.paperRelevance=this.maximalSimilarity;
                
            foundPaperSet.add(newFoundPaper);
        }
        
        return foundPaperSet;
    }
    
    public TreeSet<Paper> findPaperIdSetBySimilarity (HashSet<String> stemInterpretedList, double searchFactor, SerializableOntology ontology)
    {
        //System.out.println("I interpreted it as "+stemInterpretedList);
        
        TreeSet<Paper> foundPaperSet = new TreeSet<Paper>(new Paper());
        HashSet<String> initialPaperIdSet = new HashSet<String>();
        
        //double tresholdForPaper = (double) this.tresholdSimilarity*stemInterpretedList.size();
        double tresholdForPaper = (this.maximalSimilarity-(this.maximalSimilarity*searchFactor)+this.tresholdSimilarity)*stemInterpretedList.size();
        
        //System.out.println(tresholdForPaper);
        
        for (String stem : stemInterpretedList)
        {
            if (this.keywordIdToPaperIdMap.containsKey(stem)) initialPaperIdSet.addAll(this.keywordIdToPaperIdMap.get(stem));
        }
        
        for (String paperId : initialPaperIdSet)
        {
            double similiarityMeasure = 0;
            
            for (String stem : stemInterpretedList)
            {
                ArrayList<String> tempCouple = new ArrayList<String>();
                tempCouple.add(stem);
                tempCouple.add(paperId);
            
                if (this.similarityMeasure.containsKey(tempCouple))
                {
                    similiarityMeasure=similiarityMeasure+this.similarityMeasure.get(tempCouple);
                }
            }
            
            if (similiarityMeasure > tresholdForPaper)
            {
                Paper newFoundPaper = ontology.paperMap.get(Integer.parseInt(paperId));
                newFoundPaper.paperRelevance=similiarityMeasure;
                
                foundPaperSet.add(newFoundPaper);
                
            }
        }
        
        return foundPaperSet;
    }
    
    
     public HashSet<ArrayList<String>> findInterpretation()
    {
        HashSet<ArrayList<String>> listOfStemLists = new HashSet<ArrayList<String>>();
        ArrayList<String> stemmedQuestion = new ArrayList<String>(findStemList(this.question));
        
        this.questionStemmedList=stemmedQuestion;
        
        //System.out.println("after stemming : "+this.questionStemmedList);
    
        if (this.questionStemmedList.size()>0) 
        {
            listOfStemLists = findStemListOfLists(stemmedQuestion, new ArrayList<String>(), 0, this.maxLengthOfComplexKeyword, listOfStemLists);
            this.questionIdentifiedList=listOfStemLists;
        }
     
        return listOfStemLists;
    }
    
    
    public HashSet<ArrayList<String>> findStemListOfLists (ArrayList<String> stemmedQuestion, ArrayList<String> stemmedInterpretation, int start, int length, HashSet<ArrayList<String>> listOfStemLists)
    {
        //System.out.println("Q:"+stemmedQuestion);
        //System.out.println("I:"+stemmedInterpretation);
        //System.out.println(start+" vs "+length);
        //System.out.println(listOfStemLists);
        //System.out.println("***");
        
        if (stemmedQuestion.size()<maxLengthOfComplexKeyword) maxLengthOfComplexKeyword=stemmedQuestion.size();
        
        if (stemmedQuestion.size()>start)
        {
            if (stemmedQuestion.size()>start+length)
            {
                ArrayList sublist = new ArrayList<String>(stemmedQuestion.subList(start, start+length));
                String keywordCandidate = concatenateList(sublist);
            
                if (this.keywordIdToPaperIdMap.containsKey(keywordCandidate))// || (keywordToKeywordIdPartialMap.containsKey(keywordCandidate) && includePartial))
                {
                    if (!stemmedInterpretation.contains(keywordCandidate)) stemmedInterpretation.add(keywordCandidate);
                    //System.out.println(">"+keywordCandidate+"<");
                    if (start+length==0) listOfStemLists=findStemListOfLists(stemmedQuestion, stemmedInterpretation, 1, maxLengthOfComplexKeyword, listOfStemLists);
                    else listOfStemLists=findStemListOfLists(stemmedQuestion, stemmedInterpretation, start+length, maxLengthOfComplexKeyword, listOfStemLists);
                }
                else
                    if (length>0) {listOfStemLists=findStemListOfLists(stemmedQuestion, stemmedInterpretation, start, length-1, listOfStemLists);}
                        else {listOfStemLists=findStemListOfLists(stemmedQuestion, stemmedInterpretation, start+1, maxLengthOfComplexKeyword, listOfStemLists);}
            } 
            else 
            {
                ArrayList sublist = new ArrayList<String>(stemmedQuestion.subList(start, stemmedQuestion.size()));
                String keywordCandidate = concatenateList(sublist);
            
                if (this.keywordIdToPaperIdMap.containsKey(keywordCandidate))// || (keywordToKeywordIdPartialMap.containsKey(keywordCandidate) && includePartial))
                {
                    
                    if (!stemmedInterpretation.contains(keywordCandidate)) stemmedInterpretation.add(keywordCandidate);
                    listOfStemLists=findStemListOfLists(stemmedQuestion, stemmedInterpretation, stemmedQuestion.size(), maxLengthOfComplexKeyword, listOfStemLists);
                }
                else {listOfStemLists=findStemListOfLists(stemmedQuestion, stemmedInterpretation, start, length-1, listOfStemLists);}
            }
        }
        else 
            if (length>1)
            {
                if (stemmedInterpretation.size()>0)
                {
                    listOfStemLists.add(stemmedInterpretation);
                    
                    ArrayList<String> newInterpretation = new ArrayList<String>(stemmedInterpretation.subList(0, stemmedInterpretation.size()-1));
                
                    maxLengthOfComplexKeyword--;
                    listOfStemLists=findStemListOfLists(stemmedQuestion, newInterpretation, start-length, length-1, listOfStemLists);
                }
                
                else
                {
                    maxLengthOfComplexKeyword--;
                    listOfStemLists=findStemListOfLists(stemmedQuestion, stemmedInterpretation, start-length, length-1, listOfStemLists);
                }
            
            }
                else
                {
                    ArrayList sublist = new ArrayList<String>(stemmedQuestion.subList(start, start));
                    String keywordCandidate = concatenateList(sublist);
            
                    if (this.keywordIdToPaperIdMap.containsKey(keywordCandidate))// || (keywordToKeywordIdPartialMap.containsKey(keywordCandidate) && includePartial))
                    {
                        if (this.keywordIdToPaperIdMap.containsKey(keywordCandidate))
                        {
                            if (!stemmedInterpretation.contains(keywordCandidate)) stemmedInterpretation.add(keywordCandidate);
                        }
                        
                        listOfStemLists.add(stemmedInterpretation);
                    }
            
                    listOfStemLists.add(stemmedInterpretation);
                    
                    return listOfStemLists;
                }
        
            return listOfStemLists;
    }
        
    public ArrayList<String> findStemList (String chunk)
    {
        
        ArrayList<String> stemmedList = new ArrayList<String>();

        Pattern wordPattern = Pattern.compile("(.+?)(\\(|\\s|\\z|\\?|,|\\))");
        Matcher wordMatch = null;
        String word = null;
        String stem = null;
        String tag = null;

        boolean stemFound = false;

        morfologik.stemming.PolishStemmer stemmer = new morfologik.stemming.PolishStemmer();
        List<morfologik.stemming.WordData> stemList = null;

        wordMatch = wordPattern.matcher(chunk);
        while (wordMatch.find())
        {
            stemFound = false;
            word = wordMatch.group(1).trim();
            
            if (word.startsWith("(")) 
            {
                word=word.substring(1);
            }
            
            String unprocessedWord = word;
            stemList = stemmer.lookup(word);

            if (stemList.size()>0)
            {
                String firstStem = stemList.get(0).getStem().toString();
            
                for (morfologik.stemming.WordData wordData : stemList)
                {
                    stem = wordData.getStem().toString();
                    //System.out.println("Stem found in"+stemList.size()+" : "+stem);
                    //System.out.println(wordData.toString());

                    if (!wordData.getTag().toString().contains("pltant")) 
                    {
                        if (this.keywordIdToPaperIdMap != null)
                        {
                            if (this.keywordIdToPaperIdMap.containsKey(stem))
                            {
                                //System.out.println("Stem added"+stem);
                                stemFound = true;
                                stemmedList.add(stem);
                                break;
                            }
                        }
                        
                        else
                        {
                            stemFound = true;
                            stemmedList.add(stem);
                            break; 
                        }
                    }
                }
                
                if (!stemFound)
                {
                    stemmedList.add(firstStem);
                    stemFound=true;
                }
            }
            
            if (!stemFound)
            {
                word = word.toLowerCase();
                stemList = stemmer.lookup(word);

                if (stemList.size()>0)
                {
                    String firstStem = stemList.get(0).getStem().toString();
            
                    for (morfologik.stemming.WordData wordData : stemList)
                    {
                        stem = wordData.getStem().toString();

                        if (!wordData.getTag().toString().contains("pltant")) 
                        {
                            if (this.keywordIdToPaperIdMap != null)
                            {
                                if (this.keywordIdToPaperIdMap.containsKey(stem))
                                {
                                    stemFound = true;
                                    stemmedList.add(stem);
                                    break;
                                }
                            }
                            
                            else
                            {
                                stemFound = true;
                                stemmedList.add(stem);
                                break; 
                            }
                        }
                    }
                
                    if (!stemFound)
                    {
                        stemmedList.add(firstStem);
                        stemFound=true;
                    }
                }
            }
            
            if (!stemFound)
            {
                stemmedList.add(unprocessedWord);
            }
        }

        return stemmedList;

    }
    
    
    public String concatenateList(ArrayList<String> list)
    {
        String concatenatedList = "";
                                
        for (String listItem : list)
        {
            concatenatedList=concatenatedList+" "+listItem;
        }
        concatenatedList=concatenatedList.trim();
        
        return concatenatedList;
   }
    
    public HashSet<String> concatenateInterpretation(HashSet<ArrayList<String>> questionInterpretation)
    {
        HashSet<String> interpretation = new HashSet<String>();
        
        //System.out.println(questionInterpretation);
        
        for (ArrayList<String> foundList : questionInterpretation)
        {
           //interpretation.addAll(foundList);
           for (String foundKeyword : foundList) 
           {
               boolean addKeyword = true;
               boolean removeKeyword = false;
               HashSet<String> keySetToBeRemoved= new HashSet<String>();
               
               for (String intKeyword : interpretation)
               {
                   if (intKeyword.contains(foundKeyword))
                   {
                       addKeyword=false;
                       break;
                   }
                   
                   if (foundKeyword.contains(intKeyword))
                   {
                       removeKeyword=true;
                       keySetToBeRemoved.add(intKeyword);
                   }
               }
               
               if (addKeyword) {
                   //System.out.println("Adding "+foundKeyword);
                   interpretation.add(foundKeyword);
               }
               
               if (removeKeyword) 
               {
                   for (String keyToBeRemoved :keySetToBeRemoved)
                   {
                       //System.out.println("Substituting "+foundKeyword+" for "+keyToBeRemoved);
                       interpretation.remove(keyToBeRemoved);
                   }
                   
                   interpretation.add(foundKeyword);
                   
               }
           }
            
        }
        
        return interpretation;
        
    }
    
    public ArrayList<Integer> retrieveIdFromFoundPapers (TreeSet<Paper> paperSet, int size)
    {
        ArrayList<Integer> paperIdSet = new ArrayList<Integer>();
        
        int count=0;
        
        for (Paper paper : paperSet)
        {
            
            paperIdSet.add(paper.paperId);
            
            count++;
            if (count>size && size >0) break;
        }
        
        return paperIdSet;
    }
    
}
