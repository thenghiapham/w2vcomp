package io.word;

import io.sentence.BasicTreeInputStream;
import io.sentence.TreeInputStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import tree.Tree;

public class TreeWordInputStream implements WordInputStream{

    TreeInputStream treeStream;
    Iterator<String> currentSentence = null;
    boolean endOfStream = false;
    public TreeWordInputStream(TreeInputStream treeStream) {
        this.treeStream = treeStream;
    }
    
    @Override
    public String readWord() throws IOException {
        // TODO Auto-generated method stub
        // end of file, new line?
        if (endOfStream) return "";
        if (currentSentence == null || !currentSentence.hasNext()) {
            Tree tree = treeStream.readTree();
            while (tree == BasicTreeInputStream.NEXT_DOC_TREE) {
                tree = treeStream.readTree();
            }
            
            if (tree == null) {
                endOfStream = true;
                return "</s>";
            } else {
                if (currentSentence == null) {
                    currentSentence = Arrays.asList(tree.getSurfaceWords()).iterator();
                } else {
                    currentSentence = Arrays.asList(tree.getSurfaceWords()).iterator();
                    return "</s>";
                }
            }
            
        }
        String nextWord = currentSentence.next();
//        System.out.println(nextWord);
        return nextWord;
    }

    @Override
    public boolean endOfFile() {
        // TODO Auto-generated method stub
        return endOfStream;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        treeStream.close();
    }

}
