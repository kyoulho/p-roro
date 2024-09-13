/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jhpark       8월 04, 2022            First Draft.
 */
package io.playce.roro.mw.asmt.jboss;

import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.code.Domain1109;
import io.playce.roro.common.dto.info.JdbcInfo;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.util.JdbcURLParser;
import io.playce.roro.mw.asmt.AbstractMiddlewarePostProcess;
import io.playce.roro.mw.asmt.dto.DiscApplication;
import io.playce.roro.mw.asmt.dto.DiscInstanceInterface;
import io.playce.roro.mw.asmt.dto.DiscMiddlewareInstance;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.jboss.dto.JbossAssessmentResult;
import io.playce.roro.mw.asmt.jboss.enums.ENGINE;
import io.playce.roro.mw.asmt.jboss.enums.attribute.RESOURCE;
import io.playce.roro.mw.asmt.jboss.strategy.helper.JBossHelper;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author jhpark
 * @version 3.0
 */
@Component("JBOSSPostProcessor")
@RequiredArgsConstructor
@Slf4j
public class JBossPostProcessor extends AbstractMiddlewarePostProcess {

    private final JBossHelper jBossHelper;

    @Override
    public List<DiscInstanceInterface> getDiscoveredInstanceInterfaces(MiddlewareAssessmentResult.Instance resultInstance, DiscMiddlewareInstance discInstance) {
        if (resultInstance == null) {
            return null;
        }

        JbossAssessmentResult.Instance instance = (JbossAssessmentResult.Instance) resultInstance;
        return getDiscInstanceInterfaces(instance);
    }

    @NotNull
    private List<DiscInstanceInterface> getDiscInstanceInterfaces(JbossAssessmentResult.Instance instance) {
        if (instance.getRunTimeOptions() != null) {
            LinkedHashMap<String, String> datasource = null;
            for (String opt : instance.getRunTimeOptions()) {
                if (opt.toLowerCase().contains("datasource")) {
                    if (datasource == null) {
                        datasource = new LinkedHashMap<>();
                    }

                    if (opt.toLowerCase().contains("url")) {
                        String jdbcUrl = opt.split("=")[1];

                        try {
                            List<JdbcInfo> parse = JdbcURLParser.parse(jdbcUrl);

                            // TODO Multiple Datasource 처리
                            if (!parse.isEmpty()) {
                                datasource.put(RESOURCE.poolname.name(), parse.get(0).getDatabase());
                            }
                        } catch (Exception e) {
                            // ignore
                        }

                        datasource.put(RESOURCE.connectionurl.name(), jdbcUrl);
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
        extractInstanceInterface(resultList, instance.getResources());
        return resultList;
    }

    private void extractInstanceInterface(List<DiscInstanceInterface> resultList, List<LinkedHashMap> resources) {
        if (resources == null) {
            resources = new ArrayList<>();
        }

        for (Map<String, String> resource : resources) {
            DiscInstanceInterface discInstanceInterface = new DiscInstanceInterface();

            String connectionUrl = resource.get(RESOURCE.connectionurl.getCodeName());
            String poolName = resource.get(RESOURCE.poolname.getCodeName());

            if (connectionUrl != null && poolName != null && connectionUrl.contains("jdbc:") && !poolName.toLowerCase().contains("jndi") && !poolName.contains("/")) {
                discInstanceInterface.setDiscoveredInstanceDetailTypeCode(Domain1109.JDBC.name());
            } else {
                discInstanceInterface.setDiscoveredInstanceDetailTypeCode(Domain1109.JNDI.name());
            }
            discInstanceInterface.setDescriptorsName(resource.get(RESOURCE.poolname.getCodeName()));
            discInstanceInterface.setFullDescriptors(resource.get(RESOURCE.connectionurl.getCodeName()));
            discInstanceInterface.setUsername(resource.get(RESOURCE.username.getCodeName()));
            discInstanceInterface.setPassword(resource.get(RESOURCE.password.getCodeName()));
            resultList.add(discInstanceInterface);
        }
    }

    @Override
    public List<DiscApplication> getDiscoveredApplications(InventoryProcessQueueItem item, MiddlewareAssessmentResult.Instance resultInstance, DiscMiddlewareInstance discInstance, GetInfoStrategy strategy) {
        if (resultInstance == null)
            return null;

        JbossAssessmentResult.Instance instance = (JbossAssessmentResult.Instance) resultInstance;
        List<DiscApplication> resultList = new ArrayList<>();
        if (instance.getApplications() != null) {
            for (JbossAssessmentResult.Applications app : instance.getApplications()) {
                DiscApplication applicationInstance = new DiscApplication();
                String contextPath = app.getContextPath();

                // https://cloud-osci.atlassian.net/browse/PCR-6207
                if (StringUtils.isNotEmpty(app.getServiceName())) {
                    applicationInstance.setApplication(app.getServiceName());
                } else {
                    String application;
                    if (StringUtils.isEmpty(contextPath)) {
                        application = FilenameUtils.removeExtension(app.getDeployFileName());
                    } else {
                        if (contextPath.equals("/")) {
                            application = "ROOT";
                        } else if (contextPath.startsWith("/")) {
                            application = contextPath.substring(1);
                        } else {
                            application = contextPath;
                        }
                    }

                    applicationInstance.setApplication(application);
                }

                applicationInstance.setDeployPath(app.getSourcePath());
                applicationInstance.setContextPath(contextPath);
                applicationInstance.setAutoDeployYn(Domain101.N.name());
                applicationInstance.setReloadableYn(Domain101.N.name());
                resultList.add(applicationInstance);
            }
        }
        return resultList;
    }

    @Override
    public String getEngineVersion(MiddlewareAssessmentResult result) {
        JbossAssessmentResult.Engine engine = (JbossAssessmentResult.Engine) result.getEngine();
        return engine.getVersion();
    }

    @Override
    public List<DiscMiddlewareInstance> getDiscoveredMiddlewareInstances(MiddlewareAssessmentResult result, GetInfoStrategy strategy) {
        if (result == null)
            return null;

        String separator = strategy.getSeparator();

        JbossAssessmentResult r = (JbossAssessmentResult) result;
        JbossAssessmentResult.Instance instance = (JbossAssessmentResult.Instance) r.getInstance();
        JbossAssessmentResult.Engine engine = (JbossAssessmentResult.Engine) r.getEngine();
        List<DiscMiddlewareInstance> resultList = new ArrayList<>();

        /**
         * domain mode 일경우 server-group에 등록된 인스턴스 저장한다.
         */
        if (instance.getInstances() != null && engine.getMode().equals(ENGINE.DOMAIN_NAME.codeName())) {
            if (instance.getInstances().size() > 0) {
                return addDomainSvrInstances(instance, separator, resultList);
            }
        }

        // https://cloud-osci.atlassian.net/browse/PCR-6207
        // String instancePath = instance.getDomainPath();
        // int index = instancePath.lastIndexOf(separator);
        // String instanceName = index == -1 ? instancePath : instancePath.substring(index + 1);
        String instanceName = instance.getDomainName();

        DiscMiddlewareInstance discMiddlewareInstance = new DiscMiddlewareInstance();
        discMiddlewareInstance.setMiddlewareInstanceName(instanceName);
        discMiddlewareInstance.setMiddlewareInstancePath(instance.getDomainPath());
        discMiddlewareInstance.setMiddlewareInstanceDetailDivision(instance.getDomainPath());
        //discMiddlewareInstance.setMiddlewareConfigPath(instance.getPath());

        String ports = jBossHelper.strJoin(instance.getConnectors(), "port", ",");
        String protocol = jBossHelper.strJoin(instance.getConnectors(), "name", ",");
        discMiddlewareInstance.setMiddlewareInstanceServicePort(ports);
        discMiddlewareInstance.setMiddlewareInstanceProtocol(protocol);
        discMiddlewareInstance.setRunningUser(instance.getRunUser());
        discMiddlewareInstance.setJavaVersion(instance.getJavaVersion());
        discMiddlewareInstance.setJavaVendor(instance.getJavaVendor());
        discMiddlewareInstance.setRuuning(instance.getIsRunning().equals("true"));
        resultList.add(discMiddlewareInstance);

        return resultList;
    }

    private List<DiscMiddlewareInstance> addDomainSvrInstances(JbossAssessmentResult.Instance instance, String separator, List<DiscMiddlewareInstance> resultList) {
        for (JbossAssessmentResult.Instances svrIns : instance.getInstances()) {
            if (!svrIns.getName().equals(instance.getDomainName())) { // 같은 인스턴스 중복으로 저장되지 않게 하기 위해 Host Controller 는 실제 DB에 저장하지 않고 instances json으로만 보여준다.
                DiscMiddlewareInstance discMiddlewareInstance = new DiscMiddlewareInstance();
                discMiddlewareInstance.setMiddlewareInstanceName(svrIns.getName());
                discMiddlewareInstance.setMiddlewareInstancePath(instance.getDomainPath());
                discMiddlewareInstance.setMiddlewareInstanceDetailDivision(instance.getDomainPath() + "/" + svrIns.getName());
                discMiddlewareInstance.setMiddlewareConfigPath(instance.getConfigPath());

                String ports = jBossHelper.strJoin(svrIns.getSvrConnectors(), "port", ",");
                String protocol = jBossHelper.strJoin(svrIns.getSvrConnectors(), "name", ",");
                discMiddlewareInstance.setMiddlewareInstanceServicePort(ports);
                discMiddlewareInstance.setMiddlewareInstanceProtocol(protocol);
                if (StringUtils.isNotEmpty(svrIns.getRunUser())) {
                    discMiddlewareInstance.setRunningUser(svrIns.getRunUser());
                } else {
                    discMiddlewareInstance.setRunningUser(instance.getRunUser());
                }
                discMiddlewareInstance.setJavaVersion(svrIns.getJavaVersion());
                discMiddlewareInstance.setJavaVendor(svrIns.getJavaVendor());
                discMiddlewareInstance.setRuuning(svrIns.getIsRunning().equals("true"));
                resultList.add(discMiddlewareInstance);
            }

        }
        return resultList;
    }

}