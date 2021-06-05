package hu.xannosz.blue.queen;

import lombok.Data;

import java.util.List;

@Data
public class ContainerLog {
    private List<LogLine> logs;

    @Data
    public static class LogLine {
        private String log;
        private String stream;
        private String time;
    }
}
