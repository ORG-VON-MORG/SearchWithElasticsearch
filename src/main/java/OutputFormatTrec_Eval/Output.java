package OutputFormatTrec_Eval;

/**
 * Diese Klasse fasst die Ausgabe unseres Information-Retrieval-Systems zusammen.
 * Die Ausgabe wird in die einzelnen Attribute dieser Klasse eingefügt.
 * Dies vereinfacht das Weiterverarbeiten der Ausgabe zu einem späteren Zeitpunkt, um das "trec_eval Output Format" zu erzeugen.
 *
 * @author Michelle Blau, Johannes Gerwert
 * @version 09.07.2018
 */

public class Output {

    int    topicID;
    String wapoArtikelID;
    double score;


    /**
     * Konstruktor
     *
     * @param topicID Die von TREC vorgegebene Topic-ID
     * @param wapoArtikelID Die WashingtonPost-Artikel-ID
     * @param score Der Score unseres Information-Retrieval Systems
     */
    public Output(int topicID, String wapoArtikelID, double score){
        this.topicID = topicID;
        this.wapoArtikelID = wapoArtikelID;
        this.score = score;

    }


    public int getTopicID() {
        return topicID;
    }

    public String getWapoArtikelID() {
        return wapoArtikelID;
    }

    public double getScore() {
        return score;
    }

}
