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
 * Jaeeon Bae       1월 10, 2022            First Draft.
 */
package io.playce.roro.mw.asmt.weblogic.helper;

import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.common.windows.TaskListResult;
import io.playce.roro.common.windows.WebLogicOhsExtract;
import io.playce.roro.mw.asmt.AbstractMiddlewareAssessment;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.mw.asmt.util.WasAnalyzerUtil;
import io.playce.roro.mw.asmt.weblogic.dto.WebLogicAssessmentResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.XML;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebLogicHelper {
    private final CommandConfig commandConfig;
    private final WebLogicOhsExtract webLogicOhsExtract;

    public WebLogicAssessmentResult.Engine getEngineMap(TargetHost targetHost, MiddlewareInventory middleware, JSONObject domain, GetInfoStrategy strategy) throws InterruptedException {
        log.debug(":+:+:+:+:+:+:+: getEngineMap() :+:+:+:+:+:+:+:");
        String[] resultArr;

        String name = "WebLogic";
        String path = null;
        String version= null;

//        String command = "sudo ps -ef | grep weblogic.Server";
        String command = COMMAND.GET_PROCESS.command(commandConfig, strategy.isWindows(), "weblogic.Server");
        String result = AbstractMiddlewareAssessment.getSshCommandResultTrim(targetHost, command, COMMAND.GET_PROCESS, strategy);
        resultArr = result.split("\\s+");

        for (String process : resultArr) {
            if (process.startsWith("-Dweblogic.home=")) {
                path = process.split("=")[1];
            }
        }

        if (domain.containsKey("domain-version")) {
//            version = (String) domain.get("domain-version");
            version = MWCommonUtil.getStringValue(domain, "domain-version");
        }

        if(StringUtils.isEmpty(path)) {
            path = middleware.getEngineInstallationPath();
        }
        WebLogicAssessmentResult.Engine engine = new WebLogicAssessmentResult.Engine();
        engine.setName(name);
        engine.setPath(path);
        engine.setVersion(version);
        engine.setVendor("Oracle");

        return engine;
    }
//
//    public String getVmOptions(TargetHost targetHost, MiddlewareInventory middleware) {
//        log.debug(":+:+:+:+:+:+:+:+: getVmOptions() :+:+:+:+:+:+:+:+:");
//        return WasAnalyzerUtil.getJvmOptions(targetHost, middleware.getProcessName());
//    }

    public List<WebLogicAssessmentResult.ConfigFile> getConfigFiles(TargetHost targetHost, String configFilePath, JSONObject domain, GetInfoStrategy strategy) throws InterruptedException {
        String separator = strategy.getSeparator();

        log.debug(":+:+:+:+:+:+:+:+: getConfigFiles() :+:+:+:+:+:+:+:+:");
        // config file 설정
        List<WebLogicAssessmentResult.ConfigFile> configFileList = new ArrayList<>();
        WebLogicAssessmentResult.ConfigFile configFile;

        // config.xml
        configFile = new WebLogicAssessmentResult.ConfigFile();
        configFile.setPath(configFilePath + separator + "config.xml");

        configFile.setContents(AbstractMiddlewareAssessment.getFileContents(targetHost, configFile.getPath(), commandConfig, strategy));
        configFileList.add(configFile);

        // jdbc-system-resource
        JSONArray jdbcSystemResourceArr = new JSONArray();
        if (domain.get("jdbc-system-resource") instanceof JSONObject) {
            JSONObject jdbcSystemObject = (JSONObject) domain.get("jdbc-system-resource");
            jdbcSystemResourceArr.add(jdbcSystemObject);
        } else if (domain.get("jdbc-system-resource") instanceof JSONArray) {
            jdbcSystemResourceArr = (JSONArray) domain.get("jdbc-system-resource");
        }

        if (!jdbcSystemResourceArr.isEmpty()) {
            for (Object o : jdbcSystemResourceArr) {
                JSONObject obj = (JSONObject) o;

                configFile = new WebLogicAssessmentResult.ConfigFile();
                configFile.setPath(configFilePath + separator + obj.get("descriptor-file-name"));
                configFile.setContents(AbstractMiddlewareAssessment.getFileContents(targetHost, configFile.getPath(), commandConfig, strategy));
                configFileList.add(configFile);
            }
        }

        // jms-system-resource
        JSONArray jmsSystemArr = new JSONArray();
        if (domain.get("jms-system-resource") instanceof JSONObject) {
            JSONObject jmsSystemObj = (JSONObject) domain.get("jms-system-resource");
            jmsSystemArr.add(jmsSystemObj);
        } else if (domain.get("jms-system-resource") instanceof JSONArray) {
            jmsSystemArr = (JSONArray) domain.get("jms-system-resource");
        }

        if (jmsSystemArr != null && !jmsSystemArr.isEmpty()) {
            for (Object o : jmsSystemArr) {
                JSONObject jmsObj = (JSONObject) o;

                configFile = new WebLogicAssessmentResult.ConfigFile();
//                String descriptorFileName = (String) jmsObj.get("descriptor-file-name");
                String descriptorFileName = MWCommonUtil.getStringValue(jmsObj, "descriptor-file-name");
                if(strategy.isWindows()) {
                    descriptorFileName = descriptorFileName.replaceAll("/", "\\\\");
                }
                configFile.setPath(configFilePath + separator + descriptorFileName);
                configFile.setContents(AbstractMiddlewareAssessment.getFileContents(targetHost, configFile.getPath(), commandConfig, strategy));
                configFileList.add(configFile);
            }
        }

        return configFileList;
    }

    public List<WebLogicAssessmentResult.Instances> getServer(TargetHost targetHost, String configFilePath, JSONObject domain, GetInfoStrategy strategy) throws InterruptedException {
        String separator = strategy.getSeparator();

        log.debug(":+:+:+:+:+:+:+:+: getServer() :+:+:+:+:+:+:+:+:");
        List<WebLogicAssessmentResult.Instances> serverList = new ArrayList<>();
        WebLogicAssessmentResult.Instances server;

        JSONArray serverArray = new JSONArray();
        if (domain.get("server") instanceof JSONObject) {
            JSONObject serverObject = (JSONObject) domain.get("server");
            serverArray.add(serverObject);
        } else if (domain.get("server") instanceof JSONArray) {
            serverArray = (JSONArray) domain.get("server");
        }

        if (serverArray != null && !serverArray.isEmpty()) {
            for (Object o : serverArray) {
                JSONObject obj = (JSONObject) o;

                // server 설정
                server = new WebLogicAssessmentResult.Instances();
                if (obj.get("name") instanceof String) {
//                    server.setName((String) obj.get("name"));
                    server.setName(MWCommonUtil.getStringValue(obj, "name"));
                }
                if (obj.get("listen-address") instanceof String) {
//                    server.setListenAddress((String) obj.get("listen-address"));
                    server.setListenAddress(MWCommonUtil.getStringValue(obj, "listen-address"));
                }
                if (obj.containsKey("listen-port") && obj.get("listen-port") instanceof Long) {
//                    server.setListenPort((Long) obj.get("listen-port"));
                    server.setListenPort(MWCommonUtil.getLongValue(obj,"listen-port"));
                } else {
                    server.setListenPort(7001L);
                }

                // admin server name
                if (domain.containsKey("admin-server-name") && domain.get("admin-server-name") instanceof String) {
//                    String adminServerName = (String) domain.get("admin-server-name");
                    String adminServerName = MWCommonUtil.getStringValue(domain,"admin-server-name");
                    server.setType(adminServerName.equals(server.getName()) ? "Admin" : "Managed");
                }

                // get minHeap maxHeap
                generatedHeapSize(targetHost, server, strategy);

                // get runUser
                generatedVmOptionAndRunUser(targetHost, server, strategy);

                if (obj.containsKey("ssl") && obj.get("ssl") instanceof JSONObject) {
                    JSONObject sslObj = (JSONObject) obj.get("ssl");
                    if (sslObj.containsKey("enabled") && sslObj.get("enabled") instanceof Boolean) {
//                        server.setSslEnabled((Boolean) sslObj.get("enabled"));
                        server.setSslEnabled(MWCommonUtil.getBooleanValue(sslObj,"enabled"));
                    }
                }

                // log 설정
                if (obj.containsKey("log") && obj.get("log") instanceof JSONObject) {
                    JSONObject logObj = (JSONObject) obj.get("log");

                    WebLogicAssessmentResult.Log log = new WebLogicAssessmentResult.Log();
//                    log.setRotationType((String) logObj.get("rotation-type"));
                    log.setRotationType(MWCommonUtil.getStringValue(logObj,"rotation-type"));
//                    log.setLogFileSeverity((String) logObj.get("log-type-severity"));
                    log.setLogFileSeverity(MWCommonUtil.getStringValue(logObj, "log-type-severity"));
//                    log.setStdoutSeverity((String) logObj.get("stdout-severity"));
                    log.setStdoutSeverity(MWCommonUtil.getStringValue(logObj,"stdout-severity"));
//                    log.setDomainLogBroadcastSeverity((String) logObj.get("domain-log-broadcast-severity"));
                    log.setDomainLogBroadcastSeverity(MWCommonUtil.getStringValue(logObj,"domain-log-broadcast-severity"));
//                    log.setMemoryBufferSeverity((String) logObj.get("memory-buffer-severity"));
                    log.setMemoryBufferSeverity(MWCommonUtil.getStringValue(logObj,"memory-buffer-severity"));
                    log.setPath(configFilePath + separator + "servers" + separator + server.getName());
                    server.setLog(log);
                }

                // cluster name 설정
                if (obj.containsKey("cluster")) {
                    if (obj.get("cluster") instanceof String) {
//                        server.setClusterName((String) obj.get("cluster"));
                        server.setClusterName(MWCommonUtil.getStringValue(obj,"cluster"));
                    } else if (obj.get("cluster") instanceof JSONObject) {
                        JSONObject clusterObj = (JSONObject) obj.get("cluster");
                        if (clusterObj.containsKey("content") && clusterObj.get("content") instanceof String) {
//                            server.setClusterName((String) clusterObj.get("content"));
                            server.setClusterName(MWCommonUtil.getStringValue(clusterObj,"content"));
                        }
                    }
                }

                // 인스턴스에 속한 어플리케이션 add applications
//                List<WebLogicAssessmentResult.Application> applicationList = new ArrayList<>();
//                for (WebLogicAssessmentResult.Application app : applications) {
//                    if (server.getName().equals(app.getTarget())) {
//                        applicationList.add(app);
//                    }
//                }
//                server.setApplication(applicationList);
                serverList.add(server);
            }
        }

        return serverList;
    }

    private void generatedVmOptionAndRunUser(TargetHost targetHost, WebLogicAssessmentResult.Instances server, GetInfoStrategy strategy) throws InterruptedException {
        log.debug(":+:+:+:+:+:+:+:+: generatedVmOptionAndRunUser() :+:+:+:+:+:+:+:+:");
        String findName = "weblogic.Name=" + server.getName();
        server.setVmOption(WasAnalyzerUtil.getJvmOptions(targetHost, findName, commandConfig, strategy));
        String runUser = WasAnalyzerUtil.getRunUser(targetHost, findName, commandConfig, strategy);
        if(StringUtils.isEmpty(runUser) && strategy.isWindows()) {
            List<TaskListResult> webLogicProcessList = webLogicOhsExtract.getWebLogicProcessList(targetHost);
            for(TaskListResult result: webLogicProcessList) {
                String registryParameters = webLogicOhsExtract.getRegistryParameterByServiceName(targetHost, result.getServiceName());
                if(registryParameters.contains(server.getName())) {
                    runUser = MWCommonUtil.getExecuteResult(targetHost, COMMAND.GET_USER_BY_PID, commandConfig, strategy, result.getPid());
                    runUser = MWCommonUtil.extractUser(runUser);
                }
            }
        }
        server.setRunUser(runUser);
    }

    private void generatedHeapSize(TargetHost targetHost, WebLogicAssessmentResult.Instances server, GetInfoStrategy strategy) throws InterruptedException {
        log.debug(":+:+:+:+:+:+:+:+: generatedHeapSize() :+:+:+:+:+:+:+:+:");
        server.setMinHeap(WasAnalyzerUtil.getHeapSize(targetHost, "weblogic.Name=" + server.getName(), "-Xms", commandConfig, strategy));
        server.setMaxHeap(WasAnalyzerUtil.getHeapSize(targetHost, "weblogic.Name=" + server.getName(), "-Xmx", commandConfig, strategy));
    }

    public WebLogicAssessmentResult.Cluster getCluster(JSONObject domain) {
        log.debug(":+:+:+:+:+:+:+:+: getCluster() :+:+:+:+:+:+:+:+:");
        // cluster 설정
        WebLogicAssessmentResult.Cluster cluster = new WebLogicAssessmentResult.Cluster();

        if (domain.get("cluster") instanceof JSONObject) {
            JSONObject clusterObj = (JSONObject) domain.get("cluster");
//            cluster.setName((String) clusterObj.get("name"));
            cluster.setName(MWCommonUtil.getStringValue(clusterObj,"name"));
//            cluster.setClusterMessagingMode((String) clusterObj.get("cluster-messaging-mode"));
            cluster.setClusterMessagingMode(MWCommonUtil.getStringValue(clusterObj,"cluster-messaging-mode"));
            cluster.setDefaultLoadAlgorithm(MWCommonUtil.getStringValue(clusterObj,"default-load-algorithm"));
        }

        return cluster;
    }

    public List<WebLogicAssessmentResult.Application> getApplications(MiddlewareInventory middleware, JSONObject domain, GetInfoStrategy strategy) {
        log.debug(":+:+:+:+:+:+:+:+: getApplications() :+:+:+:+:+:+:+:+:");
        // application 설정
        List<WebLogicAssessmentResult.Application> applicationList = new ArrayList<>();
        WebLogicAssessmentResult.Application application;

        JSONArray applicationArr = new JSONArray();
        if (domain.get("app-deployment") instanceof JSONObject) {
            JSONObject appObj = (JSONObject) domain.get("app-deployment");
            applicationArr.add(appObj);
        } else if (domain.get("app-deployment") instanceof JSONArray) {
            applicationArr = (JSONArray) domain.get("app-deployment");
        }

        if (applicationArr != null && !applicationArr.isEmpty()) {
            for (Object o : applicationArr) {
                JSONObject obj = (JSONObject) o;

                application = new WebLogicAssessmentResult.Application();
//                application.setName((String) obj.get("name"));
                application.setName(MWCommonUtil.getStringValue(obj,"name"));
//                application.setTarget((String) obj.get("target"));
                application.setTarget(MWCommonUtil.getStringValue(obj,"target"));
//                application.setModuleType((String) obj.get("module-type"));
                application.setModuleType(MWCommonUtil.getStringValue(obj,"module-type"));
//                application.setSourcePath((String) obj.get("source-path"));
                application.setSourcePath(MWCommonUtil.getStringValue(obj,"source-path"));
//                application.setSecurityDDMode((String) obj.get("security-dd-model"));
                application.setSecurityDDMode(MWCommonUtil.getStringValue(obj,"security-dd-model"));

                // source path 가 상대경로인 경우 domain_home을 붙여서 경로를 찾아준다.
                String applicationSourcePath = application.getSourcePath();
                String separator = strategy.getSeparator();
                if ((!strategy.isWindows() && !applicationSourcePath.startsWith("/")) || (strategy.isWindows() && applicationSourcePath.charAt(1) != ':')) {
                    String sourcePath = middleware.getDomainHomePath();
                    if (middleware.getDomainHomePath().endsWith(separator)) {
                        sourcePath += application.getSourcePath();
                    } else {
                        sourcePath += separator + application.getSourcePath();
                    }
                    application.setSourcePath(sourcePath);
                }

                // staging-mode 의 Attribute 'xsi:nil="true"'가 있는 경우, 해당 태그의 값을 가져와서 설정한다.;
                if (obj.containsKey("staging-mode")) {
                    if (obj.get("staging-mode") instanceof String) {
//                        application.setStagingMode((String) obj.get("staging-mode"));
                        application.setStagingMode(MWCommonUtil.getStringValue(obj,"staging-mode"));
                    } else if (obj.get("staging-mode") instanceof JSONObject) {
                        JSONObject stagingModeObj = (JSONObject) obj.get("staging-mode");

                        if (stagingModeObj.get("content") instanceof String) {
//                            application.setStagingMode((String) stagingModeObj.get("content"));
                            application.setStagingMode(MWCommonUtil.getStringValue(stagingModeObj,"content"));
                        }
                    }
                }
                applicationList.add(application);
            }
        }

        return applicationList;
    }

    @SneakyThrows
    public WebLogicAssessmentResult.Resource getResources(TargetHost targetHost, String configFilePath, JSONObject domain, GetInfoStrategy strategy) {
        log.debug(":+:+:+:+:+:+:+:+: getResources() :+:+:+:+:+:+:+:+:");
        WebLogicAssessmentResult.Resource resource = new WebLogicAssessmentResult.Resource();
        List<WebLogicAssessmentResult.Jdbc> jdbcList = new ArrayList<>();
        WebLogicAssessmentResult.Jdbc jdbc;

        JSONArray resourceArr = new JSONArray();
        if (domain.get("jdbc-system-resource") instanceof JSONObject) {
            JSONObject resourceObj = (JSONObject) domain.get("jdbc-system-resource");
            resourceArr.add(resourceObj);
        } else if (domain.get("jdbc-system-resource") instanceof JSONArray) {
            resourceArr = (JSONArray) domain.get("jdbc-system-resource");
        }

        if (resourceArr != null && !resourceArr.isEmpty()) {
            for (Object o : resourceArr) {
                JSONObject obj = (JSONObject) o;
                String jdbcFilePath = configFilePath + strategy.getSeparator() + obj.get("descriptor-file-name");
                String jdbcXmlFile = AbstractMiddlewareAssessment.getFileContents(targetHost, jdbcFilePath, commandConfig, strategy);

                // attribute (xsi:nil="true", i:nil="true") 제거
                jdbcXmlFile = jdbcXmlFile.replaceAll("xsi:nil=\"true\"", "");
                jdbcXmlFile = jdbcXmlFile.replaceAll("i:nil=\"true\"", "");
                String jdbcXmlStr = String.valueOf(XML.toJSONObject(jdbcXmlFile));

                // xml to simple.JSONObject
                JSONObject jsonObject = (JSONObject) new JSONParser().parse(jdbcXmlStr);
                JSONObject jdbcDataSource = (JSONObject) jsonObject.get("jdbc-data-source");

                // jdbc data 설정
                jdbc = new WebLogicAssessmentResult.Jdbc();
//                jdbc.setName((String) jdbcDataSource.get("name"));
                jdbc.setName(MWCommonUtil.getStringValue(jdbcDataSource,"name"));
//                jdbc.setTarget((String) obj.get("target"));
                jdbc.setTarget(MWCommonUtil.getStringValue(obj,"target"));
//                jdbc.setDescriptorFileName((String) obj.get("descriptor-file-name"));
                jdbc.setDescriptorFileName(MWCommonUtil.getStringValue(obj,"descriptor-file-name"));

                // datasource data 설정
                WebLogicAssessmentResult.Datasource datasource = new WebLogicAssessmentResult.Datasource();
//                datasource.setName((String) jdbcDataSource.get("name"));
                datasource.setName(MWCommonUtil.getStringValue(jdbcDataSource,"name"));

                // jdbc driver params data 설정
                JSONObject driverObj = (JSONObject) jdbcDataSource.get("jdbc-driver-params");
                if (driverObj != null) {
                    WebLogicAssessmentResult.JdbcDriverParams jdbcDriverParams = new WebLogicAssessmentResult.JdbcDriverParams();
//                    jdbcDriverParams.setUrl((String) driverObj.get("url"));
                    jdbcDriverParams.setUrl(MWCommonUtil.getStringValue(driverObj,"url"));
//                    jdbcDriverParams.setDriverName((String) driverObj.get("driver-name"));
                    jdbcDriverParams.setDriverName(MWCommonUtil.getStringValue(driverObj,"driver-name"));

                    // properties data 설정
                    List<WebLogicAssessmentResult.Properties> properties = new ArrayList<>();
                    WebLogicAssessmentResult.Properties property;

                    JSONArray propertyArr = new JSONArray();
                    if (driverObj.get("properties") != null && driverObj.get("properties") instanceof JSONObject) {
                        JSONObject propertiesObj = (JSONObject) driverObj.get("properties");

                        if (propertiesObj.get("property") instanceof JSONObject) {
                            JSONObject propertyObj = (JSONObject) propertiesObj.get("property");
                            propertyArr.add(propertyObj);
                        } else if (propertiesObj.get("property") instanceof JSONArray) {
                            propertyArr = (JSONArray) propertiesObj.get("property");
                        }

                        if (propertyArr != null && !propertyArr.isEmpty()) {
                            for (Object value : propertyArr) {
                                JSONObject propObj = (JSONObject) value;

                                property = new WebLogicAssessmentResult.Properties();
//                                property.setName((String) propObj.get("name"));
                                property.setName(MWCommonUtil.getStringValue(propObj,"name"));
//                                property.setValue((String) propObj.get("value"));
                                property.setValue(MWCommonUtil.getStringValue(propObj,"value"));
                                properties.add(property);
                            }
                        }
                    }
                    jdbcDriverParams.setProperties(properties);
                    datasource.setJdbcDriverParams(jdbcDriverParams);
                }
                jdbc.setDatasource(datasource);

                // jdbc connection pool params data 설정
                if (jdbcDataSource.containsKey("jdbc-connection-pool-params") &&
                        jdbcDataSource.get("jdbc-connection-pool-params") instanceof JSONObject) {
                    JSONObject jdbcConnectionPoolParamsObj = (JSONObject) jdbcDataSource.get("jdbc-connection-pool-params");

                    WebLogicAssessmentResult.JdbcConnectionPoolParams jdbcConnectionPoolParams = new WebLogicAssessmentResult.JdbcConnectionPoolParams();
//                    jdbcConnectionPoolParams.setInitialCapacity((Long) jdbcConnectionPoolParamsObj.get("initial-capacity"));
//                    jdbcConnectionPoolParams.setMaxCapacity((Long) jdbcConnectionPoolParamsObj.get("max-capacity"));
//                    jdbcConnectionPoolParams.setTestConnectionsOnReserve((Boolean) jdbcConnectionPoolParamsObj.get("test-connections-on-reserve"));
//                    jdbcConnectionPoolParams.setTestTableName((String) jdbcConnectionPoolParamsObj.get("test-table-name"));
//                    jdbcConnectionPoolParams.setSecondsToTrustAnIdlePoolConnection((Long) jdbcConnectionPoolParamsObj.get("seconds-to-trust-an-idle-pool-connection"));
                    jdbcConnectionPoolParams.setInitialCapacity(MWCommonUtil.getLongValue(jdbcConnectionPoolParamsObj,"initial-capacity"));
                    jdbcConnectionPoolParams.setMaxCapacity(MWCommonUtil.getLongValue(jdbcConnectionPoolParamsObj,"max-capacity"));
                    jdbcConnectionPoolParams.setTestConnectionsOnReserve(MWCommonUtil.getBooleanValue(jdbcConnectionPoolParamsObj,"test-connections-on-reserve"));
                    jdbcConnectionPoolParams.setTestTableName(MWCommonUtil.getStringValue(jdbcConnectionPoolParamsObj,"test-table-name"));
                    jdbcConnectionPoolParams.setSecondsToTrustAnIdlePoolConnection(MWCommonUtil.getLongValue(jdbcConnectionPoolParamsObj,"seconds-to-trust-an-idle-pool-connection"));
                    jdbc.setJdbcConnectionPoolParams(jdbcConnectionPoolParams);
                }

                // jdbc data source params data 설정
                if (jdbcDataSource.containsKey("jdbc-data-source-params") &&
                        jdbcDataSource.get("jdbc-data-source-params") instanceof JSONObject) {
                    JSONObject jdbcDataSourceParamsObj = (JSONObject) jdbcDataSource.get("jdbc-data-source-params");

                    WebLogicAssessmentResult.JdbcDataSourceParams jdbcDataSourceParams = new WebLogicAssessmentResult.JdbcDataSourceParams();
                    List<String> jndiNameList = new ArrayList<>();
                    if (jdbcDataSourceParamsObj.get("jndi-name") instanceof String) {
//                        jndiNameList.add((String) jdbcDataSourceParamsObj.get("jndi-name"));
                        jndiNameList.add(MWCommonUtil.getStringValue(jdbcDataSourceParamsObj,"jndi-name"));
                    } else if (jdbcDataSourceParamsObj.get("jndi-name") instanceof JSONArray) {
                        JSONArray jndiArr = (JSONArray) jdbcDataSourceParamsObj.get("jndi-name");

                        if (!jndiArr.isEmpty()) {
                            for (Object value : jndiArr) {
                                jndiNameList.add((String) value);
                            }
                        }
                    }
                    jdbcDataSourceParams.setJndiName(jndiNameList);
//                    jdbcDataSourceParams.setGlobalTransactionsProtocol((String) jdbcDataSourceParamsObj.get("global-transactions-protocol"));
                    jdbcDataSourceParams.setGlobalTransactionsProtocol(MWCommonUtil.getStringValue(jdbcDataSourceParamsObj,"global-transactions-protocol"));
                    jdbc.setJdbcDataSourceParams(jdbcDataSourceParams);
                }
                jdbcList.add(jdbc);
            }
            resource.setJdbc(jdbcList);
        }

        // get jms
        WebLogicAssessmentResult.Jms jms = getJms(domain);
        resource.setJms(jms);

        return resource;
    }

    private WebLogicAssessmentResult.Jms getJms(JSONObject domain) {
        log.debug(":+:+:+:+:+:+:+:+: getJms() :+:+:+:+:+:+:+:+:");
        WebLogicAssessmentResult.Jms jms = new WebLogicAssessmentResult.Jms();
        List<WebLogicAssessmentResult.JmsSystemResource> jmsSystemResourceList = new ArrayList<>();
        WebLogicAssessmentResult.JmsSystemResource jmsSystemResource;

        // jms system resource data 설정
        JSONArray jmsArr = new JSONArray();
        if (domain.get("jms-system-resource") instanceof JSONObject) {
            JSONObject jmsObj = (JSONObject) domain.get("jms-system-resource");
            jmsArr.add(jmsObj);
        } else if (domain.get("jms-system-resource") instanceof JSONArray) {
            jmsArr = (JSONArray) domain.get("jms-system-resource");
        }

        if (jmsArr != null && !jmsArr.isEmpty()) {
            for (Object o : jmsArr) {
                JSONObject jmsSystemObj = (JSONObject) o;

                jmsSystemResource = new WebLogicAssessmentResult.JmsSystemResource();
//                jmsSystemResource.setName((String) jmsSystemObj.get("name"));
                jmsSystemResource.setName(MWCommonUtil.getStringValue(jmsSystemObj,"name"));
//                jmsSystemResource.setDescriptorFileName((String) jmsSystemObj.get("descriptor-file-name"));
                jmsSystemResource.setDescriptorFileName(MWCommonUtil.getStringValue(jmsSystemObj,"descriptor-file-name"));
//                jmsSystemResource.setTarget((String) jmsSystemObj.get("target"));
                jmsSystemResource.setTarget(MWCommonUtil.getStringValue(jmsSystemObj,"target"));

                // sub deployment data 설정
                List<WebLogicAssessmentResult.SubDeployment> subDeploymentList = new ArrayList<>();
                WebLogicAssessmentResult.SubDeployment subDeployment;

                JSONArray subDeploymentArr = new JSONArray();
                if (jmsSystemObj.get("sub-deployment") instanceof JSONObject) {
                    JSONObject subDeploymentObj = (JSONObject) jmsSystemObj.get("sub-deployment");
                    subDeploymentArr.add(subDeploymentObj);
                } else if (jmsSystemObj.get("sub-deployment") instanceof JSONArray) {
                    subDeploymentArr = (JSONArray) jmsSystemObj.get("sub-deployment");
                }

                if (subDeploymentArr != null && !subDeploymentArr.isEmpty()) {
                    for (Object value : subDeploymentArr) {
                        JSONObject obj = (JSONObject) value;

                        subDeployment = new WebLogicAssessmentResult.SubDeployment();
//                        subDeployment.setName((String) obj.get("name"));
                        subDeployment.setName(MWCommonUtil.getStringValue(obj,"name"));
//                        subDeployment.setTarget((String) obj.get("target"));
                        subDeployment.setTarget(MWCommonUtil.getStringValue(obj,"target"));
                        subDeploymentList.add(subDeployment);
                    }
                    jmsSystemResource.setSubDeployment(subDeploymentList);
                }
                jmsSystemResourceList.add(jmsSystemResource);
            }
            jms.setJmsSystemResource(jmsSystemResourceList);
        }

        // jms server data 설정
        List<WebLogicAssessmentResult.JmsServer> jmsServerList = new ArrayList<>();
        WebLogicAssessmentResult.JmsServer jmsServer;

        JSONArray jmsServerArr = new JSONArray();
        if (domain.get("jms-server") instanceof JSONObject) {
            JSONObject jmsServerObj = (JSONObject) domain.get("jms-server");
            jmsServerArr.add(jmsServerObj);
        } else if (domain.get("jms-server") instanceof JSONArray) {
            jmsServerArr = (JSONArray) domain.get("jms-server");
        }

        if (jmsServerArr != null && !jmsServerArr.isEmpty()) {
            for (Object o : jmsServerArr) {
                JSONObject serverObj = (JSONObject) o;

                jmsServer = new WebLogicAssessmentResult.JmsServer();
//                jmsServer.setPersistentStore((String) serverObj.get("persistent-store"));
                jmsServer.setPersistentStore(MWCommonUtil.getStringValue(serverObj,"persistent-store"));
//                jmsServer.setName((String) serverObj.get("name"));
                jmsServer.setName(MWCommonUtil.getStringValue(serverObj,"name"));
//                jmsServer.setTarget((String) serverObj.get("target"));
                jmsServer.setTarget(MWCommonUtil.getStringValue(serverObj,"target"));
//                jmsServer.setHostingTemporaryDestinations((Boolean) serverObj.get("hosting-temporary-destinations"));
                jmsServer.setHostingTemporaryDestinations(MWCommonUtil.getBooleanValue(serverObj,"hosting-temporary-destinations"));
                jmsServerList.add(jmsServer);
            }
            jms.setJmsServer(jmsServerList);
        }

        return jms;
    }
}