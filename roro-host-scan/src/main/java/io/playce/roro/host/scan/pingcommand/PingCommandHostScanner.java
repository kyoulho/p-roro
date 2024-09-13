package io.playce.roro.host.scan.pingcommand;/*
 * Copyright 2022.06.14 Ip Range Scan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision History
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * JinHyun Kyun       06 16, 2022       First Draft.
 */

import io.playce.roro.host.scan.HostScanner;
import io.playce.roro.host.scan.config.HostScanProperties;
import io.playce.roro.host.scan.dto.ScanResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * <pre>
 *  Ip Address 를 받아 해당 ip 에 운영체제를 추론한다.
 *
 * </pre>
 *
 * @author Jinhyun Kyun
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PingCommandHostScanner implements HostScanner {
    private final HostScanProperties properties;

    @Override
    @Async("hostScanTaskExecutor")
    public CompletableFuture<ScanResult> scanIpAddress(String ipAddress) throws TaskRejectedException {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            log.error("Exception occurred by wrong Ip Address [{}]", ipAddress);
            return CompletableFuture.completedFuture(null);
        }

        // icmp echo request
        boolean reachable = false;
        try {
            reachable = inetAddress.isReachable(properties.getTimeOut());
        } catch (IOException e) {
            log.debug("Exception occurred during request icmp packet from {} [{}]", ipAddress, e.getMessage());
        }

        if (!reachable) {
            log.trace("{} is not alive", ipAddress);
            return CompletableFuture.completedFuture(null);
        }

        Date discoveredDateTime = new Date();
        Integer ttl = getTTL(ipAddress);
        log.trace("The reply TTL of {} is {}", ipAddress, ttl);

        return CompletableFuture.completedFuture(ScanResult.builder()
                .ipAddress(ipAddress)
                .replyTTL(ttl)
                .discoveredDatetime(discoveredDateTime)
                .osName(getOsName(ttl))
                .build());
    }

    private Integer getTTL(String ipAddress) {
        // 결과 출력 스트림 , 에러 출력 스트림
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ByteArrayOutputStream errStream = new ByteArrayOutputStream()) {
            // 명령어
            CommandLine commandLine = new CommandLine("ping")
                    .addArgument(properties.getPingOption(), false) // 따옴표 처리 여부
                    .addArgument(ipAddress, false);

            Executor executor = new DefaultExecutor();
            executor.setStreamHandler(new PumpStreamHandler(outputStream, errStream));

            // command 실행
            int exitedCode = 1;
            try {
                log.debug("Execute command {}", commandLine.toString());
                exitedCode = executor.execute(commandLine);
            } catch (IOException e) {
                log.debug("Exception occurred during run command [{}]", e.getMessage());
            }

            // command 실패 시
            if (exitedCode != 0) {
                log.debug("Failed to execute command [{}]", errStream.toString(StandardCharsets.UTF_8));
                return null;
            }

            String result = StreamUtils.copyToString(outputStream, StandardCharsets.UTF_8);
            // command 실행 결과에 ttl이 없을 시
            if (!result.contains("ttl=")) {
                log.debug("There is no TTL in the Icmp reply packet from {}", ipAddress);
                return null;
            }

            result = result.substring(result.indexOf("ttl="));
            result = result.substring(0, result.indexOf(" "));
            result = result.replace("ttl=", "");

            return Integer.valueOf(result);

        } catch (IOException e) {
            log.error("Exception in ByteArrayStream while executing command [{}]", e.getMessage());
            return null;
        } //end of try-resource
    }

    private String getOsName(Integer ttl) {
        Set<Integer> linuxTTLs = properties.getLinuxTTLs();
        Set<Integer> windowsTTLs = properties.getWindowsTTLs();
        Set<Integer> unixTTLs = properties.getUnixTTLs();

        if (linuxTTLs.contains(ttl)) {
            return "linux";

        } else if (windowsTTLs.contains(ttl)) {
            return "windows";

        } else if (unixTTLs.contains(ttl)) {
            return "unix";

        } else {
            return "unknown";
        }
    }


}

