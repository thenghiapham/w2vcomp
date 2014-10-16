package common.correlation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import space.CompositionSemanticSpace;
import space.RawSemanticSpace;

import common.IOUtils;
import composition.WeightedAdditive;

public class SVCorrelation extends TwoWordPhraseCorrelation{

    public SVCorrelation(String dataset) {
        super(dataset);
    }
    
    @Override
    public void readDataset(String dataset) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        double[] golds = new double[data.size()];
        String[][] phrasePairs = new String[data.size()][2];
        HashSet<String> phraseSet = new HashSet<>();
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split(" ");
            String phrase1 = elements[1] + "_" + elements[0];
            String phrase2 = elements[1] + "_" + elements[2];
            phrasePairs[i][0] = phrase1;
            phrasePairs[i][1] = phrase2;
            phraseSet.add(phrase1);
            phraseSet.add(phrase2);
            golds[i] = Double.parseDouble(elements[3]);
        }
        correlation = new MenCorrelation(phrasePairs, golds);
        composeData = TwoWordPhraseCorrelation.convertComposeData(phraseSet);
    }

    @Override
    protected String[] getParseComposeData() {
        String[] result = new String[composeData.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = "(S (VP " + composeData[i][1] + ") (NP " + composeData[i][0] + "))";
//            System.out.println(result[i]);
        }
        return result;
    }
    
    public static void main(String[] args) throws IOException {
        CompositionSemanticSpace compSpace = CompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/old/bnc.cmp-1ttt", true);
        RawSemanticSpace space = RawSemanticSpace.readSpace("/home/thenghiapham/work/project/mikolov/output/old/bnc.bin-1ttt");
        WeightedAdditive add = new WeightedAdditive();
        SVCorrelation anCorrelation = new SVCorrelation("/home/thenghiapham/work/dataset/lapata/sv_lemma.txt");
        System.out.println("an add: " + anCorrelation.evaluateSpacePearson(space, add));
        System.out.println("an comp: " + anCorrelation.evaluateSpacePearson(compSpace));
    }

}
