package evaluation;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;

import org.ejml.simple.SimpleMatrix;

import space.Neighbor;
import space.SemanticSpace;

import common.HeatMapPanel;
import common.IOUtils;
import common.SimpleMatrixUtils;
import demo.TestConstants;

public class ImageRetrievalFlickr8k {
    
    SemanticSpace imageSpace ;
    SemanticSpace captionSpace;
    SemanticSpace coreSpace;
    SimpleMatrix map;
    SemanticSpace visionSpace = SemanticSpace.importSpace(TestConstants.VISION_FILE);

    
    
    public ImageRetrievalFlickr8k(String semanticSpace, String images, String captions, String mapFile) throws FileNotFoundException{
        
        coreSpace = SemanticSpace.readSpace(semanticSpace);
        /*map =  new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream(mapFile)), false));
        map = solve(coreSpace, visionSpace,4);
        HeatMapPanel.plotHeatMap(map);
        
        imageSpace =  new SemanticSpace(imageSpace.getWords(), SimpleMatrixUtils.to2DArray((new SimpleMatrix(imageSpace.getVectors())).mult(map)));*/
        imageSpace = SemanticSpace.importSpace(images);
        
        readFlickrDataset(captions);
        
        
    }
    
    private static SimpleMatrix solve(SemanticSpace a, SemanticSpace b,double lambda) {
        
        HashSet<String> allConcepts = new HashSet<String>(a.getWord2Index().keySet());
        allConcepts.retainAll(b.getWord2Index().keySet());
        
        SimpleMatrix A = new SimpleMatrix(a.getSubSpace(allConcepts).getVectors());
        SimpleMatrix B = new SimpleMatrix(b.getSubSpace(allConcepts).getVectors());
        
        SimpleMatrix ATA = A.transpose().mult(A);
        int size = ATA.numCols();
        SimpleMatrix eye = SimpleMatrix.identity(size).scale(lambda);
        SimpleMatrix eyeTeye = eye.transpose().mult(eye);
        
        return (ATA.plus(eyeTeye)).pseudoInverse().mult(A.transpose()).mult(B);
        //return SimpleMatrix.identity(size);
        
    }

    private void  readFlickrDataset(String dataset) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        double[][] vectors = new double[data.size()][coreSpace.getVectorSize()];
        
        ArrayList<String> rows = new ArrayList<String>();
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split("\t");
            String captionName = elements[0];
            ArrayList<String> sentence = new ArrayList<String>();
           
            for (String word: elements[1].split(" ")){
                word = word.toLowerCase();
                if (word == "." || word =="," || word =="?" || word =="!" || word == ";" || word == ":" || word == "@" || word == "&"){
                    continue;
                }
                if (word == "and" || word =="or" || word =="a" || word =="an" || word == "the"){
                    continue;
                }
                if (sentence.contains(word)){
                    continue;
                }
                sentence.add(word);
            }
            //SimpleMatrix vec = (SimpleMatrixUtils.sumRows((new SimpleMatrix(coreSpace.getSubSpace(sentence).getVectors())).mult(map)));
            //SimpleMatrix vec = (SimpleMatrixUtils.sumRows((new SimpleMatrix(coreSpace.getSubSpace(sentence).getVectors())))).scale(1/(double)sentence.size()).mult(map);
            SimpleMatrix vec = SimpleMatrixUtils.sumRows((new SimpleMatrix(coreSpace.getSubSpace(sentence).getVectors())));
            
            vectors[i] = SimpleMatrixUtils.to2DArray(vec)[0];
            rows.add(captionName);
        }
        captionSpace = new SemanticSpace(rows, vectors);
    }
    
    private double[] evaluationRetrieval(){
        double[] ranks= new  double[] {0,0,0, 0};

        //for every caption
        int med = 0;
        for (String captionId: captionSpace.getWords()){
            String caption = captionId.split("#")[0];
            //rank images
            Neighbor[] NNs = captionSpace.getNeighbors(captionId, 1002,imageSpace);
            
            for (int i=0;i<NNs.length;i++){
                //System.out.println(NNs[i].word +" "+caption);
                if (NNs[i].word.startsWith(caption)){
                    med +=i;
                    if (i==0){
                        ranks[0]+=1; ranks[1]+=1; ranks[2]+=1;  ranks[3]+=1; 
                        break;
                    }
                    if (i<=4){
                        ranks[1]+=1; ranks[2]+=1; ranks[3]+=1; 
                        break;
                    }
                    if (i<=9){
                        ranks[2]+=1; ranks[3]+=1; 
                        break;
                    }
                    if (i<=50){
                        ranks[3]+=1; 
                        break;
                    }
                }
            }
        }
        System.out.println("Median is "+med/(double)captionSpace.getWords().length);
        for (int i=0;i<ranks.length;i++){
            ranks[i] /= (double) captionSpace.getWords().length;
        }
        return ranks;
    }
    
    public static void printRanks(double [] ranks){
        System.out.print("R@1: "+ranks[0] * 100+" ");
        System.out.print("R@5: "+ranks[1] * 100+" ");
        System.out.print("R@10: "+ranks[2] * 100+" ");
        System.out.print("R@50: "+ranks[3] * 100+" ");
       
        System.out.println();
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        String coreSpace = "/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/mm_models/out_wiki_n20_m0.5_20_r11.0_r21.0l1.0E-6.bin";
        String mapFile = "/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/r2_10/out_enwik9_n5_m0.5_5_r11.0_r210.0l1.0E-4.mapping";
        String images = "/home/angeliki/sata/mmskipgram/flickr8k/data/vectors/testImages/pmisvd.dm";
        String captions  = "/home/angeliki/sata/mmskipgram/flickr8k/data/captions/Flickr_8k.testImages.lemma.token.txt";
       
        
        ImageRetrievalFlickr8k eval = new ImageRetrievalFlickr8k(coreSpace, images, captions, mapFile);
        double[] ranks = eval.evaluationRetrieval();
        ImageRetrievalFlickr8k.printRanks(ranks);
        
    }

}
