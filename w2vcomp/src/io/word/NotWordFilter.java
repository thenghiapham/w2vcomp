package io.word;

public class NotWordFilter implements WordFilter {
    WordFilter filter;

    public NotWordFilter(WordFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean isFiltered(String word) {
        // TODO Auto-generated method stub
        return !filter.isFiltered(word);
    }

}
