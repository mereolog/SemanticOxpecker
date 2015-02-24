/*
 * OntoBeefGUIApp.java
 */

package GUI;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import java.util.ArrayList;
import java.io.*;
import java.util.regex.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.awt.*;
import java.util.Locale;
import org.apache.poi.hssf.usermodel.*;


/**
 * The main class of the application.
 */
public class OntoBeefApplication extends SingleFrameApplication {

    public static Prepare.TFIDF retrievedTFIDF;
    public static Prepare.Expander expanderModel;
    
    public static Search.Search search;
    public static Search.Search initialIdentitySearch;
    public static Search.Search initialSimilaritySearch;
    
    public static Search.SerializableOntology newOntology;
    
    public static HashMap<String, Prepare.Paper> paperTitleToPaperMap = new HashMap <String, Prepare.Paper>(); 
    
    
    public static boolean findBySimilarity=false;
    
    public static double similarityTreshold;
    
    public static String defaultExpanderLocation;
    
    public static OntoBeefGUIView GUI;
    
    public static HSSFWorkbook report;
    
    public static int questionNo;
    
    
    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        
        GregorianCalendar start = new GregorianCalendar();
        String startName =  String.valueOf(start.get(GregorianCalendar.YEAR))+//
                            start.get(GregorianCalendar.MONTH)+
                            start.get(GregorianCalendar.DAY_OF_MONTH)+//
                            start.get(GregorianCalendar.HOUR)+//
                            start.get(GregorianCalendar.MINUTE);
                                
        //System.out.print(startName);
        
        report = new HSSFWorkbook();
        
        questionNo=0;
        
        GUI = new OntoBeefGUIView(this);
        SplashScreen screen = OntoBeefGUIView.inititSplash();
        Graphics2D g = null;
        
        if (screen != null)
        {
            g = OntoBeefGUIView.inititSplashFrame(screen);
            OntoBeefGUIView.renderSplashFrame("Ładuję OntoBeef", g);
            screen.update();
        }
        
        newOntology = new Search.SerialiseOntology().retrieveOntology("data/OntologySerialisation.java");
        
        if (screen != null)
        {
            OntoBeefGUIView.renderSplashFrame("Ładuję moduł statystyczny", g);
            screen.update();
        }
        
        defaultExpanderLocation = "data/expanders/Basic.exp";
        final String tfidfSerialisationLocation="data/TFIDFSerialisation.java";
        retrievedTFIDF = new Prepare.TFIDF();
        retrievedTFIDF = retrievedTFIDF.retrieveTFIDF(tfidfSerialisationLocation);
        
        if (screen != null)
        {
            OntoBeefGUIView.renderSplashFrame("Ładuję ekspander", g);
            screen.update();
        }
        
        
        expanderModel = retrieveExpanderModel(defaultExpanderLocation, retrievedTFIDF);
        
        if (screen != null)
        {
            OntoBeefGUIView.renderSplashFrame("Przygotowuję przestrzeń semantyczną", g);
            screen.update();
        }
        
        activateExpander(GUI);
        
        if (screen != null) OntoBeefGUIView.killSplash(screen);
        
        show(GUI);
        
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of OntoBeefGUIApp
     */
    public static OntoBeefApplication getApplication() {
        return OntoBeefApplication.getInstance(OntoBeefApplication.class);
    }
    
    protected void shutdown()
    {
        GregorianCalendar end = new GregorianCalendar();
        String endName =  String.valueOf(end.get(GregorianCalendar.YEAR))+//
                            end.get(GregorianCalendar.MONTH)+
                            end.get(GregorianCalendar.DAY_OF_MONTH)+//
                            end.get(GregorianCalendar.HOUR)+//
                            end.get(GregorianCalendar.MINUTE);
                                
        File newFile = new File ("logs/"+endName+".xls");
        try {newFile.createNewFile();} catch (java.io.IOException e) {e.printStackTrace();}
        
        try
        {
            OutputStream out = new FileOutputStream(newFile);
            report.write(out);
        }
        catch(IOException ex) {ex.printStackTrace();}
        
        super.shutdown();
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        
        launch(OntoBeefApplication.class, args);
        
        
    }

    public static void addSheet(Search.Search search, HashSet<String> interpretation)
    {
        HSSFSheet sheet = report.createSheet("Pytanie nr "+questionNo);
        
        HSSFRow questionRow = sheet.createRow(0);
        HSSFCell questionLabel = questionRow.createCell(0);
        questionLabel.setCellValue("Pytanie");
        HSSFCell questionCell = questionRow.createCell(1);
        questionCell.setCellValue(search.question);
        
        HSSFRow stemRow = sheet.createRow(1);
        HSSFCell stemLabel = stemRow.createCell(0);
        stemLabel.setCellValue("Stemming");
        HSSFCell stemCell = stemRow.createCell(1);
        stemCell.setCellValue(search.questionStemmedList.toString());
        
        HSSFRow interpretationRow = sheet.createRow(2);
        HSSFCell intLabel = interpretationRow.createCell(0);
        intLabel.setCellValue("Interpretacja");
        HSSFCell intCell = interpretationRow.createCell(1);
        intCell.setCellValue(interpretation.toString());
        
        HSSFRow statRow = sheet.createRow(3);
        HSSFCell statLabel = statRow.createCell(0);
        statLabel.setCellValue("Wspomaganie statystyczne");
        HSSFCell statCell = statRow.createCell(1);
        statCell.setCellValue(findBySimilarity);
        
        HSSFRow paperIntroRow = sheet.createRow(4);
        HSSFCell paperIntroLabel = paperIntroRow.createCell(0);
        paperIntroLabel.setCellValue("Znalezione artykuły");
        
        int indentNo=1;
        
        HSSFRow paperDetailIntroRow = sheet.createRow(5);
        HSSFCell paperDetailIntroLabel1 = paperDetailIntroRow.createCell(1+indentNo);
        paperDetailIntroLabel1.setCellValue("Tytuł");
        HSSFCell paperDetailIntroLabel2 = paperDetailIntroRow.createCell(2+indentNo);
        paperDetailIntroLabel2.setCellValue("Słowa kluczowe");
        HSSFCell paperDetailIntroLabel3 = paperDetailIntroRow.createCell(3+indentNo);
        paperDetailIntroLabel3.setCellValue("Tezy");
        HSSFCell paperDetailIntroLabel4 = paperDetailIntroRow.createCell(4+indentNo);
        paperDetailIntroLabel4.setCellValue("Abstrakt");
        HSSFCell evaluationCell = paperDetailIntroRow.createCell(0+indentNo);
        evaluationCell.setCellValue("Czy trafnie?");
        
        int rowCount=6;
        
        for (Prepare.Paper answer : search.answerSet)
        {
            HSSFRow paperRow = sheet.createRow(rowCount);
            HSSFCell detail1 = paperRow.createCell(1+indentNo);
            detail1.setCellValue(answer.paperTitle);
            HSSFCell detail2 = paperRow.createCell(2+indentNo);
            detail2.setCellValue(answer.keyword);
            HSSFCell detail3 = paperRow.createCell(3+indentNo);
            detail3.setCellValue(answer.theses);
            HSSFCell detail4 = paperRow.createCell(4+indentNo);
            detail4.setCellValue(answer.paperAbstract);
            
            rowCount++;
        }
        
    }
    
    public static Prepare.Expander retrieveExpanderModel(String location, Prepare.TFIDF tfidf)
    {
        Prepare.Expander retrievedExpanderModel = new Prepare.Expander();
        retrievedExpanderModel=retrievedExpanderModel.loadExpander(location);
        
        return retrievedExpanderModel;
    }
    
   public static Search.Search findAnswer (String question, Prepare.TFIDF tfidf, Prepare.Expander expander)
   {        
            Search.Search newSearch = null;
            if (findBySimilarity) newSearch = new Search.Search(initialSimilaritySearch); 
            else newSearch = new Search.Search(initialIdentitySearch);
            newSearch.question=question;
            
            HashSet<String> interpretation = newSearch.concatenateInterpretation(newSearch.findInterpretation());
            
            if (!findBySimilarity) 
            newSearch.answerSet=newSearch.findPaperListByIdentity(interpretation, newOntology);
            else 
            newSearch.answerSet=newSearch.findPaperIdSetBySimilarity(interpretation, similarityTreshold, newOntology);
            
            questionNo++;
            
            addSheet(newSearch, interpretation);
            
            return newSearch;
   }  
   
   public static void activateExpander(OntoBeefGUIView gui)
   {
        HashMap<ArrayList<String>, Double> initialSimilarityMeasureForSimilaritySearch = new HashMap<ArrayList<String>, Double>();
        HashMap<String, HashSet<String>> initialMapSimilaritySearch = new HashMap<String, HashSet<String>>();
        HashMap<ArrayList<String>, Double> initialSimilarityMeasureForIdentitySearch = new HashMap<ArrayList<String>, Double>();
        HashMap<String, HashSet<String>> initialMapIdentitySearch = new HashMap<String, HashSet<String>>();
        
        initialIdentitySearch = new Search.Search(initialSimilarityMeasureForIdentitySearch, initialMapIdentitySearch);
        initialIdentitySearch.answerSet= new TreeSet<Prepare.Paper>();
        initialIdentitySearch.maxLengthOfComplexKeyword=4;
        initialIdentitySearch.addExpander(retrievedTFIDF, expanderModel);
        
        initialSimilaritySearch = new Search.Search(initialSimilarityMeasureForSimilaritySearch, initialMapSimilaritySearch);
        initialSimilaritySearch.answerSet= new TreeSet<Prepare.Paper>();
        initialSimilaritySearch.maxLengthOfComplexKeyword=4;
        initialSimilaritySearch.addTFIDF(retrievedTFIDF);
        initialSimilaritySearch.addExpander(retrievedTFIDF, expanderModel);
        
        for (Prepare.Paper paper : newOntology.paperMap.values())
        {
            paperTitleToPaperMap.put(paper.paperTitle, paper);
        }
        
        if (findBySimilarity) OntoBeefApplication.search=initialSimilaritySearch;
        else OntoBeefApplication.search=initialIdentitySearch;
        
        similarityTreshold=(double) (gui.similaritySlider.getValue())/100;
        
        //System.out.println(expanderModel.keywordToPaperURIMap);
   }
}
