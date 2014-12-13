package space;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.ejml.simple.SimpleMatrix;

import common.IOUtils;
import common.SimpleMatrixUtils;

import tree.Tree;
import neural.DiagonalCompositionMatrices;
import neural.ProjectionMatrix;
import neural.SimpleDiagonalTreeNetwork;
import neural.function.ActivationFunction;
import neural.function.IdentityFunction;
import neural.function.Tanh;

public class DiagonalCompositionSemanticSpace implements CompositionalSemanticSpace {
    protected ProjectionMatrix      projectionMatrix;
    protected DiagonalCompositionMatrices   compositionMatrices;
    protected ActivationFunction hiddenActivationFunction;
    
    public static DiagonalCompositionSemanticSpace loadCompositionSpace(String inputFilePath, boolean binary) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFilePath));
            return loadCompositionSpace(inputStream, binary);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static DiagonalCompositionSemanticSpace loadCompositionSpace(BufferedInputStream inputStream, 
            boolean binary) throws IOException{
        ProjectionMatrix projectionMatrix = ProjectionMatrix.loadProjectionMatrix(inputStream, binary);
        DiagonalCompositionMatrices compositionMatrices = DiagonalCompositionMatrices.loadConstructionMatrices(inputStream, binary);
        String functionString = IOUtils.readWord(inputStream);
        System.out.println(functionString);
        ActivationFunction function = new Tanh();
        if ("identity".equals(functionString)) {
            function = new IdentityFunction();
        }
        return new DiagonalCompositionSemanticSpace(projectionMatrix, compositionMatrices, function);
    }
    
    
    public static DiagonalCompositionSemanticSpace loadProjectionSpace(String inputFilePath, boolean binary) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFilePath));
            return loadProjectionSpace(inputStream, binary);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static DiagonalCompositionSemanticSpace loadProjectionSpace(BufferedInputStream inputStream, 
            boolean binary) throws IOException{
        ProjectionMatrix projectionMatrix = ProjectionMatrix.loadProjectionMatrix(inputStream, binary);
        DiagonalCompositionMatrices compositionMatrices = DiagonalCompositionMatrices.identityInitialize(new HashMap<String, String>(), projectionMatrix.getVectorSize());
        return new DiagonalCompositionSemanticSpace(projectionMatrix, compositionMatrices, new IdentityFunction());
    }
    
    public DiagonalCompositionSemanticSpace(ProjectionMatrix projectionMatrix, 
            DiagonalCompositionMatrices compositionMatrices, ActivationFunction hiddenActivationFunction) {
        this.projectionMatrix = projectionMatrix;
        this.compositionMatrices = compositionMatrices;
        this.hiddenActivationFunction = hiddenActivationFunction;
    }
    
    public SimpleMatrix getComposedVector(String parseTreeString) {
        Tree parseTree = Tree.fromPennTree(parseTreeString);
        SimpleDiagonalTreeNetwork network = SimpleDiagonalTreeNetwork.createComposingNetwork(parseTree, 
                projectionMatrix, compositionMatrices, hiddenActivationFunction);
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
    
    
}
