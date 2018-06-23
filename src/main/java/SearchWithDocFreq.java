import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SearchWithDocFreq {

    public List search(String WAPOId){
        Map map;

        SearchClient searchClient = new SearchClient();
        try {
           map = searchClient.getArticelByWPID(WAPOId);
        } catch (IOException e) {
            e.printStackTrace();
        }




    }
}
