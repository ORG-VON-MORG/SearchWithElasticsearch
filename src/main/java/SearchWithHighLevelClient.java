import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.profile.ProfileShardResult;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SearchWithHighLevelClient {

    public static void main(String args[]){

        SearchWithHighLevelClient searchClient = new SearchWithHighLevelClient();
        searchClient.getDocumentByID("z-wemGMB4VpJCKChc3eB");
        searchClient.searchAuthor("Evan Soltas");
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

    public void searchAuthor(String author){

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.178.240", 9200, "http")));

        //Search Request
        SearchRequest searchRequest = new SearchRequest();

        //
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();


        //sourceBuilder.query(QueryBuilders.termQuery("author", "Max Fischer"));
        sourceBuilder.query(QueryBuilders.matchQuery("author", "Max Fischer"));


        //sourceBuilder.from(0);
        //sourceBuilder.size(5);
        //sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        searchRequest.source(sourceBuilder);

        System.out.println(searchRequest.toString());

        //Synchronous Execution
        //Aynchronous Excution ebenfalls möglich

        try {
            SearchResponse searchResponse = client.search(searchRequest);




            SearchHits hits = searchResponse.getHits();

            SearchHit[] searchHits = hits.getHits();

            for (SearchHit hit : searchHits) {
                System.out.println(hit.getId());
                System.out.println(hit.docId());

            }



            System.out.println("test");

        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
