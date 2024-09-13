/*
 * Copyright 2020 The Playce-RoRo Project.
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
 * Sang-cheon Park	2020. 3. 2.		First Draft.
 */
package io.playce.roro.common.util;

import com.jcraft.jsch.*;
import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.exception.ConnectionErrorException;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.Security;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
@Slf4j
public class SSHUtil {

    // AIX에서는 RORO_CMD_ERROR 가 console output으로 출력되면서 RORO _CMD _ERROR 와 같은 형태로 변경되는 경우가 있음.
    private static final String START_WHOAMI_CMD = CommonProperties.getStartWhoamiCmd();
    private static final String END_WHOAMI_CMD = CommonProperties.getEndWhoamiCmd();
    private static final String START_CMD = CommonProperties.getStartCustomCmd();
    private static final String END_CMD = CommonProperties.getEndCustomCmd();
    private static final String CMD_ERROR = CommonProperties.getCmdError();
    private static final String CMD_ERROR_SCRIPT = "; if [ $? != 0 ]; then echo " + CMD_ERROR + "; fi";

    private static final String UNIQ = "| uniq";
    private static final String UNIQ_REPLACE = "| uniq | egrep \".*\"";

    private static final String preferredAuthentications;

    static {
        if (CommonProperties.getUseBouncyCastleProvider()) {
            // BouncyCastleProvider의 Position을 재 설정하기 위해 이미 Provider로 지정되어 있다면 삭제 후 재 등록한다.
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
            Security.insertProviderAt(new BouncyCastleProvider(), 1);
        }

        preferredAuthentications = CommonProperties.getPreferredAuthentications();
    }

    private static ConcurrentMap<String, Session> sessionMap = ExpiringMap.builder()
            .maxSize(1000)
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expiration(10, TimeUnit.MINUTES)
            .expirationListener((ExpirationListener<String, Session>) (key, session) -> {
                if (session != null && session.isConnected()) {
                    log.debug("Jsch Session for [{}] will be disconnected by listener.", key);
                    session.disconnect();
                }
            })
            .build();

    /**
     * <pre>
     * 아래 조건을 모두 만족하는 경우에만 su를 실행할 수 있도록 한다.
     * 시스템 마다 또는 언어 설정마다 인증 실패 메시지가 다르기 때문에 SSHUtil.canExecuteCommandWithSu()를 실행하여
     * root 로 switch user를 할 수 있는지 확인을 먼저 진행한다.
     * <ul>
     *     <li>username이 root가 아닌 경우</li>
     *     <li>sudoer가 아닌 경우</li>
     *     <li>rootPassowrd가 존재하는 경우</li>
     * </ul>
     * </pre>
     */
    public static String executeCommandWithSu(TargetHost targetHost, String command) throws InterruptedException {
        Session session = null;
        ChannelShell channel = null;
        PipedOutputStream commandIO = null;
        InputStream sessionOutput = null;

        command = command.replaceAll("bash ", "sh ")
                .replaceAll("/usr/bin/sudo ", "")
                .replaceAll("sudo ", "");

        String cmdMessage = "[root@" + targetHost.getIpAddress() + " ~]$ " + command;

        String result = null;
        try {
            // create the IO streams to send input to remote session.
            commandIO = new PipedOutputStream();

            session = getSessionForTimeout(targetHost);
            channel = getSuChannel(targetHost, session, commandIO);

            // su 실행 권한 및 root password 불일치에 의한 channel 생성 실패
            if (channel == null) {
                return StringUtils.EMPTY;
            }

            // this will have the STDOUT from server
            sessionOutput = channel.getInputStream();

            String str = sendCommand(channel, commandIO, command, sessionOutput);

            RemoteExecResult remoteExecResult = getShellResult(str.trim(), null, command);

            log.debug("SSHUtil.executeCommandWithSu(\"{}\")'s Result : [{}]", cmdMessage, remoteExecResult);

            result = remoteExecResult.getResult();

            commandIO.write("exit\n".getBytes());
            commandIO.write("exit\n".getBytes());
            commandIO.flush();
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.warn("Unhandled exception occurred while execute command '{}' with '{}' error.", cmdMessage, e.getMessage());

            exceptionCheck(e, targetHost);
        } finally {
            IOUtils.closeQuietly(commandIO);
            IOUtils.closeQuietly(sessionOutput);

            close(targetHost, channel, session);
        }

        return result;
    }

    /**
     * <pre>
     * 아래 조건을 모두 만족하는 경우에만 su를 실행할 수 있도록 한다.
     * 시스템 마다 또는 언어 설정마다 인증 실패 메시지가 다르기 때문에 SSHUtil.canExecuteCommandWithSu()를 실행하여
     * root 로 switch user를 할 수 있는지 확인을 먼저 진행한다.
     * <ul>
     *     <li>username이 root가 아닌 경우</li>
     *     <li>sudoer가 아닌 경우</li>
     *     <li>rootPassowrd가 존재하는 경우</li>
     * </ul>
     * </pre>
     */
    public static <T> T executeCommandsWithSu(TargetHost targetHost, Object commands, Object results) throws InterruptedException {
        Session session = null;
        ChannelShell channel = null;
        PipedOutputStream commandIO = null;
        InputStream sessionOutput = null;

        String str;

        try {
            // create the IO streams to send input to remote session.
            commandIO = new PipedOutputStream();

            session = getSessionForTimeout(targetHost);
            channel = getSuChannel(targetHost, session, commandIO);

            // su 실행 권한 및 root password 불일치에 의한 channel 생성 실패
            if (channel == null) {
                return null;
            }

            // this will have the STDOUT from server
            sessionOutput = channel.getInputStream();

            Map<String, String> commandMap = new LinkedHashMap<>();
            if (commands instanceof List) {
                for (int idx = 0; idx < ((List) commands).size(); idx++) {
                    commandMap.put(Integer.toString(idx), (String) ((List) commands).get(idx));
                }
            } else if (commands instanceof Map) {
                commandMap = (Map<String, String>) commands;
            }

            for (String key : commandMap.keySet()) {
                String command = commandMap.get(key);

                command = command.replaceAll("bash ", "sh ")
                        .replaceAll("/usr/bin/sudo ", StringUtils.EMPTY)
                        .replaceAll("sudo ", StringUtils.EMPTY);

                String cmdMessage = "[root@" + targetHost.getIpAddress() + " ~]$ " + command;

                str = sendCommand(channel, commandIO, command, sessionOutput);

                RemoteExecResult remoteExecResult = getShellResult(str.trim(), key, command);

                log.debug("SSHUtil.executeCommandsWithSu(\"{}\")'s Result : [{}]", cmdMessage, remoteExecResult);

                if (results instanceof Map) {
                    ((Map) results).put(key, remoteExecResult);
                } else if (results instanceof List) {
                    ((List) results).add(remoteExecResult);
                }
            }

            commandIO.write("exit\n".getBytes());
            commandIO.write("exit\n".getBytes());
            commandIO.flush();
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.warn("Unhandled exception occurred while execute commands with '{}' error.", e.getMessage());

            exceptionCheck(e, targetHost);
        } finally {
            IOUtils.closeQuietly(commandIO);
            IOUtils.closeQuietly(sessionOutput);

            close(targetHost, channel, session);
        }

        return (T) results;
    }

    /**
     * <pre>
     * root로 switch user를 할 수 있는지 여부를 결정한다.
     * root 유저가 su -를 실행할 경우 비밀번호 입력이 필요치 않아 root 유저는 false가 리턴된다.
     * </pre>
     */
    public static Boolean canExecuteCommandWithSu(TargetHost targetHost) throws InterruptedException {
        if (StringUtils.isEmpty(targetHost.getUsername()) || "root".equals(targetHost.getUsername())) {
            return false;
        }

        if (StringUtils.isEmpty(targetHost.getRootPassword())) {
            return false;
        }

        Session session = null;
        ChannelShell channel = null;
        PipedOutputStream commandIO = null;
        InputStream sessionOutput = null;
        InputStream sessionError = null;

        StringBuilder str = new StringBuilder();

        try {
            // create the IO streams to send input to remote session.
            commandIO = new PipedOutputStream();

            session = getSessionForTimeout(targetHost);
            channel = getSuChannel(targetHost, session, commandIO);

            // su 실행 권한 및 root password 불일치에 의한 channel 생성 실패
            if (channel == null) {
                return false;
            }

            // this will have the STDOUT from server
            sessionOutput = channel.getInputStream();

            // this will have the STDERR from server
            sessionError = channel.getExtInputStream();

            commandIO.write(("echo " + START_WHOAMI_CMD + ";").getBytes());
            commandIO.write("whoami".getBytes());
            commandIO.write((";echo " + END_WHOAMI_CMD + "\n").getBytes());
            commandIO.flush();

            byte[] tmp = new byte[4096];
            int i, cnt = 0;
            while (true) {
                while (sessionOutput.available() > 0) {
                    i = sessionOutput.read(tmp, 0, tmp.length);

                    if (i < 0) {
                        break;
                    }

                    str.append(new String(tmp, 0, i));
                }

                if (channel.isClosed()) {
                    if (sessionOutput.available() > 0) {
                        continue;
                    }

                    break;
                }

                if (cnt++ >= 30) {
                    break;
                }

                Thread.sleep(100);
            }

            commandIO.write("exit\n".getBytes());
            commandIO.write("exit\n".getBytes());
            commandIO.flush();
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);

            exceptionCheck(e, targetHost);
        } finally {
            IOUtils.closeQuietly(commandIO);
            IOUtils.closeQuietly(sessionError);
            IOUtils.closeQuietly(sessionOutput);

            close(targetHost, channel, session);
        }

        String result = str.toString().trim();
        int startIdx, endIdx;

        startIdx = result.lastIndexOf(START_WHOAMI_CMD) + START_WHOAMI_CMD.length();
        endIdx = result.lastIndexOf(END_WHOAMI_CMD);

        String whoami = targetHost.getUsername();
        if (startIdx > -1 && endIdx > -1 && startIdx < endIdx) {
            whoami = result.substring(startIdx, endIdx).trim();
        }

        return "root".equals(whoami);
    }

    /**
     * <pre>
     * Health Check
     * </pre>
     */
    public static Boolean healthCheck(TargetHost targetHost) throws InterruptedException {
        boolean healthy = false;
        Session session = null;
        //JSch jsch = new JSch();

        try {
            session = getSessionForTimeout(targetHost);

            healthy = true;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.warn("[{}] Health check failed with '{}'.", targetHost.getIpAddress(), e.getMessage());
        } finally {
            close(targetHost, null, session);
        }

        return healthy;
    }

    /**
     * <pre>
     * Execute shell command in remote server via SSH
     * </pre>
     */
    public static String executeCommand(TargetHost targetHost, String command) throws RoRoException, InterruptedException {
        try {
            return executeCommand(targetHost, command, null);
        } catch (ConnectionErrorException e) {
            log.warn("ConnectionErrorException({}) occurred. Trying to retry.", e.getMessage());
            clearSession(targetHost);
            return executeCommand(targetHost, command, null);
        }
    }

    /**
     * <pre>
     * Execute shell command in remote server via SSH
     * </pre>
     */
    public static String executeCommand(TargetHost targetHost, String command, PipedOutputStream pos) throws RoRoException, InterruptedException {
        return executeCommand(targetHost, command, pos, true);
    }

    /**
     * <pre>
     * Execute shell command in remote server via SSH
     * </pre>
     */
    public static String executeCommand(TargetHost targetHost, String command, PipedOutputStream pos, boolean checkSudoer) throws InterruptedException {
        if (checkSudoer && canExecuteCommandWithSu(targetHost)) {
            return executeCommandWithSu(targetHost, command);
        }

        Session session = null;
        Channel channel = null;

        //JSch jsch = new JSch();
        StringBuilder str = new StringBuilder();

        BufferedWriter writer = null;

        if (pos != null) {
            writer = new BufferedWriter(new OutputStreamWriter(pos));
        }

        boolean isSudoer = true;
        if (checkSudoer && (command.startsWith("sudo ") || command.startsWith("/usr/bin/sudo "))) {
            isSudoer = isSudoer(targetHost);
        }

        if (targetHost.getUsername().equals("root") || !isSudoer) {
            if (command.startsWith("sudo ")) {
                command = command.substring(5);
            } else if (command.startsWith("/usr/bin/sudo ")) {
                command = command.substring(14);
            }
        }

        command = command.replaceAll("bash ", "sh ");

        String cmdMessage = "[" + targetHost.getUsername() + "@" + targetHost.getIpAddress() + " ~]$ " + command;

        // if (checkSudoer) {
        //     logger.debug(cmdMessage + "\n");
        // }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream errStream = new PrintStream(baos);

        try {
            session = getSessionForTimeout(targetHost);

            channel = session.openChannel("exec");

            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(errStream);

            if (CommonProperties.getUsePty()) {
                if (command.startsWith("sudo ") || command.startsWith("/usr/bin/sudo ")) {
                    ((ChannelExec) channel).setPty(true);
                }
            }

            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] tmp = new byte[4096];
            int cnt = 0;
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 4096);

                    if (i < 0) {
                        break;
                    }

                    String line = new String(tmp, 0, i);
                    if (writer != null) {
                        writer.write(line);
                        writer.flush();
                    }

                    str.append(new String(tmp, 0, i));
                }

                if (channel.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }

                    if (checkSudoer) {
                        if (str.toString().length() > 0) {
                            log.debug("SSHUtil.executeCommand(\"{}\")'s Exit Status: [{}], Result : [{}]", cmdMessage, channel.getExitStatus(), str.toString().trim());

                            // exit code가 0이 아닌 경우 응답을 ""로 리턴, 일부 Command는 exit code가 0이 아님에도 결과를 처리해야 하는 경우도 있음. (java -version 등)
                            // if (channel.getExitStatus() != 0) {
                            //     return "";
                            // }
                        } else {
                            if (baos.size() > 0) {
                                log.debug("SSHUtil.executeCommand(\"{}\")'s Exit Status: [{}], Result : [{}]", cmdMessage, channel.getExitStatus(), baos.toString().trim());
                            } else {
                                log.debug("SSHUtil.executeCommand(\"{}\")'s Exit Status: [{}]", cmdMessage, channel.getExitStatus());
                            }
                        }
                    }
                    break;
                }

                if (!checkSudoer && cnt++ >= 20) {
                    break;
                }

                Thread.sleep(500);
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.warn("Unhandled exception occurred while execute command '{}' with '{}' error.", cmdMessage, e.getMessage());

            exceptionCheck(e, targetHost);
        } finally {
            close(targetHost, channel, session);

            if (writer != null) {
                IOUtils.closeQuietly(writer);
            }
        }

        return checkResult(str.toString().trim());
    }

    /**
     * <pre>
     * Put local file to remote server
     * </pre>
     */
    public static void putFile(TargetHost targetHost, File source, String target) throws RoRoException, InterruptedException {
        Session session = null;
        Channel channel = null;

        try {
            log.debug("[{}] will be saved to [{}]", source.getAbsolutePath(),
                    targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":" + target);

            //JSch jsch = new JSch();
            session = getSession(targetHost, 20 * 1000);

            channel = session.openChannel("sftp");
            channel.connect();

            ((ChannelSftp) channel).put(source.getAbsolutePath(), target);

            log.debug("[{}] transfer completed.", target);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("Unhandled exception occurred during handle().", e);

            exceptionCheck(e, targetHost);
        } finally {
            closeExplicit(channel, session);
        }
    }

    /**
     * <pre>
     * Get file from remote server to local
     * </pre>
     */
    public static void getFile(TargetHost targetHost, String source, String target) throws RoRoException, InterruptedException {
        Session session = null;
        Channel channel = null;

        try {
            log.debug("[{}] will be saved to [{}]",
                    targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":" + source,
                    target);

            //JSch jsch = new JSch();
            session = getSession(targetHost, 20 * 1000);

            channel = session.openChannel("sftp");
            channel.connect();

            FileUtils.forceMkdirParent(new File(target));
            ((ChannelSftp) channel).get(source, target);

            log.debug("[{}] transfer completed.", target);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("Unhandled exception occurred during handle().", e);

            exceptionCheck(e, targetHost);
        } finally {
            closeExplicit(channel, session);
        }
    }

    /**
     * Get dir.
     *
     * @param targetHost the target host
     * @param source     the source
     * @param target     the target
     * @throws RoRoException the ro ro exception
     */
    public static void getDir(TargetHost targetHost, String source, String target) throws RoRoException, InterruptedException {
        Session session = null;
        Channel channel = null;

        try {
            target = FilenameUtils.separatorsToSystem(target);

            log.debug("[{}] will be saved to [{}]",
                    targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":" + source,
                    target);

            //JSch jsch = new JSch();
            session = getSession(targetHost, 20 * 1000);

            channel = session.openChannel("sftp");
            channel.connect();

            File targetFile = new File(target);
            if (!((ChannelSftp) channel).lstat(source).isDir() && !target.endsWith("/")) {
                targetFile = targetFile.getParentFile();
            }

            FileUtils.forceMkdir(targetFile);
            ((ChannelSftp) channel).lcd(targetFile.getAbsolutePath());
            getDir((ChannelSftp) channel, source, target);

            log.debug("Transfer completed to [{}].", target);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("Unhandled exception occurred during getDir().", e);

            exceptionCheck(e, targetHost);
        } finally {
            closeExplicit(channel, session);
        }
    }

    /**
     * Put dir.
     *
     * @param targetHost the target host
     * @param source     the source
     * @param target     the target
     * @throws RoRoException the ro ro exception
     */
    public static void putDir(TargetHost targetHost, String source, String target) throws RoRoException, InterruptedException {
        Session session = null;
        Channel channel = null;

        try {
            source = FilenameUtils.separatorsToSystem(source);

            log.debug("[{}] will be saved to [{}]",
                    source,
                    targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":" + target);

            //JSch jsch = new JSch();
            session = getSession(targetHost, 20 * 1000);

            channel = session.openChannel("sftp");
            channel.connect();
            ((ChannelSftp) channel).setFilenameEncoding("UTF-8");

            putDir((ChannelSftp) channel, source, target);

            log.debug("Transfer completed to [{}].", target);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("Unhandled exception occurred during putDir().", e);

            exceptionCheck(e, targetHost);
        } finally {
            closeExplicit(channel, session);
        }
    }

    /**
     * Is sudoer boolean.
     *
     * @param targetHost the target host
     * @return the boolean
     */
    public static boolean isSudoer(TargetHost targetHost) throws InterruptedException {
        boolean isSudoer = false;

        if ("root".equals(targetHost.getUsername())) {
            isSudoer = true;
        } else {
            // non interactive를 위해 -n 옵션을 사용하던 중 오래된 sudo 명령어에는 -n 옵션이 존재하지 않아 -S로 대체
            String result = executeCommand(targetHost, "echo \"FAKE_PASSWORD\" | sudo -S echo " + targetHost.getUsername(), null, false);
            if (result.trim().endsWith(targetHost.getUsername())) {
                isSudoer = true;
            }
        }

        return isSudoer;
    }

    /**
     * Get dir.
     *
     * @param sftpChannel the sftp channel
     * @param sourcePath  the source path
     * @param destPath    the dest path
     * @throws SftpException the sftp exception
     * @throws IOException   the io exception
     */
    private static void getDir(ChannelSftp sftpChannel, String sourcePath, String destPath) throws SftpException, IOException {
        Vector<ChannelSftp.LsEntry> list = sftpChannel.ls(sourcePath);

        File destFile = null;
        Set<PosixFilePermission> perms = null;
        if (list.size() == 1 && !list.get(0).getAttrs().isDir()) {
            if (destPath.endsWith("/")) {
                destFile = new File(destPath, list.get(0).getFilename());
            } else {
                destFile = new File(destPath);
            }

            sftpChannel.get(sourcePath, destPath);

            // Set file permissions
            perms = PosixFilePermissions.fromString(list.get(0).getAttrs().getPermissionsString().substring(1));
            Files.setPosixFilePermissions(destFile.toPath(), perms);
        } else {
            for (ChannelSftp.LsEntry oListItem : list) {
                destFile = new File(destPath, oListItem.getFilename());

                if (!oListItem.getAttrs().isDir()) {
                    sftpChannel.get(sourcePath + File.separator + oListItem.getFilename(), destFile.getAbsolutePath());
                } else if (!".".equals(oListItem.getFilename()) && !"..".equals(oListItem.getFilename())) {
                    FileUtils.forceMkdir(destFile);
                    getDir(sftpChannel, sourcePath + File.separator + oListItem.getFilename(), destFile.getAbsolutePath());
                }

                // Set file permissions
                perms = PosixFilePermissions.fromString(oListItem.getAttrs().getPermissionsString().substring(1));
                Files.setPosixFilePermissions(destFile.toPath(), perms);
            }
        }
    }

    /**
     * Put dir.
     *
     * @param sftpChannel the sftp channel
     * @param sourcePath  the source path
     * @param destPath    the dest path
     * @throws SftpException the sftp exception
     * @throws IOException   the io exception
     */
    private static void putDir(ChannelSftp sftpChannel, String sourcePath, String destPath) throws SftpException, IOException, InterruptedException {
        File sourceFile = new File(sourcePath);
        if (sourceFile.isFile()) {
            sftpChannel.cd(destPath);
            if (!sourceFile.getName().startsWith(".")) {
                sftpChannel.put(new FileInputStream(sourceFile), sourceFile.getName(), ChannelSftp.OVERWRITE);
            }
        } else {
            File[] files = sourceFile.listFiles();
            if (files != null && !sourceFile.getName().startsWith(".")) {
                sftpChannel.cd(destPath);
                SftpATTRS attrs = null;

                try {
                    // check if the directory is already existing
                    attrs = sftpChannel.stat(destPath + "/" + sourceFile.getName());
                } catch (Exception e) {
                    // ignore
                    RoRoException.checkInterruptedException(e);
                }

                if (attrs == null) {
                    // create a directory
                    sftpChannel.mkdir(sourceFile.getName());
                }

                for (File f : files) {
                    putDir(sftpChannel, f.getAbsolutePath(), destPath + "/" + sourceFile.getName());
                }
            }
        }
    }

    /**
     * Gets session.
     *
     * @param targetHost the target host
     * @return the session
     * @throws Exception the exception
     */
    public static Session getSession(TargetHost targetHost, int connectTimeout) throws Exception {
        JSch jsch = getjSch(targetHost);

        Session session = jsch.getSession(targetHost.getUsername(), targetHost.getIpAddress(), targetHost.getPort());
        session.setPassword(targetHost.getPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(connectTimeout);

        return session;
    }

    @NotNull
    private static JSch getjSch(TargetHost targetHost) throws IOException, JSchException {
        JSch jsch = new JSch();

        if (StringUtils.isNotEmpty(targetHost.getKeyFilePath()) || StringUtils.isNotEmpty(targetHost.getKeyString())) {
            File keyFile = null;
            if (StringUtils.isNotEmpty(targetHost.getKeyFilePath())) {
                keyFile = new File(targetHost.getKeyFilePath());
            }

            if (keyFile == null || !keyFile.exists()) {
                if (targetHost.getKeyString() != null && !"".equals(targetHost.getKeyString())) {
                    File tempFile = File.createTempFile(targetHost.getIpAddress() + "-", ".pem");
                    IOUtils.write(targetHost.getKeyString(), new FileOutputStream(tempFile), "UTF-8");

                    targetHost.setKeyFilePath(tempFile.getAbsolutePath());
                }
            }

            jsch.addIdentity(targetHost.getKeyFilePath());
        }
        return jsch;
    }

    /**
     * Gets session for timeout.
     *
     * @param targetHost the target host
     * @return the session
     * @throws Exception the exception
     */
    public static Session getSessionForTimeout(TargetHost targetHost) throws IOException, JSchException {
        Session session = getSessionWithoutConnect(targetHost);
        if (!session.isConnected()) {
            session.connect(CommonProperties.getTimeout() * 1000);
        }

        return session;
    }

    private static Session getSessionWithoutConnect(TargetHost targetHost) throws IOException, JSchException {
        Session session = sessionMap.get(getKey(targetHost));

        if (session == null || !session.isConnected()) {
            JSch jsch = getjSch(targetHost);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("HashKnownHosts", "yes");
            config.put("PreferredAuthentications", preferredAuthentications);
            // https://serverfault.com/questions/515604/ssh-client-option-to-suppress-server-banners
            //config.put("LogLevel", "error");

            session = jsch.getSession(targetHost.getUsername(), targetHost.getIpAddress(), targetHost.getPort());
            session.setPassword(targetHost.getPassword());
            //session.setConfig("StrictHostKeyChecking", "no");
            // https://stackoverflow.com/questions/42451456/jschexception-timeout-socket-is-not-established
            session.setConfig(config);

            log.debug("Jsch Session for [{}] has been created.", getKey(targetHost));

            sessionMap.put(getKey(targetHost), session);

            return session;
        } else {
            return session;
        }
    }

    public static void clearSession(TargetHost targetHost) {
        clearSession(getKey(targetHost));
    }


    public static void clearSession(String key) {
        sessionMap.remove(key);
    }

    private static ChannelShell getSuChannel(TargetHost targetHost, Session session, PipedOutputStream commandIO) throws Exception {
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setPty(true);
        // 기본값 : 80, 24, 640, 480
        channel.setPtySize(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

        InputStream sessionInput, sessionError, sessionOutput;

        sessionInput = new PipedInputStream(commandIO);
        // this set's the InputStream the remote server will read from.
        channel.setInputStream(sessionInput);

        // this will have the STDERR from server
        sessionError = channel.getExtInputStream();

        // this will have the STDOUT from server
        sessionOutput = channel.getInputStream();

        channel.connect();

        // 2022.12.08 도로공사에서 일부 시스템에 SSH 접속 시 "-sh -l: ulimit: open files: cannot modify limit: Operation not permitted" 문구가
        // 표시되면서 su - 명령이 정상적이지 않은 현상 발생
        // 의미 없는 명령을 전송하여 해당 오류 문구를 무시하도록 처리한다.
        commandIO.write("whoami".getBytes());
        commandIO.write("\n".getBytes());
        commandIO.flush();

        byte[] tmp = new byte[4096];
        StringBuilder stdOut = new StringBuilder();
        StringBuilder stdErr = new StringBuilder();
        String result = null;
        int i, cnt = 0;
        while (true) {
            if (sessionError.available() > 0) {
                i = sessionError.read(tmp, 0, tmp.length);
                if (i < 0) {
                    log.warn("input stream closed earlier than expected");
                    break;
                }

                stdErr.append(new String(tmp, 0, i));
            }

            if (sessionOutput.available() > 0) {
                i = sessionOutput.read(tmp, 0, tmp.length);
                if (i < 0) {
                    log.warn("input stream closed earlier than expected");
                    break;
                }

                stdOut.append(new String(tmp, 0, i));
            }

            result = removeInvisibleCharacters(stdOut.toString());

            boolean isMatch = result.lines().collect(Collectors.toList()).stream().anyMatch(l -> l.startsWith(targetHost.getUsername()));
            if (isMatch || cnt++ >= 50) {
                break;
            }

            Thread.sleep(100);
        }

        // 언어 설정에 따라 root 비밀번호 입력 프롬프트가 Password: 가 아닌 암호: 와 같은 형태일 수 있음.
        commandIO.write("LANG=C;export LANG;su -".getBytes());
        commandIO.write(CMD_ERROR_SCRIPT.getBytes());
        commandIO.write("\n".getBytes());
        commandIO.flush();

        tmp = new byte[4096];
        stdOut = new StringBuilder();
        stdErr = new StringBuilder();
        result = null;
        cnt = 0;
        while (true) {
            if (sessionError.available() > 0) {
                i = sessionError.read(tmp, 0, tmp.length);
                if (i < 0) {
                    log.warn("input stream closed earlier than expected");
                    break;
                }

                stdErr.append(new String(tmp, 0, i));
            }

            if (sessionOutput.available() > 0) {
                i = sessionOutput.read(tmp, 0, tmp.length);
                if (i < 0) {
                    log.warn("input stream closed earlier than expected");
                    break;
                }

                stdOut.append(new String(tmp, 0, i));
            }

            result = removeInvisibleCharacters(stdOut.toString());
            if (result.contains("assword") || result.contains("암호") || cnt++ >= 100) {
                break;
            }

            Thread.sleep(100);
        }

        //*
        // 2022.12.08 도로공사에서 su - 명령 실행시 passowrd: 프롬프트 없이 패스워드를 입력하도록 되어 있음.
        // 패스워드 프롬프트가 있거나 CMD_ERROR에 대한 결과 메시지가 없는 경우 패스워드를 입력한다.
        boolean hasError = result.lines().collect(Collectors.toList()).stream().anyMatch(l -> l.startsWith(CMD_ERROR));
        if (result.contains("assword") || result.contains("암호") || !hasError) {
            commandIO.write(targetHost.getRootPassword().getBytes());
            commandIO.write("\n".getBytes());
            commandIO.flush();
        } else {
            log.warn("su(switch user) failed. Please check '{}@{}' has permission to execute su command and below information.", targetHost.getUsername(), targetHost.getIpAddress());
            log.warn("Command : [LANG=C;export LANG;su -{}]", CMD_ERROR_SCRIPT);
            log.warn("Sleep Count : [{}]", cnt);
            log.warn("Standard Error : [{}]", stdErr);
            log.warn("Standard Output : [{}]", stdOut);
            log.warn("Standard Output with removeInvisibleCharacters : [{}]", result);
            return null;
        }
        /*/
        if (!result.contains("assword") && !result.contains("암호")) {
            log.warn("su(switch user) failed. Please check '{}@{}' has permission to execute su command and below information.", targetHost.getUsername(), targetHost.getIpAddress());
            log.warn("Command : [LANG=C;export LANG;su -{}]", CMD_ERROR_SCRIPT);
            log.warn("Sleep Count : [{}]", cnt);
            log.warn("Standard Error : [{}]", stdErr);
            log.warn("Standard Output : [{}]", stdOut);
            log.warn("Standard Output with removeInvisibleCharacters : [{}]", result);
            return null;
        }

        commandIO.write(targetHost.getRootPassword().getBytes());
        commandIO.write("\n".getBytes());
        commandIO.flush();
        //*/

        StringBuilder str = new StringBuilder();
        cnt = 0;
        while (true) {
            while (sessionOutput.available() > 0) {
                i = sessionOutput.read(tmp, 0, tmp.length);

                if (i < 0) {
                    break;
                }

                str.append(new String(tmp, 0, i));
            }

            if (channel.isClosed()) {
                if (sessionOutput.available() > 0) {
                    continue;
                }

                break;
            }

            // su - 에 대한 output는 사이즈가 크지 않고 한번에 출력된다. 따라서 5초를 대기하지 않고 적당한 크기(5)를 넘어선 경우 loop 를 종료한다.
            if (cnt++ >= 50 || str.toString().length() > 5) {
                break;
            }

            Thread.sleep(100);
        }

        if (str.toString().contains(CMD_ERROR)) {
            log.warn("su(switch user) authentication failed. Please check root password is correct and below information.");
            log.warn("Standard Output : [{}]", str);
            return null;
        }

        return channel;
    }

    private static String sendCommand(ChannelShell channel, PipedOutputStream commandIO, String command, InputStream sessionOutput) throws Exception {
        StringBuilder sb = new StringBuilder();

        // "entstat -d lo0 | egrep Hardware | uniq" 와 같이 uniq가 뒤에 들어가면 exit code가 0으로 넘어온다.
        // "entstat -d lo0 | egrep Hardware | uniq | egrep '.*'" 와 같은 형태는 정상적으로 exit code가 반환되므로 치환한다.
        // Solaris에는 grep에 -E 옵션이 없음. egrep은 존재
        command = command.replace(UNIQ, UNIQ_REPLACE);

        sb.append("echo " + START_CMD + ";")
                .append("\\\n")
                .append(command)
                .append("\\\n")
                .append(CMD_ERROR_SCRIPT)
                .append("\\\n")
                .append(";echo " + END_CMD);

        commandIO.write(sb.toString().getBytes());
        commandIO.write("\n".getBytes());
        commandIO.flush();

        StringBuilder str = new StringBuilder();

        byte[] tmp = new byte[4096];
        int i, cnt = 0;
        String result = null;
        while (true) {
            while (sessionOutput.available() > 0) {
                i = sessionOutput.read(tmp, 0, tmp.length);

                if (i < 0) {
                    break;
                }

                str.append(new String(tmp, 0, i));
                result = removeInvisibleCharacters(str.toString());
            }

            if (channel.isClosed()) {
                if (sessionOutput.available() > 0) {
                    continue;
                }

                break;
            }

            // 일부 오래 걸리는 명령이 존재하며, 최대 30초 대기한다.
            if (cnt++ >= 300) {
                log.warn("Command({}) execute failed with timeout.", command);

                log.warn("Sleep Count : [{}]", cnt);
                log.warn("Standard Output : [{}]", str);
                log.warn("Standard Output with removeInvisibleCharacters : [{}]", result);

                return CMD_ERROR + "Command(" + command + ") execute failed with timeout.";
            }

            // 간헐적으로 command가 한번 더 출력되는 경우가 있어 StringUtils.countMatches(str, END_CMD) > 1 조건만 있는 경우 정상 응답을 받지 못하게 된다.
            if (StringUtils.countMatches(result, START_CMD) == StringUtils.countMatches(result, END_CMD) && StringUtils.countMatches(result, END_CMD) > 1) {
                break;
            }

            // 간헐적으로 결과내의 START_CMD, END_CMD 값이 깨지는 경우가 있음.
            // 마지막 START_CMD 뒤의 문자열에 END_CMD가 있고 CMD_ERROR가 없으면 정상 응답으로 처리
            if (StringUtils.isNotEmpty(result) && result.length() > START_CMD.length()) {
                int idx = result.lastIndexOf(START_CMD) + START_CMD.length();
                if (result.substring(idx).contains(END_CMD) && !result.substring(idx).contains(CMD_ERROR)) {
                    return result;
                }
            }

            Thread.sleep(100);
        }

        // 비 정상적인 메시지의 경우 parsing 오류를 방지하기 위해 값을 비운다. (Exit Code는 정상이지만 END_CMD가 2개가 아님)
        if (StringUtils.countMatches(result, END_CMD) < 2 && StringUtils.countMatches(result, CMD_ERROR) < 2) {
            result = StringUtils.EMPTY;
        }

        return result;
    }

    private static RemoteExecResult getShellResult(String result, String key, String command) {
        String originResult = result;

        int startIdx, endIdx;

        startIdx = result.lastIndexOf(START_CMD) + START_CMD.length();
        endIdx = result.lastIndexOf(END_CMD);

        if (startIdx > -1 && endIdx > -1 && startIdx < endIdx) {
            result = result.substring(startIdx, endIdx).trim();
        }

        if (result.equals((";" + command + ";echo")) || result.equals((";echo " + END_CMD))) {
            result = "";
        }

        // remove invisible characters
        result = removeInvisibleCharacters(result);

        boolean hasError = false;
        String errorMessage = null;
        if (result.contains(CMD_ERROR)) {
            if (command.startsWith("/usr/sbin/rmsock")) {
                result = result.replaceAll(CMD_ERROR, StringUtils.EMPTY).trim();
            } else {
                hasError = true;
                errorMessage = result.replaceAll(CMD_ERROR, StringUtils.EMPTY).trim();
                log.debug("SSHUtil.executeCommand(s)WithSu(\"{}\") has error. Full Message : [{}]", command, originResult.replaceAll(CMD_ERROR + "Command", "Command"));
                // log.debug("SSHUtil.executeCommand(s)WithSu(\"{}\") has error. Message : [{}]", command, errorMessage);
                result = StringUtils.EMPTY;
            }
        } else {
            result = checkResult(result);
        }

        RemoteExecResult remoteExecResult = RemoteExecResult.builder()
                .command(command)
                .key(key)
                .err(hasError)
                .result(result)
                .error(errorMessage)
                .build();

        return remoteExecResult;
    }

    public static void close(TargetHost targetHost, Channel channel, Session session) {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }

        // session의 종료는 sessionMap의 expiringListener에 의해서 자동 종료됨.
        if (sessionMap.get(getKey(targetHost)) == null) {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    public static void closeExplicit(Channel channel, Session session) {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }

        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    private static String getKey(TargetHost targetHost) {
        return targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":" + targetHost.getPort();
    }

    private static String removeInvisibleCharacters(String str) {
        /*
         * 리눅스 시스템에서 grep은 일반적으로 Alias로 등록되어 있음. (`grep --color=auto')
         * 따라서 명령어에 grep이 포함된 경우 결과에 GREP_COLORS에 등록된 값으로 ANSI Color code가 포함됨.
         * 이를 해결하기 위해 다음 방법을 사용할 수 있음.
         *  1. grep 뒤에 --color=never를 지정한다.
         *     - 명령을 한곳에서 치환하거나, 기존 로직을 찾아서 수정해야 함. 치환 과정에서 Side effect 발생 가능성이 있음.
         *     - UNIX 시스템에는 --color 옵션이 없어 명령 실행 시 오류가 발생
         *  2. grep은 일반적으로 /usr/bin 디렉토리에 위치하고 있으며 명령어에 grep이 포함된 경우 /usr/bin/grep으로 치환한다.
         *     - 모든 시스템의 grep 명령이 /usr/bin 디렉토리에 위치한다고 단정을 할 수 없으며, 치환 과정에서 Side effect 발생 가능성이 있음.
         *  3. 명령 수행 결과에 포함된 ANSI Color 코드가 존재할 경우 제거
         *     - https://howtodoinjava.com/java/regex/java-clean-ascii-text-non-printable-chars/
         *     - https://stackoverflow.com/questions/17998978/removing-colors-from-output
         */

        // remove invisible characters
        // strips off all non-ASCII characters
        // ascii 코드가 아닌 값이 치환되면 한글이 표현되지 않는다.
        // str = str.replaceAll("[^\\x00-\\x7F]", "");

        // erases all the ASCII control characters (include Carriage Return(\r))
        str = str.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "");

        // removes non-printable characters from Unicode
        // str = str.replaceAll("\\p{C}", "");

        // grep에 의해 ansi color가 빨간색으로 지정된 내용을 제거한다.
        // "cat /etc/*-release 2>/dev/null | uniq | egrep --color=never '^ID='" 와 같이 --color=never를 지정해야 함. (Unix에는 해당 옵션이 없음)
        // str = str.replaceAll("\\[01;31m\\[K", "").replaceAll("\\[m\\[K", "");
        str = str.replaceAll("\\[([0-9]{1,3}(;[0-9]{1,2})?)?[mGK]", "");

        return str;
    }

    private static void exceptionCheck(Exception e, TargetHost targetHost) {
        if (e instanceof JSchException) {
            clearSession(targetHost);
            throw new ConnectionErrorException(e.getMessage());
        }
        if (e.getCause() != null && e.getCause() instanceof JSchException) {
            clearSession(targetHost);
            throw new ConnectionErrorException(e.getCause().getMessage());
        } else {
            throw new RoRoException(e.getMessage(), e);
        }
    }

    public static String checkResult(String result) {
        // Command 실행 결과의 첫 번째 라인에 [YOU HAVE NEW MAIL]과 같이 파싱하면 안되는 문구를 처리하기 위함
        List<String> skipMessages = CommonProperties.getSkipMessages();

        if (skipMessages != null && skipMessages.size() > 0 && StringUtils.isNotEmpty(result)) {
            boolean isContains = true;
            boolean needToStop = false;

            while (isContains) {
                if (needToStop) {
                    break;
                }

                List<String> resultList = result.lines().limit(1).collect(Collectors.toList());

                if (resultList.size() > 0) {
                    // Command 실행 결과의 첫 번째 라인
                    String firstLine = resultList.get(0);

                    // 파싱하면 안되는 문구가 포함되어 있는지 처리
                    for (String skipMessage : skipMessages) {
                        isContains = true;

                        if (StringUtils.isEmpty(skipMessage)) {
                            isContains = false;
                            break;
                        }

                        for (String s : skipMessage.split(",")) {
                            if (!firstLine.toLowerCase().contains(s.toLowerCase())) {
                                isContains = false;
                                break;
                            }
                        }

                        if (isContains) {
                            int idx = result.indexOf(System.lineSeparator());

                            if (idx < 0) {
                                needToStop = true;
                            } else {
                                // 두 분째 라인부터 결과로 처리
                                result = result.substring(idx + 1);
                            }

                            break;
                        }
                    }
                } else {
                    isContains = false;
                }
            }
        }

        return result;
    }
}
// end of SSHUtil.java