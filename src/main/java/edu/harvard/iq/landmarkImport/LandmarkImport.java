/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.landmarkImport;

import au.com.bytecode.opencsv.CSVReader;
import edu.harvard.iq.text.core.model.MongoDB;
import edu.harvard.iq.text.core.service.MongoSetService;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.bson.types.ObjectId;

/**
 *
 * @author ellenk
 */
public class LandmarkImport {
    public static void main(String args[]) throws IOException {
         String mongoHost = "mdb-consilience.cloudapp.net";
      //   String mongoHost = "localhost";
         MongoDB.init("mydb", mongoHost);
         String mongoSetId = "57ac9a4e85fc693b457806bc";
        
         String landmarkCSV = "/Users/ellenk/test/text_doc_root/King_no_dupes/brandon_landmarks.csv";
         importLandmarks(mongoSetId,landmarkCSV);
    }
    
    public static void importLandmarks(String mongoSetId, String filename) throws IOException {
            CSVReader reader = new CSVReader(new FileReader(filename));
        String[] nextLine;
        
       while ((nextLine = reader.readNext()) != null) {
        
           int[] membership = new int[nextLine.length-1];
            String name = nextLine[0];
            Set<String> clusterIds = new HashSet<>();
            for (int i=1;i< nextLine.length;i++) {
               String id = nextLine[i];
               clusterIds.add(id);
               membership[i-1]= Integer.parseInt(id);
            }
            String[] values =clusterIds.toArray(new String[clusterIds.size()]);
            MongoSetService.createLandmark(new ObjectId(mongoSetId), name,values, membership);
       }
    
    }
}
