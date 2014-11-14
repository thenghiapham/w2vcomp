package parallel.workers;

import java.io.File;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import parallel.comm.ParameterMessager;
import parallel.workers.models.ExampleEstimator;

/**
 * This is the actual worker (TIE Fighter)
 * 
 * @author german
 * 
 */
public class ParameterEstimatorWorker extends BaseClusterLaunchable  {

    private String training_file;
    private String hostname;
    private int    aggregatorPort;

    public ParameterEstimatorWorker(File home_path, String hostname,
            int aggregatorPort, String training_file) {
        super(home_path);
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
    public JobTemplate getJobTemplate(Session session) throws DrmaaException {
        return getJobTemplate(session, new String[] { hostname,
                new Integer(aggregatorPort).toString(), training_file });
    }

    public static void main(String[] args) {
        File home_path = new File(args[0]);
        String monitorHostname = args[1];
        int aggregatorPort = Integer.parseInt(args[2]);
        String trainingFile = args[3];

        ParameterEstimatorWorker paramEst = new ParameterEstimatorWorker(home_path,
                monitorHostname, aggregatorPort, trainingFile);
        
        //Hardcoded task to run
        ParameterEstimator estimator = new ExampleEstimator();
        paramEst.run(estimator);
    }
}
