package evaluation;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;

import space.Neighbor;
import space.SemanticSpace;
import word2vec.Images;

import common.IOUtils;
import common.MenCorrelation;
import common.SimpleMatrixUtils;
import demo.TestConstants;

public class ComputeNearestNeighs {

    
    HashMap<String, String> absScores;
    Set<String> testWords;
    Set<String> visionWords;
    
    ComputeNearestNeighs(){
        
    }

    
    ComputeNearestNeighs(String dataset,Set<String> testWords, Set<String> visionWords){
        readDataset(dataset);
        this.testWords = testWords;
        this.visionWords = visionWords;
    }
    
    
    
    private void readDataset(String dataset){
        ArrayList<String>  data =  IOUtils.readFile(dataset);
        absScores  = new HashMap<String, String>();
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split("[ \t]+");
            absScores.put(elements[0], elements[1]);
            
        }
    }
    
    public void createList(SemanticSpace space, String outFile) throws IOException{
        
        BufferedWriter f = new BufferedWriter(new FileWriter(outFile));
        int id = 0;
        String score;
        boolean hasVisual;
        String text = "";
        boolean isFirst;
        for (String word: space.getWord2Index().keySet()){
            hasVisual = visionWords.contains(word);
            score = (absScores.containsKey(word)? absScores.get(word): "-1");
            //info for word
            text+= Integer.toString(id)+" ";
            text+= word+" ";
            text+= Boolean.toString(hasVisual)+" ";
            text+= score+" ";
            
            isFirst = true;
            
            for (Neighbor s: space.getNeighbors(word,10)){
                if (isFirst){
                    text += s.word+" "+ Double.toString(s.sim)+" ";
                    isFirst= false;
                }
                else{
                    text += s.word+" ";
                }
            }
            text +="\n";
            //System.out.println(text);
            id++;
        }
        f.write(text);
        f.close();
        
    }
    
public void createList(SemanticSpace space, String outFile,SemanticSpace space2) throws IOException{
        
        BufferedWriter f = new BufferedWriter(new FileWriter(outFile));
        int id = 0;
        String score;
        boolean hasVisual;
        String text = "";
        boolean isFirst;
        for (String word: space.getWord2Index().keySet()){
            hasVisual = visionWords.contains(word);
            score = (absScores.containsKey(word)? absScores.get(word): "-1");
            //info for word
            text+= Integer.toString(id)+" ";
            text+= word+" ";
            text+= Boolean.toString(hasVisual)+" ";
            text+= score+" ";
            
            isFirst = true;
            
            for (Neighbor s: space.getNeighbors(word,10,space2)){
                if (isFirst){
                    text += s.word+" "+ Double.toString(s.sim)+" ";
                    isFirst= false;
                }
                else{
                    text += s.word+" ";
                }
            }
            text +="\n";
            //System.out.println(text);
            id++;
        }
        f.write(text);
        f.close();
        
    }

    
    public static void main(String[] args) throws IOException {
        String absFile = "/home/angeliki/Documents/mikolov_composition/misc/abstract-concrete.txt";
        Images im = new Images(TestConstants.VISION_FILE, true);
        
        

        
        MenCorrelation men = new MenCorrelation(TestConstants.CCG_MEN_FILE);
        MenCorrelation sim999 = new MenCorrelation(TestConstants.SIMLEX_FILE,3);
        MenCorrelation visSim =  new MenCorrelation(TestConstants.Carina_FILE,3);

        
        Set<String> testData = new HashSet<String>();
        testData.addAll(men.getWords());
        testData.addAll(sim999.getWords());
        testData.addAll(visSim.getWords());
        System.out.println(testData.size());
        //SemanticSpace space = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn_again_mm/out_wiki_n10_m0.5_10_r11.0_r21.0l1.0E-6.bin");
        SemanticSpace space = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn/out_wiki_n5_m0.5_5_r11.0_r220.0l1.0E-4.bin");
        String mapFile = "/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn/out_wiki_n5_m0.5_5_r11.0_r220.0l1.0E-4.mapping";
        SimpleMatrix map = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream(mapFile)), false));
        
        Set<String> toMap = new HashSet<String>(testData);
        toMap.retainAll(space.getWord2Index().keySet());
        SemanticSpace wordSpaceMapped = new SemanticSpace(space.getSubSpace(toMap).getWords(), SimpleMatrixUtils.to2DArray((new SimpleMatrix(space.getSubSpace(toMap).getVectors())).mult(map)));
        //space = wordSpaceMapped;
        String outFile = "/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/NNs/B_words.txt";
        
        
        
        ComputeNearestNeighs eval = new ComputeNearestNeighs(absFile,testData,im.getVisionSpace().getWord2Index().keySet());
        
        eval.createList(space.getSubSpace(testData), outFile);
        //eval.createList(space.getSubSpace(testData), outFile,im.getVisionSpace());
       
        

    }

}
