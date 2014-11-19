package parallel.workers;

import java.io.IOException;

import org.zeromq.ZMQ;

import parallel.comm.MessageBroker;
import parallel.comm.NetUtils;
import utils.SerializationUtils;

/**
 * This is the DeathStar's core. I allows communication between (from the
 * submission host) the StarDestroyer (ParameterAggregator) and the TIE Fighters
 * (ParameterEstimator)
 * 
 * @author german
 * 
 */
public class WorkersMonitor  {

    private String hostname;
    private int    aggregatorPort;
    private int    estimatorsPort;
    private int    resultsPort;

    public WorkersMonitor(int aggregatorPort, int estimatorsPort,
            int resultsPort) {
        this.aggregatorPort = aggregatorPort;
        this.estimatorsPort = estimatorsPort;
        this.resultsPort = resultsPort;

        // Get the submission host
        try {
            hostname = NetUtils.getHostname();
        } catch (IOException e) {
            throw new RuntimeException(
                    "Couldn't determine the submission host name: " + e);
        }

    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void run(ParameterFinalizer finalizer) {
        final MessageBroker messageBroker = new MessageBroker(
                aggregatorPort, estimatorsPort);
        final Thread proxy = new Thread(messageBroker);
        proxy.start();

        ZMQ.Context context = ZMQ.context(1);
        // Socket that will be used to receive communications (from the star
        // destroyer)
        ZMQ.Socket resultReqSocket = context.socket(ZMQ.REQ);

        resultReqSocket.bind("tcp://*:" + resultsPort);
        resultReqSocket.send("You are in command now, Admiral Piett", 0);

        System.out.println("Wating for result...");
        byte[] final_result = resultReqSocket.recv();

        ModelParameters finalParameters = (ModelParameters) SerializationUtils.deserialize(final_result);
        finalizer.finish(finalParameters);
                

        messageBroker.stop(context);
        System.out.println("Stopped");
        try {
            proxy.join();
        } catch (InterruptedException e) {
            //Do nothing
        }
        resultReqSocket.close();
        context.term();
    }

}
