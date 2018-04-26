package words;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WordCounter {
    private final List<CountedWord> words;
    private final HashMap<Word,Integer> wordmap;

    WordCounter() {
	words = new ArrayList<>();
        wordmap = new HashMap<>();
    }
    
    private CountedWord findWord(Word w) {
        
        for(int i = 0; i < words.size(); i++) {
            CountedWord cw = words.get(i);
//            wordmap.put(cw, wordmap.getOrDefault(cw, 0) + 1);
//            if(cw.getWord().equals(w))
//                return cw;
        
//    
                if(wordmap.containsKey(w)){
                    return cw;
                }
        }
        
        return null;
    }

    void countWord(Word w) {
        CountedWord cw = findWord(w);
	if(cw == null) {
	    cw = new CountedWord(w);
            wordmap.put(w, getCount(w));
//           wordmap.put(cw, wordmap.get(cw) + 1);
        }
	cw.tally();
    }

    int getCount(Word w) {
	CountedWord cw = findWord(w);
	if(cw == null)
	    return(0);
	else
	    return(wordmap.get(w));
    }

    int numWords() {
	return(words.size());
    }

    CountedWord [] sortWords() {
        CountedWord[] wds = words.toArray(new CountedWord[0]);
	Quicksort.quickSort(wds);
	return(wds);
    }

}
