package io.playce.roro.common.dto.statistics;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChartServerCountPerServiceResponse {

    private Long serviceId;
    private String serviceName;
    private ServerCount serverCount;

    @Getter
    @Setter
    @ToString
    public static class ServerCount {
        private int windows;
        private int linux;
        private int unix;
        private int other;
    }
}
