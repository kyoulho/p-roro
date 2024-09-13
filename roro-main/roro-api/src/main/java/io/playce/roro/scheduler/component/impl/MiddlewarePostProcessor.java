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
 * Dong-Heon Han    Feb 16, 2022		First Draft.
 */

package io.playce.roro.scheduler.component.impl;

import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.common.code.Domain1006;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.jpa.entity.DiscoveredInstanceMaster;
import io.playce.roro.jpa.entity.MiddlewareInstance;
import io.playce.roro.jpa.entity.MiddlewareInstanceProtocol;
import io.playce.roro.jpa.entity.MiddlewareMaster;
import io.playce.roro.jpa.repository.MiddlewareInstanceProtocolRepository;
import io.playce.roro.jpa.repository.MiddlewareInstanceRepository;
import io.playce.roro.jpa.repository.MiddlewareMasterRepository;
import io.playce.roro.mw.asmt.MiddlewarePostProcess;
import io.playce.roro.mw.asmt.dto.*;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.scheduler.component.AbstractPostProcessor;
import io.playce.roro.scheduler.component.CommonMiddlewarePostProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MiddlewarePostProcessor extends AbstractPostProcessor {
    private final CommonMiddlewarePostProcessor commonMiddlewarePostProcessor;
    private final MiddlewareInstanceRepository middlewareInstanceRepository;
    private final MiddlewareInstanceProtocolRepository middlewareInstanceProtocolRepository;
    private final MiddlewareMasterRepository middlewareMasterRepository;

    public void setDiscoveredData(MiddlewarePostProcess postProcess, InventoryProcessQueueItem item, TargetHost targetHost, Domain1006 code, MiddlewareInventory middlewareInventory, MiddlewareAssessmentResult result, GetInfoStrategy strategy) throws InterruptedException {
        //0. set running_user = null by inventory_id
        List<MiddlewareInstance> middlewareInstances = middlewareInstanceRepository.findByMiddlewareInstanceIdIn(commonMiddlewarePostProcessor.getDiscoveredInstanceMasters(item.getInventoryId()));
        middlewareInstances.forEach(m -> m.setRunningUser(null));

        //1. get data from implementation
        List<DiscMiddlewareInstance> instances = postProcess.getDiscoveredMiddlewareInstances(result, strategy);

        for (DiscMiddlewareInstance instance : instances) {
            //2. 인스턴스가 실행중이 아니면 저장안함.
            // if (!instance.isRunning()) continue;

            setEngineVersion(item, postProcess.getEngineVersion(result));

            String instanceDetailDivision = instance.getMiddlewareInstanceDetailDivision();
            //3. Super set 저장.
            DiscoveredInstanceMaster discoveredInstanceMasterForMiddleware =
                    commonMiddlewarePostProcessor.getDiscoveredInstanceMaster(item, targetHost, code, middlewareInventory.getInventoryDetailTypeCode(), instanceDetailDivision);

            //4. middleware instance 저장.
            Long discoveredInstanceId = discoveredInstanceMasterForMiddleware.getDiscoveredInstanceId();
            MiddlewareInstance middlewareInstance = middlewareInstanceRepository.findById(discoveredInstanceId).orElse(new MiddlewareInstance());
            saveMiddlewareInstance(middlewareInstance, discoveredInstanceId, instance);

            //7. add inventory application
            List<DiscApplication> apps = postProcess.getDiscoveredApplications(item, result.getInstance(), instance, strategy);
            commonMiddlewarePostProcessor.addDiscoveredApplications(discoveredInstanceMasterForMiddleware, item, targetHost, apps);

            //8. add discovered database
            // List<DiscDatabase> databases = postProcess.getDiscoveredDatabases(item, result.getInstance(), instance);
            List<DiscInstanceInterface> instanceInterfaces = postProcess.getDiscoveredInstanceInterfaces(result.getInstance(), instance);
            List<DiscDatabase> databases = commonMiddlewarePostProcessor.saveInterface(discoveredInstanceMasterForMiddleware, instanceInterfaces);
            commonMiddlewarePostProcessor.addDiscoveredDatabases(discoveredInstanceMasterForMiddleware, item, databases);
        }
    }

    private void setEngineVersion(InventoryProcessQueueItem item, String engineVersion) {
        MiddlewareMaster master = middlewareMasterRepository.findById(item.getInventoryId()).orElse(new MiddlewareMaster());
        master.setEngineVersion(engineVersion);
        middlewareMasterRepository.save(master);
    }


    private void saveMiddlewareInstance(MiddlewareInstance instance, Long discoveredInstanceId, DiscMiddlewareInstance discMWInstance) {
        instance.setMiddlewareInstanceId(discoveredInstanceId);
        instance.setMiddlewareInstanceName(discMWInstance.getMiddlewareInstanceName());
        instance.setMiddlewareInstancePath(discMWInstance.getMiddlewareInstancePath());
        instance.setMiddlewareConfigPath(discMWInstance.getMiddlewareConfigPath());
        instance.setRunningUser(discMWInstance.getRunningUser());

        //성공한 스캔이 존재하지만 이후 죽은상태에서 다시스캔했을때.. 기존 java version 을 살린다.
        String javaVersion = discMWInstance.getJavaVersion();
        if (StringUtils.isNotEmpty(javaVersion)) {
            instance.setJavaVersion(javaVersion);
        }
        instance.setRegistUserId(WebUtil.getUserId());
        instance.setRegistDatetime(new Date());

        middlewareInstanceRepository.save(instance);

        String portStr = discMWInstance.getMiddlewareInstanceServicePort();
        String protoCol = discMWInstance.getMiddlewareInstanceProtocol();
        if (protoCol != null) {
            saveMiddlewareInstanceProtocol(instance.getMiddlewareInstanceId(), portStr, protoCol);
        }
    }

    private void saveMiddlewareInstanceProtocol(Long middlewareInstanceId, String portStr, String protoCol) {

        String[] ports = portStr.split(",");
        String[] protocols = protoCol.split(",");
        for (int i = 0; i < ports.length; i++) {
            int port = -1;
            try {
                port = Integer.parseInt(ports[i]);
            } catch (NumberFormatException ignored) {}
            MiddlewareInstanceProtocol middlewareInstanceProtocol = middlewareInstanceProtocolRepository
                    .findByMiddlewareInstanceIdAndMiddlewareInstanceServicePort(middlewareInstanceId, port).orElse(new MiddlewareInstanceProtocol());
            middlewareInstanceProtocol.setMiddlewareInstanceId(middlewareInstanceId);
            middlewareInstanceProtocol.setMiddlewareInstanceServicePort(port);
            middlewareInstanceProtocol.setProtocol(protocols[i]);
            middlewareInstanceProtocolRepository.save(middlewareInstanceProtocol);
        }
    }
}