package io.sentence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import common.exception.ValueException;

import tree.Tree;

public class BasicTreeInputStream implements TreeInputStream {
    public static final Tree NEXT_DOC_TREE = Tree.fromPennTree("next_document");
    BufferedReader reader;
    long readLineNum;
    
    public BasicTreeInputStream(File file) throws IOException {
        reader = new BufferedReader(new FileReader(file));
    }
    
    public BasicTreeInputStream(String filePath) throws IOException{
        this(new File(filePath));
    }

    @Override
    public Tree readTree() throws IOException{
        // TODO Auto-generated method stub
        if (reader == null) return null;
        
        String line = reader.readLine();
        while (line != null) {
            Tree tree = null;
            if (line.startsWith("<text id")) {
                return NEXT_DOC_TREE;
            }
            if (!line.equals("")) {
                try {
                    readLineNum++;
                    line = line.replaceAll("\\(\\)", "LRB)");
                    line = line.replaceAll(" \\)", " RRB");
                    if (!(line.length() <= 20 || line.length() >= 1000)) {
                        tree = Tree.fromPennTree(line);
                    }
                    
                } catch (ValueException e) {
                    tree = null;
                }
                if (tree != null) return tree;
            }
            line = reader.readLine();
        }
        return null;
        
        
    }

    @Override
    public long getReadLine() {
        return readLineNum;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
