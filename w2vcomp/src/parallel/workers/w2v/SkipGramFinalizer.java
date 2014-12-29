package parallel.workers.w2v;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import common.IOUtils;

import demo.TestConstants;
import parallel.workers.ModelParameters;
import parallel.workers.ParameterFinalizer;
import space.RawSemanticSpace;
import vocab.Vocab;

public class SkipGramFinalizer implements ParameterFinalizer{

    public SkipGramFinalizer() {
        
    }
    
    public void saveNetwork(String outputFile, ModelParameters finalParameters, boolean binary) {
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
            double[][] weights0 = ((SkipGramParameters) finalParameters).getWeights0();
            IOUtils.saveMatrix(outputStream, weights0, binary);
            double[][] weights1 = ((SkipGramParameters) finalParameters).getWeights1();
            IOUtils.saveMatrix(outputStream, weights1, binary);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    @Override
    public void finish(ModelParameters finalParameters) {
        Vocab vocab = new Vocab(RunningConstant.MIN_FREQUENCY);
        vocab.loadVocab(TestConstants.S_VOCABULARY_FILE);
        saveNetwork(TestConstants.S_WORD_MODEL_FILE, finalParameters, true);
        double[][] weights0 = ((SkipGramParameters) finalParameters).getWeights0();
        RawSemanticSpace space = new RawSemanticSpace(vocab, weights0, false);
        space.exportSpace(TestConstants.S_TEXT_VECTOR_FILE);
    }

}
