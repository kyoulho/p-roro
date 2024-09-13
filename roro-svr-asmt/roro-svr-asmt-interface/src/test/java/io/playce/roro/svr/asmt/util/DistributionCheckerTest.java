package io.playce.roro.svr.asmt.util;

import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.svr.asmt.config.DistributionConfig;
import io.playce.roro.svr.asmt.dto.Distribution;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Dong-Heon Han    Apr 07, 2022		First Draft.
 */

@Slf4j
class DistributionCheckerTest {
    @Test
    void getDistribution() throws InterruptedException {
        print(getDistribution(getTargetHost("127.0.0.1", 2253, "root", "jan01jan")));
        print(getDistribution(getTargetHost("127.0.0.1", 2261, "roro", "jan01jan")));
        print(getDistribution(getTargetHost("127.0.0.1", 2260, "roro", "jan01jan")));
    }

    private Distribution getDistribution(TargetHost targetHost) throws InterruptedException {
        DistributionConfig config = getConfig();
        boolean sudo = SSHUtil.isSudoer(targetHost);
        return DistributionChecker.getDistribution(config, targetHost, sudo);
    }

    @NotNull
    private DistributionConfig getConfig() {
        DistributionConfig config = new DistributionConfig();
        config.setCommand("cat /etc/*-release 2>/dev/null | uniq");
        config.setNameMap(Map.of("REDHAT", List.of("REDHAT", "CENTOS"), "DEBIAN", List.of("DEBIAN")));
        return config;
    }

    private void print(Distribution distribution) {
        log.info("{}, {}, {}, {}",
                distribution.getOsFamily(), distribution.getDistribution(),
                distribution.getDistributionVersion(), distribution.getDistributionRelease());
    }

    private TargetHost getTargetHost(String ip, int port, String user, String pass) {
        TargetHost targetHost = new TargetHost();
        targetHost.setIpAddress(ip);
        targetHost.setPort(port);
        targetHost.setUsername(user);
        targetHost.setPassword(pass);
        return targetHost;
    }
}