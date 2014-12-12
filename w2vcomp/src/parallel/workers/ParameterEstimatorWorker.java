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
    private Integer worker_id;

    public ParameterEstimatorWorker(Integer worker_id, File home_path, String hostname,
            int aggregatorPort, String training_file) {
        this.worker_id = worker_id;
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
                parameterMessager.sendInit(worker_id).getContent();
        
        System.out.println("Running estimation");
        estimator.run(worker_id, initParameters, parameterMessager);

        System.out.println("Estimation done");
        parameterMessager.sendEnd(worker_id);
        
        // We never get here but clean up anyhow
        aggParamsRequester.close();
        context.term();

    }

    @Override
    public String[] getArgs() {
        return new String[] { worker_id.toString(), hostname,
                new Integer(aggregatorPort).toString(), training_file };
    }

    public static void main(String[] args) {
        Integer worker_id = Integer.parseInt(args[0]);
        File home_path = new File(args[1]);
        String monitorHostname = args[2];
        int aggregatorPort = Integer.parseInt(args[3]);
        String trainingFile = args[4];

        ParameterEstimatorWorker paramEst = new ParameterEstimatorWorker(worker_id,
                home_path, monitorHostname, aggregatorPort, trainingFile);
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
