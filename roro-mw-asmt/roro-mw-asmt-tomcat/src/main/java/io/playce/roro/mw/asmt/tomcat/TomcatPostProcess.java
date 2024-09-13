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
 * Dong-Heon Han    Feb 17, 2022		First Draft.
 */

package io.playce.roro.mw.asmt.tomcat;

import io.playce.roro.common.code.Domain1109;
import io.playce.roro.common.dto.info.JdbcInfo;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.util.JdbcURLParser;
import io.playce.roro.mw.asmt.AbstractMiddlewarePostProcess;
import io.playce.roro.mw.asmt.dto.DiscApplication;
import io.playce.roro.mw.asmt.dto.DiscInstanceInterface;
import io.playce.roro.mw.asmt.dto.DiscMiddlewareInstance;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.enums.TOMCAT_DEPLOY_APP;
import io.playce.roro.mw.asmt.tomcat.dto.TomcatAssessmentResult;
import io.playce.roro.mw.asmt.tomcat.enums.attribute.CONNECTOR;
import io.playce.roro.mw.asmt.tomcat.enums.attribute.RESOURCE;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component("TOMCATPostProcessor")
@Slf4j
public class TomcatPostProcess extends AbstractMiddlewarePostProcess {
    @Override
    public List<DiscInstanceInterface> getDiscoveredInstanceInterfaces(MiddlewareAssessmentResult.Instance resultInstance, DiscMiddlewareInstance discInstance) {
        if (resultInstance == null) {
            return null;
        }

        TomcatAssessmentResult.Instance instance = (TomcatAssessmentResult.Instance) resultInstance;
        return getDiscInstanceInterfaces(instance);
    }

    @NotNull
    private List<DiscInstanceInterface> getDiscInstanceInterfaces(TomcatAssessmentResult.Instance instance) {
        if (instance.getOptions() != null) {
            Map<String, String> datasource = null;
            for (String opt : instance.getOptions()) {
                if (opt.toLowerCase().contains("datasource")) {
                    if (datasource == null) {
                        datasource = new HashMap<>();
                        datasource.put(RESOURCE.type.name(), "DataSource");
                    }

                    if (opt.toLowerCase().contains("url")) {
                        String jdbcUrl = opt.split("=")[1];

                        try {
                            List<JdbcInfo> parse = JdbcURLParser.parse(jdbcUrl);

                            // TODO Multiple Datasource 처리
                            if (!parse.isEmpty()) {
                                datasource.put(RESOURCE.name.name(), parse.get(0).getDatabase());
                            }
                        } catch (Exception e) {
                            // ignore
                        }

                        datasource.put(RESOURCE.url.name(), jdbcUrl);
                    }

                    if (opt.toLowerCase().contains("username")) {
                        datasource.put(RESOURCE.username.name(), opt.split("=")[1]);
                    }

                    if (opt.toLowerCase().contains("password")) {
                        datasource.put(RESOURCE.password.name(), opt.split("=")[1]);
                    }
                }
            }

            if (datasource != null) {
                if (instance.getResources() == null) {
                    instance.setResources(new ArrayList<>());
                }

                instance.getResources().add(datasource);
            }
        }

        List<DiscInstanceInterface> resultList = new ArrayList<>();
        extractInstanceInterface(resultList, instance.getGlobalResources());
        extractInstanceInterface(resultList, instance.getResources());
        return resultList;
    }

    private void extractInstanceInterface(List<DiscInstanceInterface> resultList, List<Map<String, String>> resources) {
        if (resources == null) {
            resources = new ArrayList<>();
        }

        for (Map<String, String> resource : resources) {
            String value = resource.get(RESOURCE.type.name());
            if (!value.contains("DataSource")) {
                continue;
            }

            DiscInstanceInterface discInstanceInterface = new DiscInstanceInterface();

            if (resource.get(RESOURCE.url.name()).contains("jdbc:") && !resource.get(RESOURCE.name.name()).toLowerCase().contains("jndi") && !resource.get(RESOURCE.name.name()).contains("/")) {
                discInstanceInterface.setDiscoveredInstanceDetailTypeCode(Domain1109.JDBC.name());
            } else {
                discInstanceInterface.setDiscoveredInstanceDetailTypeCode(Domain1109.JNDI.name());
            }
            discInstanceInterface.setDescriptorsName(resource.get(RESOURCE.name.name()));
            discInstanceInterface.setFullDescriptors(resource.get(RESOURCE.url.name()));
            discInstanceInterface.setUsername(resource.get(RESOURCE.username.name()));
            discInstanceInterface.setPassword(resource.get(RESOURCE.password.name()));
            resultList.add(discInstanceInterface);
        }
    }

    @Override
    public List<DiscApplication> getDiscoveredApplications(InventoryProcessQueueItem item, MiddlewareAssessmentResult.Instance resultInstance, DiscMiddlewareInstance discInstance, GetInfoStrategy strategy) {
        if (resultInstance == null)
            return null;

        TomcatAssessmentResult.Instance instance = (TomcatAssessmentResult.Instance) resultInstance;
        List<DiscApplication> resultList = new ArrayList<>();
        for (Map<String, String> deployedApp : instance.getDeployApps()) {
            DiscApplication applicationInstance = new DiscApplication();
            String contextPath = deployedApp.get(TOMCAT_DEPLOY_APP.contextPath.name());
            String application = StringUtils.isEmpty(contextPath) || contextPath.equals("/") ? "ROOT" : contextPath.substring(1);
            String autoDeploy = deployedApp.get(TOMCAT_DEPLOY_APP.autoDeploy.name());
            String reloadable = deployedApp.get(TOMCAT_DEPLOY_APP.relodable.name());
            applicationInstance.setApplication(application);
            applicationInstance.setDeployPath(deployedApp.get(TOMCAT_DEPLOY_APP.deployLocation.name()));
            applicationInstance.setContextPath(contextPath);
            applicationInstance.setAutoDeployYn(autoDeploy == null || autoDeploy.equals("false") ? "N" : "Y");
            applicationInstance.setReloadableYn(reloadable == null || reloadable.equals("false") ? "N" : "Y");
            resultList.add(applicationInstance);
        }
        return resultList;
    }

    @Override
    public String getEngineVersion(MiddlewareAssessmentResult result) {
        TomcatAssessmentResult.Engine engine = (TomcatAssessmentResult.Engine) result.getEngine();
        return engine.getVersion();
    }

    @Override
    public List<DiscMiddlewareInstance> getDiscoveredMiddlewareInstances(MiddlewareAssessmentResult result, GetInfoStrategy strategy) {
        if (result == null)
            return null;
        String separator = strategy.getSeparator();

        TomcatAssessmentResult r = (TomcatAssessmentResult) result;
        TomcatAssessmentResult.Instance instance = (TomcatAssessmentResult.Instance) r.getInstance();
        List<DiscMiddlewareInstance> resultList = new ArrayList<>();

        String instancePath = instance.getPath();
        int index = instancePath.lastIndexOf(separator);
        String instanceName = index == -1 ? instancePath : instancePath.substring(index + 1);

        DiscMiddlewareInstance discMiddlewareInstance = new DiscMiddlewareInstance();
        discMiddlewareInstance.setMiddlewareInstanceName(instanceName);
        discMiddlewareInstance.setMiddlewareInstancePath(instance.getPath());
        discMiddlewareInstance.setMiddlewareInstanceDetailDivision(instance.getPath());
//        discMiddlewareInstance.setMiddlewareConfigPath(instance.getPath());
        String ports = instance.getConnectors().stream().map(m -> m.get(CONNECTOR.port.name())).collect(Collectors.joining(","));
        discMiddlewareInstance.setMiddlewareInstanceServicePort(ports);
        String protocol = instance.getConnectors().stream().map(m -> m.get(CONNECTOR.protocol.name())).collect(Collectors.joining(","));
        discMiddlewareInstance.setMiddlewareInstanceProtocol(protocol);
        discMiddlewareInstance.setRunningUser(instance.getRunUser());
        discMiddlewareInstance.setJavaVersion(instance.getJavaVersion());
        discMiddlewareInstance.setJavaVendor(instance.getJavaVendor());
        discMiddlewareInstance.setRuuning(instance.getIsRunning().equals("true"));
        resultList.add(discMiddlewareInstance);
        return resultList;
    }
}