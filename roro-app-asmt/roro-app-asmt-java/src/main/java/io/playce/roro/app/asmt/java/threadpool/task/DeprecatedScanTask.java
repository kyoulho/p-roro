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
package io.playce.roro.app.asmt.java.threadpool.task;

import io.playce.roro.app.asmt.result.ApplicationAssessmentResult;
import io.playce.roro.app.asmt.support.ApplicationAssessmentHelper;
import io.playce.roro.common.util.CommandUtil;
import io.playce.roro.common.util.ThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.*;

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
public class DeprecatedScanTask extends BaseTask {

    private Integer version;
    private File assessmentFile;
    private ApplicationAssessmentResult applicationAssessmentResult;

    /**
     * Instantiates a new Deprecated scan task.
     *
     * @param version                     the version
     * @param assessmentFile              the assessment file
     * @param applicationAssessmentResult the application assessment result
     */
    public DeprecatedScanTask(Integer version, File assessmentFile, ApplicationAssessmentResult applicationAssessmentResult) {
        super(assessmentFile.getAbsoluteFile() + " Deprecated API Scan Task");

        this.version = version;
        this.assessmentFile = assessmentFile;
        this.applicationAssessmentResult = applicationAssessmentResult;

        log.debug("[{}, {}] Deprecated Scan Task has been started.", assessmentFile.getAbsolutePath(), version);
    }

    @Override
    protected void taskRun() {
        CommandLine cl = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            DefaultExecutor executor = new DefaultExecutor();
            PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            cl = CommandUtil.getCommandLine(
                    //CommandUtil.findCommand("sudo"),
                    CommandUtil.findCommand("sh"),
                    ApplicationAssessmentHelper.getJdeprscanFile().getAbsolutePath(),
                    version.toString(),
                    assessmentFile.getAbsolutePath());

            /*
            cl = new CommandLine(CollectionHelper.findCommand("sudo"))
                    .addArguments(CollectionHelper.findCommand("sh"))
                    .addArguments(CollectionHelper.getJdeprscanFile().getAbsolutePath())
                    .addArguments(version.toString())
                    .addArguments(assessmentFile.getAbsolutePath());
            */
            /*
            cl = new CommandLine(CollectionHelper.findCommand("jdeprscan"))
                    .addArguments("--release")
                    .addArguments(version.toString())
                    .addArguments(file.getAbsolutePath())
                    .addArguments("2> /dev/null");
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
            log.info("jdeprscan execution failed. [Command] : {}", cl.toString());
            log.error("Command execution failed while execute jdeprscan. Error Log => [{}]", e.getMessage());
            ThreadLocalUtils.add(APP_SCAN_ERROR, "Command execution failed while execute jdeprscan.");
        }
    }

    private void parse(String result) {
        ApplicationAssessmentResult.Deprecated deprecated = new ApplicationAssessmentResult.Deprecated();
        deprecated.setRelease(version);

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(result.getBytes())))) {
            String lineStr = null;
            int lineNum = 1;

            while ((lineStr = buffer.readLine()) != null) {
                if (lineNum++ == 1) {
                    continue;
                }

                String[] scanInfo = lineStr.split(" ");

                if (scanInfo.length >= 5 && !lineStr.startsWith("error:") && !lineStr.startsWith("warning:")) {
                    ApplicationAssessmentResult.Use use = new ApplicationAssessmentResult.Use();
                    use.setClazz(scanInfo[1].replaceAll("/", "."));

                    ApplicationAssessmentResult.Reference reference = new ApplicationAssessmentResult.Reference();

                    if (scanInfo[5].indexOf("::") < 0) {
                        reference.setClazz(scanInfo[5].replaceAll("/", "."));
                    } else {
                        String clazz = scanInfo[5].substring(0, scanInfo[5].indexOf("::")).replaceAll("/", ".");
                        String method = scanInfo[5].substring(scanInfo[5].indexOf("::") + 2);
                        method = method.substring(0, method.indexOf("("));

                        if (method.equals("<init>")) {
                            method = "<init> - Constructor";
                        } else if (method.equals("<clinit>")) {
                            method = "<clinit> - Static Initializer";
                        }

                        reference.setClazz(clazz);
                        reference.setMethod(method);
                    }

                    if (scanInfo.length == 6) {
                        reference.setForRemoval(true);
                    }

                    use.setReference(reference);
                    deprecated.getUses().add(use);
                }
            }

            if (deprecated.getUses().size() > 0) {
                applicationAssessmentResult.getDeprecatedList().add(deprecated);
            }
        } catch (Exception e) {
            log.error("Exception occurred while execute DeprecatedScanTask.parse().", e);
            ThreadLocalUtils.add(APP_SCAN_ERROR, "Command execution failed while parsing jdeprscan result.");
        }
    }
}
//end of DeprecatedScanTask.java