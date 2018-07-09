import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

public class SearchClient {

    private RestHighLevelClient client;
    private String index;
    private String ipAdresse;
    private int port;



    public SearchClient(String ipAdresse, int port, String index) {
        this.ipAdresse = ipAdresse;
        this.port = port;
        this.index = index;

        connectionClient(ipAdresse,port);

    }

    /**
     * Default Kontruktor. Der SearchClient wird mit den default Werten initialisiert.
     * IP-Adresse: locahost
     * Port: 9200
     * Index: last
     */
    public SearchClient(){
        this("localhost", 9200,"last");

    }

    private void connectionClient(String ipAdresse, int port){
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(ipAdresse, port, "http")));
    }


/**
 * Die Methode gibt ein Artikel aus dem Index zurück
 * @param idDocument Die ArtikelID ist die ID, welche vom Index vergeben wird. Nicht von WP.
 */

    public Map getDocumentByIDIndex(String idDocument) throws IOException {
        GetRequest getRequest;
        GetResponse getResponse;


        getRequest = new GetRequest(index,"_doc",idDocument);
        getResponse = client.get(getRequest);

        if (getResponse.isExists()) {
            String sourceAsString = getResponse.getSourceAsString();
            Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();

            return sourceAsMap;
        }

        return null;

    }


    /**
     * Methode nimmt die ID von einem Zeitungsartikel der Washington Post (WP) entgegen und gibt
     * eine Map mit den entsprechenden Feldern zurück.
     * @param artikelID Nimmt die ArtikelID der WP entgegen
     */

    public Map getArticleByWPID(String artikelID) throws IOException {
        SearchRequest searchRequest;
        SearchSourceBuilder searchSourceBuilder;
        SearchResponse searchResponse;
        SearchHit[] searchHits;
        String documentID;

        searchRequest = new SearchRequest("last");
        searchSourceBuilder = new SearchSourceBuilder();

        //Beim query muss Operator AND sein, sonst findet er zu viele Artikle
        searchSourceBuilder.query(matchQuery("id",artikelID).operator(Operator.AND));

        searchRequest.source(searchSourceBuilder);

        searchResponse = client.search(searchRequest);

        searchHits = searchResponse.getHits().getHits();

        documentID = searchHits[0].getId();

        //ruft die getDocumentByIDIndex auf und gibt eine Map zurück. Vielleicht muss das noch geändert. Kommt
        //drauf an ob wir den Content innerhalb der Map weiter analysieren muessen.
        return getDocumentByIDIndex(documentID);

    }

    public Map searchArticleByStringAndDate(String searchText, Long publishedDate) throws IOException {

        HashMap<String,Map> map;
        SearchResponse searchResponse;
        SearchHits hits;
        SearchHit[] searchHits;

        map = new HashMap<String,Map>();

        QueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("contents.contentString",searchText).operator(Operator.OR))
                .must(QueryBuilders.rangeQuery("published_date").lt(publishedDate));


        ((BoolQueryBuilder) query).mustNot(QueryBuilders.matchQuery("contents.kicker","Opionion" ));
        ((BoolQueryBuilder) query).mustNot(QueryBuilders.matchQuery("contents.kicker","Letters to the Editor" ));
        ((BoolQueryBuilder) query).mustNot(QueryBuilders.matchQuery("contents.kicker","Opionion" ));

        searchResponse = getSearchResultFromResponse(query);


        hits = searchResponse.getHits();

        searchHits = hits.getHits();

        for (SearchHit hit : searchHits) {
            String idDocument= hit.getId();

            map.put(idDocument,getDocumentByIDIndex(idDocument));
        }

        return map;

    }

    /**
     * Methode nimmt ein QueryBuilder Objekt an und führt die Suche aus.
     * @param query Nimmt eine Objekt vom Typ QueryBuilder entgegen
     * @return Gibt ein SearchResponse mit den ensprechenden Hits zurueck
     */
    public SearchResponse getSearchResultFromResponse(QueryBuilder query){
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(query);

        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = client.search(searchRequest);

            return searchResponse;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Schliesst und beendet die Verbindung
     */
    public void closeClient() throws IOException {
        client.close();
    }

}


