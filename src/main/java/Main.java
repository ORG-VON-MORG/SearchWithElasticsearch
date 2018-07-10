import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.IOException;

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
    private void start() throws IOException{
        //TODO: Methode bauen, die aus den gegebenen Topics die topicIDs und die wapoArtikelIDs extrahiert. Die wapoArtikelID ist von dem Artikel, für den Kontextinformationen gesucht werden.
        //Die Topics von TREC sind im XML-Format
        String wapoArtikelID = "34d4708d7cce27237b991c02c98eeeb5";
        int topicID = 1;
        String filepath  = "...";

        erzeugeDateiFuerDoqFreqAnfrage(wapoArtikelID, topicID);
        erzeugeDateiFuerCoreNLPAnfrage(wapoArtikelID, topicID);
        readfromXML(filepath);

    }


    /**
     * Erstellt für einen gegebenen Artikel eine Datei "blablablaVerzeichnisblabla", die die DoqFreq-Anfrage-Ergebnisse
     * im TREC-Output-Format enthält
     *
     * @param zuSuchenderArtikel wapoArtikelID, für die die Kontextsuche ausgeführt wird
     * @param topicID            TopicID von TREC
     */
    private void erzeugeDateiFuerDoqFreqAnfrage(String zuSuchenderArtikel, int topicID) {
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
     * @param topicID            TopicID von TREC
     */
    private void erzeugeDateiFuerCoreNLPAnfrage(String zuSuchenderArtikel, int topicID) {
        SearchWithCoreNLP searchWithCoreNLP = new SearchWithCoreNLP();
        searchWithCoreNLP.startClient();

        Map ausgabe = searchWithCoreNLP.search(zuSuchenderArtikel);
        Set ergebnisMenge = ausgabe.keySet();
        //List ergebnisListe = Arrays.asList(ergebnisMenge.toArray());

        //TODO: score muss auch zurückgeliefert werden
        System.out.println("Gefundene Artikel:");
        System.out.println(ausgabe);
        System.out.println(ausgabe.keySet());

       // searchWithCoreNLP.closeClient();
    }


    public HashMap<Integer, String> readfromXML(String filepath) throws IOException {
        HashMap<Integer, String> hsm = new HashMap<Integer, String>();
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        String line; // Hier noch nicht br.readLine(), weil in Kopf von while schon gelesen wird, dann wuerde die erste Zeile uebersprungen
        String id = "";
        Integer num = 0;
        boolean insideTop = false; // Flag, um zu pruefen, ob man sich innerhalb eines <top> Tags befindet
        while ((line = br.readLine()) != null) {
            // Pruefung auf <top> Tag
            if (line.contains("<top>")) {
                insideTop = true;
            } else if (line.contains("</top>")) {
                insideTop = false;
            }

            // Wenn innerhalb <top> Tag
            if (insideTop) {
                if (line.contains("<num>")) {
                    Integer number = Integer.parseInt(line.replace("<num>", "").replace("</num>", "").replace(" ", "").replace("Number:", ""));
                    num = number;
                } else if (line.contains("<docid>")) {
                    String docId = line.replace("<docid>", "").replace("</docid>", "").replace(" ", "");
                    id = docId;
                }

                // Falls <top> und </top> in der selben Zeile sind, wieder auf false setzen
                if (line.contains("</top>")) {
                    insideTop = false;
                }
                hsm.put(num, id);
            }
        }
        System.out.println(hsm);
        return hsm;
    }

    public static void main(String[] args) throws IOException{
        new Main().start();
    }


}