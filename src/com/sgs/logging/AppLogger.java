package com.sgs.logging;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Application Logger - Enterprise-grade centralized logging.
 * Implements structured logging with file and console handlers.
 * Thread-safe singleton pattern following IBM standards.
 */
public class AppLogger {

    private static final AppLogger INSTANCE = new AppLogger();
    private static final Logger LOGGER = Logger.getLogger("StudentGradingSystem");
    private static final String LOG_FILE = "logs/sgs-application.log";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    static {
        LOGGER.setLevel(Level.ALL);
        try {
            // File handler for persistent logging
            FileHandler fh = new FileHandler(LOG_FILE, 5 * 1024 * 1024, 3, true); // 5MB, 3 backups
            fh.setLevel(Level.ALL);
            fh.setFormatter(new EnhancedFormatter());
            LOGGER.addHandler(fh);
        } catch (SecurityException | IOException e) {
            LOGGER.warning("Failed to initialize file logging: " + e.getMessage());
        }
    }

    private AppLogger() {}

    public static AppLogger getInstance() {
        return INSTANCE;
    }

    public void info(String category, String message, Object... params) {
        log(Level.INFO, category, message, null, params);
    }

    public void warn(String category, String message, Object... params) {
        log(Level.WARNING, category, message, null, params);
    }

    public void error(String category, String message, Throwable throwable, Object... params) {
        log(Level.SEVERE, category, message, throwable, params);
    }

    public void debug(String category, String message, Object... params) {
        log(Level.FINE, category, message, null, params);
    }

    private void log(Level level, String category, String message, Throwable throwable, Object... params) {
        StringBuilder msg = new StringBuilder()
                .append("[").append(category).append("] ")
                .append(String.format(message, params));

        if (throwable != null) {
            msg.append("\n").append(getStackTrace(throwable));
            LOGGER.log(level, msg.toString(), throwable);
        } else {
            LOGGER.log(level, msg.toString());
        }
    }

    private String getStackTrace(Throwable throwable) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (IOException e) {
            return "Error retrieving stack trace";
        }
    }

    /**
     * Custom formatter for structured logging output.
     */
    private static class EnhancedFormatter extends SimpleFormatter {
        @Override
        public synchronized String format(java.util.logging.LogRecord record) {
            return String.format(
                    "%s [%s] %s - %s%n",
                    SDF.format(new Date(record.getMillis())),
                    record.getLevel(),
                    record.getLoggerName(),
                    record.getMessage()
            );
        }
    }
}
