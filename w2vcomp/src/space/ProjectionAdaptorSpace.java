package space;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;
import common.exception.UnimplementedException;

import neural.ProjectionMatrix;

public class ProjectionAdaptorSpace implements SemanticSpace{
    protected ProjectionMatrix projectionMatrix;
    public ProjectionAdaptorSpace(ProjectionMatrix projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }
    @Override
    public int getVectorSize() {
        return projectionMatrix.getVectorSize();
    }
    @Override
    public SimpleMatrix getVector(String word) {
        return projectionMatrix.getVector(word);
    }
    @Override
    public double getSim(String word1, String word2) {
        SimpleMatrix v1 = getVector(word1);
        SimpleMatrix v2 = getVector(word2);
        return SimpleMatrixUtils.cosine(v1, v2);
    }
    @Override
    public Neighbor[] getNeighbors(String word, int noNeighbor) {
        // TODO Auto-generated method stub
        throw new UnimplementedException("not yet implemented");
    }
    @Override
    public Neighbor[] getNeighbors(SimpleMatrix vector, int noNeighbor,
            String[] excludedWords) {
        // TODO Auto-generated method stub
        throw new UnimplementedException("not yet implemented");
    }
}
