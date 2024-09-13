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

package io.playce.roro.mw.asmt.jeus;

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
import io.playce.roro.mw.asmt.jeus.dto.JeusAssessmentResult;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.playce.roro.mw.asmt.jeus.helper.JeusHelper.DatabaseHelper.getConnectionUrl;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component("JEUSPostProcessor")
@Slf4j
public class JeusPostProcessor extends AbstractMiddlewarePostProcess {
    @Override
    public List<DiscInstanceInterface> getDiscoveredInstanceInterfaces(MiddlewareAssessmentResult.Instance resultInstance, DiscMiddlewareInstance discInstance) {
        if (resultInstance == null) {
            return null;
        }

        JeusAssessmentResult.Instance instance = (JeusAssessmentResult.Instance) resultInstance;
        return getDiscInstanceInterfaces(instance);
    }

    @NotNull
    private List<DiscInstanceInterface> getDiscInstanceInterfaces(JeusAssessmentResult.Instance instance) {
        for (JeusAssessmentResult.Instances instances : instance.getInstances()) {
            JeusAssessmentResult.Database database = null;
            if (instances.getJvmConfig() != null) {
                for (String opt : instances.getJvmConfig().getJvmOption()) {
                    if (opt.toLowerCase().contains("datasource")) {
                        if (database == null) {
                            database = new JeusAssessmentResult.Database();
                            database.setDataSourceType(Domain1109.JDBC.name());
                        }

                        if (opt.toLowerCase().contains("url")) {
                            String jdbcUrl = opt.split("=")[1];

                            try {
                                // TODO Multiple Datasource 처리
                                JdbcProperty jdbcProperty = JdbcURLParser.getJdbcProperty(jdbcUrl);
                                database.setExportName(jdbcProperty.getDatabase());
                            } catch (InterruptedException e) {
                                // ignore
                            }

                            List<JeusAssessmentResult.DatabaseProperty> properties = new ArrayList<>();
                            JeusAssessmentResult.DatabaseProperty property = new JeusAssessmentResult.DatabaseProperty();
                            property.setName("URL");
                            property.setValue(jdbcUrl);
                            properties.add(property);

                            database.setProperty(properties);
                        }

                        if (opt.toLowerCase().contains("username")) {
                            database.setUser(opt.split("=")[1]);
                        }

                        if (opt.toLowerCase().contains("password")) {
                            database.setPassword(opt.split("=")[1]);
                        }

                        // TODO Target Server 지정 필요
                    }
                }

                if (database != null) {
                    if (instance.getResources() == null) {
                        instance.setResources(new JeusAssessmentResult.Resources());
                    }

                    if (instance.getResources().getDatabases() == null) {
                        instance.getResources().setDatabases(new ArrayList<>());
                    }

                    instance.getResources().getDatabases().add(database);
                }
            }
        }

        List<DiscInstanceInterface> resultList = new ArrayList<>();
        extractInstanceInterface(resultList, instance.getResources().getDatabases());
        return resultList;
    }

    private void extractInstanceInterface(List<DiscInstanceInterface> resultList, List<JeusAssessmentResult.Database> databaseResources) {
        if (databaseResources == null) {
            databaseResources = new ArrayList<>();
        }

        for (JeusAssessmentResult.Database resource : databaseResources) {
            DiscInstanceInterface discInstanceInterface = new DiscInstanceInterface();
            String dataSourceId = resource.getDataSourceId();
            if (dataSourceId == null) {
                dataSourceId = resource.getExportName();
            }

            String connectionUrl = getConnectionUrl(resource);
            if (connectionUrl != null && connectionUrl.contains("jdbc:") && !dataSourceId.toLowerCase().contains("jndi") && !dataSourceId.contains("/")) {
                discInstanceInterface.setDiscoveredInstanceDetailTypeCode(Domain1109.JDBC.name());
            } else {
                discInstanceInterface.setDiscoveredInstanceDetailTypeCode(Domain1109.JNDI.name());
            }

            discInstanceInterface.setDescriptorsName(dataSourceId);
            discInstanceInterface.setFullDescriptors(connectionUrl);
            discInstanceInterface.setUsername(resource.getUser());
            discInstanceInterface.setPassword(resource.getPassword());
            resultList.add(discInstanceInterface);
        }
    }

    @Override
    public List<DiscApplication> getDiscoveredApplications(InventoryProcessQueueItem item, MiddlewareAssessmentResult.Instance resultInstance, DiscMiddlewareInstance discInstance, GetInfoStrategy strategy) {
        if (resultInstance == null)
            return null;

        JeusAssessmentResult.Instance instance = (JeusAssessmentResult.Instance) resultInstance;
        List<DiscApplication> resultList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(instance.getApplications())) {
            for (JeusAssessmentResult.Application app : instance.getApplications()) {
                if (!app.getTarget().contains(discInstance.getMiddlewareInstanceName())) {
                    log.debug("Jeus ==> App TargetName : {}, DiscMW Instance Name {}", app.getTarget(), discInstance.getMiddlewareInstanceName());
                    continue;
                }

                DiscApplication applicationInstance = new DiscApplication();

                applicationInstance.setApplication(app.getId());
                applicationInstance.setDeployPath(app.getSourcePath());
//                applicationInstance.setContextPath(app.getContextRoot());
                applicationInstance.setAutoDeployYn(Domain101.N.name());
                applicationInstance.setReloadableYn(Domain101.N.name());
                resultList.add(applicationInstance);
            }
        }

        return resultList;
    }

    @Override
    public String getEngineVersion(MiddlewareAssessmentResult result) {
        JeusAssessmentResult.Engine engine = (JeusAssessmentResult.Engine) result.getEngine();
        return engine.getVersion();
    }

    @Override
    public List<DiscMiddlewareInstance> getDiscoveredMiddlewareInstances(MiddlewareAssessmentResult result, GetInfoStrategy strategy) {
        if (result == null)
            return null;

        JeusAssessmentResult r = (JeusAssessmentResult) result;
        JeusAssessmentResult.Engine engine = (JeusAssessmentResult.Engine) r.getEngine();
        JeusAssessmentResult.Instance instance = (JeusAssessmentResult.Instance) r.getInstance();
        List<DiscMiddlewareInstance> resultList = new ArrayList<>();

        for (JeusAssessmentResult.Instances ins : instance.getInstances()) {
            JeusAssessmentResult.Engines engines = ins.getEngines();

            if (engines == null)
                continue;

            for (JeusAssessmentResult.EngineContainer container : engines.getEngineContainer()) {
                String instanceName = ins.getName() + "_" + container.getName();
                DiscMiddlewareInstance discMiddlewareInstance = new DiscMiddlewareInstance();
                discMiddlewareInstance.setMiddlewareInstanceName(instanceName);

                discMiddlewareInstance.setMiddlewareInstancePath(engine.getPath());
                discMiddlewareInstance.setMiddlewareInstanceDetailDivision(engine.getPath() + "|" + instanceName);

                if (ins.getListeners() != null) {
                    discMiddlewareInstance.setMiddlewareInstanceServicePort(ins.getListeners().getListeners().stream().map(JeusAssessmentResult.Listener::getListenPort).collect(Collectors.joining(",")));
                    discMiddlewareInstance.setMiddlewareInstanceProtocol(ins.getListeners().getListeners().stream().map(JeusAssessmentResult.Listener::getName).collect(Collectors.joining(",")));
                } else {
                    List<JeusAssessmentResult.WebEngine> webEngines = engines.getWebEngine();
                    if (webEngines == null)
                        continue;

                    for (JeusAssessmentResult.WebEngine webEngine : webEngines) {
                        JeusAssessmentResult.WebConnections webConnections = webEngine.getWebConnections();
                        if (webConnections == null)
                            continue;

                        List<JeusAssessmentResult.HttpListener> listeners = webConnections.getHttpListener();
                        if (listeners == null)
                            continue;

                        discMiddlewareInstance.setMiddlewareInstanceServicePort(listeners.stream().map(JeusAssessmentResult.HttpListener::getPort).collect(Collectors.joining(",")));
                        discMiddlewareInstance.setMiddlewareInstanceProtocol(listeners.stream().map(l -> "HTTP").collect(Collectors.joining(",")));
                    }
                }
                discMiddlewareInstance.setRunningUser(ins.getRunUser());
                discMiddlewareInstance.setJavaVersion(instance.getJavaVersion());
                discMiddlewareInstance.setJavaVendor(instance.getJavaVendor());
                if (ins.getStatus() != null) {
                    discMiddlewareInstance.setRuuning(ins.getStatus().contains("Running"));
                }
                resultList.add(discMiddlewareInstance);
            }
        }

        return resultList;
    }
}