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
 * Jeongho Baek   6월 23, 2021		First Draft.
 */
package io.playce.roro.mw.asmt.util;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.playce.roro.common.util.ThreadLocalUtils.MW_SCAN_ERROR;
import static io.playce.roro.mw.asmt.AbstractMiddlewareAssessment.getSshCommandResultTrim;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 3.0.0
 */
@Slf4j
public class WasAnalyzerUtil {

    // private static final String JAVA_VERSION_COMMAND = "java -version 2>&1 | head -n 1 | awk -F '\"' '{print $2}'";
    private static final String RUNNING_PROCESS = "RUNNING";
    private static final String STOP_PROCESS = "STOP";

    public static void saveAssessmentFile(String ipAddress, String fullFilePath, String contents, String workDir, GetInfoStrategy strategy) {
        try {
            // String saveDir = getWorkDir(workDir) + File.separator + "assessment" + File.separator + "raw_files" + File.separator + ipAddress;
            String saveDir = workDir + File.separator + "assessment" + File.separator + "raw_files" + File.separator + ipAddress;
            // FileUtils.forceMkdir(new File(saveDir + fullFilePath.substring(0, fullFilePath.lastIndexOf(File.separator))));
            // File file = new File(saveDir + fullFilePath);

            String saveDirectory = strategy.getParentDirectoryByPath(saveDir, fullFilePath);
            FileUtils.forceMkdir(new File(saveDirectory));
            log.debug("Assessment File will be save to [{}]", saveDir + fullFilePath);
            File file = new File(saveDir + fullFilePath);
            FileUtils.writeStringToFile(file, contents, "UTF-8");
        } catch (Exception e) {
            log.error("Unhandled exception occurred while save Assessment Files.", e);
            ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while save Assessment Files. Detail : [" + e.getMessage() + "]");
        }
    }

    // public static String getWorkDir(String workDir) {
    //     if (workDir == null || "null".equals(workDir)) {
    //         workDir = FileUtils.getTempDirectory().getAbsolutePath();
    //     }
    //     return workDir;
    // }

    // 실행되고 있는 Java의 HeapMemory(-Xms, -Xmx) 설정을 가져온다.
    public static String getHeapSize(TargetHost targetHost, String processName, String heapMemoryName, CommandConfig commandConfig, GetInfoStrategy strategy, String... excludeStrs) throws InterruptedException {
        String[] jvmOptionArray = getProcessArgument(targetHost, processName, commandConfig, strategy, excludeStrs);
        String memoryCapacity = "";

        for (String jvmOption : jvmOptionArray) {
            if (jvmOption.startsWith(heapMemoryName)) {
                memoryCapacity = jvmOption;
                break;
            }
        }

        return memoryCapacity.replace(heapMemoryName, StringUtils.EMPTY);
    }

    public static String getJvmOptions(TargetHost targetHost, String processName, CommandConfig commandConfig, GetInfoStrategy strategy, String... excludeStrs) throws InterruptedException {
        List<String> jvmOptions = new ArrayList<>();
        String[] jvmOptionArray = getProcessArgument(targetHost, processName, commandConfig, strategy, excludeStrs);

        for (String jvmOption : jvmOptionArray) {
            if (jvmOption.startsWith("/")
                    || (strategy.isWindows() && jvmOption.contains(":"))
                    || jvmOption.toLowerCase().startsWith("-d")
                    || jvmOption.toLowerCase().startsWith("-x")
                    || jvmOption.toLowerCase().startsWith("-classpath")
                    || jvmOption.toLowerCase().startsWith("-v")) {
                jvmOptions.add(jvmOption);
            }
        }

        return StringUtils.join(jvmOptions, StringUtils.SPACE);
    }

    // 현재 Process 동작여부 판단.
    public static String getProcessStatus(TargetHost targetHost, String processName, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
//        String processStatusCommand = "ps -ef | grep " + processName + " | grep -v grep | awk '{print $2}'";
        String processStatusCommand = COMMAND.PROCESS_STATUS.command(commandConfig, strategy.isWindows(), processName);
        String responseString = getSshCommandResultTrim(targetHost, processStatusCommand, COMMAND.PROCESS_STATUS, strategy);

        return StringUtils.isEmpty(responseString) ? STOP_PROCESS : RUNNING_PROCESS;
    }

    // Process Argument를 가져온다.
    public static String[] getProcessArgument(TargetHost targetHost, String processName, CommandConfig commandConfig, GetInfoStrategy strategy, String... excludeStrs) throws InterruptedException {
        log.debug("*** Execute Method : getProcessArgument ***");

        // String processArgumentCommand = "ps -eo args | grep " + processName + " | grep -v grep | tr ' ' '\\n'";
        String processArgumentCommand = COMMAND.PROCESS_ARGUMENT.command(commandConfig, strategy.isWindows(), processName, processName);
        String responseString = getSshCommandResultTrim(targetHost, processArgumentCommand, COMMAND.PROCESS_ARGUMENT, strategy);

        if (StringUtils.isEmpty(responseString)) {
            return new String[]{StringUtils.EMPTY};
        }

        //process가 여러개 나올때 excludeStrs로 필터링
        String[] spliteResponse = responseString.split(strategy.getCarriageReturn());

        if (spliteResponse.length > 0) {
            List<String> processes = Arrays.stream(spliteResponse).filter(s -> {
                for (String excludeStr : excludeStrs) {
                    s = StringUtils.strip(s, strategy.getCarriageReturn());
                    if (s.contains(excludeStr))
                        return false;
                    if (StringUtils.isEmpty(s))
                        return false;
                }
                return true;
            }).collect(Collectors.toList());

            if (processes.size() > 1) {
                log.error("Check out the logic that brings the process. {}", Arrays.toString(excludeStrs));
            }
            responseString = processes.get(0);
            if (strategy.isWindows() && responseString.startsWith("CommandLine")) {
                responseString = responseString.substring(12);
            }

            // if (StringUtils.isNotEmpty(responseString)) {
            //     return splitToArrayByCrlf(responseString);
            // }
            if (StringUtils.isNotEmpty(responseString)) {
                // https://cloud-osci.atlassian.net/browse/ROROQA-840
                /*/
                try {
                    CSVParser parser = new CSVParserBuilder().withSeparator(' ').build();
                    return parser.parseLine(responseString);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                /*/
                return parseProcess(responseString);
                //*/
            }
        }

        return new String[]{StringUtils.EMPTY};
    }

    public static String[] parseProcess(String process) {
        int startIdx = 0, endIdx = -1;
        String subStr;

        try {
            while (startIdx > -1) {
                startIdx = process.indexOf("\"", endIdx + 1);

                if (startIdx > -1) {
                    endIdx = process.indexOf("\"", startIdx + 1);

                    if (endIdx > -1) {
                        subStr = process.substring(startIdx, endIdx + 1);

                        String subStr1 = subStr.replaceAll(" ", "::SPACE::");

                        // Windows 프로세스에 역슬래시(\)를 포함하는 경우 replaceAll() 에서 "java.util.regex.PatternSyntaxException: Unknown character property name {In/Isr} near index" 에러가 발생
                        // process = process.replaceAll(subStr, subStr1);
                        process = process.substring(0, startIdx) + subStr1 + process.substring(endIdx + 1);
                    } else {
                        endIdx = startIdx + 1;
                    }
                }
            }

            List<String> strList = new ArrayList<>();
            for (String str : process.split(" ")) {
                strList.add(str.replaceAll("::SPACE::", " "));
            }

            return strList.toArray(new String[0]);
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            CSVParser parser = new CSVParserBuilder().withSeparator(' ').build();
            try {
                return parser.parseLine(process);
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        return new String[]{StringUtils.EMPTY};
    }

    // 실행되고 있는 Process의 User를 가져온다.
    public static String getRunUser(TargetHost targetHost, String processName, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("*** Execute Method : getRunUser {} ***", processName);

        // String runUserCommand = "ps -ef | grep " + processName + " | grep -v grep | awk '{print $1}' | uniq";
        String runUserCommand = COMMAND.RUN_USER.command(commandConfig, strategy.isWindows(), processName);
        String responseString = getSshCommandResultTrim(targetHost, runUserCommand, COMMAND.RUN_USER, strategy);

        if (StringUtils.isNotEmpty(responseString)) {
            // String[] runUserArray = splitToArrayByCrlf(responseString);
            String[] runUserArray = responseString.split(StringUtils.SPACE);
            List<String> runUsers = new ArrayList<>(Arrays.asList(runUserArray));

            return StringUtils.join(runUsers, StringUtils.SPACE);
        } else {
            return StringUtils.EMPTY;
        }
    }

    public static String getJavaVersion(TargetHost targetHost, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        String command = COMMAND.JAVA_VERSION_WITH_JAVAHOME.command(commandConfig, strategy.isWindows());
        return getSshCommandResultTrim(targetHost, command, COMMAND.JAVA_VERSION_WITH_JAVAHOME, strategy);
    }

    public static String getJavaVersion(TargetHost targetHost, String javaHome, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        String command = COMMAND.JAVA_VERSION_WITH_JAVAPATH.command(commandConfig, strategy.isWindows(), javaHome);
        return getSshCommandResultTrim(targetHost, command, COMMAND.JAVA_VERSION_WITH_JAVAPATH, strategy);
    }

    public static String getJavaVendor(TargetHost targetHost, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        String command = COMMAND.JAVA_VENDOR_WITH_JAVAHOME.command(commandConfig, strategy.isWindows());
        return getJavaVendorProperty(getSshCommandResultTrim(targetHost, command, COMMAND.JAVA_VENDOR_WITH_JAVAHOME, strategy));
    }

    public static String getJavaVendor(TargetHost targetHost, String javaHome, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        String command = COMMAND.JAVA_VENDOR_WITH_JAVAPATH.command(commandConfig, strategy.isWindows(), javaHome);
        return getJavaVendorProperty(getSshCommandResultTrim(targetHost, command, COMMAND.JAVA_VENDOR_WITH_JAVAPATH, strategy));
    }

    private static String getJavaVendorProperty(String javaVendorProperties) {
        String vendorName = "";

        if (StringUtils.isNotEmpty(javaVendorProperties)) {
            String[] vendor = javaVendorProperties.split("\\r?\\n");

            for (String tempString : vendor) {
                if (tempString.contains("java.vendor =")) {
                    vendorName = tempString.substring(tempString.lastIndexOf("=") + 1).trim();
                }
            }
        }

        return vendorName;
    }

    public static boolean isExistDirectory(TargetHost targetHost, String directoryPath, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        // String cellCommand = "if test -d \"" + directoryPath + "\"; then echo \"exist\"; fi";
        String cellCommand = COMMAND.CELL_COMMAND_CHECK_DIRECTORY.command(commandConfig, strategy.isWindows(), directoryPath);
        String responseString = getSshCommandResultTrim(targetHost, cellCommand, COMMAND.CELL_COMMAND_CHECK_DIRECTORY, strategy);

        return responseString.equals("exist");
    }

    public static boolean isEmptyDirectory(TargetHost targetHost, String directoryPath, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        // String cellCommand = "ls -F " + directoryPath;
        String cellCommand = COMMAND.CELL_COMMAND_EMPTY_DIRECTORY.command(commandConfig, strategy.isWindows(), directoryPath);
        String responseString = getSshCommandResultTrim(targetHost, cellCommand, COMMAND.CELL_COMMAND_EMPTY_DIRECTORY, strategy);

        return StringUtils.isEmpty(responseString);
    }

    // public static String getFileContentCommand(String filePath) {
    //     return "cat " + surroundDoubleQuotation(filePath);
    // }

    // public static String surroundDoubleQuotation(String command) {
    //     return "\"" + command + "\"";
    // }

    public static String removeBackSlashAndSlash(String tempString) {
        if (StringUtils.isNotEmpty(tempString)) {
            return tempString
                    .replaceAll("\\\\", StringUtils.EMPTY)
                    .replaceAll("/", StringUtils.EMPTY);
        }

        return tempString;
    }

}
//end of WasAnalyzerUtil.java