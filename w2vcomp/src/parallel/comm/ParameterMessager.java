package parallel.comm;

import java.io.Serializable;

import org.zeromq.ZMQ.Socket;

import parallel.workers.ModelParameters;

import utils.SerializationUtils;

public class ParameterMessager {
    Socket socket;

    public ParameterMessager(Socket socket) {
        this.socket = socket;
    }
    
    private ParameterMessage sendMessage(String type, ModelParameters content) {
        Serializable msg = new ParameterMessage(type, content);
        socket.send(SerializationUtils.serialize(msg), 0);
        ParameterMessage reply = (ParameterMessage) SerializationUtils
                .deserialize(socket.recv());
        return reply;
        
    }
    
    public ParameterMessage sendUpdate(ModelParameters content) {
        return sendMessage("UPDATE", content);
    }
    
    public ParameterMessage sendInit() {
        return sendMessage("INIT", null);
    }
    
    public ParameterMessage sendEnd() {
        return sendMessage("END", null);
    }
    
}
