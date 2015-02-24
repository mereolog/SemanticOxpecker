/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Configure;

import org.apache.commons.net.ftp.*;
import com.hp.hpl.jena.ontology.*;
import java.io.*;
import java.util.regex.*;
import java.util.ArrayList;

/**
 *
 * @author PG
 */
public class Configure {
    
    public static String bibEncoding;
    
    public static void main(String[] args) {
        
        if (args == null)
        {
            System.err.println("NO ARGUMENTS");
            System.exit(1);
        }
        else 
            {
                if (args.length<1)
                {
                    System.err.println("AT LEAST ONE ARGUMENT IS MISSING");
                    System.exit(1);
                }
                else
                {
                    if (args[0].equals("c") || args[0].equals("f"))
                    {
                        if (args.length<3)
                        {
                            System.err.println("AT LEAST ONE ARGUMENT IS MISSING");
                            System.exit(1);
                        }
                        
                        if (args.length==3) bibEncoding="UTF8";
                        else bibEncoding=args[3];
                        System.out.println("BIBLIOGRAPHY ENCODING IS SET TO "+bibEncoding);
                        
                        Prepare.DomainReification.main(args);
                        System.out.println("DOMAIN ONTOLOGY IS REIFIED");
                        Prepare.OntologyFix.main(args);
                        System.out.println("CONCEPTUALISATION ONTOLOGY IS FIXED");
                        Prepare.OntologyFix tempFix = new Prepare.OntologyFix(  "data/ontologies/Conceptualisation.owl",//
                                            "data/ontologies/Conceptualisation.owl",//
                                            "http://www.l3g.pl/ontologies/OntoBeef/Conceptualisation.owl"        
                                        );
        
                        tempFix.loadOntology();
                        upLoadOntology(tempFix.conceptModel);
                        System.out.println("FIXED ONTOLOGY IS UPLOADED TO L3G SERVER");
                        Prepare.PaperRepopulation.main(args);
                        System.out.println("PAPERS ONTOLOGY IS REPOPULATED");
                        tempFix.loadOntology();
                        upLoadOntology(tempFix.conceptModel);
                        System.out.println("CLEAN CONCEPTUALISATION ONTOLOGY IS UPLOADED TO L3G SERVER AGAIN");
                        Search.SerialiseOntology.main(args);
                        System.out.println("PAPERS ONTOLOGY IS SERIALISED");
        
                        ArrayList<String> exNameList = new ArrayList<String>();
                        ArrayList<String> exLingList = new ArrayList<String>();
                        Pattern oxPattern = Pattern.compile("(.*?)\\-");
                        Matcher expanderPattern = oxPattern.matcher(args[1]);
                    
                        while (expanderPattern.find())
                        {
                            exNameList.add(expanderPattern.group(1));
                        }
                    
                        expanderPattern = oxPattern.matcher(args[2]);
                        while (expanderPattern.find())
                        {
                            exLingList.add(expanderPattern.group(1));
                        }
                        
                        if (!exNameList.contains("b"))
                        {
                            exNameList.add("b");
                            exLingList.add("pl@en@");
                        }
                        
                        //System.out.println(exNameList);
                        //System.out.println(exLingList);
                    
                        if (exNameList.size() == exLingList.size())
                        {
                            for (int i=0;i<exNameList.size();i++)
                            {
                                String expArgs[] = {exNameList.get(i), exLingList.get(i)};
                                Prepare.Expander.main(expArgs);
                            }
                        }   
                        else
                        {
                            System.err.println("EXPANDER SPEC AND LANGUAGE DATA DO NOT MATCH");
                            System.exit(1);
                        }
                        
                        if (args[0].equals("f"))
                        {
                            GUI.OntoBeefApplication.main(args);
                        }
                    }
                    else
                        if (args[0].equals("r"))
                        {
                            if (args.length == 1 || args.length > 2) bibEncoding="UTF8";
                            else bibEncoding=args[1]; 
                            
                            System.out.println("BIBLIOGRAPHY ENCODING IS SET TO "+bibEncoding);
                            
                            GUI.OntoBeefApplication.main(args);
                        }
                        else
                        {
                            System.err.println("THE FIRST PARAMETER IS OUT OF RANGE");
                            System.exit(1);
                        }
                    
                }
            }
        
        
    }
    
   public static void upLoadOntology (OntModel model)
   {
       FTPClient client = new FTPClient();
       
       try {
           
           client.connect("s4.hekko.pl");
           
           if (client.isConnected())
           {
               boolean isLoggedIn = client.login("garbacz", "sagan1");
           
               if (isLoggedIn)
               {
                    client.setFileType(FTP.BINARY_FILE_TYPE);
                    //ftp://s4.hekko.pl/domains/l3g.pl/public_html/ontologies/OntoBeef/Conceptualisation.owl
                    try {
                            OutputStream writer = client.storeFileStream("/domains/l3g.pl/public_html/ontologies/OntoBeef/Conceptualisation.owl");
                            model.write(writer, null);
                            try {
                                    writer.close();
                                } catch (java.io.IOException ex) {ex.printStackTrace();}
                        
                            model.close();
                    
                        } catch (java.io.FileNotFoundException ex) {ex.printStackTrace();}
             
                    client.completePendingCommand();
                    client.logout();
               }
               else
               {
                   System.err.println("CANNOT LOG TO"+"s4.hekko.pl"+" AS "+"garbacz");
                   System.exit(1);
               }
           
                client.disconnect();
           }
           else 
           {
               System.err.println("CANNOT CONNECT TO"+"s4.hekko.pl");
               System.exit(1);
           }
           
           
           
       } catch (java.net.SocketException e) {e.printStackTrace();} catch (java.io.IOException f) {f.printStackTrace();}
   }
    
}
