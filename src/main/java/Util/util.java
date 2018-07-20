package Util;

import org.elasticsearch.search.SearchHit;
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

    private static final long docsCount = getDocsCount();
    private static final Set<String> stopWordSet = getStopWordListAsSet();


    /**
     * Methode Splitten einen String in Teilwoerter und gibt ein Array zurueck.
     * Tokenization mittles White-Space
     *
     * @param string String, welcher zerteilt werden soll
     * @return Gibt ein Array mit den Teilwoerten zurueck
     */
    public static String[] StringToArrayTokenization(String string) {
        return string.split("\\s");
    }

    /**
     * Methode Nimmt eine WAPOId entgegen, berechnet IDF und gibt den Wert
     * zurueck
     *
     * @param WAPOId Die ID im Index der WAPO entgegen
     * @return Gibt eine HashMap zurueck mit dem Wort als String und der errechneten IDF
     */
    public static HashMap calculateIDF(String WAPOId) {
        HashMap<String, int[]> mapDocFreq;
        Iterator it;
        HashMap<String, Double> idf = new HashMap<String, Double>();


        mapDocFreq = getWordsFrequencies(WAPOId);

        //it = mapDocFreq.entrySet().iterator();

        for (Map.Entry<String, int[]> entry : mapDocFreq.entrySet()) {
            String key = entry.getKey();
            int[] value = entry.getValue();

            //idf = tf * log(1 + |D|/df)
            //|D| : anzahl alle Dokumente im Index, df : doc_freq
            double idfValue = value[2] * Math.log((docsCount / (double) value[0]));
            idf.put(key, idfValue);
        }
        return idf;


    }

    /**
     * Methode nimmt HashMap aus calcuteIDF entgegen und sortiert diese basierend auf idf-Ranking
     *
     * @param idf nimmt HashMap idf entgegen
     * @return gibt sortierte List zurueck
     */

    public static List<Entry<String, Double>> sortedMap(HashMap<String, Double> idf) {
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

    /**
     * Diese Funktion liest die Stopwortlist in resources folder und füge alle Wörter in einem Set
     *
     * @return stopWordSet das Stopwort-Set
     */
    public static Set<String> getStopWordListAsSet() {
        Set<String> stopWordSet = new HashSet<String>();
        try {
            BufferedReader buffered = new BufferedReader(new FileReader(new File("src/main/resources/stopwords")));
            String line;
            while ((line = buffered.readLine()) != null) {
                stopWordSet.add(line);
            }
            return stopWordSet;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * prüfe, ob ein Wort ein Stopwort oder nicht anhand gespeicherte Stopword-Set
     *
     * @param word zu überprüfende Wort
     * @return true wenn das Wort ein Stopwort ist, false wenn nicht
     */
    private static boolean isStopword(String word) {
        if (word.length() < 2) return true;
        if (word.charAt(0) >= '0' && word.charAt(0) <= '9') return true; //remove numbers, "25th", etc
        if (stopWordSet.contains(word.toLowerCase())) return true;
        else return false;
    }

    /**
     * Diese Funktion nimmt ein String und filtert alle Stopwörter heraus
     *
     * @param string das String
     * @return result gefilterte String
     */
    public static String removeStopWords(String string) {
        String result = "";
        String[] words = string.split("\\s+");
        for (String word : words) {
            if (word == null || word.trim().length() == 0) continue;
            if (isStopword(word)) continue; //remove stopwords
            result += (word + " ");
        }
        return result;
    }

    /**
     * nimmt ein String, lösche alle XML- oder HTML-Tags
     *
     * @param string das String
     * @return gefilterte String
     */
    public static String cleanXMLTags(String string) {
        return Jsoup.parse(string).text();
    }


    /**
     *
     * @param filepath wird entgegengenommen, Methode liest XML Dokument aus
     * @return Hashmap mit gegebener Nummer und DocID
     * @throws IOException
     * @author Kevin Engelhardt
     */

    public HashMap<Integer, String> readfromXML(String filepath) throws IOException {
        HashMap<Integer, String> hsm = new HashMap<Integer, String>();
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        String line; // Hier noch nicht br.readLine(), weil in Kopf von while schon gelesen wird, dann wuerde die erste Zeile uebersprungen
        String id = "";
        Integer num = 0;
        boolean insideTop = false; // Flag, um zu pruefen, ob man sich innerhalb eines <top> Tags befindet
        while ((line = br.readLine()) != null) {
            // Pruefung auf <top> Tag
            if (line.contains("<top>")) {
                insideTop = true;
            } else if (line.contains("</top>")) {
                insideTop = false;
            }

            // Wenn innerhalb <top> Tag
            if (insideTop) {
                if (line.contains("<num>")) {
                    Integer number = Integer.parseInt(line.replace("<num>", "").replace("</num>", "").replace(" ", "").replace("Number:", ""));
                    num = number;
                } else if (line.contains("<docid>")) {
                    String docId = line.replace("<docid>", "").replace("</docid>", "").replace(" ", "");
                    id = docId;
                }
                hsm.put(num, id);

                // Falls <top> und </top> in der selben Zeile sind, wieder auf false setzen
                if (line.contains("</top>")) {
                    insideTop = false;
                }
            }
        }
        return hsm;
    }

    public static ArrayList<String[]> filterDuplicateResults(SearchHit[] searchHits) {
        ArrayList<String[]> arrayList       = new ArrayList<String[]>();
        ArrayList<result> results           = new ArrayList<result>();
        for (SearchHit hit : searchHits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String id                       = (String)sourceAsMap.get("id");
            Float score                     = hit.getScore();
            String title                    = (String)sourceAsMap.get("title");
            String author                   = (String)sourceAsMap.get("author");
            long pub_date                   = (Long)sourceAsMap.get("published_date");
            //erstelle für jede Ergebnis ein Objekt
            result att                      = new result(id, score, title, author, pub_date);
			//list enthält Artikel mit gleichen title, author, und published_date?
			if (results.contains(att)) {
				int index               = results.indexOf(att);
				if (results.get(index).getScore() < att.getScore()) {
					//ersetzen der alte Eintrag mit der neue (höheren score)
					results.set(index, att);
				}
			} else {
				results.add(att);
			}
        }
        for (result r : results) {
            arrayList.add(new String[]{r.getId(), r.getScore().toString()});
            //System.out.println(r.getTitle());      //for testing
        }
        //System.out.println("\n--------------------------\n\n");
        return arrayList;
    }
}

/**
 * Hilfsklasse, speichert Suchergebnisanfrage als einem Objekt
 */
class result
{
    private String id;
    private Float score;
    private String title;
    private String author;
    private long published_date;

    result(String id, Float score, String title, String author, long published_date) {
        this.id = id;
        this.score = score;
        this.title = title;
        this.author = author;
        this.published_date = published_date;
    }

    String getId() { return this.id; }
    String getTitle() { return this.title; }
    String getAuthor() { return this.author; }
    long getPublished_date() { return this.published_date; }
    Float getScore() { return this.score; }

    /**
     * prüfe, ob 2 Artikel gleich sind
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof result) {
            return this.title.equals(((result) obj).getTitle()) &&
                    this.author.equals(((result) obj).getAuthor()) &&
                    this.published_date == (((result) obj).getPublished_date());
        }
        return false;
    }
}
