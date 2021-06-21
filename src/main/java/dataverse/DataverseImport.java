/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataverse;


import au.com.bytecode.opencsv.CSVWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;

/**
 *
 * @author ellenk
 */
public class DataverseImport {

    public static void main(String args[]) throws Exception {

        String titleFile = "/Users/ellenk/scratch/harvard_dataverse/ellenhvddstitle.txt";
        String subjectFile = "/Users/ellenk/scratch/harvard_dataverse/ellenhvddssubject.txt";
        String fileDir = "/Users/ellenk/scratch/harvard_dataverse/allfiles/";
        String outputCSVFile = "subject.csv";
        int docsetSize = 50000;
  
   // scanFiles(titleFile, fileDir, docsetSize);
   // createMetadata(subjectFile, fileDir, docsetSize);
        convertToCSV(fileDir,subjectFile,outputCSVFile);
        System.out.println("DONE!");

    }
    
  

    
    
    public static void convertToPlainText(String queryCSV) throws java.io.IOException {
        File fout = new File("plaintext.csv");
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        Scanner scanner = new Scanner(new File(queryCSV));
        String scanned;
        try {
            while ((scanned = scanner.nextLine()) != null) {
                String plain = new HtmlToPlainText().getPlainText(Jsoup.parse(scanned));
                bw.write(plain);
                bw.newLine();
            }
        } catch (NoSuchElementException e) {
        }
        bw.flush();
        bw.close();

    }
    
    public static void convertToCSV(String fileDir, String sqlFile, String csvFile  ) throws Exception {
        Scanner scanner = new Scanner(new File(sqlFile));
        scanner.useDelimiter("\\n");
        List<String[]> csvList = new ArrayList<>();
        int i=0;
        scanner.next();
        scanner.next();
        while ( scanner.hasNext()) {
            String line = scanner.next();
              Scanner lineScanner = new Scanner(line).useDelimiter("\\|");
                System.out.println("LINE " + i + ": " + line);
                
                 String[] arr = new String[3];
                if (lineScanner.hasNext() ) arr[0] =lineScanner.next();
                if (lineScanner.hasNext() ) arr[1] =(lineScanner.next().trim()+".txt");
                 if (lineScanner.hasNext() ) arr[2] =lineScanner.next();
           
                csvList.add(arr);
            i++;
           }
        System.out.println("read "+i+ "lines");
        createMetadataFileWithList(csvList, fileDir+csvFile);
  
    }
    
    public static void scanFiles(String csv, String fileDir, int docsetSize) throws Exception {
        Scanner scanner = new Scanner(new File(csv));
        scanner.useDelimiter("\\n");
    

        int i = 0;
        String datasetId = "-1";
        String versionId = "-1";
        String completeText = "";
        while (i < docsetSize  && scanner.hasNext()) {
            String line = scanner.next();
            System.out.println("LINE " + i + ": " + line);
            Scanner lineScanner = new Scanner(line).useDelimiter("\\|");

          
                
                String token1= "";
                if (lineScanner.hasNext()) token1= lineScanner.next().trim();  // datasetId
                String token2 = "";
                if (lineScanner.hasNext()) token2 = lineScanner.next().trim();  // versionId
                
                String title = "";
                if (lineScanner.hasNext()) title = lineScanner.next().trim();  // title
                String description = "";
                if (lineScanner.hasNext()) description = lineScanner.next().trim(); //description
                String affiliation = "";
                if (lineScanner.hasNext()) affiliation = lineScanner.next().trim();  // affiliation  
                if (!StringUtils.isEmpty(token1)) {
                    if (token1.equals(datasetId)) {
                        continue;
                    } else {
                        datasetId = token1;
                        versionId = token2;
                    }
                }
                if (description.endsWith("+")) {
                    // save for next line
                    if (completeText.equals("")) {
                        completeText = title + " " + description.substring(0, description.length() - 1);
                    } else {
                        completeText += description.substring(0, description.length() - 1);
                    }
                } else {
                    // write the line!
                    if (completeText.equals("")) {
                         completeText = title + " " + description;
                    } else {
                         completeText= completeText.trim()+ description;
                    }
                    createFile(fileDir + versionId + ".txt", new HtmlToPlainText().getPlainText(Jsoup.parse(completeText)));
                    i++;
                    completeText = "";
                }
            }

        
    }

   

    public static void createMetadata(String csv, String destination, int docsetSize) throws Exception {
        Scanner scanner = new Scanner(new File(csv));
        scanner.useDelimiter("\\n");
        scanner.next();
        scanner.next();
        int i = 0;
        List<String[]> dataList = new ArrayList<>();
        String[] headers = new String[2];
        headers[0] = "filename";
        headers[1] = "subject";
        dataList.add(headers);
        while (i < docsetSize && scanner.hasNext()) {
            String[] arr = new String[2];
            String line = scanner.next();
        //    System.out.println("LINE " + i + ": " + line);
            Scanner lineScanner = new Scanner(line).useDelimiter("\\|");
            String datasetId ="";
            if (lineScanner.hasNext()) datasetId = lineScanner.next();
            String versionId = "";
            if (lineScanner.hasNext()) versionId = lineScanner.next().trim();
            
            String subject = "";
            if (lineScanner.hasNext()) subject = lineScanner.next().trim();
            arr[0] = versionId+".txt";
            arr[1] = subject;
            dataList.add(arr);
         //   System.out.println(datasetId + " " + versionId + " " + subject);
            
            i++;
        }
        createMetadataFileWithList(dataList, destination+"metadata.csv");

    }

    public static void createMetadataFileWithList(List<String[]> dataList, String destination) throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter(destination));

        writer.writeAll(dataList);
        writer.flush();
        writer.close();

    }

    public static void createFile(String filename, String filetext) throws Exception {

        if (!StringUtils.isEmpty(filetext)) {

            System.out.println("filetext: " + filetext.substring(0,Math.min(filetext.length()-1,100)));
            Files.write(new File(filename).toPath(), filetext.getBytes());

        }
        System.out.println("filename: " + filename);

    }

}
