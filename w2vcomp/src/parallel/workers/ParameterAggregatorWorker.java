package parallel.workers;

import java.io.File;
import java.io.Serializable;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import parallel.comm.ParameterMessage;
import parallel.workers.count.LineNumAggregator;
import utils.SerializationUtils;

/**
 * This the StarDestroyer. It coordinates the work (learns the global
 * parameters) between the actual workers.
 * 
 * It communicates with them through a proxy installed in the DeathStar.
 * 
 * @author german
 */
public class ParameterAggregatorWorker implements Launchable {

    private String monitorHostname;
    private int    estimatorsPort;
    private int    monitorPort;

    public ParameterAggregatorWorker(File home_path, String monitorHostname,
            int estimatorsPort, int monitorPort) {
        this.monitorHostname = monitorHostname;
        this.estimatorsPort = estimatorsPort;
        this.monitorPort = monitorPort;

    }

    public void run(ParameterAggregator parameterAggregator) {

        Context context = ZMQ.context(1);

        Socket aggParamsResponder = context.socket(ZMQ.REP);
        aggParamsResponder.connect("tcp://" + monitorHostname + ":"
                + estimatorsPort);

        Socket monitorResponder = context.socket(ZMQ.REP);
        monitorResponder
                .connect("tcp://" + monitorHostname + ":" + monitorPort);

        // Get the order to start working (hopefully it will be already waiting
        // for us)
        monitorResponder.recv();

        int n_workers = 0;

        // Socket to talk to server
        while (!Thread.currentThread().isInterrupted()) {
            // Wait for next request from client
            ParameterMessage msg = (ParameterMessage) SerializationUtils
                    .deserialize(aggParamsResponder.recv());

            Serializable res;
            switch (msg.getType()) {
            case "INIT":
                res = new ParameterMessage("INIT_REPLY",
                        parameterAggregator.getAggregatedParameters());
                n_workers += 1;
                aggParamsResponder.send(SerializationUtils.serialize(res), 0);
                break;
            case "UPDATE":
                ModelParameters aggregated = parameterAggregator.aggregate(msg
                        .getContent());
                res = new ParameterMessage("UPDATE_REPLY", aggregated);
                aggParamsResponder.send(SerializationUtils.serialize(res), 0);
                break;
            case "END":
                res = new ParameterMessage("END_REPLY", null);
                n_workers -= 1;
                // We are done!
                if (n_workers == 0)
                    Thread.currentThread().interrupt();
                aggParamsResponder.send(SerializationUtils.serialize(res), 0);
                break;
            default:

            }
        }
        // Send reply back to client
        aggParamsResponder.close();

        // Sir, we found the rebels
        ModelParameters result = parameterAggregator.getAggregatedParameters();
        System.out.println("Sending results");
        monitorResponder.send(SerializationUtils.serialize(result), 0);
        System.out.println("results sent");

        System.out.println("closing");
        monitorResponder.close();
        System.out.println("term");
        context.term();
        System.out.println("done");
    }

    @Override
    public String[] getArgs() {
        return new String[] { monitorHostname,
                new Integer(estimatorsPort).toString(),
                new Integer(monitorPort).toString(), };
    }

    public static void main(String[] args) {
        File home_path = new File(args[0]);
        String monitorHostname = args[1];
        int estimatorsPort = Integer.parseInt(args[2]);
        int monitorPort = Integer.parseInt(args[3]);

        ParameterAggregatorWorker paramAgg = new ParameterAggregatorWorker(
                home_path, monitorHostname, estimatorsPort, monitorPort);

        //Hardcoded task to run
        ParameterAggregator parameterAggregator = new LineNumAggregator();

        paramAgg.run(parameterAggregator);
    }

}
