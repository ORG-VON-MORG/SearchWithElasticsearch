import java.util.ArrayList;

public class TestSearchWithDocFreq {

    public void main (String[] args){

        SearchWithDocFreq searchWithDocFreq;
        String WAPOId ="34d4708d7cce27237b991c02c98eeeb5";
        ArrayList<String> arrayList;

        System.out.println("Es wird die Suche mit DocFreq genutzt");
        System.out.println("Basis Artikel " + WAPOId);

        searchWithDocFreq = new SearchWithDocFreq();

        arrayList = searchWithDocFreq.search(WAPOId);

        System.out.println(arrayList);


    }
}
