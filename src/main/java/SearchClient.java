import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.Map;

public class SearchClient {

    private RestHighLevelClient client;
    private String index;


    public SearchClient(String ipAdresse, int port, String index) {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(ipAdresse, port, "http")));
        this.index=index;
    }


    public Map getDocumentByID(String idDocument) throws IOException {
        GetRequest getRequest = new GetRequest(index,"_doc",idDocument);
        GetResponse getResponse = client.get(getRequest);

        if (getResponse.isExists()) {
            long version = getResponse.getVersion();
            String sourceAsString = getResponse.getSourceAsString();
            Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
            byte[] sourceAsBytes = getResponse.getSourceAsBytes();

            return sourceAsMap;
        } else {
            return null;
        }



    }






    public void CloseClient() throws IOException {
        client.close();


    }
}
