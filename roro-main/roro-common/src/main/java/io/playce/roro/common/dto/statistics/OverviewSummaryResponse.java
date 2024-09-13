package io.playce.roro.common.dto.statistics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.playce.roro.common.code.Domain1003;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class OverviewSummaryResponse {

    private Service service;
    private Server server;
    private Middleware middleware;
    private Database database;
    private Application application;

    @Getter
    public static class Service {
        private final long count;

        public Service(long serviceCount) {
            this.count = serviceCount;
        }
    }

    @Getter
    public static class Server {
        private final long count;
        private final long scanCompleteCount;
        // 추후 상태 분리를 고려하여 필드는 유지한다.
        @JsonIgnore
        private final long scanPartialCompleteCount;
        private final long scanFailCount;

        public Server(List<Map<String, Object>> summaryMap) {
            this.count = (long) getSummaryValue(summaryMap, "INVENTORY");
            this.scanPartialCompleteCount = (long) getSummaryValue(summaryMap, Domain1003.PC.name());
            this.scanCompleteCount = this.scanPartialCompleteCount + (long) getSummaryValue(summaryMap, Domain1003.CMPL.name());
            this.scanFailCount = (long) getSummaryValue(summaryMap, Domain1003.FAIL.name());
        }
    }

    @Getter
    public static class Middleware {
        private final long count;
        private final long scanCompleteCount;
        // 추후 상태 분리를 고려하여 필드는 유지한다.
        @JsonIgnore
        private final long scanPartialCompleteCount;
        private final long scanFailCount;

        public Middleware(List<Map<String, Object>> summaryMap) {
            this.count = (long) getSummaryValue(summaryMap, "INVENTORY");
            this.scanPartialCompleteCount = (long) getSummaryValue(summaryMap, Domain1003.PC.name());
            this.scanCompleteCount = this.scanPartialCompleteCount + (long) getSummaryValue(summaryMap, Domain1003.CMPL.name());
            this.scanFailCount = (long) getSummaryValue(summaryMap, Domain1003.FAIL.name());
        }
    }

    @Getter
    public static class Database {
        private final long count;
        private final long scanCompleteCount;
        // 추후 상태 분리를 고려하여 필드는 유지한다.
        @JsonIgnore
        private final long scanPartialCompleteCount;
        private final long scanFailCount;

        public Database(List<Map<String, Object>> summaryMap) {
            this.count = (long) getSummaryValue(summaryMap, "INVENTORY");
            this.scanPartialCompleteCount = (long) getSummaryValue(summaryMap, Domain1003.PC.name());
            this.scanCompleteCount = this.scanPartialCompleteCount + (long) getSummaryValue(summaryMap, Domain1003.CMPL.name());
            this.scanFailCount = (long) getSummaryValue(summaryMap, Domain1003.FAIL.name());
        }
    }

    @Getter
    public static class Application {
        private final long count;
        private final long scanCompleteCount;
        // 추후 상태 분리를 고려하여 필드는 유지한다.
        @JsonIgnore
        private final long scanPartialCompleteCount;
        private final long scanFailCount;

        public Application(List<Map<String, Object>> summaryMap) {
            this.count = (long) getSummaryValue(summaryMap, "INVENTORY");
            this.scanPartialCompleteCount = (long) getSummaryValue(summaryMap, Domain1003.PC.name());
            this.scanCompleteCount = this.scanPartialCompleteCount + (long) getSummaryValue(summaryMap, Domain1003.CMPL.name());
            this.scanFailCount = (long) getSummaryValue(summaryMap, Domain1003.FAIL.name());
        }
    }

    private static Object getSummaryValue(List<Map<String, Object>> summaries, String key) {
        for (Map<String, Object> summary : summaries) {
            if (summary.get("countType").equals(key)) {
                return summary.get("count");
            }
        }

        return 0L;
    }

}