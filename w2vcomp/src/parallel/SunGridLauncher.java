package parallel;

import java.util.Collections;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import demo.TestConstants;

public class SunGridLauncher {

    public static void main (String[] args) {
        SessionFactory factory = SessionFactory.getFactory ();
        Session session = factory.getSession ();

        try {
           session.init (null);
           JobTemplate jt = session.createJobTemplate ();
           jt.setRemoteCommand ("sleeper.sh");
           jt.setWorkingDirectory (TestConstants.S_PROJECT_DIR + "/jobs");
           jt.setArgs (Collections.singletonList(""));

           String id = session.runJob (jt);

           session.deleteJobTemplate (jt);
           session.exit ();
        }
        catch (DrmaaException e) {
           System.out.println ("Error: " + e.getMessage ());
        }
      }
}
