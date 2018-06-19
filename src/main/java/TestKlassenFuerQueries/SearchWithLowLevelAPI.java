package TestKlassenFuerQueries;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.RequestLine;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.*;

import org.json.JSONObject;
import org.json.*;


public class SearchWithLowLevelAPI {


   public static void main(String[] args){

       //getFirstResponse();
       //IndexStatus();
       //searchAuthor("Max Fischer");
       getWordsFrequencies("C7BVyGMBKRrm5z8MDDI8", "contents.contentString");
   }

   public static void getFirstResponse(){
       RestClient restClient = RestClient.builder(new HttpHost("192.168.178.240", 9200, "http")).build();

       //  Request request = new Request("GET", "/");
       //Response response = restClient.performRequest(request);

       Response response = null;
       try {
           response = restClient.performRequest("GET", "/");
       } catch (IOException e) {
           e.printStackTrace();
       }
       RequestLine requestLine = response.getRequestLine();
       HttpHost host = response.getHost();
       int statusCode = response.getStatusLine().getStatusCode();
       Header[] headers = response.getHeaders();


       System.out.println("Request" + response.toString());

       try {
           String responseBody = EntityUtils.toString(response.getEntity());
           System.out.println(responseBody);
       } catch (IOException e) {
           e.printStackTrace();
       }

       try {
           restClient.close();
       } catch (IOException e) {
           e.printStackTrace();
       }

   }

   public static void IndexStatus(){
       RestClient restClient = RestClient.builder(new HttpHost("192.168.178.240", 9200, "http")).build();


       Map<String, String> params = Collections.emptyMap();
       String jsonString = "{" +
               "\"user\":\"kimchy\"," +
               "\"postDate\":\"2013-01-30\"," +
               "\"message\":\"trying out Elasticsearch\"" +
               "}";
       HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
       try {
           Response response = restClient.performRequest("PUT", "/posts/doc/1", params, entity);

           String responseBody = EntityUtils.toString(response.getEntity());
           System.out.println(responseBody);

       } catch (IOException e) {
           e.printStackTrace();
       }

   }

   public static void getFirstDocumentAtIndex(){
       RestClient restClient = RestClient.builder(new HttpHost("192.168.178.240", 9200, "http")).build();

       Map<String, String> params = Collections.emptyMap();
       String jsonString = "{" +
               "\"author\":\"kimchy\"," +
               "\"postDate\":\"2013-01-30\"," +
               "\"message\":\"trying out Elasticsearch\"" +
               "}";
   }

   public static void searchAuthor(String author){
       RestClient restClient = RestClient.builder(new HttpHost("192.168.178.240", 9200, "http")).build();
       Map<String, String> params = Collections.emptyMap();
       String jsonString = "{" +
               "\"query\" : {" +
                    "\"term\" :{ \"user\" : " + "\"" + author + "\"" + "}" +
               "}" +
               "}";

       HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
       try {
           Response response = restClient.performRequest("GET", "/customer/_search", params, entity);

           String responseBody = EntityUtils.toString(response.getEntity());
           System.out.println(responseBody);



       } catch (IOException e) {
           e.printStackTrace();
       }
   }

    /**
     * Funktion, die Artikel-ID und ein Feld akzeptiert.
     * Diese Funktion gibt eine Map zurück, die ein Wort als Key und ein Integer-Array als Value enthält.
     * Das Array enthält: in wie vielen Dokumenten das Wort vorkommt,
     *                    wie oft das Wort insgesamt vorkommt und
     *                    wie oft das Wort im angegebenen Artikel(nach Artikel ID) vorkommt
     * @param id Zeitungsartikel ID
     * @param field field in JSON-Datei, in dem die Funktion die Wörter suche
     * @return wordFreq
     */
   public static HashMap<String, int[]> getWordsFrequencies(String id, String field){
       RestClient rc = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
       Map<String, String> params = Collections.emptyMap();
       String json = "{  \"ids\" : [\"" + id +"\"], " +
                        "\"parameters\": { " +
                            "\"fields\": [\""+ field +"\"], " +
                            "\"term_statistics\": true, " +
                            "\"offsets\":false, " +
                            "\"payloads\":false, " +
                            "\"positions\":false } }";
       HttpEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
       HashMap<String, int[]> wordFreq = new HashMap<String, int[]>();
       try{
           Response response = rc.performRequest("GET", "last/_doc/_mtermvectors?pretty", params, entity);
           String responseBody = EntityUtils.toString(response.getEntity());
           JSONObject terms = new JSONObject(responseBody)
                                    .getJSONArray("docs")
                                    .getJSONObject(0)
                                    .getJSONObject("term_vectors")
                                    .getJSONObject("contents.contentString")
                                    .getJSONObject("terms");
           //iterate over the JSONObject, which contains the word AND another JSONObject (the statistics)
           Iterator keys = terms.keys();
           while (keys.hasNext()){
               String key = (String)keys.next();
               JSONObject word = terms.getJSONObject(key);
               int[] wordStatistics = new int[3];
               wordStatistics[0] = word.getInt("doc_freq");     //in wie vielen Dokumenten das Wort vorkommt
               wordStatistics[1] = word.getInt("ttf");          //wie oft das Wort insgesamt vorkommt
               wordStatistics[2] = word.getInt("term_freq");    //wie oft das Wort im angegebenen Artikel vorkommt
               wordFreq.put(key, wordStatistics);
           }
           //comment this line below to disable printing the Statistics
           printWordFrequencies(wordFreq);
       } catch (IOException e){
           e.printStackTrace();
       }
       return wordFreq;
   }

    /**
     * Funktion für die Ausgabe der wordFreq map
     * @param wordFreq
     */
   public static void printWordFrequencies(HashMap<String, int[]> wordFreq){
       System.out.printf("%-30s    %-10s%-10s%-10s%n", "Word", "doc_freq", "ttf", "term_freq");
       System.out.println("----------------------------------------------------------------");
       for(String st : wordFreq.keySet()){
           System.out.printf("%-30s:   ", st);
           for(int i : wordFreq.get(st))
               System.out.printf("%-10s", i);
           System.out.println();
       }
       System.out.println("----------------------------------------------------------------");
       System.out.printf("%-30s    %-10s%-10s%-10s%n", "Word", "doc_freq", "ttf", "term_freq");
   }



}
