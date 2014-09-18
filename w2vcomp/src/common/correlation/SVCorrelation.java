package common.correlation;

public class SVCorrelation extends TwoWordPhraseCorrelation{

    public SVCorrelation(String dataset) {
        super(dataset);
    }

    @Override
    protected String[] getParseComposeData() {
        String[] result = new String[composeData.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = "(S (VP " + composeData[i][1] + ") (NP " + composeData[i][0] + "))";
            System.out.println(result[i]);
        }
        return result;
    }

}
