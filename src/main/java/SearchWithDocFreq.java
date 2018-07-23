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
        SearchClient searchClient       = new SearchClient();
        Map documentSource              = null;
        List<Map.Entry<String, Double>> idf;
        HashMap<String, Double> tmp;
        final int MAX_KEYWORDS_IN_QUERY = 20;

        try {
           documentSource = searchClient.getArticleByWPID(WAPOId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Holt Published Date vom Artikel
        Long published_date = (Long)documentSource.get("published_date");

        QueryBuilder query = QueryBuilders.boolQuery()
               // .must(QueryBuilders.rangeQuery("published_date").lt(published_date.toString()));
               //.must(QueryBuilders.rangeQuery("published_date").from(published_date - 31556952000L).to((published_date)));
                 .must(QueryBuilders.rangeQuery("published_date").from(published_date - 94670856000L).to((published_date -1L)));



        ((BoolQueryBuilder) query).mustNot(QueryBuilders.matchQuery("contents.kicker","Opionion" ));
        ((BoolQueryBuilder) query).mustNot(QueryBuilders.matchQuery("contents.kicker","Letters to the Editor" ));
        ((BoolQueryBuilder) query).mustNot(QueryBuilders.matchQuery("contents.kicker","Opionion" ));

        tmp = util.calculateIDF(WAPOId);
        idf = util.sortedMap(tmp);

        Iterator <Map.Entry<String, Double>> iterator = idf.iterator();
        for(int i = 0;i<=MAX_KEYWORDS_IN_QUERY;i++){
           Map.Entry<String,Double> entry   = iterator.next();
           ((BoolQueryBuilder) query).should(QueryBuilders.matchQuery("contents.contentString",entry.getKey()));
        }

        SearchResponse searchResponse       = searchClient.getSearchResultFromResponse(query);
        SearchHits hits                     = searchResponse.getHits();
        SearchHit[] searchHits              = hits.getHits();
        int size = searchHits.length;

        return util.filterDuplicateResults(published_date, searchHits);

    }

}
