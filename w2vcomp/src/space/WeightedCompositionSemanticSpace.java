package space;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
    
    private void normalize(ArrayList<WordWeight> wordWeights) {
        double sumWeights = 0;
        for (WordWeight wordWeight: wordWeights) {
            sumWeights += wordWeight.weight;
        }
        if (sumWeights == 0) return;
        for (WordWeight wordWeight: wordWeights) {
            wordWeight.weight /= sumWeights;
        }
    }
    
    public String getComposedString(String parseString) {
        
        Tree parseTree = Tree.fromPennTree(parseString);
        ArrayList<WordWeight> wordWeights = getWordWeights(parseTree);
        StringBuffer buffer = new StringBuffer();
        for (WordWeight wordWeight: wordWeights) {
            buffer.append(" + ");
            buffer.append(wordWeight.weight);
            buffer.append(" * ");
            buffer.append(wordWeight.word);
        }
        return buffer.toString().substring(3);
    }
    
    
    public String getComposedLengthString(String parseString) {
        DecimalFormat format = new DecimalFormat("#.000");
        Tree parseTree = Tree.fromPennTree(parseString);
        ArrayList<WordWeight> wordWeights = getWordWeightLengths(parseTree);
        StringBuffer buffer = new StringBuffer();
        for (WordWeight wordWeight: wordWeights) {
            buffer.append(" + ");
            buffer.append(format.format(wordWeight.weight));
            buffer.append(" * ");
            buffer.append(wordWeight.word);
        }
        return buffer.toString().substring(3);
    }
    
    private ArrayList<WordWeight> getWordWeights(Tree parseTree) {
        // TODO Auto-generated method stub
        ArrayList<WordWeight> wordWeights = new ArrayList<>();
        if (parseTree.isPreTerminal()) {
            String word = parseTree.getChildren().get(0).getRootLabel();
            WordWeight wordWeight = new WordWeight(word, 1.0);
            wordWeights.add(wordWeight);
            return wordWeights;
        }
        ArrayList<Tree> children = parseTree.getChildren(); 
        if (children.size() >= 2) {
            String construction = parseTree.getConstruction();
            SimpleMatrix weightMat = getConstructionMatrix(construction);
            double[] weights = weightMat.getMatrix().data;
            for (int i = 0; i < weights.length; i++) {
                double weight = weights[i];
                ArrayList<WordWeight> childWordWeights = getWordWeights(children.get(i));
                for (WordWeight wordWeight: childWordWeights) {
                    wordWeight.weight = weight * wordWeight.weight;
                    wordWeights.add(wordWeight);
                }
            }
            return wordWeights;
        } else if (children.size() == 1){
            return getWordWeights(children.get(0));
        } else return null;
        
    }

    private ArrayList<WordWeight> getWordWeightLengths(Tree parseTree) {
        // TODO Auto-generated method stub
        ArrayList<WordWeight> wordWeights = new ArrayList<>();
        if (parseTree.isPreTerminal()) {
            String word = parseTree.getChildren().get(0).getRootLabel();
            SimpleMatrix wordVector = this.getVector(word);
            double length = 0; 
            if (wordVector != null) {
                length = wordVector.normF();
            }
            WordWeight wordWeight = new WordWeight(word, length);
            wordWeights.add(wordWeight);
            return wordWeights;
        }
        ArrayList<Tree> children = parseTree.getChildren(); 
        if (children.size() >= 2) {
            String construction = parseTree.getConstruction();
            SimpleMatrix weightMat = getConstructionMatrix(construction);
            double[] weights = weightMat.getMatrix().data;
            for (int i = 0; i < weights.length; i++) {
                double weight = weights[i];
                ArrayList<WordWeight> childWordWeights = getWordWeightLengths(children.get(i));
                for (WordWeight wordWeight: childWordWeights) {
                    wordWeight.weight = weight * wordWeight.weight;
                    wordWeights.add(wordWeight);
                }
            }
            return wordWeights;
        } else if (children.size() == 1){
            return getWordWeightLengths(children.get(0));
        } else return null;
        
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
    
    class WordWeight {
        String word;
        double weight;
        public WordWeight(String word, double weight) {
            // TODO Auto-generated constructor stub
            this.word = word;
            this.weight = weight;
        }
    }
}
