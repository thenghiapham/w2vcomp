package preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.xml.sax.SAXException;

import common.WordForm;

import tree.CcgTree;

public class Ccg2SimpleTree {
    public static void main(String args[]) {
        String outputFile = args[0];
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in,"iso-8859-1"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            preprocessCorpus(reader, writer);
            reader.close();
            writer.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void preprocessCorpus(BufferedReader reader, BufferedWriter writer) throws IOException{
        String line = reader.readLine();
        String treeString = "";
        boolean inTree = false;
        int lineNum = 0;
        while (line != null) {
            lineNum++;
            if (lineNum % 1000000 == 0) {
                System.out.println("progress: " + lineNum);
            }
            if ("<ccg>".equals(line)) {
                inTree = true;
            }
            if (inTree) {
                treeString += line;
            }
            if ("</ccg>".equals(line)) {
                inTree = false;
                String simpleTreeString = getSimpleTreeString(treeString);
                if (!"".equals(simpleTreeString)) {
                    writer.write(simpleTreeString);
                    writer.write("\n");
                }
                treeString = "";
            }
            line = reader.readLine();
        }
    }
    
    public static String getSimpleTreeString(String xmlString) {
        try {
            CcgTree ccgTree = CcgTree.parseTreeFromCcgXml(xmlString);
            return ccgTree.toSimplePennTree(WordForm.LEMMA_POS);
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }
}
