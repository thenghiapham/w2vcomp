package space;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.ejml.simple.SimpleMatrix;

import vocab.Vocab;

import common.SimpleMatrixUtils;
import common.exception.ValueException;

public class SubtituteSpace extends RawSemanticSpace {

    public SubtituteSpace(String[] words, double[][] vectors) {
        super(words, vectors);
        // TODO Auto-generated constructor stub
    }
    
    public ArrayList<String> getNfromA(String adj) {
        ArrayList<String> result = new ArrayList<>();
        String prefix = adj + "_";
        for (String an: words) {
            if (an.startsWith(prefix)) {
                result.add(an.split("_")[1]);
            }
        }
        return result;
    }
    
    public String getBestFit(String adj, String noun) {
        ArrayList<String> possibleNouns = getNfromA(adj);
        RawSemanticSpace nSpace = this.getSubSpace(possibleNouns);
        Neighbor[] neighborNouns = nSpace.getNeighbors(this.getVector(noun), 10);
//        System.out.println("Nearest neighbors of \"" + noun + "\" are: ");
//        for (int i = 0; i < neighborNouns.length; i++) {
//            System.out.println(neighborNouns[i].word + " " + neighborNouns[i].sim);
//        }
        String bestNoun = neighborNouns[0].word;
        if (noun.equals(bestNoun)) {
            bestNoun  = neighborNouns[1].word;
        }
        System.out.println("Best fit: " + bestNoun);
        return bestNoun;
    }
    
    public SimpleMatrix getVector(String adj, String noun) {
//        SimpleMatrix aV = this.getVector(adj);
        SimpleMatrix nV = this.getVector(noun);
        nV = nV.scale(1 / nV.normF());
        String bestNoun = getBestFit(adj, noun);
        SimpleMatrix bestNV = this.getVector(bestNoun);
        bestNV = bestNV.scale(1 / bestNV.normF());
        SimpleMatrix anV = this.getVector(adj + "_" + bestNoun);
        anV = anV.scale(1 / anV.normF());
        
        anV = anV.minus(bestNV).plus(nV);
//        System.out.println(SimpleMatrixUtils.cosine(nV, bestNV));
//        System.out.println(SimpleMatrixUtils.cosine(nV, anV));
//        System.out.println(SimpleMatrixUtils.cosine(bestNV, anV));
        return anV;
    }
    
    public void printNeighbor(String adj, String noun) {
        SimpleMatrix predictedAnV = getVector(adj, noun);
        Neighbor[] neighbors = getNeighbors(predictedAnV, 10);
        System.out.println("Nearest neighbors of \"" + adj + "_" + noun + "\" are: ");
        for (int i = 0; i < neighbors.length; i++) {
            System.out.println(neighbors[i].word + " " + neighbors[i].sim);
        }
    }
    
    public int findRank(SimpleMatrix v, String word) {
        double[] rawVector = v.getMatrix().getData();
        Neighbor[] neighbors = new Neighbor[words.length];
        int neighborIndex = 0;
        for (int i = 0; i < words.length; i++) {
            neighbors[neighborIndex] = new Neighbor(words[i],
                    Similarity.cosine(rawVector, vectors[i]));
            neighborIndex++;
        }
        Arrays.sort(neighbors, Neighbor.NeighborComparator);
        neighborIndex = 0;
        for (int i = 0; i < words.length; i++) {
            if (neighbors[i].word.equals(word)) 
                return i;
        }
        throw new ValueException("Cannot find the word " + word + " in the vocabulary");
    }
    
    public static void main(String[] args) {
        String spaceFile = args[0];
        String vocFile = args[1];
        int minFreq = 30;
        RawSemanticSpace space = RawSemanticSpace.readSpace(spaceFile);
        Vocab vocab = new Vocab(minFreq);
        vocab.loadVocab(vocFile);
        int vocabSize = vocab.getVocabSize();
        String[] words = new String[vocabSize];
        for (int i = 0; i < vocabSize; i++) {
            words[i] = vocab.getEntry(i).word;
        }
        space = space.getSubSpace(Arrays.asList(words));
        SubtituteSpace sSpace = new SubtituteSpace(space.getWords(), space.getVectors());
        
        
        System.out.println("Enter a word or EXIT to exit");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    System.in));
            String input;
            while ((input = br.readLine()) != null) {
                if (input.equals("EXIT")) {
                    break;
                } else {
                    String[] elements = input.split("\\s");
                    sSpace.printNeighbor(elements[0], elements[1]);
                }
            }

        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}
