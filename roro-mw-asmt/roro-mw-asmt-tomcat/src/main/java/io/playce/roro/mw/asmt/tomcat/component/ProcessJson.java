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
 * Dong-Heon Han    Feb 13, 2022		First Draft.
 */

package io.playce.roro.mw.asmt.tomcat.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.enums.TOMCAT_DEPLOY_APP;
import io.playce.roro.mw.asmt.tomcat.dto.TomcatAssessmentResult;
import io.playce.roro.mw.asmt.tomcat.enums.NODE;
import io.playce.roro.mw.asmt.tomcat.enums.attribute.*;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static io.playce.roro.common.util.ThreadLocalUtils.MW_SCAN_ERROR;

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
@Slf4j
public class ProcessJson {
    private final ProcessRemote processRemote;
    private final XmlMapper xmlMapper;

    public JsonNode getJsonNode(TomcatAssessmentResult.ConfigFile tc) {
        JsonNode serverXml = null;
        try {
            serverXml = xmlMapper.readTree(tc.getContents());
        } catch (JsonProcessingException e) {
            log.error("{} parse error -> {}", tc.getPath(), e);
            ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while json parsing error. Detail : [" + e.getMessage() + "]");
        }
        return serverXml;
    }

    public void extractConnectorsFromService(TomcatAssessmentResult.Instance instance, JsonNode service, String serviceName, GetInfoStrategy strategy) {
        JsonNode node = service.at(NODE.CONNECTOR.path());
        if (node.isMissingNode())
            return;

        List<Map<String, String>> list = JsonUtil.getNodeValueFromJsonNode(node, Arrays.stream(CONNECTOR.values()).map(Enum::name).collect(Collectors.toList()));
        for (Map<String, String> map : list) {
            map.put("serviceName", serviceName);
            String httpPort = map.get(CONNECTOR.port.name());
            findConnectorPort(httpPort, instance.getOptions(), CONNECTOR.port, map, strategy);

            String sslPort = map.get(CONNECTOR.redirectPort.name());
            findConnectorPort(sslPort, instance.getOptions(), CONNECTOR.redirectPort, map, strategy);
        }
        List<Map<String, String>> connectors = instance.getConnectors();
        if (connectors == null) {
            connectors = list;
        } else {
            connectors.addAll(list);
        }
        instance.setConnectors(connectors);
    }

    private void findConnectorPort(String variable, List<String> options, CONNECTOR connector, Map<String, String> map, GetInfoStrategy strategy) {
        if (variable == null)
            return;
        if (!strategy.checkVariable(variable))
            return;
        if (variable.length() < 3)
            return;

        variable = variable.substring(2, variable.length() - 1);
        for (String option : options) {
            if (option.contains(variable)) {
                int index = option.indexOf("=");
                String value = option.substring(index + 1);
                map.put(connector.name(), value);
            }
        }
    }

    public void extractContextResourcesFromContextXml(TomcatAssessmentResult.Instance instance, JsonNode contextXml) {
        JsonNode node = contextXml.at(NODE.RESOURCE.path());
        if (node.isMissingNode())
            return;

        List<Map<String, String>> list = JsonUtil.getNodeValueFromJsonNode(node, Arrays.stream(RESOURCE.values()).map(Enum::name).collect(Collectors.toList()));
        instance.setResources(list);
    }

    private enum GLOBAL_RESOURCES {
        name, auth, type, description, factory, pathname, url
    }

    public void extractGlobalResourcesFromServerXml(TomcatAssessmentResult.Instance instance, JsonNode serverXml) {
        JsonNode node = serverXml.at(NODE.GLOBALRESOURCE.path());
        if (node.isMissingNode())
            return;

        List<Map<String, String>> list = JsonUtil.getNodeValueFromJsonNode(node, Arrays.stream(GLOBAL_RESOURCES.values()).map(Enum::name).collect(Collectors.toList()));
        instance.setGlobalResources(list);
    }

    public void extractExecutorFromService(TomcatAssessmentResult.Instance instance, JsonNode service) {
        //getExecutor
        JsonNode node = service.at(NODE.EXECUTOR.path());
        if (node.isMissingNode())
            return;

        List<Map<String, String>> list = JsonUtil.getNodeValueFromJsonNode(node, Arrays.stream(EXECUTOR.values()).map(Enum::name).collect(Collectors.toList()));
        List<Map<String, String>> executors = instance.getExecutors();
        if (executors == null) {
            executors = list;
        } else {
            executors.addAll(list);
        }
        instance.setExecutors(executors);
    }

    public void extractApplicationsFromService(TargetHost targetHost, TomcatAssessmentResult.Engine engine, TomcatAssessmentResult.Instance instance, JsonNode service, String serviceName, boolean sudo, GetInfoStrategy strategy) throws InterruptedException {
        String separator = strategy.getSeparator();
        JsonNode node = service.at(NODE.HOST.path());
        if (node.isMissingNode())
            return;

        String root = engine.getPath().equals(instance.getPath()) ? engine.getPath() : instance.getPath();

        List<Map<String, String>> list = JsonUtil.getNodeValueFromJsonNode(node, Arrays.stream(HOST.values()).map(Enum::name).collect(Collectors.toList()));
        List<TomcatAssessmentResult.Applications> applications = instance.getApplications();
        if (applications == null) {
            instance.setApplications(new ArrayList<>());
        }
        List<Map<String, String>> deployApps = instance.getDeployApps();
        if (deployApps == null) {
            instance.setDeployApps(new ArrayList<>());
        }
        for (Map<String, String> map : list) {
            TomcatAssessmentResult.Applications application = new TomcatAssessmentResult.Applications();
            TomcatAssessmentResult.Webapps webapps = createWebApp(targetHost, engine, instance, map, sudo, strategy);
            application.setServiceName(serviceName);
            application.setWebapps(webapps);
            instance.getApplications().add(application);

            // Host tage, appBase
            String basePath = webapps.getBasePath();
            basePath = strategy.isAbstractPath(basePath) ? basePath : root + separator + basePath;
            String autoDeploy = map.get(HOST.autoDeploy.name());
            autoDeploy = autoDeploy == null ? "false" : autoDeploy;

            //deploy apps
            Map<String, Map<String, String>> deployedAppMap = new HashMap<>();
            for (String app : webapps.getApps()) {
                Map<String, String> directoryApp = new HashMap<>();
                directoryApp.put(TOMCAT_DEPLOY_APP.serviceName.name(), serviceName);
                directoryApp.put(TOMCAT_DEPLOY_APP.contextPath.name(), app.equals("ROOT") ? "/" : "/" + app);
                directoryApp.put(TOMCAT_DEPLOY_APP.autoDeploy.name(), autoDeploy);
                directoryApp.put(TOMCAT_DEPLOY_APP.relodable.name(), "false");
                directoryApp.put(TOMCAT_DEPLOY_APP.deployLocation.name(), basePath + separator + app);
                //dploylocation base keep
                deployedAppMap.put(directoryApp.get(TOMCAT_DEPLOY_APP.deployLocation.name()), directoryApp);
            }

            JsonNode contexts = service.at(NODE.CONTEXT.path());
            if (contexts.isMissingNode()) {
                instance.getDeployApps().addAll(deployedAppMap.values());
                continue;
            }

            List<Map<String, String>> contextMaps = JsonUtil.getNodeValueFromJsonNode(contexts, Arrays.stream(CONTEXT.values()).map(Enum::name).collect(Collectors.toList()));
            application.setContext(Arrays.asList(contextMaps.toArray()));

            for (Map<String, String> contextMap : contextMaps) {
                String docBase = contextMap.get(CONTEXT.docBase.name());

                // https://cloud-osci.atlassian.net/browse/ROROQA-1045
                if (StringUtils.isEmpty(docBase)) {
                    continue;
                }

                docBase = strategy.isAbstractPath(docBase) ? docBase : basePath + File.separator + docBase;
                if (!deployedAppMap.containsKey(docBase)) {
                    deployedAppMap.put(docBase, new HashMap<>());
                }
                Map<String, String> contextApp = deployedAppMap.get(docBase);
                contextApp.put(TOMCAT_DEPLOY_APP.serviceName.name(), serviceName);
                contextApp.put(TOMCAT_DEPLOY_APP.contextPath.name(), contextMap.get(CONTEXT.path.name()));
                contextApp.put(TOMCAT_DEPLOY_APP.autoDeploy.name(), autoDeploy);
                contextApp.put(TOMCAT_DEPLOY_APP.relodable.name(), contextMap.get(CONTEXT.reloadable.name()));
                contextApp.put(TOMCAT_DEPLOY_APP.deployLocation.name(), docBase);
            }
            instance.getDeployApps().addAll(deployedAppMap.values());
        }
    }

    private TomcatAssessmentResult.Webapps createWebApp(TargetHost targetHost, TomcatAssessmentResult.Engine engine, TomcatAssessmentResult.Instance instance, Map<String, String> map, boolean sudo, GetInfoStrategy strategy) throws InterruptedException {
        TomcatAssessmentResult.Webapps webapps = new TomcatAssessmentResult.Webapps();
        webapps.setBasePath(map.get(HOST.appBase.name()));
        webapps.setUnpackWARs(map.get(HOST.unpackWARs.name()));
        webapps.setAutoDeploy(map.get(HOST.autoDeploy.name()));
        webapps.setApps(new ArrayList<>());

        processRemote.loadDirectories(targetHost, engine.getPath(), instance.getPath(), webapps, sudo, strategy);
        return webapps;
    }
}