package common.classifier;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;

import common.HeatMapPanel;
import common.IOUtils;
import common.SimpleMatrixUtils;
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
//        ArrayList<String[]> pairs = function.getAntonymPairs(wordnetAdj);
//        Collections.shuffle(pairs);
//        printListPair(pairs, "/home/thenghiapham/ant_pairs.txt");
        ArrayList<String[]> pairs = IOUtils.readTupleList("/home/thenghiapham/ant_pairs.txt");
        System.out.println(pairs.size());
        int foldNum = 10;
        int foldLength = pairs.size() / foldNum;
        int mod = pairs.size() % foldNum;
        for (int i = 0; i < foldNum; i++) {
            int begin = 0;
            int end = 0;
            if (i < mod) {
                begin = (foldLength + 1) * i;
                end = begin + foldLength + 1;
            } else {
                begin = (foldLength * i) + mod;
                end = begin + foldLength;
            }
            List<String[]> topPairs = new ArrayList<>();
            if (i != 0) {
                topPairs = pairs.subList(0, begin);
            }
            List<String[]> botPairs = new ArrayList<>();
            if (i != 0) {
                topPairs = pairs.subList(0, begin);
            }
        }
        
        
        int testSize = 500;
        List<String[]> trainPairs = pairs.subList(0, pairs.size() - testSize);
        List<String[]> testPairs = pairs.subList(pairs.size() - testSize, pairs.size());
        function.train(trainPairs);
        System.out.println("Result on train:" + function.test(trainPairs, 1));
        System.out.println("Result on test:" + function.test(testPairs, 1));
//        JFrame f = new JFrame();
//        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        SimpleMatrix notMatrix = function.notFunction;
//        IOUtils.saveMatrix("/home/thenghiapham/matrix2.txt", SimpleMatrixUtils.to2DArray(notMatrix), false);
//        f.getContentPane().add(new HeatMapPanel(notMatrix));
//
////        f.setSize(notMatrix.numCols() * 2, notMatrix.numRows() * 2);
//
//        f.setSize(notMatrix.numCols() * 1, notMatrix.numRows() * 1);
//
//        f.setLocation(200,200);
//        f.setVisible(true);
        
    }
    
    public static void printListPair(ArrayList<String[]> pairs, String outputFile) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        for (int i = 0; i < pairs.size(); i++) {
            String[] pair = pairs.get(i);
            writer.write(pair[0]);
            for (int j = 1; j < pair.length; j++) {
                writer.write("\t" + pair[j]);
            }
            writer.write("\n");
        }
        writer.close();
    }
    
}
