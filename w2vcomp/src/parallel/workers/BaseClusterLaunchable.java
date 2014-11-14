package parallel.workers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;

public abstract class BaseClusterLaunchable {

    protected File home_path;
    
    public BaseClusterLaunchable(File home_path) {
        this.home_path = home_path;
    }

    public abstract JobTemplate getJobTemplate(Session session) throws DrmaaException;
    
    public JobTemplate getJobTemplate(Session session, String[] args) throws DrmaaException {
        return createJobTemplate(
                home_path, session,
                this.getClass().getName(),
                args);
    }

    protected JobTemplate createJobTemplate(File home_path,
            Session session, String mainClass, String[] args)
            throws DrmaaException {
        JobTemplate jt = session.createJobTemplate();
        jt.setNativeSpecification("-b n -j y -l h_vmem=4G -l h_cpu=48:0:0");
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

}
