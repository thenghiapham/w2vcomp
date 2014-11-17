package common.correlation;

import java.io.IOException;

import composition.WeightedAdditive;
import demo.TestConstants;

//import demo.TestConstants;

import space.CompositionSemanticSpace;
import space.DiagonalCompositionSemanticSpace;
import space.RawSemanticSpace;
import space.WeightedCompositionSemanticSpace;

public class NNCorrelation extends TwoWordPhraseCorrelation{

    public NNCorrelation(String dataset) {
        super(dataset);
    }

    @Override
    protected String[] getParseComposeData() {
        String[] result = new String[composeData.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = "(@NP (NN " + composeData[i][0] + ") (NN " + composeData[i][1] + "))";
//            System.out.println(result[i]);
        }
        return result;
    }
    
    public static void main(String[] args) throws IOException {
//        DiagonalCompositionSemanticSpace compSpace = DiagonalCompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/dbnc.cmp", true);
//        DiagonalCompositionSemanticSpace compSpace = DiagonalCompositionSemanticSpace.loadCompositionSpace(TestConstants.S_COMPOSITION_FILE, true);
        WeightedCompositionSemanticSpace compSpace = WeightedCompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/wbnc.cmp", true);
//        CompositionSemanticSpace compSpace = CompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/bnc.cmp", true);
        NNCorrelation nnCorrelation = new NNCorrelation("/home/thenghiapham/work/dataset/lapata/nn_lemma.txt");
//        MenCorrelation men = new MenCorrelation(TestConstants.S_MEN_FILE);
        RawSemanticSpace space = RawSemanticSpace.readSpace(TestConstants.S_VECTOR_FILE);
        WeightedAdditive add = new WeightedAdditive();
        System.out.println("nn add: " + nnCorrelation.evaluateSpacePearson(space, add));
        System.out.println("nn comp: " + nnCorrelation.evaluateSpacePearson(compSpace));
//        System.out.println("men: " + men.evaluateSpacePearson(space));
    }

}
