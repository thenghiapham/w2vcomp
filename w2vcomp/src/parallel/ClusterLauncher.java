package parallel;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import parallel.workers.BaseClusterLaunchable;

public class ClusterLauncher {

    private Session session;
    
    public ClusterLauncher() {
        SessionFactory factory = SessionFactory.getFactory();
        session = factory.getSession();
    }
    
    public void init() throws DrmaaException {
        session.init(null);
    }
    
    public String launch(BaseClusterLaunchable launchable) throws DrmaaException {
        JobTemplate jt = launchable.getJobTemplate(session);
        String id = session.runJob(jt);
        session.deleteJobTemplate(jt);
        return id;
    }

    public void exit() throws DrmaaException {
        session.exit();
    }

    public void kill(String proccess_id) throws DrmaaException {
        session.control(proccess_id, Session.TERMINATE);
        
    }

}
