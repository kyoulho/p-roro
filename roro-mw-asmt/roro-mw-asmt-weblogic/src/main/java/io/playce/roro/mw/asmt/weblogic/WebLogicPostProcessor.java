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

package io.playce.roro.mw.asmt.weblogic;

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
import io.playce.roro.mw.asmt.weblogic.dto.WebLogicAssessmentResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.File;
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
@Component("WEBLOGICPostProcessor")
@Slf4j
public class WebLogicPostProcessor extends AbstractMiddlewarePostProcess {
    @Override
    public List<DiscInstanceInterface> getDiscoveredInstanceInterfaces(MiddlewareAssessmentResult.Instance resultInstance, DiscMiddlewareInstance discInstance) {
        if (resultInstance == null) {
            return null;
        }

        WebLogicAssessmentResult.Instance instance = (WebLogicAssessmentResult.Instance) resultInstance;
        return getDiscInstanceInterfaces(instance);
    }

    @NotNull
    private List<DiscInstanceInterface> getDiscInstanceInterfaces(WebLogicAssessmentResult.Instance instance) {
        for (WebLogicAssessmentResult.Instances instances : instance.getInstances()) {
            WebLogicAssessmentResult.Jdbc jdbc = null;
            WebLogicAssessmentResult.Datasource datasource = null;
            WebLogicAssessmentResult.JdbcDriverParams jdbcDriverParams = null;
            WebLogicAssessmentResult.Properties properties;
            if (StringUtils.isNotEmpty(instances.getVmOption())) {
                String[] vmOptions = instances.getVmOption().split(StringUtils.SPACE);
                for (String opt : vmOptions) {
                    if (opt.toLowerCase().contains("datasource")) {
                        if (jdbc == null) {
                            jdbc = new WebLogicAssessmentResult.Jdbc();
                            datasource = new WebLogicAssessmentResult.Datasource();
                            jdbcDriverParams = new WebLogicAssessmentResult.JdbcDriverParams();
                            jdbcDriverParams.setProperties(new ArrayList<>());
                        }

                        if (opt.toLowerCase().contains("url")) {
                            String jdbcUrl = opt.split("=")[1];

                            try {
                                // TODO Multiple Datasource 처리
                                JdbcProperty jdbcProperty = JdbcURLParser.getJdbcProperty(jdbcUrl);
                                jdbc.setName(jdbcProperty.getDatabase());
                                datasource.setName(jdbcProperty.getDatabase());
                            } catch (InterruptedException e) {
                                // ignore
                            }

                            jdbcDriverParams.setUrl(jdbcUrl);
                        }

                        if (opt.toLowerCase().contains("username")) {
                            properties = new WebLogicAssessmentResult.Properties();
                            properties.setName("username");
                            properties.setValue(opt.split("=")[1]);

                            jdbcDriverParams.getProperties().add(properties);
                        }

                        if (opt.toLowerCase().contains("password")) {
                            properties = new WebLogicAssessmentResult.Properties();
                            properties.setName("password");
                            properties.setValue(opt.split("=")[1]);

                            jdbcDriverParams.getProperties().add(properties);
                        }

                        // TODO Target Server 지정 필요
                    }
                }

                if (jdbc != null) {
                    if (instance.getResource() == null) {
                        instance.setResource(new WebLogicAssessmentResult.Resource());
                    }

                    if (instance.getResource().getJdbc() == null) {
                        instance.getResource().setJdbc(new ArrayList<>());
                    }

                    instance.getResource().getJdbc().add(jdbc);
                }
            }
        }

        List<DiscInstanceInterface> resultList = new ArrayList<>();
        extractInstanceInterface(resultList, instance.getResource().getJdbc(), instance.getInstances());
        return resultList;
    }

    private void extractInstanceInterface(List<DiscInstanceInterface> resultList, List<WebLogicAssessmentResult.Jdbc> jdbcResources, List<WebLogicAssessmentResult.Instances> instancesList) {
        if (jdbcResources == null) {
            jdbcResources = new ArrayList<>();
        }

        for (WebLogicAssessmentResult.Jdbc jdbc : jdbcResources) {
            DiscInstanceInterface discInstanceInterface = new DiscInstanceInterface();
            discInstanceInterface.setDiscoveredInstanceDetailTypeCode(Domain1109.JNDI.name());
            discInstanceInterface.setDescriptorsName(jdbc.getName());
            if (jdbc.getDatasource().getJdbcDriverParams() != null) {
                String url = jdbc.getDatasource().getJdbcDriverParams().getUrl();
                discInstanceInterface.setFullDescriptors(url);

                if (url.contains("jdbc:") && !jdbc.getName().toLowerCase().contains("jndi") && !jdbc.getName().contains("/")) {
                    discInstanceInterface.setDiscoveredInstanceDetailTypeCode(Domain1109.JDBC.name());
                } else {
                    discInstanceInterface.setDiscoveredInstanceDetailTypeCode(Domain1109.JNDI.name());
                }

                resultList.add(discInstanceInterface);
            }
        }
    }

    @Override
    public List<DiscApplication> getDiscoveredApplications(InventoryProcessQueueItem item, MiddlewareAssessmentResult.Instance resultInstance, DiscMiddlewareInstance discInstance, GetInfoStrategy strategy) {
        if (resultInstance == null)
            return null;

        WebLogicAssessmentResult.Instance instance = (WebLogicAssessmentResult.Instance) resultInstance;

        List<DiscApplication> resultList = new ArrayList<>();
        for (WebLogicAssessmentResult.Application app : instance.getApplication()) {
            String detailDivision = instance.getDomainHome() + File.separator + app.getTarget();
            if (!discInstance.getMiddlewareInstanceDetailDivision().equals(detailDivision))
                continue;

            DiscApplication applicationInstance = new DiscApplication();
            applicationInstance.setApplication(app.getName());
            applicationInstance.setDeployPath(app.getSourcePath());
            applicationInstance.setAutoDeployYn(Domain101.N.name());
            applicationInstance.setReloadableYn(Domain101.N.name());
            resultList.add(applicationInstance);
        }
        return resultList;
    }

    @Override
    public String getEngineVersion(MiddlewareAssessmentResult result) {
        WebLogicAssessmentResult.Engine engine = (WebLogicAssessmentResult.Engine) result.getEngine();
        return engine.getVersion();
    }

    @Override
    public List<DiscMiddlewareInstance> getDiscoveredMiddlewareInstances(MiddlewareAssessmentResult result, GetInfoStrategy strategy) {
        if (result == null)
            return null;
        WebLogicAssessmentResult.Instance instance = (WebLogicAssessmentResult.Instance) result.getInstance();
        List<DiscMiddlewareInstance> resultList = new ArrayList<>();

        for (WebLogicAssessmentResult.Instances ins : instance.getInstances()) {
            DiscMiddlewareInstance discMiddlewareInstance = new DiscMiddlewareInstance();
            discMiddlewareInstance.setMiddlewareInstanceName(ins.getName());
            discMiddlewareInstance.setMiddlewareInstancePath(instance.getDomainHome());
            discMiddlewareInstance.setMiddlewareInstanceDetailDivision(instance.getDomainHome() + File.separator + ins.getName());
//            discMiddlewareInstance.setMiddlewareConfigPath(instance.getDomainHome());
            discMiddlewareInstance.setMiddlewareInstanceServicePort(Long.toString(ins.getListenPort()));
            discMiddlewareInstance.setMiddlewareInstanceProtocol("HTTP");
            discMiddlewareInstance.setRunningUser(ins.getRunUser());
            discMiddlewareInstance.setJavaVersion(instance.getJavaVersion());
            discMiddlewareInstance.setJavaVendor(instance.getJavaVendor());
            discMiddlewareInstance.setRuuning(ins.getMinHeap() != null);
            resultList.add(discMiddlewareInstance);
        }
        return resultList;
    }
}