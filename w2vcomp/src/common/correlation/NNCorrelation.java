package common.correlation;

import java.io.IOException;

import space.CompositionSemanticSpace;

public class NNCorrelation extends TwoWordPhraseCorrelation{

    public NNCorrelation(String dataset) {
        super(dataset);
    }

    @Override
    protected String[] getParseComposeData() {
        String[] result = new String[composeData.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = "(NP (NN " + composeData[i][0] + ") (NN " + composeData[i][1] + "))";
            System.out.println(result[i]);
        }
        return result;
    }
    
    public static void main(String[] args) throws IOException {
        CompositionSemanticSpace space = CompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/bnc.cmp3tft", true);
        NNCorrelation anCorrelation = new NNCorrelation("/home/thenghiapham/work/project/mikolov/an_ml/an_ml_lemma.txt");
        System.out.println("an: " + anCorrelation.evaluateSpacePearson(space));
    }

}
