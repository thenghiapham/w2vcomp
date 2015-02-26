package space;

import java.util.Arrays;
import java.util.HashSet;

import org.ejml.simple.SimpleMatrix;

import common.DataStructureUtils;
import common.SimpleMatrixUtils;

public class NormalizedSemanticSpace extends SMSemanticSpace {

    public NormalizedSemanticSpace(String[] words, double[][] vectors) {
        super(words, vectors);
        this.vectors = SimpleMatrixUtils.rowNormalize(this.vectors);
    }
    
    @Override
    public Neighbor[] getNeighbors(SimpleMatrix vector, int noNeighbor,
            String[] excludedWords) {
        Neighbor[] neighbors = new Neighbor[words.length - excludedWords.length];
        HashSet<String> excludedDict = DataStructureUtils
                .arrayToSet(excludedWords);
        int neighborIndex = 0;
        double[] sims = vectors.mult(vector.transpose()).getMatrix().data;
        for (int i = 0; i < words.length; i++) {
            if (!excludedDict.contains(words[i])) {
                neighbors[neighborIndex] = new Neighbor(words[i],sims[i]);
                neighborIndex++;
            }
        }
        Arrays.sort(neighbors, Neighbor.NeighborComparator);
        if (noNeighbor < words.length) {
            return Arrays.copyOfRange(neighbors, 0, noNeighbor);
        } else {
            return neighbors;
        }
    }
    
    public String getAnalogy(String word1, String word2, String word3) {
        Neighbor[] neighbors = new Neighbor[words.length];
        SimpleMatrix v1 = this.getVector(word1);
        SimpleMatrix v2 = this.getVector(word2);
        SimpleMatrix v3 = this.getVector(word3);
        double[] sims1 = vectors.mult(v1.transpose()).getMatrix().data;
        double[] sims2 = vectors.mult(v2.transpose()).getMatrix().data;
        double[] sims3 = vectors.mult(v3.transpose()).getMatrix().data;
        for (int i = 0; i < words.length; i++) {
            neighbors[i] = new Neighbor(words[i],((sims2[i] * sims3[i]) / (sims1[i] + 0.001)));
//            neighbors[i] = new Neighbor(words[i],(sims2[i] + sims3[i] - sims1[i]));
        }
        Arrays.sort(neighbors, Neighbor.NeighborComparator);
        for (int i = 0; i < 4; i++) {
            String word = neighbors[i].word;
            if (!word.equals(word1) && !word.equals(word2) && !word.equals(word3)) {
                return word;
            }
        }
        // should never arrive here
        return null;
    }

}
