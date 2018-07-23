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
        String WAPOId ="3902c9005a0563742fc4acb2c011b164";
        //String WAPOId ="09c0bf80-7e9e-11e6-8d0c-fb6c00c90481";

        System.out.println("Der Basis Artikel hat die ID: "+ WAPOId );



        //--------------------------Test SUCHE MIT DocFreq--------------------------

        System.out.println("---------------TEST MIT DOC FREQ---------------");
        SearchWithDocFreq searchWithDocFreq = new SearchWithDocFreq();
        ArrayList<String[]> arrayList;
        arrayList = searchWithDocFreq.search(WAPOId);

        System.out.println("Gefundene Artikel:");
        for(String[] arr : arrayList)
            System.out.println("id: " + arr[0] + "\tscore :" + arr[1]);




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
