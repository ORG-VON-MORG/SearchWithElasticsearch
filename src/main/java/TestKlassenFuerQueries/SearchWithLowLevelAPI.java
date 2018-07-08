package TestKlassenFuerQueries;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import java.io.IOException;
import java.util.*;
import java.lang.*;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;


public class SearchWithLowLevelAPI {

    public static RestClient restClient;

  public static void main(String[] args) {
       //getWordsFrequencies("C7BVyGMBKRrm5z8MDDI8", "contents.contentString");
       //getWordsFrequencies("ybGNyGMBKRrm5z8M_q_l", "contents.contentString");
       startClient();
       System.out.println(getDocsCount());
       getElasticIdFromArtikelId("34d4708d7cce27237b991c02c98eeeb5");
       getElasticIdFromArtikelId("35f30c00-efdd-11e2-a1f9-ea873b7e0424");
       getElasticIdFromArtikelId("ecb715f2-efd4-11e2-9008-61e94a7ea20d");
       closeClient();

   }

    /**
     * Funktion, die die Anzahl alle Dokumente im elasticsearch index zurückgibt
     * @return docsCount
     */
   public static long getDocsCount() {
       Map<String, String> params = Collections.emptyMap();
       HttpEntity entity = new NStringEntity("", ContentType.TEXT_PLAIN);
       try {
           Response response = restClient.performRequest("GET", "_stats/docs?pretty", params, entity);
           String responseBody = EntityUtils.toString(response.getEntity());
           long docsCount = new JSONObject(responseBody)
                                    .getJSONObject("_all")
                                    .getJSONObject("primaries")
                                    .getJSONObject("docs")
                                    .getLong("count");
           return docsCount;
       } catch (IOException e) {
           e.printStackTrace();
       }
       return 0;
   }

    /**
     * Funktion, die nimmt ein Zeitungsartikel ID und gibt die Index id im elasticsearch zurück
     * @param artikelId die Zeitungsartikel ID
     * @return elasticId
     */
   public static String getElasticIdFromArtikelId(String artikelId) {
       Map<String, String> params = Collections.emptyMap();
       String json = "{\"_source\":[\"_id\"],"       +
                      "\"query\":"                   +
                                "{\"match\":"        +
                                        "{\"id\":\"" + artikelId + "\"}}}";
       HttpEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
       try {
           Response response = restClient.performRequest("GET", "last/_doc/_search?pretty", params, entity);
           String responseBody = EntityUtils.toString(response.getEntity());
           String elasticId = new JSONObject(responseBody)
                                    .getJSONObject("hits")
                                    .getJSONArray("hits")
                                    .getJSONObject(0)
                                    .getString("_id");
           return elasticId;
       } catch (IOException e) {
           e.printStackTrace();
       }
       return null;
   }

    /**
     * Funktion, die Artikel-ID und ein Feld akzeptiert.
     * Diese Funktion gibt eine Map zurück, die ein Wort als Key und ein Integer-Array als Value enthält.
     * Das Array enthält: in wie vielen Dokumenten das Wort vorkommt,
     *                    wie oft das Wort insgesamt vorkommt und
     *                    wie oft das Wort im angegebenen Artikel(nach Artikel ID) vorkommt
     * @param artikelId Zeitungsartikel ID
     * @param field field in JSON-Datei, in dem die Funktion die Wörter suche
     * @return wordFreq
     */

   public static HashMap<String, int[]> getWordsFrequencies(String artikelId, String field){
       Map<String, String> params = Collections.emptyMap();
       String elasticId = getElasticIdFromArtikelId(artikelId);
       String contentString = getContentString(elasticId);
       String json = "{  \"ids\" : [\"" + elasticId +"\"], "    +
                        "\"parameters\": { "                    +
                            "\"fields\": [\""+ field +"\"], "   +
                            "\"term_statistics\": true, "       +
                            "\"offsets\":false, "               +
                            "\"payloads\":false, "              +
                            "\"positions\":false } }";
       HttpEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
       HashMap<String, int[]> wordFreq = new HashMap<String, int[]>();
       try {
           Response response = restClient.performRequest("GET", "last/_doc/_mtermvectors?pretty", params, entity);
           String responseBody = EntityUtils.toString(response.getEntity());
           JSONObject terms = new JSONObject(responseBody)
                                    .getJSONArray("docs")
                                    .getJSONObject(0)
                                    .getJSONObject("term_vectors")
                                    .getJSONObject("contents.contentString")
                                    .getJSONObject("terms");
           //iterate over the JSONObject, which contains the word AND another JSONObject (the statistics)
           Iterator keys = terms.keys();
           while (keys.hasNext()) {
               String key = (String)keys.next();
               JSONObject word = terms.getJSONObject(key);
               int[] wordStatistics = new int[3];
               wordStatistics[0] = word.getInt("doc_freq");     //in wie vielen Dokumenten das Wort vorkommt
               wordStatistics[1] = word.getInt("ttf");          //wie oft das Wort insgesamt vorkommt
               wordStatistics[2] = word.getInt("term_freq");    //wie oft das Wort im angegebenen Artikel vorkommt
               wordFreq.put(key, wordStatistics);
           }
           //comment this line below to disable printing the Statistics
           //printWordFrequencies(wordFreq);
       } catch (IOException ioe) {
           ioe.printStackTrace();
       }
       return wordFreq;
   }

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
     * Funktion für die Ausgabe der wordFreq map
     * @param wordFreq
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


   //this method is getting contentstring from elasticId
   public static String getContentString(String elasticId){
       String cs = "";

       Map<String, String> params = Collections.emptyMap();
       HttpEntity entity = new NStringEntity("", ContentType.TEXT_PLAIN);
       try {
           String endpoint = "last/_doc/" + elasticId + "?_source_include=contents.contentString&pretty";
           Response response = restClient.performRequest("GET", endpoint, params, entity);
           String responseBody = EntityUtils.toString(response.getEntity());
           JSONArray content = new JSONObject(responseBody).getJSONObject("_source").getJSONArray("contents");
           for (int i = 0; i < content.length(); i++)
                      cs += content.getJSONObject(i).getString("contentString");
                      cs.replaceAll("[\\p{Punct}&&[^0-9]]", "");
       }
       catch (IOException ioe) {
           ioe.printStackTrace();
       }

       return Jsoup.parse(cs).text();
   }
}
