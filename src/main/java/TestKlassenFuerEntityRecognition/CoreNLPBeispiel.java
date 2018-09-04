package TestKlassenFuerEntityRecognition;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

/**
 * Diese Klasse demonstriert ein Anfangs-Beispiel für StanfordCoreNLP.
 * Es wird ein einfacher String verwendet, aus dem die Entitäten extrahiert und auf der Standardausgabe ausgegeben werden
 */
public class CoreNLPBeispiel {

    public static void main(String[] args) {
        // Create a document. No computation is done yet.

        String zeitungstext = "What could possibly go wrong when Trump goes to Texas?";

        Document doc = new Document(zeitungstext);

        for (Sentence sent : doc.sentences()) {  // Will iterate over two sentences
            // We're only asking for words -- no need to load any models yet
            //System.out.println("The second word of the sentence '" + sent + "' is " + sent.word(1));
            // When we ask for the lemma, it will load and run the part of speech tagger
            //  System.out.println("The second lemma of the sentence '" + sent + "' is " + sent.lemma(1));
            // When we ask for the parse, it will load and run the parser
            System.out.println("The NER tags of the sentence are " + sent.nerTags());
            // ...
            System.out.println("The mentions of the sentence are " + sent.mentions());


            //JSONObject contentString = get("contentString");
        }

    }

}
