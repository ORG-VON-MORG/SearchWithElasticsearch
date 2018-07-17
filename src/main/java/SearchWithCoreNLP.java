import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;
import edu.stanford.nlp.util.CoreMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

public class SearchWithCoreNLP {

    Map mapWithAllRelevantArticle;
    Long published_date;
    Map map = null;
    SearchClient searchClient;

    //public SearchWithCoreNLP() {
    //    searchClient = new SearchClient();
    //}

    public void startClient() {
        searchClient = new SearchClient();
    }

    /**
     * Methode nimmt ein QueryBuilder Objekt an und führt die Suche aus.
     * @param WAPOId Nimmt eine Artike-ID der Washington Post an
     * @return Gibt eine ArrayListe mit einem String[] zurueck. An der erste Stelle im Array steht die WAPO ID. An der
     * zweiten Stelle steht die Score.
     */
    public ArrayList<String[]> search(String WAPOId) {
        ArrayList<String[]> arrayList = new ArrayList<String[]>();

        try {
            map = searchClient.getArticleByWPID(WAPOId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        published_date = (Long) map.get("published_date");
        String title = map.get("title").toString();

        //Properties für das corenlp server konfigurieren
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, coref");
        props.setProperty("coref.algorithm", "neural");

        /**
         * --!update ihre PuTTY tunnel konfiguration, add new forwarded port:
         *      Source port: 9000
         *      Destination: localhost:9000
         * (später vielleicht auf 134.96.217.37 adresse?)
         *
         * das corenlp server liegt momentan auf spiri home verzeichnis ~/spiri/stanford-corenlp-full-2018-02-27
         * (besser später auf anderem Verzeichnis verschoben werden? und vom admin gestartet werden)
         * das Server (falls noch nicht gestartet werden) kann mit dem Kommando:
         * nohup java -mx4g -cp "*" edu.stanford.nlp.p ipeline.StanfordCoreNLPServer -port 9000 -timeout 15000 >/dev/null 2>&1 &
         * gestartet werden
         *
         * Überprüfen, ob das Server auf dem VM im hintergrunc läuft oder nicht:
         * netstat -atnu | grep -E ".*9000.*LISTEN"
         * ps aux | grep stanford
         */
        StanfordCoreNLPClient pipeline = new StanfordCoreNLPClient(props, "http://localhost", 9000, 4);
        Annotation document = new Annotation(title);
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
        try {
            return arrayList = searchClient.searchArticleByStringAndDate(titleEntities, published_date);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}