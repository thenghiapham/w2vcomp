package parallel.workers.w2v;

import java.io.Serializable;

import parallel.workers.ModelParameters;

public class SkipGramParameters implements ModelParameters{
    
    protected double[][] vectors;
    protected double[][] outVectors;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Serializable getValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setValue(Serializable value) {
        // TODO Auto-generated method stub
        
    }

}
