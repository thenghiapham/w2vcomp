package parallel.workers.count;

import parallel.workers.ModelParameters;
import parallel.workers.ParameterAggregator;

public class LineNumAggregator implements ParameterAggregator {
    long totalLineNum = 0;
    @Override
    public ModelParameters aggregate(ModelParameters content) {
        // TODO Auto-generated method stub
        long additionalLineNum = ((LineNumParameters) content).getValue();
        totalLineNum += additionalLineNum;
        content.setValue(totalLineNum);
        return content;
    }

    @Override
    public ModelParameters getInitParameters() {
        // TODO Auto-generated method stub
        LineNumParameters finalParameters = new LineNumParameters();
        finalParameters.setValue(totalLineNum);
        return finalParameters;
    }


    @Override
    public void finalize() {
        // TODO Auto-generated method stub
        return getInitParameters();
    }

}
