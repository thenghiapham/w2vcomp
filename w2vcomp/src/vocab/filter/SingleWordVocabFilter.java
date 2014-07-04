package vocab.filter;

import common.WordForm;

import vocab.VocabEntry;
import vocab.VocabEntryFilter;

public class SingleWordVocabFilter implements VocabEntryFilter {

    int wordForm;

    public SingleWordVocabFilter(int wordForm) {
        this.wordForm = wordForm;
    }

    @Override
    public boolean isFiltered(VocabEntry entry) {
        // if the "word" consists of no less than two words, return true
        if (this.wordForm == WordForm.WORD
                || this.wordForm == WordForm.LEMMA) {
            String[] elements = entry.word.split("_");
            return elements.length >= 2;
        } else {
            String[] elements = entry.word.split("-[a-z]_");
            return elements.length >= 2;
        }
    }

}
