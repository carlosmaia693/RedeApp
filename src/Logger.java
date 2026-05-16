import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    static final String LOG_DIR = "C:\\ProgramData\\AgentRede\\logs\\";
    static final long MAX_SIZE = 1024 * 1024; // 1 MB
    static final int MAX_FILES = 5;

    static void rotateLogs(String fileName) {
        try {
            File log = new File(LOG_DIR + fileName);

            if (!log.exists()) return;
            if (log.length() < MAX_SIZE) return;

            File last = new File(LOG_DIR + fileName + "." + MAX_FILES);
            if (last.exists()) last.delete();

            for (int i = MAX_FILES - 1; i >= 1; i--) {
                File oldFile = new File(LOG_DIR + fileName + "." + i);

                if (oldFile.exists()) {
                    oldFile.renameTo(
                            new File(LOG_DIR + fileName + "." + (i + 1))
                    );
                }
            }

            log.renameTo(new File(LOG_DIR + fileName + ".1"));

        } catch (Exception ignored) {}
    }

    static void write(String fileName, String level, String msg) {
        try {
            new File(LOG_DIR).mkdirs();

            rotateLogs(fileName);

            String time = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern(
                            "yyyy-MM-dd HH:mm:ss"
                    ));

            String linha =
                    "[" + time + "] [" + level + "] " + msg + "\n";

            FileWriter fw = new FileWriter(LOG_DIR + fileName, true);
            fw.write(linha);
            fw.close();

        } catch (Exception ignored) {}
    }

    public static void info(String file, String msg) {
        write(file, "INFO", msg);
    }

    public static void warn(String file, String msg) {
        write(file, "WARN", msg);
    }

    public static void error(String file, String msg) {
        write(file, "ERROR", msg);
    }

    public static void error(String file, String msg, Exception e) {
        write(file, "ERROR", msg + " | " + e);
    }
}