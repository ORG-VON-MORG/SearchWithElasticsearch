package TestKlassenFuerStatistik;

import org.apache.http.HttpHost;
import org.elasticsearch.action.termvectors.TermVectorsRequest;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

public class VectorTerm {

    public static void main(String[] args) {

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));

        //     TermVectorsRequest termVectorsRequest = new TermVectorsRequest("last","_doc","C7BVyGMBKRrm5z8MDDI8");
        //   termVectorsRequest.fieldStatistics(true);
        // termVectorsRequest.termStatistics(true);


        TermVectorsResponse termVectorsResponse = new TermVectorsResponse("last", "_doc", "C7BVyGMBKRrm5z8MDDI8");


        TermVectorsRequest request = new TermVectorsRequest().index("TWITTER").type("TWEET").id("1")
                .offsets(true)
                .payloads(true)
                .positions(true)
                .termStatistics(true)
                .fieldStatistics(true)
                .selectedFields(new String[]{"text"});


        try {
            System.out.println(termVectorsResponse.getFields());

            System.out.println(termVectorsResponse);

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("twest");


        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}