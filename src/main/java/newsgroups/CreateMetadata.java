/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newsgroups;

import au.com.bytecode.opencsv.CSVWriter;
import edu.harvard.iq.text.core.elasticSearch.helpers.Tuple2;
import edu.harvard.iq.text.core.model.MongoDB;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ellenk
 */
public class CreateMetadata {
    public static void main(String args[]) throws IOException {
        
        
         String newsgroupsDir = "/Users/ellenk/Downloads/20news-18828";
         writeCSV(newsgroupsDir);
    }
    
    public static void writeCSV(String newsgroupsDir) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter("newsgroups_metadata.csv"))) {
            File dir = new File(newsgroupsDir);
            List<String[]> list = new ArrayList<>();
            // for each subdirectory in dir,
            // for each file in subdir
            // create tuple with subdir, filename
            // add tuple to list
            File[] subdirs  = dir.listFiles();
            for (File subdir : subdirs) {
                if (!subdir.getName().equals(".DS_Store")) {
                    File[] docs = subdir.listFiles();
                    for (File doc : docs) {
                        if (!doc.getName().equals(".DS_Store")) {
                            String[] arr = new String[2];
                            arr[0] = subdir.getName();
                            arr[1] = doc.getName();
                            list.add(arr);
                        }
                    }
                }
            }
            
            // write list of tuples to csv file
            writer.writeAll(list);
            writer.flush();
        }      
          
     
    
    }
}
