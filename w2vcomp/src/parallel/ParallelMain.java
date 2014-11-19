package parallel;

import java.io.File;
import java.util.ArrayList;

import parallel.workers.ParameterAggregatorWorker;
import parallel.workers.ParameterEstimatorWorker;
import parallel.workers.ParameterFinalizer;
import parallel.workers.WorkersMonitor;
import parallel.workers.models.ExampleFinalizer;
import demo.TestConstants;

/**
 * Requirements: Install ZMQ library: http://zeromq.org/intro:get-the-software
 * and the Java bindings http://zeromq.org/bindings:java
 */

public class ParallelMain {

    public static void main(String[] args) {

        /*Configuration constants (to move somewhere else)*/
        File home_path = new File(TestConstants.S_PROJECT_HOME_DIR);

        int aggregatorPort = 5556;
        int estimatorPort = 5557;
        int resultsPort = 5558;

        
        String trainDirPath = TestConstants.S_TRAIN_DIR;
        File trainDir = new File(trainDirPath);
        File[] trainFiles = trainDir.listFiles();

        
        // Create the DeathStar
        WorkersMonitor processMonitor = new WorkersMonitor(
                aggregatorPort, estimatorPort,
                resultsPort);

        // Spaceship launcher
        final Launcher launcher = new MultiprocessLauncher(home_path);

        final ArrayList<String> proccess_ids = new ArrayList<String>();
        
        try {
            launcher.init();

            // Launch a StarDestroyer
            // (Create a job that will centralize the parameters)
            ParameterAggregatorWorker aggregatorJob = new ParameterAggregatorWorker(
                    home_path, processMonitor.getHostname(),
                    aggregatorPort, resultsPort);
            String id = launcher.launch(aggregatorJob);
            proccess_ids.add(id);

            // Launch TIE Fighters
            // (Run parallel parameter-estimating jobs on each training file)
            for (File trainingFile : trainFiles) {
                ParameterEstimatorWorker estimatorJob = new ParameterEstimatorWorker(
                        home_path, processMonitor.getHostname(),
                        estimatorPort, trainingFile.getAbsolutePath());
                id = launcher.launch(estimatorJob);
                proccess_ids.add(id);
            }
            
            //If we go away(Ctrl-C), all the rest of the jobs go down with us
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    System.out.println("Killing zombie workers...");
                    for (String proccess_id : proccess_ids) {
                        try {
                            System.out.println("Killing " + proccess_id);
                            launcher.kill(proccess_id);
                        } catch (RuntimeException e) {
                            System.err.println("Error while killing proccess "
                                    + proccess_id + ": " + e);
                        }
                    }
                    try {
                        launcher.exit();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }

        
        //Hardcoded class that knows what to do when the parameters are done computing
        ParameterFinalizer finalizer = new ExampleFinalizer();
        // Wait for the mission to be complete
        processMonitor.run(finalizer);


    }
}
