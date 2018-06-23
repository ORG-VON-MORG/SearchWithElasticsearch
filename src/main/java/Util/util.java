package Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static TestKlassenFuerQueries.SearchWithLowLevelAPI.getWordsFrequencies;

public class util {



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
     * Methode Nimmt eine WAPOId entgegen, berechnet TFIDF und gibt den Wert
     * zurueck
     * @param WAPOId Die ID im Index der WAPO entgegen
     * @return Gibt eine HashMap zurueck mit dem Wort als String und der errechneten TFIDF
     */
    public static HashMap calculateIDF(String WAPOId){
        HashMap<String,int[]> mapDocFreq;
        Iterator it;
        HashMap<String,Integer> idf = new HashMap<String, Integer>();


        mapDocFreq = getWordsFrequencies(WAPOId, "contents.contentString");

        it = mapDocFreq.entrySet().iterator();

        for (Map.Entry<String, int[]> entry : mapDocFreq.entrySet()) {
            String key = entry.getKey();
            int[] value = entry.getValue();

            idf.put(key,(value[2]/value[3]));
        }


        return idf;


    }

}