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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.jpa.entity.k8s.*;
import io.playce.roro.jpa.repository.k8s.IngressRepository;
import io.playce.roro.jpa.repository.k8s.IngressRuleTargetRepository;
import io.playce.roro.jpa.repository.k8s.IngressRuleRepository;
import io.playce.roro.k8s.handler.CommandResultHandler;
import io.playce.roro.k8s.handler.CommonHandler;
import io.playce.roro.k8s.parser.Parser;
import io.playce.roro.k8s.parser.ParserManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IngressHandler implements CommandResultHandler {
    private final CommonHandler commonHandler;
    private final ParserManager parserManager;
    private final IngressRepository ingressRepository;
    private final IngressRuleRepository ingressRuleRepository;
    private final IngressRuleTargetRepository ingressRuleTargetRepository;

    @RequiredArgsConstructor
    private enum R {
        name("/spec/ingressClassName"),
        rules("/spec/rules"),
        tls("/spec/tls"),
        host("/host"),
        paths("/http/paths"),
        serviceName("/backend/service/name"),
        servicePortName("/backend/service/port/name"),
        servicePortNumber("/backend/service/port/number"),
        pathsPath("/path"),
        pathsPathType("/pathType"),
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
        for (JsonNode ingress : items) {
            Long objectId = commonHandler.setObjectInfo(clusterScanId, ingress);
            saveIngress(ingress, objectId);
            saveIngressRulesAndIngressRuleHosts(objectId, ingress.at(R.rules.path), ingress.at(R.tls.path));
        }
    }


    @SneakyThrows
    private void saveIngressRulesAndIngressRuleHosts(Long objectId, JsonNode rules, JsonNode tlses) {
        for (JsonNode rule : rules) {
            IngressRule ingressRule = new IngressRule();
            ingressRule.setObjectId(objectId);
            String host = rule.at(R.host.path).textValue();
            ingressRule.setHost(host);
            ingressRule.setSecretName(getSecretName(tlses, host));
            ingressRuleRepository.save(ingressRule);


            JsonNode paths = rule.at(R.paths.path);
            for (JsonNode path : paths) {
                IngressRuleTarget ruleHost = new IngressRuleTarget();
                ruleHost.setServiceName(path.at(R.serviceName.path).textValue());
                ruleHost.setServicePortName(path.at(R.servicePortName.path).textValue());
                ruleHost.setServicePortNumber(path.at(R.servicePortNumber.path).intValue());
                ruleHost.setPath(path.at(R.pathsPath.path).textValue());
                ruleHost.setPathType(path.at(R.pathsPathType.path).textValue());
                ruleHost.setIngressRuleId(ingressRule.getIngressRuleId());
                ingressRuleTargetRepository.save(ruleHost);
            }
        }
    }

    private String getSecretName(JsonNode tlsList, String host) throws InterruptedException {
        for (JsonNode tls : tlsList) {
            List<String> hosts = JsonUtil.convertValue(tls.at("/hosts"), new TypeReference<>() {
            });
            if (hosts.contains(host)) {
                return tls.at("/secretName").textValue();
            }
        }
        return null;
    }

    private void saveIngress(JsonNode ingress, Long objectId) {
        Ingress entity = new Ingress();
        entity.setClassName(ingress.at(R.name.path).textValue());
        entity.setObjectId(objectId);
        ingressRepository.save(entity);
    }

    @Override
    public void setRelation(JsonNode resultData) {
    }
}