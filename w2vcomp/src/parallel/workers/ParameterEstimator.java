package parallel.workers;

import parallel.comm.ParameterMessager;

public interface ParameterEstimator {
    public void run(ModelParameters init, ParameterMessager parameterMessager);
}
