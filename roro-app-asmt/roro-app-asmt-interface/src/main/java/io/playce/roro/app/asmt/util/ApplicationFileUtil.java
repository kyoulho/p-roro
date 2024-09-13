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
 * SangCheon Park   Nov 26, 2020		First Draft.
 */
package io.playce.roro.app.asmt.util;

import com.ibm.icu.text.CharsetDetector;
import io.playce.roro.app.asmt.ApplicationScanConfig;
import io.playce.roro.app.asmt.result.ApplicationAssessmentResult;
import io.playce.roro.app.asmt.support.ApplicationAssessmentHelper;
import io.playce.roro.common.dto.assessment.ApplicationDto;
import io.playce.roro.common.exception.InsufficientException;
import io.playce.roro.common.exception.NotsupportedException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.CommandUtil;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.WinRmUtils;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 2.0.0
 */
@Slf4j
public class ApplicationFileUtil {

    private static String extensions_find_options = null;

    /**
     * Remote copy files.
     *
     * @param targetHost the targetHost
     * @param sourceDir  the source dir
     * @param targetDir  the target dir
     * @param result     the result
     *
     * @return the string
     */
    public static String remoteCopy(TargetHost targetHost, String sourceDir, String targetDir, ApplicationAssessmentResult result) {
        sourceDir = sourceDir.replaceAll("file://", "");

        boolean isDir = false;
        try {
            String r = SSHUtil.executeCommand(targetHost, "sh -c \"[ -d '" + sourceDir + "' ] && echo 'DIR' || echo 'FILE'\"");
            if (r.contains("DIR")) {
                isDir = true;
            }
        } catch (Exception e) {
            // ignore
        }

        ApplicationSSHUtil.getDir(targetHost, sourceDir, targetDir, result);

        if (isDir) {
            return targetDir;
        } else {
            String sourceName = FilenameUtils.getName(sourceDir);
            File targetFile = new File(targetDir, sourceName);

            if (targetFile.exists()) {
                return targetFile.getAbsolutePath();
            } else {
                return targetDir + File.separator + sourceName;
            }
        }
    }

    /**
     * Remote copy files.
     *
     * @param targetHost the target host
     * @param sourceDir  the source dir
     * @param targetDir  the target dir
     * @param result     the result
     *
     * @return the string
     */
    public static String remoteCopyWithTar(TargetHost targetHost, String sourceDir, String targetDir, ApplicationAssessmentResult result) throws Exception {
        ApplicationScanConfig applicationScanConfig = CommonProperties.getApplicationContext().getBean(ApplicationScanConfig.class);
        if (applicationScanConfig == null) {
            applicationScanConfig = new ApplicationScanConfig();
        }

        String inputFileName = "/tmp/" + RandomStringUtils.random(10, true, false);
        boolean inputFileCreated = false;

        boolean onlyMatchedExtensions = applicationScanConfig.getCopy().isOnlyMatchedExtensions();
        String excludeFileNames = applicationScanConfig.getCopy().getIgnoreFilenames();

        sourceDir = sourceDir.replaceAll("file://", "");

        if (sourceDir.startsWith("/")) {
            sourceDir = FilenameUtils.normalize(sourceDir);
        }

        boolean isDir = false;
        try {
            String r = SSHUtil.executeCommand(targetHost, "sudo sh -c \"[ -d '" + sourceDir + "' ] && echo 'DIR' || echo 'FILE'\"");
            if (r.contains("DIR")) {
                isDir = true;
            }
        } catch (Exception e) {
            // ignore
        }

        int depth = StringUtils.countMatches(sourceDir, "/");
        if (depth > 1) {
            depth = depth - 1;
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            DefaultExecutor executor = new DefaultExecutor();
            PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    CommandUtil.findPython(),
                    ApplicationAssessmentHelper.getApplicationFileDownloadFile().getAbsolutePath(),
                    "-H " + targetHost.getIpAddress(),
                    "-P " + targetHost.getPort(),
                    "-u " + targetHost.getUsername());

            if (StringUtils.isNotEmpty(targetHost.getPassword())) {
                cl = cl.addArguments("-p '" + targetHost.getPassword() + "'");
            }

            if (StringUtils.isNotEmpty(targetHost.getKeyFilePath())) {
                cl = cl.addArguments("-k " + targetHost.getKeyFilePath());
            }

            cl = cl.addArguments("-s " + SSHUtil.isSudoer(targetHost))
                    .addArguments("--source_dir " + sourceDir)
                    .addArguments("--target_dir " + targetDir)
                    .addArguments("--depth " + depth);

            if (onlyMatchedExtensions && isDir) {
                List<String> fileExtensions = applicationScanConfig.getFileExtensions();

                inputFileCreated = createInputFile(targetHost, sourceDir, fileExtensions, inputFileName);

                if (inputFileCreated) {
                    cl = cl.addArguments("--input_file " + inputFileName);
                }
            }

            if (StringUtils.isNotEmpty(excludeFileNames)) {
                cl = cl.addArguments("--exclude " + excludeFileNames);
            }

            // https://cloud-osci.atlassian.net/browse/PCR-6223
            log.debug("Download files using [{}]", getLoggingMessage(Arrays.asList(cl.toStrings())));

            int exitCode = executor.execute(cl);

            if (exitCode == 0) {
                log.debug("File downloaded from [{}] to [{}].",
                        targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":" + sourceDir,
                        targetDir);
            } else {
                log.warn("File({}) download failed. [Reason] : {}",
                        targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":" + sourceDir,
                        baos);

                throw new RuntimeException("File(" + sourceDir + ") download failed. [Reason] : " + baos);
            }
        } catch (Exception e) {
            log.error("File({}) download failed.", sourceDir, e);

            if (e instanceof ExecuteException) {
                e = new Exception("Please check 'python' command or '" + ApplicationAssessmentHelper.getApplicationFileDownloadFile().getAbsolutePath() + "' script file exists.");
            }

            throw e;
        } finally {
            if (inputFileCreated) {
                removeInputFile(targetHost, inputFileName);
            }
        }

        return getApplicationFile(sourceDir, targetDir, result, isDir);
    }

    /**
     * Remote copy files.
     *
     * @param application the application
     * @param sourceDir   the source dir
     * @param targetDir   the target dir
     * @param result      the result
     *
     * @return the string
     */
    public static String remoteCopyWithPscp(ApplicationDto application, String sourceDir, String targetDir, ApplicationAssessmentResult result) throws Exception {
        sourceDir = sourceDir.replaceAll("file://", "");

        boolean isDir = false;

        try {
            String r = WinRmUtils.executeCommand(application.getTargetHost(), "where pscp");
            if (StringUtils.isEmpty(r)) {
                // throw new Exception("'pscp' command not found. Please check 'pscp' is installed and Path has been set.");
                throw new InsufficientException("'pscp' command not found. Please check 'pscp' is installed and Path has been set.");
            }

            r = WinRmUtils.executeCommand(application.getTargetHost(), "IF EXIST \"" + sourceDir + "\\*\" (echo DIR) ELSE (echo FILE)");
            if (r.contains("DIR")) {
                isDir = true;
            }

            String ipAddress = application.getSsh().getIpAddress();
            Integer port = application.getSsh().getPort();
            String userName = application.getSsh().getUserName();
            String password = application.getSsh().getPassword();
            String ppkFilePath = application.getSsh().getPpkFilePath();

            if (StringUtils.isEmpty(ipAddress) || StringUtils.isEmpty(userName) || (StringUtils.isEmpty(password) && StringUtils.isEmpty(ppkFilePath))) {
                // throw new Exception("RoRo server authentication information does not set. Please check roro.ssh.ip-address, port, user-name, password, and ppk-file-path have been set.");
                throw new InsufficientException("RoRo server authentication information does not set. Please check roro.ssh.ip-address, port, user-name, password, and ppk-file-path have been set.");
            }

            StringBuilder sb = new StringBuilder()
                    .append("echo y")
                    .append(" | ")
                    .append("pscp ");

            if (StringUtils.isNotEmpty(password)) {
                sb = sb.append("-pw ")
                        .append(password)
                        .append(StringUtils.SPACE);
            }

            if (StringUtils.isNotEmpty(ppkFilePath)) {
                sb = sb.append("-i ")
                        .append("\"")
                        .append(ppkFilePath)
                        .append("\"")
                        .append(StringUtils.SPACE);
            }

            if (port == null || port == 0) {
                port = 22;
            }

            if (port != 22) {
                sb = sb.append("-P ")
                        .append(port)
                        .append(StringUtils.SPACE);
            }

            sb = sb.append("-r ")
                    .append("\"")
                    .append(sourceDir)
                    .append("\"")
                    .append(StringUtils.SPACE)
                    .append(userName)
                    .append("@")
                    .append(ipAddress)
                    .append(":")
                    .append(result.getApplicationDir() + File.separator);

            if (StringUtils.isNotEmpty(password)) {
                // https://cloud-osci.atlassian.net/browse/PCR-6223
                log.debug("Windows scp command [{}] will be sent to [{}]", getLoggingMessage(Arrays.asList(sb.toString().split(StringUtils.SPACE))), application.getTargetHost().getIpAddress());
            } else {
                log.debug("Windows scp command [{}] will be sent to [{}]", sb, application.getTargetHost().getIpAddress());
            }

            String scpResult = WinRmUtils.executeCommand(application.getTargetHost(), sb.toString());

            // https://cloud-osci.atlassian.net/browse/PCR-6223
            log.debug("Windows scp command [{}], result [{}]", getLoggingMessage(Arrays.asList(sb.toString().split(StringUtils.SPACE))), scpResult);
        } catch (Exception e) {
            log.error("File({}) download failed.", sourceDir, e);
            throw e;
        }

        return getApplicationFile(sourceDir, targetDir, result, isDir);
    }

    @NotNull
    private static String getApplicationFile(String sourceDir, String targetDir, ApplicationAssessmentResult result, boolean isDir) {
        chmod(targetDir, "777");

        if (isDir) {
            parseDirectory(targetDir, new File(targetDir), result);
            return targetDir;
        } else {
            String sourceName = FilenameUtils.getName(sourceDir);
            File targetFile = new File(targetDir, sourceName);

            if (targetFile.exists()) {
                return targetFile.getAbsolutePath();
            } else {
                return targetDir + File.separator + sourceName;
            }
        }
    }

    /**
     * Unzip string.
     *
     * @param archiveFile the archive file
     * @param targetDir   the target dir
     * @param result      the result
     *
     * @return the string
     *
     * @throws Exception the exception
     */
    public static String unzip(String archiveFile, String targetDir, ApplicationAssessmentResult result) throws Exception {
        String fqfn = null;
        String entryName = null;

        // switching separator to current system.
        archiveFile = FilenameUtils.separatorsToSystem(archiveFile);
        targetDir = FilenameUtils.separatorsToSystem(targetDir);

        InputStream is = new FileInputStream(archiveFile);

        int seq = 0;
        if (archiveFile.toLowerCase().endsWith(".zip") || archiveFile.toLowerCase().endsWith(".ear") ||
                archiveFile.toLowerCase().endsWith(".war") || archiveFile.toLowerCase().endsWith(".jar")) {
            ZipInputStream zis = new ZipInputStream(is);
            FileOutputStream fos = null;

            ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null) {
                entryName = FilenameUtils.separatorsToSystem(entry.getName());

                if (seq++ == 0) {
                    fqfn = targetDir + File.separator + entryName;
                }

                if (entry.isDirectory()) {
                    File dir = new File(targetDir, entryName);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    continue;
                }

                unzip(targetDir, entryName, zis, result);
            }

            IOUtils.closeQuietly(zis);
        } else if (archiveFile.toLowerCase().endsWith(".tar.gz")) {
            TarArchiveInputStream fin = new TarArchiveInputStream(new GzipCompressorInputStream(is));
            FileOutputStream fos = null;

            TarArchiveEntry entry = null;
            while ((entry = fin.getNextTarEntry()) != null) {
                entryName = FilenameUtils.separatorsToSystem(entry.getName());

                if (seq++ == 0) {
                    fqfn = targetDir + File.separator + entryName;
                }

                if (entry.isDirectory()) {
                    File dir = new File(targetDir, entryName);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    continue;
                }

                unzip(targetDir, entryName, fin, result);
            }

            IOUtils.closeQuietly(fin);
        } else {
            // throw new Exception("Unzip only supports [tar.gz], [zip], [ear], [war] and [jar] files.");
            throw new NotsupportedException("Application does support only zip, tar.gz, ear, war and jar.");
        }

        return fqfn;
    }

    /**
     * @param targetDir
     * @param entryName
     * @param fin
     * @param result
     *
     * @throws Exception
     */
    private static void unzip(String targetDir, String entryName, InputStream fin, ApplicationAssessmentResult result) throws Exception {
        // 파일 크기가 10MB 초과 파일에 대해서는 Skip 한다.
        // 분석 대상 텍스트 파일이 아닐 가능성이 크며, 압축해제 시간을 단축시키기 위함.
        if (fin.available() > 10 * 1024 * 1024) {
            // 추가 분석 대상 Library인 경우 10MB 를 넘더라도 압축을 해제한다.
            if (!result.getAnalysisLibList().contains(FilenameUtils.getName(entryName))) {
                log.info("[{}] file will be skip to extract. Too large({}).", entryName, fin.available());
                return;
            }
        }

        io.playce.roro.common.util.FileUtil.unzip(targetDir, entryName, fin);

        if (result != null) {
            File currFile = new File(targetDir, entryName);
            ApplicationSSHUtil.parseFile(targetDir, currFile, result);
        }
    }

    /**
     * @param file
     *
     * @return
     *
     * @throws IOException
     */
    private static String detectCharset(File file) {
        String encoding = null;

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buf = new byte[4096];

            UniversalDetector detector = new UniversalDetector(null);

            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();

            encoding = detector.getDetectedCharset();

            if (encoding != null && detector.isDone() && Charset.isSupported(encoding)) {
                detector.reset();
            }
        } catch (Exception e) {
            log.error("Unhandled exception occurred while detect a file encoding.", e);
        }

        return encoding;
    }

    /**
     * @param file
     *
     * @return
     *
     * @throws IOException
     */
    public static String getFileContents(File file) {
        String contents = null;

        try {
            //*
            CharsetDetector detector = new CharsetDetector();

            try (InputStream input = new FileInputStream(file)) {
                byte[] data = org.apache.commons.io.IOUtils.toByteArray(input, file.length());
                detector.setDeclaredEncoding("UTF-8");
                detector.setText(data);
                detector.detectAll();

                for (com.ibm.icu.text.CharsetMatch m : detector.detectAll()) {
                    if (m.getName().toLowerCase().equals("euc-kr")) {
                        contents = m.getString();
                        break;
                    }
                }

                if (contents == null) {
                    contents = detector.detect().getString();
                }
            }
            /*/
            String charset = detectCharset(file);

            log.debug("[{}]'s charset is [{}]", file.getAbsolutePath(), charset);

            contents = org.apache.commons.io.IOUtils.toString(new FileReader(file));

            if (StringUtils.isNotEmpty(charset)) {
                if (!"UTF8".equals(charset.toUpperCase()) && !"UTF-8".equals(charset.toUpperCase())) {
                    contents = new String(contents.getBytes(charset), "UTF-8");
                }
            } else {
                CharsetDetector detector = new CharsetDetector();

                try (InputStream input = new FileInputStream(file)) {
                    byte[] data = org.apache.commons.io.IOUtils.toByteArray(input, file.length());
                    detector.setDeclaredEncoding("UTF-8");
                    detector.setText(data);
                    detector.detectAll();

                    for (com.ibm.icu.text.CharsetMatch m : detector.detectAll()) {
                        if (m.getName().toLowerCase().equals("euc-kr")) {
                            contents = m.getString();
                            break;
                        }
                    }

                    if (contents == null) {
                        contents = detector.detect().getString();
                    }
                }
            }
            //*/
        } catch (IOException e) {
            log.error("Unhandled exception occurred while get a file contents.", e);
        }

        return contents;
    }

    /**
     * @param targetDir
     * @param dir
     * @param result
     */
    private static void parseDirectory(String targetDir, File dir, ApplicationAssessmentResult result) {
        if (dir != null) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : dir.listFiles()) {
                    if (file.isFile()) {
                        try {
                            ApplicationSSHUtil.parseFile(targetDir, file, result);
                        } catch (Exception e) {
                            log.warn("Application file({}) parse failed. [Reason] : {}", file.getAbsolutePath(), e.getMessage());
                        }
                    } else {
                        parseDirectory(targetDir, file, result);
                    }
                }
            }
        }
    }

    /**
     * Change directory permission
     *
     * @param filePath
     * @param permission
     */
    public static void chmod(String filePath, String permission) {
        try {
            if (filePath.startsWith(CommonProperties.getWorkDir())) {
                CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                        CommandUtil.findCommand("chmod"),
                        "-R",
                        permission,
                        filePath);

                CommandUtil.executeCommand(cl);
            }
        } catch (Exception e) {
            log.error("Shell execution error while change permissions. Error Log => [{}]", e.getMessage());
        }
    }

    /**
     * Change directory permission
     *
     * @param filePath
     */
    public static void rm(String filePath) {
        try {
            if (filePath.startsWith(CommonProperties.getWorkDir())) {
                CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                        CommandUtil.findCommand("rm"),
                        "-rf",
                        filePath);

                CommandUtil.executeCommand(cl);
            }
        } catch (Exception e) {
            log.error("Shell execution error while remove directory. Error Log => [{}]", e.getMessage());
        }
    }

    // https://cloud-osci.atlassian.net/browse/PCR-6223
    public static String getLoggingMessage(List<String> messages) {
        StringBuilder sb = new StringBuilder();
        String msg;
        for (int i = 0; i < messages.size(); i++) {
            msg = messages.get(i);

            if (sb.length() > 0) {
                sb.append(StringUtils.SPACE);
            }
            sb.append(msg);

            if (msg.equals("-p") || msg.equals("-pw")) {
                sb.append(StringUtils.SPACE).append("*****");
                i++;
                continue;
            }
        }

        return sb.toString();
    }

    /**
     * <pre>
     * 애플리케이션 스캔 시간을 줄이기 위해 스캔 대상 확장자들만 다운로드 하기 위해 파일 목록을 작성한다.
     * </pre>
     *
     * @param targetHost
     * @param sourceDir
     * @param fileExtensions
     * @param inputFileName
     *
     * @return
     */
    private static boolean createInputFile(TargetHost targetHost, String sourceDir, List<String> fileExtensions, String inputFileName) {
        StringBuilder sb;
        if (StringUtils.isEmpty(extensions_find_options)) {
            sb = new StringBuilder();

            for (String extension : fileExtensions) {
                if (sb.length() > 0) {
                    sb.append(StringUtils.SPACE).append("-o").append(StringUtils.SPACE);
                }

                sb.append("-name").append(StringUtils.SPACE).append("'*.").append(extension).append("'");
            }

            extensions_find_options = sb.toString();
        }

        sb = new StringBuilder();
        sb.append("sudo")
                .append(StringUtils.SPACE).append("find")
                .append(StringUtils.SPACE).append(sourceDir)
                .append(StringUtils.SPACE).append("-type")
                .append(StringUtils.SPACE).append("f")
                .append(StringUtils.SPACE).append(extensions_find_options)
                .append(StringUtils.SPACE).append(">")
                .append(StringUtils.SPACE).append(inputFileName);

        try {
            SSHUtil.executeCommand(targetHost, sb.toString());
            return true;
        } catch (InterruptedException e) {
            log.warn("Unable to create a tar input file. Reason : [{}]", e.getMessage());
        }

        return false;
    }

    /**
     * <pre>
     * 생성된 input file을 삭제한다.
     * </pre>
     *
     * @param targetHost
     * @param inputFileName
     */
    private static void removeInputFile(TargetHost targetHost, String inputFileName) {
        try {
            SSHUtil.executeCommand(targetHost, "sudo rm -f " + inputFileName);
        } catch (Exception e) {
            log.warn("Unable to delete a tar input file({}). Reason : [{}]", inputFileName, e.getMessage());
        }
    }
}
//end of FileUtil.java