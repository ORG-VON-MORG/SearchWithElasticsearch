
import java.util.Map;

public class SearchWithCoreNLPTest {


    public static void main (String[] args)
    {
        SearchWithCoreNLP searchWithCoreNLP;
        String artikelID = "34d4708d7cce27237b991c02c98eeeb5";
        searchWithCoreNLP = new SearchWithCoreNLP();
        Map ausgabe = searchWithCoreNLP.search(artikelID);
        System.out.println(ausgabe);


    }

}
