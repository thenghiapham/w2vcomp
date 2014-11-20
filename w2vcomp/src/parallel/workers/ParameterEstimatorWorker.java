package parallel.workers;

import io.sentence.PlainSentenceInputStream;
import io.sentence.SentenceInputStream;
import io.word.PushBackWordStream;

import java.io.File;
import java.io.IOException;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import parallel.comm.ParameterMessager;
import parallel.workers.w2v.SkipGramEstimator;

/**
 * This is the actual worker (TIE Fighter)
 * 
 * @author german
 * 
 */
public class ParameterEstimatorWorker implements Launchable  {

    private String training_file;
    private String hostname;
    private int    aggregatorPort;

    public ParameterEstimatorWorker(File home_path, String hostname,
            int aggregatorPort, String training_file) {
        this.hostname = hostname;
        this.aggregatorPort = aggregatorPort;
        this.training_file = training_file;

    }

    public void run(ParameterEstimator estimator) {
        Context context = ZMQ.context(1);
        Socket aggParamsRequester = context.socket(ZMQ.REQ);
        aggParamsRequester.connect("tcp://" + hostname + ":" + aggregatorPort);

        ParameterMessager parameterMessager = new ParameterMessager(aggParamsRequester);
        
        System.out.println("Getting initial parameters");
        ModelParameters initParameters =
                parameterMessager.sendInit().getContent();
        
        System.out.println("Running estimation");
        estimator.run(initParameters, parameterMessager);

        System.out.println("Estimation done");
        parameterMessager.sendEnd();
        
        // We never get here but clean up anyhow
        aggParamsRequester.close();
        context.term();

    }

    @Override
    public String[] getArgs() {
        return new String[] { hostname,
                new Integer(aggregatorPort).toString(), training_file };
    }

    public static void main(String[] args) {
        File home_path = new File(args[0]);
        String monitorHostname = args[1];
        int aggregatorPort = Integer.parseInt(args[2]);
        String trainingFile = args[3];

        ParameterEstimatorWorker paramEst = new ParameterEstimatorWorker(home_path,
                monitorHostname, aggregatorPort, trainingFile);
        System.out.println("Train file: " + trainingFile);
        try {
            SentenceInputStream sentenceInputStream = new PlainSentenceInputStream(
                    new PushBackWordStream(trainingFile, 100));
            //Hardcoded task to run
            ParameterEstimator estimator = new SkipGramEstimator(sentenceInputStream);
            paramEst.run(estimator);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
