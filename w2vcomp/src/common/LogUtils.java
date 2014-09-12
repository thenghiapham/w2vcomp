package common;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class LogUtils {
    static private FileHandler fileTxt;
    static private SimpleFormatter formatterTxt;
    
    static public void setup(String logFileName) throws IOException {

        // get the global logger to configure it
//        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        // suppress the logging output to the console
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
          rootLogger.removeHandler(handlers[0]);
        }

        rootLogger.setLevel(Level.INFO);
        fileTxt = new FileHandler(logFileName);

        // create a TXT formatter
        formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        rootLogger.addHandler(fileTxt);

      }

}
