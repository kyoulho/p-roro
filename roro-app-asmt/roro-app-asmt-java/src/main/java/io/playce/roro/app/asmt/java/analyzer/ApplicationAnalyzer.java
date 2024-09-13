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
 * SangCheon Park   Jan 12, 2022	    First Draft.
 */
package io.playce.roro.app.asmt.java.analyzer;

import io.playce.roro.app.asmt.ApplicationScanConfig;
import io.playce.roro.app.asmt.java.policy.Policy;
import io.playce.roro.app.asmt.java.threadpool.executor.AssessmentThreadPoolExecutor;
import io.playce.roro.app.asmt.java.threadpool.task.DependencyCheckTask;
import io.playce.roro.app.asmt.java.threadpool.task.DeprecatedScanTask;
import io.playce.roro.app.asmt.result.ApplicationAssessmentResult;
import io.playce.roro.app.asmt.support.ApplicationAssessmentHelper;
import io.playce.roro.common.util.CommandUtil;
import io.playce.roro.common.util.ThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static io.playce.roro.common.util.ThreadLocalUtils.APP_SCAN_ERROR;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0.0
 */
@Slf4j
public class ApplicationAnalyzer {

    /**
     * The constant CHECK_FILE_EXTENSIONS.
     */
    // application-appscan.yml 파일로 이동
    private static final List<String> CHECK_FILE_EXTENSIONS = Arrays.asList(new String[]{
            "java", "class", "jsp", "jspx", "html", "htm", "js", "properties", "sql", "xml", "xmi", "yml", "yaml", "json", "sh", "bat", "scala", "groovy", "c", "pc", "cpp"
    });

    /**
     * The Policy.
     */
    private Policy policy;
    /**
     * The Result.
     */
    private ApplicationAssessmentResult result;
    /**
     * The Executor.
     */
    private AssessmentThreadPoolExecutor executor;
    private String assessmentDir;

    private List<String> fileExtensions;
    private Pattern excludeFilePattern;
    private List<String> excludeDomains;

    /**
     * Instantiates a new Application analyzer.
     *
     * @param policy        the policy
     * @param result        the result
     * @param assessmentDir the assessmentDir
     */
    public ApplicationAnalyzer(Policy policy, ApplicationAssessmentResult result, String assessmentDir, ApplicationScanConfig applicationScanConfig) {
        this.policy = policy;
        this.result = result;
        this.executor = new AssessmentThreadPoolExecutor(result.getApplicationDir());
        this.assessmentDir = assessmentDir;

        this.fileExtensions = applicationScanConfig.getFileExtensions();
        this.excludeFilePattern = applicationScanConfig.getExcludeFilePattern();
        this.excludeDomains = applicationScanConfig.getExcludeDomains();
    }

    /**
     * Analyze.
     *
     * @param path the path
     */
    public void analyze(String path) {
        File file = new File(path);
        analyze(file, assessmentDir);

        /**
         ********************************
         * Search Deprecated API Usages
         ********************************
         * 1. jdeprscan 명령이 존재하는지 확인
         * 2. jdeprscan 버전 확인 (jdeprscan --version)
         * 3. release 8 부터 jdeprscan의 버전까지 assessmentDir을 대상으로 scan (jdeprscan --release {version} {assessmentDir} 2> /dev/null)
         *    - Thread 사용
         * 4. assessmentDir을 대상으로 jdeps 실행 (jdeps --jdk-internals {assessmentDir})
         */
        Integer version = getVersion();

        log.debug("jdeps version : {}", version);

        if (version != null) {
            for (int v = 6; v <= version; v++) {
                executor.execute(new DeprecatedScanTask(v, new File(assessmentDir), result));
            }

            deps(new File(assessmentDir));
        }

        executor.getExecutor().shutdown();

        try {
            while (!executor.getExecutor().isTerminated()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * Analyze.
     *
     * @param file     the file
     * @param rootPath the root path
     */
    private void analyze(File file, String rootPath) {
        File[] fileList = file.listFiles();

        File jarfile = null;
        String extension = null;
        for (File f : fileList) {
            // Symbolic Link는 분석 대상에서 제외한다. 링크가 애플리케이션 내에 있는 디렉토리면 탐색된다.
            if (Files.isSymbolicLink(f.toPath())) {
                continue;
            }

            if (f.isDirectory()) {
                if (!f.getAbsolutePath().replaceAll(assessmentDir, "").startsWith("/target/")) {
                    analyze(f, rootPath);
                }
            } else {
                extension = FilenameUtils.getExtension(f.getName()).toLowerCase();

                if (fileExtensions.contains(extension) &&
                        !excludeFilePattern.matcher(f.getName()).matches() &&
                        !f.getAbsolutePath().contains("node_modules")) {
                    if (!f.getName().equals("build.xml") && !f.getName().equals("pom.xml") && !f.getName().endsWith(".jar")) {
                        executor.execute(new DependencyCheckTask(f, extension, rootPath, policy, result, excludeDomains));
                    }
                }

                if (jarfile == null) {
                    if (extension.equalsIgnoreCase("jar")) {
                        jarfile = f;
                    }
                }
            }
        }

        if ((result.getApplicationType() == null || !result.getApplicationType().contains("Web")) && jarfile != null) {
            if (jarfile.getParentFile().getAbsolutePath().contains("WEB-INF")) {
                // web.xml이 존재하지 않는 Web Application
                result.setApplicationType("Java Web Application");
            } else {
                result.setApplicationType("Java Application");
            }
        }
    }

    /**
     * Gets version.
     *
     * @return the version
     */
    private Integer getVersion() {
        Integer version = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            DefaultExecutor executor = new DefaultExecutor();
            PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sh"),
                    ApplicationAssessmentHelper.getJdepsFile().getAbsolutePath(),
                    "--version");

            /*
            CommandLine cl = new CommandLine(CollectionHelper.findCommand("sh"))
                    .addArguments(CollectionHelper.getJdepsFile().getAbsolutePath())
                    .addArguments("--version");
            */

            /*
            CommandLine cl = new CommandLine(CollectionHelper.findCommand("jdeps"))
                    .addArguments("--version");
            */

            log.debug("jdeps version check command : {}", cl.toString());

            int exitCode = executor.execute(cl);

            String result;

            if (exitCode == 0) {
                result = baos.toString();
            } else {
                log.info("jdeps execution failed. [Command] : {}", cl.toString());
                throw new Exception(baos.toString());
            }

            result = result.substring(0, result.indexOf("."));
            version = Integer.parseInt(result);
        } catch (Exception e) {
            log.error("Command execution failed while get Java Release version. Error Log => [{}]", e.getMessage());
            ThreadLocalUtils.add(APP_SCAN_ERROR, "Command execution failed while get Java Release version.");
        }

        return version;
    }

    /**
     * Deps.
     *
     * @param file the file
     */
    private void deps(File file) {
        CommandLine cl = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            DefaultExecutor executor = new DefaultExecutor();
            PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    CommandUtil.findCommand("sh"),
                    ApplicationAssessmentHelper.getJdepsFile().getAbsolutePath(),
                    file.getAbsolutePath());

            /*
            cl = new CommandLine(CollectionHelper.findCommand("sudo"))
                    .addArguments(CollectionHelper.findCommand("sh"))
                    .addArguments(CollectionHelper.getJdepsFile().getAbsolutePath())
                    .addArguments(file.getAbsolutePath());
            */

            /*
            cl = new CommandLine(CollectionHelper.findCommand("jdeps"))
                    .addArguments("--jdk-internals")
                    .addArguments(file.getAbsolutePath());
            */

            int exitCode = executor.execute(cl);

            String result;

            if (exitCode == 0) {
                result = baos.toString();
                parse(result);
            } else {
                throw new Exception(baos.toString());
            }
        } catch (Exception e) {
            log.info("jdeps execution failed. [Command] : {}", cl.toString());
            log.error("Command execution failed while execute jdeps. Error Log => [{}]", e.getMessage());
            ThreadLocalUtils.add(APP_SCAN_ERROR, "Command execution failed while execute jdeps.");
        }
    }

    /**
     * Parse.
     *
     * @param jdepsResult the jdeps result
     */
    private void parse(String jdepsResult) {
        ApplicationAssessmentResult.Removed removed = new ApplicationAssessmentResult.Removed();

        if (removed.getClazz() != null) {
            result.getRemovedList().add(removed);
        }

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(jdepsResult.getBytes())))) {
            String lineStr = null;
            int lineNum = 1;

            boolean afterWarning = false;
            boolean afterDash = false;
            while ((lineStr = buffer.readLine()) != null) {
                if (lineNum++ == 1) {
                    continue;
                }

                if (lineStr.startsWith("Warning:")) {
                    afterWarning = true;
                    continue;
                }

                if (lineStr.startsWith("-----")) {
                    afterDash = true;
                    continue;
                }

                if (!afterWarning && !afterDash && lineStr.contains("->")) {
                    removed = new ApplicationAssessmentResult.Removed();
                    int i = 0;
                    for (String s : lineStr.split(" ")) {
                        if (!s.equals("->") && !s.equals(" ") && !s.equals("")) {
                            if (i++ == 0) {
                                removed.setClazz(s);
                            } else {
                                removed.setApi(s);
                                break;
                            }
                        }
                    }

                    if (removed.getClazz() != null && removed.getApi() != null) {
                        result.getRemovedList().add(removed);
                    }
                }

                if (afterDash) {
                    String[] replacement = lineStr.split("Use");
                    for (ApplicationAssessmentResult.Removed r : result.getRemovedList()) {
                        if (r.getApi().equals(replacement[0].trim())) {
                            r.setReplacement(replacement[1].trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception occurred while parsing jdpes result.", e);
            ThreadLocalUtils.add(APP_SCAN_ERROR, "Command execution failed while parsing jdeps result.");
        }
    }

    public void cancel() {
        try {
            if (!executor.getExecutor().isTerminated()) {
                executor.getExecutor().shutdown();
            }
        } catch (Exception ignored) {
            // ignore
        }
    }
}
//end of ApplicationAnalyzer.java