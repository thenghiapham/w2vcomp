package utils;

import space.WeightedCompositionSemanticSpace;

import common.correlation.ANCorrelation;
import common.correlation.NNCorrelation;
import common.correlation.ParsedPhraseCorrelation;
import common.correlation.VOCorrelation;

import demo.TestConstants;

public class WeightedEvaluation {
    public static void main(String[] args) {
        String compFile = TestConstants.S_COMPOSITION_FILE;
        if (args.length > 0) {
            compFile = args[0];
        }
        WeightedCompositionSemanticSpace space = WeightedCompositionSemanticSpace.loadCompositionSpace(compFile, true);
        ANCorrelation an = new ANCorrelation(TestConstants.S_AN_FILE);
        NNCorrelation nn = new NNCorrelation(TestConstants.S_NN_FILE);
        VOCorrelation vo = new VOCorrelation(TestConstants.S_VO_FILE);
        ParsedPhraseCorrelation sick = new ParsedPhraseCorrelation(TestConstants.S_SICK_FILE);
        
//        System.out.println("men: " + men.evaluateSpacePearson(space));
        System.out.println("an cmp: " + an.evaluateSpacePearson(space));
        System.out.println("nn cmp: " + nn.evaluateSpacePearson(space));
        System.out.println("vo cmp: " + vo.evaluateSpacePearson(space));
        System.out.println("sick cmp: " + sick.evaluateSpacePearson(space));
        
    }
}
