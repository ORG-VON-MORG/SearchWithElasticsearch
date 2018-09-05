import OutputFormatTrec_Eval.Output;
import OutputFormatTrec_Eval.OutputWriter;
import Util.util;

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
            HashMap<Integer, String> mapMitDatenAusTopicDatei = util.readfromXML(topic.toString());
            Set<Integer>             alleTopicIDs             = mapMitDatenAusTopicDatei.keySet();

            System.out.println("ANZAHL TOPICS: " + (alleTopicIDs.size()-1));
            System.out.println(alleTopicIDs);
            System.out.println(mapMitDatenAusTopicDatei);


            OutputWriter outputWriterDocFreq = new OutputWriter(RUNTAG_DOCFREQ);
            OutputWriter outputWriterCoreNLP = new OutputWriter(RUNTAG_CORENLP);

            for(Integer topicID : alleTopicIDs) {
                String wapoArtikelID = mapMitDatenAusTopicDatei.get(topicID);

                if(stringIstNichtLeer(wapoArtikelID)){

                  fuelleOutputWriterMitDocFreqAnfrage(wapoArtikelID, topicID, outputWriterDocFreq);
                  fuelleOutputWriterMitCoreNLPAnfrage(wapoArtikelID, topicID, outputWriterCoreNLP);
                }
            }

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
        SearchWithDocFreq searchWithDocFreq = new SearchWithDocFreq();
        ArrayList<String[]> ergebnisListe;

        System.out.println("DOCFREQ ZU SUCHENDER ARTIKEL: " + zuSuchenderArtikel);

        ergebnisListe = searchWithDocFreq.search(zuSuchenderArtikel);

        //System.out.println("DOCFREQ ERGEBNISLISTE: " + ergebnisListe);

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
        SearchWithCoreNLP searchWithCoreNLP = new SearchWithCoreNLP();
        ArrayList<String[]> ergebnisListe;

        System.out.println("CORENLP ZU SUCHENDER ARTIKEL: " + zuSuchenderArtikel);

        ergebnisListe = searchWithCoreNLP.search(zuSuchenderArtikel);

        //System.out.println("CORENLP ERGEBNISLISTE: " + ergebnisListe);

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