package TestKlassenFuerQueries;

import Util.util;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.*;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;


public class SearchWithLowLevelAPI {

    public static RestClient restClient;

    /**
     * Funktion, die die Anzahl alle Dokumente im elasticsearch index zurückgibt
     * @return docsCount
     */
   public static long getDocsCount() {
       Map<String, String> params = Collections.emptyMap();
       HttpEntity entity = new NStringEntity("", ContentType.TEXT_PLAIN);
       try {
           startClient();
           Response response = restClient.performRequest("GET", "_stats/docs?pretty", params, entity);
           String responseBody = EntityUtils.toString(response.getEntity());
           long docsCount = new JSONObject(responseBody)
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
   public static String getElasticIdFromArtikelId(String artikelId) {
       Map<String, String> params = Collections.emptyMap();
       String json = "{\"_source\":[\"_id\"],"       +
                      "\"query\":"                   +
                                "{\"match\":"        +
                                        "{\"id\":\"" + artikelId + "\"}}}";
       HttpEntity entity = new NStringEntity(json, APPLICATION_JSON);
       try {
           startClient();
           Response response = restClient.performRequest("GET", "last/_doc/_search?pretty", params, entity);
           String responseBody = EntityUtils.toString(response.getEntity());
           String elasticId = new JSONObject(responseBody)
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
       List<String> paragraph = getCleanContentString(elasticId);
       HashMap<String, int[]> wordFreq = null;

       for(String sentences : paragraph) {
           String json = "{\"doc\":{" +
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
           HttpEntity entity = new NStringEntity(json, APPLICATION_JSON);
           wordFreq = new HashMap<String, int[]>();
           try {
               startClient();
               Response response = restClient.performRequest("GET", "last/_doc/_termvector?pretty", params, entity);
               String responseBody = EntityUtils.toString(response.getEntity());

               JSONObject terms = new JSONObject(responseBody)
                       .getJSONObject("term_vectors")
                       .getJSONObject("contents.contentString")
                       .getJSONObject("terms");
               //System.out.println(terms);
               //iterate over the JSONObject, which contains the word AND another JSONObject (the statistics)
               Iterator keys = terms.keys();
               while (keys.hasNext()) {
                   String key = ((String)keys.next());
                   JSONObject word = terms.getJSONObject(key);
                   System.out.println(key);
                   System.out.println(word);
                   int[] wordStatistics = new int[3];

                   int doc_freq     = word.isNull("doc_freq") ? 1 : word.getInt("doc_freq");
                   int ttf          = word.getInt("ttf");
                   int term_freq    = word.getInt("term_freq");

                   if(wordFreq.containsKey(key)) {
                       int[] oldWordStatistics = wordFreq.get(key);
                       wordStatistics[0] = oldWordStatistics[0] + doc_freq;     //in wie vielen Dokumenten das Wort vorkommt
                       wordStatistics[1] = oldWordStatistics[1] + ttf;          //wie oft das Wort insgesamt vorkommt
                       wordStatistics[2] = oldWordStatistics[2] + term_freq;    //wie oft das Wort im angegebenen Artikel vorkommt
                   } else {
                       //System.out.println(word.isNull("doc_freq"));
                       wordStatistics[0] = doc_freq;     //in wie vielen Dokumenten das Wort vorkommt
                       wordStatistics[1] = ttf;          //wie oft das Wort insgesamt vorkommt
                       wordStatistics[2] = term_freq;    //wie oft das Wort im angegebenen Artikel vorkommt
                   }
                   wordFreq.put(key, wordStatistics);
               }
               //comment this line below to disable printing the Statistics
               //printWordFrequencies(wordFreq);
               closeClient();
           } catch (IOException ioe) {
               ioe.printStackTrace();
           }
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

   public static List<String> getCleanContentString(String elasticId){
       List<String> contentStringList = new ArrayList<String>();
       Map<String, String> params = Collections.emptyMap();
       HttpEntity entity = new NStringEntity("", ContentType.TEXT_PLAIN);
       try {
           startClient();
           String endpoint = "last/_doc/" + elasticId + "?_source_include=contents.contentString&pretty";
           Response response = restClient.performRequest("GET", endpoint, params, entity);
           String responseBody = EntityUtils.toString(response.getEntity());
           JSONArray content = new JSONObject(responseBody)
                                     .getJSONObject("_source")
                                     .getJSONArray("contents");
           for (int i = 0; i < content.length(); i++) {
               String paragraph = util.cleanXMLTags(content.getJSONObject(i).getString("contentString"));
               contentStringList.add(paragraph);
           }
           //System.out.println("done ");
           closeClient();
       }
       catch (IOException ioe) {
           ioe.printStackTrace();
       }
       return contentStringList;
   }


    public static void main(String[] args) {

        //getWordsFrequencies("ybGNyGMBKRrm5z8M_q_l", "contents.contentString");
        startClient();
        getWordsFrequencies("34d4708d7cce27237b991c02c98eeeb5", "contents.contentString");
        //System.out.println(getDocsCount());
        //getElasticIdFromArtikelId("34d4708d7cce27237b991c02c98eeeb5");
        //getElasticIdFromArtikelId("35f30c00-efdd-11e2-a1f9-ea873b7e0424");
        //getElasticIdFromArtikelId("ecb715f2-efd4-11e2-9008-61e94a7ea20d");
        //util.getStopWordListAsSet();
        //String s = util.removeStopWords(util.cleanXMLTags(getContentString("j6qkx2MBKRrm5z8MDDOZ")));
        //util.removeStopWords(s);
        //System.out.println(s);
        //System.out.println(util.stopWordSet.size());
        //getCleanContentString(getElasticIdFromArtikelId("34d4708d7cce27237b991c02c98eeeb5"));
        closeClient();

    }

}
