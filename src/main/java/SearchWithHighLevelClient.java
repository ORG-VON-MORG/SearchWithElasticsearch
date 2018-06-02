import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.profile.ProfileShardResult;
import org.elasticsearch.search.profile.query.QueryProfileShardResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;


public class SearchWithHighLevelClient {

    public static void main(String args[]){

        SearchWithHighLevelClient searchClient = new SearchWithHighLevelClient();
        //searchClient.getDocumentByID("z-wemGMB4VpJCKChc3eB");
        try {
            searchClient.searchAuthorWithProfiling("Evan Soltas");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //searchClient.getAllArticle();

        // searchClient.getAllArticleSearchScroll();


    }

    public void getDocumentByID(String idOfDocument){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.178.240", 9200, "http")));

        GetRequest getRequest = new GetRequest("customer", "_doc", idOfDocument);
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
            } else {
                System.out.println("Dokument existiert nicht");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void searchAuthorWithProfiling(String author) throws IOException {

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.178.240", 9200, "http")));



        //Search Request
        SearchRequest searchRequest = new SearchRequest();

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //sourceBuilder.query(QueryBuilders.termQuery("author", "Max Fischer"));
        sourceBuilder.query(matchQuery("author", author));


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

    public void searchAuthor(String author) throws IOException{
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.178.240", 9200, "http")));



    }




    public void getAllArticle(){

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.178.240", 9200, "http")));

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

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.178.240", 9200, "http")));


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

    }
}
