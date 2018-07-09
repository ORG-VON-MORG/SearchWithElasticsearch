import java.util.Map;

public class SearchWithCoreNLPTest {

    public static void main (String[] args)
    {
        String artikelID = "34d4708d7cce27237b991c02c98eeeb5";
        String artikelID2 = "35f30c00-efdd-11e2-a1f9-ea873b7e0424";
        String artikelID3 = "d9ab1a04-eff1-11e2-9008-61e94a7ea20d";
        String artikelID4 = "ecb715f2-efd4-11e2-9008-61e94a7ea20d";
        SearchWithCoreNLP searchWithCoreNLP = new SearchWithCoreNLP();
        searchWithCoreNLP.startClient();

        //-----delete the time later, only for testing-----

        long start1 = System.currentTimeMillis();
        Map ausgabe = searchWithCoreNLP.search(artikelID);
        System.out.println(ausgabe);
        long end1 = System.currentTimeMillis() - start1;

        long start2 = System.currentTimeMillis();
        Map ausgabe2 = searchWithCoreNLP.search(artikelID2);
        System.out.println(ausgabe2);
        long end2 = System.currentTimeMillis() - start2;

        long start3 = System.currentTimeMillis();
        Map ausgabe3 = searchWithCoreNLP.search(artikelID3);
        System.out.println(ausgabe3);
        long end3 = System.currentTimeMillis() - start3;

        System.out.println(end1);
        System.out.println(end2);
        System.out.println(end3);

        Map a4 = searchWithCoreNLP.search(artikelID4);
        System.out.println(a4);

        //searchWithCoreNLP.closeClient();
    }

}
