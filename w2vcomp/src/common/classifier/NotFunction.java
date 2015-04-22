package common.classifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;

import common.HeatMapPanel;
import common.exception.ValueException;
import common.wordnet.WordNetAdj;

import space.Neighbor;
import space.RawSemanticSpace;

public class NotFunction {
    protected RawSemanticSpace space;
    protected SimpleMatrix notFunction = null;
    
    public NotFunction(RawSemanticSpace space) {
        this.space = space;
    }
    
    public void train(List<String[]> trainPairs) {
        ArrayList<String> words = new ArrayList<>();
        ArrayList<String> antonyms = new ArrayList<>();
        for (String[] pair: trainPairs) {
            if (space.contains(pair[0])&& space.contains(pair[1])) {
                words.add(pair[0]);
                antonyms.add(pair[1]);
            }
        }
        SimpleMatrix wordMatrix = new SimpleMatrix(space.getSubSpace(words).getVectors());
        SimpleMatrix antonymMatrix = new SimpleMatrix(space.getSubSpace(antonyms).getVectors());
        notFunction = wordMatrix.solve(antonymMatrix);
    }
    
    public double test(List<String[]> testPairs, int topN) {
        if (notFunction == null) throw new ValueException("Not trained yet");
        ArrayList<String> words = new ArrayList<>();
        ArrayList<String> antonyms = new ArrayList<>();
        for (String[] pair: testPairs) {
            if (space.contains(pair[0])&& space.contains(pair[1])) {
                words.add(pair[0]);
                antonyms.add(pair[1]);
            }
        }
        int correct = 0;
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            String antonym = antonyms.get(i);
            SimpleMatrix wordVector = space.getVector(word);
            SimpleMatrix predictedAntonymVector = wordVector.mult(notFunction);
            Neighbor[] neighbors = space.getNeighbors(predictedAntonymVector, topN);
//            Neighbor[] neighbors = space.getNeighbors(predictedAntonymVector, topN, new String[]{word});
            for (Neighbor neighbor: neighbors) {
                if (neighbor.word.equals(antonym)) {
//                    System.out.println(word + " " + antonym);
                    correct++;
                    break;
                }
            }
        }
        return correct / (double) words.size();
    }
    
    public ArrayList<String[]> getAntonymPairs(WordNetAdj wordnetAdj) {
        ArrayList<String[]> pairs = new ArrayList<>();
        for (String word: wordnetAdj.getAllWords()) {
            String[] antonyms = wordnetAdj.getAllFirstSenseAntonyms(word);
            if (antonyms.length != 0) {
                pairs.add(new String[]{word, antonyms[0]});
            }
        }
        return pairs;
    }
    
    public static void main(String[] args) throws IOException{
        String adjFile = args[0];
        String spaceFile = args[1];
        RawSemanticSpace space = RawSemanticSpace.readSpace(spaceFile);
        WordNetAdj wordnetAdj = new WordNetAdj(adjFile);
        space = space.getSubSpace(wordnetAdj.getAllWords());
        NotFunction function = new NotFunction(space);
        ArrayList<String[]> pairs = function.getAntonymPairs(wordnetAdj);
        Collections.shuffle(pairs);
        System.out.println(pairs.size());
        int testSize = 500;
        List<String[]> trainPairs = pairs.subList(0, pairs.size() - testSize);
        List<String[]> testPairs = pairs.subList(pairs.size() - testSize, pairs.size());
        function.train(trainPairs);
        System.out.println("Result on train:" + function.test(trainPairs, 1));
        System.out.println("Result on test:" + function.test(testPairs, 1));
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SimpleMatrix notMatrix = function.notFunction;
        f.getContentPane().add(new HeatMapPanel(notMatrix));
        f.setSize(notMatrix.numCols() * 4, notMatrix.numRows() * 4);
        f.setLocation(200,200);
        f.setVisible(true);
        
    }
}
