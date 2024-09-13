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
 * Jaeeon Bae       1월 11, 2022            First Draft.
 */
package io.playce.roro.mw.asmt;

import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static io.playce.roro.common.util.ThreadLocalUtils.MW_SCAN_ERROR;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public abstract class AbstractMiddlewareAssessment implements MiddlewareAssessment {
    // key => ex) weblogic.Name=AdminServer 형태.. 유일하게 구분할수 있는 값.
    public static String getJavaVersion(TargetHost targetHost, String key, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        // grep: unknown devices method 방지를 위해 처음 -를 escape 처리한다.
        if (key.startsWith("-")) {
            key = "\\" + key;
        }

        Map<String, String> commandMap = Map.of(COMMAND.JAVA_PATH.name(), COMMAND.JAVA_PATH.command(commandConfig, strategy.isWindows(), key));
        boolean sudo = !strategy.isWindows() && SSHUtil.isSudoer(targetHost);
        Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);
        RemoteExecResult result = resultMap.get(COMMAND.JAVA_PATH.name());
        return MWCommonUtil.getJavaVersion(targetHost, result, sudo, commandConfig, strategy);
    }

    public static String getJavaVendor(TargetHost targetHost, String key, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        // grep: unknown devices method 방지를 위해 처음 -를 escape 처리한다.
        if (key.startsWith("-")) {
            key = "\\" + key;
        }

        Map<String, String> commandMap = Map.of(COMMAND.JAVA_PATH.name(), COMMAND.JAVA_PATH.command(commandConfig, strategy.isWindows(), key));
        boolean sudo = !strategy.isWindows() && SSHUtil.isSudoer(targetHost);
        Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);
        RemoteExecResult result = resultMap.get(COMMAND.JAVA_PATH.name());
        return MWCommonUtil.getJavaVendor(targetHost, result, sudo, commandConfig, strategy);
    }

//    private static final String BLANK = " ";

    // SSH 실행결과 Trim 적용.
//    public static String getSshCommandResultTrim(TargetHost targetHost, String executeCommand, GetInfoStrategy strategy) throws InterruptedException {
//        return getSshCommandResultTrim(targetHost, executeCommand, true, strategy);
//    }

    //    public static String getSshCommandResultTrim(TargetHost targetHost, String executeCommand, boolean isSudo, GetInfoStrategy strategy) throws InterruptedException {
    public static String getSshCommandResultTrim(TargetHost targetHost, String executeCommand, COMMAND cmd, GetInfoStrategy strategy) throws InterruptedException {
//        String sudoExecuteCommand = isSudo ? "sudo " + executeCommand : executeCommand;
//        String sudoExecuteCommand = strategy.isWindows() ? executeCommand : "sudo " + executeCommand;

//        return StringUtils.defaultString(SSHUtil.executeCommand(targetHost, sudoExecuteCommand).trim());
        return StringUtils.defaultString(strategy.executeCommand(targetHost, executeCommand, cmd).trim());
    }

    /**
     * Engine 설치 경로, 인스턴스 설치 경로 하위에 특정 파일이 존재하는지 확인한다.
     *
     * @param absolutePath the absolute path
     * @param strategy
     *
     * @return the boolean
     */
    public static boolean fileExists(TargetHost targetHost, String absolutePath, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        boolean fileExists = false;

//        String command = "sudo sh -c \"[ -f '" + absolutePath + "' ] && echo 'EXIST' || echo 'NOT_EXIST'\"";
//        String result = SSHUtil.executeCommand(targetHost, command).trim();
//        String command = COMMAND.FILE_EXISTS.command(commandConfig, strategy.isWindows(), absolutePath);
//        String result = strategy.executeCommand(targetHost, command, COMMAND.FILE_EXISTS).trim();
        String result = MWCommonUtil.getExecuteResult(targetHost, COMMAND.FILE_EXISTS, commandConfig, strategy, absolutePath);

        if ("EXIST".equals(result)) {
            fileExists = true;
        }

        return fileExists;
    }

    public static String getFileContents(TargetHost targetHost, String absolutePath, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        String result = null;
        absolutePath = StringUtils.strip(absolutePath, "\r");

        try {
//            StringBuilder cmd = new StringBuilder();
//            if (enableSudo) {
//                cmd.append("/usr/bin/sudo").append(BLANK);
//            }
//            cmd.append("cat").append(BLANK).append(absolutePath);

//            result = SSHUtil.executeCommand(targetHost, cmd.toString());
//            String command = COMMAND.CAT.command(commandConfig, strategy.isWindows(), absolutePath);
//            result = strategy.executeCommand(targetHost, command, COMMAND.CAT);
            result = MWCommonUtil.getExecuteResult(targetHost, COMMAND.CAT, commandConfig, strategy, absolutePath);

        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("IOException occurred while get file contents.", e);
            ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while get file contents. Detail : [" + e.getMessage() + "]");
        }

        return result;
    }

    /**
     * 서버에 특정 프로세스가 구동중인지 여부를 확인한다.
     *
     * @param processName the process name
     * @param args        the args
     *
     * @return the boolean
     */
//    protected boolean processRunning(TargetHost targetHost, String processName, String... args) throws InterruptedException {
//        StringBuilder cmd = new StringBuilder("ps").append(BLANK).append("-ef").append(BLANK)
//                .append("|").append(BLANK).append("grep").append(BLANK).append(processName);
//
//        for (String arg : args) {
//            cmd.append(BLANK).append("|").append(BLANK).append("grep").append(BLANK).append(arg);
//        }
//
//        cmd.append(BLANK).append("|").append(BLANK).append("grep -v grep");
//
//        String result = SSHUtil.executeCommand(targetHost, cmd.toString()).trim();
//
//        if (result.contains(processName)) {
//            return true;
//        }
//
//        return false;
//    }

    /**
     * Save Middleware Assessment files.
     *
     * @param ipAddress the ip address
     * @param path      the file path
     * @param contents  the contents
     * @param strategy
     */
    public static void saveMiddlewareFile(String ipAddress, String path, String contents, GetInfoStrategy strategy) throws InterruptedException {
        try {
            String workDir = CommonProperties.getWorkDir() + File.separator
                    + "assessment" + File.separator + "raw_files" + File.separator + ipAddress;

            log.debug("Assessment File will be save to [{}]", workDir + path);

            String saveDirectory = strategy.getParentDirectoryByPath(workDir, path); //
            FileUtils.forceMkdir(new File(saveDirectory));
            File file = new File(workDir + path);
            FileUtils.writeStringToFile(file, contents, "UTF-8");
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.debug("Unhandled exception occurred while save Middleware Assessment Files.", e);
            ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while save Middleware Assessment Files. Detail : [" + e.getMessage() + "]");
        }
    }


    public static boolean hasUploadedConfigFile(MiddlewareInventory middleware) {
        return StringUtils.isNotEmpty(middleware.getConfigFilePath());
    }

    public static String findFileAbsolutePath(String rootPath, String targetPath) {
        File files[] = new File(rootPath).listFiles();

        String path = null;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                path = findFileAbsolutePath(file.getPath(), targetPath);
                if (StringUtils.isNotEmpty(path))
                    break;
            } else {
                if (file.getAbsolutePath().contains(targetPath)) {
                    return file.getAbsolutePath();
                }
            }
        }

        return path;
    }

    public static String readUploadedFile(String absolutePath) throws InterruptedException {
        String result = null;
        try {
            result = FileUtils.readFileToString(new File(absolutePath), "UTF-8");
        } catch (IOException e) {
            RoRoException.checkInterruptedException(e);
            log.error("IOException occurred while get file contents.", e);
            ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while get file contents. Detail : [" + e.getMessage() + "]");
        }

        return result;
    }

  /*  public static String getFileContents(TargetHost targetHost, String absolutePath, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        if (!targetHost.getUsername().equals("root")) {
            return getFileContents(targetHost, absolutePath, true);
        }
        return getFileContents(targetHost, absolutePath, false);
    }*/

    /*public static String getFileContents(TargetHost targetHost, String absolutePath, boolean enableSudo) {
        String result = null;

//        try {
//            if (isWeb()) {
                StringBuilder cmd = new StringBuilder("");

                if (enableSudo) {
                    cmd.append("/usr/bin/sudo").append(BLANK);
                }

                cmd.append("cat").append(BLANK).append(absolutePath);

                result = SSHUtil.executeCommand(targetHost, cmd.toString());
//            } else {
//                result = FileUtils.readFileToString(new File(absolutePath), "UTF-8");
//            }
//        } catch (IOException e) {
//            log.error("IOException occurred while get file contents.", e);
//        }

        return result;
    }*/
}