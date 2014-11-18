package parallel.workers.w2v;

import java.io.Serializable;

import parallel.workers.ModelParameters;

public class W2vParameters implements ModelParameters {

    protected int numLines;
    @Override
    public Integer getValue() {
        // TODO Auto-generated method stub
        return numLines;
    }

    @Override
    public void setValue(Serializable value) {
        // TODO Auto-generated method stub
        numLines = (Integer) value;
    }

}
