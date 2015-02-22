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
        double[] sims = vectors.mult(vector).getMatrix().data;
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

}
