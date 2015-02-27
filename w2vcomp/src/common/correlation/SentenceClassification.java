package common.correlation;

import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import space.SemanticSpace;
import tree.Tree;
import common.IOUtils;
import composition.BasicComposition;

public class SentenceClassification {
    String[] parsedSentences;
    String[] sentences;
    String[] labels;
    public SentenceClassification(String dataset) {
        ArrayList<String> lines = IOUtils.readFile(dataset);
        parsedSentences = new String[lines.size()];
        sentences = new String[lines.size()];
        labels = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            String[] elements = lines.get(i).split("\t");
            parsedSentences[i] = elements[0];
            sentences[i] = removePunctuation(Tree.fromPennTree(elements[0]).getSurfaceString());
//            System.out.println(sentences[i]);
            labels[i] = elements[1];
        }
    }
    
    public static String removePunctuation(String sentence) {
        String[] wordArray = sentence.split(" ");
        ArrayList<String> words = new ArrayList<String>();
        for (String word: wordArray) {
            if (!(".".equals(word) || "--".equals(word) || ",".equals(word) 
                    || "''".equals(word) || "``".equals(word))) {
                words.add(word);
            }
        }
        String[] result = new String[words.size()];
        result = words.toArray(result);
        return join(result, " ");
    }
    
    public static String join(String[] words, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        if (words.length == 0) return "";
        else {
            buffer.append(words[0]);
            for (int i = 1; i < words.length; i++) {
                buffer.append(delimiter);
                buffer.append(words[i]);
            }
        }
        return buffer.toString();
    }
    
    public String[] getLabels() {
        return labels;
    }
    
    public String[] getSentences() {
        return sentences;
    }
    
    public String[] getParsedSentences() {
        return parsedSentences;
    }
    
    public double[][] getSentenceVectors(SemanticSpace space, BasicComposition comp) {
        SemanticSpace phraseSpace = comp.composeSpace(space, sentences);
        return getSentenceVectors(phraseSpace);
    }
    
    // TODO: not average here?
    public double[][] getSentenceVectors(SemanticSpace phraseSpace) {
        double[][] result = new double[sentences.length][];
        for (int i = 0; i < sentences.length; i++) {
            SimpleMatrix vector = phraseSpace.getVector(sentences[i]);
//            vector = vector.scale(1.0 / sentences[i].split(" ").length);
            result[i] = vector.getMatrix().data;
        }
        return result;
    }
}
