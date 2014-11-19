package parallel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import parallel.workers.Launchable;
import edu.stanford.nlp.util.ArrayMap;

public class MultiprocessLauncher implements Launcher {

    private File                 home_path;
    private Integer              pid_counter;
    private Map<String, Process> launched_processes;

    public MultiprocessLauncher(File home_path) {
        this.home_path = home_path;
        launched_processes = new ArrayMap<>();
        pid_counter = 1;
    }

    @Override
    public String launch(Launchable launchable) {
        try {
            return launch(launchable.getClass().getName(), launchable.getArgs());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String launch(String mainClass, String[] args) throws IOException {
        File pathToExecutable = new File(home_path,
                "w2vcomp/w2vcomp/src/parallel/launcher.sh");
        File workDir = new File(home_path, "jobs");
        workDir.mkdirs();

        List<String> cmd = new ArrayList<>();
        cmd.add(pathToExecutable.getAbsolutePath());
        cmd.add(mainClass);
        cmd.add(home_path.getPath());
        cmd.addAll(Arrays.asList(args));
        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.directory(workDir.getAbsoluteFile()); // this is where you set
                                                      // the root folder for the
                                                      // executable to run with
        builder.redirectErrorStream(true);

        final Integer pid = pid_counter++;
        final File output_path = new File(workDir, "output." + pid + ".log");
        System.out.println("Launching process " + pid );
        final Process process = builder.start();
        launched_processes.put(pid.toString(), process);

        Thread output_listener = new Thread(new Runnable() {
            @Override
            public void run() {
                copy(process.getInputStream(), output_path);

                int result;
                try {
                    result = process.waitFor();
                    System.out.printf(
                            "Process %d exited with result %d and output %n",
                            pid, result);
                } catch (InterruptedException e) {
                    System.out.printf("Process died");
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            private void copy(InputStream in, File file) {
                try {
                    OutputStream out = new FileOutputStream(file);
                    byte[] buf = new byte[1024 * 100];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        output_listener.start();

        return pid.toString();
    }

    @Override
    public void kill(String proccess_id) {
        launched_processes.get(proccess_id).destroy();
    }

    @Override
    public void init() {
        // Do nothing
    }

    public void exit() {
        // Do nothing
    }

}
