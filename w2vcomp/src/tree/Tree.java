package tree;

import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Lorenzo Dell'Arciprete
 * This class represents a tree as a root node and references to its children. 
 * As such, this class can also be considered as representing a single tree node (i.e. the root node).
 */
public class Tree {
    
    protected String rootLabel;   // The label of the root node
    protected List<Tree> children = new ArrayList<Tree>();    // The ordered list of children of the root node
    protected Tree parent;
    
    public Tree() {
        
    }
    
    public Tree(String rootLabel) {
        this.rootLabel = rootLabel;
    }
    
    
    /**
     * @param treeString - a tree in string parenthetic format
     * @return the Tree object representing the input tree
     * @throws Exception if the input tree string is malformed
     */
    public static Tree fromPennTree(String treeString) throws Exception {
        if (treeString == null || treeString.length() < 1)
            throw new Exception("Parse error: empty (sub)tree");
        Tree tree = null;
        treeString = treeString.trim();
        if (treeString.indexOf('(') == -1) {
            //It is a terminal node
            tree = new Tree(treeString);
        }
        else if (treeString.charAt(0) == '(' && treeString.charAt(treeString.length()-1) == ')') {
            //It is a tree
            String content = treeString.substring(1, treeString.length()-1);
            int firstPar = content.indexOf('(');
            if (firstPar == -1) {
                //It is either a terminal node or a preterminal node with a single terminal node
                int firstBlank = content.indexOf(' '); 
                if (firstBlank == -1)
                    tree = new Tree(content.trim());
                else {
                    tree = new Tree(content.substring(0, firstBlank).trim());
                    tree.getChildren().add(new Tree(content.substring(firstBlank+1).trim()));
                }
            }
            else {
                //It is a tree
                tree = new Tree(content.substring(0, firstPar).trim());
                content = content.substring(firstPar).trim();
                while (content.length() > 0) {
                    if (content.charAt(0) != '(')
                        throw new Exception("Parse error for (sub)tree 1: "+ "->" + content + "<- ->" +treeString + "<-");
                    int openPars = 1;
                    int index = 1;
                    while (openPars > 0) {
                        if (index >= content.length())
                            throw new Exception("Parse error for (sub)tree 2: "+treeString);
                        if (content.charAt(index) == ')')
                            openPars--;
                        else if (content.charAt(index) == '(')
                            openPars++;
                        index++;
                    }
                    tree.getChildren().add(Tree.fromPennTree(content.substring(0, index).trim()));
                    content = content.substring(index).trim();
                }
            }
        }
        else
            throw new Exception("Parse error for (sub)tree 3: "+treeString);
        return tree;
    }
    
    /**
     * @return the parenthetic format string representation for this tree 
     */
    public String toPennTree() {
        String treeString = "("+rootLabel;
        if (children.size() == 1) {
            treeString += " ";
            if (children.get(0).getChildren().size() == 0)
                treeString += children.get(0).getRootLabel();
            else
                treeString += children.get(0).toPennTree();
        }
        else if (children.size() > 1) {
            treeString += " ";
            for (Tree child : children)
                treeString += child.toPennTree();
        }
        treeString += ")";
        return treeString;
    }
    
    public void initializeParents() {
        initializeParent(null);
    }
    
    private void initializeParent(Tree parent) {
        this.parent = parent;
        for (Tree child : getChildren())
            child.initializeParent(this);
    }
    
    @Override
    public String toString() {
        return toPennTree();
    }
    
    public boolean equals(Tree tree) {
        if (!rootLabel.equals(tree.getRootLabel()))
            return false;
        if (children.size() != tree.getChildren().size())
            return false;
        for (int i=0; i<children.size(); i++)
            if (!children.get(i).equals(tree.getChildren().get(i)))
                return false;
        return true;
    }

    public String getRootLabel() {
        return rootLabel;
    }

    public void setRootLabel(String root) {
        this.rootLabel = root;
    }

    public List<Tree> getChildren() {
        return children;
    }

    public boolean isTerminal() {
        return children.isEmpty();
    }
    
    public boolean isPreTerminal() {
        if (isTerminal())
            return false;
        else {
            for (Tree c : children)
                if (!c.isTerminal())
                    return false;
            return true;
        }
    }

    public void setChildren(ArrayList<Tree> children) {
        this.children = children;
    }

    public Tree getParent() {
        return parent;
    }

    public void setParent(Tree parent) {
        this.parent = parent;
    }
    

    public ArrayList<Tree> allNodes() {
        return allNodes(this);
    }
    
    private ArrayList<Tree> allNodes(Tree node) {
        ArrayList<Tree> all = new ArrayList<Tree>();
        all.add(node);
        for (Tree child : node.getChildren())
            all.addAll(allNodes(child));
        return all;
    }

    
}