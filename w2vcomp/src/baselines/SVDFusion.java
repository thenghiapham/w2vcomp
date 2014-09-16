package baselines;

import java.util.HashSet;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

import common.MenCorrelation;
import common.SimpleMatrixUtils;
import demo.TestConstants;

import space.SemanticSpace;

public class SVDFusion {

    int k = 0;
    SimpleMatrix trainingData;
    SimpleSVD svdInfo;
    int[] images_dim;
    int[] words_dim;
    
    public SVDFusion(SemanticSpace vision,SemanticSpace language){
        createPairedData(vision, language);
        fuseData();
    }
    
    
    //gets 2 spaces, find common elements, and creates paired data
    private void createPairedData(SemanticSpace vision, SemanticSpace language){
        
        Set<String> common_elements = new HashSet<String>(vision.getWord2Index().keySet());
        common_elements.retainAll(language.getWord2Index().keySet());
        
        
        SimpleMatrix images = new SimpleMatrix(vision.getSubSpace(common_elements).getVectors());
        SimpleMatrix words = new SimpleMatrix(language.getSubSpace(common_elements).getVectors());
        
        images_dim = new int[2];
        images_dim[0] = images.numRows();
        images_dim[1] = images.numCols();
        
        words_dim = new int[2];
        words_dim[0] = words.numRows();
        words_dim[1] = words.numCols();
        
        this.trainingData = SimpleMatrixUtils.hStack(images, words);
    }
    
    //svd's the paired data
    private  void fuseData(){
            this.svdInfo = this.trainingData.svd();
    }
    
    
    //given data and an int, map data to multimodal space by using the first k important components
    public SemanticSpace mapData(SemanticSpace data,int k,boolean isVision){
        int cols,rows;
        
        SimpleMatrix data_mat = new SimpleMatrix(data.getVectors());
        SimpleMatrix data_mat_extended ;
        
        rows = data_mat.numRows();
        if (isVision){
            cols =this.words_dim[1];
            SimpleMatrix zeros = new  SimpleMatrix(rows,cols); 
            data_mat_extended = SimpleMatrixUtils.hStack(data_mat, zeros);
            
        }
        else{
            cols = this.images_dim[1];
            SimpleMatrix zeros = new  SimpleMatrix(rows,cols); 
            data_mat_extended = SimpleMatrixUtils.hStack(zeros,data_mat);
        }
        
        SimpleMatrix mapped = data_mat_extended.mult(this.svdInfo.getV().extractMatrix(0, this.svdInfo.getV().numRows(), 0, k));
        return new SemanticSpace(data.getWords(),SimpleMatrixUtils.to2DArray(mapped));
    }
    
   
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        MenCorrelation men = new MenCorrelation(TestConstants.CCG_MEN_FILE);
        SemanticSpace wordSpace = SemanticSpace.readSpace(TestConstants.VECTOR_FILE);
        SemanticSpace visionSpace = SemanticSpace.importSpace(TestConstants.VISION_FILE);
        SVDFusion baseline = new SVDFusion(wordSpace, visionSpace);
        
        int k=300;
     
        
        
        SemanticSpace mapped_vectors = baseline.mapData(wordSpace, k, false);
        System.out.println("Correlation with Baseline "+k+": " + men.evaluateSpacePearson(mapped_vectors)); 
        System.out.println("Correlation with Original "+men.evaluateSpacePearson(wordSpace)); 

        
      
        
        

    }

}
