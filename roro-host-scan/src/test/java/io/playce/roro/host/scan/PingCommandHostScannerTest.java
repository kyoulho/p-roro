package io.playce.roro.host.scan;

import io.playce.roro.host.scan.config.HostScanProperties;
import io.playce.roro.host.scan.dto.ScanResult;
import io.playce.roro.host.scan.pingcommand.PingCommandHostScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PingCommandHostScannerTest {
    @Mock
    private HostScanProperties hostScanProperties;

    private Properties properties;


    @BeforeEach
    void setUp() throws IOException {
        InputStream is = getClass().getResourceAsStream("/application-hostscan.yml");
        properties = new Properties();
        properties.load(is);
        if (is != null) {
            is.close();
        }
    }

    @Test
    @DisplayName("스캔 테스트")
    void scan() throws ExecutionException, InterruptedException {
        // given
        String ipAddress = "127.0.0.1";
        int timeOut = Integer.parseInt(properties.getProperty("time-out").trim());
        String pingOption = properties.getProperty("ping-option").trim();
        List<Integer> windowsTTL = Arrays.stream(properties.getProperty("windows-ttl").split(","))
                .map(str -> Integer.parseInt(str.trim()))
                .collect(Collectors.toList());
        List<Integer> linuxTTL = Arrays.stream(properties.getProperty("linux-ttl").split(","))
                .map(str -> Integer.parseInt(str.trim()))
                .collect(Collectors.toList());
        List<Integer> unixTTL = Arrays.stream(properties.getProperty("unix-ttl").split(","))
                .map(str -> Integer.parseInt(str.trim()))
                .collect(Collectors.toList());

        given(hostScanProperties.getTimeOut())
                .willReturn(timeOut);
        given(hostScanProperties.getPingOption())
                .willReturn(pingOption);
        given(hostScanProperties.getLinuxTTLs())
                .willReturn(new HashSet<>(linuxTTL));
        given(hostScanProperties.getWindowsTTLs())
                .willReturn(new HashSet<>(windowsTTL));
        given(hostScanProperties.getUnixTTLs())
                .willReturn(new HashSet<>(unixTTL));
        // when
        HostScanner hostScanner = new PingCommandHostScanner(hostScanProperties);
        CompletableFuture<ScanResult> future = hostScanner.scanIpAddress(ipAddress);
        ScanResult scanResult = future.get();
        // then
        assertThat(scanResult.getIpAddress()).isEqualTo(ipAddress);
        assertThat(scanResult.getReplyTTL()).isNotNull();
        assertThat(scanResult.getOsName()).isNotNull();
    }
}