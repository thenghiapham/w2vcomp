package common.correlation;

import java.io.IOException;

import org.ejml.simple.SimpleMatrix;

import composition.WeightedAdditive;
import demo.TestConstants;

import space.RawSemanticSpace;
import space.WeightedCompositionSemanticSpace;

public class ANCorrelation extends TwoWordPhraseCorrelation{

    public ANCorrelation(String dataset) {
        super(dataset);
    }

    @Override
    protected String[] getParseComposeData() {
        String[] result = new String[composeData.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = "(@NP (JJ " + composeData[i][0] + ") (@NP " + composeData[i][1] + "))";
//            System.out.println(result[i]);
        }
        return result;
    }
    
    public static void main(String[] args) throws IOException {
//        CompositionSemanticSpace compSpace = CompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/bnc.cmp", true);
//        DiagonalCompositionSemanticSpace compSpace = DiagonalCompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/dbnc.cmp", true);
        WeightedCompositionSemanticSpace compSpace = WeightedCompositionSemanticSpace.loadCompositionSpace(TestConstants.S_COMPOSITION_FILE, true);
//        DiagonalCompositionSemanticSpace compSpace = DiagonalCompositionSemanticSpace.loadCompositionSpace(TestConstants.S_COMPOSITION_FILE, true);
//        CompositionSemanticSpace compSpace = CompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/tanh/bnc.cmp", true);
//        RawSemanticSpace space = RawSemanticSpace.readSpace("/home/thenghiapham/work/project/mikolov/output/bnc.bin");
        SimpleMatrix vector = compSpace.getConstructionMatrix("@NP JJ @NP");
        System.out.println(vector);
        RawSemanticSpace space = RawSemanticSpace.readSpace(TestConstants.S_VECTOR_FILE);
        WeightedAdditive add = new WeightedAdditive();
        ANCorrelation anCorrelation = new ANCorrelation(TestConstants.S_AN_FILE);
        System.out.println("an add: " + anCorrelation.evaluateSpacePearson(space, add));
        System.out.println("an comp: " + anCorrelation.evaluateSpacePearson(compSpace));
    }

}

