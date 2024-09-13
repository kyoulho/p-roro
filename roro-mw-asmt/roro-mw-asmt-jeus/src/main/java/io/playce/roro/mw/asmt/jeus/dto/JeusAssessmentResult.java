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
 * Jaeeon Bae       11월 10, 2021            First Draft.
 */
package io.playce.roro.mw.asmt.jeus.dto;

import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
public class JeusAssessmentResult extends MiddlewareAssessmentResult {

    @Getter
    @Setter
    public static class Engine extends MiddlewareAssessmentResult.Engine {
        private String name;
        private String path;
        private String vendor;
        private String version;
        private String productionMode;
        private String description;
    }

    @Getter
    @Setter
    public static class Instance extends MiddlewareAssessmentResult.Instance {
        // private List<String> vmOptions;
        private List<Application> applications;
        private List<ConfigFile> configFiles;
        private Clusters clusters;
        private SessionClusterConfig sessionClusterConfig;
        private Resources resources;
        // private String minHeap;
        // private String maxHeap;
        private String javaVersion;
        private String javaVendor;
        // private String runUser;
        private List<Instances> instances;

    }

    @Getter
    @Setter
    @ToString
    public static class ConfigFile {
        private String path;
        private String contents;
    }

    @Getter
    @Setter
    public static class Instances {
        private String name;
        private String nodeName;
        private String status;
        private Engines engines;
        private Listeners listeners;
        private List<SystemLogging> logs;
        private JvmConfig jvmConfig;
        private WebAdminConfig webAdminConfig;
        private String useEjbEngine;
        private String useJmsEngine;
        private String useWebEngine;
        private String enableWebAdmin;
        private String minHeap;
        private String maxHeap;
        private String runUser;
        private String vmOption;
    }

    @Getter
    @Setter
    public static class WebAdminConfig {
        private AllowedServer allowedServer;
    }

    @Getter
    @Setter
    public static class AllowedServer {
        private List<String> address;
    }

    @Getter
    @Setter
    public static class Engines {
        /*
         *  Jeus6
         * */
        private List<EngineContainer> engineContainer;

        /*
         *  Jeus7+
         * */
        private List<EjbEngine> ejbEngine;
        private List<JmsEngine> jmsEngine;
        private List<WebEngine> webEngine;
    }

    @Getter
    @Setter
    public static class EngineContainer {
        private String name;
        private String sequentialStart;
        // private String commandOption;
        private List<EngineCommand> engineCommands;
        private List<SystemLogging> systemLogging;
        private String userClassPath;
        private String minHeap;
        private String maxHeap;
        private String runUser;
        private String vmOption;
    }

    @Getter
    @Setter
    public static class EngineCommand {
        private String name;
        private String type;
    }

    @Getter
    @Setter
    public static class EjbEngine {
        private String useDynamicProxyForEjb2;
        private TimerService timerService;
        private String resolution;
        private String enableUserNotify;
        private AsyncService asyncService;
        private ActiveManagement activeManagement;
    }


    @Getter
    @Setter
    public static class ActiveManagement {
        private String maxIdleTime;
        private String maxBlockedThread;
    }

    @Getter
    @Setter
    public static class AsyncService {
        private String threadMin;
        private String threadMax;
        private String accessTimeout;
    }

    @Getter
    @Setter
    public static class ThreadPool {
        private String period;
        private String min;
        private String max;
        private String keepAliveTime;
        private String maxQueue;
        private String maxIdleTime;
        private String maxWaitQueue;
    }

    @Getter
    @Setter
    public static class TimerService {
        private String supportPersistence;
        private String retrialInterval;
        private String maxRetrialCount;
        private ThreadPool threadPool;
    }

    @Getter
    @Setter
    public static class JmsEngine {
        private ThreadPool threadPool;
        private ServiceConfig serviceConfig;
        private List<ConnectionFactory> connectionFactory;
    }

    @Getter
    @Setter
    public static class ServiceConfig {
        private String name;
        private String listenerName;
        private String clientLimit;
        private String clientKeepaliveTimeout;
        private String checkSecurity;
    }

    @Getter
    @Setter
    public static class ConnectionFactory {
        private String type;
        private String requestBlockingTime;
        private String reconnectPeriod;
        private String reconnectInterval;
        private String reconnectEnabled;
        private String name;
        private String fixedClientId;
        private String exportName;
        private String brokerSelectionPolicy;
    }

    @Getter
    @Setter
    public static class WebEngine {
        private WebConnections webConnections;
        private SessionConfig sessionConfig;
        private Monitoring monitoring;
        private JspEngine jspEngine;
        private BlockedUrlPatterns blockedUrlPatterns;
        private String attachStacktraceOnError;
        private String asyncTimeoutMinThreads;
        private AccessLog accessLog;
        private Map<String, Object> undefined;
    }

    @Getter
    @Setter
    public static class Resources {
        private List<Database> databases;
    }

    @Getter
    @Setter
    public static class Database {
        private String vendor;
        private String user;
        private String autoCommit;
        private String supportXaEmulation;
        private String stmtQueryTimeout;
        private String serverName;
        private String portNumber;
        private String poolDestroyTimeout;
        private String password;
        private String loginTimeout;
        private String isolationLevel;
        private String exportName;
        private String description;
        private String databaseName;
        private String dataSourceType;
        private String dataSourceTarget;
        private String dataSourceId;
        private String dataSourceClassName;
        private ConnectionPool connectionPool;
        private List<DatabaseProperty> property;
    }

    @Getter
    @Setter
    public static class ConnectionPool {
        private WaitFreeConnection waitFreeConnection;
        private String useSqlTrace;
        private String stmtFetchSize;
        private String stmtCachingSize;
        private Pooling pooling;
        private String maxUseCount;
        private String keepConnectionHandleOpen;
        private String dbaTimeout;
        private ConnectionTrace connectionTrace;
        private String checkQuery;
        private String checkQueryPeriod;
    }

    @Getter
    @Setter
    public static class DatabaseProperty {
        private String name;
        private String type;
        private String value;
    }

    @Getter
    @Setter
    public static class ConnectionTrace {
        private String getConnectionTrace;
        private String enabled;
        private String autoCommitTrace;
    }

    @Getter
    @Setter
    public static class Pooling {
        private String step;
        private String period;
        private String min;
        private String max;
    }

    @Getter
    @Setter
    public static class WaitFreeConnection {
        private String waitTime;
        private String enableWait;
    }

    @Getter
    @Setter
    public static class SystemLogging {
        private String formatterClass;
        private String level;
        private String name;
        private String useParentHandlers;
        private Handler handler;
    }


    @Getter
    @Setter
    public static class JvmConfig {
        private List<String> jvmOption;
    }

    @Getter
    @Setter
    public static class Listeners {
        private String base;
        private List<Listener> listeners;
    }

    @Getter
    @Setter
    public static class Listener {
        private String useNio;
        private String useDualSelector;
        private String selectors;
        private String selectTimeout;
        private String reservedThreadNum;
        private String readTimeout;
        private String name;
        private String listenPort;
        private String backlog;
    }

    @Getter
    @Setter
    public static class Clusters {
        private List<SessionServer> sessionServers;
        private List<Cluster> cluster;
        private Map<String, Object> properties;
    }

    @Getter
    @Setter
    public static class SessionClusterConfig {
        private String usingSessionCluster;
        private SessionClusters sessionClusters;
        private ClusterConfig commonClusterConfig;

    }

    @Getter
    @Setter
    public static class CommonClusterConfig {
        private ClusterConfig clusterConfig;
    }

    @Getter
    @Setter
    public static class SessionClusters {
        private List<SessionCluster> sessionClusters;
    }

    @Getter
    @Setter
    public static class SessionCluster {
        private String name;
        private ClusterConfig clusterConfig;
    }

    @Getter
    @Setter
    public static class ClusterConfig {
        private JeusLoginManager jeusLoginManager;
        private String backupLevel;
        private String connectTimout;
        private String failoverDelay;
        private String readTimeout;
        private String reservedThreadNum;
        private String restartDelay;
        private String allowFailBack;
        private FileDb fileDB;
    }

    @Getter
    @Setter
    public static class JeusLoginManager {
        private String primary;
        private String secondary;
    }

    @Getter
    @Setter
    public static class SessionServer {
        private String type;
        private String replicatedServer;
        private String recoveryMode;
        private String connectTimout;
        private String readTimeout;
        private String backupTrigger;
    }

    @Getter
    @Setter
    public static class Cluster {
        private String name;
        private String status;
        private List<String> servers;
        private SessionRouterConfig sessionRouterConfig;
        private Map<String, Object> options;
    }

    @Getter
    @Setter
    public static class SessionRouterConfig {
        private String backupLevel;
        private String connectTimout;
        private String failoverDelay;
        private String readTimeout;
        private String reservedThreadNum;
        private String restartDelay;
        private String allowFailBack;
        private FileDb fileDB;
    }

    @Getter
    @Setter
    public static class FileDb {
        private String minHole;
        private String packingRate;
        private String passivationTimeout;
    }

    @Getter
    @Setter
    public static class Applications {
        private List<Application> application;
    }

    @Getter
    @Setter
    public static class Application {
        private String id;
        private String type;
        private List<String> target;
        private String sourcePath;
        private String deployedDate;
        private List<String> contextRoot;
        private Map<String, Object> options;

        // Cluster 추가.
        private List<String> clusterNames;
        private boolean isCluster;
    }

    @Getter
    @Setter
    public static class WebComponent {
        private String contextRoot;
    }

    @Getter
    @Setter
    public static class AccessLog {
        private String useParentHandlers;
        private String level;
        private Handler handler;
        private String formatterClass;
        private String format;
        private String enableHostNameLookup;
        private String enable;
    }

    @Getter
    @Setter
    public static class BlockedUrlPatterns {
        private List<String> encodedPattern;
        private String denyNullCharacter;
        private String denyLastSpaceCharacter;
        private List<String> decodedPattern;
    }

    @Getter
    @Setter
    public static class FileHandler {
        private String validDay;
        private String name;
        private String level;
        private String enableRotation;
        private String bufferSize;
        private String append;
        private String fileName;
        private String rotationDir;
    }

    @Getter
    @Setter
    public static class Handler {
        private FileHandler fileHandler;
    }

    @Getter
    @Setter
    public static class HttpListener {
        private ThreadPool threadPool;
        private String serverListenerRef;
        private String serverAccessControl;
        private String postdataReadTimeout;
        private String name;
        private String maxQuerystringSize;
        private String maxPostSize;
        private String maxParameterCount;
        private String maxHeaderSize;
        private String maxHeaderCount;

        //Jeus6
        private String listenerId;
        private String port;
        private String webtobAddress;
        private String outputBufferSize;
    }

    @Getter
    @Setter
    public static class WebToBConnector {
        private String name;
        private String maxQuerystringSize;
        private String maxPostSize;
        private String maxParameterCount;
        private String maxHeaderSize;
        private String maxHeaderCount;
        private String postdataReadTimeout;
        private String wjpVersion;
        private String registrationId;
        private NetworkAddress networkAddress;
        private WebToBThreadPool threadPool;
        private String hthCount;
        private String requestPrefetch;
        private String readTimeout;
        private String reconnectInterval;
        private String reconnectCountForBackup;


        @Getter
        @Setter
        public static class WebToBThreadPool {
            public String number;
            public ThreadStateNotify threadStateNotify;
        }

        @Getter
        @Setter
        public static class ThreadStateNotify {
            public String maxThreadActiveTime;
            public String interruptThread;
            public String activeTimeoutNotification;
            public String notifyThresholdRatio;
            public String restartThresholdRatio;
        }
    }


    @Getter
    @Setter
    public static class NetworkAddress {
        private String port;
        private String ipAddress;
    }

    @Getter
    @Setter
    public static class JspEngine {
        private String useInMemoryCompilation;
        private String keepGenerated;
        private String javaCompiler;
        private String gracefulJspReloadingPeriod;
        private String gracefulJspReloading;
        private String checkIncludedJspfile;
    }

    @Getter
    @Setter
    public static class Monitoring {
        private String checkThreadPool;
        private String checkSession;
        private String checkClassReload;
    }

    @Getter
    @Setter
    public static class SessionConfig {
        private TrackingMode trackingMode;
        private String timeout;
        private String shared;
        private SessionCookie sessionCookie;
        private String reloadPersistent;
        private String maxSessionCount;
    }

    @Getter
    @Setter
    public static class SessionCookie {
        private String version;
        private String secure;
        private String sameSite;
        private String maxAge;
        private String httpOnly;
        private String cookieName;
    }

    @Getter
    @Setter
    public static class TrackingMode {
        private String url;
        private String ssl;
        private String cookie;
    }

    @Getter
    @Setter
    public static class WebConnections {
        private List<HttpListener> httpListener;
        private List<HttpListener> webtobListener;
        private List<WebToBConnector> webToBConnector;
    }


}
//end of JeusAssessmentResult.java