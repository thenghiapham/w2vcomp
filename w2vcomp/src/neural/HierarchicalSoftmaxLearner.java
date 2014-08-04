package neural;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

import vocab.Vocab;
import vocab.VocabEntry;

public class HierarchicalSoftmaxLearner {
    protected SimpleMatrix softmaxVector;
    protected Vocab vocab;
    
    public HierarchicalSoftmaxLearner(Vocab vocab) {
        this.vocab = vocab;
    }
    
    // TODO: transpose or not?
    public SimpleMatrix getOutputWeights(String word) {
        VocabEntry wordEntry = vocab.getEntry(word);
        int[] parentIndices = wordEntry.ancestors;
        return SimpleMatrixUtils.getRows(softmaxVector, parentIndices);
    }
    
    // TODO: column or row vector?
    public SimpleMatrix getGoldOutput(String word) {
        VocabEntry wordEntry = vocab.getEntry(word);
        String code = wordEntry.code;
        int codeLength = code.length();
        double[] data = new double[codeLength];
        for (int i = 0; i < codeLength; i++) {
            data[i] = code.charAt(i) - 48;
        }
        return new SimpleMatrix(codeLength, 1, false, data);
    }
}
