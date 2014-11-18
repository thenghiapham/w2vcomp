package common.correlation;

import java.io.IOException;

import space.CompositionSemanticSpace;
import space.DiagonalCompositionSemanticSpace;
import space.RawSemanticSpace;
import space.WeightedCompositionSemanticSpace;

import composition.WeightedAdditive;
import demo.TestConstants;

public class VOCorrelation extends TwoWordPhraseCorrelation{

    public VOCorrelation(String dataset) {
        super(dataset);
    }
    
    @Override
    protected String[] getParseComposeData() {
        String[] result = new String[composeData.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = "(@VP (VB " + composeData[i][1] + ") (NP " + composeData[i][0] + "))";
//            System.out.println(result[i]);
        }
        return result;
    }
    
    public static void main(String[] args) throws IOException {
//        CompositionSemanticSpace compSpace = CompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/bnc.cmp", true);
//        DiagonalCompositionSemanticSpace compSpace = DiagonalCompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/dbnc.cmp", true);
//        DiagonalCompositionSemanticSpace compSpace = DiagonalCompositionSemanticSpace.loadCompositionSpace(TestConstants.S_COMPOSITION_FILE, true);
        WeightedCompositionSemanticSpace compSpace = WeightedCompositionSemanticSpace.loadCompositionSpace(TestConstants.S_COMPOSITION_FILE, true);
        RawSemanticSpace space = RawSemanticSpace.readSpace(TestConstants.S_VECTOR_FILE);
        WeightedAdditive add = new WeightedAdditive();
        VOCorrelation voCorrelation = new VOCorrelation(TestConstants.S_VO_FILE);
        System.out.println("vo add: " + voCorrelation.evaluateSpacePearson(space, add));
        System.out.println("vo comp: " + voCorrelation.evaluateSpacePearson(compSpace));
    }

}
