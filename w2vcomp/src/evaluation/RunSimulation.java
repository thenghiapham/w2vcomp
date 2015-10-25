package evaluation;

import java.io.IOException;

import common.exception.ValueException;

import demo.CrossSituationalLearning;

public class RunSimulation {

   

    public static void main(String[] args) throws ValueException, IOException {
        
        int repeat = 100;
        
        
        for (int i=1;i<repeat;i++){
            System.out.println("Running "+i+" simulation!");
            //first Learning
            CrossSituationalLearning.main(args);

             
            //The evaluation
            EvalFScore.main(args);
        }
        

    }

}
