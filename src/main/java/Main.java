import java.util.*;

/**
 * Diese Klasse soll zu den von TREC gegebenen Topics die entsprechenden Anfrage-Ergebnisse ermitteln.
 * Diese Ergebnisse sollen in eine Datei geschrieben werden, diese Datei ist im TREC-Output-Format.
 *
 * In dieser Klasse sollen alle Anfrage-Arten ausgeführt werden. Für jede Anfrage-Art wird eine eigene Datei erstellt.
 *
 * @author Michelle Blau
 * @version 09.07.2018
 */

public class Main {

    //WICHTIG: DIESE METHODE BETRACHTET NUR EIN TOPIC VON TREC, JEDOCH MÜSSEN WIR MEHRERE BETRACHTEN
    private void start(){
        //TODO: Methode bauen, die aus den gegebenen Topics die topicIDs und die wapoArtikelIDs extrahiert. Die wapoArtikelID ist von dem Artikel, für den Kontextinformationen gesucht werden.
        //Die Topics von TREC sind im XML-Format
        String wapoArtikelID = "34d4708d7cce27237b991c02c98eeeb5";
        int topicID = 1;

        //erzeugeDateiFuerDoqFreqAnfrage(wapoArtikelID, topicID);
        erzeugeDateiFuerCoreNLPAnfrage(wapoArtikelID, topicID);

    }


    /**
     * Erstellt für einen gegebenen Artikel eine Datei "blablablaVerzeichnisblabla", die die DoqFreq-Anfrage-Ergebnisse
     * im TREC-Output-Format enthält
     *
     * @param zuSuchenderArtikel wapoArtikelID, für die die Kontextsuche ausgeführt wird
     * @param topicID TopicID von TREC
     */
    private void erzeugeDateiFuerDoqFreqAnfrage(String zuSuchenderArtikel, int topicID){
        SearchWithDocFreq searchWithDocFreq;
        ArrayList<String> ergebnisListe;
        searchWithDocFreq = new SearchWithDocFreq();
        ergebnisListe = searchWithDocFreq.search(zuSuchenderArtikel);
        //TODO: score muss auch zurückgeliefert werden
        System.out.println("Gefundene Artikel:");
        System.out.println(ergebnisListe);

    }


    /**
     * Erstellt für einen gegebenen Artikel eine Datei "blablablaVerzeichnisblabla", die die CoreNLP-Anfrage-Ergebnisse
     * im TREC-Output-Format enthält
     *
     * @param zuSuchenderArtikel wapoArtikelID, für die die Kontextsuche ausgeführt wird
     * @param topicID TopicID von TREC
     */
    private void erzeugeDateiFuerCoreNLPAnfrage(String zuSuchenderArtikel, int topicID){
        SearchWithCoreNLP searchWithCoreNLP = new SearchWithCoreNLP();
        searchWithCoreNLP.startClient();

        Map ausgabe = searchWithCoreNLP.search(zuSuchenderArtikel);
        Set ergebnisMenge = ausgabe.keySet();
        //List ergebnisListe = Arrays.asList(ergebnisMenge.toArray());

        //TODO: score muss auch zurückgeliefert werden
        System.out.println("Gefundene Artikel:");
        System.out.println(ausgabe);
        System.out.println(ausgabe.keySet());

        searchWithCoreNLP.closeClient();
    }

    public static void main(String[] args){
        new Main().start();
    }
}
