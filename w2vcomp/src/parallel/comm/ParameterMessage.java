package parallel.comm;

import java.io.Serializable;

import parallel.workers.ModelParameters;

public class ParameterMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String type;
    private ModelParameters content;
    
    
    public ParameterMessage(String type, ModelParameters content) {
        super();
        this.setType(type);
        this.setContent(content);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ModelParameters getContent() {
        return content;
    }

    public void setContent(ModelParameters content) {
        this.content = content;
    }
    
    

}
