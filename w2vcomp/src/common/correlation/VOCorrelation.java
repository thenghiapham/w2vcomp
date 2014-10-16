package common.correlation;

import java.io.IOException;

import space.CompositionSemanticSpace;
import space.RawSemanticSpace;

import composition.WeightedAdditive;

public class VOCorrelation extends TwoWordPhraseCorrelation{

    public VOCorrelation(String dataset) {
        super(dataset);
    }
    
    @Override
    protected String[] getParseComposeData() {
        String[] result = new String[composeData.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = "(VP (VB " + composeData[i][1] + ") (NP " + composeData[i][0] + "))";
//            System.out.println(result[i]);
        }
        return result;
    }
    
    public static void main(String[] args) throws IOException {
        CompositionSemanticSpace compSpace = CompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/old/bnc.cmp-1ttt", true);
        RawSemanticSpace space = RawSemanticSpace.readSpace("/home/thenghiapham/work/project/mikolov/output/old/bnc.bin-1ttt");
        WeightedAdditive add = new WeightedAdditive();
        VOCorrelation anCorrelation = new VOCorrelation("/home/thenghiapham/work/dataset/lapata/vo_lemma.txt");
        System.out.println("vo add: " + anCorrelation.evaluateSpacePearson(space, add));
        System.out.println("vo comp: " + anCorrelation.evaluateSpacePearson(compSpace));
    }

}
