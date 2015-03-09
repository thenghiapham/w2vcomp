package baselines;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

import common.IOUtils;
import common.MenCorrelation;
import common.SimpleMatrixUtils;
import demo.TestConstants;

import space.SemanticSpace;
import word2vec.Images;

public class SVDFusion {

    int k = 0;
    SimpleMatrix trainingData;
    Set<String> trainingWords;
    
    SimpleMatrix testData;
    SimpleSVD svdInfo;
    int[] images_dim;
    int[] words_dim;
    
    public SVDFusion(SemanticSpace language,SemanticSpace vision){
        createPairedData(language,vision);
        fuseData();
    }
    
    
    //gets 2 spaces, find common elements, and creates paired data
    private void createPairedData(SemanticSpace language, SemanticSpace vision){
        
        trainingWords = new HashSet<String>(vision.getWord2Index().keySet());
        trainingWords.retainAll(language.getWord2Index().keySet());
        
        SimpleMatrix images = new SimpleMatrix((vision.getSubSpace(trainingWords)).rowNormalize().getVectors());
        SimpleMatrix words = new SimpleMatrix((language.getSubSpace(trainingWords)).rowNormalize().getVectors());
        
        images_dim = new int[2];
        images_dim[0] = images.numRows();
        images_dim[1] = images.numCols();
        
        words_dim = new int[2];
        words_dim[0] = words.numRows();
        words_dim[1] = words.numCols();
        
        this.trainingData = SimpleMatrixUtils.hStack(words, images);
    }
    
    //svd's the paired data
    private  void fuseData(){
            this.svdInfo = this.trainingData.svd();
    }
    
    
    //given data and an int, map data to multimodal space by using the first k important components
    public SemanticSpace mapData(SemanticSpace data, int k,boolean isVision){
        int cols,rows;
        
        Set<String> testWords = new HashSet<String>(data.getWord2Index().keySet());
        testWords.removeAll(trainingWords);
        
        SimpleMatrix data_mat = new SimpleMatrix(data.getSubSpace(testWords).getVectors());
        SimpleMatrix data_mat_extended ;
        
        rows = data_mat.numRows();
        if (isVision){
            cols = this.words_dim[1];
            SimpleMatrix zeros = new  SimpleMatrix(rows,cols); 
            data_mat_extended = SimpleMatrixUtils.hStack( zeros,data_mat);
            
        }
        else{
            cols = this.images_dim[1];
            SimpleMatrix zeros = new  SimpleMatrix(rows,cols); 
            data_mat_extended = SimpleMatrixUtils.hStack(data_mat,zeros);
        }
        
        System.out.println(this.svdInfo.getU().numCols()+" "+this.svdInfo.getU().numRows());
        System.out.println(this.svdInfo.getV().numCols()+" "+this.svdInfo.getV().numRows());
        
        SimpleMatrix mapped = data_mat_extended.mult(this.svdInfo.getV().extractMatrix(0, this.svdInfo.getV().numRows(), 0, k));
        SimpleMatrix SVDed = (this.svdInfo.getU().mult(this.svdInfo.getW().extractMatrix(0, this.svdInfo.getW().numRows(), 0, k)));

      

        SimpleMatrix all = SimpleMatrixUtils.vStack(mapped,SVDed);
        ArrayList<String> allWords = new ArrayList<String>();
        for (String w: testWords){
            allWords.add(w);
        }
        for (String w: trainingWords){
            allWords.add(w);
        }
        return new SemanticSpace(allWords,SimpleMatrixUtils.to2DArray(all));
    }
    
   
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        MenCorrelation men = new MenCorrelation(TestConstants.CCG_MEN_FILE);
        MenCorrelation sim999 = new MenCorrelation(TestConstants.SIMLEX_FILE,3);
        MenCorrelation semSim =  new MenCorrelation(TestConstants.Carina_FILE,2);
        MenCorrelation visSim =  new MenCorrelation(TestConstants.Carina_FILE,3);
        MenCorrelation sim999abs = new MenCorrelation(TestConstants.SIMLEX_FILE,3,6,1);
        HashSet<String> common_elements = new HashSet<String>(IOUtils.readFile("/home/angeliki/Documents/mikolov_composition/misc/common_elements_alternative"));

        Set<String> testData = new HashSet<String>();
        testData.addAll(men.getWords());
        testData.addAll(sim999.getWords());
        testData.addAll(semSim.getWords());
        //testData.addAll(men)
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/baseline/out_wiki_n-1_m0.5_-1_r11.0_r220.0l1.0E-4.bin");
        
        Images im = new Images(TestConstants.VISION_FILE, true);
        SemanticSpace visionSpace = im.getVisionSpace();
        
        SVDFusion baseline = new SVDFusion(wordSpace, visionSpace);
        
        int k=300;
     
        
        
        wordSpace = baseline.mapData(wordSpace.getSubSpace(testData).rowNormalize(), k, false);
        double[] cors = im.pairwise_cor(wordSpace);
        
        System.out.println("Evaluating only common in all spaces elements");

        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman2(wordSpace,common_elements, 1)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman2(wordSpace,common_elements,1)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman2(wordSpace,common_elements,1)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman2(wordSpace,common_elements,1 )); 
        System.out.println();
        
        
        
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman(wordSpace )); 
      
        
        



        
      
        
        

    }

}
