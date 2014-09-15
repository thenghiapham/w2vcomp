package space;

import org.ejml.simple.SimpleMatrix;

import tree.Tree;

import neural.CompositionMatrices;
import neural.ProjectionMatrix;
import neural.SimpleTreeNetwork;
import neural.Tanh;

public class CompositionSemanticSpace {
    protected ProjectionMatrix      projectionMatrix;
    protected CompositionMatrices   compositionMatrices;
    public SimpleMatrix getVector(String word) {
        return null;
    }
    public SimpleMatrix getComposedVector(String parseTreeString) {
        Tree parseTree = Tree.fromPennTree(parseTreeString);
        SimpleTreeNetwork network = SimpleTreeNetwork.createComposingNetwork(parseTree, projectionMatrix, compositionMatrices, new Tanh());
        SimpleMatrix topVector = network.compose();
        return topVector;
    }
}
