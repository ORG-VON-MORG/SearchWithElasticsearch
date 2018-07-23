import OutputFormatTrec_Eval.Output;
import OutputFormatTrec_Eval.OutputWriter;

import java.io.*;
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


    private static final String TOPIC_VERZEICHNIS    = ".\\src\\main\\resources\\TREC-TOPICS";
    private static final String ERGEBNIS_VERZEICHNIS = ".\\src\\main\\resources\\Anfrageergebnisse";
    private static final String ERGEBNIS_VERZEICHNIS_DOCFREQ = ".\\src\\main\\resources\\Anfrageergebnisse\\DocFreq\\";
    private static final String ERGEBNIS_VERZEICHNIS_CORENLP = ".\\src\\main\\resources\\Anfrageergebnisse\\CoreNLP\\";
    private static final String RUNTAG_DOCFREQ = "htwsaar01";
    private static final String RUNTAG_CORENLP = "htwsaar02";


    /**
     * Erstellt für jede Anfrage-Art eine eigene Ergebnis-Datei, Zielverzeichnis: "..../resources/Anfrageergebenisse"
     * Liest dazu die entsprechenden Topics im Verzeichnis "..../resources/TREC-TOPICS"
     *
     * @throws IOException
     */
    private void start() throws IOException{
        File   topicVerzeichnis        = new File(TOPIC_VERZEICHNIS).getCanonicalFile();
        File[] alleTopicsImVerzeichnis = topicVerzeichnis.listFiles();

        for(File topic : alleTopicsImVerzeichnis) {
            HashMap<Integer, String> mapMitDatenAusTopicDatei = readfromXML(topic.toString());
            Set<Integer>             alleTopicIDs             = mapMitDatenAusTopicDatei.keySet();

            OutputWriter outputWriterDocFreq = new OutputWriter(RUNTAG_DOCFREQ);
            OutputWriter outputWriterCoreNLP = new OutputWriter(RUNTAG_CORENLP);

            for(Integer topicID : alleTopicIDs) {
                String wapoArtikelID = mapMitDatenAusTopicDatei.get(topicID);

                if(stringIstNichtLeer(wapoArtikelID)){

                    //fuelleOutputWriterMitDocFreqAnfrage(wapoArtikelID, topicID, outputWriterDocFreq);
                    fuelleOutputWriterMitCoreNLPAnfrage(wapoArtikelID, topicID, outputWriterCoreNLP);
                }
            }
            //TODO: dieser Teil hier sollte lesbarer gemacht werden
            outputWriterDocFreq.printToSTDOUT();
            outputWriterDocFreq.writeToFile(ERGEBNIS_VERZEICHNIS_DOCFREQ +"DocFreq_" + erzeugeDateiName(topic.getName()));
            outputWriterDocFreq.reset();

            outputWriterCoreNLP.printToSTDOUT();
            outputWriterCoreNLP.writeToFile(ERGEBNIS_VERZEICHNIS_CORENLP +"CoreNLP_" + erzeugeDateiName(topic.getName()));
            outputWriterCoreNLP.reset();
        }
    }



    /**
     * Führt für die gegebene ArtikelID die DocFreq-Anfrage aus und speichert die Ergebnisse im "outputWriter".
     *
     * @param zuSuchenderArtikel wapoArtikelID, für die die Kontextsuche ausgeführt wird
     * @param outputWriter sammelt die Anfrage-Ergebnisse
     * @param topicID TopicID von TREC
     */
    private void fuelleOutputWriterMitDocFreqAnfrage(String zuSuchenderArtikel, int topicID, OutputWriter outputWriter) {
        SearchWithDocFreq searchWithDocFreq;
        ArrayList<String[]> ergebnisListe;
        searchWithDocFreq = new SearchWithDocFreq();
        ergebnisListe = searchWithDocFreq.search(zuSuchenderArtikel);

        //(ergebnisListe.get(0)[0]) -> artikelID als string
        //(ergebnisListe.get(0)[1]) -> score als string

        Output[] outputArray = new Output[ergebnisListe.size()];

        for(int i = 0; i < ergebnisListe.size(); i++){
            String[] artikelIDUndScore = ergebnisListe.get(i);
            String artikelID = artikelIDUndScore[0];
            double score = Double.parseDouble(artikelIDUndScore[1]);

            outputArray[i] = new Output(topicID, artikelID, score);
        }
        outputWriter.receive(outputArray);
    }



    /**
     * Führt für die gegebene ArtikelID die CoreNLP-Anfrage aus und speichert die Ergebnisse im "outputWriter".
     *
     * @param zuSuchenderArtikel wapoArtikelID, für die die Kontextsuche ausgeführt wird
     * @param outputWriter sammelt die Anfrage-Ergebnisse
     * @param topicID TopicID von TREC
     */
    private void fuelleOutputWriterMitCoreNLPAnfrage(String zuSuchenderArtikel, int topicID, OutputWriter outputWriter) {

        ArrayList<String[]> ergebnisListe;
        SearchWithCoreNLP searchWithCoreNLP = new SearchWithCoreNLP();
        //searchWithCoreNLP.startClient();

        ergebnisListe = searchWithCoreNLP.search(zuSuchenderArtikel);

        Output[] outputArray = new Output[ergebnisListe.size()];

        for(int i = 0; i < ergebnisListe.size(); i++){
            String[] artikelIDUndScore = ergebnisListe.get(i);
            String artikelID = artikelIDUndScore[0];
            double score = Double.parseDouble(artikelIDUndScore[1]);

            outputArray[i] = new Output(topicID, artikelID, score);
        }

        outputWriter.receive(outputArray);
    }



    /**
     * TODO: Kommentar einfügen
     *
     * @author Kevin Engelhardt
     * @param filepath
     * @return
     * @throws IOException
     */
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
        //System.out.println(hsm);
        return hsm;
    }


    /**
     * Prüft, ob ein übergebener String nicht leer ist.
     *
     * @param string zu überprüfender String
     * @return true falls der "string" nicht leer ist, sonst false
     */
    private boolean stringIstNichtLeer(String string){
        if (!(string.trim().isEmpty())){
            return true;

        }else{
            return false;
        }
    }

    /**
     * Erzeugt zum uebergebenen TREC-Topic-Dateinamen eine passende Dateiendung ohne die Zeichenfolge ".xml"
     *
     * @param topicDateiname
     * @return einen Dateinamen, der die Zeichenfolge ".xml" nicht enthält
     */
    private String erzeugeDateiName(String topicDateiname) {
        if (topicDateiname.contains(".xml")) {
            topicDateiname = topicDateiname.replaceAll("\\.xml","");
        }
        topicDateiname = topicDateiname + ".txt";

        return topicDateiname;
    }


    /**
     * Starten des Programms
     *
     * @param args wird nicht beachtet
     * @throws IOException
     */
    public static void main(String[] args) throws IOException{
        new Main().start();
    }


}