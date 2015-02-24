/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prepare;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author PG
 */
public class SerializableExpander implements Serializable{
    
    HashMap<String, HashSet<String>> keywordToPaperIdMap;
    
    String serialisationLocation;
}
