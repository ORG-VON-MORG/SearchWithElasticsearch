package OutputFormatTrec_Eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Stellt Methoden zur Verfügung, die die Daten von "Output"-Objekten in Strings umwandeln, welche dem TREC-Output-Format
 * entsprechen.
 *
 * Die Strings, die ins TREC-Output-Format umgewandelt wurden, kann man:
 * 1) in eine Datei schreiben
 * 2) auf der Standardausgabe ausgeben lassen
 *
 *
 * TREC-Output-Format:          topicID Q0 wapoArtikelID rank score RUNTYPE
 *
 * @author Michelle Blau, Johannes Gerwert
 * @version 09.07.2018
 */

public class OutputWriter {

    private static final String RUNTYPE = "htwsaar";

    private StringBuilder outputBuilder;

    /**
     * Konstruktor, der das Attribut "outputBuilder" mit einem leeren String initialisiert, sodass dort später die Daten
     * im TREC-Output-Format abgespeichert werden können
     */
    public OutputWriter(){
        outputBuilder = new StringBuilder("");
    }

    /**
     * Diese Methode erstellt fuer ein uebergebenes Output-Array das gewünschte Output-Format von TREC.
     * Das Output-Format wird in dem "outputBuilder" Attribut gespeichert.
     * Das uebergebene Output-Array beinhaltet alle Daten, die für GENAU EIN Topic ermittelt wurden.
     * Das bedeutet: diese Methode muss für jedes von TREC gegebene Topic GENAU EIN MAL aufgerufen werden.
     * Wenn unterschiedliche Topic ID's verwendet werden, wird eine Exception geworfen.
     *
     * @param outputArray Array mit "Output"-Objekten - die "Output"-Objekte beinhalten die Ergebnisse vom Information-Retrieval System für EIN Topic
     */
    public void receive(Output[] outputArray){
    //TODO: überprüfe, ob alle topic-IDS des "Output"-Objekts gleich sind -> wirf Exception, wenn nicht
        int topicID = outputArray[0].getTopicID();

        for(int i = 0; i < outputArray.length; i++) {
            if (outputArray[i].getTopicID() != topicID) {
                throw new RuntimeException("Die Topic-IDs müssen alle gleich sein");
            }
        }

        for(int i= 0; i < outputArray.length; i++){
            format(outputArray[i], i+1);
        }

    }

    /**
     * Bereitet die Daten aus dem "Output"-Objekt auf und hängt diese an das Attribut "outputBuilder" an,
     * um das TREC-Output-Format zu erzeugen
     *
     * @param output "Output"-Objekt, das die Daten des Information-Retrieval-Ergebnisses erhält
     * @param rank Position des vom Information-Retrieval-Systems gefundenen Artikels
     */
    private void format(Output output, int rank){
        String temp = output.getTopicID() + " Q0 " + output.getWapoArtikelID() + " "
                + rank + " " + output.getScore() + " " + RUNTYPE + "\n";

        outputBuilder.append(temp);
    }

    /**
     * Speichert die aufbereiteten Daten, die im "outputBuilder"-Objekt drinstehen, in einer Datei ab.
     * Diese Methode sollte ERST NACH DER receive() METHODE AUSGEFÜHRT WERDEN!!!
     *
     * Die erzeugte Datei hat dann das TREC-Output-Format
     *
     * @param pfadname Pfadname der zu speichernden Datei
     * @throws FileNotFoundException
     */
    public void writeToFile(String pfadname) throws IOException {

        File datei = new File(pfadname).getCanonicalFile();
        PrintWriter writer = new PrintWriter(datei);

        writer.print(outputBuilder.toString());
        writer.flush();
        writer.close();


    }/**
     * Gibt die aufbereiteten Daten, die im "outputBuilder"-Objekt drinstehen, auf der Standardausgabe aus.
     * Diese Methode sollte ERST NACH DER receive() METHODE AUSGEFÜHRT WERDEN!!!
     */
    public void printToSTDOUT()  {

        System.out.println(outputBuilder.toString());
    }

    public void reset() {
        outputBuilder.setLength(0);
    }
}
