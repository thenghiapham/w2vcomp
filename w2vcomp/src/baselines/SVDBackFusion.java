package baselines;

import java.util.HashSet;
import java.util.Set;

import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

import common.MenCorrelation;
import common.SimpleMatrixUtils;
import demo.TestConstants;
import space.SemanticSpace;
import word2vec.Images;

public class SVDBackFusion {

    int k = 0;
    SimpleMatrix trainingData;
    SimpleMatrix textV;
    SimpleMatrix visualV;
    int[] images_dim;
    int[] words_dim;
    
    public SVDBackFusion(SemanticSpace language,SemanticSpace vision){
        createPairedData(language, vision);
        fuseData();
    }
    
    
    //gets 2 spaces, find common elements, and creates paired data
    private void createPairedData(SemanticSpace language, SemanticSpace vision){
        
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
        
        this.trainingData = SimpleMatrixUtils.hStack(words, images);
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
        
        System.out.println(data_mat.numRows());
        System.out.println(data_mat.numCols());
        
        System.out.println(mappedMatrix.numRows());
        System.out.println(mappedMatrix.numCols());
        SimpleMatrix mapped = new SimpleMatrix(mappedMatrix.numCols(), data_mat.numRows());
        CommonOps.solve(mappedMatrix.getMatrix(),data_mat.transpose().getMatrix(),mapped.getMatrix());//mappedMatrix.solve(data_mat.transpose()).transpose();
        return new SemanticSpace(data.getWords(),SimpleMatrixUtils.to2DArray(mapped));
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
        
        Set<String> testData = new HashSet<String>();
        testData.addAll(men.getWords());
        testData.addAll(sim999.getWords());
        testData.addAll(semSim.getWords());
        
        
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/baseline/out_wiki_n-1_m0.5_-1_r11.0_r220.0l1.0E-4.bin");
        
        Images im = new Images(TestConstants.VISION_FILE, true);
        SemanticSpace visionSpace = im.getVisionSpace();
        
        SVDBackFusion baseline = new SVDBackFusion(wordSpace, visionSpace);
        
        int k=300;
     
        wordSpace = baseline.mapData(wordSpace.getSubSpace(testData), k, false);
        

        //double[] cors = im.pairwise_cor(wordSpace);
        
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman2(wordSpace,visionSpace, 1)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman2(wordSpace,visionSpace,1)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman2(wordSpace,visionSpace,1)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman2(wordSpace,visionSpace,1 )); 
        System.out.println();
        
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman2(wordSpace,visionSpace,2)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman2(wordSpace,visionSpace,2)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman2(wordSpace,visionSpace,2)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman2(wordSpace,visionSpace,2 )); 
        System.out.println();
        
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman2(wordSpace,visionSpace,3)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman2(wordSpace,visionSpace,3)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman2(wordSpace,visionSpace,3)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman2(wordSpace,visionSpace,3 )); 
        System.out.println();
       
        
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman2(wordSpace,visionSpace,4)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman2(wordSpace,visionSpace,4)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman2(wordSpace,visionSpace,4)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman2(wordSpace,visionSpace,4 )); 
        System.out.println();
        
        System.out.println("SIMLEX ABS :"+sim999abs.evaluateSpaceSpearman(wordSpace));
        
        System.out.println("MEN EVALUATION: " + men.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("sim999 EVALUATION: " + sim999.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("semSim EVALUATION: " + semSim.evaluateSpaceSpearman(wordSpace)); 
        System.out.println("visSim EVALUATION: " + visSim.evaluateSpaceSpearman(wordSpace )); 
        //System.out.println("Printing pearson "+cors[0]);
        //System.out.println("Printing spearman "+cors[1]);

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

