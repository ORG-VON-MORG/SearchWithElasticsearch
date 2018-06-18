package TestKlassenFuerQueries;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.collect.HppcMaps;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class BoostSearch {

    RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    SearchRequest searchRequest = new SearchRequest();

    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    QueryBuilder query = QueryBuilders.boolQuery()
            .must(QueryBuilders.matchQuery("contents.contentString",title).boost(3))//operator(Operator.AND))
            .must(QueryBuilders.rangeQuery("published_date").lt(published_date.toString()));

       public void check(HashMap<String, int> artikel) {
           for (Map.Entry<String, HppcMaps.Object.Integer> entry : map.entrySet()) {
               System.out.println(entry.getKey() + "/" + entry.getValue());
           }
       }







}
