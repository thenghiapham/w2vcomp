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
import neural.Tanh;

public class CompositionSemanticSpace {
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
    
    public double getSim(String parseTreeString1, String parseTreeString2) {
        SimpleMatrix phraseVector1 = getComposedVector(parseTreeString1);
        SimpleMatrix phraseVector2 = getComposedVector(parseTreeString2);
        return SimpleMatrixUtils.cosine(phraseVector1, phraseVector2);
    }
    
    public SimpleMatrix getVector(String word) {
        return projectionMatrix.getVector(word);
    }
    
}
