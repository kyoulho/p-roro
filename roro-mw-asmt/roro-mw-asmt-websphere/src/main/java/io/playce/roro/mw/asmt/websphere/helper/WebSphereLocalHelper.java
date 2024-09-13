/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Jaeeon Bae       7월 26, 2021            First Draft.
 */
package io.playce.roro.mw.asmt.websphere.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.FileUtil;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.WasAnalyzerUtil;
import io.playce.roro.mw.asmt.websphere.dto.WebSphereAssessmentDto;
import io.playce.roro.mw.asmt.websphere.dto.WebSphereAssessmentResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.XML;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.modelmapper.ModelMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.playce.roro.common.util.CommandUtil.getFileContentCommand;
import static io.playce.roro.common.util.CommandUtil.getSshCommandResultTrim;
import static io.playce.roro.common.util.JsonUtil.getJsonArray;
import static io.playce.roro.common.util.JsonUtil.getJsonObject;
import static io.playce.roro.mw.asmt.util.MWCommonUtil.getLongValue;
import static io.playce.roro.mw.asmt.util.MWCommonUtil.getStringValue;


/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 2.0.0
 */
@Slf4j
public class WebSphereLocalHelper {

    //    private static final List<String> EXCLUDE_DEFAULT_APPLICATIONS = new ArrayList<>(Arrays.asList("WebSphereWSDM", "isclite", "OTiS", "ibmasyncrsp"));
    private static final List<String> EXCLUDE_DEFAULT_DATASOURCE = new ArrayList<>(Arrays.asList("built-in-derby-datasource", "OTiSDataSource", "DefaultEJBTimerDataSource"));

    private static final String PROFILE_REGISTRY_NAME = "profileRegistry.xml";
    private static final String SECURITY_NAME = "security.xml";
    private static final String CLUSTER_NAME = "cluster.xml";
    private static final String SERVER_INDEX_NAME = "serverindex.xml";
    private static final String SERVER_NAME = "server.xml";
    private static final String DYNAMIC_WEIGHT_CONTROLLER_NAME = "dynamicweightcontroller.xml";
    private static final String RESOURCE_NAME = "resources.xml";
    private static final String IBM_EDITION_METADATA = "ibm-edition-metadata.props";
    private static final String DEPLOYMENT_FILE = "deployment.xml";
    private static final String WC_DEFAULT_HOST = "WC_defaulthost";
    private static final String DIRECTORY_NAME_COMMAND = " | grep \\/$ | cut -d '/' -f 1 | tr ' ' '*'";

    public static WebSphereAssessmentResult.Engine getLocalEngine(TargetHost targetHost, String wasInstallRootPath, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("*** Execute Method : getLocalEngine ***");

        String wasInfoPath = File.separator + "properties" + File.separator + "version" + File.separator + "WAS.product";
        String path = (wasInstallRootPath + wasInfoPath).replaceAll("//", "/");
        File f = new File(path);

        WebSphereAssessmentResult.Engine engine = new WebSphereAssessmentResult.Engine();
        if (f.exists()) {
            String responseString = FileUtil.getFileContents(f);

            if (StringUtils.isNotEmpty(responseString)) {
                String xmlToJson = String.valueOf(XML.toJSONObject(responseString));

                log.debug("-- Get Product Info.");
                log.debug("-- File Path ==> {}", path);
                log.debug("-- File Content ==> \n{}", responseString);
                log.debug("-- File Convert JSON ==> \n{}", xmlToJson);

                // -- Start File Save -- //
                WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), path, responseString, CommonProperties.getWorkDir(), strategy);
                // -- End File Save -- //

                JSONObject jsonObject = getJsonObject(xmlToJson);

                engine = modelMapper.map(jsonObject.get("product"), WebSphereAssessmentResult.Engine.class);
            }
        } else {
            // 파일이 없으면 원격지의 정보를 가져온다.
            engine = getEngine(targetHost, wasInstallRootPath, modelMapper, strategy);
        }

        engine.setPath(wasInstallRootPath);

        return engine;
    }

    public static WebSphereAssessmentResult.Engine getEngine(TargetHost targetHost, String wasInstallRootPath, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("*** Execute Method : getEngine ***");

        String wasInfoPath = File.separator + "properties" + File.separator + "version" + File.separator + "WAS.product";
        String wasInfoCommand = getFileContentCommand(wasInstallRootPath + wasInfoPath);
        String responseString = getSshCommandResultTrim(targetHost, wasInfoCommand);

        WebSphereAssessmentResult.Engine engine = new WebSphereAssessmentResult.Engine();

        if (StringUtils.isNotEmpty(responseString)) {
            String xmlToJson = stringValueOf(XML.toJSONObject(responseString));

            log.debug("-- Get Product Info.");
            log.debug("-- File Path ==> {}", wasInstallRootPath + wasInfoPath);
            log.debug("-- File Content ==> \n{}", responseString);
            log.debug("-- File Convert JSON ==> \n{}", xmlToJson);

            // -- Start File Save -- //
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), wasInstallRootPath + wasInfoPath, responseString, CommonProperties.getWorkDir(), strategy);
            // -- End File Save -- //

            JSONObject jsonObject = getJsonObject(xmlToJson);

            engine = modelMapper.map(jsonObject.get("product"), WebSphereAssessmentResult.Engine.class);
        }

        engine.setPath(wasInstallRootPath);

        return engine;
    }

    public static List<WebSphereAssessmentResult.Profile> getLocalProfile(TargetHost targetHost, MiddlewareInventory middleware, String wasInstallRootPath, ModelMapper modelMapper) throws InterruptedException {
        log.debug("*** Execute Method : getLocalProfiles ***");

        String profilePath = wasInstallRootPath + File.separator + "properties" + File.separator + PROFILE_REGISTRY_NAME;
        // String profileCommand = getFileContentCommand(profilePath);
        String path = profilePath.replaceAll("//", "/");
        File f = new File(path);

        List<WebSphereAssessmentResult.Profile> profiles = new ArrayList<>();
        if (f.exists()) {
            String responseString = FileUtil.getFileContents(f);
            String xmlToJson = String.valueOf(XML.toJSONObject(responseString));

            log.debug("-- Get Profile Info.");
            log.debug("-- File Path ==> {}", path);
            log.debug("-- File Content ==> \n{}", responseString);
            log.debug("-- File Convert JSON ==> \n{}", xmlToJson);

            if (StringUtils.isNotEmpty(xmlToJson)) {
                JSONObject jsonObject = getJsonObject(xmlToJson);
                JSONObject profilesJsonObject = getJsonObject((JSONObject) jsonObject.get("profiles"));

                if (profilesJsonObject.get("profile") instanceof JSONObject) {
                    JSONObject profileJsonObject = getJsonObject((JSONObject) profilesJsonObject.get("profile"));

                    profiles.add(modelMapper.map(profileJsonObject, WebSphereAssessmentResult.Profile.class));
                } else {
                    JSONArray profileJsonArray = getJsonArray((JSONArray) profilesJsonObject.get("profile"));

                    for (Object profileObject : profileJsonArray) {
                        JSONObject profileJsonObject = getJsonObject((JSONObject) profileObject);
                        profiles.add(modelMapper.map(profileJsonObject, WebSphereAssessmentResult.Profile.class));
                    }
                }

                for (WebSphereAssessmentResult.Profile profile : profiles) {
//                    profile.setMinHeap(getHeapSize(targetHost, profile.getName(), "-Xms"));
//                    profile.setMaxHeap(getHeapSize(targetHost, profile.getName(), "-Xmx"));
//                    profile.setRunUser(getRunUser(targetHost, profile.getName()));
                    profile.setPath(profile.getPath().replaceAll(middleware.getEngineInstallationPath(), wasInstallRootPath).replaceAll("//", "/"));
                }
            }
        } else {
            profiles = getProfile(targetHost, wasInstallRootPath, modelMapper);
        }

        return profiles;
    }


    public static List<WebSphereAssessmentResult.Profile> getProfile(TargetHost targetHost, String wasInstallRootPath, ModelMapper modelMapper) throws InterruptedException {
        log.debug("*** Execute Method : getProfiles ***");

        String profilePath = wasInstallRootPath + File.separator + "properties" + File.separator + PROFILE_REGISTRY_NAME;
        String profileCommand = getFileContentCommand(profilePath);
        String responseString = getSshCommandResultTrim(targetHost, profileCommand);

        String xmlToJson = stringValueOf(XML.toJSONObject(responseString));

        log.debug("-- Get Profile Info.");
        log.debug("-- File Path ==> {}", profilePath);
        log.debug("-- File Content ==> \n{}", responseString);
        log.debug("-- File Convert JSON ==> \n{}", xmlToJson);

        List<WebSphereAssessmentResult.Profile> profiles = new ArrayList<>();

        if (StringUtils.isNotEmpty(xmlToJson)) {
            JSONObject jsonObject;
            try {
                jsonObject = getJsonObject((JSONObject) new JSONParser().parse(xmlToJson));
            } catch (ParseException e) {
                log.error("xml parse error: {}", e.getMessage(), e);
                throw new RoRoException(e.getMessage());
            }
            JSONObject profilesJsonObject = getJsonObject((JSONObject) jsonObject.get("profiles"));

            if (profilesJsonObject.get("profile") instanceof JSONObject) {
                JSONObject profileJsonObject = getJsonObject((JSONObject) profilesJsonObject.get("profile"));

                profiles.add(modelMapper.map(profileJsonObject, WebSphereAssessmentResult.Profile.class));
            } else {
                JSONArray profileJsonArray = getJsonArray((JSONArray) profilesJsonObject.get("profile"));

                for (Object profileObject : profileJsonArray) {
                    JSONObject profileJsonObject = getJsonObject((JSONObject) profileObject);
                    profiles.add(modelMapper.map(profileJsonObject, WebSphereAssessmentResult.Profile.class));
                }
            }

//            for (WebSphereAssessmentResult.Profile profile : profiles) {
//                profile.setMinHeap(getHeapSize(targetHost, profile.getName(), "-Xms"));
//                profile.setMaxHeap(getHeapSize(targetHost, profile.getName(), "-Xmx"));
//                profile.setRunUser(getRunUser(targetHost, profile.getName()));
//            }
        }

        return profiles;
    }

    public static WebSphereAssessmentDto.DirectoryStructure getLocalDirectoryStructure(List<WebSphereAssessmentResult.Profile> profiles, TargetHost targetHost, ModelMapper modelMapper, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("*** Execute Method : getLocalDirectoryStructure ***");
        WebSphereAssessmentDto.DirectoryStructure directoryStructure = new WebSphereAssessmentDto.DirectoryStructure();

        // -- Start  Profile --//
        List<WebSphereAssessmentDto.ProfileFileInfo> profileFileInfos = new ArrayList<>();
        for (WebSphereAssessmentResult.Profile profile : profiles) {
            profileFileInfos.add(modelMapper.map(profile, WebSphereAssessmentDto.ProfileFileInfo.class));
        }
        directoryStructure.setProfileFileInfos(profileFileInfos);
        // -- End   Profile --//

        // -- Start  Cell --//
        for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
            List<WebSphereAssessmentDto.CellFileInfo> cellFileInfos = WebSphereLocalHelper.getLocalCellFileInfos(targetHost, profileFileInfo);
            profileFileInfo.setCellFileInfos(cellFileInfos);
        }
        // -- End   Cell --//

        // -- Start  Cluster and Node --//
        // Cluster, Node는 동일 레벨.
        for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
            for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                List<WebSphereAssessmentDto.ClusterFileInfo> clusterFileInfos = WebSphereLocalHelper.getLocalClusterFileInfos(targetHost, cellFileInfo, commandConfig, strategy);
                List<WebSphereAssessmentDto.NodeFileInfo> nodeFileInfos = WebSphereLocalHelper.getLocalNodeFileInfos(targetHost, cellFileInfo);

                cellFileInfo.setClusterFileInfos(clusterFileInfos);
                cellFileInfo.setNodeFileInfos(nodeFileInfos);
            }
        }
        // -- End   Cluster and Node --//

        // -- Start  Server --//
        for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
            for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                for (WebSphereAssessmentDto.NodeFileInfo nodeFileInfo : cellFileInfo.getNodeFileInfos()) {
                    List<WebSphereAssessmentDto.ServerFileInfo> serverFileInfos = WebSphereLocalHelper.getLocalServerFileInfos(targetHost, nodeFileInfo, commandConfig, strategy);

                    nodeFileInfo.setServerFileInfos(serverFileInfos);
                }
            }
        }
        // -- End  Server --//

        return directoryStructure;
    }

    public static List<WebSphereAssessmentDto.ClusterParse> getLocalCluster(TargetHost targetHost, MiddlewareInventory middleware, WebSphereAssessmentDto.ClusterFileInfo clusterFileInfo, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("*** Execute Method : getLocalCluster ***");
        String clusterFilePath = clusterFileInfo.getPath() + File.separator + CLUSTER_NAME;

        clusterFilePath.replaceAll(middleware.getEngineInstallationPath(), middleware.getConfigFilePath()).replaceAll("//", "/");
        File f = new File(clusterFilePath);
        if (f.exists()) {
            String responseString = FileUtil.getFileContents(f);

            // -- Start File Save -- //
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), clusterFilePath, responseString, CommonProperties.getWorkDir(), strategy);
            // -- End File Save -- //

            String xmlToJson = XML.toJSONObject(responseString).toString();
            log.debug("===> {}", CLUSTER_NAME);
            log.debug(xmlToJson);

            if (StringUtils.isNotEmpty(xmlToJson)) {
                JSONObject jsonObject = getJsonObject(xmlToJson);
                JSONObject xmiJsonObject = getJsonObject((JSONObject) jsonObject.get("xmi:XMI"));
                JSONArray serverJsonArray = new JSONArray();

                if (xmiJsonObject.isEmpty()) {
                    if (jsonObject.get("topology.cluster:ServerCluster") instanceof JSONObject) {
                        serverJsonArray.add(getJsonObject((JSONObject) jsonObject.get("topology.cluster:ServerCluster")));
                    } else {
                        serverJsonArray = getJsonArray((JSONArray) jsonObject.get("topology.cluster:ServerCluster"));
                    }
                } else {
                    if (xmiJsonObject.get("topology.cluster:ServerCluster") instanceof JSONObject) {
                        serverJsonArray.add(getJsonObject((JSONObject) xmiJsonObject.get("topology.cluster:ServerCluster")));
                    } else {
                        serverJsonArray = getJsonArray((JSONArray) xmiJsonObject.get("topology.cluster:ServerCluster"));
                    }
                }

                List<WebSphereAssessmentDto.ClusterParse> clusterParses = new ArrayList<>();

                for (Object serverJsonObject : serverJsonArray) {
                    JSONObject serverClusterJsonObject = getJsonObject((JSONObject) serverJsonObject);

                    WebSphereAssessmentDto.ClusterParse clusterParse = new WebSphereAssessmentDto.ClusterParse();
                    clusterParse.setName(stringValueOf(serverClusterJsonObject.get("name")));
                    clusterParse.setNodeGroupName(stringValueOf(serverClusterJsonObject.get("nodeGroupName")));

                    List<WebSphereAssessmentDto.ClusterMember> clusterMembers = new ArrayList<>();

                    if (serverClusterJsonObject.get("members") instanceof JSONObject) {
                        JSONObject memberJsonObject = getJsonObject((JSONObject) serverClusterJsonObject.get("members"));

                        clusterMembers.add(modelMapper.map(memberJsonObject, WebSphereAssessmentDto.ClusterMember.class));
                    } else {
                        JSONArray memberJsonArray = getJsonArray((JSONArray) serverClusterJsonObject.get("members"));

                        for (Object memberObject : memberJsonArray) {
                            JSONObject memberJsonObject = getJsonObject((JSONObject) memberObject);
                            clusterMembers.add(modelMapper.map(memberJsonObject, WebSphereAssessmentDto.ClusterMember.class));
                        }
                    }

                    clusterParse.setMembers(clusterMembers);

                    clusterParses.add(clusterParse);
                }

                return clusterParses;
            }
        } else {
            return WebSphereHelper.ClusterHelper.getCluster(targetHost, clusterFileInfo, modelMapper, strategy);
        }

        return null;
    }

    public static WebSphereAssessmentDto.ResourceParse getLocalResource(TargetHost targetHost, MiddlewareInventory middleware, String pathName, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("*** Execute Method : getLocalResource ***");
        WebSphereAssessmentDto.ResourceParse resourceParse = new WebSphereAssessmentDto.ResourceParse();

        String resourcePath = pathName + File.separator + RESOURCE_NAME;

        resourcePath.replaceAll(middleware.getEngineInstallationPath(), middleware.getConfigFilePath()).replaceAll("//", "/");
        File f = new File(resourcePath);
        if (f.exists()) {
            String responseString = FileUtil.getFileContents(f);

            // -- Start File Save -- //
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), resourcePath, responseString, CommonProperties.getWorkDir(), strategy);
            // -- End File Save -- //

            String xmlToJson = XML.toJSONObject(responseString).toString();

            log.debug("===> {}", RESOURCE_NAME);
            log.debug(xmlToJson);

            JSONObject jsonObject = getJsonObject(xmlToJson);
            JSONObject xmiJsonObject = getJsonObject((JSONObject) jsonObject.get("xmi:XMI"));

            List<WebSphereAssessmentDto.JdbcProvider> jdbcProviders = new ArrayList<>();

            if (xmiJsonObject.get("resources.jdbc:JDBCProvider") instanceof JSONObject) {
                JSONObject jdbcProviderJsonObject = getJsonObject((JSONObject) xmiJsonObject.get("resources.jdbc:JDBCProvider"));

                jdbcProviders.add(getJdbcProvider(jdbcProviderJsonObject, modelMapper));
            } else {
                JSONArray jdbcProviderJsonArray = getJsonArray((JSONArray) xmiJsonObject.get("resources.jdbc:JDBCProvider"));

                for (Object jdbcProviderObject : jdbcProviderJsonArray) {
                    JSONObject jdbcProviderJsonObject = getJsonObject((JSONObject) jdbcProviderObject);

                    jdbcProviders.add(getJdbcProvider(jdbcProviderJsonObject, modelMapper));
                }
            }

            resourceParse.setJdbcProvider(jdbcProviders);

            return resourceParse;

        } else {
            return WebSphereHelper.DatabaseHelper.getResource(targetHost, pathName, modelMapper, strategy);
        }
    }

    public static List<WebSphereAssessmentResult.DataSource> getJdbcProvider(WebSphereAssessmentDto.ProfileFileInfo profileFileInfo, WebSphereAssessmentDto.ResourceParse resourceParse, String range) {
        log.debug("*** Execute Method : getJdbcProvider ***");
        List<WebSphereAssessmentResult.DataSource> dataSources = new ArrayList<>();

        for (WebSphereAssessmentDto.JdbcProvider jdbcProvider : resourceParse.getJdbcProvider()) {
            for (WebSphereAssessmentDto.Factories factories : jdbcProvider.getFactories()) {
                if (!EXCLUDE_DEFAULT_DATASOURCE.contains(factories.getName())) {
                    WebSphereAssessmentResult.DataSource dataSource = new WebSphereAssessmentResult.DataSource();
                    dataSource.setProfileName(profileFileInfo.getName());
                    dataSource.setDataSourceName(factories.getName());
                    dataSource.setJndiName(factories.getJndiName());
                    dataSource.setJdbcProvider(jdbcProvider.getName());
                    dataSource.setAuthDataAlias(factories.getAuthDataAlias());
                    dataSource.setRange(range);
                    dataSource.setMin(factories.getConnectionPool().getMinConnections());
                    dataSource.setMax(factories.getConnectionPool().getMaxConnections());
                    dataSource.setTimeout(factories.getConnectionPool().getConnectionTimeout());
                    dataSource.setConnectionUrl(getConnectionUrl(jdbcProvider.getImplementationClassName(),
                            factories.getPropertySet().getResourceProperties()));

                    dataSources.add(dataSource);
                }
            }
        }

        return dataSources;
    }

    private static WebSphereAssessmentDto.JdbcProvider getJdbcProvider(JSONObject jdbcProviderJsonObject, ModelMapper modelMapper) {
        log.debug("*** Execute Method : getJdbcProvider ***");
        WebSphereAssessmentDto.JdbcProvider jdbcProvider = new WebSphereAssessmentDto.JdbcProvider();

        jdbcProvider.setName(getStringValue(jdbcProviderJsonObject, "name"));
        jdbcProvider.setDescription(getStringValue(jdbcProviderJsonObject, "description"));
        jdbcProvider.setProviderType(getStringValue(jdbcProviderJsonObject, "providerType"));
        jdbcProvider.setImplementationClassName(getStringValue(jdbcProviderJsonObject, "implementationClassName"));
        jdbcProvider.setFactories(getFactories(jdbcProviderJsonObject.get("factories"), modelMapper));

        return jdbcProvider;
    }

    private static List<WebSphereAssessmentDto.Factories> getFactories(Object object, ModelMapper modelMapper) {
        log.debug("*** Execute Method : getFactories(Object object, ModelMapper modelMapper) ***");
        List<WebSphereAssessmentDto.Factories> factories = new ArrayList<>();

        if (object instanceof JSONObject) {
            JSONObject factoriesJsonObject = getJsonObject((JSONObject) object);
            factories.add(getFactories(factoriesJsonObject, modelMapper));
        } else {
            JSONArray factoriesJsonArray = getJsonArray((JSONArray) object);

            for (Object factoriesObject : factoriesJsonArray) {
                JSONObject factoriesJsonObject = getJsonObject((JSONObject) factoriesObject);
                WebSphereAssessmentDto.Factories factory = getFactories(factoriesJsonObject, modelMapper);
                factories.add(factory);
            }
        }

        return factories;
    }

    private static WebSphereAssessmentDto.Factories getFactories(JSONObject factoriesJsonObject, ModelMapper modelMapper) {
        log.debug("*** Execute Method : getFactories(JSONObject factoriesJsonObject, ModelMapper modelMapper) ***");
        WebSphereAssessmentDto.Factories factory = new WebSphereAssessmentDto.Factories();

        factory.setStatementCacheSize(getLongValue(factoriesJsonObject, "statementCacheSize"));
        factory.setName(getStringValue(factoriesJsonObject, "name"));
        factory.setDescription(getStringValue(factoriesJsonObject, "description"));
        factory.setCategory(getStringValue(factoriesJsonObject, "category"));
        factory.setJndiName(getStringValue(factoriesJsonObject, "jndiName"));
        factory.setAuthDataAlias(getStringValue(factoriesJsonObject, "authDataAlias"));
        factory.setConnectionPool(modelMapper.map(factoriesJsonObject.get("connectionPool"), WebSphereAssessmentDto.ConnectionPool.class));
        factory.setPropertySet(getPropertySet(factoriesJsonObject.get("propertySet"), modelMapper));

        return factory;
    }

    private static WebSphereAssessmentDto.PropertySet getPropertySet(Object object, ModelMapper modelMapper) {
        log.debug("*** Execute Method : getPropertySet ***");
        WebSphereAssessmentDto.PropertySet propertySet = new WebSphereAssessmentDto.PropertySet();
        JSONObject propertySetJsonObject = getJsonObject((JSONObject) object);

        List<WebSphereAssessmentDto.ResourceProperties> resourceProperties = new ArrayList<>();

        if (propertySetJsonObject.get("resourceProperties") instanceof JSONObject) {
            JSONObject resourcePropertyJsonObject = getJsonObject((JSONObject) propertySetJsonObject.get("resourceProperties"));
            WebSphereAssessmentDto.ResourceProperties resourceProperty = modelMapper.map(resourcePropertyJsonObject, WebSphereAssessmentDto.ResourceProperties.class);

            resourceProperties.add(resourceProperty);
        } else {
            JSONArray resourcePropertyJsonArray = getJsonArray((JSONArray) propertySetJsonObject.get("resourceProperties"));

            for (Object resourcePropertyObject : resourcePropertyJsonArray) {
                JSONObject resourcePropertyJsonObject = getJsonObject((JSONObject) resourcePropertyObject);
                WebSphereAssessmentDto.ResourceProperties resourceProperty = modelMapper.map(resourcePropertyJsonObject, WebSphereAssessmentDto.ResourceProperties.class);

                resourceProperties.add(resourceProperty);
            }
        }

        propertySet.setResourceProperties(resourceProperties);

        return propertySet;
    }

    private static String getConnectionUrl(String implementationClassName, List<WebSphereAssessmentDto.ResourceProperties> resourceProperties) {
        log.debug("*** Execute Method : getConnectionUrl ***");
        final String db2ClassName = "ibm";
        final String oracleClassName = "oracle";
        final String mssqlClassName = "sqlserver";

        String serverName = getResourceProperty(resourceProperties, "serverName");
        String portNumber = getResourceProperty(resourceProperties, "portNumber");
        String databaseName = getResourceProperty(resourceProperties, "databaseName");
        String connectionUrl = "";

        if (implementationClassName.contains(db2ClassName)) {
            if (implementationClassName.toLowerCase().contains("com.ibm.db2.jdbc.app")) {
                connectionUrl = "jdbc:db2:" + databaseName;
            } else if (implementationClassName.toLowerCase().contains("com.ibm.as400")) {
                connectionUrl = "jdbc:as400://" + serverName;
            } else {
                // example : com.ibm.db2.jcc
                if (StringUtils.isEmpty(portNumber)) {
                    connectionUrl = "jdbc:db2://" + serverName + "/" + databaseName;
                } else {
                    connectionUrl = "jdbc:db2://" + serverName + ":" + portNumber + "/" + databaseName;
                }
            }
        } else if (implementationClassName.contains(oracleClassName)) {
            connectionUrl = getResourceProperty(resourceProperties, "URL");
        } else if (implementationClassName.contains(mssqlClassName)) {
            if (StringUtils.isEmpty(portNumber)) {
                connectionUrl = "jdbc:sqlserver://" + serverName + ";DatabaseName=" + databaseName;
            } else {
                connectionUrl = "jdbc:sqlserver://" + serverName + ":" + portNumber + ";DatabaseName=" + databaseName;
            }
        } else {
            connectionUrl = getResourceProperty(resourceProperties, "URL");
        }

        return connectionUrl;
    }

    private static String getResourceProperty(List<WebSphereAssessmentDto.ResourceProperties> resourceProperties, String attribute) {
        log.debug("*** Execute Method : getResourceProperty ***");
        String attributeValue = "";

        for (WebSphereAssessmentDto.ResourceProperties resourceProperty : resourceProperties) {
            if (resourceProperty.getName().equals(attribute)) {
                attributeValue = resourceProperty.getValue();
                break;
            }
        }

        return attributeValue;
    }

    public static List<WebSphereAssessmentDto.SecurityParse> getLocalSecurity(TargetHost targetHost, MiddlewareInventory middleware, String pathName, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("*** Execute Method : getLocalSecurity ***");
        List<WebSphereAssessmentDto.SecurityParse> securityParses = new ArrayList<>();

        String securityPath = pathName + File.separator + SECURITY_NAME;
        String securityCommand = getFileContentCommand(securityPath);
        String responseString = getSshCommandResultTrim(targetHost, securityCommand);

        securityPath.replaceAll(middleware.getEngineInstallationPath(), middleware.getConfigFilePath()).replaceAll("//", "/");
        File f = new File(securityPath);

        if (f.exists()) {
            // -- Start File Save -- //
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), securityPath, responseString, CommonProperties.getWorkDir(), strategy);
            // -- End File Save -- //

            String xmlToJson = XML.toJSONObject(responseString).toString();

            JSONObject jsonObject = getJsonObject(xmlToJson);
            JSONObject securityJsonObject = getJsonObject((JSONObject) jsonObject.get("security:Security"));

            if (securityJsonObject.get("authDataEntries") instanceof JSONObject) {
                JSONObject authDataJsonObject = getJsonObject((JSONObject) securityJsonObject.get("authDataEntries"));

                securityParses.add(modelMapper.map(authDataJsonObject, WebSphereAssessmentDto.SecurityParse.class));
            } else {
                JSONArray authDataJsonArray = getJsonArray((JSONArray) securityJsonObject.get("authDataEntries"));

                for (Object authDataObject : authDataJsonArray) {
                    JSONObject authDataJsonObject = getJsonObject((JSONObject) authDataObject);
                    securityParses.add(modelMapper.map(authDataJsonObject, WebSphereAssessmentDto.SecurityParse.class));
                }
            }
        } else {
            return WebSphereHelper.DatabaseHelper.getSecurity(targetHost, pathName, modelMapper, strategy);
        }

        return securityParses;
    }

    public static WebSphereAssessmentDto.ServerIndex getLocalServerIndex(TargetHost targetHost, MiddlewareInventory middleware, WebSphereAssessmentDto.NodeFileInfo nodeFileInfo, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("*** Execute Method : getLocalServerIndex ***");
        String serverIndexNamePath = nodeFileInfo.getPath() + File.separator + SERVER_INDEX_NAME;

        serverIndexNamePath.replaceAll(middleware.getEngineInstallationPath(), middleware.getConfigFilePath()).replaceAll("//", "/");
        File f = new File(serverIndexNamePath);

        WebSphereAssessmentDto.ServerIndex serverIndex = new WebSphereAssessmentDto.ServerIndex();

        if (f.exists()) {
            String responseString = FileUtil.getFileContents(f);

            // -- Start File Save -- //
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), serverIndexNamePath, responseString, CommonProperties.getWorkDir(), strategy);
            // -- End File Save -- //

            if (StringUtils.isNotEmpty(responseString)) {
                String xmlToJson = XML.toJSONObject(responseString).toString();
                log.debug("==> serverindex.xml");
                log.debug(xmlToJson);

                JSONObject jsonObject = getJsonObject(xmlToJson);
                JSONObject serverIndexJsonObject = getJsonObject((JSONObject) jsonObject.get("serverindex:ServerIndex"));

                serverIndex.setHostName(getStringValue(serverIndexJsonObject, "hostName"));
                serverIndex.setServerEntries(getServerEntries(serverIndexJsonObject.get("serverEntries"), modelMapper));
            }
        } else {
            serverIndex = WebSphereHelper.NodeHelper.getServerIndex(targetHost, nodeFileInfo, modelMapper, strategy);
        }

        return serverIndex;
    }

    private static List<WebSphereAssessmentDto.ServerEntries> getServerEntries(Object object, ModelMapper modelMapper) {
        log.debug("*** Execute Method : getServerEntries ***");
        List<WebSphereAssessmentDto.ServerEntries> serverEntries = new ArrayList<>();

        if (object instanceof JSONObject) {
            JSONObject serverEntryJsonObject = getJsonObject((JSONObject) object);
            serverEntries.add(getServerEntry(serverEntryJsonObject, modelMapper));
        } else {
            JSONArray serverEntryJsonArray = getJsonArray((JSONArray) object);

            for (Object serverEntryObject : serverEntryJsonArray) {
                JSONObject serverEntriesJsonObject = getJsonObject((JSONObject) serverEntryObject);
                serverEntries.add(getServerEntry(serverEntriesJsonObject, modelMapper));
            }
        }

        return serverEntries;
    }

    private static WebSphereAssessmentDto.ServerEntries getServerEntry(Object object, ModelMapper modelMapper) {
        log.debug("*** Execute Method : getServerEntry ***");
        JSONObject serverEntryJsonObject = getJsonObject((JSONObject) object);

        WebSphereAssessmentDto.ServerEntries serverEntry = new WebSphereAssessmentDto.ServerEntries();
        serverEntry.setServerType(getStringValue(serverEntryJsonObject, "serverType"));
        serverEntry.setServerName(getStringValue(serverEntryJsonObject, "serverName"));

        List<WebSphereAssessmentDto.SpecialEndpoints> specialEndpoints = new ArrayList<>();
        JSONArray specialEndpointsJsonArray = getJsonArray((JSONArray) serverEntryJsonObject.get("specialEndpoints"));

        for (Object specialEndpointObject : specialEndpointsJsonArray) {
            JSONObject specialEndpointJsonObject = getJsonObject((JSONObject) specialEndpointObject);
            specialEndpoints.add(modelMapper.map(specialEndpointJsonObject, WebSphereAssessmentDto.SpecialEndpoints.class));
        }

        serverEntry.setSpecialEndpoints(specialEndpoints);
        serverEntry.setDeployedApplications(getDeployedApplications(serverEntryJsonObject));

        return serverEntry;
    }

    private static List<String> getDeployedApplications(Object object) {
        log.debug("*** Execute Method : getDeployedApplications ***");
        JSONObject serverEntryJsonObject = getJsonObject((JSONObject) object);

        List<String> deployedApplications = new ArrayList<>();

        if (serverEntryJsonObject.get("deployedApplications") == null) {
            return null;
        } else {
            if (serverEntryJsonObject.get("deployedApplications") instanceof String) {
                deployedApplications.add(serverEntryJsonObject.get("deployedApplications").toString());
            } else {
                JSONArray deployedApplicationsJsonArray = getJsonArray((JSONArray) serverEntryJsonObject.get("deployedApplications"));

                for (Object deployedApplicationsObject : deployedApplicationsJsonArray) {
                    deployedApplications.add(deployedApplicationsObject.toString());
                }
            }
        }

        return deployedApplications;
    }

    public static List<WebSphereAssessmentDto.CellFileInfo> getLocalCellFileInfos(TargetHost targetHost, WebSphereAssessmentDto.ProfileFileInfo profileFileInfo) throws InterruptedException {
        log.debug("*** Execute Method : getLocalCellFileInfos ***");
        String cellPath = profileFileInfo.getPath() + File.separator + "config" + File.separator + "cells";
        File f = new File(cellPath);

        List<WebSphereAssessmentDto.CellFileInfo> cellFileInfos = new ArrayList<>();
        if (f.exists()) {
            File[] files = f.listFiles();

            if (files != null) {
                for (File tempFile : files) {
                    if (tempFile.isDirectory()) {
                        WebSphereAssessmentDto.CellFileInfo cellFileInfo = new WebSphereAssessmentDto.CellFileInfo();
                        cellFileInfo.setPath(profileFileInfo.getPath() + File.separator + "config" + File.separator + "cells" + File.separator + tempFile.getName());
                        cellFileInfo.setName(tempFile.getName());
                        cellFileInfos.add(cellFileInfo);
                    }
                }
            }
        } else {
            cellFileInfos = WebSphereHelper.CellHelper.getCellFileInfos(targetHost, profileFileInfo);
        }

        return cellFileInfos;
    }

    public static List<WebSphereAssessmentDto.ClusterFileInfo> getLocalClusterFileInfos(TargetHost targetHost, WebSphereAssessmentDto.CellFileInfo cellFileInfo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("*** Execute Method : getLocalClusterFileInfos ***");
        List<WebSphereAssessmentDto.ClusterFileInfo> clusterFileInfos = new ArrayList<>();
        String clusterPath = cellFileInfo.getPath() + File.separator + "clusters";

        File f = new File(clusterPath);
        if (f.exists()) {
            File[] files = f.listFiles();

            if (files != null) {
                for (File tempFile : files) {
                    if (tempFile.isDirectory()) {
                        WebSphereAssessmentDto.ClusterFileInfo clusterFileInfo = new WebSphereAssessmentDto.ClusterFileInfo();
                        clusterFileInfo.setPath(cellFileInfo.getPath() + File.separator + "clusters" + File.separator + tempFile.getName());
                        clusterFileInfo.setName(tempFile.getName());
                        clusterFileInfos.add(clusterFileInfo);
                    }
                }
            }
        } else {
            clusterFileInfos = WebSphereHelper.ClusterHelper.getClusterFileInfos(targetHost, cellFileInfo, commandConfig, strategy);
        }

        return clusterFileInfos;
    }

    public static List<WebSphereAssessmentDto.NodeFileInfo> getLocalNodeFileInfos(TargetHost targetHost, WebSphereAssessmentDto.CellFileInfo cellFileInfo) throws InterruptedException {
        log.debug("*** Execute Method : getLocalNodeFileInfos ***");
        String nodePath = cellFileInfo.getPath() + File.separator + "nodes";

        File f = new File(nodePath);
        List<WebSphereAssessmentDto.NodeFileInfo> nodeFileInfos = new ArrayList<>();

        if (f.exists()) {
            File[] files = f.listFiles();

            if (files != null) {
                for (File tempFile : files) {
                    if (tempFile.isDirectory()) {
                        WebSphereAssessmentDto.NodeFileInfo nodeFileInfo = new WebSphereAssessmentDto.NodeFileInfo();
                        nodeFileInfo.setPath(cellFileInfo.getPath() + File.separator + "nodes" + File.separator + tempFile.getName());
                        nodeFileInfo.setName(tempFile.getName());
                        nodeFileInfos.add(nodeFileInfo);
                    }
                }
            }
        } else {
            nodeFileInfos = WebSphereHelper.NodeHelper.getNodeFileInfos(targetHost, cellFileInfo);
        }

        return nodeFileInfos;
    }

    public static List<WebSphereAssessmentDto.ServerFileInfo> getLocalServerFileInfos(TargetHost targetHost, WebSphereAssessmentDto.NodeFileInfo nodeFileInfo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("*** Execute Method : getLocalServerFileInfos ***");
        List<WebSphereAssessmentDto.ServerFileInfo> serverFileInfos = new ArrayList<>();
        String serverPath = nodeFileInfo.getPath() + File.separator + "servers";

        File f = new File(serverPath);

        if (f.exists()) {
            File[] files = f.listFiles();

            if (files != null) {
                for (File tempFile : files) {
                    if (tempFile.isDirectory()) {
                        WebSphereAssessmentDto.ServerFileInfo serverFileInfo = new WebSphereAssessmentDto.ServerFileInfo();
                        serverFileInfo.setPath(nodeFileInfo.getPath() + File.separator + "servers" + File.separator + tempFile.getName());
                        serverFileInfo.setName(tempFile.getName());
                        serverFileInfos.add(serverFileInfo);
                    }
                }
            }
        } else {
            serverFileInfos = WebSphereHelper.ServerHelper.getServerFileInfos(targetHost, nodeFileInfo, commandConfig, strategy);
        }

        return serverFileInfos;
    }

    public static String getLocalHostName(TargetHost targetHost, MiddlewareInventory middleware, WebSphereAssessmentDto.NodeFileInfo nodeFileInfo, String serverName, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("*** Execute Method : getLocalHostName ***");
        WebSphereAssessmentDto.ServerIndex serverIndex = WebSphereLocalHelper.getLocalServerIndex(targetHost, middleware, nodeFileInfo, modelMapper, strategy);

        List<WebSphereAssessmentDto.ServerEntries> serverEntries = serverIndex.getServerEntries();

        for (WebSphereAssessmentDto.ServerEntries serverEntry : serverEntries) {
            if (serverEntry.getServerName().equals(serverName)) {
                return serverIndex.getHostName();
            }
        }

        return "";
    }

    public static Integer getLocalServerPort(TargetHost targetHost, MiddlewareInventory middleware, WebSphereAssessmentDto.NodeFileInfo nodeFileInfo, String clusterMemberName, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("*** Execute Method : getLocalServerPort ***");
        WebSphereAssessmentDto.ServerIndex serverIndex = WebSphereLocalHelper.getLocalServerIndex(targetHost, middleware, nodeFileInfo, modelMapper, strategy);

        List<WebSphereAssessmentDto.ServerEntries> serverEntries = serverIndex.getServerEntries();

        for (WebSphereAssessmentDto.ServerEntries serverEntry : serverEntries) {
            if (serverEntry.getServerName().equals(clusterMemberName)) {
                List<WebSphereAssessmentDto.SpecialEndpoints> specialEndpoints = serverEntry.getSpecialEndpoints();
                for (WebSphereAssessmentDto.SpecialEndpoints specialEndpoint : specialEndpoints) {
                    if (specialEndpoint.getEndPointName().equals(WC_DEFAULT_HOST)) {
                        return specialEndpoint.getEndPoint().getPort();
                    }
                }
            }
        }

        return null;
    }

    public static WebSphereAssessmentResult.Config getLocalConfig(TargetHost targetHost, MiddlewareInventory middleware, WebSphereAssessmentDto.ServerFileInfo serverFileInfo, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("*** Execute Method : getLocalConfig ***");
        String serverPath = serverFileInfo.getPath() + File.separator + SERVER_NAME;

        serverPath.replaceAll(middleware.getEngineInstallationPath(), middleware.getConfigFilePath()).replaceAll("//", "/");
        File f = new File(serverPath);
        WebSphereAssessmentResult.Config config = new WebSphereAssessmentResult.Config();

        if (f.exists()) {
            String responseString = FileUtil.getFileContents(f);

            // -- Start File Save -- //
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), serverPath, responseString, CommonProperties.getWorkDir(), strategy);
            // -- End File Save -- //

            String xmlToJson = XML.toJSONObject(responseString).toString();
            log.debug("==> server.xml");
            log.debug(xmlToJson);

            WebSphereAssessmentResult.JvmEntries jvmEntries = new WebSphereAssessmentResult.JvmEntries();

            JSONObject jsonObject = getJsonObject(xmlToJson);
            JSONObject serverJsonObject = getJsonObject((JSONObject) jsonObject.get("process:Server"));
            JSONObject processDefinitionsJsonObject = getJsonObject((JSONObject) serverJsonObject.get("processDefinitions"));
            JSONObject ioRedirectJsonObject = getJsonObject((JSONObject) processDefinitionsJsonObject.get("ioRedirect"));
            JSONObject jvmEntriesJsonObject = getJsonObject((JSONObject) processDefinitionsJsonObject.get("jvmEntries"));

            // System Properties
            List<WebSphereAssessmentResult.SystemProperty> systemProperties = new ArrayList<>();

            if (jvmEntriesJsonObject.get("systemProperties") instanceof JSONObject) {
                JSONObject systemPropertyJsonObject = getJsonObject((JSONObject) jvmEntriesJsonObject.get("systemProperties"));
                systemProperties.add(modelMapper.map(systemPropertyJsonObject, WebSphereAssessmentResult.SystemProperty.class));
            } else {
                JSONArray systemPropertiesJsonArray = getJsonArray((JSONArray) jvmEntriesJsonObject.get("systemProperties"));

                for (Object systemPropertiesObject : systemPropertiesJsonArray) {
                    JSONObject systemPropertyJsonObject = getJsonObject((JSONObject) systemPropertiesObject);
                    systemProperties.add(modelMapper.map(systemPropertyJsonObject, WebSphereAssessmentResult.SystemProperty.class));
                }
            }

            // Boot ClassPath
            List<String> bootClasspath = new ArrayList<>();

            if (jvmEntriesJsonObject.get("bootClasspath") instanceof String) {
                bootClasspath.add(jvmEntriesJsonObject.get("bootClasspath").toString());
            } else {
                JSONArray bootClasspathJsonArray = getJsonArray((JSONArray) jvmEntriesJsonObject.get("bootClasspath"));

                for (Object bootClasspathObject : bootClasspathJsonArray) {
                    bootClasspath.add(bootClasspathObject.toString());
                }
            }

            ObjectMapper objectMapper = new ObjectMapper();
            ConcurrentHashMap<String, Object> propertyMap;
            try {
                propertyMap = objectMapper.readValue(jvmEntriesJsonObject.toJSONString(), ConcurrentHashMap.class);
            } catch (JsonProcessingException e) {
                log.error("object mapper read value error: {}", e.getMessage(), e);
                throw new RoRoException(e.getMessage());
            }

            for (Map.Entry<String, Object> elem : propertyMap.entrySet()) {
                if (elem.getKey().contains("systemProperties")
                        || elem.getKey().contains("bootClasspath")
                        || elem.getKey().contains("xmi:id")) {
                    propertyMap.remove(elem.getKey());
                }
            }

            jvmEntries.setSystemProperties(systemProperties);
            jvmEntries.setBootClasspath(bootClasspath);
            jvmEntries.setProperties(propertyMap);
            config.setIoRedirect(modelMapper.map(ioRedirectJsonObject, WebSphereAssessmentResult.IoRedirect.class));
            config.setJvmEntries(jvmEntries);
        } else {
            config = WebSphereHelper.ServerHelper.getConfig(targetHost, serverFileInfo, modelMapper, strategy);
        }

        return config;
    }

    private static String stringValueOf(Object object) {
        return object == null ? "" : String.valueOf(object);
    }
}