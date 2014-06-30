package dependency;

public class DependencyNode {
    public static final String FILLER_POS = "FILLER";
    public static final int    LEMMA      = 0;
    public static final int    LEMMA_POS  = 1;
    public static final int    WORD       = 2;
    public static final int    WORD_POS   = 3;

    public String              word;
    public String              lemma;
    public String              pos;
    public int                 position;
    public int                 headPosition;
    public String              headRelation;

    // static methods
    public static DependencyNode getFiller(int position) {
        String word = "";
        String lemma = "";
        String pos = FILLER_POS;
        int headPosition = 0;
        String headRelation = FILLER_POS;
        return new DependencyNode(word, lemma, pos, position, headPosition,
                headRelation);
    }

    // constructors
    public DependencyNode(String line) {
        String[] elements = line.split("\t");
        if (elements.length != 6) {
            word = "";
            lemma = "";
            pos = "";
            position = 0;
            headPosition = 0;
            headRelation = "";
        } else {
            word = elements[0];
            lemma = elements[1];
            pos = elements[2];
            position = Integer.parseInt(elements[3]);
            headPosition = Integer.parseInt(elements[4]);
            headRelation = elements[5];
        }
    }

    public DependencyNode(String word, String lemma, String pos, int position,
            int headPosition, String headRelation) {
        this.word = word;
        this.lemma = lemma;
        this.pos = pos;
        this.position = position;
        this.headPosition = headPosition;
        this.headRelation = headRelation;
    }

    // instance methods
    public void toLowerCase() {
        word = word.toLowerCase();
        lemma = lemma.toLowerCase();
        pos = pos.toLowerCase();
    }

    public String getLemmaPos() {
        return lemma + "-" + pos.charAt(0);
    }

    public String getWordPos() {
        return word + "-" + pos.charAt(0);
    }

    public boolean isNoun() {
        return Character.toLowerCase(pos.charAt(0)) == 'n';
    }

    public boolean isVerb() {
        return (Character.toLowerCase(pos.charAt(0)) == 'v' || pos
                .toLowerCase().startsWith("MD"));
    }

    public boolean isAdj() {
        return Character.toLowerCase(pos.charAt(0)) == 'j';
    }

    public String toString() {
        StringBuffer sbResult = new StringBuffer();
        sbResult.append(word);
        sbResult.append("\t" + lemma);
        sbResult.append("\t" + pos);
        sbResult.append("\t" + position);
        sbResult.append("\t" + headPosition);
        sbResult.append("\t" + headRelation);
        return sbResult.toString();
    }

    public String getPresentation(int wordFormatOption) {
        String presentation = "";
        switch (wordFormatOption) {
        case LEMMA:
            presentation = lemma;
            break;
        case LEMMA_POS:
            presentation = getLemmaPos();
            break;
        case WORD:
            presentation = word;
            break;
        case WORD_POS:
            presentation = getWordPos();
            break;
        default:
            // default: lemma
            presentation = lemma;
            break;
        }
        return presentation;
    }

}
