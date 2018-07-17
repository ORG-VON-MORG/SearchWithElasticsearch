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
     * @return Gibt eine ArrayListe mit einem String[] zurueck. An der erste Stelle im Array steht die WAPO ID. An der
     * zweiten Stelle steht die Score.
     */
    public ArrayList<String[]> search(String WAPOId){
        Map map = null;
        SearchClient searchClient;
        QueryBuilder query;
        Long  published_date;
        List<Map.Entry<String, Double>> idf;
        HashMap<String, Double> tmp;
        SearchResponse searchResponse;
        ArrayList<String[]> arrayList = new ArrayList<String[]>();
        final int MAX_KEYWORDS_IN_QUERY = 5;


        searchClient = new SearchClient();      //start the client from SearchClient.java
        try {
           map = searchClient.getArticleByWPID(WAPOId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Holt Published Date vom Artikel
        published_date = (Long) map.get("published_date");

        query = QueryBuilders.boolQuery()
               .must(QueryBuilders.rangeQuery("published_date").lt(published_date.toString()));
               //.must(QueryBuilders.rangeQuery("published_date").from(published_date - 31556952000L).to((published_date)));
                // .must(QueryBuilders.rangeQuery("published_date").from(published_date - 94670856000L).to((published_date -1L)));



        ((BoolQueryBuilder) query).mustNot(QueryBuilders.matchQuery("contents.kicker","Opionion" ));
        ((BoolQueryBuilder) query).mustNot(QueryBuilders.matchQuery("contents.kicker","Letters to the Editor" ));
        ((BoolQueryBuilder) query).mustNot(QueryBuilders.matchQuery("contents.kicker","Opionion" ));

        tmp = util.calculateIDF(WAPOId);
        idf = util.sortedMap(tmp);

        Iterator <Map.Entry<String, Double>> iterator = idf.iterator();
        for(int i = 0;i<=MAX_KEYWORDS_IN_QUERY;i++){
           Map.Entry<String,Double> entry = iterator.next();
           ((BoolQueryBuilder) query).should(QueryBuilders.matchQuery("contents.contentString",entry.getKey()));
        }

        searchResponse = searchClient.getSearchResultFromResponse(query);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String[] stringarray = new String[2];
            //Konvertiert float score zu einem String
            String score = Float.toString(hit.getScore());


            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            stringarray[0] = (String) sourceAsMap.get("id");
            stringarray[1] = score;


            arrayList.add(stringarray);
        }

        return arrayList;

    }



}
