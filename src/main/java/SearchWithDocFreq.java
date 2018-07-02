import Util.util;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.util.*;

import static TestKlassenFuerQueries.SearchWithLowLevelAPI.closeClient;
import static TestKlassenFuerQueries.SearchWithLowLevelAPI.startClient;

public class SearchWithDocFreq {

    /**
     * Methode nimmt ein QueryBuilder Objekt an und führt die Suche aus.
     * @param WAPOId Nimmt eine Artike-ID der Washington Post an
     * @return Gibt eine ArrayListe mit den gefunden WAPOId's der Artikel zurueck
     */
    public ArrayList search(String WAPOId){
        Map map = null;
        SearchClient searchClient;
        QueryBuilder query;
        Long  published_date = null;
        List<Map.Entry<String, Double>> idf;
        HashMap<String, Double> tmp;
        SearchResponse searchResponse;
        ArrayList<String> arrayList = new ArrayList<String>();
        final int MAX_KEYWORDS_IN_QUERY = 5;


        searchClient = new SearchClient();      //start the client from SearchClient.java
        startClient();                          //start the client from SearchWithLowLevelAPI.java
        try {
           map = searchClient.getArticleByWPID(WAPOId);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Holt Published Date vom Artikel
        published_date = (Long) map.get("published_date");


        query = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("published_date").lt(published_date.toString()));


        tmp = util.calculateIDF(WAPOId);
        idf = util.sortedMap(tmp);

        System.out.println("test");

       Iterator <Map.Entry<String, Double>> iterator = idf.iterator();


        for(int i = 0;i<=MAX_KEYWORDS_IN_QUERY;i++){
           Map.Entry<String,Double> entry = iterator.next();
           ((BoolQueryBuilder) query).must(QueryBuilders.matchQuery("contents.contentString",entry.getKey()));


        }

        searchResponse = searchClient.getSearchResultFromResponse(query);


        SearchHits hits = searchResponse.getHits();


        SearchHit[] searchHits = hits.getHits();

        for (SearchHit hit : searchHits) {
            System.out.println(hit.getId());
            System.out.println(hit.docId());

            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String WAPOIDofHit = (String) sourceAsMap.get("id");



            arrayList.add(WAPOIDofHit);


            try {
                searchClient.closeClient();     //close the client from SearchClient.java
            } catch (IOException e) {
                e.printStackTrace();
            }
            closeClient();                  //close the client from SearchWithLowLevelAPI.java
                                            //maybe later combine the two into single class(?)

            return arrayList;

        }

        return null;

    }



}
