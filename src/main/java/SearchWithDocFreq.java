import Util.util;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.io.IOException;
import java.util.*;

import static TestKlassenFuerQueries.SearchWithLowLevelAPI.getWordsFrequencies;

public class SearchWithDocFreq {

    public List search(String WAPOId){
        Map map = null;
        SearchClient searchClient;
        QueryBuilder query;
        Long  published_date = null;
        HashMap<String, Double> idf;
        HashMap<String, Double> tmp;


        searchClient = new SearchClient();
        try {
           map = searchClient.getArticelByWPID(WAPOId);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Holt Published Date vom Artikel
        published_date = (Long) map.get("published_date");

        idf = util.calculateIDF(WAPOId);

        tmp = util.sortedMap(idf);

        query = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("published_date").lt(published_date.toString()));


        return null;

    }



}
