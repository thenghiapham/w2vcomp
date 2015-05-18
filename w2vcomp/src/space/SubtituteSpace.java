package space;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.ejml.simple.SimpleMatrix;

import vocab.Vocab;

import common.IOUtils;
import common.exception.ValueException;

public class SubtituteSpace extends RawSemanticSpace {
    int threadNum = 8;
    public static final double THRESHOLD = 0.6;
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
    
    public ArrayList<String> getAfromN(String noun) {
        ArrayList<String> result = new ArrayList<>();
        String suffix = "_" + noun;
        for (String an: words) {
            if (an.endsWith(suffix)) {
                result.add(an.split("_")[0]);
            }
        }
        return result;
    }
    
    public String getBestFitNoun(String adj, String noun) {
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
//        System.out.println("Best fit: " + bestNoun);
        return bestNoun;
    }
    
    public String getBestFitAdj(String adj, String noun) {
        ArrayList<String> possibleAdjs = getAfromN(noun);
        RawSemanticSpace nSpace = this.getSubSpace(possibleAdjs);
        Neighbor[] neighborAdjs = nSpace.getNeighbors(this.getVector(adj), 10);
//        System.out.println("Nearest neighbors of \"" + noun + "\" are: ");
//        for (int i = 0; i < neighborNouns.length; i++) {
//            System.out.println(neighborNouns[i].word + " " + neighborNouns[i].sim);
//        }
        String bestAdj = neighborAdjs[0].word;
        if (adj.equals(bestAdj)) {
            bestAdj  = neighborAdjs[1].word;
        }
//        System.out.println("Best fit: " + bestAdj);
        return bestAdj;
    }
    
    public SimpleMatrix getChildSubVector(String adj, String noun) {
        String bestNoun = getBestFitNoun(adj, noun);
        if (getSim(bestNoun, noun) >= THRESHOLD) {
            SimpleMatrix nV = this.getVector(noun);
            nV = nV.scale(1 / nV.normF());
            SimpleMatrix bestNV = this.getVector(bestNoun);
            bestNV = bestNV.scale(1 / bestNV.normF());
            SimpleMatrix anV = this.getVector(adj + "_" + bestNoun);
            anV = anV.scale(1 / anV.normF());
            
            anV = anV.minus(bestNV).plus(nV);
            return anV;
        } else {
            SimpleMatrix a = getVector(adj);
            SimpleMatrix n = getVector(noun);
            return a.plus(n);
        }
    }
    
    public SimpleMatrix getHeadSubVector(String adj, String noun) {
        String bestAdj = getBestFitAdj(adj, noun);
        if (getSim(bestAdj, adj) >= THRESHOLD) {
            SimpleMatrix aV = this.getVector(adj);
            aV = aV.scale(1 / aV.normF());
          
            SimpleMatrix bestAV = this.getVector(bestAdj);
            bestAV = bestAV.scale(1 / bestAV.normF());
            SimpleMatrix anV = this.getVector(bestAdj + "_" + noun);
            anV = anV.scale(1 / anV.normF());
          
            anV = anV.minus(bestAV).plus(anV);
            return anV;
        } else {
            SimpleMatrix a = getVector(adj);
            SimpleMatrix n = getVector(noun);
            return a.plus(n);
        }
    }
    
    public void printNeighbor(String adj, String noun) {
        SimpleMatrix predictedAnV = getChildSubVector(adj, noun);
        Neighbor[] neighbors = getNeighbors(predictedAnV, 10);
        System.out.println("Nearest neighbors of \"" + adj + "_" + noun + "\" are: ");
        for (int i = 0; i < neighbors.length; i++) {
            System.out.println(neighbors[i].word + " " + neighbors[i].sim);
        }
        
        SimpleMatrix a = getVector(adj);
        SimpleMatrix n = getVector(noun);
        neighbors = getNeighbors(a.plus(n), 10);
        System.out.println("Nearest neighbors of \"" + adj + "_" + noun + "\" with additive are: ");
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
    
    public int findRankChildSub(String adj, String noun) {
        SimpleMatrix anV = getChildSubVector(adj, noun);
        return findRank(anV, adj + "_" + noun);
    }
    
    public int findRankHeadSub(String adj, String noun) {
        SimpleMatrix anV = getHeadSubVector(adj, noun);
        return findRank(anV, adj + "_" + noun);
    }
    
    public int findRankAdd(String adj, String noun) {
        SimpleMatrix a = getVector(adj);
        SimpleMatrix n = getVector(noun);
        return findRank(a.plus(n), adj + "_" + noun);
    }
    
    public void evaluateANs(String anFile) {
        ArrayList<String> ans = IOUtils.readFile(anFile);
        DescriptiveStatistics nStats = new DescriptiveStatistics();
        DescriptiveStatistics aStats = new DescriptiveStatistics();
        DescriptiveStatistics addStats = new DescriptiveStatistics();
        
        Integer[] nRank = new Integer[ans.size()];
        Integer[] aRank = new Integer[ans.size()];
        Integer[] addRank = new Integer[ans.size()];
        // create threads;
        int total = ans.size();
        int div = total / threadNum;
        int mod = total % threadNum;
        EvaluateThread[] threads = new EvaluateThread[threadNum];
        for (int index = 0; index < threadNum; index++) {
            int beginIndex;
            int endIndex;
            if (index < mod) {
                beginIndex = (div + 1) * index;
                endIndex = (div + 1) * (index + 1);
            } else {
                beginIndex = div * index + mod;
                endIndex = div * (index + 1) + mod;
            }
            threads[index] = new EvaluateThread(ans, nRank, aRank, addRank, beginIndex, endIndex);
            // start threads;
            threads[index].start();
        }
        
     // join threads;
        for (int index = 0; index < threadNum; index++) {
            try {
                threads[index].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        for (int i = 0; i < nRank.length; i++) {
            if (nRank[i] != -1) {
                nStats.addValue(nRank[i]);
                aStats.addValue(aRank[i]);
                addStats.addValue(addRank[i]);
            }
        }
        IOUtils.printToFile("/home/thenghiapham/nRank.txt", nRank);
        IOUtils.printToFile("/home/thenghiapham/aRank.txt", aRank);
        IOUtils.printToFile("/home/thenghiapham/addRank.txt", addRank);
        System.out.println("******************************");
        System.out.println("Summary");
        System.out.println("rank n sub: " + nStats.getPercentile(50));
        System.out.println("rank a sub: " + aStats.getPercentile(50));
        System.out.println("rank add: " + addStats.getPercentile(50));
    }
    
    public static void main(String[] args) {
        String spaceFile = args[0];
        String vocFile = args[1];
        String testFile = args[2];
        
        int minFreq = 20;
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
        sSpace.evaluateANs(testFile);
    }
    
    protected class EvaluateThread extends Thread {
        ArrayList<String> quesions; 
        Integer[] nRank;
        Integer[] aRank;
        Integer[] addRank;
        int beginIndex;
        int endIndex;
        NormalizedSemanticSpace space;
        public EvaluateThread(ArrayList<String> questions, 
                Integer[] nRank, Integer[] aRank, Integer[] addRank, int beginIndex, int endIndex) {
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            this.quesions = questions;
            this.nRank = nRank;
            this.aRank = aRank;
            this.addRank = addRank;
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
        }
        
        public void run() {
            for (int i = beginIndex; i < endIndex; i++) {
                String question = quesions.get(i);
                String[] elements = question.split("_");
                try {
                    nRank[i] = findRankChildSub(elements[0], elements[1]);
                    aRank[i] = findRankHeadSub(elements[0], elements[1]);
                    addRank[i] = findRankAdd(elements[0], elements[1]);
                } catch (Exception e) {
                    nRank[i] = -1;
                    aRank[i] = -1;
                    addRank[i] = -1;
                    e.printStackTrace();
                }
            }
        }
    }
}
