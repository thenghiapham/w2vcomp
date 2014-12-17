package parallel.workers.count;

import java.io.Serializable;

import parallel.workers.ModelParameters;

public class LineNumParameters implements ModelParameters {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected long numLines;

    public Long getValue() {
        // TODO Auto-generated method stub
        return numLines;
    }

    public void setValue(Serializable value) {
        // TODO Auto-generated method stub
        numLines = (Long) value;
    }

}
