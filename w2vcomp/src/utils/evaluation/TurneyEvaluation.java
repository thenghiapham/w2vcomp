package utils.evaluation;

import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import space.RawSemanticSpace;
import space.SubtituteSpace;
import common.IOUtils;
import composition.BasicComposition;
import composition.WeightedAdditive;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class TurneyEvaluation {
    protected ArrayList<String[]> questions;
    int threadNum = 8;
    
    public TurneyEvaluation(String questionFile) {
        questions = new ArrayList<String[]>();
        ArrayList<String> lines = IOUtils.readFile(questionFile);
        for (String line: lines) {
            String[] question = line.split("\\s");
            for (int i = 0; i < question.length; i++) {
                question[i] = question[i].substring(0, question[i].length() - 2);
            }
            questions.add(question);
        }
    }
    
    public int findRank(RawSemanticSpace space, BasicComposition comp, String[] question) {
        String a = question[0];
        String n = question[2];
        String synonym = question[4];
        SimpleMatrix u = space.getVector(a);
        SimpleMatrix v = space.getVector(n);
        SimpleMatrix p = comp.compose(u, v);
        int rank = space.findRank(p, synonym);
        return rank;
    }
    
    public int findRank(SubtituteSpace space, String[] question) {
        String a = question[0];
        String n = question[1];
        String synonym = question[2];
        SimpleMatrix p = space.getChildSubVector(a, n);
        int rank = space.findRank(p, synonym);
        return rank;
    }
    
    public double medianRank(RawSemanticSpace space, BasicComposition comp) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        int total = questions.size();
        Integer[] ranks = new Integer[total];
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
            threads[index] = new EvaluateThread(ranks, beginIndex, endIndex, space, comp);
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
        
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i] != -1) {
                stats.addValue(ranks[i]);
            }
        }
        return stats.getPercentile(50);
    }
    
    public double medianRank(SubtituteSpace space) {
        return medianRank(space, null);
    }
    
    protected class EvaluateThread extends Thread {
        Integer[] ranks;
        int beginIndex;
        int endIndex;
        BasicComposition comp;
        RawSemanticSpace space;
        public EvaluateThread(Integer[] ranks, int beginIndex, int endIndex, RawSemanticSpace space, BasicComposition comp) {
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            this.ranks = ranks;
            this.space = space;
            this.comp = comp;
        }
        
        public void run() {
            for (int i = beginIndex; i < endIndex; i++) {
                String[] question = questions.get(i);
                try {
                    if (!(space instanceof SubtituteSpace)) {
                        ranks[i] = findRank(space, comp, question);
                    } else {
                        ranks[i] = findRank((SubtituteSpace) space, question);
                    }
                } catch (Exception e) {
                    ranks[i] = -1;
                }
                System.out.println("question " + i + ": " + ranks[i]);
            }
        }
    }
    
    public static void main(String[] args) {
        String questionFile = args[0];
        String binFile = args[1];
        WeightedAdditive comp = new WeightedAdditive();
        TurneyEvaluation eval = new TurneyEvaluation(questionFile);
        RawSemanticSpace space = RawSemanticSpace.readSpace(binFile);
        double addRank = eval.medianRank(space, comp);
        System.out.println("add rank: " + addRank);
    }
}
