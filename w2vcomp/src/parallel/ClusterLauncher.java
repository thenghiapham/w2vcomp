package parallel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import parallel.workers.Launchable;

public class ClusterLauncher implements Launcher {

    private Session session;
    private File    home_path;

    public ClusterLauncher(File home_path) {
        this.home_path = home_path;
        SessionFactory factory = SessionFactory.getFactory();
        session = factory.getSession();
    }

    public void init() {
        try {
            session.init(null);
        } catch (DrmaaException e) {
            throw new RuntimeException(e);
        }
    }

    public JobTemplate getJobTemplate(Session session, String mainClass,
            String[] args) throws DrmaaException {
        return createJobTemplate(home_path, session, mainClass, args);
    }

    protected JobTemplate createJobTemplate(File home_path, Session session,
            String mainClass, String[] args) throws DrmaaException {
        JobTemplate jt = session.createJobTemplate();
        jt.setNativeSpecification("-b n -j y -l h_vmem=8G -l h_cpu=48:0:0");
        jt.setRemoteCommand(new File(home_path,
                "w2vcomp/w2vcomp/src/parallel/launcher.sh").getPath());
        File working_directory = new File(home_path, "jobs");
        working_directory.mkdirs();
        jt.setWorkingDirectory(working_directory.getPath());

        List<String> main_args = new ArrayList<>();
        main_args.add(mainClass);
        main_args.add(home_path.getPath());
        main_args.addAll(Arrays.asList(args));
        jt.setArgs(main_args);
        return jt;
    }

    public String launch(Launchable launchable) {
        try {
            JobTemplate jt = getJobTemplate(session, launchable.getClass()
                    .getName(), launchable.getArgs());
            String id = session.runJob(jt);
            session.deleteJobTemplate(jt);
            return id;
        } catch (DrmaaException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void exit() {
        try {
            session.exit();
        } catch (DrmaaException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void kill(String proccess_id) {
        try {
            session.control(proccess_id, Session.TERMINATE);
        } catch (DrmaaException ex) {
            throw new RuntimeException(ex);
        }

    }

}
