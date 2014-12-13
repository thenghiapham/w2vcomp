package parallel.workers.w2v;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import parallel.workers.ModelParameters;

public class SkipGramParameters implements ModelParameters {

    private SkipGramParametersSubset  parameters;

    protected int[]           updatesCount0, updatesCount1;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SkipGramParameters(double alpha, double[][] weights0,
            double[][] weights1) {
        parameters = new SkipGramParametersSubset(alpha, weights0, weights1,
                null, null);
        this.updatesCount0 = new int[this.getWeights0().length];
        this.updatesCount1 = new int[this.getWeights1().length];
    }

    public SkipGramParameters(SkipGramParameters ot) {
        this.parameters = new SkipGramParametersSubset(ot.parameters);
    }

    /**
     * Get the difference between two states of the parameters
     * 
     * @param oldParams
     *            Previous state of the parameters
     * @param deltaCount
     *            A quantification of the time lapse that has passed between the
     *            two states
     * @param updateThreshold
     *            The threshold for the number of updates above which a row is
     *            included in the delta
     */
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

        double[][] deltaWeights0 = new double[deltaSize0][getWeights0()[0].length];
        double[][] deltaWeights1 = new double[deltaSize1][getWeights1()[0].length];

        List<Integer> words_ids0 = new ArrayList<Integer>();
        for (int i = 0; i < getWeights0().length; i++) {
            if (updatesCount0[i] >= updateThreshold) {
                words_ids0.add(i);
                for (int j = 0; j < getWeights0()[i].length; j++)
                    deltaWeights0[words_ids0.size() - 1][j] = getWeights0()[i][j]
                            - oldParams.getWeights0()[i][j];
            }
        }
        List<Integer> words_ids1 = new ArrayList<Integer>();
        for (int i = 0; i < getWeights1().length; i++) {
            if (updatesCount1[i] >= updateThreshold) {
                words_ids1.add(i);
                for (int j = 0; j < getWeights1()[i].length; j++)
                    deltaWeights1[words_ids1.size() - 1][j] = getWeights1()[i][j]
                            - oldParams.getWeights1()[i][j];
            }
        }

        return new SkipGramParametersDelta(deltaCount,
                new SkipGramParametersSubset(getAlpha(), deltaWeights0,
                        deltaWeights1, words_ids0, words_ids1));

    }

    /**
     * Extracts a subset of these parameters
     * 
     * @param set_words_ids0
     *            weight0 rows to extract
     * @param set_words_ids1
     *            weight1 rows to extract
     */
    public SkipGramParametersSubset getSubset(
            Collection<Integer> set_words_ids0,
            Collection<Integer> set_words_ids1) {
        double[][] deltaWeights0 = new double[set_words_ids0.size()][getWeights0()[0].length];
        List<Integer> words_ids0 = new ArrayList<Integer>();
        for (Integer id : set_words_ids0) {
            words_ids0.add(id);
            for (int j = 0; j < getWeights0()[id].length; j++)
                deltaWeights0[words_ids0.size() - 1][j] = getWeights0()[id][j];
        }

        double[][] deltaWeights1 = new double[set_words_ids1.size()][getWeights1()[0].length];
        List<Integer> words_ids1 = new ArrayList<Integer>();
        for (Integer id : set_words_ids1) {
            words_ids1.add(id);
            for (int j = 0; j < getWeights1()[id].length; j++)
                deltaWeights1[words_ids1.size() - 1][j] = getWeights1()[id][j];
        }

        return new SkipGramParametersSubset(getAlpha(), deltaWeights0,
                deltaWeights1, words_ids0, words_ids1);
    }

    /**
     * Replace my parameters with those in the given subset
     * 
     * @param subsetParams
     *            a subset of parameters to apply
     */
    public void setSubset(SkipGramParametersSubset subsetParams) {
        setAlpha(subsetParams.getAlpha());
        for (int i = 0; i < subsetParams.getWordsIds0().size(); i++) {
            getWeights0()[subsetParams.getWordsIds0().get(i)] = Arrays.copyOf(
                    subsetParams.getWeights0()[i],
                    subsetParams.getWeights0()[i].length);
            // Reset counts for those words
            updatesCount0[subsetParams.getWordsIds0().get(i)] = 0;
        }
        for (int i = 0; i < subsetParams.getWordsIds1().size(); i++) {
            getWeights1()[subsetParams.getWordsIds1().get(i)] = Arrays.copyOf(
                    subsetParams.getWeights1()[i],
                    subsetParams.getWeights1()[i].length);
            // Reset counts for those words
            updatesCount1[subsetParams.getWordsIds1().get(i)] = 0;

        }
    }

    public double[][] getWeights0() {
        return parameters.getWeights0();
    }

    public void setWeights0(double[][] weights0) {
        this.parameters.setWeights0(weights0);
    }

    protected double[][] getWeights1() {
        return this.parameters.getWeights1();
    }

    protected void setWeights1(double[][] weights1) {
        this.parameters.setWeights1(weights1);
    }

    double getAlpha() {
        return this.parameters.getAlpha();
    }

    void setAlpha(double alpha) {
        this.parameters.setAlpha(alpha);
    }

}
