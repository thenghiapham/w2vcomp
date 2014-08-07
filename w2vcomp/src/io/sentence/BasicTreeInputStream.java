package io.sentence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import common.exception.ValueException;

import tree.Tree;

public class BasicTreeInputStream implements TreeInputStream {
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
            if (!line.equals("") && !line.startsWith("<text id")) {
                try {
                    line = line.replaceAll("\\(\\)", "LRB)");
                    line = line.replaceAll(" \\)", " RRB");
                    tree = Tree.fromPennTree(line);
                    readLineNum++;
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
