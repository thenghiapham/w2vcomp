package io.sentence;

import io.word.Phrase;
import io.word.WordInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import common.DataStructureUtils;

import tree.CcgTree;
import vocab.Vocab;

public class CcgInputStream implements WordInputStream, SentenceInputStream {
    BufferedReader reader;
    boolean endOfFile = false;
    String[] words;
    int[] sentence;
    Phrase[] phrases;
    CcgTree ccgTree;
    int wordCount;
    int wordIndex = 0;
    
    public CcgInputStream(BufferedReader reader) {
        this.reader = reader;
        words = new String[0];
    }

    public void readNextLine() {
        if (endOfFile) return;
        try {
            String line = reader.readLine();
            if (line == null) {
                endOfFile = true;
                words = null;
                sentence = null;
                phrases = null;
                ccgTree = null;
                wordCount = 0;
                return;
            }
            ccgTree = CcgTree.fromSimplePennTree(line);
            String surfaceString = ccgTree.getSurfaceString();
            words = surfaceString.split(" ");
        } catch (IOException e) {
            e.printStackTrace();
            endOfFile = true;
        }
    }

    @Override
    public boolean readNextSentence(Vocab vocab) throws IOException {
        // TODO Auto-generated method stub
        if (endOfFile) return false;
        else {
            readNextLine();
            if (endOfFile) return false;
            ccgTree.updateSubTreePositio(0);
            List<CcgTree> subTrees = ccgTree.getAllSubTreeAtHeight(2);
            
            
            // single words
            ArrayList<Integer> wordIndices = new ArrayList<Integer>();
            int[] newIndices = new int[words.length];
            int newIndex = 0;
            wordCount = 0;
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                
                int wordIndex = vocab.getWordIndex(word);
                if (wordIndex != -1) {
                    newIndices[i] = newIndex;
                    wordIndices.add(wordIndex);
                    newIndex++;
                    wordCount ++;
                } 
                // set those words'positions that are not in vocab to -1 
                else {
                    newIndices[i] = Integer.MIN_VALUE;
                }
            }
            // endOfSentence
            wordCount++;
            sentence = DataStructureUtils.intListToArray(wordIndices);
            
            
            // phrases
            ArrayList<Phrase> phraseList = new ArrayList<Phrase>(); 
            for (int i = 0; i < subTrees.size(); i++) {
                CcgTree subTree = subTrees.get(i);
                int startPosition = newIndices[subTree.getLeftPos()];
                int endPosition = newIndices[subTree.getRightPos()];
                if (endPosition - startPosition == subTree.getRightPos() - subTree.getLeftPos()) {
                    phrases[i] = new Phrase(subTree.getType(), startPosition, endPosition, subTree);
                }
            }
            phrases = DataStructureUtils.phraseListToArray(phraseList);
        }
        return true;
    }

    @Override
    public int[] getCurrentSentence() throws IOException {
        // TODO Auto-generated method stub
        return sentence;
    }

    @Override
    public Phrase[] getCurrentPhrases() throws IOException {
        // TODO Auto-generated method stub
        return phrases;
    }

    @Override
    public long getWordCount() {
        // TODO Auto-generated method stub
        return wordCount;
    }

    @Override
    public String readWord() throws IOException {
        // TODO Auto-generated method stub
        if (words == null) {
            return "";
        }
        if (wordIndex == words.length) {
            wordIndex = 0;
            readNextLine();
            if (words == null) return "";
        }
        wordIndex++;
        return words[wordIndex - 1];
        
    }

    @Override
    public boolean endOfFile() {
        // TODO Auto-generated method stub
        return endOfFile;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        
    }
    
}
