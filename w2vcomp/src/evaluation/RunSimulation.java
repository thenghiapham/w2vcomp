package evaluation;

import java.io.IOException;
import java.util.Random;

import common.exception.ValueException;

import demo.CrossSituationalLearning;
import demo.TestConstants;

public class RunSimulation {

   

    public static void main(String[] args) throws ValueException, IOException {
       
        Random r = new Random();
        int repeat = 100;
        
        long a = TestConstants.SEED;
        TestConstants.SEED = r.nextLong();
        
        System.out.println(TestConstants.SEED);
        
        //new directory with shuffled corpus
        String main_words = TestConstants.ROOT_EXP_DIR+"corpus/perm_frank/words.txt";
        String main_objects = TestConstants.ROOT_EXP_DIR+"corpus/perm_frank/objects.txt";
        
        for (int i=1;i<repeat;i++){
            
            //for when vector-object shuffling is used
            TestConstants.SEED = r.nextLong();

            //for when corpus shuffling is used
            //TestConstants.SOURCE_FILE_TRAIN =main_words+Integer.toString(i);
            //TestConstants.TARGET_FILE_TRAIN =main_objects+Integer.toString(i);
            
            System.out.println("Running "+i+" simulation!");
            
            //first Learning
            CrossSituationalLearning.main(args);

             
            //The evaluation
            EvalFScore.main(args);
        }
        

    }

}
