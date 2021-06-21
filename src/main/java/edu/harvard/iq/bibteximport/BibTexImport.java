/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.bibteximport;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;

/**
 *
 * @author ellenk
 */
public class BibTexImport {

    public static void main(String args[]) throws Exception {
        String bibTexCSV = new String("/Users/ellenk/test/text_doc_root/King_no_dupes/ExportedItems.csv");
        String taxonomyCSV = new String("/Users/ellenk/test/text_doc_root/King_no_dupes/Gary_Taxonomy.csv");
        String queryCSV = new String("/Users/ellenk/test/text_doc_root/King_no_dupes/gking-pubs.csv");
     //   createAbstractFiles(bibTexCSV);
        createMetadataFile(bibTexCSV, taxonomyCSV);
     //   createAbstractsAndMetadata(queryCSV);

    }
    
    public static void createAbstractsAndMetadata(String queryCSV) throws Exception {
         CSVReader reader = new CSVReader(new FileReader(queryCSV));
         String[] nextLine;
         List<String[]> dataList = new ArrayList<>();
         String[] headers = {"Filename", "Item Type", "Publication", "Author", "Title", "Taxonomy"};
         dataList.add(headers);
         nextLine = reader.readNext();  // headers
         int taxonomyIndex=-1;
         for (int i=0;i< nextLine.length;i++) {
             if (nextLine[i].trim().equals("Research Interests")) {
                 taxonomyIndex = i;
             }
         }
         int i=1;
            while ((nextLine = reader.readNext()) != null) {
                String[] data = new String[7];
                String filename = "abstract"+i+".txt";
                String itemType = nextLine[0];
                String pubYear = nextLine[1];
                String author = nextLine[2];
                String title = nextLine[3];
                String abstractNote = nextLine[9];
                String taxonomy = nextLine[taxonomyIndex];
                data[0]=filename;
                data[1]=itemType;
                data[2]=pubYear;
                data[3]=author;
                data[4]=title;
                data[5]=taxonomy;
          //      data[6]=abstractNote;
                if (!StringUtils.isEmpty(abstractNote.trim())) {
                    dataList.add(data);
                }
             //   System.out.println(itemType+","+pubYear+","+author+","+title+","+abstractNote+","+taxonomy);
            
              String plain = new HtmlToPlainText().getPlainText(Jsoup.parse(abstractNote));  
              System.out.println("parsed abstract: "+plain);
                if (!StringUtils.trim(plain).isEmpty()) {
                    createAbstractFile(filename, plain);
                    i++;
                }
            }
            createMetadataFileWithList(dataList);
     
    }
     public static void createMetadataFileWithList(List<String []> dataList) throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter("metadatabibtexAbstract.csv"));
        
     
        writer.writeAll(dataList);
        writer.flush();
        writer.close();
        
    }
    public static void createMetadataFile(String bibTexCSV, String taxonomyCSV) throws Exception {
        CSVReader reader = new CSVReader(new FileReader(bibTexCSV));
        String[] nextLine;
        List<String[]> data = new ArrayList<String[]>();
        nextLine = reader.readNext();  // skip over headers for now 
        String[] headers = {"Filename", "Item Type", "Publication", "Author", "Title", "Taxonomy"};
            
        data.add(headers);
        CSVWriter writer = new CSVWriter(new FileWriter("metadatabibtex.csv"));
        Map<String, List<String>> titleMap = new HashMap<>();
        System.out.println("READING BIBTEX: ");
        while ((nextLine = reader.readNext()) != null) {
            
            String title = nextLine[4];
            System.out.println(title);
            ArrayList<String> dataList = new ArrayList<>();
            System.out.println(nextLine[0]+".txt");
            dataList.add(nextLine[0] + ".txt");
            dataList.add(nextLine[1]);
            dataList.add(nextLine[2]);
            dataList.add(nextLine[3]);
            dataList.add(nextLine[4]);         
            titleMap.put(title.trim(),dataList);
          //  data.add(lineData);
        }
        // Read taxonomy file - find title in map, then add taxonomy to list of linedata.
        CSVReader taxReader = new CSVReader(new FileReader(taxonomyCSV));
        taxReader.readNext();  // skip headers
        System.out.println("READING TAXONOMY: ");
        while((nextLine = taxReader.readNext())!=null) {
            String title = nextLine[1].trim();
            System.out.println(title);
            if (titleMap.containsKey(title)) {
                System.out.println("adding tax: " + nextLine[2]+" to title: "+ title);
                
                titleMap.get(title).add(nextLine[2]);
            }
        }
        for (List<String> dataList: titleMap.values()) {
            data.add(dataList.toArray(new String[0]));
        }
        writer.writeAll(data);
        writer.flush();
        writer.close();
        
    }
    
 public static void createAbstractFile(String filename, String abstractNote) throws Exception {
       
            if (!StringUtils.isEmpty(abstractNote)) {
              
                System.out.println("writing file: " + filename);
                Files.write(new File(filename).toPath(), abstractNote.getBytes());

            }
            System.out.println("filename: " + filename + ";   " + abstractNote);
        
    }
    public static void createAbstractFiles(String bibTexCSV) throws Exception {
        CSVReader reader = new CSVReader(new FileReader(bibTexCSV));
        String[] nextLine;
        nextLine = reader.readNext();  // skip over headers for now   
        while ((nextLine = reader.readNext()) != null) {
            String key = nextLine[0];
            String abstractNote = nextLine[10];
            if (!abstractNote.equals("")) {
                String filename = key + ".txt";
                File file = new File(filename);
                System.out.println("writing file: " + file.getAbsolutePath());
                Files.write(file.toPath(), abstractNote.getBytes());

            }
            System.out.println("key: " + key + ";   " + abstractNote);
        }
    }

}
