package parallel.workers.w2v;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import common.MathUtils;

import parallel.workers.ModelParameters;

public class SkipGramParameters implements ModelParameters {

    // for sending back to the estimator
    double                    alpha;

    protected double[][]      weights0, weights1;

    protected int[]           updatesCount0, updatesCount1;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SkipGramParameters(double alpha, double[][] weights0,
            double[][] weights1) {
        this.alpha = alpha;
        this.weights0 = weights0;
        this.weights1 = weights1;
        this.updatesCount0 = new int[this.weights0.length];
        this.updatesCount1 = new int[this.weights1.length];
    }

    public SkipGramParameters(SkipGramParameters ot) {
        this.alpha = ot.alpha;
        this.weights0 = MathUtils.deepCopy(ot.weights0);
        this.weights1 = MathUtils.deepCopy(ot.weights1);
    }

    public SkipGramParametersDelta getDelta(SkipGramParameters oldParams,
            long deltaCount, int updateThreshold) {
        int deltaSize0 = 0, deltaSize1 = 0;
        for (int c : updatesCount0) {
            if (c >= updateThreshold) {
                deltaSize0 += 1;
            }
        }
        for (int c : updatesCount1) {
            if (c >= updateThreshold) {
                deltaSize1 += 1;
            }
        }

        double[][] deltaWeights0, deltaWeights1;

        deltaWeights0 = new double[deltaSize0][weights0[0].length];
        deltaWeights1 = new double[deltaSize1][weights1[0].length];

        List<Integer> words_ids0 = new ArrayList<Integer>();
        for (int i = 0; i < weights0.length; i++) {
            if (updatesCount0[i] >= updateThreshold) {
                words_ids0.add(i);
                for (int j = 0; j < weights0[i].length; j++)
                    deltaWeights0[words_ids0.size()-1][j] = weights0[i][j]
                            - oldParams.weights0[i][j];
            }
        }
        List<Integer> words_ids1 = new ArrayList<Integer>();
        for (int i = 0; i < weights1.length; i++) {
            if (updatesCount1[i] >= updateThreshold) {
                words_ids1.add(i);
                for (int j = 0; j < weights1[i].length; j++)
                    deltaWeights1[words_ids1.size()-1][j] = weights1[i][j]
                            - oldParams.weights1[i][j];
            }
        }

        return new SkipGramParametersDelta(alpha, deltaCount, deltaWeights0,
                deltaWeights1, words_ids0, words_ids1);

    }

    public void applyDelta(SkipGramParametersDelta deltaParams) {
        alpha = deltaParams.alpha;
        for (int i = 0; i < deltaParams.words_ids0.size(); i++) {
            weights0[deltaParams.words_ids0.get(i)] = Arrays.copyOf(
                    deltaParams.weights0[i], deltaParams.weights0[i].length);
            // Reset counts for those words
            updatesCount0[deltaParams.words_ids0.get(i)] = 0;
        }
        for (int i = 0; i < deltaParams.words_ids1.size(); i++) {
            weights1[deltaParams.words_ids1.get(i)] = Arrays.copyOf(
                    deltaParams.weights1[i], deltaParams.weights1[i].length);
            // Reset counts for those words
            updatesCount1[deltaParams.words_ids1.get(i)] = 0;

        }
    }

}
