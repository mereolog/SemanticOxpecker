/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Prepare;

import com.hp.hpl.jena.ontology.*;
import java.io.*;

/**
 *
 * @author PG
 */
public class LinkType implements Serializable {
    
    int depth;
    ObjectProperty type;
    
    LinkType(int i, ObjectProperty p)
    {
        depth=i;
        type=p;
    }
    
}
