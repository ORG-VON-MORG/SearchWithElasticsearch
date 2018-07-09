package TestKlassenFuerEntityRecognition;

import java.util.Scanner;
import java.io.*;
import edu.stanford.nlp.simple.*;
import org.json.JSONObject;
import org.json.*;

import javax.json.JsonValue;

/**
 * Diese Klasse zeigt ein kleines Beispiel zu StanfordCoreNLP: Hierbei wird ein konkreter WaPo-Artikel im JSON-Format
 * mittels der Scanner-Klasse eingelesen.
 * Anschließend werden alle dort gefundenen Entiäten auf der Standardausgabe ausgegeben
 */

public class CoreNLP {

    //-------------------------------------------------------------------------KLASSEN-KONSTANTEN
    private static final String DATEI_FALSCH = "Datei ist entweder nicht lesbar oder "
            + "keine normale Datei\n";


    //-------------------------------------------------------------------------

    public static void main(String[] args){
        new CoreNLP().start(args);
    }

    public void start(String[] args){
        try{
            for(int i = 0; i < args.length; i++) {
                ersteZeileZuJSON(args[i]);
            }
        }catch(Exception e) {
            System.out.println(e);
        }
    }


    public void ersteZeileZuJSON(String dateiname)throws java.io.FileNotFoundException, java.io.IOException{

        File datei = new File(dateiname).getCanonicalFile();
        Scanner input = new Scanner(datei);
        input.useDelimiter("\\n");
        check (!datei.isFile() || !datei.canRead(), DATEI_FALSCH);

        String ersteZeile = input.next();
        ersteZeile = input.next();
        ersteZeile = input.next();
        System.out.println(ersteZeile);

        JSONObject einZeitungsartikel = new JSONObject(ersteZeile);
       // System.out.println(einZeitungsartikel.toString());
        JSONArray contentsArray = einZeitungsartikel.getJSONArray("contents");
        JSONObject objektAusContentsArray;

        for (int i = 0; i < contentsArray.length(); i++){
            objektAusContentsArray = contentsArray.getJSONObject(i);

            if (objektAusContentsArray.has("contentString")){
                String saetze = objektAusContentsArray.get("contentString").toString();
                Document doc = new Document(saetze);

                for (Sentence sent : doc.sentences()) {
                    System.out.println("NER tags of the sentence in \"contentString\" nr: " + i +"-->" + sent.nerTags());
                    // ...
                    System.out.println("mentions of the sentence in \"contentString\" nr: " + i +"-->" +  sent.mentions());
                    System.out.println("");
                }
            }
        }
    }

    private void check (boolean bedingung, String msg) {
        if (bedingung) {
            throw new RuntimeException(msg);
        }
    }


    public static void beispielMethodeCoreNLP (){
        // Create a document. No computation is done yet.

        String zeitungstext = "Kate Spade’s husband, Andy, said Wednesday that the fashion designer was being treated for "+
                "depression and anxiety in the years leading up to her death by apparent suicide this week. "+
                "“Kate suffered from depression and anxiety for many years,” Andy Spade said in a statement that was "+
                "published in full by the New York Times. “She was actively seeking help and working closely with her "+
                "doctors to treat her disease, one that takes far too many lives.”";

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
