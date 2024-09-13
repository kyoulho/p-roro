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

package io.playce.roro.mw.asmt.websphere;

import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.code.Domain1109;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.util.JdbcURLParser;
import io.playce.roro.common.util.support.JdbcProperty;
import io.playce.roro.mw.asmt.AbstractMiddlewarePostProcess;
import io.playce.roro.mw.asmt.dto.DiscApplication;
import io.playce.roro.mw.asmt.dto.DiscInstanceInterface;
import io.playce.roro.mw.asmt.dto.DiscMiddlewareInstance;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.websphere.dto.WebSphereAssessmentResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component("WSPHEREPostProcessor")
@Slf4j
public class WebSpherePostProcessor extends AbstractMiddlewarePostProcess {
    @Override
    public List<DiscInstanceInterface> getDiscoveredInstanceInterfaces(MiddlewareAssessmentResult.Instance resultInstance, DiscMiddlewareInstance discInstance) {
        if (resultInstance == null) {
            return null;
        }

        WebSphereAssessmentResult.Instance instance = (WebSphereAssessmentResult.Instance) resultInstance;
        return getDiscInstanceInterfaces(instance);
    }

    @NotNull
    private List<DiscInstanceInterface> getDiscInstanceInterfaces(WebSphereAssessmentResult.Instance instance) {
        for (WebSphereAssessmentResult.Instances instances : instance.getInstances()) {
            WebSphereAssessmentResult.DataSource dataSource = null;
            if (StringUtils.isNotEmpty(instances.getVmOption())) {
                String[] vmOptions = instances.getVmOption().split(StringUtils.SPACE);
                for (String opt : vmOptions) {
                    if (opt.toLowerCase().contains("datasource")) {
                        if (dataSource == null) {
                            dataSource = new WebSphereAssessmentResult.DataSource();
                        }

                        if (opt.toLowerCase().contains("url")) {
                            String jdbcUrl = opt.split("=")[1];

                            try {
                                // TODO Multiple Datasource 처리
                                JdbcProperty jdbcProperty = JdbcURLParser.getJdbcProperty(jdbcUrl);
                                dataSource.setDataSourceName(jdbcProperty.getDatabase());
                            } catch (InterruptedException e) {
                                // ignore
                            }

                            dataSource.setConnectionUrl(jdbcUrl);
                        }

                        if (opt.toLowerCase().contains("username")) {
                            dataSource.setUserId(opt.split("=")[1]);
                        }

                        if (opt.toLowerCase().contains("password")) {
                            dataSource.setPassword(opt.split("=")[1]);
                        }

                        // TODO Target Server 지정 필요
                    }
                }

                if (dataSource != null) {
                    if (instance.getDataSources() == null) {
                        instance.setDataSources(new ArrayList<>());
                    }

                    instance.getDataSources().add(dataSource);
                }
            }
        }

        List<DiscInstanceInterface> resultList = new ArrayList<>();
        extractInstanceInterface(resultList, instance.getDataSources());
        return resultList;
    }

    private void extractInstanceInterface(List<DiscInstanceInterface> resultList, List<WebSphereAssessmentResult.DataSource> dataSources) {
        if (CollectionUtils.isNotEmpty(dataSources)) {
            for (WebSphereAssessmentResult.DataSource dataSource : dataSources) {
                DiscInstanceInterface discInstanceInterface = new DiscInstanceInterface();

                if (StringUtils.isNotEmpty(dataSource.getDataSourceName())) {
                    discInstanceInterface.setDescriptorsName(dataSource.getDataSourceName());
                } else {
                    discInstanceInterface.setDescriptorsName(dataSource.getJndiName());
                }

                if (dataSource.getConnectionUrl().contains("jdbc:") && !discInstanceInterface.getDescriptorsName().toLowerCase().contains("jndi") && !discInstanceInterface.getDescriptorsName().contains("/")) {
                    discInstanceInterface.setDiscoveredInstanceDetailTypeCode(Domain1109.JDBC.name());
                } else {
                    discInstanceInterface.setDiscoveredInstanceDetailTypeCode(Domain1109.JNDI.name());
                }

                discInstanceInterface.setFullDescriptors(dataSource.getConnectionUrl());
                discInstanceInterface.setUsername(StringUtils.defaultString(dataSource.getUserId()));
                discInstanceInterface.setPassword(StringUtils.defaultString(dataSource.getPassword()));
                resultList.add(discInstanceInterface);
            }
        }
    }

    @Override
    public List<DiscApplication> getDiscoveredApplications(InventoryProcessQueueItem item, MiddlewareAssessmentResult.Instance resultInstance, DiscMiddlewareInstance discInstance, GetInfoStrategy strategy) {
        if (resultInstance == null) {
            return null;
        }

        WebSphereAssessmentResult.Instance instance = (WebSphereAssessmentResult.Instance) resultInstance;
        List<DiscApplication> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(instance.getApplications())) {
            for (WebSphereAssessmentResult.Application app : instance.getApplications()) {
                String[] splitDetailDivision = StringUtils.split(discInstance.getMiddlewareInstanceDetailDivision(), "|");
                String profileName = splitDetailDivision[0];
                String cellName = splitDetailDivision[1];
                String nodeName = splitDetailDivision[2];
                String serverName = splitDetailDivision[3];

                if (app.getProfileName().equals(profileName) && app.getCellName().equals(cellName)
                        && app.getNodeName().equals(nodeName) && app.getServerName().equals(serverName)) {
                    DiscApplication applicationInstance = new DiscApplication();
                    applicationInstance.setApplication(app.getApplicationName());
                    applicationInstance.setDeployPath(app.getApplicationBinaryUrlPath());
                    applicationInstance.setAutoDeployYn(Domain101.N.name());
                    applicationInstance.setReloadableYn(Domain101.N.name());

                    resultList.add(applicationInstance);
                }
            }
        }

        return resultList;
    }

    @Override
    public String getEngineVersion(MiddlewareAssessmentResult result) {
        WebSphereAssessmentResult.Engine engine = (WebSphereAssessmentResult.Engine) result.getEngine();
        return engine.getVersion();
    }

    @Override
    public List<DiscMiddlewareInstance> getDiscoveredMiddlewareInstances(MiddlewareAssessmentResult result, GetInfoStrategy strategy) {
        if (result == null)
            return null;

        WebSphereAssessmentResult r = (WebSphereAssessmentResult) result;
        WebSphereAssessmentResult.Engine engine = (WebSphereAssessmentResult.Engine) r.getEngine();
        WebSphereAssessmentResult.Instance instance = (WebSphereAssessmentResult.Instance) r.getInstance();
        List<DiscMiddlewareInstance> resultList = new ArrayList<>();

        for (WebSphereAssessmentResult.Server server : instance.getServers()) {
            // Server Type이 APPLICATION_SERVER 일때만 등록 한다.
            // NODE_AGENT, DEPLOYMENT_MANAGER는 패스한다.
            if (StringUtils.defaultString(server.getServerType()).equals("APPLICATION_SERVER")) {
                DiscMiddlewareInstance discMiddlewareInstance = new DiscMiddlewareInstance();
                discMiddlewareInstance.setMiddlewareInstanceName(server.getServerName());
                discMiddlewareInstance.setMiddlewareInstancePath(engine.getPath()
                        + File.separator + server.getProfileName() + File.separator + server.getCellName()
                        + File.separator + server.getNodeName() + File.separator + server.getServerName());
                discMiddlewareInstance.setMiddlewareInstanceDetailDivision(server.getProfileName() + "|" +
                        server.getCellName() + "|" + server.getNodeName() + "|" + server.getServerName());
//            discMiddlewareInstance.setMiddlewareConfigPath();
                discMiddlewareInstance.setMiddlewareInstanceServicePort(instance.getServers().stream().filter(t -> t.getListenPort() != null).map(s -> String.valueOf(s.getListenPort())).distinct().collect(Collectors.joining(",")));
                discMiddlewareInstance.setMiddlewareInstanceProtocol(generateProtocols(discMiddlewareInstance.getMiddlewareInstanceServicePort()));
                discMiddlewareInstance.setRunningUser(generateRunUser(server, instance.getInstances()));
                discMiddlewareInstance.setJavaVersion(instance.getGeneral().getJavaVersion());
                discMiddlewareInstance.setJavaVendor(instance.getGeneral().getJavaVendor());
                discMiddlewareInstance.setRuuning("RUNNING".equals(server.getStatus()));
                resultList.add(discMiddlewareInstance);
            }

        }

        return resultList;
    }

    private String generateProtocols(String middlewareInstanceServicePort) {
        String[] ports = middlewareInstanceServicePort.split(",");
        List<String> protocols = new ArrayList<>();

        if (ports.length > 0) {
            for (int i = 0; i < ports.length; i++) {
                protocols.add("HTTP");
            }
        }

        return String.join(",", protocols);
    }

    private String generateRunUser(WebSphereAssessmentResult.Server server, List<WebSphereAssessmentResult.Instances> instances) {
        String runUser = "";

        if (CollectionUtils.isNotEmpty(instances)) {
            for (WebSphereAssessmentResult.Instances instance : instances) {
                if (instance.getName().equals(server.getServerName())) {
                    runUser = instance.getRunUser();
                }
            }
        }

        return runUser;
    }
}