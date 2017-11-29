package logging;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class BankLogger {

    private static final String DEAULT_LOG_NAME = "log";
    private static final String LOG_FOLDER = "logs";
    private static final String LOG_PATH = LOG_FOLDER + "/";

    private static String serverLogFilename;

    public static synchronized void logUserAction(String userId, String message) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(userId);

        try {
            String fileName = LOG_PATH + userId + ".txt";

            //Create logUserAction directory if doesn't exist
            File logDir = new File(LOG_FOLDER);
            if (!logDir.exists())
                logDir.mkdir();

            //Create logUserAction file if doesn't exist
            File file = new File(fileName);
            if (!file.exists())
                file.createNewFile();

            //Init a file handler to write logUserAction output to
            FileHandler fileHandler = new FileHandler(fileName, true);

            //Remove console handler and add file handler to logger
            logger.setUseParentHandlers(false);
            logger.addHandler(fileHandler);

            //Set a formatter for the logger
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fileHandler.setFormatter(simpleFormatter);

            //Print to console
            System.out.println(message);

            //Log to file
            logger.info(message);

            //Close the file handler
            fileHandler.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void logAction(String message) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DEAULT_LOG_NAME);

        try {
            if (serverLogFilename == null) {
                serverLogFilename = generateServerLogFilename();
                System.out.println("Log name for server session: " + serverLogFilename);
            }

            //Create logUserAction directory if doesn't exist
            File logDir = new File(LOG_FOLDER);
            if (!logDir.exists())
                logDir.mkdir();

            //Create logUserAction file if doesn't exist
            File file = new File(serverLogFilename);
            if (!file.exists())
                file.createNewFile();

            //Init a file handler to write logUserAction output to
            FileHandler fileHandler = new FileHandler(serverLogFilename, true);

            //Remove console handler and add file handler to logger
            logger.setUseParentHandlers(false);
            logger.addHandler(fileHandler);

            //Set a formatter for the logger
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fileHandler.setFormatter(simpleFormatter);

            //Print to console
            System.out.println(message);

            //Log to file
            logger.info(message);

            //Close the file handler
            fileHandler.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized String logAndReturn(String message) {
        logAction("\nReturning message to client:");
        logAction(message);
        return message;
    }

    private static String generateServerLogFilename() {
        return LOG_PATH + UUID.randomUUID() + ".txt";
    }
}
