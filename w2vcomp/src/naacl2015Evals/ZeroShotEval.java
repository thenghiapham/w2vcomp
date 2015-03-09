package naacl2015Evals;

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
import word2vec.Images;

public class ZeroShotEval {
    
    long seed ;
    
    SemanticSpace vision;
    SemanticSpace language;
    
    Set<String> trConcepts;
    Set<String> tsConcepts;
    Set<String> allConcepts;
    
    SimpleMatrix trVision ;
    SimpleMatrix tsVision ;
    SimpleMatrix trLanguage ;
    SimpleMatrix tsLanguage ;
    
    
    
    
    enum trainingDirection {
        v2l, l2v;
    }
    
    
    public ZeroShotEval(SemanticSpace language, SemanticSpace vision, boolean normalize){
        
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
        
        
        /* //Create random set from seed 
        //create training and testing concepts
        ArrayList<String> el = new ArrayList<String>(allConcepts);
        Collections.shuffle(el,new Random(seed));
        trConcepts = new HashSet<String>(el.subList(0, 1769));
        tsConcepts = new HashSet<String>(el.subList(1769, el.size()-1));
        for (String w: trConcepts){
            System.out.println(w);
        }*/
        
        
        //read in train and test set
        trConcepts = new HashSet<String>(IOUtils.readFile(TestConstants.TRAIN_CONCEPTS));
        trConcepts.retainAll(allConcepts);
        tsConcepts = new HashSet<String>(allConcepts);
        tsConcepts.removeAll(trConcepts);
        
        
        System.out.println(trConcepts.size()+" "+tsConcepts.size());
        System.out.println(allConcepts.size());
        
        //create training and testing spaces
       
        trVision = new SimpleMatrix(vision.getSubSpace(trConcepts).getVectors());
        tsVision = new SimpleMatrix(vision.getSubSpace(tsConcepts).getVectors());
        trLanguage = new SimpleMatrix(language.getSubSpace(trConcepts).getVectors());
        tsLanguage = new SimpleMatrix(language.getSubSpace(tsConcepts).getVectors());
        
                
    }
    
    
    private static SimpleMatrix solve(SimpleMatrix A, SimpleMatrix B) {
        SimpleMatrix ATA = A.transpose().mult(A);
        //11 and 2.5
        double lambda = 10;
        int size = ATA.numCols();
        SimpleMatrix eye = SimpleMatrix.identity(size).scale(lambda);
        SimpleMatrix eyeTeye = eye.transpose().mult(eye);
        return (ATA.plus(eyeTeye)).pseudoInverse().mult(A.transpose()).mult(B);
        //return SimpleMatrix.identity(size);
        
    }
    
    public SimpleMatrix trainMapping(trainingDirection dir){
        
        switch(dir){
        case v2l:
            System.out.println("Training v2l "+trConcepts.size()+" concepts");
            return solve(trVision,trLanguage);
        case l2v:
            System.out.println("Training l2v for "+trConcepts.size()+" concepts");
            return solve(trLanguage,trVision);
        }
        return null;
    }
    
    
    public SimpleMatrix applyMapping(SimpleMatrix mappingF, trainingDirection dir){
        
        
        switch(dir){
        case v2l:
            System.out.println("Mapping v2l "+tsConcepts.size()+" concepts");
            return tsVision.mult(mappingF);
        case l2v:
            System.out.println("Mapping l2v for "+tsConcepts.size()+" concepts");
            return tsLanguage.mult(mappingF);
        }
        
        return null;
    }
    
    public double[] evalRankAggr(SimpleMatrix vectors, trainingDirection dir){
        double[] ranks= new  double[] {0,0,0,0,0,0 };
        
        SemanticSpace space = new SemanticSpace(new ArrayList<String>(tsConcepts),SimpleMatrixUtils.to2DArray(vectors));
        SemanticSpace evalSpace = null ;
        
        switch (dir){
        case l2v:
            evalSpace = vision.getSubSpace(allConcepts);
            break;
        case v2l:
            evalSpace = language.getSubSpace(allConcepts);
            break;
        }
        int r=0;
        System.out.println("size of eval space "+evalSpace.getWords().length);
        int numOfNs = 100;
        for (String word: tsConcepts){
            Neighbor[] NNs = space.getNeighbors(word, numOfNs,  evalSpace);
            //System.out.println(word+" ---->"+NNs[0].word+" "+NNs[0].sim);
            for (int i=0;i<numOfNs;i++){
               
                if (NNs[i].word==word){
                    r+=i;
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
        System.out.println(r/(double) tsConcepts.size());
        for (int i=0;i<ranks.length;i++){
            ranks[i] /= (double) tsConcepts.size();
        }
        
        return ranks;
        
    }
    
    
    
    
    private void printRanks(double [] ranks){
        System.out.println("P@1: "+ranks[0] * 100);
        System.out.println("P@2: "+ranks[1] * 100);
        System.out.println("P@10: "+ranks[2] * 100);
        System.out.println("P@20: "+ranks[3] * 100);
        System.out.println("P@50: "+ranks[4] * 100);


    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        long seed = System.nanoTime();
        //long seed = TestConstants.SEED;
        System.out.println("Seed is "+seed);
        
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn_again_mm_zero/out_wiki_n10_m0.5_10_r11.0_r21.0l1.0E-6.bin");
        Images im = new Images(TestConstants.VISION_FILE, true);
        SemanticSpace visionSpace = im.getVisionSpace();
        
        
        trainingDirection dir = trainingDirection.v2l;
        
        //training mapping
        ZeroShotEval exp = new ZeroShotEval(wordSpace,visionSpace,false);
        SimpleMatrix mappingF = exp.trainMapping(dir);
        SimpleMatrix estimated = exp.applyMapping(mappingF,dir);

        HeatMapPanel.plotHeatMap(mappingF);
        System.out.println(mappingF);
        System.out.println(SimpleMatrixUtils.elementMax(mappingF));
        System.out.println(SimpleMatrixUtils.elementMin(mappingF));
        //evaluate mapping
        double[] ranks = exp.evalRankAggr(estimated, dir);
        exp.printRanks(ranks);
        //without mapping
        System.out.println("Without applying mapping");
        //without mapping for l2v
        if (dir == trainingDirection.l2v){
            estimated = new SimpleMatrix(wordSpace.getSubSpace(exp.tsConcepts).getVectors());
        }
        else{ //without mapping for v2l
            estimated = new SimpleMatrix(visionSpace.getSubSpace(exp.tsConcepts).getVectors());
        }
        //evaluate mapping

        ranks = exp.evalRankAggr(estimated, dir);
        exp.printRanks(ranks);
        
        
        
        
     

    }

    

}
