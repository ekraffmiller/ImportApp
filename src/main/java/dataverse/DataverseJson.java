/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataverse;


import au.com.bytecode.opencsv.CSVWriter;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import edu.harvard.iq.text.core.elasticSearch.helpers.Tuple2;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.helper.StringUtil;

/**
 * Read Json export files to create text files for clustering, and a 
 * corresponding metadata file
 * @author ellenk
 */
public class DataverseJson {

    public static void main(String args[]) throws Exception {

        String jsonFile = "/Users/ellenk/scratch/harvard_dataverse/metadata/pretty2.txt";
        String hdlStr = "/Users/ellenk/scratch/harvard_dataverse/metadata/hdl";
        String dvnStr = "/Users/ellenk/scratch/harvard_dataverse/metadata/doi/DVN";
        String dvn1Str = "/Users/ellenk/scratch/harvard_dataverse/metadata/doi/DVN1";
        String parentDir = "/Users/ellenk/scratch/harvard_dataverse/metadata/docs/";

        String csvFile = "/Users/ellenk/scratch/harvard_dataverse/metadata/metadata.csv";

        List<String> result = readJson(new File(jsonFile));
        System.out.println("done! result: " + result);

        List< List<String>> resultList = ingestAllJson(hdlStr, dvnStr, dvn1Str);

        writeToCSV(resultList, csvFile);
        writeTextFiles(resultList, parentDir);

    }

    public static void writeTextFiles(List<List<String>> resultList, String parentDir) throws Exception {
        for (List<String> elem : resultList) {
            String text = new HtmlToPlainText().getPlainText(Jsoup.parse(elem.get(4)));
            createFile(parentDir + elem.get(0), text);
        }

    }

    public static void createFile(String filename, String filetext) throws Exception {

        if (!StringUtils.isEmpty(filetext)) {

            //   System.out.println("filetext: " + filetext.substring(0,Math.min(filetext.length()-1,100)));
            Files.write(new File(filename).toPath(), filetext.getBytes());

        }
        System.out.println("filename: " + filename);

    }

    /**
     * read all json files, compile into one list. Replace Affilations below
     * cutoff point with "Other"
     */
    public static List<List<String>> ingestAllJson(String hdlStr, String dvnStr, String dvn1Str) throws Exception {
        List<List<String>> resultList = readParentDir(hdlStr);
        int hdlSize = resultList.size();
        resultList.addAll(readParentDir(dvnStr));
        int dvnSize = resultList.size() - hdlSize;
        resultList.addAll(readParentDir(dvn1Str));
        int dvn1Size = resultList.size() - (hdlSize + dvnSize);

        // get counts for each unique affiliation value
        HashMap<String, Integer> countMap = new HashMap<>();
        resultList.forEach(item -> {
            String affiliation = item.get(1);
            if (countMap.containsKey(affiliation)) {
                int newCount = countMap.get(affiliation) + 1;
                countMap.put(affiliation, newCount);
            } else {
                countMap.put(affiliation, 1);
            }
        });

        // Replace affilations with less than cutoff occurrances with "Other"
        List<Entry<String, Integer>> orderedList = new ArrayList<>();

        orderedList.addAll(countMap.entrySet());
        /* ordering here just for debug purposes 
        orderedList.sort((Entry<String, Integer> e1, Entry<String, Integer> e2) -> {
            return e1.getValue().compareTo(e2.getValue());
        });
        orderedList.forEach(item -> System.out.println(item));
         */
        System.out.println("unique names: " + countMap.size());
        int cutoff = 20;
        long cutoffCount = orderedList.stream().filter(item -> item.getValue() >= cutoff).count();
        System.out.println("number of items >= " + cutoff + ": " + cutoffCount);
        Set<String> savedNames = orderedList.stream().filter(item -> item.getValue() >= cutoff).map(item -> item.getKey()).collect(Collectors.toSet());
        System.out.println("saved names: " + savedNames);

        resultList.forEach((item) -> {
            String affiliation = item.get(1);
            if (!savedNames.contains(affiliation)) {
                item.set(1, "Other");
            }

        });
        System.out.println("DONE!  sizes: " + hdlSize + "," + dvnSize + "," + dvn1Size);

        return resultList;
    }

    public static void writeToCSV(List< List<String>> resultList, String csvFile) throws Exception {

        // Write to CSV
        CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
        writer.writeNext("filename", "affiliation", "subject", "title");
        resultList.forEach(item -> {
            writer.writeNext(item.subList(0, 4).toArray(new String[0]));
        });
        writer.flush();
        writer.close();
    }

    public static List<List<String>> readParentDir(String parentDirStr) throws IOException {
        List<String> result = null;
        List<List< String>> resultList = new ArrayList<>();
        File parentDir = new File(parentDirStr);
        File[] subDirs = parentDir.listFiles();
        for (File subDir : subDirs) {
            File[] jsonFiles = subDir.listFiles();
            if (jsonFiles != null) {
                for (File json : jsonFiles) {
                    System.out.println("reading: " + json.getAbsolutePath());
                    result = readJson(json);
                    if (StringUtils.isEmpty(result.get(0))) {
                        System.out.println("null version Id");
                    }
                    if (!StringUtils.isEmpty(result.get(0))) {
                        //    System.out.println("adding: " + result);
                        resultList.add(result);
                    }
                }
            }
        }
        return resultList;
    }

   

    /**
     * Reads json file and returns a list of metadata strings: versionId,
     * affiliation, subject, and title
     *
     * @param jsonFile dataset export json file
     * @return list of values
     * @throws IOException
     */
    public static List<String> readJson(File jsonFile) throws IOException {
        Integer versionId = null;
        DocumentContext doc = JsonPath.parse(jsonFile);
        // Get data from document
        versionId = doc.read("$.datasetVersion.id");
        JSONArray affiliationValues = doc.read("$.datasetVersion.metadataBlocks.citation.fields[*].value[*].authorAffiliation.value");
        JSONArray subjects = doc.read("$.datasetVersion.metadataBlocks.citation.fields[?(@.typeName=='subject')].value");
        JSONArray titleArray = doc.read("$.datasetVersion.metadataBlocks.citation.fields[?(@.typeName=='title')].value");
        JSONArray descriptionValues = doc.read("$.datasetVersion.metadataBlocks.citation.fields[*].value[*].dsDescriptionValue.value");
        String title = (String) titleArray.get(0);

        // Convert data to String. Use Set to remove duplicates within each metadata value,
        // and alphabetical order to correctly compare occurances within the doc set
        Set<String> affiliationSet = new HashSet<>();
        affiliationValues.forEach(value -> {
            affiliationSet.add(value.toString());
        });
        List<String> affiliationList = new ArrayList<>();
        affiliationList.addAll(affiliationSet);
        affiliationList.sort((String s1, String s2) -> {
            return s1.compareTo(s2);
        });

        Set<String> subjectSet = new HashSet<>();
        subjects.forEach(item -> {
            JSONArray itemArray = (JSONArray) item;
            itemArray.stream().forEach((obj) -> {
                subjectSet.add(obj.toString());
            });
        });
        List<String> subjectList = new ArrayList<>();
        subjectList.addAll(subjectSet);
        subjectList.sort((String s1, String s2) -> {
            return s1.compareTo(s2);
        });

        // clusterText = title + all descriptionValues that aren't N/A
        String clusterText = title;
        clusterText = descriptionValues.stream()
                .filter((item) -> (!item.toString().equals("N/A")))
                .map((item) -> " " + item.toString()).reduce(clusterText, String::concat);

        ArrayList<String> list = new ArrayList<>();
        list.add(versionId + ".txt");
        
        
        list.add(String.join(",", affiliationList));
        if (StringUtils.isEmpty(list.get(1))) {
            list.set(1, "NONE");
        }
        list.add(String.join(",", subjectList));
        if (StringUtils.isEmpty(list.get(2))) {
            list.set(2, "NONE");
        }
        list.add(title);
        list.add(clusterText);

        return list;
    }


  


}
