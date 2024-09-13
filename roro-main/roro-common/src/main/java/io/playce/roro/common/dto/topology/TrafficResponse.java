package io.playce.roro.common.dto.topology;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class TrafficResponse {

    private List<TrafficInbound> inbound;
    private List<TrafficOutbound> outbound;

    @Getter
    @Setter
    @ToString
    public static class TrafficInbound {

        private Integer port;
        private String protocol;
        private String type;
        private String source;
        private Long serverId;
        private String serverName;
        private String resourceType;
        private Long inventoryId;
        private Long resourceId;
        private String resourceName;

    }

    @Getter
    @Setter
    @ToString
    public static class TrafficOutbound {

        private Integer port;
        private String protocol;
        private String type;
        private String target;
        private Long serverId;
        private String serverName;
        private String resourceType;
        private Long inventoryId;
        private Long resourceId;
        private String resourceName;

    }

}
