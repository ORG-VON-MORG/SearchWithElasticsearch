import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;
import edu.stanford.nlp.util.CoreMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SearchWithCoreNLP {
    /**
     * Methode nimmt ein QueryBuilder Objekt an und führt die Suche aus.
     * @param WAPOId Nimmt eine Artike-ID der Washington Post an
     * @return Gibt eine ArrayListe mit einem String[] zurueck. An der erste Stelle im Array steht die WAPO ID. An der
     * zweiten Stelle steht die Score.
     */
    public ArrayList<String[]> search(String WAPOId) {
        SearchClient searchClient = new SearchClient();
        Map documentSource = null;
        try {
            documentSource = searchClient.getArticleByWPID(WAPOId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Long published_date = (Long)documentSource.get("published_date");
        String title        = documentSource.get("title").toString();

        //Properties für das corenlp server konfigurieren
        Properties props    = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, coref");
        props.setProperty("coref.algorithm", "neural");

        //nohup java -mx4g -cp "*" edu.stanford.nlp.p ipeline.StanfordCoreNLPServer -port 9000 -timeout 15000 >/dev/null 2>&1 &
        StanfordCoreNLPClient pipeline  = new StanfordCoreNLPClient(props, "http://localhost", 9000, 4);
        Annotation document             = new Annotation(title);
        pipeline.annotate(document);            //run the document / title through the corenlp server
        String titleEntities = "";

        //get every sentences in title, then get every named entity in the sentence
        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap entityMention : sentence.get(CoreAnnotations.MentionsAnnotation.class)) {
                String m = entityMention.get(CoreAnnotations.TextAnnotation.class);
                titleEntities += m + " ";
                //System.out.println( m + ", ");      //delete this later, only for testing
            }
        }
        //System.out.println(titleEntities);
        return searchClient.searchArticleByStringAndDate(titleEntities, published_date);
    }
}