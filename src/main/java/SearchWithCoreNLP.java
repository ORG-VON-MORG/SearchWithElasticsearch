import edu.stanford.nlp.simple.*;
import edu.stanford.nlp.simple.Document;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class SearchWithCoreNLP {

    Map mapWithAllRelevantArticle;
    Long published_date;
    Map map = null;
    SearchClient searchClient = new SearchClient();

    public Map search(String artikelID) {

        try {
            map = searchClient.getArticelByWPID(artikelID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        published_date = (Long) map.get("published_date");
        String title = map.get("title").toString();

        Document doc = new Document(title);
        String titleEntities = "";

        for(Sentence sent: doc.sentences()){
            List<String> entityList = sent.mentions();
            for (String entity : entityList){
                titleEntities += entity +" ";
            }

        }

        try {
           return mapWithAllRelevantArticle = searchClient.searchArticleByStringAndDate(titleEntities,published_date);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            searchClient.closeClient();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
