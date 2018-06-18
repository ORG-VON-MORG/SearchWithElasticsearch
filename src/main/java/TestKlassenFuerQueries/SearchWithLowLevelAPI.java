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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONObject;
import org.json.*;


public class SearchWithLowLevelAPI {


   public static void main(String[] args){

       //getFirstResponse();
       //IndexStatus();
       //searchAuthor("Max Fischer");
       showTerms("C7BVyGMBKRrm5z8MDDI8");
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

   public static HashMap<String, Integer> showTerms(String id){
       RestClient rc = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
       Map<String, String> params = Collections.emptyMap();
       String json = "{ \"ids\" : [\"" + id +"\"], \"parameters\": { \"fields\": [\"contents.contentString\"], \"term_statistics\": true, \"offsets\":false, \"payloads\":false, \"positions\":false } }";
       HttpEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
       HashMap<String, Integer> wordFreq = new HashMap<String, Integer>();
       try{
           Response rsp = rc.performRequest("GET", "last/_doc/_mtermvectors?pretty", params, entity);
           String responsebody = EntityUtils.toString(rsp.getEntity());
           JSONObject terms = new JSONObject(responsebody)
                                    .getJSONArray("docs")
                                    .getJSONObject(0)
                                    .getJSONObject("term_vectors")
                                    .getJSONObject("contents.contentString")
                                    .getJSONObject("terms");
           System.out.println(terms);

           Iterator keys = terms.keys();
           while (keys.hasNext()){
               String key = (String)keys.next();
               JSONObject word = terms.getJSONObject(key);
               Integer freq = word.getInt("ttf");
               wordFreq.put(key, freq);
           }
           for( String st : wordFreq.keySet()) {
               System.out.println(st + ": " + wordFreq.get(st));
           }





       } catch (IOException e){
           e.printStackTrace();
       }
       return wordFreq;
   }



}
