package io.playce.roro.host.scan.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@ConfigurationProperties(prefix = "host-scan")
@Getter
@Setter
public class HostScanProperties {
    private int timeOut;
    private String pingOption;
    private Set<Integer> windowsTTLs;
    private Set<Integer> linuxTTLs;
    private Set<Integer> unixTTLs;

}

