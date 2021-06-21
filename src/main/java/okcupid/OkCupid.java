/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package okcupid;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;

/**
 *
 * @author ellenk
 */
public class OkCupid {

    public static void main(String args[]) throws Exception {

        String csv = new String("/Users/ellenk/Downloads/profiles_no_essays.csv");
        String essayCSV = new String("/Users/ellenk/Downloads/essays2_endofline.csv");
        String metadataCSV = new String("/Users/ellenk/Downloads/okcupid_new.csv");
        String essayDir = new String("/Users/ellenk/scratch/okcupid2000/essays/");
        int docsetSize = 2000;
      //  createMetadata(csv, metadataCSV, docsetSize);

        createFiles(essayCSV, essayDir, docsetSize);

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

    public static void createFiles(String csv, String essayDir, int docsetSize) throws Exception {

        Scanner scanner = new Scanner(new File(csv));
        String scanned = "";
        int i = 0;
        StringBuilder line;
        //    Pattern p = Pattern.compile("*okcupidendofline");
        while (i < docsetSize) {
            boolean foundPattern = false;
            line = new StringBuilder();
            while (!foundPattern) {

                scanned = scanner.nextLine();

                // Matcher m = p.matcher(scanned);
                if (scanned.endsWith("okcupidendofline")) {
                    line.append(" " + scanned);
                    foundPattern = true;
                } else {
                    line.append(" " + scanned);
                }
            }
            String essay = line.toString();
            essay = essay.substring(0, essay.indexOf("okcupidendofline") - 1);
            String filename = essayDir + "essay" + i;
            if ( !StringUtils.remove(StringUtils.trim(essay),",").isEmpty()) {
                String plain = new HtmlToPlainText().getPlainText(Jsoup.parse(essay));
                System.out.println("" + i + " " + plain);
                createFile(filename, plain);
                
            }
            i++;
        }
        System.out.println("wrote " + i + " files.");

    }
    
    public static void getSubset(String csv,  String destination, int docsetSize) throws Exception {
        CSVReader reader = new CSVReader(new FileReader(csv));
       File fout = new File(destination);
	FileOutputStream fos = new FileOutputStream(fout);
 
	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
 
	for (int i = 0; i < 10; i++) {
		bw.write("something");
		bw.newLine();
	}
 
	    String[] nextLine;
        int i = 0;
        while ((nextLine = reader.readNext()) != null && i < docsetSize) {
            bw.write(Arrays.toString(nextLine));
        }
        bw.close();
    }

    public static void createMetadata(String csv, String destination, int docsetSize) throws Exception {
        CSVReader reader = new CSVReader(new FileReader(csv));
        String[] nextLine;
        List<String[]> dataList = new ArrayList<>();
        String[] arr = {"age", "drinks", "ethnicity", "job", "orientation", "religion", "filename", 
            "essay0", "essay1", "essay2", "essay3", "essay4", "essay5", "essay6", "essay7", "essay8","essay9"
        };

        List<String> headers = Arrays.asList(arr);
        HashMap<Integer, String> colMap = new HashMap<>();

        dataList.add(arr);
        nextLine = reader.readNext();  // headers
        int idx = 0;
        for (idx = 0; idx < nextLine.length; idx++) {
            if (headers.contains(nextLine[idx])) {
                colMap.put(idx, nextLine[idx]);
            }
        }

        int i = 0;
        int numExceptions = 0;
        while ((nextLine = reader.readNext()) != null && i < docsetSize) {
            ArrayList<String> colValue = new ArrayList<>();

            for (int col = 0; col < nextLine.length; col++) {
                if (colMap.containsKey(col)) {
                    if (colMap.get(col).equals("age")) {
                        System.out.println("col" + col + ", nextLine[col] " + nextLine[col]);
                        Integer range = 0;
                        try {
                            range = Integer.valueOf(nextLine[col]);
                            range = range / 10;
                            System.out.println("range: " + range);
                            colValue.add(range.toString());
                        } catch (NumberFormatException e) {
                            if (numExceptions < 10) {
                                System.out.println("Exception!!");
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
                    } else if (colMap.get(col).equals("ethnicity")) {
                        colValue.add(nextLine[col]);
                    } else if (colMap.get(col).equals("orientation")) {
                        colValue.add(nextLine[col]);
                    } else if (colMap.get(col).startsWith("essay")) {
                        colValue.add(nextLine[col]);
                    }
                }
            }
            String filename = "essay" + i;
            colValue.add(filename);
            i++;
            dataList.add(colValue.toArray(new String[0]));

        }
        System.out.println("creating metadata for " + dataList.size() + " files.");
        createMetadataFileWithList(dataList, destination);

    }

    public static void createMetadataFileWithList(List<String[]> dataList, String destination) throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter(destination));

        writer.writeAll(dataList);
        writer.flush();
        writer.close();

    }

    public static void createFile(String filename, String filetext) throws Exception {

        if (!StringUtils.isEmpty(filetext)) {

            System.out.println("writing file: " + filename);
            Files.write(new File(filename).toPath(), filetext.getBytes());

        }
        System.out.println("filename: " + filename);

    }

}
