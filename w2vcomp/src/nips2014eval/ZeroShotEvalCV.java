package nips2014eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;

import common.HeatMapPanel;
import common.IOUtils;
import common.SimpleMatrixUtils;
import demo.TestConstants;

import space.Neighbor;
import space.SemanticSpace;

public class ZeroShotEvalCV {
    
    long seed ;
    
    SemanticSpace vision;
    SemanticSpace language;
    
    Set<String> skipragramConcepts;
    Set<String> zeroshotConcepts;
    Set<String> allConcepts;
  
    SimpleMatrix trVision;
    SimpleMatrix trLanguage;
    SimpleMatrix tsVision;
    SimpleMatrix tsLanguage;
    
    Set<String> searchConcepts;
    Set<String> trConcepts;
    Set<String> tsConcepts;
    
    static double[] lambdas = {0,1,2,3,4,5,6,7,8,9,10,11,12};
    
    
    enum trainingDirection {
        v2l, l2v;
    }
    
    
    public ZeroShotEvalCV(SemanticSpace language, SemanticSpace vision, boolean normalize ){
        
        
        if (normalize){
            this.language = language.rowNormalize();
            this.vision = vision.rowNormalize();
        }
        else{
            this.language = language;
            this.vision = vision;
        }
        
        createTrainingTestData(language, vision);
       
        
    }
    
    private void createTrainingTestData(SemanticSpace language, SemanticSpace vision){
        
        //find common elements

        allConcepts = new HashSet<String>(vision.getWord2Index().keySet());
        allConcepts.retainAll(language.getWord2Index().keySet());

        System.out.println(allConcepts.size());
        //read in train and test set
        skipragramConcepts = new HashSet<String>(IOUtils.readFile(TestConstants.TRAIN_CONCEPTS));
        skipragramConcepts.retainAll(allConcepts);
        zeroshotConcepts = new HashSet<String>(allConcepts);
        zeroshotConcepts.removeAll(skipragramConcepts);
       
        
        
                
    }
    
    public void startCV(int folds,trainingDirection dir){
        SimpleMatrix mapping, estimated;
        
        ArrayList<String> concepts = new ArrayList<String>(zeroshotConcepts);

        System.out.println("Size of 0shot concepts "+zeroshotConcepts.size());
     
        
        int fold_size = concepts.size()/folds;
        for (double lambda: lambdas){
            double[] ranks= new  double[] {0,0,0,0,0,0};
            for (int i=0;i<folds;i++){
                int start_index = fold_size * i;
                int end_index = start_index + fold_size;
                trConcepts = new HashSet<String>(concepts);
                tsConcepts = new HashSet<String>(concepts.subList(start_index, end_index));
                trConcepts.removeAll(tsConcepts);
                //trConcepts.addAll(skipragramConcepts);
                searchConcepts  = allConcepts;
    
                
                trVision = new SimpleMatrix(vision.getSubSpace(trConcepts).getVectors());
                tsVision = new SimpleMatrix(vision.getSubSpace(tsConcepts).getVectors());
                trLanguage = new SimpleMatrix(language.getSubSpace(trConcepts).getVectors());
                tsLanguage = new SimpleMatrix(language.getSubSpace(tsConcepts).getVectors());
                
                mapping = trainMapping(dir,lambda);
                //HeatMapPanel.plotHeatMap(mapping);
    
                estimated = applyMapping(mapping,dir);
                int j=0;
                for(double r: evalRankAggr(estimated, dir )){
                    ranks[j++] += r/(double)folds;
    
                }
             }
            printRanks(ranks);
        }
    }
    
    
    
    
    private static SimpleMatrix solve(SimpleMatrix A, SimpleMatrix B,double lambda) {
        SimpleMatrix ATA = A.transpose().mult(A);
        int size = ATA.numCols();
        SimpleMatrix eye = SimpleMatrix.identity(size).scale(lambda);
        SimpleMatrix eyeTeye = eye.transpose().mult(eye);
        
        return (ATA.plus(eyeTeye)).pseudoInverse().mult(A.transpose()).mult(B);
        //return SimpleMatrix.identity(size);
        
    }
    
    private SimpleMatrix trainMapping(trainingDirection dir, double lambda){
        
        switch(dir){
        case v2l:
            return solve(trVision,trLanguage,lambda);
        case l2v:
            return solve(trLanguage,trVision, lambda);
        }
        return null;
    }
    
   
    
    private SimpleMatrix applyMapping(SimpleMatrix mappingF, trainingDirection dir){
        
        
        switch(dir){
        case v2l:
            return tsVision.mult(mappingF);
        case l2v:
            return tsLanguage.mult(mappingF);
        }
        
        return null;
    }
    
    private double[] evalRankAggr(SimpleMatrix vectors, trainingDirection dir ){
        double[] ranks= new  double[] {0,0,0,0,0,0 };
        
        SemanticSpace space = new SemanticSpace(new ArrayList<String>(tsConcepts),SimpleMatrixUtils.to2DArray(vectors));
        SemanticSpace evalSpace = null ;
        
        switch (dir){
        case l2v:
            evalSpace = vision.getSubSpace(searchConcepts);
            break;
        case v2l:
            evalSpace = language.getSubSpace(searchConcepts);
            break;
        }
        //System.out.println("size of eval space "+searchConcepts.size());
        int numOfNs = 100;
        for (String word: tsConcepts){
            Neighbor[] NNs = space.getNeighbors(word, numOfNs,  evalSpace);
            //System.out.println(word+" ---->"+NNs[0].word+" "+NNs[0].sim);
            for (int i=0;i<numOfNs;i++){
               
                if (NNs[i].word==word){
                    if (i==0){
                        ranks[0]+=1; ranks[1]+=1; ranks[2]+=1; ranks[3]+=1; ranks[4]+=1;  ranks[5]+=1;
                        break;
                    }
                    if (i==1){
                        ranks[1]+=1; ranks[2]+=1; ranks[3]+=1; ranks[4]+=1;  ranks[5]+=1;
                        break;
                    }
                    if (i<=10){
                        ranks[2]+=1; ranks[3]+=1; ranks[4]+=1;  ranks[5]+=1;
                        break;
                    }
                    if (i<=20){
                        ranks[3]+=1; ranks[4]+=1;  ranks[5]+=1;
                        break;
                    }
                    if (i<=50){
                        ranks[4]+=1; ranks[5]+=1; 
                        break;
                    }
                    if (i<=100){
                        ranks[5]+=1;
                        break;
                    }
                }
            }
            
        }
        //System.out.println(r/(double) tsConcepts.size());
        for (int i=0;i<ranks.length;i++){
            ranks[i] /= (double) tsConcepts.size();
        }
        
        return ranks;
        
    }
    
    
    
    
    public void printRanks(double [] ranks){
        System.out.print("P@1: "+ranks[0] * 100+" ");
        System.out.print("P@2: "+ranks[1] * 100+" ");
        System.out.print("P@10: "+ranks[2] * 100+" ");
        System.out.print("P@20: "+ranks[3] * 100+" ");
        System.out.print("P@50: "+ranks[4] * 100+" ");
        System.out.println();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        long seed = System.nanoTime();
        //long seed = TestConstants.SEED;
        System.out.println("Seed is "+seed);
        
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/inverseProb/out_enwiki9_20z.bin");
        
        SemanticSpace visionSpace = SemanticSpace.importSpace(TestConstants.VISION_FILE);
        
        
        trainingDirection dir = trainingDirection.v2l;
        
        //training mapping
        ZeroShotEvalCV exp = new ZeroShotEvalCV(wordSpace,visionSpace,false);
        
        
        //evaluate mapping
        exp.startCV(5,dir);
        
        
        
     
        
     

    }

}
