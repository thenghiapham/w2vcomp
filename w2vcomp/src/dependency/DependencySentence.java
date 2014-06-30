package dependency;

import java.util.ArrayList;
import java.util.regex.Pattern;

import common.DataStructureUtils;

public class DependencySentence {
    ArrayList<DependencyNode> nodes;
    Pattern                   nonChainRelationPattern = Pattern
                                                              .compile("^(PRN|OBJ|ADV|COORD|AMOD)");
    boolean                   processedPredicate      = false;
    int                       originalSize;

    public DependencySentence(ArrayList<DependencyNode> nodes) {
        this.nodes = nodes;
        originalSize = nodes.size();
        insertFiller();

    }

    /*
     * Malt parser output sometimes has incomplete sentence, where some nodes
     * are missing (in those sentences, the "position" attribute of a node can
     * be larger then its position in a sentence
     */
    protected void insertFiller() {

        int size = nodes.size();
        if (size == 0)
            return;
        DependencyNode lastNode = nodes.get(size - 1);
        // checking whether the sentence is complete
        // if yes, there is nothing to do
        if (size == lastNode.position) {
            return;
        }

        // creating new list of nodes with filler nodes
        ArrayList<DependencyNode> newNodes = new ArrayList<DependencyNode>();

        for (DependencyNode currentNode : nodes) {
            while (newNodes.size() < currentNode.position - 1) {
                newNodes.add(DependencyNode.getFiller(newNodes.size() + 1));
            }
            newNodes.add(currentNode);
        }
        this.nodes = newNodes;
    }

    protected int[] createPredicateChain() {
        int[] chain = new int[nodes.size()];

        for (DependencyNode node : nodes) {
            if (node.isVerb()) {
                String headRelation = node.headRelation;
                int headPosition = node.headPosition;
                // surprisingly it never raises out of bound exception here
                if (!nonChainRelationPattern.matcher(headRelation).matches()
                        && headPosition > 0) {
                    DependencyNode parentNode = nodes.get(headPosition - 1);
                    if (parentNode.isVerb()) {
                        chain[headPosition - 1] = node.position;
                        // also turn vvn to vvh to distinguish be + vvn from
                        // have + vvn
                        if (parentNode.lemma.equals("have")
                                && node.pos.equals("vvn")) {
                            node.pos = "vvh";
                        }
                    }
                }
            }
        }
        return chain;
    }

    protected void processPredicateChain() {
        int[] chain = createPredicateChain();
        for (DependencyNode node : nodes) {
            if (node.isNoun()) {
                int headPosition = node.headPosition;
                if (headPosition > 0) {
                    // Once in a while, the headPosition is out of the sentence
                    // for some weird reason
                    // need to check
                    if (headPosition > nodes.size())
                        continue;
                    int realHead = chain[headPosition - 1];
                    while (realHead != 0) {
                        if (realHead > nodes.size())
                            break;
                        node.headPosition = realHead;
                        realHead = chain[realHead - 1];

                    }
                }
            }
        }
    }

    public boolean isComplete() {
        return originalSize == nodes.size();
    }

    public String[] getSingleWords(int wordFormatOption) {
        String[] result = new String[nodes.size()];
        for (DependencyNode node : nodes) {
            result[node.position - 1] = node.getPresentation(wordFormatOption);
        }
        return result;
    }

    public RawPhraseEntry[] getANs(int wordFormatOption) {
        ArrayList<RawPhraseEntry> result = new ArrayList<RawPhraseEntry>();
        for (DependencyNode node : nodes) {
            if (node.isAdj() && node.headPosition != 0
                    && node.headPosition <= nodes.size()
                    && node.headRelation.equals("NMOD")) {
                DependencyNode headNode = nodes.get(node.headPosition - 1);
                if (headNode.isNoun()) {
                    String phraseString = node
                            .getPresentation(wordFormatOption)
                            + "_"
                            + headNode.getPresentation(wordFormatOption);
                    // set the an phrase position to the n
                    // TODO: set it back

                    int startPosition = node.position - 1;
                    int endPosition = headNode.position - 1;
                    if (startPosition > endPosition) {
                        int tmp = startPosition;
                        startPosition = endPosition;
                        endPosition = tmp;
                    }
                    int[] componentPositions = new int[2];
                    componentPositions[0] = node.position - 1;
                    componentPositions[1] = headNode.position - 1;
                    RawPhraseEntry phrase = new RawPhraseEntry(phraseString,
                            startPosition, endPosition, componentPositions);
                    result.add(phrase);
                }
            }
        }
        return DataStructureUtils.rawPhraseListToArray(result);
    }

    public RawPhraseEntry[] getSVs(int wordFormatOption) {
        ArrayList<RawPhraseEntry> result = new ArrayList<RawPhraseEntry>();
        if (!processedPredicate) {
            processPredicateChain();
            processedPredicate = true;
        }
        for (DependencyNode node : nodes) {
            if (node.isNoun() && node.headPosition != 0
                    && node.headPosition <= nodes.size()
                    && node.headRelation.equals("SBJ")) {
                DependencyNode headNode = nodes.get(node.headPosition - 1);
                if (headNode.isVerb()) {
                    // don't keep vvn
                    if (headNode.pos.equals("vvn"))
                        continue;

                    String phraseString = node
                            .getPresentation(wordFormatOption)
                            + "_"
                            + headNode.getPresentation(wordFormatOption);
                    // set the sv phrase position to the verb
                    // TODO: set it back

                    int startPosition = headNode.position - 1;
                    int endPosition = node.position - 1;
                    if (startPosition > endPosition) {
                        int tmp = startPosition;
                        startPosition = endPosition;
                        endPosition = tmp;
                    }

                    int[] componentPositions = new int[2];
                    componentPositions[0] = node.position - 1;
                    componentPositions[1] = headNode.position - 1;
                    RawPhraseEntry phrase = new RawPhraseEntry(phraseString,
                            startPosition, endPosition, componentPositions);
                    // System.out.println("RAW PHRASE:" + phrase.toString());
                    // System.out.println("in:\n" + toString());
                    result.add(phrase);
                }
            }
        }
        return DataStructureUtils.rawPhraseListToArray(result);
    }

    public String toString() {
        StringBuffer sbResult = new StringBuffer();
        for (DependencyNode node : nodes) {
            sbResult.append(node.toString());
            sbResult.append("\n");
        }
        return sbResult.toString();
    }
}
