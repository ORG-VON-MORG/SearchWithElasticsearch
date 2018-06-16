import java.io.IOException;
import java.util.Map;

public class BaselineSuche {

    public static void main (String[] args){

        SearchClient searchClient = new SearchClient();

        Map map = null;

        try {
            map = searchClient.getArticelByWPID("35f30c00-efdd-11e2-a1f9-ea873b7e0424");
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("test");
        //String title = map.get("title");

    }
}
