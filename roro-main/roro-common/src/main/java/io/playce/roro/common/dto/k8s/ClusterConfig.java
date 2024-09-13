package io.playce.roro.common.dto.k8s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class ClusterConfig {
    private String name;
    private String apiVersion;
    private List<ClusterEntry> clusters;

    public static class ClusterEntry {
        private ClusterInfo cluster;
        private String name;
    }

    public static class ClusterInfo {
        private String certificate_authority_data;
        private String server;

    }
}

