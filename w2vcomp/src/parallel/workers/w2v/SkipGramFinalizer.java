package parallel.workers.w2v;

import demo.TestConstants;
import parallel.workers.ModelParameters;
import parallel.workers.ParameterFinalizer;
import space.RawSemanticSpace;
import vocab.Vocab;

public class SkipGramFinalizer implements ParameterFinalizer{

    public SkipGramFinalizer() {
        
    }
    
    @Override
    public void finish(ModelParameters finalParameters) {
        // TODO Auto-generated method stub
        System.out.println("Didnot come here");
//        Vocab vocab = new Vocab(RunningConstant.MIN_FREQUENCY);
//        vocab.loadVocab(TestConstants.S_VOCABULARY_FILE);
//        double[][] weights0 = ((SkipGramParameters) finalParameters).weights0;
//        RawSemanticSpace space = new RawSemanticSpace(vocab, weights0, false);
//        space.exportSpace(TestConstants.S_TEXT_VECTOR_FILE);
    }

}