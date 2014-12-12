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
    
    private ParameterMessage sendMessage(Integer source, String type, ModelParameters content) {
        Serializable msg = new ParameterMessage(source, type, content);
        socket.send(SerializationUtils.serialize(msg), 0);
        ParameterMessage reply = (ParameterMessage) SerializationUtils
                .deserialize(socket.recv());
        return reply;
        
    }
    
    public ParameterMessage sendUpdate(Integer source, ModelParameters content) {
        return sendMessage(source, "UPDATE", content);
    }
    
    public ParameterMessage sendInit(Integer source) {
        return sendMessage(source, "INIT", null);
    }
    
    public ParameterMessage sendEnd(Integer source) {
        return sendMessage(source, "END", null);
    }
    
}
