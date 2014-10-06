package space;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

import tree.Tree;
import neural.CompositionMatrices;
import neural.ProjectionMatrix;
import neural.SimpleTreeNetwork;
import neural.function.Tanh;

public class CompositionSemanticSpace implements SemanticSpace {
    protected ProjectionMatrix      projectionMatrix;
    protected CompositionMatrices   compositionMatrices;
    
    public static CompositionSemanticSpace loadCompositionSpace(String inputFilePath, boolean binary) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFilePath));
            return loadCompositionSpace(inputStream, binary);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static CompositionSemanticSpace loadCompositionSpace(BufferedInputStream inputStream, 
            boolean binary) throws IOException{
        ProjectionMatrix projectionMatrix = ProjectionMatrix.loadProjectionMatrix(inputStream, binary);
        CompositionMatrices compositionMatrices = CompositionMatrices.loadConstructionMatrices(inputStream, binary);
        return new CompositionSemanticSpace(projectionMatrix, compositionMatrices);
    }
    
    public CompositionSemanticSpace(ProjectionMatrix projectionMatrix, 
            CompositionMatrices compositionMatrices) {
        this.projectionMatrix = projectionMatrix;
        this.compositionMatrices = compositionMatrices;
    }
    
    public SimpleMatrix getComposedVector(String parseTreeString) {
        Tree parseTree = Tree.fromPennTree(parseTreeString);
        SimpleTreeNetwork network = SimpleTreeNetwork.createComposingNetwork(parseTree, 
                projectionMatrix, compositionMatrices, new Tanh());
        SimpleMatrix topVector = network.compose();
        return topVector;
    }
    
    public SimpleMatrix getComposedMatrix(String[] parseStrings) {
        SimpleMatrix result = new SimpleMatrix(parseStrings.length, projectionMatrix.getVectorSize());
        for (int i = 0; i < parseStrings.length; i++) {
            SimpleMatrix composedVector = getComposedVector(parseStrings[i]);
            result.setRow(i, 0, composedVector.getMatrix().getData());
        }
        return result;
    }
    
    public double getSim(String parseTreeString1, String parseTreeString2) {
        SimpleMatrix phraseVector1 = getComposedVector(parseTreeString1);
        SimpleMatrix phraseVector2 = getComposedVector(parseTreeString2);
        return SimpleMatrixUtils.cosine(phraseVector1, phraseVector2);
    }
    
    public SimpleMatrix getVector(String word) {
        return projectionMatrix.getVector(word);
    }
    
    public SimpleMatrix getConstructionMatrix(String construction) {
        return compositionMatrices.getCompositionMatrix(construction);
    }

    @Override
    public int getVectorSize() {
        // TODO Auto-generated method stub
        return projectionMatrix.getVectorSize();
    }

    @Override
    public Neighbor[] getNeighbors(String word, int noNeighbor) {
        // TODO Auto-generated method stub
//        throw new UnIm
        return null;
    }

    @Override
    public Neighbor[] getNeighbors(SimpleMatrix vector, int noNeighbor,
            String[] excludedWords) {
        // TODO Auto-generated method stub
        return null;
    }
    
//    public SimpleMatrix getGroupMatrix(String group) {
//        return compositionMatrices.g
//    }
    
}
