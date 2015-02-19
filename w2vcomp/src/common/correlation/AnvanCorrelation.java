package common.correlation;

import java.io.IOException;
import java.util.ArrayList;

import space.RawSemanticSpace;
import space.WeightedCompositionSemanticSpace;

import common.IOUtils;
import composition.WeightedAdditive;

public class AnvanCorrelation extends ParsedPhraseCorrelation{
    public static final String parseTemplate = "(S (NP (JJ a1) (NN n1)) (VP (VB v) (NP (JJ a2) (NN n2))))";

    public AnvanCorrelation(String dataset) {
        ArrayList<String> rawData = IOUtils.readFile(dataset);
        ArrayList<String> parseData = convertRawToParse(rawData);
        convertRawData(parseData);
        // TODO Auto-generated constructor stub
    }
    
    public ArrayList<String> convertRawToParse(ArrayList<String> rawData) {
        ArrayList<String> parseData = new ArrayList<>();
        for (String sentence: rawData) {
            String[] e = sentence.split(" ");
            if (e.length != 9) {
                System.out.println("blah");
                System.exit(1);
            }
            String parse1 = toParse(e[2], e[3], e[4], e[6], e[7]);
            String parse2 = toParse(e[2], e[3], e[5], e[6], e[7]);
            String parsePair = parse1 + "\t" + parse2 + "\t" + e[8];
            parseData.add(parsePair);
//            System.out.println(parsePair);
        }
        return parseData;
    }
    
    public static String toParse(String a1, String n1, String v, String a2, String n2) {
        String result = parseTemplate.replaceAll("v", v);
        result = result.replaceAll("n1", n1);
        result = result.replaceAll("a2", a2);
        result = result.replaceAll("n2", n2);
        result = result.replaceAll("a1", a1);
        return result;
    }
    
    public static void main(String[] args) throws IOException {
        WeightedCompositionSemanticSpace compSpace = WeightedCompositionSemanticSpace.loadCompositionSpace("/home/thenghiapham/work/project/mikolov/output/wbnc.cmp", true);
        RawSemanticSpace space = RawSemanticSpace.readSpace("/home/thenghiapham/work/project/mikolov/output/bnc.bin");
        WeightedAdditive add = new WeightedAdditive();
        AnvanCorrelation anvanCorrelation = new AnvanCorrelation("/home/thenghiapham/work/dataset/blah/GS2012data.txt");
        System.out.println("an add: " + anvanCorrelation.evaluateSpacePearson(space, add));
        System.out.println("an comp: " + anvanCorrelation.evaluateSpacePearson(compSpace));
    }
    
}
