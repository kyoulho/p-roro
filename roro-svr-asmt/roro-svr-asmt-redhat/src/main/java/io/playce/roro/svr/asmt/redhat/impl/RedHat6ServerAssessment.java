/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Hoon Oh          11월 10, 2021		First Draft.
 */
package io.playce.roro.svr.asmt.redhat.impl;

import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.svr.asmt.AssessmentItems;
import io.playce.roro.svr.asmt.config.DistributionConfig;
import io.playce.roro.svr.asmt.dto.result.RedHatAssessmentResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Slf4j
@Component("REDHAT6Assessment")
public class RedHat6ServerAssessment extends RedHatServerAssessment {

    private final String DAEMON_LIST = "chkconfig --list";

    public RedHat6ServerAssessment(DistributionConfig config) {
        super(config);
    }

    @Override
    public Map<String, String> generateCommand() {
        Map<String, String> cmdMap = super.generateCommand();
        cmdMap.put(AssessmentItems.DAEMON_LIST.toString(), DAEMON_LIST);
        return cmdMap;
    }

    @Override
    protected Map<String, Map<String, String>> getDaemons(TargetHost targetHost, String daemonList, Map<String, String> errorMap) throws InterruptedException {
        Map<String, Map<String, String>> daemons = new HashMap<>();

        try {
            if (StringUtils.isNotEmpty(daemonList)) {
                for (String daemon : daemonList.split("\n")) {
                    if (StringUtils.isNotEmpty(daemon)) {
                        String[] value = daemon.split("\\s+");

                        if (value.length > 7) {
                            Map<String, String> infoMap = new HashMap<>();
                            for (int i = 1; i < value.length; i++) {
                                String[] detail = value[i].split(":");
                                infoMap.put(detail[0], detail[1]);
                            }
                            daemons.put(value[0], infoMap);
                        } else {
                            log.debug("GetDaemons:daemon [{}] is ignore because data format", Arrays.toString(value));
                        }
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error(e.getMessage());
        }
        return daemons;
    }

    @Override
    public RedHatAssessmentResult assessment(TargetHost targetHost) throws InterruptedException {
        return super.assessment(targetHost);
    }
}
//end of RedHatServerAssessment6.java