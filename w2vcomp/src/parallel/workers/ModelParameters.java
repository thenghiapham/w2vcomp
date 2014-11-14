package parallel.workers;

import java.io.Serializable;

public interface ModelParameters extends Serializable {
    public Serializable getValue();

    public void setValue(Serializable value);
}
