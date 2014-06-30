package vocab.filter;

import java.util.ArrayList;

import common.DataStructureUtils;

import vocab.VocabEntry;
import vocab.VocabEntryFilter;

public class BooleanVocabFilter implements VocabEntryFilter {
    public static final int     AND_FILTER = 0;
    public static final int     OR_FILTER  = 1;
    ArrayList<VocabEntryFilter> filters;
    int                         type;

    public BooleanVocabFilter(ArrayList<VocabEntryFilter> filters, int type) {
        if (type == OR_FILTER) {
            this.type = type;
        } else {
            this.type = AND_FILTER;
        }
        this.filters = filters;
    }

    public BooleanVocabFilter(VocabEntryFilter[] filters, int type) {
        if (type == OR_FILTER) {
            this.type = type;
        } else {
            this.type = AND_FILTER;
        }
        this.filters = DataStructureUtils.arrayToList(filters);
    }

    // AND: !isFiltered if !isFiltered by all
    // OR: isFiltered if isFiltered by all
    @Override
    public boolean isFiltered(VocabEntry entry) {
        // TODO Auto-generated method stub
        if (type == OR_FILTER) {
            for (int i = 0; i < filters.size(); i++) {
                if (!filters.get(i).isFiltered(entry))
                    return false;
            }
            return true;
        } else {
            for (int i = 0; i < filters.size(); i++) {
                if (filters.get(i).isFiltered(entry))
                    return true;
            }
            return false;
        }
    }

}
