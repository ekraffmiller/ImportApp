/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enron;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import org.apache.mahout.math.Arrays;

/**
 *
 * @author ellenk
 */
public class EnronImport {
    public static void main(String args[]) throws IOException {
        File enronCSV = new File("files/enronEmails.csv");
        CSVReader reader = new CSVReader(new FileReader(enronCSV));
        CSVWriter writer = new CSVWriter(new FileWriter(new File("files/enronFull.tsv")),'\t');
        int count=0;
        int missing = 0;/*
        for (int i=0;i<10;i++) {
            String[] items=reader.readNext();
            System.out.println("ROW "+i+": "+ Arrays.toString(items));
            count++;
        }*/
        String[] elems = null;
        String pattern = "E, d MMM yyyy HH:mm:ss x (z)";
        DateTimeFormatter formatter =
                      DateTimeFormatter.ofPattern(pattern);
       // SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String[] headers = reader.readNext(); // skipHeaders
        System.out.println("Headers: "+ Arrays.toString(headers));
        writer.writeNext("monthAndYear","year","from","body");
        while(((count < 6000) &&( elems = reader.readNext())!=null)) {
            String datestr = elems[1];
         //   System.out.println("parsing: "+datestr);
             LocalDate date = null;
             String year = null;
             String monthYear=null;
             
            try {
                date = LocalDate.parse(datestr, formatter);
                monthYear = date.getMonth() + " " + date.getYear();
                year = "" + date.getYear();
            } catch (DateTimeParseException e) {
                // skip this date
                year = "Parse Exception";
                monthYear = "Parse Exception";
            }
            //Wed, 19 Sep 2001 18:11:58 -0700 (PDT)
              // if (count> 6000) {
               //    System.out.println("length: "+elems.length);
              //     System.out.println(Arrays.toString(elems));
             //  }
             String from = elems[2];
            String body = elems[15];
            List<String> list = Collections.list(new StringTokenizer(body, "\n")).stream()
                    .map(token -> (String) token).collect(Collectors.toList());

            from = replaceChars(from);
            List<String> filtered = new ArrayList<>();
            for (String line : list) {
            //    System.out.println("line: "+line);
                if (!(line.startsWith("To:")
                        || line.startsWith("From:")
                        || line.startsWith("cc:")
                        || line.startsWith("----"))) {
               //     System.out.println("adding line: " + line);
                    filtered.add(line);
                }
            }
            String filteredBody = new String();
            for (String f: filtered) {
                filteredBody = filteredBody+" " + f;
            }
            
            body = replaceChars(filteredBody);
     
            //    System.out.println("body: "+ body);
                writer.writeNext(monthYear,year,from,body);
            count++;
            System.out.println(count);
        }
        writer.flush();
        writer.close();
        System.out.println("count:"+count);
       }
    
       private static String replaceChars(String s) {
           s = s.replace('\t', ' ');
                s = s.replace("\"","");
                s = s.replace('\n', ' ');
                s = s.replace("-", " ");
                  s = s.replace("<br>", " ");
                s = s.replace("</b>", " ");
                s = s.replace("</td>", " ");
                s = s.replace("</font>", " ");
                s = s.replace("href", " ");
             s = s.replace("http", " ");
                s = s.replace("serif", " ");
                s = s.replace("<", " ");
                s = s.replace(">", " ");
                 return s.trim();
       }
}
