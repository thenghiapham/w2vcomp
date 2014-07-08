package io.word.filter;

import io.word.WordFilter;

public class ContentWordFilter implements WordFilter {

    @Override
    public boolean isFiltered(String word) {
        if (word.endsWith("n") || word.endsWith("a") || word.endsWith("j")
                || word.endsWith("r")) {
            return false;
        } else {
            if (word.equals("</s>") || word.equals("")) {
                return false;
            }
            return true;
        }
    }

}
