package io.sentence;

import io.word.Phrase;

import java.io.IOException;
import java.util.ArrayList;

import common.DataStructureUtils;

import tree.Tree;
import vocab.Vocab;

public class ParseSentenceInputStream implements SentenceInputStream{
    protected TreeInputStream inputStream;
    int[] currentSentence;
    Phrase[] emptyList = new Phrase[0];
    long wordCount = 0;
    public ParseSentenceInputStream(TreeInputStream inputStream) {
        this.inputStream = inputStream;
    }
    @Override
    public boolean readNextSentence(Vocab vocab) throws IOException {
        // TODO Auto-generated method stub
        Tree tree = inputStream.readTree();
        while (tree == BasicTreeInputStream.NEXT_DOC_TREE) {
            tree = inputStream.readTree();
        }
        if (tree == null) {
            currentSentence = null;
            return false;
        } else {
            String[] words = tree.getSurfaceWords();
            ArrayList<Integer> wordIndexList = new ArrayList<>();
            for (String word: words) {
                int wordIndex = vocab.getWordIndex(word);
                if (wordIndex != -1)
                    wordIndexList.add(wordIndex);
            }
            currentSentence = DataStructureUtils.intListToArray(wordIndexList);
            wordCount += currentSentence.length;
            return true;
        }
    }
    
    @Override
    public int[] getCurrentSentence() throws IOException {
        // TODO Auto-generated method stub
        return currentSentence;
    }
    
    @Override
    public Phrase[] getCurrentPhrases() throws IOException {
        // TODO Auto-generated method stub
        return emptyList;
    }
    @Override
    public long getWordCount() {
        // TODO Auto-generated method stub
        return wordCount;
    }
    
}
