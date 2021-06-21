/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package okcupid;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;

/**
 *
 * @author ellenk
 */
public class OkCupidTab {

    public static void main(String args[]) throws Exception {
      
       // String csv = new String("/Users/ellenk/Downloads/profiles_tab.txt");
        String csv = new String("/Users/ellenk/scratch/profiles_subset2.csv");
        createFilesAndMetadata(csv);
        System.out.println("DONE!");

    }
    
    public static void createFilesAndMetadata(String queryCSV) throws Exception {
         Scanner scanner= new Scanner(new File(queryCSV));
      
         String[] nextLine;
         List<String[]> dataList = new ArrayList<>();
         String[] arr = {"age", "drinks", "job", "religion","essay0","essay1","essay2","essay3","essay4","essay5","essay6","essay7","essay8","essay9"};
         List<String> headers = Arrays.asList(arr);
         HashMap<Integer,String> colMap = new HashMap<>();
        
         
         dataList.add(arr);
         String scanned = scanner.nextLine();
         nextLine = StringUtils.split(scanned, "\t");  // headers
         int idx =0;
         for (idx=0;idx< nextLine.length;idx++ ) {
             if (headers.contains(nextLine[idx])) {
                 colMap.put( idx, nextLine[idx]);
             }
         }
         
         int i=1;
          int numExceptions = 0; 
          
            while ((scanned = scanner.nextLine()) != null && i < 20 ) {
                nextLine = StringUtils.split(scanned, "\t");  
                ArrayList<String> colValue = new ArrayList<>();
                String essay = new String();
                //"drinks", "job", "religion","essay0","essay1","essay2","essay3","essay4","essay5","essay6","essay7","essay8","essay9"};
                int col=0;
              
                
                    System.out.println("LINE: "+i+"--- "+Arrays.toString(nextLine));
                
                for(col=0;col< nextLine.length;col++) {
                    if (colMap.containsKey(col)) {
                        if (colMap.get(col).equals("age")) {
                            System.out.println("col"+col+", nextLine[col] "+nextLine[col]);
                            Integer range = 0;
                            try {
                                range = Integer.valueOf(nextLine[col].trim());
                                range = range/10;    
                                System.out.println("range: "+range);
                                colValue.add(range.toString());     
                            } catch (NumberFormatException e) {
                                if (numExceptions < 10) {
                                System.out.println("Exception!!" + e);
                                System.out.println(Arrays.toString(nextLine));
                                numExceptions++;
                                }
                            }
                          
                            
                        } else if (colMap.get(col).equals("drinks")) {
                            colValue.add(nextLine[col]);
                        } else if (colMap.get(col).equals("job")) {
                            colValue.add(nextLine[col]);
                        } else if (colMap.get(col).equals("religion")) {
                            colValue.add(nextLine[col]);
                        } else if (colMap.get(col).startsWith("essay")) {
                            essay += " "+ nextLine[col];
                        } 
                    }
                }
                String plainEssay = new HtmlToPlainText().getPlainText(Jsoup.parse(essay));
                colValue.add( plainEssay);
                dataList.add(colValue.toArray(new String[0]));
                String filename = "essay"+i;
                if (!StringUtils.trim(essay).isEmpty()) {
                      String plain = new HtmlToPlainText().getPlainText(Jsoup.parse(essay));  
                    createFile(filename, plain);
                    i++;
                }
            }
            createMetadataFileWithList(dataList);
     
    }
     public static void createMetadataFileWithList(List<String []> dataList) throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter("new_metadata.csv"));
        
     
        writer.writeAll(dataList);
        writer.flush();
        writer.close();
        
    }
    
 public static void createFile(String filename, String filetext) throws Exception {
       
            if (!StringUtils.isEmpty(filetext)) {
              
                System.out.println("writing file: " + filename);
                Files.write(new File(filename).toPath(), filetext.getBytes());

            }
          System.out.println("filename: " + filename );
        
    }
   

}
