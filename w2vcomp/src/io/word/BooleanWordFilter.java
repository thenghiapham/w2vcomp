package io.word;

import java.util.ArrayList;

public class BooleanWordFilter implements WordFilter {
    public static final int AND_FILTER = 0;
    public static final int OR_FILTER  = 1;
    ArrayList<WordFilter>   filters;
    int                     type;

    public BooleanWordFilter(ArrayList<WordFilter> filters, int type) {
        if (type == OR_FILTER) {
            this.type = type;
        } else {
            this.type = AND_FILTER;
        }
        this.filters = filters;
    }

    @Override
    public boolean isFiltered(String word) {
        // TODO Auto-generated method stub
        if (type == AND_FILTER) {
            for (int i = 0; i < filters.size(); i++) {
                if (!filters.get(i).isFiltered(word))
                    return false;
            }
            return true;
        } else {
            for (int i = 0; i < filters.size(); i++) {
                if (filters.get(i).isFiltered(word))
                    return true;
            }
            return false;
        }
    }

}
