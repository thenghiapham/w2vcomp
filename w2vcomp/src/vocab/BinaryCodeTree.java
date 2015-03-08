package vocab;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import common.exception.ValueException;

public class BinaryCodeTree {
    BinaryCodeTree leftChild;
    BinaryCodeTree rightChild;
    String name = null;
    int frequency = 0;
    
    public BinaryCodeTree() {
        leftChild = null;
        rightChild = null;
    }
    
    public void addChild(String code, String word, int frequency) {
        if (code.length() == 0) {
            if (name == null) {
                if (leftChild != null || rightChild != null) {
                    throw new ValueException("Not a leaf node");
                }
                name = word;
                this.frequency = frequency;
            } else {
                throw new ValueException("code already taken");
            }
        } else {
            if (code.charAt(0) == '0') {
                if (leftChild == null)
                    leftChild = new BinaryCodeTree();
                leftChild.addChild(code.substring(1), word, frequency);
            } else {
                if (rightChild == null)
                    rightChild = new BinaryCodeTree();
                rightChild.addChild(code.substring(1), word, frequency);
            }
        }
    }
    
    public void pruneTree() {
        if (leftChild != null) {
            leftChild.pruneTree();
            if (leftChild.leftChild == null && leftChild.rightChild == null) {
                // do nothing
            } else if (leftChild.leftChild == null) {
                leftChild = leftChild.rightChild;
            } else if (leftChild.rightChild == null) {
                leftChild = leftChild.leftChild;
            }
        }
        if (rightChild != null) {
            rightChild.pruneTree();
            if (rightChild.leftChild == null && rightChild.rightChild == null) {
                // do nothing
            } else if (rightChild.leftChild == null) {
                rightChild = rightChild.rightChild;
            } else if (leftChild.rightChild == null) {
                rightChild = rightChild.leftChild;
            }
        }
    }
    
    public static BinaryCodeTree readTree(String clusterFile, int minFrequency) throws IOException{
        BinaryCodeTree root = new BinaryCodeTree();
        BufferedReader reader = new BufferedReader(new FileReader(clusterFile));
        String line = reader.readLine(); 
        while (line != null) {
            String[] elements = line.split("( |\t)");
            String code = elements[0];
            String word = elements[1];
            int frequency = Integer.parseInt(elements[2]);
            if (frequency >= minFrequency) {
                root.addChild(code, word, frequency);
            }
            line = reader.readLine();
        }
        reader.close();
        root.pruneTree();
        return root;
    }
    
    public int getNumNode() {
        int leftNode = 0;
        int rightNode = 0;
        if (leftChild != null) leftNode = leftChild.getNumNode();
        if (rightChild != null) rightNode = rightChild.getNumNode();
        return leftNode + rightNode + 1;
    }
    
    public int getNumLeafNode() {
        int leftLeafNode = 0;
        int rightLeafNode = 0;
        if (leftChild != null) leftLeafNode = leftChild.getNumLeafNode();
        if (rightChild != null) rightLeafNode = rightChild.getNumLeafNode();
        if (leftLeafNode == 0 && rightLeafNode == 0) return 1;
        return leftLeafNode + rightLeafNode;
    }
}
