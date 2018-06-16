import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
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
        GetRequest getRequest = new GetRequest(index,"_doc",idDocument);
        GetResponse getResponse = client.get(getRequest);

        if (getResponse.isExists()) {
           // long version = getResponse.getVersion();
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

    public Map getArticelByWPID(String artikelID) throws IOException {
        SearchRequest searchRequest = new SearchRequest("last");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //Beim query muss Operator AND sein, sonst findet er zu viele Artikle
        searchSourceBuilder.query(matchQuery("id",artikelID).operator(Operator.AND));

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);

        SearchHit[] searchHits = searchResponse.getHits().getHits();

        String documentID = searchHits[0].getId();

        //ruft die getDocumentByIDIndex auf und gibt eine Map zurück. Vielleicht muss das noch geändert. Kommt
        //drauf an ob wir den Content innerhalb der Map weiter analysieren muessen.
        return getDocumentByIDIndex(documentID);

    }

    public void searchArticleByStringAndDate(String searchText, Long date) throws IOException {

        HashMap<String,Map> map= new HashMap<String,Map>();

        QueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("contents.contentString",searchText).operator(Operator.AND))
                .must(QueryBuilders.rangeQuery("published_date").lt(date));



        SearchResponse searchResponse = getSearchResultFromResponse(query);


        SearchHits hits = searchResponse.getHits();

        SearchHit[] searchHits = hits.getHits();

        for (SearchHit hit : searchHits) {
            String idDocument= hit.getId();

            map.put(idDocument,getDocumentByID(idDocument));
        }

    }

    /**
     * Methode nimmt ein QueryBuilder Objekt an und führt die Suche aus.
     * @param query Nimmt eine Objekt vom Typ QueryBuidler entgegen
     * @return Gibt ein SearchResponse mit den ensprechenden Hits zurueck
     */
    private SearchResponse getSearchResultFromResponse(QueryBuilder query){
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


    public Map getDocumentByID(String idOfDocument){

        GetRequest getRequest = new GetRequest("last", "_doc", idOfDocument);
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

                System.out.println(sourceAsMap.get("contents").toString());

                return sourceAsMap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }






    /**
     * Schliesst und beendet die Verbindung
     */
    public void CloseClient() throws IOException {
        client.close();


    }

}


