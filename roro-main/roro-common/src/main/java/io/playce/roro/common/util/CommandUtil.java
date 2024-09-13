/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * SangCheon Park   Nov 12, 2021		    First Draft.
 */
package io.playce.roro.common.util;

import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
public class CommandUtil {

    private static Map<String, String> environment;
    private static String username;
    private static boolean isSudoer;

    static {
        try {
            setEnvironment();
        } catch (IOException e) {
            log.error("Unhandled exception occurred while initialize CommandUtil.", e);
        }
        try {
            checkUsername();
        } catch (Exception e) {
            log.error("Unhandled exception occurred while initialize CommandUtil.", e);
        }
    }

    /**
     * Set path.
     *
     * @throws IOException
     */
    private static void setEnvironment() throws IOException {
        environment = EnvironmentUtils.getProcEnvironment();
        String path = environment.get("PATH");

        path = "/usr/local/bin:" + path;
        environment.put("PATH", path);
    }

    /**
     * Is sudoer boolean.
     *
     * @return the boolean
     */
    private static void checkUsername() throws InterruptedException {
        username = System.getProperty("user.name");

        if ("root".equals(username)) {
            isSudoer = true;
        } else {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                DefaultExecutor executor = new DefaultExecutor();
                PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
                executor.setStreamHandler(streamHandler);

                int exitCode = executor.execute(new CommandLine(findCommand("sudo")).addArguments(new String[]{"-n", "echo", username}));

                if (exitCode == 0) {
                    String result = baos.toString().replaceAll("\\n", "").replaceAll("\\r", "");

                    if (result.trim().equals(username)) {
                        isSudoer = true;
                    }
                }
            } catch (Exception e) {
                RoRoException.checkInterruptedException(e);
                log.warn("Unable to check [{}] is sudoer or not. ({})", username, e.getMessage());
            }
        }

        log.info("RoRo Runtime Information - Username : [{}], Is Sudoer : [{}]", username, isSudoer);
    }

    /**
     * Find command string.
     *
     * @param command the command
     *
     * @return the string
     */
    public static String findCommand(String command) throws InterruptedException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            DefaultExecutor executor = new DefaultExecutor();
            PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            int exitCode = executor.execute(new CommandLine("which").addArgument(command), environment);

            if (exitCode == 0) {
                return baos.toString().replaceAll("\\n", "").replaceAll("\\r", "");
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.warn("[{}] command cannot be found using /usr/bin/which. Command will be used without path.", command);
        }

        return command;
    }

    /**
     * @return
     */
    public static String findPython() throws InterruptedException {
        String version = "python";
        String cmd = findCommand(version);
        if (!version.equals(cmd)) {
            return cmd;
        }

        version = "python2";
        cmd = findCommand(version);
        if (!version.equals(cmd)) {
            return cmd;
        }

        version = "python3";
        cmd = findCommand(version);
        if (!version.equals(cmd)) {
            return cmd;
        }

        return "python";
    }

    public static CommandLine getCommandLine(String... commands) {
        CommandLine cl = null;

        //*
        if (commands[0].endsWith("sudo")) {
            if (!"root".equals(username) && isSudoer) {
                cl = new CommandLine(commands[0]);
            }
        } else {
            cl = new CommandLine(commands[0]);
        }

        for (int i = 1; i < commands.length; i++) {
            if (cl == null) {
                cl = new CommandLine(commands[i]);
            } else {
                cl = cl.addArguments(commands[i]);
            }
        }
        /*/
        if (commands[0].endsWith("sudo")) {
            if (!"root".equals(usernmae) && isSudoer) {
                cl = new CommandLine(CollectionHelper.findCommand("sudo"));
                cl.addArguments(CollectionHelper.findCommand("sh"));
            } else {
                cl = new CommandLine(CollectionHelper.findCommand("sh"));
            }
        } else {
            cl = new CommandLine(CollectionHelper.findCommand("sh"));
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commands.length; i++) {
            if (i == 0) {
                if (!commands[0].endsWith("sudo")) {
                    sb.append(commands[i]);
                }
            } else {
                sb.append(" ").append(commands[i]);
            }
        }

        cl.addArguments(new String[]{"-c", sb.toString()}, false);
        //*/

        return cl;
    }

    /**
     * @param commands
     *
     * @return
     *
     * @throws Exception
     */
    public static String executeCommand(String... commands) throws Exception {
        CommandLine cl = CommandUtil.getCommandLine(commands);
        return executeCommand(cl);
    }

    /**
     * @param cl
     *
     * @return
     *
     * @throws Exception
     */
    public static String executeCommand(CommandLine cl) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            DefaultExecutor executor = new DefaultExecutor();
            PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            log.debug("executeCommand()'s CommandLine : {}", cl);

            int exitCode = executor.execute(cl);

            if (exitCode != 0) {
                throw new Exception(baos.toString());
            }

            String result = baos.toString().trim();
            log.debug("executeCommand()'s result : {}", result);

            return result;
        }
    }

    public static String getFileContentCommand(String filePath) {
        return "cat " + surroundDoubleQuotation(filePath);
    }

    public static String surroundDoubleQuotation(String command) {
        return "\"" + command + "\"";
    }

    public static String getSshCommandResultTrim(TargetHost targetHost, String executeCommand) throws InterruptedException {
        return getSshCommandResultTrim(targetHost, executeCommand, true);
    }

    public static String getSshCommandResultTrim(TargetHost targetHost, String executeCommand, boolean isSudo) throws InterruptedException {
        String sudoExecuteCommand = isSudo ? "sudo " + executeCommand : executeCommand;

        return StringUtils.defaultString(SSHUtil.executeCommand(targetHost, sudoExecuteCommand).trim());
    }
}
//end of CommandUtil.java