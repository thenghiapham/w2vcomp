package parallel.workers.w2v;

import parallel.workers.ModelParameters;
import parallel.workers.ParameterAggregator;

public class W2vAggregator implements ParameterAggregator {
    long totalLineNum = 0;
    @Override
    public ModelParameters aggregate(ModelParameters content) {
        // TODO Auto-generated method stub
        long additionalLineNum = ((W2vParameters) content).getValue();
        totalLineNum += additionalLineNum;
        content.setValue(totalLineNum);
        return content;
    }

    @Override
    public ModelParameters getAggregatedParameters() {
        // TODO Auto-generated method stub
        W2vParameters finalParameters = new W2vParameters();
        finalParameters.setValue(totalLineNum);
        return finalParameters;
    }

}
