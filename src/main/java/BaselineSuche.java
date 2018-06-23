import edu.stanford.nlp.simple.*;
import edu.stanford.nlp.simple.Document;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BaselineSuche {

    public static void main (String[] args){

        Map mapWithAllRelevantArticle;
        Long published_date;
        Map map = null;
        SearchClient searchClient = new SearchClient();


        try {
            map = searchClient.getArticleByWPID("35f30c00-efdd-11e2-a1f9-ea873b7e0424");
        } catch (IOException e) {
            e.printStackTrace();
        }

        published_date = (Long) map.get("published_date");



        String title = map.get("title").toString();

        //-------miblau----------
        System.out.println(title);

        Document doc = new Document(title);

        String titleEntities = "";

        /*in dieser for-Schleife werden alle Entitaeten des title-Feldes ermittelt und in die titleEntities Variable gemacht*/
        for(Sentence sent: doc.sentences()){
            List<String> entityList = sent.mentions();
            for (String entity : entityList){
                titleEntities += entity +" ";
            }

        }
        System.out.println(titleEntities);
        //-------miblau-----------

        try {
            mapWithAllRelevantArticle= searchClient.searchArticleByStringAndDate(titleEntities,published_date);

            System.out.println("test");
        } catch (IOException e) {
            e.printStackTrace();
        }




        try {
            searchClient.closeClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
