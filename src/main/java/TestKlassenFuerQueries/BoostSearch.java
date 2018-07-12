package TestKlassenFuerQueries;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static TestKlassenFuerQueries.SearchWithLowLevelAPI.getWordsFrequencies;

public class BoostSearch {



    public static void main(String[] args){
            new BoostSearch().start();
    }


    public void start() {

        HashMap<String,int[]> map = getWordsFrequencies("C7BVyGMBKRrm5z8MDDI8");
        this.getArticlesByBoostSearch(map,1374190070000L);

    }

       public void getArticlesByBoostSearch(HashMap<String, int[]> artikel, Long published_date) {
           RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
           SearchRequest searchRequest = new SearchRequest();

           SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
           final QueryBuilder query = QueryBuilders.boolQuery()
                  // .must(QueryBuilders.matchQuery("contents.contentString",title).boost(3))//operator(Operator.AND))
                   .must(QueryBuilders.rangeQuery("published_date").lt(published_date.toString()));





           //artikel.forEach((k,v)->((BoolQueryBuilder) query).must(QueryBuilders.matchQuery("contents.contentString", k).boost(v)));


           for (Map.Entry<String, int[]> entry : artikel.entrySet()) {

               ((BoolQueryBuilder) query).must(QueryBuilders.matchQuery("contents.contentString",entry.getKey())
                       .boost(entry.getValue()[1])      //0 for doc_freq, 1 for ttf, 2 for term_freq
                       .operator(Operator.AND));

           }

           try {
               SearchResponse searchResponse = client.search(searchRequest);
               System.out.println(searchResponse);
           } catch (IOException e) {
               e.printStackTrace();
           }


       }







}
