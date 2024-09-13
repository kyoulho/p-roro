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
 * Hoon Oh       1월 27, 2022            First Draft.
 */
package io.playce.roro.discover.middleware.detector.impl;

import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.code.Domain1102;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.discover.middleware.detector.AbstractDetector;
import io.playce.roro.discover.middleware.dto.DetectResultInfo;
import io.playce.roro.discover.middleware.scenario.ExtractScenario;
import io.playce.roro.discover.middleware.scenario.apache.ApacheEnginePathScenario;
import io.playce.roro.discover.middleware.scenario.apache.ApacheHttpdParentDirScenario;
import io.playce.roro.discover.middleware.scenario.apache.ApacheInstancePathScenario;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Slf4j
public class ApacheDetector extends AbstractDetector {

    public ApacheDetector(Process process) {
        super(process);
    }

    @Override
    public DetectResultInfo generateMiddleware(TargetHost targetHost, InventoryProcessConnectionInfo connectionInfo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {

        ExtractScenario httpdParentScenario = generateHttpdParentScenario();

        String httpdParentDir = httpdParentScenario.execute(process, commandConfig, strategy);

        ExtractScenario enginePathScenarios = generateEnginePathScenario(targetHost, httpdParentDir);
        String enginePath = enginePathScenarios.execute(process, commandConfig, strategy);

//        ExtractScenario resourceNameScenarios = generateResourceNameScenario();
        ExtractScenario instancePathScenarios = generateInstancePathScenario(targetHost, httpdParentDir, enginePath);
//        ExtractScenario solutionTypeScenarios = generateSolutionTypeScenario(targetHost,httpdParentDir,enginePath);

//        String nodeName = resourceNameScenarios.execute(process);
        String instancePath = instancePathScenarios.execute(process, commandConfig, strategy);
//        if(StringUtils.isEmpty(nodeName)) {
//            log.error("The node name could not be found. {}", process);
//        }

        if ("/".equals(instancePath)) {
            instancePath = null;
        }

        if (StringUtils.isEmpty(enginePath)) {
            log.debug("The engine path could not be found. {}", process);
        }
        if (StringUtils.isEmpty(instancePath)) {
            log.error("The instance path could not be found. {}", process);
        }

        // generate httpd vendor name
        String vendorName = generateVendor(targetHost, enginePath, httpdParentDir, commandConfig, strategy);

        return DetectResultInfo.builder()
                .vendor(vendorName)
                .mwDetailType(Domain1013.APACHE)
                .mwType(Domain1102.WEB)
                .pid(process.getPid())
                .runUser(process.getUser())
                // .nodeName(nodeName)
                .enginePath(enginePath)
                .domainPath(instancePath)
                .instancePath(instancePath)
                // .javaVersion(getJavaVersion(process.getCmd(), targetHost, commandConfig, strategy))
                .javaVersion(null)
                .build();
    }

    private ExtractScenario generateHttpdParentScenario() {
        return new ApacheHttpdParentDirScenario();
    }

    /*private ExtractScenario generateSolutionTypeScenario(TargetHost targetHost, String httpdParentDir, String solutionPath) {
        ExtractScenario scenario = new ApacheSolutionTypeScenario(httpdParentDir,solutionPath).new Step1();
        scenario.setNext(new ApacheSolutionTypeScenario(httpdParentDir,solutionPath).new Step2());
        scenario.setIgnoreCases(getIgnoreCase());
        scenario.setTargetHost(targetHost);
        return scenario;
    }*/

    private ExtractScenario generateInstancePathScenario(TargetHost targetHost, String httpdParentDir, String enginePath) {
        ExtractScenario scenario = new ApacheInstancePathScenario(httpdParentDir, enginePath).new Step1();
        ExtractScenario stepYumInstall = new ApacheInstancePathScenario(httpdParentDir, enginePath).new StepYumInstall();
        ExtractScenario stepAptInstall = new ApacheInstancePathScenario(httpdParentDir, enginePath).new StepAptInstall();

        ExtractScenario step2 = new ApacheInstancePathScenario(httpdParentDir, enginePath).new Step2();
//        ExtractScenario step3 = new ApacheInstancePathScenario(httpdParentDir,enginePath).new Step3();
//        ExtractScenario step4 = new ApacheInstancePathScenario(httpdParentDir,enginePath).new Step4();
        ExtractScenario step5 = new ApacheInstancePathScenario(httpdParentDir, enginePath).new Step5();
        ExtractScenario step6 = new ApacheInstancePathScenario.Step6();
//        ExtractScenario.setChain(targetHost, scenario, step2, step3, step4, step5, step6);
        ExtractScenario.setChain(targetHost, scenario, stepYumInstall, stepAptInstall, step2, step5, step6);
        scenario.setIgnoreCases(getIgnoreCase());

        return scenario;
    }

    private List<String> getIgnoreCase() {
        return List.of("-");
    }

//    private ExtractScenario generateResourceNameScenario(){
//        return new ApacheNameScenario();
//    }

    private ExtractScenario generateEnginePathScenario(TargetHost targetHost, String httpdParentDir) {
        ExtractScenario scenario = new ApacheEnginePathScenario(httpdParentDir).new Step1();
        ExtractScenario step2 = new ApacheEnginePathScenario(httpdParentDir).new Step2();
        ExtractScenario.setChain(targetHost, scenario, step2);
        scenario.setIgnoreCases(getIgnoreCase());
        return scenario;
    }

    /**
     * generated Apache httpd Vendor
     */
    private String generateVendor(TargetHost targetHost, String enginePath, String httpdParentDir, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        String vendor = "Unknown";
        String separator = strategy.getSeparator();
        if (StringUtils.isNotEmpty(httpdParentDir)) {
//            String command = "sudo " + enginePath + httpdParentDir + "/httpd -v";
//            String commandResult = SSHUtil.executeCommand(targetHost, command);
            String command = enginePath + httpdParentDir + separator + "httpd";
            String commandResult = MWCommonUtil.getExecuteResult(targetHost, COMMAND.APACHE_VERSION, commandConfig, strategy, command);

            if (StringUtils.isEmpty(commandResult) || commandResult.contains("not")) {
//                command = "sudo " + enginePath + httpdParentDir + "/apache2 -v";
//                commandResult = SSHUtil.executeCommand(targetHost, command);
                command = enginePath + httpdParentDir + separator + "apache2";
                commandResult = MWCommonUtil.getExecuteResult(targetHost, COMMAND.APACHE_VERSION, commandConfig, strategy, command);
            }

            if (StringUtils.isEmpty(commandResult) || commandResult.contains("not")) {
//                command = "sudo " + enginePath + httpdParentDir + "/apachectl -v";
//                commandResult = SSHUtil.executeCommand(targetHost, command);
                command = enginePath + httpdParentDir + separator + "apachectl";
                commandResult = MWCommonUtil.getExecuteResult(targetHost, COMMAND.APACHE_VERSION, commandConfig, strategy, command);
            }

            if (StringUtils.isEmpty(commandResult) || commandResult.contains("not")) {
//                command = "sudo " + enginePath + httpdParentDir + "/apachectl -v";
//                commandResult = SSHUtil.executeCommand(targetHost, command);
                command = enginePath + httpdParentDir + separator + "lighttpd";
                commandResult = MWCommonUtil.getExecuteResult(targetHost, COMMAND.APACHE_VERSION, commandConfig, strategy, command);
            }

            for (String line : commandResult.split(strategy.getCarriageReturn())) {
                if (line.contains("Oracle")) {
                    vendor = "Oracle";
                } else if (line.contains("IBM")) {
                    vendor = "IBM";
                } else if (line.contains("Apache")) {
                    vendor = "Apache";
                } else if (line.contains("lighttpd")) {
                    vendor = "Lighttpd";
                }
            }
        }

        if ("Unknown".equals(vendor)) {
            if (enginePath.toLowerCase().contains("ohs") || httpdParentDir.toLowerCase().contains("ohs")) {
                vendor = "Oracle";
            } else if (enginePath.toLowerCase().contains("ibm") || enginePath.toLowerCase().contains("websphere") ||
                    httpdParentDir.toLowerCase().contains("ibm") || httpdParentDir.toLowerCase().contains("websphere")) {
                vendor = "IBM";
            }
        }

        return vendor;
    }
}
//end of ApacheDetector.java