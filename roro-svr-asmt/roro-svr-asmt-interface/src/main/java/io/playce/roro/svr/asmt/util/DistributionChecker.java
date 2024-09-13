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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Hoon Oh       11월 23, 2021            First Draft.
 */
package io.playce.roro.svr.asmt.util;

import com.jcraft.jsch.JSchException;
import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.SSHUtil2;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.svr.asmt.config.DistributionConfig;
import io.playce.roro.svr.asmt.dto.Distribution;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class DistributionChecker {

//    private static final Map<String, String> searchStrMap;

    /*static {
        searchStrMap = new HashMap<>();
        searchStrMap.put("OracleLinux", "Oracle Linux");
        searchStrMap.put("RedHat", "Red Hat");
        searchStrMap.put("Altlinux", "ALT Linux");
        searchStrMap.put("ClearLinux", "Clear Linux Software for Intel Architecture");
        searchStrMap.put("SMGL", "Source Mage GNU/Linux");
    }*/

    public static Distribution getDistribution(DistributionConfig config, TargetHost targetHost, boolean sudo) throws InterruptedException {
        Distribution distribution = new Distribution();

        try {
            RemoteExecResult result = SSHUtil2.runCommands(targetHost, List.of(config.getCommand()), sudo).get(0);
            if (result.isErr()) {
                if (StringUtils.contains(result.getError(), "/etc/*-release")) {
                    String release = SSHUtil.executeCommand(targetHost, "lsb_release -a");

                    if (StringUtils.isEmpty(release)) {
                        throw new RoRoException(result.getError());
                    }

                    String[] lines = release.split("\n");

                    for (String line : lines) {
                        if (line.startsWith("Distributor ID:")) {
                            distribution.setOsFamily(line.replaceAll("Distributor ID:", StringUtils.EMPTY).trim().toLowerCase());
                            distribution.setDistribution(distribution.getOsFamily());
                        } else if (line.startsWith("Release:")) {
                            distribution.setDistributionVersion(line.replaceAll("Release:", StringUtils.EMPTY).trim());
                        } else if (line.startsWith("Description:")) {
                            distribution.setDistributionRelease(line.replaceAll("Description:", StringUtils.EMPTY).trim());
                        }
                    }
                } else {
                    throw new RoRoException(result.getError());
                }
            } else {
                String message = result.getResult();
                String[] lines = message.split("\n");
                distribution.setOsFamily(getValue(lines, "ID_LIKE="));
                if (StringUtils.isEmpty(distribution.getOsFamily())) {
                    distribution.setOsFamily(getValue(lines, "ID="));
                }

                distribution.setDistribution(getValue(lines, "NAME="));
                distribution.setDistributionVersion(getValue(lines, "VERSION="));
                if (StringUtils.isEmpty(distribution.getDistributionVersion())) {
                    distribution.setDistributionVersion(getValue(lines, "VERSION_ID="));
                }

                distribution.setDistributionRelease(getValue(lines, distribution.getDistribution(), false));
                if (StringUtils.isEmpty(distribution.getDistributionRelease())) {
                    distribution.setDistributionRelease(getValue(lines, "PRETTY_NAME="));
                }
                if (StringUtils.isEmpty(distribution.getDistributionRelease())) {
                    distribution.setDistributionRelease(distribution.getDistribution() + " " + distribution.getDistributionVersion());
                }

                if (StringUtils.contains(distribution.getOsFamily(), StringUtils.SPACE)) {
                    String dist = distribution.getOsFamily();
                    int index = dist.indexOf(StringUtils.SPACE);
                    distribution.setOsFamily(dist.substring(0, index));
                }
                if (StringUtils.isEmpty(distribution.getDistribution())) {
                    List<String> removeBlankLine = Arrays.stream(lines).map(l -> l.replaceAll(StringUtils.SPACE, StringUtils.EMPTY).toUpperCase()).collect(Collectors.toList());
                    String dist = findByConfig(config, removeBlankLine);
                    if (StringUtils.isEmpty(dist)) {
                        String r = SSHUtil.executeCommand(targetHost, "sudo cat /proc/version");
                        if (StringUtils.isNotEmpty(r)) {
                            if (r.toLowerCase().contains("red hat") || r.toLowerCase().contains("redhat")) {
                                distribution.setOsFamily("rhel");
                            } else if (r.toLowerCase().contains("ubuntu") || r.toLowerCase().contains("debian")) {
                                distribution.setOsFamily("debian");
                            }
                        } else {
                            throw new RoRoException("No supported linux - ip: " + targetHost.getIpAddress());
                        }
                    } else {
                        distribution.setOsFamily(dist);
                        if (lines.length >= 1) {
                            int index = getDistributionRelease(removeBlankLine, config.getNameMap().get(dist));
                            if (index != -1) {
                                distribution.setDistributionRelease(lines[index]);
                            }
                        }
                    }
                }
            }

        } catch (JSchException | IOException e) {
            RoRoException.checkInterruptedException(e);
            log.error("{}", e.getMessage(), e);
        }

        return distribution;
    }

    private static int getDistributionRelease(List<String> lines, List<String> names) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (names.stream().anyMatch(line::contains)) {
                return i;
            }
        }
        return -1;
    }

    private static String findByConfig(DistributionConfig config, List<String> lines) {
        Map<String, List<String>> nameMap = config.getNameMap();
        for (String dist : nameMap.keySet()) {
            List<String> matchString = nameMap.get(dist);
            for (String match : matchString) {
                if (lines.stream().anyMatch(l -> l.contains(match))) {
                    return dist;
                }

            }
        }
        return null;
    }

    private static String getValue(String[] lines, String startStr) {
        return getValue(lines, startStr, true);
    }

    private static String getValue(String[] lines, String startStr, boolean flag) {
        if (startStr == null)
            return null;

        for (String line : lines) {
            String value = getString(startStr, flag, line);
            if (value != null)
                return value;
        }
        return null;
    }

    @Nullable
    private static String getString(String startStr, boolean flag, String line) {
        if (startStr == null)
            return null;

        if (line.startsWith(startStr)) {
            String value = flag ? line.substring(startStr.length()) : line;
            if (value.length() > 2 && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            return value;

        }
        return null;
    }

    /*static class DistributionValidator {

        public Distribution getDistributionHPUX(OSRelease osRelease, String data) {

            return null;
        }

        public Distribution getDistributionAIX(OSRelease osRelease, String data) {
            return null;
        }

        public Distribution getDistributionFreeBSD(OSRelease osRelease, String data) {

            return null;
        }

        public Distribution getDistributionOpenBSD(OSRelease osRelease, String data) {

            return null;
        }

        public Distribution getDistributionSlackware(OSRelease osRelease, String data) {
            Distribution d = null;
            if (data.indexOf("Slackware") > -1) {
                d.setDistribution(osRelease.getName());
                Pattern p = Pattern.compile("(\\w+[.]\\w+)");
                Matcher m = p.matcher(data);
                if (m.find()) {
                    d.setDistributionVersion(m.group(1));
                }
            }
            return d;
        }

        public Distribution getDistributionAlpine(OSRelease osRelease, String data) {

            return null;
        }

        public Distribution getDistributionSMGL(OSRelease osRelease, String data) {

            return null;
        }

        public Distribution getDistributionSunOS(OSRelease osRelease, String data) {

            return null;
        }

        public Distribution getDistributionSuSE(OSRelease osRelease, String data) {

            return null;
        }

        public Distribution getDistributionDarwin(OSRelease osRelease, String data) {
            Distribution d = new Distribution();
            d.setDistribution("MacOSX");
            return d;
        }

        public Distribution getDistributionDebian(OSRelease osRelease, String data) {
            Distribution d = null;
            if (data.indexOf("Debian") > -1 || data.indexOf("Rasbian") > -1) {
                d = new Distribution();
                d.setDistribution("Debian");

                Pattern pattern = Pattern.compile("PRETTY_NAME=[^(]+ \\(?([^)]+?)\\)");
                Matcher m = pattern.matcher(data);
                if (m.find()) {
                    d.setDistributionRelease(m.group(1));
                }

            } else if (data.indexOf("Ubuntu") > -1) {
                d = new Distribution();
                d.setDistribution("Ubuntu");
                Pattern pattern = Pattern.compile("VERSION=\"(.*)\"");
                Matcher m = pattern.matcher(data);
                if (m.find()) {
                    d.setDistributionRelease(m.group(1));
                }
            }

            return d;
        }

        public Distribution getDistributionNA(OSRelease osRelease, String data) {

            return null;
        }

        public Distribution getDistributionAmazon(OSRelease osRelease, String data) {
            Distribution d = null;
            if (data.indexOf("Amazon") > -1) {
                d.setDistribution(osRelease.getName());
            }
            return d;
        }

    }*/
}