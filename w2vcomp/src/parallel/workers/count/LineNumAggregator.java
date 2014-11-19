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
    public ModelParameters getAggregatedParameters() {
        // TODO Auto-generated method stub
        LineNumParameters finalParameters = new LineNumParameters();
        finalParameters.setValue(totalLineNum);
        return finalParameters;
    }

}
