/*
 * Copyright 2023 The playce-roro-k8s-assessment Project.
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
 * Dong-Heon Han    Jul 13, 2023		First Draft.
 */

package io.playce.roro.k8s.handler.impl;

import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.jpa.entity.k8s.ClusterIp;
import io.playce.roro.jpa.entity.k8s.K8sService;
import io.playce.roro.jpa.entity.k8s.K8sServicePort;
import io.playce.roro.jpa.entity.k8s.Selector;
import io.playce.roro.jpa.repository.k8s.ClusterIpRepository;
import io.playce.roro.jpa.repository.k8s.K8sServicePortRepository;
import io.playce.roro.jpa.repository.k8s.K8sServiceRepository;
import io.playce.roro.jpa.repository.k8s.SelectorRepository;
import io.playce.roro.k8s.enums.Base;
import io.playce.roro.k8s.handler.CommandResultHandler;
import io.playce.roro.k8s.handler.CommonHandler;
import io.playce.roro.k8s.parser.Parser;
import io.playce.roro.k8s.parser.ParserManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class K8sServiceHandler implements CommandResultHandler {
    private final CommonHandler commonHandler;
    private final ParserManager parserManager;
    private final K8sServiceRepository k8sServiceRepository;
    private final K8sServicePortRepository k8sServicePortRepository;
    private final ClusterIpRepository clusterIpRepository;
    private final SelectorRepository selectorRepository;

    @RequiredArgsConstructor
    private enum R {
        clusterIP("/spec/clusterIP"),
        clusterIPs("/spec/clusterIPs"),
        ports("/spec/ports"),
        selector("/spec/selector"),
        type("/spec/type"),
        ;
        private final String path;
    }

    @Override
    public JsonNode parse(String result, String parserName) {
        Parser parser = parserManager.getParser(parserName);
        return parser.parse(result);
    }

    @Override
    public void saveData(Long clusterScanId, JsonNode resultData) {
        JsonNode items = resultData.get("items");
        for (JsonNode service : items) {
            Long objectId = commonHandler.setObjectInfo(clusterScanId, service);
            saveService(service, objectId);
            saveServicePort(service.at(R.ports.path), objectId);
            saveSelector(service.at(R.selector.path), objectId);
            saveClusterIps(service.at(R.clusterIPs.path), objectId);
        }
    }

    private void saveClusterIps(JsonNode clusterIps, Long objectId) {
        for (JsonNode clusterIp : clusterIps) {
            ClusterIp entity = new ClusterIp();
            entity.setIp(clusterIp.textValue());
            entity.setObjectId(objectId);
            clusterIpRepository.save(entity);
        }
    }

    private void saveSelector(JsonNode selector, Long objectId) {
        Map<String, String> nameValues = commonHandler.getNameValues(selector);
        for (String name : nameValues.keySet()) {
            Selector entity = new Selector();
            entity.setName(name);
            entity.setValue(nameValues.get(name));
            entity.setObjectId(objectId);
            selectorRepository.save(entity);
        }
    }

    private void saveServicePort(JsonNode ports, Long objectId) {
        for (JsonNode port : ports) {
            K8sServicePort servicePort = new K8sServicePort();
            if (port.has("name")) {
                servicePort.setName(port.get("name").textValue());
            }
            if (port.has("nodePort")) {
                servicePort.setNodePort(port.get("nodePort").intValue());
            }
            servicePort.setPort(port.get("port").intValue());
            servicePort.setProtocol(port.get("protocol").textValue());
            servicePort.setTargetPort(port.get("targetPort").asText());
            servicePort.setObjectId(objectId);
            k8sServicePortRepository.save(servicePort);
        }
    }

    private void saveService(JsonNode service, Long objectId) {
        K8sService k8sService = new K8sService();
        k8sService.setObjectId(objectId);
        k8sService.setName(service.at(Base.name.getPath()).textValue());
        k8sService.setClusterIp(service.at(R.clusterIP.path).textValue());
        k8sService.setType(service.at(R.type.path).textValue());
        k8sServiceRepository.save(k8sService);
    }

    @Override
    public void setRelation(JsonNode resultData) {
    }
}