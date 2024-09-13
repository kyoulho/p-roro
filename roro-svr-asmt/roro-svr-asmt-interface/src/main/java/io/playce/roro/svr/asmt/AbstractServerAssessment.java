/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Hoon Oh       1월 07, 2022            First Draft.
 */
package io.playce.roro.svr.asmt;

import com.jcraft.jsch.JSchException;
import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.SSHUtil2;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.svr.asmt.dto.common.Package;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public abstract class AbstractServerAssessment implements ServerAssessment {

    protected static final List<String> CHECK_NETSTAT_STATUS = List.of("ESTABLISHED", "FIN_WAIT1", "FIN_WAIT2", "CLOSED", "WAIT");

    public abstract Map<String, String> generateCommand();

    public Map<String, RemoteExecResult> runCommands(TargetHost targetHost, Map<String, String> cmdMap) throws InterruptedException {
        Map<String, RemoteExecResult> resultMap = new HashMap<>();
        try {
            boolean sudo = SSHUtil.isSudoer(targetHost);
            resultMap = SSHUtil2.runCommands(targetHost, cmdMap, sudo);

        } catch (JSchException | IOException e) {
            RoRoException.checkInterruptedException(e);
            log.error("{}", e.getMessage(), e);
        }

        return resultMap;
    }

    public String getResult(AssessmentItems item, Map<String, RemoteExecResult> results) {
        if (results.containsKey(item.toString())) {
            return results.get(item.toString()).getResult();
        } else {
            return null;
        }
    }

    public String getResult2(AssessmentItems item, AssessmentItems item2, Map<String, RemoteExecResult> results) {
        RemoteExecResult result1 = results.get(item.name());
        if (result1 == null || result1.isErr()) {
            return results.get(item2.name()).getResult();
        } else {
            return result1.getResult();
        }
    }

    protected Long getUptime(String uptimeStr) {
        if (!uptimeStr.contains("up")) {
            throw new RoRoException("Please check \"uptime\" command result is valid. Result : [" + uptimeStr + "]");
        }

        // "  4:59pm  up 6 hr(s),  3 users,  load average: 0.33, 0.30, 0.20";
        // "  5:02pm  up  6:04,  2 users,  load average: 0.12, 0.23, 0.20";
        // "17:02:33 up 33 days,  8:13,  3 users,  load average: 0.01, 1.02, 2.05";
        // "17:02:33 up 58 min,  3 users,  load average: 0.01, 1.02, 2.05";
        String[] uptimes = uptimeStr.split(",");

        String dayStr = null, hourStr = null, minStr = null;
        long day = 0, hour = 0, min = 0;
        if (uptimes[0].contains("up")) {
            String tmp = uptimes[0].substring(uptimes[0].indexOf("up") + 2).trim();

            if (tmp.contains("d")) {
                dayStr = tmp.split("\\s")[0];
            } else if (tmp.contains("h")) {
                hourStr = tmp.split("\\s")[0];
            } else if (tmp.contains("m")) {
                minStr = tmp.split("\\s")[0];
            } else if (tmp.contains(":")) {
                hourStr = tmp.split(":")[0];
                minStr = tmp.split(":")[1];
            }
        }

        if (!uptimes[1].contains("user")) {
            String tmp = uptimes[1].trim();

            if (tmp.contains("h")) {
                hourStr = tmp.split("\\s")[0];
            } else if (tmp.contains("m")) {
                minStr = tmp.split("\\s")[0];
            } else if (tmp.contains(":")) {
                hourStr = tmp.split(":")[0];
                minStr = tmp.split(":")[1];
            }
        }

        if (NumberUtils.isDigits(dayStr)) {
            day = Long.parseLong(dayStr);
        }

        if (NumberUtils.isDigits(hourStr)) {
            hour = Long.parseLong(hourStr);
        }

        if (NumberUtils.isDigits(minStr)) {
            min = Long.parseLong(minStr);
        }

        long current = System.currentTimeMillis();
        long up = (((day) * 24 + (hour)) * 60 + (min)) * 60000;

        return current - up;
    }

    protected List<Package> getPackage(String packages) {
        List<Package> result = new ArrayList<>();
        if (packages == null) return result;
        String[] nameAndVersions = packages.split(StringUtils.LF);
        for (String nameAndVersion : nameAndVersions) {
            if (nameAndVersion.isBlank()) {
                continue;
            }
            String[] array = nameAndVersion.split(StringUtils.SPACE);
            Package aPackage = new Package();
            aPackage.setName(array[0]);
            aPackage.setVersion(array[1]);
            result.add(aPackage);
        }
        return result;
    }
}
//end of AbstractServerAssessment.java