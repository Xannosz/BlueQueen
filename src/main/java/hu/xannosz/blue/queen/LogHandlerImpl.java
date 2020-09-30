package hu.xannosz.blue.queen;

import hu.xannosz.veneos.core.LogHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

public class LogHandlerImpl implements LogHandler {

    public static final LogHandlerImpl INSTANCE = new LogHandlerImpl();

    @Getter
    private List<Log> logs = new ArrayList<>();

    @Override
    public void log(LogLevel logLevel, String reason, String message) {
        logs.add(new Log(logLevel, reason, message));
        System.out.println(new Log(logLevel, reason, message));
    }

    @Data
    @AllArgsConstructor
    @ToString
    public static class Log {
        private LogLevel logLevel;
        private String reason;
        private String message;
    }
}
