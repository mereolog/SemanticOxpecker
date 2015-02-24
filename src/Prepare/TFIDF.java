/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prepare;


import java.util.ArrayList;
import java.util.regex.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.TreeSet;
import java.io.*;
import java.sql.*;
import java.util.Collections;

/**
 *
 * @author PG
 */
public class TFIDF implements Serializable {
    
    public String dbConnectionData;
    
    public HashMap<String, HashSet<String>> wordToPaperIdMap;
    public HashMap<ArrayList<String>, Double> similarityMeasure;
    
    public double tresholdSimilarity;
    
    public double maximalSimilarity;
    
    public String serialisationLocation;
    
    public static void main(String[] args) {
        
        TFIDF newTFIDF = new TFIDF();
        newTFIDF.dbConnectionData="jdbc:mysql://localhost:8889/TFIDF?useUnicode=yes&characterEncoding=UTF-8";
        newTFIDF.serialisationLocation="data/TFIDFSerialisation.java";
        newTFIDF.wordToPaperIdMap = new HashMap<String, HashSet<String>>();
        newTFIDF.similarityMeasure = new HashMap<ArrayList<String>, Double>();
        
        newTFIDF.loadDatabase();
        
        //System.out.println(newTFIDF.wordToPaperIdMap.entrySet().size());
        //System.out.println(newTFIDF.wordAndPaperIdToTFIDFMap.entrySet().size());
        
        newTFIDF.saveTFIDF();
    
    }
    
    public TFIDF loadDatabase()
    {
        //TreeSet<Double> substSet = new TreeSet<Double>();
        //TreeSet<Double> adjSet = new TreeSet<Double>();
        
        ArrayList<Double> substList = new ArrayList<Double>();
        ArrayList<Double> adjList = new ArrayList<Double>();
        
        double substTreshold=0;
        double adjTreshold=0;
        
        try {Class.forName("com.mysql.jdbc.Driver");} catch (ClassNotFoundException ex) {ex.printStackTrace();}
        
        try {
            
            Expander newExpander = new Expander();
            
            HashSet<String> stopList = newExpander.loadStopList();
            
            Connection connDB = DriverManager.getConnection(this.dbConnectionData, "root", "root");

            java.sql.Statement findData = connDB.createStatement();
            ResultSet findDataRS = findData.executeQuery("SELECT slowo, paper_id, tfidf FROM subst");
            while (findDataRS.next())
            {
                String word = findDataRS.getString("slowo");
                String paperId = findDataRS.getString("paper_id");
                Double tfidfValue = findDataRS.getDouble("tfidf");
                
                if (!stopList.contains(word))
                {
                    substList.add(tfidfValue);
                
                    if (this.wordToPaperIdMap.containsKey(word))
                    {
                        this.wordToPaperIdMap.get(word).add(paperId);
                    }
                    else
                    {
                        HashSet<String> newPaperIdSet = new HashSet<String>();
                        newPaperIdSet.add(paperId);
                    
                        this.wordToPaperIdMap.put(word, newPaperIdSet);
                    }
                
                    ArrayList<String> tfidfCouple = new ArrayList<String>();
                    tfidfCouple.add(word);
                    tfidfCouple.add(paperId);
                
                    this.similarityMeasure.put(tfidfCouple, tfidfValue);
                }
            }
            
            //ArrayList<Double> substList = new ArrayList<Double>(substSet);
            //TreeSet<Double> substSet = new TreeSet<Double>(substList);
            Collections.sort(substList);
            //System.out.println(substList);
            substTreshold = substList.get((int) (substList.size()/2));
            
            //System.out.println(substTreshold);
            
            findDataRS = findData.executeQuery("SELECT slowo, paper_id, tfidf FROM adj");
            while (findDataRS.next())
            {
                String word = findDataRS.getString("slowo");
                String paperId = findDataRS.getString("paper_id");
                Double tfidfValue = findDataRS.getDouble("tfidf");
                
                if (!stopList.contains(word))
                {
                    adjList.add(tfidfValue);
                
                    if (this.wordToPaperIdMap.containsKey(word))
                    {
                        this.wordToPaperIdMap.get(word).add(paperId);
                    }
                    else
                    {
                        HashSet<String> newPaperIdSet = new HashSet<String>();
                        newPaperIdSet.add(paperId);
                    
                        this.wordToPaperIdMap.put(word, newPaperIdSet);
                    }
                
                    ArrayList<String> tfidfCouple = new ArrayList<String>();
                    tfidfCouple.add(word);
                    tfidfCouple.add(paperId);
                
                    this.similarityMeasure.put(tfidfCouple, tfidfValue);
                }
            }
            
            Collections.sort(adjList);
            //System.out.println(substList);
            adjTreshold = adjList.get((int) (adjList.size()/2));
            
            //System.out.println(adjTreshold);
            
            this.tresholdSimilarity=(double) (substTreshold+adjTreshold)/2;
            
            //System.out.println(this.tresholdSimilarity);
            
            if (substList.get(substList.size()-1) > adjList.get(adjList.size()-1))
            {
                this.maximalSimilarity=substList.get(substList.size()-1);
            }
            else
            {
                this.maximalSimilarity=adjList.get(adjList.size()-1);
            }
            
            
        } catch (java.sql.SQLException ex) {ex.printStackTrace();}
        
        return this;
    
    }
    
    public void saveTFIDF()
    {
        File newFile = new File (this.serialisationLocation);
        try {newFile.createNewFile();} catch (java.io.IOException e) {e.printStackTrace();}
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try
        {
            fos = new FileOutputStream(newFile);
            out = new ObjectOutputStream(fos);
            out.writeObject(this);
            out.close();
        }
        catch(IOException ex) {ex.printStackTrace();}
    }
    
    public TFIDF retrieveTFIDF(String serialisationLocation)
    {
        TFIDF retrievedTFIDF = null;
        
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try
        {
            fis = new FileInputStream(serialisationLocation);
            in = new ObjectInputStream(fis);
            try {
                    TFIDF tempTFIDF = (TFIDF)in.readObject();
                    
                    retrievedTFIDF = tempTFIDF;
                
                //newMap = newExpander.keywordToPaperURIMap;
            } catch (java.lang.ClassNotFoundException ex) {ex.printStackTrace();}
            
            in.close();
        }
        
        catch(IOException ex) {ex.printStackTrace();}
        
        return retrievedTFIDF;
    }
    
    
}
