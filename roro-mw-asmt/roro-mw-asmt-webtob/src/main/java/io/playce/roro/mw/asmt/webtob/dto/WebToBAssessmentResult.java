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
package io.playce.roro.mw.asmt.webtob.dto;

import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
public class WebToBAssessmentResult extends MiddlewareAssessmentResult {

    @Getter
    @Setter
    @ToString
    public static class Engine extends MiddlewareAssessmentResult.Engine {
        private String path;
        private String version;
        private String vendor;
        private Date scannedDate;
        private String name;
        private String runUser;

    }

    @Getter
    @Setter
    @ToString
    public static class Instance extends MiddlewareAssessmentResult.Instance {
        private WebToBAssessmentResult.Domain domain;
        private List<WebToBAssessmentResult.Node> nodes;
        private List<WebToBAssessmentResult.SvrGroup> svrGroups;
        private List<WebToBAssessmentResult.Server> servers;
        private List<WebToBAssessmentResult.Uri> uris;
        private List<WebToBAssessmentResult.Ext> exts;
        private List<WebToBAssessmentResult.ErrorDocument> errorDocuments;
        private List<WebToBAssessmentResult.ReverseProxy> reverseProxies;
        private List<WebToBAssessmentResult.Access> accesses;
        private List<WebToBAssessmentResult.Logging> loggings;
        private List<WebToBAssessmentResult.Alias> aliases;
        private List<WebToBAssessmentResult.Vhost> vhosts;
        private List<WebToBAssessmentResult.SSL> ssls;
        private List<WebToBAssessmentResult.HthThread> hthThread;
        private List<WebToBAssessmentResult.ConfigFile> configFiles;

    }

    @Getter
    @Setter
    @ToString
    public static class Domain {
        private String name;
    }

    @Getter
    @Setter
    @ToString
    public static class Node {
        private String name;
        private String webTobDir;
        private String shmKey;
        private String docRoot;
        private String port;
        private String hth;
        private String user;
        private String group;
        private String logging;
        private String errorLog;
        private String sysLog;
        private String jsvPort;

        private String nodeName;
        private String errorDocument;
        private String serviceOrder;
        private String ipcPerm;
        private String urlRewrite;
        private String urlRewriteConfig;
        private String rpafHeader;
        private String keepAliveTimeout;
        private String cacheEntry;
        private String cacheMaxFileSize;
        private String cacheRefreshImage;
        private String cacheRefreshHtml;
        private String cacheRefreshDir;
        private String cacheRefreshJsv;
        private String maxCacheMemorySize;
        private String forceCacheModificationCheck;
        private String dosBlock;
        private String dosBlockTableSize;
        private String dosBlockPageCount;
        private String dosBlockPageInterval;
        private String dosBlockSiteCount;
        private String dosBlockSiteInterval;
        private String dosBlockPeriod;
        private String dosBlockWhiteList;
        private String options;
    }

    @Getter
    @Setter
    @ToString
    public static class HthThread {
        private String name;
        private String workerThreads;
    }

    @Getter
    @Setter
    @ToString
    public static class SvrGroup {
        private String name;
        private String nodeName;
        private String svrType;
        private String vhostName;
    }

    @Getter
    @Setter
    @ToString
    public static class Server {
        private String name;
        private String svgName;
        private String minProc;
        private String maxProc;
        private String asqCount;
        private String httpOutBufSize;
        private String httpInBufSize;
        private String flowControl;
        private String schedule;
        private String options;
    }

    @Getter
    @Setter
    @ToString
    public static class Uri {
        private String name;
        private String uri;
        private String svrType;
        private String svrName;
        private String match;
        private String accessName;
        private String vhostName;
        private String gotoExt;
    }

    @Getter
    @Setter
    @ToString
    public static class Alias {
        private String name;
        private String uri;
        private String realPath;
    }

    @Getter
    @Setter
    @ToString
    public static class Vhost {
        private String vhostName;
        private String docRoot;
        private String nodeName;
        private String usrLogDir;
        private String iconDir;
        private String userDir;
        private String envFile;
        private String indexName;
        private String logging;
        private String errorLog;
        private String hostName;
        private String hostAlias;
        private String port;
        private String errorDocument;
        private String serviceOrder;
        private String urlRewrite;
        private String urlRewriteConfig;
        private String keepAliveTimeout;
        private String sslFlag;
        private String sslName;
    }

    @Getter
    @Setter
    @ToString
    public static class SSL {
        private String name;
        private String protocols;
        private String requiredCiphers;
        private String passPhraseDialog;
        private String certificateFile;
        private String certificateKeyFile;
        private String certificateChainFile;
        private String caCertificatePath;
        private String caCertificateFile;
    }

    @Getter
    @Setter
    @ToString
    public static class Ext {
        private String name;
        private String mimeType;
        private String svrType;
        private String accessName;
    }

    @Getter
    @Setter
    @ToString
    public static class ErrorDocument {
        private String name;
        private String status;
        private String url;
    }

    @Getter
    @Setter
    @ToString
    public static class Access {
        private String name;
        private String order;
        private String deny;
    }

    @Getter
    @Setter
    @ToString
    public static class ReverseProxy {
        private String name;
        private String pathPrefix;
        private String serverPathPrefix;
        private String serverAddress;
        private String setHostHeader;
        private String setVhostName;
    }

    @Getter
    @Setter
    @ToString
    public static class Logging {
        private String name;
        private String format;
        private String fileName;
        private String Option;
    }

    @Getter
    @Setter
    @ToString
    public static class ConfigFile {
        private String name;
        private String path;
        private String contents;
    }

}
//end of WebToBAssessmentResult.java