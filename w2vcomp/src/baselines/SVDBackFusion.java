package baselines;

import java.util.HashSet;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

import common.MenCorrelation;
import common.SimpleMatrixUtils;
import demo.TestConstants;
import space.SemanticSpace;

public class SVDBackFusion {

    int k = 0;
    SimpleMatrix trainingData;
    SimpleMatrix textV;
    SimpleMatrix visualV;
    int[] images_dim;
    int[] words_dim;
    
    public SVDBackFusion(SemanticSpace vision,SemanticSpace language){
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
    @SuppressWarnings("unchecked")
    private  void fuseData(){
            SimpleSVD<SimpleMatrix> svdInfo = this.trainingData.svd();
            SimpleMatrix v = svdInfo.getV();
            int rank = v.numCols();
            textV = v.extractMatrix(0, words_dim[1], 0, rank);
            visualV = v.extractMatrix(words_dim[1], words_dim[1] + images_dim[1], 0, rank);
    }
    
    
    //given data and an int, map data to multimodal space by using the first k important components
    public SemanticSpace mapData(SemanticSpace data,int k,boolean isVision){
        
        SimpleMatrix data_mat = new SimpleMatrix(data.getVectors());
        
        SimpleMatrix mappedMatrix;
//        SimpleMatrix toBeMappedMatrix;
        if (isVision){
            mappedMatrix = visualV.extractMatrix(0, visualV.numRows(), 0, k);
//            toBeMappedMatrix = textV.extractMatrix(0, textV.numRows(), 0, k);
        }
        else{
            mappedMatrix = textV.extractMatrix(0, textV.numRows(), 0, k);
//            toBeMappedMatrix = visualV.extractMatrix(0, visualV.numRows(), 0, k);
        }
        
        SimpleMatrix mapped = mappedMatrix.solve(data_mat.transpose()).transpose();
        return new SemanticSpace(data.getWords(),SimpleMatrixUtils.to2DArray(mapped));
    }
    
   
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        MenCorrelation men = new MenCorrelation(TestConstants.CCG_MEN_FILE);
        SemanticSpace wordSpace = SemanticSpace.readSpace(TestConstants.VECTOR_FILE);
        SemanticSpace visionSpace = SemanticSpace.importSpace(TestConstants.VISION_FILE);
        SVDBackFusion baseline = new SVDBackFusion(wordSpace, visionSpace);
        
        int k=300;
     
        
        
        SemanticSpace mapped_vectors = baseline.mapData(wordSpace, k, false);
        System.out.println("Correlation with Baseline "+k+": " + men.evaluateSpacePearson(mapped_vectors)); 
        System.out.println("Correlation with Original "+men.evaluateSpacePearson(wordSpace)); 

    }
    
    public static void smallTest() {
        double[][] data = {{1,2,2,2},{3,1,1,6},{4,1,1,8},{2,5,5,4}};
        SimpleMatrix a = new SimpleMatrix(data);
        @SuppressWarnings("rawtypes")
        SimpleSVD svdInfo = a.svd();
        SimpleMatrix u = svdInfo.getU().extractMatrix(0, 4, 0, 2);
        SimpleMatrix v = svdInfo.getV().extractMatrix(0, 4, 0, 2);
        SimpleMatrix s = svdInfo.getW().extractMatrix(0, 2, 0, 2);
        SimpleMatrix aPrime = u.mult(s.mult(v.transpose()));
        System.out.println(aPrime);
        
        SimpleMatrix tVec = new SimpleMatrix(new double[][]{{6,5}});
        SimpleMatrix v02 = v.extractMatrix(0, 2, 0, 2);
        SimpleMatrix v24 = v.extractMatrix(2, 4, 0, 2);
        SimpleMatrix multVec = v02.solve(tVec.transpose());
        SimpleMatrix vVec = v24.mult(multVec).transpose();
        System.out.println(vVec);
    }

}

