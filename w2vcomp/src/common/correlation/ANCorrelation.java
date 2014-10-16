package common.correlation;

import java.io.IOException;

import composition.WeightedAdditive;

import space.CompositionSemanticSpace;
import space.RawSemanticSpace;

public class ANCorrelation extends TwoWordPhraseCorrelation{

    public ANCorrelation(String dataset) {
        super(dataset);
    }

    @Override
    protected String[] getParseComposeData() {
        String[] result = new String[composeData.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = "(NP (JJ " + composeData[i][0] + ") (NN " + composeData[i][1] + "))";
//            System.out.println(result[i]);
        }
        return result;
    }
    
    public static void main(String[] args) throws IOException {
        CompositionSemanticSpace compSpace = CompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/old/bnc.cmp-1ttt", true);
        RawSemanticSpace space = RawSemanticSpace.readSpace("/home/thenghiapham/work/project/mikolov/output/old/bnc.bin-1ttt");
        WeightedAdditive add = new WeightedAdditive();
        ANCorrelation anCorrelation = new ANCorrelation("/home/thenghiapham/work/project/mikolov/an_ml/an_ml_lemma.txt");
        System.out.println("an add: " + anCorrelation.evaluateSpacePearson(space, add));
        System.out.println("an comp: " + anCorrelation.evaluateSpacePearson(compSpace));
    }

}
