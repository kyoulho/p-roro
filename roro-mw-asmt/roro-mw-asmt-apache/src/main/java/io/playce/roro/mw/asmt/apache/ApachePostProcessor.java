/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * Dong-Heon Han    Mar 02, 2022		First Draft.
 */

package io.playce.roro.mw.asmt.apache;

import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.mw.asmt.AbstractMiddlewarePostProcess;
import io.playce.roro.mw.asmt.apache.dto.ApacheAssessmentResult;
import io.playce.roro.mw.asmt.dto.DiscApplication;
import io.playce.roro.mw.asmt.dto.DiscInstanceInterface;
import io.playce.roro.mw.asmt.dto.DiscMiddlewareInstance;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component("APACHEPostProcessor")
@Slf4j
public class ApachePostProcessor extends AbstractMiddlewarePostProcess {
    @Override
    public List<DiscInstanceInterface> getDiscoveredInstanceInterfaces(MiddlewareAssessmentResult.Instance resultInstance, DiscMiddlewareInstance discInstance) {
        if (resultInstance == null) {
            return null;
        }

        // nothing to do with interfaces
        // ApacheAssessmentResult.Instance instance = (ApacheAssessmentResult.Instance) resultInstance;
        return new ArrayList<>();
    }

    @Override
    public List<DiscApplication> getDiscoveredApplications(InventoryProcessQueueItem item, MiddlewareAssessmentResult.Instance resultInstance, DiscMiddlewareInstance discInstance, GetInfoStrategy strategy) {
        if (resultInstance == null) {
            return null;
        }

        // nothing to do with applications
        // ApacheAssessmentResult.Instance instance = (ApacheAssessmentResult.Instance) resultInstance;
        return new ArrayList<>();
    }

    @Override
    public String getEngineVersion(MiddlewareAssessmentResult result) {
        if (result == null) {
            return null;
        }

        ApacheAssessmentResult.Engine engine = (ApacheAssessmentResult.Engine) result.getEngine();
        return engine.getVersion();
    }

    @Override
    public List<DiscMiddlewareInstance> getDiscoveredMiddlewareInstances(MiddlewareAssessmentResult result, GetInfoStrategy strategy) {
        if (result == null) {
            return null;
        }

        ApacheAssessmentResult r = (ApacheAssessmentResult) result;
        // ApacheAssessmentResult.Engine engine = (ApacheAssessmentResult.Engine) r.getEngine();
        ApacheAssessmentResult.Instance instance = (ApacheAssessmentResult.Instance) r.getInstance();
        List<DiscMiddlewareInstance> resultList = new ArrayList<>();

        DiscMiddlewareInstance discMiddlewareInstance = new DiscMiddlewareInstance();
        discMiddlewareInstance.setMiddlewareInstanceName(instance.getGeneral().getServerName());
        discMiddlewareInstance.setMiddlewareInstancePath(instance.getGeneral().getInstallHome());
        discMiddlewareInstance.setMiddlewareInstanceDetailDivision(instance.getGeneral().getInstallHome());
        discMiddlewareInstance.setMiddlewareConfigPath(null);

        List<String> porList = new ArrayList<>();
        List<String> protocolList = new ArrayList<>();
        List<Integer> listenPort = instance.getGeneral().getListenPort();
        if (CollectionUtils.isNotEmpty(listenPort)) {
            // throw new RoRoException("Listen ports is null. please check the conf");
            for (int port : listenPort) {
                String portStr = String.valueOf(port);
                porList.add(portStr);
                if (portStr.contains("443")) {
                    protocolList.add("SSL");
                } else {
                    protocolList.add("HTTP");
                }
            }
        } else {
            log.debug("Listen ports is null. please check Listen port in conf files.");
        }

        boolean running = "Running".equals(instance.getGeneral().getServerStatus());
        discMiddlewareInstance.setMiddlewareInstanceServicePort(String.join(",", porList));
        discMiddlewareInstance.setMiddlewareInstanceProtocol(String.join(",", protocolList));
        if (running) {
            discMiddlewareInstance.setRunningUser(instance.getGeneral().getRunUser());
        }
        discMiddlewareInstance.setJavaVersion(NOT_JAVA);
        discMiddlewareInstance.setRuuning(running);
        resultList.add(discMiddlewareInstance);

        return resultList;
    }
}