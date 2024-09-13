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
 * Jhpark       8월 13, 2022            First Draft.
 */
package io.playce.roro.mw.asmt.jboss.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.jboss.dto.JbossAssessmentResult;
import io.playce.roro.mw.asmt.jboss.strategy.enums.StrategyName;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;

/**
 * <pre>
 *
 * </pre>
 *
 * @author jhpark
 * @version 3.0
 */
public interface ServerModeStrategy {
    StrategyName getModeName();

    void setEngineInfo(TargetHost targetHost, JbossAssessmentResult.Engine engine, boolean sudo, GetInfoStrategy strategy) throws InterruptedException;
    void setInstanceVmInfo(TargetHost targetHost, JbossAssessmentResult.Engine engine, boolean sudo, GetInfoStrategy strategy, JbossAssessmentResult.Instance instance) throws InterruptedException;

    void setInstanceLocalConfigFiles(String configFilePath, JbossAssessmentResult.Instance instance, JbossAssessmentResult.Engine engine);

    void setInstanceRemoteConfigFiles(TargetHost targetHost, JbossAssessmentResult.Engine engine, JbossAssessmentResult.Instance instance, boolean sudo, GetInfoStrategy strategy) throws InterruptedException;

    void setConnectors(JbossAssessmentResult.Instance instance, JsonNode serverXml, JsonNode hostFile, GetInfoStrategy strategy, TargetHost targetHost, boolean sudo) throws InterruptedException;

    void setServers(JbossAssessmentResult.Instance instance, JsonNode serverXml, JsonNode hostFile, GetInfoStrategy strategy,TargetHost targetHost, JbossAssessmentResult.Engine engine, boolean sudo)throws InterruptedException;

    void setInterFaces(JbossAssessmentResult.Instance instance, JsonNode serverXml, GetInfoStrategy strategy);

    void setExtensions(JbossAssessmentResult.Instance instance, JsonNode serverXml, GetInfoStrategy strategy) throws InterruptedException;

    void setExecutorServer(JbossAssessmentResult.Instance instance, JsonNode serverXml);

    void setApplications(TargetHost targetHost, JbossAssessmentResult.Engine engine, JbossAssessmentResult.Instance instance, JsonNode serverXml, boolean sudo, GetInfoStrategy strategy) throws InterruptedException;

    void setResources(JbossAssessmentResult.Instance instance, JsonNode serverXml);

    void setRunUser(TargetHost targetHost, JbossAssessmentResult.Instance instance, boolean sudo, GetInfoStrategy strategy) throws InterruptedException;

    void setJavaVersion(TargetHost targetHost, JbossAssessmentResult.Instance instance, GetInfoStrategy strategy,JbossAssessmentResult.Engine engine) throws InterruptedException;

    void setJavaVendor(TargetHost targetHost, JbossAssessmentResult.Instance instance, GetInfoStrategy strategy, JbossAssessmentResult.Engine engine) throws InterruptedException;

    void findConfigFileNameFromCmd(TargetHost targetHost, JbossAssessmentResult.Engine engine, boolean sudo, GetInfoStrategy strategy, JbossAssessmentResult.Instance instance) throws InterruptedException;

}

