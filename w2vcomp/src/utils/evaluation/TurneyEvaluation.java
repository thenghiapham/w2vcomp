package utils.evaluation;

import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import space.RawSemanticSpace;
import space.SubtituteSpace;
import composition.BasicComposition;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class TurneyEvaluation {
    protected ArrayList<String[]> questions;
    public double medianRank(RawSemanticSpace space, BasicComposition comp) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (String[] question: questions) {
            String a = question[0];
            String n = question[1];
            String synonym = question[2];
            SimpleMatrix u = space.getVector(a);
            SimpleMatrix v = space.getVector(n);
            SimpleMatrix p = comp.compose(u, v);
            int rank = space.findRank(p, synonym);
            stats.addValue(rank);
        }
        return stats.getPercentile(50);
    }
    
    public double medianRank(SubtituteSpace space) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (String[] question: questions) {
            String a = question[0];
            String n = question[1];
            String synonym = question[2];
            SimpleMatrix p = space.getChildSubVector(a, n);
            int rank = space.findRank(p, synonym);
            stats.addValue(rank);
        }
        return stats.getPercentile(50);
    }
}
