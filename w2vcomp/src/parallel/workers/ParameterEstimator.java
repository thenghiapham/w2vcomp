package parallel.workers;

import parallel.comm.ParameterMessager;

public interface ParameterEstimator {
    public void run(Integer worker_id, ModelParameters init, ParameterMessager parameterMessager);
}
