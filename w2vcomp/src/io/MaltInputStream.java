package io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import dependency.DependencyNode;
import dependency.DependencySentence;
import dependency.RawPhraseEntry;

public class MaltInputStream {
    BufferedReader   reader;
    int              wordFormatOption;

    String[]         singleWords;
    RawPhraseEntry[] rawPhrases;

    public MaltInputStream(String fileName, int wordFormatOption,
            boolean gzipped) throws IOException {
        if (!gzipped) {
            reader = new BufferedReader(new FileReader(fileName));
        } else {
            reader = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(new FileInputStream(fileName))));
        }
        this.wordFormatOption = wordFormatOption;
        singleWords = new String[0];
        rawPhrases = new RawPhraseEntry[0];
    }

    public boolean readNextSentence() throws IOException {
        ArrayList<DependencyNode> nodeList = new ArrayList<DependencyNode>();
        boolean startSentence = false;
        String line = reader.readLine();
        while (!"".equals(line) && !(line == null)) {
            if (line.startsWith("<")) {
                if (line.equals("<s>")) {
                    startSentence = true;
                } else if (line.equals("</s>")) {
                    break;
                }
            } else {
                if (startSentence) {
                    DependencyNode newNode = new DependencyNode(line);
                    newNode.toLowerCase();
                    nodeList.add(newNode);
                }
            }
            line = reader.readLine();
        }

        if (nodeList.size() > 1) {
            DependencySentence sentence = new DependencySentence(nodeList);
            singleWords = sentence.getSingleWords(wordFormatOption);
            // TODO: more phrases
            // only ans for now
            RawPhraseEntry[] ans = sentence.getANs(wordFormatOption);
            RawPhraseEntry[] svs = sentence.getSVs(wordFormatOption);
            rawPhrases = new RawPhraseEntry[ans.length + svs.length];
            System.arraycopy(ans, 0, rawPhrases, 0, ans.length);
            System.arraycopy(svs, 0, rawPhrases, ans.length, svs.length);
        } else {
            singleWords = new String[0];
            rawPhrases = new RawPhraseEntry[0];
        }

        if (line == null && !startSentence) {
            return false;
        } else {
            return true;
        }
    }

    public String[] getSingleWords() {
        return singleWords;
    }

    public RawPhraseEntry[] getRawPhrases() {
        return rawPhrases;
    }

    public void close() throws IOException {
        reader.close();
    }
}
