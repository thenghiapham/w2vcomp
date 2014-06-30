package vocab.filter;

import vocab.VocabEntry;
import vocab.VocabEntryFilter;

public class NotVocabFilter implements VocabEntryFilter {

    protected VocabEntryFilter innerFilter;

    public NotVocabFilter(VocabEntryFilter innerFilter) {
        this.innerFilter = innerFilter;
    }

    @Override
    public boolean isFiltered(VocabEntry entry) {
        // TODO Auto-generated method stub
        return !innerFilter.isFiltered(entry);
    }

}
