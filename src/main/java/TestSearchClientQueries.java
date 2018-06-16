import java.io.IOException;
import java.util.Map;

public class TestSearchClientQueries{


        public static void main(String[] args){

            new TestSearchClientQueries().start();
        }

        public void start(){

            getRelevantArticel();

        }

        private void getRelevantArticel(){
            SearchClient sc = new SearchClient();
            String artikelID ="35f30c00-efdd-11e2-a1f9-ea873b7e0424";

            Map map = null;
            try {
                map = sc.getArticelByWPID(artikelID);
            } catch (IOException e) {
                e.printStackTrace();
            }


            System.out.println(map);

            try {
                sc.closeClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}
