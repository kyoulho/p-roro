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
 * SangCheon Park   Sep 22, 2022		    First Draft.
 */
package io.playce.roro.common.util;

import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1201;
import io.playce.roro.common.dto.thirdparty.ThirdPartyDiscoveryResult;
import io.playce.roro.common.dto.thirdparty.ThirdPartySearchTypeResponse;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
public class ThirdPartySolutionUtil {

    private static final String CMD_ERROR = CommonProperties.getCmdError();
    private static final String CMD_ERROR_SCRIPT = "; if [ $? != 0 ]; then echo " + CMD_ERROR + "; else ";

    private static final String GET_USERNAME_FROM_UID = "sudo awk -F: '{if($3 == '\"%s\"') print $1}' /etc/passwd";
    private static final String GET_USERNAME_FROM_USER = "sudo awk -F: '{print $1}' /etc/passwd | egrep '^%s'";

    /**
     * @param targetHost            targetHost
     * @param isWindows             isWindows
     * @param componentName         Assessment의 componentName (this.getClass().getName())
     * @param processList           processList
     * @param installedSoftwareList installedSoftwareList
     * @param serviceList           serviceList
     * @param portList              portList
     * @param scheduleMap           scheduleMap
     *
     * @return
     */
    public static List<ThirdPartyDiscoveryResult> detectThirdPartySolutionsFromServer(TargetHost targetHost, boolean isWindows, String componentName,
                                                                                      List<String> processList, List<String> processUserList, List<String> installedSoftwareList,
                                                                                      List<String> serviceList, List<Integer> portList, Map<String, String> scheduleMap) {
        if (targetHost == null) {
            return new ArrayList<>();
        }

        Map<Long, ThirdPartyDiscoveryResult> thirdPartyMap = new HashMap<>();

        List<ThirdPartySearchTypeResponse> thirdPartySearchTypeList = targetHost.getThirdPartySearchTypeList();

        List<ThirdPartyDiscoveryResult> thirdPartySolutions = new ArrayList<>();
        ThirdPartyDiscoveryResult thirdPartyDiscoveryResult;
        ThirdPartyDiscoveryResult.ThirdPartyDiscoveryDetail thirdPartyDiscoveryDetail;

        if (processList != null) {
            processList = processList.stream().distinct().collect(Collectors.toList());
        }

        if (processUserList != null) {
            processUserList = processUserList.stream().distinct().collect(Collectors.toList());

            // 윈도우가 아니고 3rd party search type에 RUNUSER가 있으면..
            if (!isWindows && thirdPartySearchTypeList.stream().anyMatch(s -> Domain1201.RUNUSER.name().equals(s.getSearchType()))) {
                List<String> userList = processUserList;
                String username;
                for (int i = 0; i < userList.size(); i++) {
                    // https://cloud-osci.atlassian.net/browse/PCR-5681
                    // process user가 숫자이거나, +로 끝나는지 확인한다.
                    username = userList.get(i);

                    try {
                        if (NumberUtils.isDigits(username)) {
                            username = SSHUtil.executeCommand(targetHost, String.format(GET_USERNAME_FROM_UID, username));
                        } else if (username.endsWith("+")) {
                            username = SSHUtil.executeCommand(targetHost, String.format(GET_USERNAME_FROM_USER, username.replaceAll("\\+", StringUtils.EMPTY)));
                            username = username.lines().limit(1).collect(Collectors.toList()).get(0);
                        }
                    } catch (Exception e) {
                        // ignore
                        log.warn("Unable to get full username for [{}]", username);
                    }

                    processUserList.set(i, username);
                }
            }
        }

        String command = null, result, message;
        boolean detected;
        for (ThirdPartySearchTypeResponse thirdPartySearchType : thirdPartySearchTypeList) {
            detected = false;
            result = null;
            message = null;

            if (Domain1201.PROCESS.name().equals(thirdPartySearchType.getSearchType())) {
                try {
                    if (processList == null) {
                        continue;
                    }

                    List<String> searchList = Arrays.stream(thirdPartySearchType.getSearchValue().split(","))
                            .map(String::trim)
                            .filter(s -> StringUtils.isNotEmpty(s))
                            .collect(Collectors.toList());

                    int matchCount;

                    for (String p : processList) {
                        matchCount = 0;

                        for (String s : searchList) {
                            if (p.contains(s)) {
                                matchCount++;
                            }
                        }

                        if (matchCount == searchList.size()) {
                            detected = true;
                            message = "[" + p + "] process is running.";
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.warn("Unhandled exception occurred while check 3rd party solutions by process. [Reason] : {}", e.getMessage());
                }
            } else if (Domain1201.RUNUSER.name().equals(thirdPartySearchType.getSearchType())) {
                try {
                    if (processUserList == null) {
                        continue;
                    }

                    for (String username : processUserList) {
                        if (username.equalsIgnoreCase(thirdPartySearchType.getSearchValue())) {
                            detected = true;
                            message = "[" + username + "] user's process is running.";
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.warn("Unhandled exception occurred while check 3rd party solutions by process runtime user. [Reason] : {}", e.getMessage());
                }
            } else if (Domain1201.PKG.name().equals(thirdPartySearchType.getSearchType())) {
                try {
                    if (installedSoftwareList != null) {
                        List<String> swList = installedSoftwareList.stream().filter(s -> s.toLowerCase().contains(thirdPartySearchType.getSearchValue().toLowerCase().trim())).collect(Collectors.toList());
                        if (swList != null && swList.size() > 0) {
                            detected = true;
                            message = "[" + swList.get(0) + "] package installed.";
                        }
                    } else {
                        if (componentName.toLowerCase().contains("windows")) {
                            command = "wmic product get name | findstr -i \"" + thirdPartySearchType.getSearchValue().trim() + "\"";
                            result = WinRmUtils.executeCommand(targetHost, command);

                            // 임시로 방어코드 추가
                            if (thirdPartySearchType.getSearchValue().trim().equalsIgnoreCase("redis") && result.contains("Redistributable")) {
                                result = null;
                            }
                        } else {
                            if (componentName.toLowerCase().contains("debian") || componentName.toLowerCase().contains("ubuntu")) {
                                command = "dpkg -l | awk '{print $2, $3}' | grep -i '" + thirdPartySearchType.getSearchValue().trim() + "'";
                            } else if (componentName.toLowerCase().contains("linux") || componentName.toLowerCase().contains("redhat") || componentName.toLowerCase().contains("fedora")) {
                                command = "rpm -qa | grep -i '" + thirdPartySearchType.getSearchValue().trim() + "'";
                            } else if (componentName.toLowerCase().contains("aix")) {
                                command = "lslpp -lc | egrep -v '^#' | awk -F':' '{print $2, $3}' | grep -i '" + thirdPartySearchType.getSearchValue().trim() + "'";
                            } else if (componentName.toLowerCase().contains("hpux")) {
                                command = "swlist | egrep -v '^#' | awk '{print $1, $2}' | grep -i '" + thirdPartySearchType.getSearchValue().trim() + "'";
                            } else if (componentName.toLowerCase().contains("solaris")) {
                                command = "pkginfo | awk '{print $2}' | grep -i '" + thirdPartySearchType.getSearchValue().trim() + "'";
                            }

                            if (StringUtils.isNotEmpty(command)) {
                                result = SSHUtil.executeCommand(targetHost, command);
                            }
                        }

                        if (StringUtils.isNotEmpty(result)) {
                            detected = true;
                            message = "[" + result.split("\n")[0] + "] package installed.";
                        }
                    }
                } catch (Exception e) {
                    log.warn("Unhandled exception occurred while check 3rd party solutions by package. [Reason] : {}", e.getMessage());
                }
            } else if (Domain1201.SVC.name().equals(thirdPartySearchType.getSearchType())) {
                try {
                    if (serviceList != null && serviceList.stream().anyMatch(s -> s.toLowerCase().contains(thirdPartySearchType.getSearchValue().toLowerCase().replaceAll("\\*", StringUtils.EMPTY)))) {
                        detected = true;
                        message = "[" + thirdPartySearchType.getSearchValue() + "] service exists.";
                    }
                } catch (Exception e) {
                    log.warn("Unhandled exception occurred while check 3rd party solutions by service. [Reason] : {}", e.getMessage());
                }
            } else if (Domain1201.CMD.name().equals(thirdPartySearchType.getSearchType())) {
                try {
                    if (isWindows) {
                        command = "where \"" + thirdPartySearchType.getSearchValue().trim() + "\"";
                        result = WinRmUtils.executeCommand(targetHost, command);
                    } else {
                        command = "type '" + thirdPartySearchType.getSearchValue().trim() + "'";
                        result = SSHUtil.executeCommand(targetHost, command);
                    }

                    if (StringUtils.isNotEmpty(result)) {
                        detected = true;
                        message = "[" + thirdPartySearchType.getSearchValue() + "] command exists.";
                    }
                } catch (Exception e) {
                    log.warn("Unhandled exception occurred while check 3rd party solutions by command. [Reason] : {}", e.getMessage());
                }
            } else if (Domain1201.PORT.name().equals(thirdPartySearchType.getSearchType())) {
                if (portList != null && portList.stream().anyMatch(p -> p == Integer.parseInt(thirdPartySearchType.getSearchValue()))) {
                    detected = true;
                    message = "[" + thirdPartySearchType.getSearchValue() + "] port is listening.";
                }
            } else if (Domain1201.SCHEDULE.name().equals(thirdPartySearchType.getSearchType())) {
                if (isWindows) {
                    if (scheduleMap != null && scheduleMap.keySet().stream().filter(c -> c.contains(thirdPartySearchType.getSearchValue().trim())).count() > 0) {
                        detected = true;
                        message = "[" + thirdPartySearchType.getSearchValue() + "] task exists in schedules.";
                    }
                } else {
                    if (scheduleMap != null && scheduleMap.values().stream().filter(c -> c.contains(thirdPartySearchType.getSearchValue().trim())).count() > 0) {
                        detected = true;
                        message = "[" + thirdPartySearchType.getSearchValue() + "] string exists in crontab.";
                    }
                }
            }
            /*
            } else if (Domain1201.FILE.name().equals(thirdPartySearchType.getSearchType())) {
                try {
                    if (isWindows && Domain101.Y.name().equals(thirdPartySearchType.getWindowsYn())) {
                        result = windowsFileCheck(targetHost, null, thirdPartySearchType.getSearchValue().trim());
                    } else if (!isWindows && Domain101.N.name().equals(thirdPartySearchType.getWindowsYn())) {
                        result = linuxFileCheck(targetHost, null, thirdPartySearchType.getSearchValue().trim());
                    }

                    if (StringUtils.isNotEmpty(result)) {
                        detected = true;
                        message = "[" + result + "] file exists.";
                    }
                } catch (Exception e) {
                    log.warn("Unhandled exception occurred while check 3rd party solutions by file. [Reason] : {}", e.getMessage());
                }
            }
            */

            if (detected) {
                if (thirdPartyMap.get(thirdPartySearchType.getThirdPartySolutionId()) != null) {
                    thirdPartyDiscoveryResult = thirdPartyMap.get(thirdPartySearchType.getThirdPartySolutionId());
                } else {
                    thirdPartyDiscoveryResult = new ThirdPartyDiscoveryResult();
                    thirdPartyDiscoveryResult.setThirdPartySolutionId(thirdPartySearchType.getThirdPartySolutionId());
                    thirdPartyDiscoveryResult.setName(thirdPartySearchType.getThirdPartySolutionName());
                    thirdPartyDiscoveryResult.setVendor(thirdPartySearchType.getVendor());
                    thirdPartyMap.put(thirdPartySearchType.getThirdPartySolutionId(), thirdPartyDiscoveryResult);
                    thirdPartySolutions.add(thirdPartyDiscoveryResult);
                }

                thirdPartyDiscoveryDetail = new ThirdPartyDiscoveryResult.ThirdPartyDiscoveryDetail();
                thirdPartyDiscoveryDetail.setThirdPartySearchTypeId(thirdPartySearchType.getThirdPartySearchTypeId());
                thirdPartyDiscoveryDetail.setType(Domain1201.valueOf(thirdPartySearchType.getSearchType()).fullname());
                thirdPartyDiscoveryDetail.setValue(message);

                thirdPartyDiscoveryResult.getDiscoveryDetails().add(thirdPartyDiscoveryDetail);
            }
        }

        // JSON 결과에서 제외
        // if (thirdPartySolutions.size() == 0) {
        //     thirdPartySolutions = null;
        // }

        return thirdPartySolutions;
    }

    /**
     * @param targetHost targetHost
     * @param isWindows  isWindows
     * @param paths      paths
     *
     * @return
     */
    @Deprecated
    public static List<ThirdPartyDiscoveryResult> detectThirdPartySolutionsFromMiddleware(TargetHost targetHost, boolean isWindows, String... paths) {
        return detectThirdPartySolutionByFile(targetHost, Domain1001.MW, isWindows, paths);
    }

    /**
     * @param targetHost targetHost
     * @param isWindows  isWindows
     * @param deployPath deployPath
     *
     * @return
     */
    @Deprecated
    public static List<ThirdPartyDiscoveryResult> detectThirdPartySolutionsFromApplication(TargetHost targetHost, boolean isWindows, String deployPath) {
        return detectThirdPartySolutionByFile(targetHost, Domain1001.APP, isWindows, deployPath);
    }

    /**
     * @param targetHost     targetHost
     * @param applicationDir applicationDir
     * @param baseDir        baseDir
     *
     * @return
     */
    @Deprecated
    public static List<ThirdPartyDiscoveryResult> detectThirdPartySolutionsFromApplication(TargetHost targetHost, File applicationDir, String baseDir) {
        if (!applicationDir.isDirectory()) {
            return null;
        }

        List<ThirdPartyDiscoveryResult> thirdPartySolutions = new ArrayList<>();
        ThirdPartyDiscoveryResult thirdPartyDiscoveryResult;
        ThirdPartyDiscoveryResult.ThirdPartyDiscoveryDetail thirdPartyDiscoveryDetail;

        Map<Long, ThirdPartyDiscoveryResult> thirdPartyMap = new HashMap<>();

        List<ThirdPartySearchTypeResponse> thirdPartySearchTypeList = targetHost.getThirdPartySearchTypeList();

        StringBuilder regex;
        String path, result;
        for (ThirdPartySearchTypeResponse thirdPartySearchType : thirdPartySearchTypeList) {
            /*
            if (Domain1201.FILE.name().equals(thirdPartySearchType.getSearchType()) && Domain1001.APP.name().equals(thirdPartySearchType.getInventoryTypeCode())) {
                try {
                    path = thirdPartySearchType.getSearchValue();
                    path = path.replaceAll("\\*", ".*");

                    regex = new StringBuilder("(")
                            .append(".*").append(path).append(".*").append(")");

                    Pattern pattern = Pattern.compile(regex.toString());

                    result = findFile(applicationDir, pattern);

                    if (StringUtils.isNotEmpty(result)) {
                        // 경로상의 {roro_work_dir}/application/{id} 삭제
                        result = result.replaceAll(baseDir, ".");

                        if (thirdPartyMap.get(thirdPartySearchType.getThirdPartySolutionId()) == null) {
                            thirdPartyDiscoveryResult = new ThirdPartyDiscoveryResult();
                            thirdPartyDiscoveryResult.setThirdPartySolutionId(thirdPartySearchType.getThirdPartySolutionId());
                            thirdPartyDiscoveryResult.setName(thirdPartySearchType.getThirdPartySolutionName());
                            thirdPartyDiscoveryResult.setVendor(thirdPartySearchType.getVendor());
                            thirdPartyMap.put(thirdPartySearchType.getThirdPartySolutionId(), thirdPartyDiscoveryResult);
                            thirdPartySolutions.add(thirdPartyDiscoveryResult);
                        } else {
                            thirdPartyDiscoveryResult = thirdPartyMap.get(thirdPartySearchType.getThirdPartySolutionId());
                        }

                        thirdPartyDiscoveryDetail = new ThirdPartyDiscoveryResult.ThirdPartyDiscoveryDetail();
                        thirdPartyDiscoveryDetail.setThirdPartySearchTypeId(thirdPartySearchType.getThirdPartySearchTypeId());
                        thirdPartyDiscoveryDetail.setType(Domain1201.valueOf(thirdPartySearchType.getSearchType()).fullname());
                        thirdPartyDiscoveryDetail.setValue("[" + result + "] file exists.");

                        thirdPartyDiscoveryResult.getDiscoveryDetails().add(thirdPartyDiscoveryDetail);
                    }
                } catch (Exception e) {
                    log.warn("Unhandled exception occurred while check 3rd party solutions by file. [Reason] : {}", e.getMessage());
                }
            }
            */
        }

        return thirdPartySolutions;
    }

    @Deprecated
    private static List<ThirdPartyDiscoveryResult> detectThirdPartySolutionByFile(TargetHost targetHost, Domain1001 inventoryTypeCode, boolean isWindows, String... paths) {
        List<ThirdPartyDiscoveryResult> thirdPartySolutions = new ArrayList<>();
        ThirdPartyDiscoveryResult thirdPartyDiscoveryResult;
        ThirdPartyDiscoveryResult.ThirdPartyDiscoveryDetail thirdPartyDiscoveryDetail;

        Map<Long, ThirdPartyDiscoveryResult> thirdPartyMap = new HashMap<>();

        List<ThirdPartySearchTypeResponse> thirdPartySearchTypeList = targetHost.getThirdPartySearchTypeList();

        String result = null;
        for (ThirdPartySearchTypeResponse thirdPartySearchType : thirdPartySearchTypeList) {
            /*
            if (Domain1201.FILE.name().equals(thirdPartySearchType.getSearchType()) && inventoryTypeCode.name().equals(thirdPartySearchType.getInventoryTypeCode())) {
                try {
                    if (isWindows && Domain101.Y.name().equals(thirdPartySearchType.getWindowsYn())) {
                        result = windowsFileCheck(targetHost, paths, thirdPartySearchType.getSearchValue());
                    } else if (!isWindows && Domain101.N.name().equals(thirdPartySearchType.getWindowsYn())) {
                        result = linuxFileCheck(targetHost, paths, thirdPartySearchType.getSearchValue());
                    }

                    if (StringUtils.isNotEmpty(result)) {
                        if (thirdPartyMap.get(thirdPartySearchType.getThirdPartySolutionId()) == null) {
                            thirdPartyDiscoveryResult = new ThirdPartyDiscoveryResult();
                            thirdPartyDiscoveryResult.setThirdPartySolutionId(thirdPartySearchType.getThirdPartySolutionId());
                            thirdPartyDiscoveryResult.setName(thirdPartySearchType.getThirdPartySolutionName());
                            thirdPartyDiscoveryResult.setVendor(thirdPartySearchType.getVendor());
                            thirdPartyMap.put(thirdPartySearchType.getThirdPartySolutionId(), thirdPartyDiscoveryResult);
                            thirdPartySolutions.add(thirdPartyDiscoveryResult);
                        } else {
                            thirdPartyDiscoveryResult = thirdPartyMap.get(thirdPartySearchType.getThirdPartySolutionId());
                        }

                        thirdPartyDiscoveryDetail = new ThirdPartyDiscoveryResult.ThirdPartyDiscoveryDetail();
                        thirdPartyDiscoveryDetail.setThirdPartySearchTypeId(thirdPartySearchType.getThirdPartySearchTypeId());
                        thirdPartyDiscoveryDetail.setType(Domain1201.valueOf(thirdPartySearchType.getSearchType()).fullname());
                        thirdPartyDiscoveryDetail.setValue("[" + result + "] file exists.");

                        thirdPartyDiscoveryResult.getDiscoveryDetails().add(thirdPartyDiscoveryDetail);
                    }
                } catch (Exception e) {
                    log.warn("Unhandled exception occurred while check 3rd party solutions by file. [Reason] : {}", e.getMessage());
                }
            }
            */
        }

        // JSON 결과에서 제외
        // if (thirdPartySolutions.size() == 0) {
        //     thirdPartySolutions = null;
        // }

        return thirdPartySolutions;
    }

    private static String findFile(File dir, Pattern filePattern) {
        String result = null;

        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                if (f.listFiles() != null) {
                    result = findFile(f, filePattern);

                    if (StringUtils.isNotEmpty(result)) {
                        break;
                    }
                }
            } else {
                if (filePattern.matcher(f.getAbsolutePath()).find()) {
                    result = f.getAbsolutePath();
                    break;
                }
            }
        }

        return result;
    }

    private static String linuxFileCheck(TargetHost targetHost, String[] basePaths, String searchValue) throws Exception {
        String result = null;
        StringBuilder command;

        // 서버 환경에서 기본 경로가 없는 경우
        if (basePaths == null || basePaths.length == 0) {
            String[] paths = devideFilePathAndName(searchValue, false);

            command = new StringBuilder();

            command.append("sudo sh -c '")
                    .append("cd ")
                    .append(paths[0])
                    .append(CMD_ERROR_SCRIPT);

            if (paths[1].contains("/")) {
                command.append("find . -type f -path \"*")
                        .append(paths[1])
                        .append("\";fi'");
            } else {
                command.append("find . -type f -name \"")
                        .append(paths[1])
                        .append("\";fi'");
            }

            result = SSHUtil.executeCommand(targetHost, command.toString());

            if (StringUtils.isNotEmpty(result)) {
                if (result.contains(CMD_ERROR)) {
                    result = null;
                } else {
                    result = getFindResult(result);

                    if (StringUtils.isNotEmpty(result)) {
                        if (!paths[0].endsWith("/")) {
                            result = paths[0] + "/" + result;
                        } else {
                            result = paths[0] + result;
                        }
                    }
                }
            }
        } else {
            // Middleware나 Application 내에서 파일을 찾는 경우
            for (String path : basePaths) {
                command = new StringBuilder();

                // path로 change directory 후 searchValue를 찾음
                command.append("sudo sh -c '")
                        .append("cd ")
                        .append(path)
                        .append(CMD_ERROR_SCRIPT);

                if (searchValue.contains("/")) {
                    command.append("find . -type f -path \"*")
                            .append(searchValue)
                            .append("\";fi'");
                } else {
                    command.append("find . -type f -name \"")
                            .append(searchValue)
                            .append("\";fi'");
                }

                result = SSHUtil.executeCommand(targetHost, command.toString());

                if (StringUtils.isNotEmpty(result)) {
                    if (result.contains(CMD_ERROR)) {
                        result = null;
                    } else {
                        result = getFindResult(result);

                        if (StringUtils.isNotEmpty(result)) {
                            if (!path.endsWith("/")) {
                                result = path + "/" + result;
                            } else {
                                result = path + result;
                            }

                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

    private static String windowsFileCheck(TargetHost targetHost, String[] basePaths, String searchValue) throws Exception {
        String result = null;
        StringBuilder command = new StringBuilder();

        String[] paths = devideFilePathAndName(searchValue, true);

        // 서버 환경에서 기본 경로가 없는 경우
        if (basePaths == null || basePaths.length == 0) {
            command.append("get-childitem -path ")
                    .append(paths[0])
                    .append(" -include ")
                    .append(paths[1])
                    .append(" -recurse -force | %{$_.fullname}");

            result = WinRmUtils.executePsShell(targetHost, command.toString());

            if (StringUtils.isNotEmpty(result)) {
                result = result.split("\n")[0].replaceAll("\r", StringUtils.EMPTY);
            }
        } else {
            // Middleware나 Application 내에서 파일을 찾는 경우
            for (String path : basePaths) {
                // path로 change directory 후 searchValue를 찾음
                command.append("get-childitem -path ")
                        .append(path)
                        .append("\\*\\")
                        .append(paths[0])
                        .append(" -include ")
                        .append(paths[1])
                        .append(" -recurse -force | %{$_.fullname}");

                result = WinRmUtils.executePsShell(targetHost, command.toString());

                if (StringUtils.isNotEmpty(result)) {
                    result = result.split("\n")[0].replaceAll("\r", StringUtils.EMPTY);
                    break;
                }
            }
        }

        return result;
    }

    /**
     * File의 디렉토리와 파일명을 구분한다.
     *
     * @param file
     *
     * @return
     */
    private static String[] devideFilePathAndName(String file, boolean isWindows) {
        // /opt/servers/roro-svr/webapps/ROOT/WEB-INF/lib/log4j*.jar => [/opt/servers/roro-svr/webapps/ROOT/WEB-INF/lib, log4j*.jar]
        // /opt/servers/roro-svr/web*/lib/log4j*.jar => [/opt/servers/roro-svr, web*/lib/log4j*.jar]
        // C:\jboss\jboss6.4\*\deploy*\*\sess*.jsp => [C:\jboss\jboss6.4\*\deploy*\*, sess*.jsp]

        int idx;
        String path, name;

        if (isWindows) {
            file = file.replaceAll("\\\\", "/");
            idx = file.lastIndexOf("/");

            if (idx > -1) {
                path = file.substring(0, idx);
                name = file.substring(idx + 1);
            } else {
                path = "C:\\";
                name = file;
            }

            path = path.replaceAll("/", "\\\\");
            name = name.replaceAll("/", "\\\\");
        } else {
            if (file.contains("*")) {
                idx = file.substring(0, file.indexOf("*")).lastIndexOf("/");
            } else {
                idx = file.lastIndexOf("/");
            }

            if (idx > -1) {
                path = file.substring(0, idx);
                name = file.substring(idx + 1);
            } else {
                path = "/";
                name = file;
            }
        }

        return new String[]{path, name};
    }

    private static String getFindResult(String result) {
        String[] paths = result.replaceAll("\r", StringUtils.EMPTY).split("\n");

        for (String path : paths) {
            if (!path.contains("find:")) {
                if (path.startsWith(".")) {
                    path = path.substring(1);
                }
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }

                return path;
            }
        }

        return null;
    }

    public static void main(String[] args) throws Exception {
        System.err.println(Arrays.toString(devideFilePathAndName("/opt/servers/roro-svr/webapps/ROOT/WEB-INF/lib/log4j*.jar", false)));
        System.err.println(Arrays.toString(devideFilePathAndName("/opt/servers/roro-svr/web*/lib/log4j*.jar", false)));
        System.err.println(Arrays.toString(devideFilePathAndName("C:\\jboss\\jboss6.4\\*\\deploy*\\*\\sess*.jsp", true)));

        String result;
        TargetHost targetHost = new TargetHost();

        targetHost.setIpAddress("192.168.4.61");
        targetHost.setPort(22);
        targetHost.setUsername("roro");
        targetHost.setPassword("jan01jan");

        result = linuxFileCheck(targetHost, null, "/opt/servers/roro-svr/webapps/ROOT/WEB-INF/lib/log4j*.jar");
        System.err.println(result);

        result = linuxFileCheck(targetHost, null, "/opt/servers/roro-svr/web*/lib/log4j*.jar");
        System.err.println(result);

        result = linuxFileCheck(targetHost, new String[]{"/opt/servers/roro-svr/webapps/ROOT"}, "lib/log4j*.jar");
        System.err.println(result);

        result = SSHUtil.executeCommand(targetHost, "type java");
        System.err.println(result);

        result = SSHUtil.executeCommand(targetHost, "type helloworld");
        System.err.println(result);

        //*
        targetHost = new TargetHost();
        targetHost.setIpAddress("192.168.1.95");
        targetHost.setPort(5985);
        targetHost.setUsername("roro2");
        targetHost.setPassword("jan01jan");

        result = windowsFileCheck(targetHost, null, "C:\\jboss\\jboss6.4\\*\\deploy*\\*\\sess*.jsp");
        System.err.println(result);

        result = windowsFileCheck(targetHost, new String[]{"C:\\jboss\\jboss6.4"}, "deploy*\\*\\sess*.jsp");
        System.err.println(result);

        result = WinRmUtils.executeCommand(targetHost, "where java");
        System.err.println(result);

        result = WinRmUtils.executeCommand(targetHost, "where helloworld");
        System.err.println(result);
        //*/

        String path = "/Download*/osci-*.ppk";
        path = path.replaceAll("\\*", ".*");

        StringBuilder regex = new StringBuilder("(");
        regex.append(".*").append(path).append(".*").append(")");
        Pattern pattern = Pattern.compile(regex.toString());

        System.err.println("Result : " + findFile(new File("/Users/nices96"), pattern));
    }
}