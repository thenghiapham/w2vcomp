package utils;

import java.io.File;

import space.RawSemanticSpace;
import common.correlation.ANCorrelation;
import common.correlation.MenCorrelation;
import common.correlation.NNCorrelation;
import common.correlation.ParsedPhraseCorrelation;
import common.correlation.PhraseCorrelation;
import common.correlation.VOCorrelation;
import composition.WeightedAdditive;
import demo.TestConstants;

public class AddEvaluation {
    public static void main(String[] args) {
//        String vectorFile = TestConstants.S_VECTOR_FILE.replace(".bin", "_cbow.bin");
        String vectorFile = TestConstants.S_VECTOR_FILE;
//        String vectorFile = TestConstants.S_WORD_VECTOR_FILE.replace("size", "40");
        if (args.length > 0) {
            vectorFile = args[0];
        }
        WeightedAdditive add = new WeightedAdditive();
        RawSemanticSpace space = null;
        if (vectorFile.endsWith("bin")) {
            String exportFile = vectorFile.replace(".bin", ".txt");
            space = RawSemanticSpace.readSpace(vectorFile);
            if (!(new File(exportFile)).exists()) {
                space.exportSpace(exportFile, false);
            }
        } else {
            String exportFile = vectorFile.replace(".txt", ".bin");
            space = RawSemanticSpace.importSpace(vectorFile);
            if (!(new File(exportFile)).exists()) {
                space.saveSpace(exportFile);
            }
        }
        MenCorrelation men = new MenCorrelation(TestConstants.S_MEN_FILE);
        MenCorrelation menTest = new MenCorrelation(TestConstants.S_MEN_TEST_FILE);
        MenCorrelation simLex = new MenCorrelation(TestConstants.SIMLEX);
        MenCorrelation wordSim = new MenCorrelation(TestConstants.WORD_SIM);
        MenCorrelation wordSimR = new MenCorrelation(TestConstants.WORD_SIM_RELATED);
        MenCorrelation wordSimS = new MenCorrelation(TestConstants.WORD_SIM_SIM);
        ANCorrelation an = new ANCorrelation(TestConstants.S_AN_FILE);
        NNCorrelation nn = new NNCorrelation(TestConstants.S_NN_FILE);
//        SVCorrelation sv = new SVCorrelation(TestConstants.S_SV_FILE);
        VOCorrelation vo = new VOCorrelation(TestConstants.S_VO_FILE);
        PhraseCorrelation onwn1 = new PhraseCorrelation(TestConstants.S_ONWN1_FILE);
        PhraseCorrelation onwn2 = new PhraseCorrelation(TestConstants.S_ONWN2_FILE);
        ParsedPhraseCorrelation sick = new ParsedPhraseCorrelation(TestConstants.S_SICK_FILE);
        ParsedPhraseCorrelation sicktest = new ParsedPhraseCorrelation(TestConstants.S_SICK_TEST_FILE);
        
        System.out.println("men: " + men.evaluateSpaceSpearman(space));
        System.out.println("men test: " + menTest.evaluateSpaceSpearman(space));
        System.out.println("lex: " + simLex.evaluateSpacePearson(space));
        System.out.println("ws: " + wordSim.evaluateSpacePearson(space));
        System.out.println("wsr: " + wordSimR.evaluateSpacePearson(space));
        System.out.println("wss: " + wordSimS.evaluateSpacePearson(space));
        
//        System.out.println("men: " + men.evaluateSpaceSpearman(space));
//        System.out.println("men test: " + menTest.evaluateSpaceSpearman(space));
//        System.out.println("lex: " + simLex.evaluateSpaceSpearman(space));
//        System.out.println("ws: " + wordSim.evaluateSpaceSpearman(space));
//        System.out.println("wsr: " + wordSimR.evaluateSpaceSpearman(space));
//        System.out.println("wss: " + wordSimS.evaluateSpaceSpearman(space));
        
        System.out.println("an add: " + an.evaluateSpacePearson(space, add));
        System.out.println("nn add: " + nn.evaluateSpacePearson(space, add));
//        System.out.println("sv add: " + sv.evaluateSpacePearson(space, add));
        System.out.println("vo add: " + vo.evaluateSpacePearson(space, add));
        System.out.println("onwn1 add: " + onwn1.evaluateSpacePearson(space, add));
        System.out.println("onwn2 add: " + onwn2.evaluateSpacePearson(space, add));
        System.out.println("sick add: " + sick.evaluateSpacePearson(space, add));
        System.out.println("sick test add: " + sicktest.evaluateSpacePearson(space, add));
        
    }
}
