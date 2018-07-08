package Util;

import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.lang.Math;

import static TestKlassenFuerQueries.SearchWithLowLevelAPI.getDocsCount;
import static TestKlassenFuerQueries.SearchWithLowLevelAPI.getWordsFrequencies;

public class util {

    public static final long docsCount = getDocsCount();
    public static final Set<String> stopWordSet = getStopWordListAsSet();



    /**
     * Methode Splitten einen String in Teilwoerter und gibt ein Array zurueck.
     * Tokenization mittles White-Space
     * @param string String, welcher zerteilt werden soll
     * @return Gibt ein Array mit den Teilwoerten zurueck
     */
    public static String[] StringToArrayTokenization(String string){
        return string.split("\\s");
    }

    /**
     * Methode Nimmt eine WAPOId entgegen, berechnet IDF und gibt den Wert
     * zurueck
     * @param WAPOId Die ID im Index der WAPO entgegen
     * @return Gibt eine HashMap zurueck mit dem Wort als String und der errechneten IDF
     */
    public static HashMap calculateIDF(String WAPOId){
        HashMap<String,int[]> mapDocFreq;
        Iterator it;
        HashMap<String,Double> idf = new HashMap<String, Double>();


        mapDocFreq = getWordsFrequencies(WAPOId, "contents.contentString");

        //it = mapDocFreq.entrySet().iterator();

        for (Map.Entry<String, int[]> entry : mapDocFreq.entrySet()) {
            String key = entry.getKey();
            int[] value = entry.getValue();

            //idf = log(1 + |D|/df)
            //|D| : anzahl alle Dokumente im Index, df : doc_freq
            double idfValue =value[2]* Math.log( (docsCount/(double)value[0]));
            idf.put(key, idfValue);
        }
        return idf;


    }

    /**
     * Methode nimmt HashMap aus calcuteIDF entgegen und sortiert diese basierend auf idf-Ranking
     * @param idf nimmt HashMap idf entgegen
     * @return gibt sortierte List zurueck
     */

    public static List<Entry<String, Double>> sortedMap(HashMap<String, Double> idf)
    {
        Set<Entry<String, Double>> set = idf.entrySet();
        List<Entry<String, Double>> list = new ArrayList<Entry<String, Double>>(set);
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        return list;
    }

    public static Set<String> getStopWordListAsSet() {
        Set <String> stopWordSet = new HashSet<String>();
        try {
            BufferedReader buffered = new BufferedReader(new FileReader(new File("src/main/resources/stopwords")));
            String line;
            while((line = buffered.readLine()) != null) {
                stopWordSet.add(line);
            }
            return stopWordSet;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isStopword(String word) {
        if(word.length() < 2) return true;
        if(word.charAt(0) >= '0' && word.charAt(0) <= '9') return true; //remove numbers, "25th", etc
        if(stopWordSet.contains(word.toLowerCase())) return true;
        else return false;
    }

    public static String removeStopWords(String string) {
        String result = "";
        String[] words = string.split("\\s+");
        for(String word : words) {
            if(word == null || word.trim().length() == 0) continue;
            if(isStopword(word)) continue; //remove stopwords
            result += (word+" ");
        }
        return result;
    }

    public static String cleanXMLTags(String string) {
        return Jsoup.parse(string).text();
    }
}