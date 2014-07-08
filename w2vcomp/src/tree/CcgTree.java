package tree;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CcgTree extends Tree{
    public static final int LEFT_HEAD = 0;
    public static final int RIGHT_HEAD = 1;
    int headInfo = LEFT_HEAD;
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
    
    public static int getHeadFromRule(String rule) {
        if (rule.equals("ba")) {
            return RIGHT_HEAD;
        } else if (rule.equals("fa")) {
            return LEFT_HEAD;
        } else if (rule.equals("lex")) {
            return LEFT_HEAD;
        } else if (rule.equals("rp")) {
            return LEFT_HEAD;
        } else {
            //System.out.println("Strange rule: " + rule);
            return LEFT_HEAD;
        }
    }
    
    
    public static String normalizeTag(String tag) {
        tag = tag.replaceAll("\\(", "<");
        tag = tag.replaceAll("\\)", ">");
        tag = tag.replaceAll("\\[.*?\\]", "");
        tag = tag.replaceAll(":", ".");
        tag = tag.replaceAll("_", "-");
        return tag;
    }
    
    public static String normalizeWord(String word) {
        if ("(".equals(word)) {
            word = "LRB";
        } else if (")".equals(word)) {
            word = "RRB";
        }
        word = word.replaceAll(":", ".");
        word = word.replaceAll("_", "-");
        word = word.replaceAll(" ", "-");
        return word;
    }
    
    protected static CcgTree createTreeFromXmlLexicalNode(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        String superTag = attributes.getNamedItem("cat").getNodeValue();
        String modifiedTag = normalizeTag(superTag);
        String rootLabel = modifiedTag;
        CcgTree result = new CcgTree(rootLabel);
        ArrayList<Tree> children = new ArrayList<Tree>();
        
        String word = attributes.getNamedItem("word").getNodeValue();
        word = normalizeWord(word);
        
        CcgTree child = new CcgTree(word);
        
        String lemma = attributes.getNamedItem("lemma").getNodeValue().toLowerCase();
        lemma = normalizeWord(lemma);
        child.lemma = lemma;
        
        child.cat = superTag;
        child.modifiedCat = modifiedTag;
        
        String pos = attributes.getNamedItem("pos").getNodeValue();
        pos = pos.replaceAll(":", ".");
        child.pos = pos;
        children.add(child);
        result.setChildren(children);
        return result;
    }
    
    protected static CcgTree createTreeFromXmlRuleNode(Node node) {
        String rootLabel = node.getAttributes().getNamedItem("cat").getNodeValue();
        rootLabel = normalizeTag(rootLabel);
        CcgTree result = new CcgTree(rootLabel);
        result.headInfo = getHeadFromRule(node.getAttributes().getNamedItem("type").getNodeValue());
        ArrayList<Tree> children = new ArrayList<Tree>();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (!childNode.getNodeName().equals("#text")) {
                children.add(parseTreeFromNode(childNode));
            }
        }
        result.setChildren(children);
        return result;
    }
    
    public static CcgTree parseTreeFromNode(Node node) {
        if (node.getNodeName().equals("ccg")) {
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
            return createTreeFromXmlRuleNode(node);
        } else if (node.getNodeName().equals("lf")) {
            return createTreeFromXmlLexicalNode(node);
        } else {
            System.out.println(node.getNodeName());
            System.out.println(node.getParentNode().getNodeName());
            return null;
        }
        
    }
    
    /**
     * Print all information to a string
     * - non-terminal node contains: head position (option for supertag?)
     * - pre-terminal node contains: pos
     * - terminal node contains: word (lemma or not) 
     */
    public String toSimplePennTree() {
        if (this.children.size() == 1) {
            String treeString = "";
            
            if (children.get(0).children.size() != 0) {
                treeString += ((CcgTree) children.get(0)).toSimplePennTree();
            } else {
                treeString += "(" + this.headInfo + " ";
                CcgTree terminalChild = (CcgTree) children.get(0); 
                treeString += terminalChild.getRootLabel();   
                treeString += ")";
            }
            return treeString;
        } else if (this.children.size() > 1) {
            String treeString = "("+ headInfo;
            treeString += " ";
            for (Tree child : children)
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
    
  
//    public String shortPennTree() {
//        String treeString = "";
//        
//        if (children.size() == 1) {
//            CcgTree child = (CcgTree) children.get(0); 
//            if (children.get(0).children.size() != 0){
//                treeString += child.shortPennTree();
//            } else {
//                treeString += child.pos;
//            }
//        } else {
//            treeString = "(";
//            if (this.isTerminal()) {
//                treeString += pos;
//            } else {
//                for (Tree child : children) {
//                    treeString += " ";
//                    treeString += ((CcgTree) child).shortPennTree();
//                }
//            }
//            treeString += ")";
//        }
//        
//        return treeString;
//    }
    
    public List<CcgTree> getAllSubTreeAtHeight(int height) {
        List<CcgTree> list = new ArrayList<CcgTree>(10);
        getHeightAndAdd(list, height);
        return list;
    }
    
    private int getHeightAndAdd(List<CcgTree> list, int targetHeight) {
        int height = 0;
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
