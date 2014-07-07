package tree;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CcgTree extends Tree{
    String cat = "";
    String modifiedCat = "";
    String pos = "";
    String lemma = "";

    public CcgTree(String rootLabel) {
        // TODO Auto-generated constructor stub
        super(rootLabel);
    }

    public static CcgTree parseTreeFromCcgXml(String xmlString) throws SAXException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes("iso-8859-1")));
            Node root = doc.getFirstChild();
            CcgTree result = parseTreeFromNode(root);   
            result.setParent(null);
            return result;
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static CcgTree createEmptyTree() {
        CcgTree tree = new CcgTree("None");
        tree.cat = "None";
        tree.lemma = "None";
        tree.pos = "None";
        tree.modifiedCat = "None";
        return tree;
    }
    
    public static CcgTree parseTreeFromNode(Node node) {
        if (node.getNodeName().equals("ccg")) {
            //System.out.println("First child:" + node.getFirstChild());
            if (node.getFirstChild() != null) {
                NodeList childNodes = node.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node childNode = childNodes.item(i);
                    if (!childNode.getNodeName().equals("#text")) {
                        return parseTreeFromNode(childNode);
                    }
                }
            } 
            return createEmptyTree();
            
        } else if (node.getNodeName().equals("rule")) {
            String rootLabel = node.getAttributes().getNamedItem("cat").getNodeValue();
            rootLabel = rootLabel.replaceAll("\\(", "<");
            rootLabel = rootLabel.replaceAll("\\)", ">");
            rootLabel = rootLabel.replaceAll("\\[.*?\\]", "");
            CcgTree result = new CcgTree(rootLabel);
            Vector<Tree> children = new Vector<Tree>();
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (!childNode.getNodeName().equals("#text")) {
                    children.add(parseTreeFromNode(childNode));
                }
            }
            result.setChildren(children);
            return result;
        } else if (node.getNodeName().equals("lf")) {
            NamedNodeMap attributes = node.getAttributes();
            String superTag = attributes.getNamedItem("cat").getNodeValue();
            String modifiedTag = superTag.replaceAll("\\(", "<");
            modifiedTag = modifiedTag.replaceAll("\\)", ">");
            modifiedTag = modifiedTag.replaceAll(":", ".");
            modifiedTag = modifiedTag.replaceAll("_", "-");
            modifiedTag = modifiedTag.replaceAll("\\[.*?\\]", "");
            String rootLabel = modifiedTag;
            CcgTree result = new CcgTree(rootLabel);
            Vector<Tree> children = new Vector<Tree>();
            
            String word = attributes.getNamedItem("word").getNodeValue();
            if ("(".equals(word)) {
                word = "LRB";
            } else if (")".equals(word)) {
                word = "RRB";
            }
            word = word.replaceAll(":", ".");
            word = word.replaceAll("_", "-");
            
            CcgTree child = new CcgTree(word);
            
            String lemma = attributes.getNamedItem("lemma").getNodeValue().toLowerCase();
            if ("(".equals(lemma)) {
                lemma = "LRB";
            } else if (")".equals(lemma)) {
                lemma = "RRB";
            }
            lemma = lemma.replaceAll(":", ".");
            lemma = lemma.replaceAll("_", "-");
            child.lemma = lemma;
            child.cat = superTag;
            
            
            child.modifiedCat = modifiedTag;
            
            String pos = attributes.getNamedItem("pos").getNodeValue();
            pos = pos.replaceAll(":", ".");
            child.pos = pos;
            children.add(child);
            result.setChildren(children);
            return result;
        } else {
            System.out.println(node.getNodeName());
            System.out.println(node.getParentNode().getNodeName());
            return null;
        }
        
    }
    
    public String toPennTree() {
        if (this.getChildren().size() == 1) {
            String treeString = "("+ this.getRootLabel();
            treeString += " ";
            if (getChildren().get(0).getChildren().size() != 0) {
                treeString += getChildren().get(0).toPennTree();
            } else {
                CcgTree terminalChild = (CcgTree) getChildren().get(0); 
                treeString += terminalChild.getRootLabel() + "__" + terminalChild.lemma + "__" + terminalChild.pos + "__" + terminalChild.modifiedCat;          
            }
            treeString += ")";
            return treeString;
        } else if (this.getChildren().size() > 1) {
            String treeString = "("+ getRootLabel();
            treeString += " ";
            for (Tree child : getChildren())
                treeString += child.toPennTree();
            treeString += ")";
            return treeString;
        } else {
            if (this.isTerminal()) {
                String treeString = "("+ this.getRootLabel()+"__0";
                treeString += " ";
                treeString += getRootLabel() + "__" + lemma + "__" + pos + "__" + modifiedCat;
                treeString += ")";
                return treeString;
            } else 
                return super.toPennTree();
        }
    }
    
    
    public String toSimplePennTree() {
        if (this.getChildren().size() == 1) {
            String treeString = "("+ this.getRootLabel();
            treeString += " ";
            if (getChildren().get(0).getChildren().size() != 0) {
                treeString += ((CcgTree) getChildren().get(0)).toSimplePennTree();
            } else {
                CcgTree terminalChild = (CcgTree) getChildren().get(0); 
                treeString += terminalChild.getRootLabel();         
            }
            treeString += ")";
            return treeString;
        } else if (this.getChildren().size() > 1) {
            String treeString = "("+ getRootLabel();
            treeString += " ";
            for (Tree child : getChildren())
                treeString += ((CcgTree) child).toSimplePennTree();
            treeString += ")";
            return treeString;
        } else {
            if (this.isTerminal()) {
                String treeString = "("+ this.getRootLabel();
                treeString += " ";
                treeString += getRootLabel();
                treeString += ")";
                return treeString;
            } else 
                return null;
        }
    }
    
  
    public String shortPennTree() {
        List<Tree> children = this.getChildren();
        String treeString = "";
        
        if (children.size() == 1) {
            CcgTree child = (CcgTree) getChildren().get(0); 
            if (children.get(0).getChildren().size() != 0){
                treeString += child.shortPennTree();
            } else {
                treeString += child.pos;
            }
        } else {
            treeString = "(";
            if (this.isTerminal()) {
                treeString += pos;
            } else {
                //treeString += "X";
                
                for (Tree child : children) {
                    treeString += " ";
                    treeString += ((CcgTree) child).shortPennTree();
                }
            }
            treeString += ")";
        }
        
        return treeString;
    }
    
    public List<CcgTree> getAllSubTree(int height) {
        List<CcgTree> list = new ArrayList<CcgTree>(10);
        getHeightAndAdd(list, height);
        return list;
    }
    
    private int getHeightAndAdd(List<CcgTree> list, int targetHeight) {
        int height = 0;
        List<Tree> children = getChildren();
        if (children.size() == 0) {
            height = 1;
            if (this.pos.length() == 1) height = 10;
        } else {
            if (children.size() == 1) {
                CcgTree ccgChild = (CcgTree) children.get(0);
                height = ccgChild.getHeightAndAdd(list, targetHeight);
            } else {
                for (Tree child: children) {
                    CcgTree ccgChild = (CcgTree) child;
                    height = Math.max(height, ccgChild.getHeightAndAdd(list, targetHeight));
                }
                height = height + 1;
            }
        }
        if (height == targetHeight) {
            list.add(this);
        }
        return height;
    }
    
    
    public String getLemma() {
        return lemma;
    }
}