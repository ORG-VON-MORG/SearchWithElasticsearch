package TestKlassenFuerQueries;

import Util.util;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.*;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Diese Klasse bearbeiten mit alle Funktionen, die curl-Kommando auf elasticsearch index ausführen
 */
public class SearchWithLowLevelAPI {

    private static RestClient restClient;
    private static final String indexName = "final";

    public static void startClient() {
        restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
    }

    public static void closeClient() {
        try {
            restClient.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Funktion, die die Anzahl alle Dokumente im elasticsearch index zurückgibt
     * @return docsCount
     */
   public static long getDocsCount() {
       String endpoint              = indexName + "/_stats";
       Map<String, String> params   = Collections.emptyMap();
       HttpEntity entity            = new NStringEntity("", ContentType.TEXT_PLAIN);
       try {
           startClient();
           Response response        = restClient.performRequest("GET", endpoint, params, entity);
           String responseBody      = EntityUtils.toString(response.getEntity());
           long docsCount           = new JSONObject(responseBody)
                                            .getJSONObject("_all")
                                            .getJSONObject("primaries")
                                            .getJSONObject("docs")
                                            .getLong("count");
           closeClient();
           return docsCount;
       } catch (IOException e) {
           e.printStackTrace();
       }
       closeClient();
       return 0;
   }

    /**
     * Funktion, die nimmt ein Zeitungsartikel ID und gibt die Index id im elasticsearch zurück
     * @param artikelId die Zeitungsartikel ID
     * @return elasticId
     */
   private static String getElasticIdFromArtikelId(String artikelId) {
       Map<String, String> params   = Collections.emptyMap();
       String endpoint              = indexName + "/_doc/_search";
       String json                  = "{\"_source\":[\"_id\"],"       +
                                       "\"query\":"                   +
                                                 "{\"match\":"        +
                                                    "{\"id\":\"" + artikelId + "\"}}}";
       HttpEntity entity            = new NStringEntity(json, APPLICATION_JSON);
       try {
           startClient();
           Response response        = restClient.performRequest("GET", endpoint, params, entity);
           String responseBody      = EntityUtils.toString(response.getEntity());
           String elasticId         = new JSONObject(responseBody)
                                            .getJSONObject("hits")
                                            .getJSONArray("hits")
                                            .getJSONObject(0)
                                            .getString("_id");
           closeClient();
           return elasticId;
       } catch (IOException e) {
           e.printStackTrace();
       }
       return null;
   }

    /**
     * Hilfsfunktion, nimmt elasticId und gibt nur die Inhalte von Felder contents.contentString zurück
     * @param elasticId die elasticId von bestimmte artikel
     * @return Liste, die alle content.contentString enthält
     */
    private static List<String> getCleanContentString(String elasticId) {
        List<String> contentStringList   = new ArrayList<String>();
        String endpoint                  = indexName + "/_doc/" + elasticId;
        Map<String, String> params       = new HashMap<String, String>();
        params.put("_source_include", "contents.contentString");
        HttpEntity entity                = new NStringEntity("", ContentType.TEXT_PLAIN);
        try {
            startClient();
            Response response            = restClient.performRequest("GET", endpoint, params, entity);
            String responseBody          = EntityUtils.toString(response.getEntity());
            JSONArray content            = new JSONObject(responseBody)
                                                 .getJSONObject("_source")
                                                 .getJSONArray("contents");
            //put every content.contentString field into the List
            for (int i = 0; i < content.length(); i++) {
                String paragraph = util.removeStopWords(
                                   util.cleanXMLTags(
                                   content.getJSONObject(i).getString("contentString")));
                contentStringList.add(paragraph);
            }
            closeClient();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return contentStringList;
    }

    /**
     * Funktion, die Artikel-ID und ein Feld akzeptiert.
     * Diese Funktion gibt eine Map zurück, die ein Wort als Key und ein Integer-Array als Value enthält.
     * Das Array enthält: in wie vielen Dokumenten das Wort vorkommt,
     *                    wie oft das Wort insgesamt vorkommt und
     *                    wie oft das Wort im angegebenen Artikel(nach Artikel ID) vorkommt
     * @param artikelId Zeitungsartikel ID
     * @return wordFreq
     */
   public static HashMap<String, int[]> getWordsFrequencies(String artikelId) {
       HashMap<String, int[]> wordFreq  = new HashMap<String, int[]>();
       String elasticId                 = getElasticIdFromArtikelId(artikelId);
       List<String> paragraph           = getCleanContentString(elasticId);
       //Liste enthält keine Strings mit "-Zeichen und keine leere Strings
       List<String> paragraphBereinigt  = new ArrayList<String>();
       String endpoint                  = indexName + "/_doc/_termvector";
       Map<String, String> params       = Collections.emptyMap();
       startClient();

       //iterate over all content.contentString field
       //this for loop will create artificial document for every content.contentString field, and count the statistics

       //for Schleife entfernt "-Zeichen und entfernt leere Strings
       for (String sentences : paragraph) {
           sentences = sentences.replaceAll("\"","").trim();

           if (!(sentences.isEmpty())){
               paragraphBereinigt.add(sentences);
           }


       }


       for(String sentences : paragraphBereinigt) {

           String json                  = "{\"doc\":{" +
                                                    "\"id\":\"test\"," +
                                                    "\"article_url\":\"www.test.com\"," +
                                                    "\"title\":\"test\"," +
                                                    "\"author\":\"test\"," +
                                                    "\"published_date\":1374190070000," +
                                                    "\"contents\":[{" +
                                                        "\"contentString\":\"" + sentences + "\"}]" +
                                                    "}," +
                                                "\"fields\":[\"contents.contentString\"]," +
                                                "\"field_statistics\":false," +
                                                "\"term_statistics\":true," +
                                                "\"positions\":false," +
                                                "\"offsets\":false" +
                                            "}";
           HttpEntity entity            = new NStringEntity(json, APPLICATION_JSON);


           try {
               Response response        = restClient.performRequest("GET", endpoint, params, entity);
               String responseBody      = EntityUtils.toString(response.getEntity());
               JSONObject terms         = new JSONObject(responseBody)
                                                .getJSONObject("term_vectors")
                                                .getJSONObject("contents.contentString")
                                                .getJSONObject("terms");
               //iterate over the JSONObject, which contains the word AND another JSONObject (the statistics)
               Iterator keys = terms.keys();
               while (keys.hasNext()) {
                   String key           = (String)keys.next();
                   JSONObject word      = terms.getJSONObject(key);
                   //System.out.println(key);
                   //System.out.println(word);
                   int[] wordStats      = new int[3];

                   //TODO: Double check this later
                   int doc_freq         = word.isNull("doc_freq") ? 1 : word.getInt("doc_freq");
                   int ttf              = word.isNull("ttf")      ? 1 : word.getInt("ttf");
                   int term_freq        = word.getInt("term_freq");

                   //the map already contains the word?
                   if(wordFreq.containsKey(key)) {
                       wordStats[0]     = doc_freq;
                       wordStats[1]     = ttf;
                       wordStats[2]     = wordFreq.get(key)[2] + term_freq;
                   } else {
                       wordStats[0]     = doc_freq;                            //in wie vielen Dokumenten das Wort vorkommt
                       wordStats[1]     = ttf;                                 //wie oft das Wort insgesamt vorkommt
                       wordStats[2]     = term_freq;                           //wie oft das Wort im angegebenen Artikel vorkommt
                   }
                   wordFreq.put(key, wordStats);
               }
               //comment this line below to disable printing the Statistics
               //printWordFrequencies(wordFreq);
           } catch (IOException ioe) {
               ioe.printStackTrace();
           }
       }
       closeClient();
       return wordFreq;
   }

    /**
     * Funktion für die Ausgabe der wordFreq map
     * @param wordFreq der Map
     */
    public static void printWordFrequencies(HashMap<String, int[]> wordFreq) {
        System.out.printf("%-30s    %-10s%-10s%-10s%n", "Word", "doc_freq", "ttf", "term_freq");
        System.out.println("----------------------------------------------------------------");
        for (String st : wordFreq.keySet()) {
            System.out.printf("%-30s:   ", st);
            for(int i : wordFreq.get(st))
                System.out.printf("%-10s", i);
            System.out.println();
        }
        System.out.println("----------------------------------------------------------------");
        System.out.printf("%-30s    %-10s%-10s%-10s%n", "Word", "doc_freq", "ttf", "term_freq");
    }
}
