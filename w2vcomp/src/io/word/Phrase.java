package io.word;

public class Phrase {
    public int   phraseIndex;
    public int   startPosition;
    public int   endPosition;
    public int[] componentPositions;

    public Phrase(int phraseIndex, int startPosition, int endPosition,
            int[] componentPositions) {
        this.phraseIndex = phraseIndex;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.componentPositions = componentPositions;
    }

    public String toString() {
        StringBuffer sbResult = new StringBuffer();
        sbResult.append("phrase index:" + phraseIndex + "\n");
        sbResult.append("start:" + startPosition + "\n");
        sbResult.append("end:" + endPosition + "\n");
        sbResult.append("components:");
        for (int i = 0; i < componentPositions.length; i++) {
            sbResult.append(" " + componentPositions[i]);
        }
        return sbResult.toString();
    }
}
