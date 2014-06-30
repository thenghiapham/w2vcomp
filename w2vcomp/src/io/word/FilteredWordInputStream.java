package io.word;

import java.io.IOException;

public class FilteredWordInputStream implements WordInputStream {

    WordInputStream inputStream;
    WordFilter      wordFilter;

    public FilteredWordInputStream(WordInputStream inputStream,
            WordFilter filter) {
        this.inputStream = inputStream;
        this.wordFilter = filter;
    }

    @Override
    public String readWord() throws IOException {
        String nextWord = inputStream.readWord();
        while (wordFilter.isFiltered(nextWord)) {
            nextWord = inputStream.readWord();
        }
        return nextWord;
    }

    @Override
    public boolean endOfFile() {
        return inputStream.endOfFile();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

}
