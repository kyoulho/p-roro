/*
 * Copyright 2017 The Athena-RoRo Project.
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
 * Sang-cheon Park	2017. 4. 4.		First Draft.
 */
package io.playce.roro.common.util;

import io.playce.roro.common.exception.RoRoException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
@Component
public class CollectionHelper {

    private static final Logger logger = LoggerFactory.getLogger(CollectionHelper.class);

    private static Map<String, String> environment;

    //for assessment
    private static File systemInfoCollectorFile;
    private static File cancelAssessmentFile;
    private static File jdeprscanFile;
    private static File jdepsFile;

    // for subscription
    private static File signatureFile;

    // for monitoring
    private static File linuxMonitoringFile;
    private static File aixMonitoringFile;
    private static File windowsMonitoringFile;

    private static String usernmae;
    private static boolean isSudoer;

    public CollectionHelper() throws InterruptedException {
        try {
            setEnvironment();
            copyResources();
            //pipInstall(findCommand("easy_install"));
            //moduleInstall(findCommand("pip"));
            checkUsername();
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            logger.error("Unhandled exception occurred while initialize collectionComponent bean.", e);
        }
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
            logger.warn("[{}] command cannot be found using /usr/bin/which. Command will be used without path.", command);
        }

        return command;
    }

    public static CommandLine getCommandLine(String... commands) {
        CommandLine cl = null;

        //*
        if (commands[0].endsWith("sudo")) {
            if (!"root".equals(usernmae) && isSudoer) {
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

        logger.debug("[RoRo] Execute local command : {}", cl.toString());

        return cl;
    }

    public static File getSystemInfoCollectorFile() {
        return systemInfoCollectorFile;
    }

    public static File getCancelAssessmentFile() {
        return cancelAssessmentFile;
    }

    public static File getSignatureFile() {
        return signatureFile;
    }

    public static File getJdeprscanFile() {
        return jdeprscanFile;
    }

    public static File getJdepsFile() {
        return jdepsFile;
    }

    public static File getLinuxMonitoringFile() {
        return linuxMonitoringFile;
    }

    public static File getAixMonitoringFile() {
        return aixMonitoringFile;
    }

    public static File getWindowsMonitoringFile() {
        return windowsMonitoringFile;
    }

    public static boolean isSudoer() {
        return isSudoer;
    }

    /**
     * Is sudoer boolean.
     *
     * @return the boolean
     */
    private void checkUsername() throws InterruptedException {
        usernmae = System.getProperty("user.name");

        if ("root".equals(usernmae)) {
            isSudoer = true;
        } else {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                DefaultExecutor executor = new DefaultExecutor();
                PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
                executor.setStreamHandler(streamHandler);

                int exitCode = executor.execute(new CommandLine(findCommand("sudo")).addArguments(new String[]{"-n", "echo", usernmae}));

                if (exitCode == 0) {
                    String result = baos.toString().replaceAll("\\n", "").replaceAll("\\r", "");

                    if (result.trim().equals(usernmae)) {
                        isSudoer = true;
                    }
                }
            } catch (Exception e) {
                RoRoException.checkInterruptedException(e);
                logger.warn("Unable to check [{}] is sudoer or not. ({})", usernmae, e.getMessage());
            }
        }

        logger.info("RoRo Runtime Information - Username : [{}], Is Sudoer : [{}]", usernmae, isSudoer);
    }

    private void setEnvironment() throws IOException {
        environment = EnvironmentUtils.getProcEnvironment();
        String path = environment.get("PATH");

        path = "/usr/local/bin:" + path;
        environment.put("PATH", path);
    }

    private void copyResources() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        ClassLoader classLoader = getClass().getClassLoader();
//        systemInfoCollectorFile = new File(tmpDir, "scripts/system_info_collector");
//        cancelAssessmentFile = new File(tmpDir, "scripts/cancel_assessment.sh");
//        signatureFile = new File(tmpDir, "scripts/signature");
//        jdeprscanFile = new File(tmpDir, "scripts/jdeprscan.sh");
//        jdepsFile = new File(tmpDir, "scripts/jdeps.sh");
        linuxMonitoringFile = new File(tmpDir, "scripts/roro_linux_monitoring.sh");
        aixMonitoringFile = new File(tmpDir, "scripts/roro_aix_monitoring.sh");
//        windowsMonitoringFile = new File(tmpDir, "scripts/roro_windows_monitoring.ps1");
//
//        try {
//            FileUtils.copyURLToFile(classLoader.getResource("scripts/system_info_collector"), systemInfoCollectorFile);
//            logger.debug("systemInfoCollector file copied to " + systemInfoCollectorFile.getAbsolutePath());
//            if (systemInfoCollectorFile.setExecutable(true, false)) {
//                logger.debug("Set executable permission " + systemInfoCollectorFile.getAbsolutePath());
//            }
//        } catch (IOException e) {
//            // nothing to do
//            logger.warn("Failed system_info_collector file copied to " + systemInfoCollectorFile.getAbsolutePath());
//        }
//
//        try {
//            FileUtils.copyURLToFile(classLoader.getResource("scripts/cancel_assessment.sh"), cancelAssessmentFile);
//            logger.debug("cancel_assessment.py file copied to " + cancelAssessmentFile.getAbsolutePath());
//            if (cancelAssessmentFile.setExecutable(true, false)) {
//                logger.debug("Set executable permission " + cancelAssessmentFile.getAbsolutePath());
//            }
//        } catch (IOException e) {
//            // nothing to do
//            logger.warn("Failed cancel_assessment.py file copied to " + cancelAssessmentFile.getAbsolutePath());
//        }
//
//        try {
//            FileUtils.copyURLToFile(classLoader.getResource("scripts/signature"), signatureFile);
//            logger.debug("signature file copied to " + signatureFile.getAbsolutePath());
//            if (signatureFile.setExecutable(true, false)) {
//                logger.debug("Set executable permission " + signatureFile.getAbsolutePath());
//            }
//        } catch (IOException e) {
//            // nothing to do
//            logger.warn("Failed signature file copied to " + signatureFile.getAbsolutePath());
//        }
//
//        try {
//            FileUtils.copyURLToFile(classLoader.getResource("scripts/jdeprscan.sh"), jdeprscanFile);
//            logger.debug("jdeprscan.sh file copied to " + jdeprscanFile.getAbsolutePath());
//            if (jdeprscanFile.setExecutable(true, false)) {
//                logger.debug("Set executable permission " + jdeprscanFile.getAbsolutePath());
//            }
//        } catch (IOException e) {
//            // nothing to do
//            logger.warn("Failed jdeprscan.sh file copied to " + jdeprscanFile.getAbsolutePath());
//        }
//
//        try {
//            FileUtils.copyURLToFile(classLoader.getResource("scripts/jdeps.sh"), jdepsFile);
//            logger.debug("jdeps.sh file copied to " + jdepsFile.getAbsolutePath());
//            if (jdepsFile.setExecutable(true, false)) {
//                logger.debug("Set executable permission " + jdepsFile.getAbsolutePath());
//            }
//        } catch (IOException e) {
//            // nothing to do
//            logger.warn("Failed jdeps.sh file copied to " + jdepsFile.getAbsolutePath());
//        }
//
        try {
            FileUtils.copyURLToFile(classLoader.getResource("scripts/roro_linux_monitoring.sh"), linuxMonitoringFile);
            logger.debug("roro_linux_monitoring.sh file copied to " + linuxMonitoringFile.getAbsolutePath());
            if (linuxMonitoringFile.setExecutable(true, false)) {
                logger.debug("Set executable permission " + linuxMonitoringFile.getAbsolutePath());
            }
        } catch (IOException e) {
            // nothing to do
            logger.warn("Failed roro_linux_monitoring.sh file copied to " + linuxMonitoringFile.getAbsolutePath());
        }

        try {
            FileUtils.copyURLToFile(classLoader.getResource("scripts/roro_aix_monitoring.sh"), aixMonitoringFile);
            logger.debug("roro_aix_monitoring.sh file copied to " + aixMonitoringFile.getAbsolutePath());
            if (aixMonitoringFile.setExecutable(true, false)) {
                logger.debug("Set executable permission " + aixMonitoringFile.getAbsolutePath());
            }
        } catch (IOException e) {
            // nothing to do
            logger.warn("Failed roro_aix_monitoring.sh file copied to " + aixMonitoringFile.getAbsolutePath());
        }
//
//        try {
//            FileUtils.copyURLToFile(classLoader.getResource("scripts/roro_windows_monitoring.ps1"), windowsMonitoringFile);
//            logger.debug("roro_windows_monitoring.ps1 file copied to " + windowsMonitoringFile.getAbsolutePath());
//            if (windowsMonitoringFile.setExecutable(true, false)) {
//                logger.debug("Set executable permission " + windowsMonitoringFile.getAbsolutePath());
//            }
//        } catch (IOException e) {
//            // nothing to do
//            logger.warn("Failed roro_windows_monitoring.ps1 file copied to " + windowsMonitoringFile.getAbsolutePath());
//        }
//
    }

    private void pipInstall(String command) throws ExecuteException, IOException, InterruptedException {
        DefaultExecutor executor = new DefaultExecutor();

        CommandLine cl = CollectionHelper.getCommandLine(CollectionHelper.findCommand("sudo"),
                command,
                "pip");

        /*
        CommandLine cl = new CommandLine(CollectionHelper.findCommand("sudo"))
                .addArguments(command)
                .addArguments("pip");
         */

        logger.debug("Command for pip install : [{}]", cl);

        executor.execute(cl);
        logger.debug("PIP installed.");
    }

    private void moduleInstall(String command) throws ExecuteException, IOException, InterruptedException {
        DefaultExecutor executor = new DefaultExecutor();

        CommandLine cl = CollectionHelper.getCommandLine(CollectionHelper.findCommand("sudo"),
                command,
                "install",
                "paramiko");

        /*
        CommandLine cl = new CommandLine(CollectionHelper.findCommand("sudo"))
                .addArguments(command)
                .addArguments("install")
                .addArguments("paramiko");
         */

        logger.debug("Command for module(paramiko) install : [{}]", cl);

        executor.execute(cl);
        logger.debug("paramiko installed.");
    }
}
//end of CollectionHelper.java