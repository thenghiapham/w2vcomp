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
    
    static public void logToConsole(Level level) throws IOException {

        // set the level
        Logger rootLogger = Logger.getLogger("");
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(level);
        rootLogger.addHandler(handler);
        rootLogger.log(Level.INFO, "Start logging");
    }

}
