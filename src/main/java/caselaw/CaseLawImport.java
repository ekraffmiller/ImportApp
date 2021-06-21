/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package caselaw;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author ellenk
 */
public class CaseLawImport {
        public static void main(String args[]) throws Exception {
            // Open stream to file
            File jsonFile = new File("files/appeals_court_opinions_compact.txt");
            File outFile = new File("files/appeals_court_opinions_caselaw.tsv");
            BufferedReader reader = new BufferedReader(new FileReader(jsonFile));
            // Read each line into a JSON Object
            // For each opinion, write a CSV line containing date, opinion type, author, and opinion text
            // Use tab separation
            // write out each line to file using CSVWriter
             JSONParser parser = new JSONParser();
             String line= reader.readLine();
             CSVWriter writer = new CSVWriter(new FileWriter(outFile), '\t');
             String[] headers = {"year","author","type","text"};
             writer.writeNext(headers);
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
             Calendar cal = Calendar.getInstance();
             int count = 0;
             while (line!=null) {
                    JSONObject obj = (JSONObject)parser.parse(line);
                    String date = (String)obj.get("date");
                    String year = null;
                    try {
                        cal.setTime(sdf.parse(date));
                        year = Integer.toString(cal.get(Calendar.YEAR));
                    } catch (java.text.ParseException e) {
                        // Assume parse failed because the date is actually
                        // just a year
                        year = date;
                    }
                    JSONArray arr = (JSONArray)obj.get("opinions");
                    for ( int i=0;i< arr.size();i++) {
                        JSONObject opinion = (JSONObject)arr.get(i);
                        String text = (String)opinion.get("text");
                        text = text.replace('\n', ' ');
                        text = text.replace('\t', ' ');
                        text = text.replace('\r', ' ');
                        String[] row = {                        
                        year,
                        (String)opinion.get("author"),
                        (String)opinion.get("type"),
                        text };
                        
                        writer.writeNext(row);
                    }
                    line = reader.readLine();
                    count++;
             }
             System.out.println("count: "+ count);
             writer.flush();
             writer.close();
             
        }
}
