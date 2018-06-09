package TestKlassenFuerQueries;

import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.profile.ProfileShardResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.*;


public class SearchWithHighLevelClient {

    RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(new HttpHost("localhost", 9200, "http")));





    public static void main(String args[]){

       SearchWithHighLevelClient searchClient = new SearchWithHighLevelClient();
        //searchClient.getDocumentByID("j6qkx2MBKRrm5z8MDDOZ");

        try {
           // searchClient.searchAuthorWithProfiling("Evan Soltas");
           // searchClient.searchArticleContent("Gottschalk");
            //searchClient.getArticelByWPID("35f30c00-efdd-11e2-a1f9-ea873b7e0424");
            searchClient.getAllRelevant("35f30c00-efdd-11e2-a1f9-ea873b7e0424");
        } catch (IOException e) {
            e.printStackTrace();
        }

      //  searchClient.getArticleByDate();

        //searchClient.getAllArticle();

        // searchClient.getAllArticleSearchScroll();



    }

    public Map getDocumentByID(String idOfDocument){

        GetRequest getRequest = new GetRequest("last", "_doc", idOfDocument);
        try {
            GetResponse getResponse = client.get(getRequest);

            //String index = getResponse.getIndex();
            //String type = getResponse.getType();
            //String id = getResponse.getId();

            if (getResponse.isExists()) {
                long version = getResponse.getVersion();
                String sourceAsString = getResponse.getSourceAsString();
                Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
                byte[] sourceAsBytes = getResponse.getSourceAsBytes();

                System.out.println(sourceAsMap.get("contents").toString());

                return sourceAsMap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    public void searchAuthorWithProfiling(String author) throws IOException {


        //Search Request
        SearchRequest searchRequest = new SearchRequest();

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //sourceBuilder.query(QueryBuilders.termQuery("author", "Max Fischer"));
        //sourceBuilder.query(matchQuery("author", author));


        //TODO Test ob es mit Term Query funktioniert
        sourceBuilder.query(termQuery("author", author));

        //sourceBuilder.from(0);
        //sourceBuilder.size(5);
        //sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //Profiling muss aktiviert sein
        sourceBuilder.profile(true);
        searchRequest.source(sourceBuilder);

        System.out.println(searchRequest.toString());

        //Synchronous Execution
        //Aynchronous Excution ebenfalls möglich


        SearchResponse searchResponse = client.search(searchRequest);

        SearchHits hits = searchResponse.getHits();


        SearchHit[] searchHits = hits.getHits();
        System.out.println("Es wurden " + hits.getTotalHits() + " Einträge gefunden");

            for (SearchHit hit : searchHits) {
                System.out.println(hit.getId());
                System.out.println(hit.docId());

                String sourceAsString = hit.getSourceAsString();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                String documentTitle = (String) sourceAsMap.get("title");


                List<Object> users = (List<Object>) sourceAsMap.get("user");
                Map<String, Object> innerObject = (Map<String, Object>) sourceAsMap.get("innerObject");


                System.out.println(documentTitle);
                System.out.println(searchHits[1].docId());
            }


            //Retrieving Profiling Results

            Map<String, ProfileShardResult> profilingResults = searchResponse.getProfileResults();
            for (Map.Entry<String, ProfileShardResult> profilingResult : profilingResults.entrySet()) {
                String key = profilingResult.getKey();
                ProfileShardResult profileShardResult = profilingResult.getValue();
            }


            System.out.println("test");


        }

    public void getAllArticle(){



        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        //TODO Per Default werden nur 10 Hits angezeigt. Wie kann man das Ändern. searchSourceBuilder.size(int size) kann ändern.
        searchSourceBuilder.size(10000);


        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = client.search(searchRequest);
            System.out.println("test");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getAllArticleSearchScroll() throws IOException {



        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.scroll(scroll);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(matchQuery("author", "Max Fisher"));
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        while (searchHits != null && searchHits.length > 0) {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = client.searchScroll(scrollRequest);
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();

        }

        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest);
        boolean succeeded = clearScrollResponse.isSucceeded();

        System.out.println("test");


        client.close();

    }

    public void getArticleByDate(){


        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        searchSourceBuilder.query(matchAllQuery());
        //searchSourceBuilder.query(rangeQuery("published_date").from(1514807916000L).to(1517270400000L));
        searchSourceBuilder.query(rangeQuery("published_date").lte("1514807916000"));



        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest);
            System.out.println("test");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void searchArticleContent(String text) throws IOException {
        SearchRequest searchRequest = new SearchRequest("last");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //searchSourceBuilder.query(matchQuery("contents", text));
        searchSourceBuilder.query(matchQuery("contents.contentString",text));

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);


        System.out.println("ttest");



    }

    public Map getArticelByWPID(String artikelID) throws IOException {
        SearchRequest searchRequest = new SearchRequest("last");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //searchSourceBuilder.query(termQuery("id",ArtikelID));



        searchSourceBuilder.query(matchQuery("id",artikelID).operator(Operator.AND));


        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);

        SearchHit[] searchHits = searchResponse.getHits().getHits();

        String documentID = searchHits[0].getId();


         return getDocumentByID(documentID);

    }


    public void getAllRelevant(String artikelID) throws IOException {
        Map artikelMap = getArticelByWPID(artikelID);

        String title = (String)artikelMap.get("title");

        Long published_date = (Long) artikelMap.get("published_date");


        searchArticleContent(title);

        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(matchQuery("contents.contentString",title));
        searchSourceBuilder.query(rangeQuery("published_date").lte(published_date.toString()));
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);








        System.out.println("test");









    }



    private Map convertSearchHitsIntoMapWithAllArticles(SearchHit[] searchHits){


        return null;
    }




}
