import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TestGesamt {

    /**
     * Methode fuehrt eine Suche mit den verschiedenen Sucharten durch und gibt die gefunden Artikel aus
     */
    public static void main (String[] args){
        //Input Artikel
        //WAPOId ist die ID der Washington Post
        //String WAPOId ="3902c9005a0563742fc4acb2c011b164";

        //Dieser Artikel funktioniert
        //String WAPOId ="09c0bf80-7e9e-11e6-8d0c-fb6c00c90481";

        //
        //String WAPOId = "9171debc316e5e2782e0d2404ca7d09d";

        //Geht jetzt
        //String WAPOId = "6b1408fea9f9587fd63b350efeb38fb6";


        //TODO: wirft bei Docfreq UND coreNLP eine ArrayIndexOutOfBoundsExeption, am Anfang fehlte die "3" !!!
        //String WAPOId = "37a8e2283e4677b703f6464d0191a700";

        //TODO: findet KEINE ERGEBNISSE - NUR bei coreNLP !!!!!!!
          String WAPOId = "6fdc62d37aaf685b809c501abe13c56c";



        System.out.println("Der Basis Artikel hat die ID: "+ WAPOId );



        //--------------------------Test SUCHE MIT DocFreq--------------------------

//        System.out.println("---------------TEST MIT DOC FREQ---------------");
//        SearchWithDocFreq searchWithDocFreq = new SearchWithDocFreq();
//        ArrayList<String[]> arrayList;
//        arrayList = searchWithDocFreq.search(WAPOId);
//
//        System.out.println("Gefundene Artikel:");
//        for(String[] arr : arrayList)
//            System.out.println("id: " + arr[0] + "\tscore :" + arr[1]);




        //--------------------------Test SUCHE MIT CORENLP--------------------------

        System.out.println("---------------TEST MIT CORENLP---------------");
        SearchWithCoreNLP searchWithCoreNLP = new SearchWithCoreNLP();
        ArrayList<String[]> arrayListCORENLP;
        arrayListCORENLP = searchWithCoreNLP.search(WAPOId);

        System.out.println("Gefundene Artikel:");
        for(String[] arr : arrayListCORENLP)
            System.out.println("id: " + arr[0] + "\tscore :" + arr[1]);

    }

}
