package space;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.ejml.simple.SimpleMatrix;

import common.IOUtils;
import common.SimpleMatrixUtils;

import tree.Tree;
import neural.ProjectionMatrix;
import neural.SimpleWeightedTreeNetwork;
import neural.WeightedCompositionMatrices;
import neural.function.ActivationFunction;
import neural.function.IdentityFunction;
import neural.function.Tanh;

public class WeightedCompositionSemanticSpace implements CompositionalSemanticSpace {
    protected ProjectionMatrix      projectionMatrix;
    protected WeightedCompositionMatrices   compositionMatrices;
    protected ActivationFunction hiddenActivationFunction;
    
    public static WeightedCompositionSemanticSpace loadCompositionSpace(String inputFilePath, boolean binary) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFilePath));
            return loadCompositionSpace(inputStream, binary);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static WeightedCompositionSemanticSpace loadCompositionSpace(BufferedInputStream inputStream, 
            boolean binary) throws IOException{
        ProjectionMatrix projectionMatrix = ProjectionMatrix.loadProjectionMatrix(inputStream, binary);
        WeightedCompositionMatrices compositionMatrices = WeightedCompositionMatrices.loadConstructionMatrices(inputStream, binary);
        String functionString = IOUtils.readWord(inputStream);
        ActivationFunction function = new IdentityFunction();
        if ("tanh".equals(functionString)) {
            function = new Tanh();
        }
        return new WeightedCompositionSemanticSpace(projectionMatrix, compositionMatrices, function);
    }
    
    
    public static WeightedCompositionSemanticSpace loadProjectionSpace(String inputFilePath, boolean binary) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFilePath));
            return loadProjectionSpace(inputStream, binary);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static WeightedCompositionSemanticSpace loadProjectionSpace(BufferedInputStream inputStream, 
            boolean binary) throws IOException{
        ProjectionMatrix projectionMatrix = ProjectionMatrix.loadProjectionMatrix(inputStream, binary);
        WeightedCompositionMatrices compositionMatrices = WeightedCompositionMatrices.identityInitialize(new HashMap<String, String>(), projectionMatrix.getVectorSize());
        return new WeightedCompositionSemanticSpace(projectionMatrix, compositionMatrices, new IdentityFunction());
    }
    
    public WeightedCompositionSemanticSpace(ProjectionMatrix projectionMatrix, 
            WeightedCompositionMatrices compositionMatrices, ActivationFunction hiddenActivationFunction) {
        this.projectionMatrix = projectionMatrix;
        this.compositionMatrices = compositionMatrices;
        this.hiddenActivationFunction = hiddenActivationFunction;
    }
    
    public SimpleMatrix getComposedVector(String parseTreeString) {
        Tree parseTree = Tree.fromPennTree(parseTreeString);
        SimpleWeightedTreeNetwork network = SimpleWeightedTreeNetwork.createComposingNetwork(parseTree, 
                projectionMatrix, compositionMatrices, hiddenActivationFunction);
        SimpleMatrix topVector = network.compose();
//        topVector = topVector.scale(1.0 / parseTree.getWidth());
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
