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

package io.playce.roro.mw.asmt.webtob;

import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.mw.asmt.AbstractMiddlewarePostProcess;
import io.playce.roro.mw.asmt.dto.DiscApplication;
import io.playce.roro.mw.asmt.dto.DiscInstanceInterface;
import io.playce.roro.mw.asmt.dto.DiscMiddlewareInstance;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.webtob.dto.WebToBAssessmentResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
@Component("WEBTOBPostProcessor")
@Slf4j
public class WebToBPostProcessor extends AbstractMiddlewarePostProcess {
    @Override
    public List<DiscInstanceInterface> getDiscoveredInstanceInterfaces(MiddlewareAssessmentResult.Instance resultInstance, DiscMiddlewareInstance discInstance) {
        if (resultInstance == null) {
            return null;
        }

        // nothing to do with interfaces
        WebToBAssessmentResult.Instance instance = (WebToBAssessmentResult.Instance) resultInstance;
        return new ArrayList<>();
    }

    @Override
    public List<DiscApplication> getDiscoveredApplications(InventoryProcessQueueItem item, MiddlewareAssessmentResult.Instance resultInstance, DiscMiddlewareInstance discInstance, GetInfoStrategy strategy) {
        if (resultInstance == null) {
            return null;
        }

        // nothing to do with applications
        WebToBAssessmentResult.Instance instance = (WebToBAssessmentResult.Instance) resultInstance;
        return new ArrayList<>();
    }

    @Override
    public String getEngineVersion(MiddlewareAssessmentResult result) {
        WebToBAssessmentResult.Engine engine = (WebToBAssessmentResult.Engine) result.getEngine();
        return engine.getVersion();
    }

    @Override
    public List<DiscMiddlewareInstance> getDiscoveredMiddlewareInstances(MiddlewareAssessmentResult result, GetInfoStrategy strategy) {
        if (result == null) {
            return null;
        }

        WebToBAssessmentResult r = (WebToBAssessmentResult) result;
        WebToBAssessmentResult.Engine engine = (WebToBAssessmentResult.Engine) r.getEngine();
        WebToBAssessmentResult.Instance instance = (WebToBAssessmentResult.Instance) r.getInstance();
        List<DiscMiddlewareInstance> resultList = new ArrayList<>();

        // node 기준으로 middleware instance add
        List<WebToBAssessmentResult.Node> nodes = instance.getNodes();
        if (CollectionUtils.isNotEmpty(nodes)) {
            for (WebToBAssessmentResult.Node node : nodes) {
                DiscMiddlewareInstance discMiddlewareInstance = new DiscMiddlewareInstance();
                discMiddlewareInstance.setMiddlewareInstanceName(node.getName());
                discMiddlewareInstance.setMiddlewareInstancePath(node.getWebTobDir());
                discMiddlewareInstance.setMiddlewareInstanceDetailDivision(node.getWebTobDir());
                discMiddlewareInstance.setMiddlewareConfigPath(null);
                discMiddlewareInstance.setMiddlewareInstanceServicePort(node.getPort());
                discMiddlewareInstance.setMiddlewareInstanceProtocol("HTTP");
                discMiddlewareInstance.setRunningUser(engine.getRunUser());
                discMiddlewareInstance.setJavaVersion(NOT_JAVA);
                discMiddlewareInstance.setRuuning((engine.getRunUser() != null && !StringUtils.isEmpty(engine.getRunUser()))); // runUser가 null이 아닌 경우 running process
                resultList.add(discMiddlewareInstance);
            }
        }

        return resultList;
    }
}