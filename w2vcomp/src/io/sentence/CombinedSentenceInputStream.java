package io.sentence;

import io.word.Phrase;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import vocab.Vocab;

public class CombinedSentenceInputStream implements SentenceInputStream {
    protected Iterator<SentenceInputStream> streamIterator;
    protected SentenceInputStream           currentInputStream;
    long                                    wordCount = 0;
    boolean crossStream = false;

    public CombinedSentenceInputStream(List<SentenceInputStream> inputStreams) {
        streamIterator = inputStreams.iterator();
        if (streamIterator.hasNext()) {
            currentInputStream = streamIterator.next();
        } else {
            currentInputStream = null;
        }
    }

    @Override
    public boolean readNextSentence(Vocab vocab) throws IOException {
        crossStream = false;
        if (currentInputStream == null)
            return false;
        while (true) {
            boolean hasNextSentence = currentInputStream
                    .readNextSentence(vocab);
            if (hasNextSentence)
                return true;
            wordCount += currentInputStream.getWordCount();

            boolean hasNextStream = false;
            while (streamIterator.hasNext()) {
                currentInputStream = streamIterator.next();
                crossStream = true;
                if (currentInputStream == null) {
                    continue;
                } else {
                    hasNextStream = true;
                    break;
                }
            }
            if (!hasNextStream) {
                currentInputStream = null;
                return false;
            }
        }
    }

    @Override
    public int[] getCurrentSentence() throws IOException {
        if (currentInputStream == null)
            return new int[0];
        else
            return currentInputStream.getCurrentSentence();
    }

    @Override
    public Phrase[] getCurrentPhrases() throws IOException {
        if (currentInputStream == null)
            return new Phrase[0];
        else
            return currentInputStream.getCurrentPhrases();
    }

    @Override
    public long getWordCount() {
        if (currentInputStream == null)
            return wordCount;
        else
            return wordCount + currentInputStream.getWordCount();
    }

    @Override
    public boolean crossDocBoundary() {
        // TODO Auto-generated method stub
        if (currentInputStream != null)
            return currentInputStream.crossDocBoundary() || crossStream ;
        else
            return crossStream;
    }

}
