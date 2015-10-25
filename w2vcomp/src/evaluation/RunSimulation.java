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
        
        for (int i=1;i<repeat;i++){
            
            TestConstants.SEED = r.nextLong();
            
            System.out.println(TestConstants.SEED);
            
            
            System.out.println("Running "+i+" simulation!");
            //first Learning
            CrossSituationalLearning.main(args);

             
            //The evaluation
            EvalFScore.main(args);
        }
        

    }

}
