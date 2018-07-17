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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

public class SearchClient {

    private RestHighLevelClient client;
    private String index;
    private String ipAdresse;
    private int port;

    //Gibt die Anzahl der Ergebnis im Ergebnis Array an
    private final int sizeResult =100;

    public SearchClient(String ipAdresse, int port, String index) {
        this.ipAdresse = ipAdresse;
        this.port = port;
        this.index = index;

    }

    /**
     * Default Konstruktor. Der SearchClient wird mit den default Werten initialisiert.
     * IP-Adresse: localhost
     * Port: 9200
     * Index: last
     */
    public SearchClient(){
        this("localhost", 9200,"final");
    }

    private void startClient(){
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(ipAdresse, port, "http")));
    }

    /**
     * Schliesst und beendet die Verbindung
     */
    public void closeClient() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

/**
 * Die Methode gibt ein Artikel aus dem Index zurück
 * @param idDocument Die ArtikelID ist die ID, welche vom Index vergeben wird. Nicht von WP.
 */

    public Map getDocumentByIDIndex(String idDocument) throws IOException {
        startClient();
        GetRequest getRequest;
        GetResponse getResponse;


        getRequest = new GetRequest(index,"_doc",idDocument);
        getResponse = client.get(getRequest);
        Map<String, Object> sourceAsMap = null;

        if (getResponse.isExists()) {
            String sourceAsString = getResponse.getSourceAsString();
            sourceAsMap = getResponse.getSourceAsMap();
        }

        closeClient();
        return sourceAsMap;

    }


    /**
     * Methode nimmt die ID von einem Zeitungsartikel der Washington Post (WP) entgegen und gibt
     * eine Map mit den entsprechenden Feldern zurück.
     * @param artikelID Nimmt die ArtikelID der WP entgegen
     */

    public Map getArticleByWPID(String artikelID) throws IOException {
        startClient();
        SearchRequest searchRequest;
        SearchSourceBuilder searchSourceBuilder;
        SearchResponse searchResponse;
        SearchHit[] searchHits;
        String documentID;

        searchRequest = new SearchRequest("final");
        searchRequest.types("_doc");
        searchSourceBuilder = new SearchSourceBuilder();

        //Beim query muss Operator AND sein, sonst findet er zu viele Artikle
        searchSourceBuilder.query(matchQuery("id",artikelID).operator(Operator.AND));
        searchRequest.source(searchSourceBuilder);
        searchResponse = client.search(searchRequest);
        searchHits = searchResponse.getHits().getHits();
        documentID = searchHits[0].getId();

        closeClient();
        //ruft die getDocumentByIDIndex auf und gibt eine Map zurück. Vielleicht muss das noch geändert. Kommt
        //drauf an ob wir den Content innerhalb der Map weiter analysieren muessen.
        return getDocumentByIDIndex(documentID);

    }

    public ArrayList<String[]> searchArticleByStringAndDate(String searchText, Long publishedDate) throws IOException {
        ArrayList<String[]> arrayList = new ArrayList<String[]>();
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
            String[] stringarray = new String[2];

            //Konvertiert float score zu einem String
            String score = Float.toString(hit.getScore());

            //----------ALTER CODE-------------
            //Kann geloscht werden, wenn alles funktioniert
            //String elasticIdDocument= hit.getId();
           // Map<String, Object> document = getDocumentByIDIndex(elasticIdDocument);
            //String artikelId = (String)document.get("id");
            //map.put(artikelId, document);

            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            stringarray[0] = (String) sourceAsMap.get("id");
            stringarray[1] = score;


            arrayList.add(stringarray);
        }

        return arrayList;

    }

    /**
     * Methode nimmt ein QueryBuilder Objekt an und führt die Suche aus.
     * @param query Nimmt eine Objekt vom Typ QueryBuidler entgegen
     * @return Gibt ein SearchResponse mit den ensprechenden Hits zurueck
     */
    public SearchResponse getSearchResultFromResponse(QueryBuilder query){
        startClient();
        SearchRequest searchRequest = new SearchRequest("final");
        searchRequest.types("_doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //Anzahl der Hit in der Map
        searchSourceBuilder.size(sizeResult);
        searchSourceBuilder.query(query);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;

        try {
            searchResponse = client.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeClient();
        return searchResponse;

    }

}


