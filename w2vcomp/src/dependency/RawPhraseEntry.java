package dependency;

public class RawPhraseEntry {
    public String phrase;
    public int    startPosition;
    public int    endPosition;
    public int[]  componentPositions;

    public RawPhraseEntry(String phrase, int startPosition, int endPosition,
            int[] componentPositions) {
        this.phrase = phrase;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.componentPositions = componentPositions;
    }

    public String toString() {
        StringBuffer sbResult = new StringBuffer();
        sbResult.append("phrase surface string:" + phrase + "\n");
        sbResult.append("start:" + startPosition + "\n");
        sbResult.append("end:" + endPosition + "\n");
        sbResult.append("components:");
        for (int i = 0; i < componentPositions.length; i++) {
            sbResult.append(" " + componentPositions[i]);
        }
        return sbResult.toString();
    }
}
