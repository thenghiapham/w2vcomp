package parallel;

import parallel.workers.Launchable;

public interface Launcher {

    public abstract String launch(Launchable launchable) ;

    public abstract void kill(String proccess_id);

    public abstract void init();

    public abstract void exit();

}