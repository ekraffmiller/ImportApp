/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Read the text files that have been saved by the RData from Tracy's Statistics
 * document set.
 * Read each line into a row in a csv table, then save the table.
 * Abstract: the text
 * DocType:  type of publication that the abstract refers to
 * @author ellenk
 */
public class StatImport {
    public static void main(String args[])throws  IOException {  
              File docTypesFile = new File("files/stat_docTypes.txt");
             
            createNewCSV(docTypesFile);
      
    }
   
    public static void createNewCSV(File docTypesFile) throws IOException {
        List<String> docTypes = Files.readAllLines(docTypesFile.toPath());
        File csvFile = new File("files/statabstracts.csv");
        CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
        writer.writeNext("docType", "abstract");
        int i = 1;
        for (String s : docTypes) {
            String fileName = "statabs" + i + ".txt";
            String text = "";
            List<String> lines = Files.readAllLines((new File("files/statsabstracts", fileName)).toPath());
            for (int j = 0; j < lines.size(); j++) {
                String line = lines.get(j) + " ";
                line = line.replace('\t', ' ');
                text = text + line;
                text = text.replaceFirst("Abstract: ", " ");
            }
            
            i++;
            if (text.trim().length() > 0) {
                writer.writeNext(s.replaceFirst("Document Type: ", " ").trim(), text.trim());
            }
        }
    }
}
