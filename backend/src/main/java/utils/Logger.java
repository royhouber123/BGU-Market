package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "application.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static Logger instance;

    // private constructor to prevent instantiation
    private Logger() {}

    // thread-safe singleton instance retrieval
    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    private void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[%s] [%s] %s", timestamp, level.toUpperCase(), message);

        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(logMessage);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    public void debug(String message) {
        log("DEBUG", message);
    }

    public void info(String message) {
        log("INFO", message);
    }

    public void error(String message) {
        log("ERROR", message);
    }
    
    public void warn(String message) {
        log("WARN", message);
    }
}