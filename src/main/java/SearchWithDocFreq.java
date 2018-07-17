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
           Map.Entry<String,Double> entry = iterator.next();
           ((BoolQueryBuilder) query).should(QueryBuilders.matchQuery("contents.contentString",entry.getKey()));
        }

        searchResponse                      = searchClient.getSearchResultFromResponse(query);
        SearchHits hits                     = searchResponse.getHits();
        SearchHit[] searchHits              = hits.getHits();
        List<result> results                = new ArrayList<result>();
        for (SearchHit hit : searchHits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String id                       = (String)sourceAsMap.get("id");
            Float score                     = hit.getScore();
            String title                    = (String)sourceAsMap.get("title");
            String author                   = (String)sourceAsMap.get("author");
            long pub_date                   = (Long)sourceAsMap.get("published_date");
            //erstelle für jede Ergebnis ein Objekt
            result att                      = new result(id, score, title, author, pub_date);
            if (results.isEmpty()) {
                results.add(att);
            } else {
                //list enthält Artikel mit gleichen title, author, und published_date?
                if(results.contains(att)){
                    int index               = results.indexOf(att);
                    if(results.get(index).getScore() < att.getScore()) {
                        //ersetzen der alte Eintrag mit der neue (höheren score)
                        results.set(index, att);
                    }
                } else {
                    results.add(att);
                }
            }
        }
        for (result r : results) {
            arrayList.add(new String[]{r.getId(), r.getScore().toString()});
            //System.out.println(r.getTitle());
        }
        return arrayList;
    }

    /**
     * Hilfsklasse, speichert Suchergebnisanfrage als einem Objekt
     */
    class result
    {
        private String id;
        private Float score;
        private String title;
        private String author;
        private long published_date;

        result(String id, Float score, String title, String author, long published_date) {
            this.id = id;
            this.score = score;
            this.title = title;
            this.author = author;
            this.published_date = published_date;
        }

        String getId() { return this.id; }
        String getTitle() { return this.title; }
        String getAuthor() { return this.author; }
        long getPublished_date() { return this.published_date; }
        Float getScore() { return this.score; }

        /**
         * prüfe, ob 2 Artikel gleich sind
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof result) {
                return this.title.equals(((result) obj).getTitle()) &&
                       this.author.equals(((result) obj).getAuthor()) &&
                       this.published_date == (((result) obj).getPublished_date());
            }
            return false;
        }
    }

}
