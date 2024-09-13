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
 * Jaeeon Bae       11월 11, 2021            First Draft.
 */
package io.playce.roro.mw.asmt.webtob;

import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.AbstractMiddlewareAssessment;
import io.playce.roro.mw.asmt.MiddlewareAssessment;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.mw.asmt.util.WasAnalyzerUtil;
import io.playce.roro.mw.asmt.webtob.dto.WebToBAssessmentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.playce.roro.mw.asmt.AbstractMiddlewareAssessment.fileExists;
import static io.playce.roro.mw.asmt.AbstractMiddlewareAssessment.readUploadedFile;

@Component("WEBTOBAssessment")
@RequiredArgsConstructor
@Slf4j
public class WebToBAssessment implements MiddlewareAssessment {
    private final CommandConfig commandConfig;

    @Override
    public MiddlewareAssessmentResult assessment(TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {
        WebToBAssessmentResult assessmentResult = new WebToBAssessmentResult();
        String engineHome = middleware.getEngineInstallationPath();
        String version = null;

        if (StringUtils.isEmpty(engineHome)) {
            engineHome = middleware.getDomainHomePath();
        }

        if (StringUtils.isEmpty(engineHome)) {
            throw new RoRoException("Failed to get \"WEBTOBDIR\" environment for engine path. Please check environment for \"WEBTOBDIR\" \n");
        } else {
            engineHome = engineHome.trim();

            if (middleware.getEngineVersion() != null) {
                version = middleware.getEngineVersion();
            } else {
                String separator = strategy.getSeparator();
                String wscfl = engineHome + separator + "bin" + separator + "wscfl" + (strategy.isWindows() ? ".exe" : StringUtils.EMPTY);
                if (fileExists(targetHost, wscfl, commandConfig, strategy)) {
                    try {
                        // version = SSHUtil.executeCommand(targetHost, engineHome + separator + "bin" + separator + "wscfl -v 2>&1 | head -1");
                        version = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_VERSION, commandConfig, strategy, wscfl);
                    } catch (Exception e) {
                        RoRoException.checkInterruptedException(e);
                        log.error("execute error: {}", e.getMessage(), e);
                        throw new RoRoException(e.getMessage());
                    }
                }

                if (StringUtils.isEmpty(version) || !version.toLowerCase().startsWith("webtob")) {
                    String wsadmin = engineHome + separator + "bin" + separator + "wsadmin" + (strategy.isWindows() ? ".exe" : StringUtils.EMPTY);
                    if (fileExists(targetHost, wsadmin, commandConfig, strategy)) {
                        try {
                            // version = SSHUtil.executeCommand(targetHost, engineHome + separator + "bin" + separator + "wsadmin -v 2>&1 | head -1");
                            version = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_VERSION, commandConfig, strategy, wsadmin);
                        } catch (Exception e) {
                            RoRoException.checkInterruptedException(e);
                            log.error("execute error: {}", e.getMessage(), e);
                            throw new RoRoException(e.getMessage());
                        }
                    }
                }

                if (version != null && version.toLowerCase().startsWith("webtob")) {
                    version = StringUtils.strip(version, strategy.getCarriageReturn());
                } else {
                    version = null;
                }
            }
        }

        log.debug(":+:+:+:+:+:+:+: engine home : [{}]", engineHome);
        log.debug(":+:+:+:+:+:+:+: engine version : [{}]", version);

        String contents = readWebToBConfigFile(engineHome, targetHost, middleware, strategy);

        if (StringUtils.isEmpty(contents)) {
            throw new RoRoException("WebToB config file read failed. Please check http.m or ws_engine.m file is exist in \"" +
                    targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":" + engineHome + "\"");
        }
        analyzeWebToB(contents, version, targetHost, middleware, assessmentResult, engineHome, strategy);

        // engine 설치 경로 이외에 각 노드의 디렉토리(WEBTOBDIR, DOCUMENT_ROOT)를 탐색 대상으로 추가한다.
        List<String> paths = new ArrayList<>();
        paths.add(engineHome);

        if (assessmentResult.getInstance() != null) {
            WebToBAssessmentResult.Instance instance = (WebToBAssessmentResult.Instance) assessmentResult.getInstance();

            if (instance.getNodes() != null) {
                for (WebToBAssessmentResult.Node node : instance.getNodes()) {
                    paths.add(node.getWebTobDir());
                    paths.add(node.getDocRoot());
                }
            }
        }

        // assessmentResult.setThirdPartySolutions(ThirdPartySolutionUtil.detectThirdPartySolutionsFromMiddleware(targetHost, strategy.isWindows(), paths.toArray(new String[0])));

        return assessmentResult;
    }

    private void analyzeWebToB(String contents, String version, TargetHost targetHost, MiddlewareInventory middleware, WebToBAssessmentResult assessmentResult, String engineHome, GetInfoStrategy strategy) throws InterruptedException {
        contents = contents.replaceAll("\t", " ");

        log.debug(":+:+:+:+:+:+:+: Starting reformat contents : [{}]", contents);
        contents = reformattingContents(contents, strategy);
        log.debug(":+:+:+:+:+:+:+: Reformat contents : [{}]", contents);

        log.debug(":+:+:+:+:+:+:+: Start parsing");
        Map<String, Map<String, Object>> sbjMap = parseContents(contents);
        log.debug(":+:+:+:+:+:+:+: Finish parsing");

        log.debug(":+:+:+:+:+:+:+: Start analyze Engine....");
        analyzeEngine(version, middleware, assessmentResult, targetHost, strategy);
        log.debug(":+:+:+:+:+:+:+: Finish analyze Engine....");

        log.debug(":+:+:+:+:+:+:+: Start analyze Instance....");
        analyzeInstance(sbjMap, targetHost, assessmentResult, engineHome, strategy);
        log.debug(":+:+:+:+:+:+:+: Finish analyze Instance....");
    }

    private void analyzeInstance(Map<String, Map<String, Object>> sbjMap, TargetHost targetHost, WebToBAssessmentResult assessmentResult, String engineHome, GetInfoStrategy strategy) throws InterruptedException {
        WebToBAssessmentResult.Instance instance = new WebToBAssessmentResult.Instance();

        if (sbjMap.containsKey("DOMAIN")) {
            WebToBAssessmentResult.Domain domain = getDomain(sbjMap.get("DOMAIN"));
            instance.setDomain(domain);
        }
        if (sbjMap.containsKey("NODE")) {
            List<WebToBAssessmentResult.Node> nodes = getNodes(sbjMap.get("NODE"));
            instance.setNodes(nodes);
        }
        if (sbjMap.containsKey("SVRGROUP")) {
            List<WebToBAssessmentResult.SvrGroup> svrGroups = getSvrGroups(sbjMap.get("SVRGROUP"));
            instance.setSvrGroups(svrGroups);
        }
        if (sbjMap.containsKey("SERVER")) {
            List<WebToBAssessmentResult.Server> servers = getServers(sbjMap.get("SERVER"));
            instance.setServers(servers);
        }
        if (sbjMap.containsKey("HTH_THREAD")) {
            List<WebToBAssessmentResult.HthThread> hthThreads = getHthThread(sbjMap.get("HTH_THREAD"));
            instance.setHthThread(hthThreads);
        }
        if (sbjMap.containsKey("URI")) {
            List<WebToBAssessmentResult.Uri> uris = getUri(sbjMap.get("URI"));
            instance.setUris(uris);
        }
        if (sbjMap.containsKey("LOGGING")) {
            List<WebToBAssessmentResult.Logging> loggings = getLogging(sbjMap.get("LOGGING"));
            instance.setLoggings(loggings);
        }
        if (sbjMap.containsKey("ALIAS")) {
            List<WebToBAssessmentResult.Alias> alias = getAlias(sbjMap.get("ALIAS"));
            instance.setAliases(alias);
        }
        if (sbjMap.containsKey("VHOST")) {
            List<WebToBAssessmentResult.Vhost> vhosts = getVhost(sbjMap.get("VHOST"));
            instance.setVhosts(vhosts);
        }
        if (sbjMap.containsKey("SSL")) {
            List<WebToBAssessmentResult.SSL> ssls = getSsl(sbjMap.get("SSL"));
            instance.setSsls(ssls);
        }

        if (sbjMap.containsKey("ACCESS")) {
            List<WebToBAssessmentResult.Access> accesses = getAccess(sbjMap.get("ACCESS"));
            instance.setAccesses(accesses);
        }
        if (sbjMap.containsKey("REVERSE_PROXY")) {
            List<WebToBAssessmentResult.ReverseProxy> reverseProxies = getReverseProxy(sbjMap.get("REVERSE_PROXY"));
            instance.setReverseProxies(reverseProxies);
        }
        if (sbjMap.containsKey("ERRORDOCUMENT")) {
            List<WebToBAssessmentResult.ErrorDocument> errorDocuments = getErrorDocument(sbjMap.get("ERRORDOCUMENT"));
            instance.setErrorDocuments(errorDocuments);
        }

        if (sbjMap.containsKey("EXT")) {
            List<WebToBAssessmentResult.Ext> exts = getExt(sbjMap.get("EXT"));
            instance.setExts(exts);
        }

        /**
         *  Configure files
         * */
        List<WebToBAssessmentResult.ConfigFile> configFileResult = getConfigFiles(targetHost, engineHome, strategy);
        instance.setConfigFiles(configFileResult);

        // config 파일 save
        String ipAddress = targetHost.getIpAddress();
        if (configFileResult.size() > 0) {
            for (WebToBAssessmentResult.ConfigFile configFile : configFileResult) {
                WasAnalyzerUtil.saveAssessmentFile(ipAddress, configFile.getPath(), configFile.getContents(), CommonProperties.getWorkDir(), strategy);
            }
        }

        assessmentResult.setInstance(instance);
    }

    private List<WebToBAssessmentResult.ConfigFile> getConfigFiles(TargetHost targetHost, String engineHome, GetInfoStrategy strategy) throws InterruptedException {
        List<WebToBAssessmentResult.ConfigFile> targetList = new ArrayList<>();

        // http.m https.m  ws_engine
//        String command = "sudo find " + engineHome + "/config -type f | xargs file | grep text";
//        String result = SSHUtil.executeCommand(targetHost, command);
        String separator = strategy.getSeparator();
        String result = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_FIND_CONFIG_FILE1, commandConfig, strategy, engineHome, "config" + separator + "*.m");
        if (strategy.isWindows() && StringUtils.isEmpty(result)) {
            result = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_FIND_CONFIG_FILE2, commandConfig, strategy, engineHome, "config" + separator + "*.m");
        }

        if (StringUtils.isNotEmpty(result)) {
            for (String path : result.split(strategy.getCarriageReturn())) {
                if (!StringUtils.isEmpty(path)) {
                    WebToBAssessmentResult.ConfigFile target = new WebToBAssessmentResult.ConfigFile();
//                    path = path.split(":")[0].replace("\r", StringUtils.EMPTY);
                    target.setName(path.substring(path.lastIndexOf(strategy.getSeparator()) + 1));
                    target.setPath(path);
                    target.setContents(AbstractMiddlewareAssessment.getFileContents(targetHost, path, commandConfig, strategy));
                    targetList.add(target);

                    log.debug("Discovered configuration files : {}", target.getPath());
                }
            }
        }

        return targetList;
    }

    private List<WebToBAssessmentResult.Ext> getExt(Map<String, Object> source) {
        List<WebToBAssessmentResult.Ext> targetList = new ArrayList<>();

        for (String item : source.keySet()) {
            WebToBAssessmentResult.Ext ext = new WebToBAssessmentResult.Ext();
            ext.setName(item);
            ext.setSvrType(getValue("SvrType", (Map) source.get(item)));
            ext.setMimeType(getValue("Mimetype", (Map) source.get(item)));
            ext.setAccessName(getValue("AccessName", (Map) source.get(item)));

            targetList.add(ext);
        }

        return targetList;
    }

    private List<WebToBAssessmentResult.ErrorDocument> getErrorDocument(Map<String, Object> source) {
        List<WebToBAssessmentResult.ErrorDocument> targetList = new ArrayList<>();

        for (String item : source.keySet()) {
            WebToBAssessmentResult.ErrorDocument errorDocument = new WebToBAssessmentResult.ErrorDocument();
            errorDocument.setName(item);
            errorDocument.setStatus(getValue("status", (Map) source.get(item)));
            errorDocument.setUrl(getValue("url", (Map) source.get(item)));

            targetList.add(errorDocument);
        }

        return targetList;
    }

    private List<WebToBAssessmentResult.ReverseProxy> getReverseProxy(Map<String, Object> source) {
        List<WebToBAssessmentResult.ReverseProxy> targetList = new ArrayList<>();

        for (String item : source.keySet()) {
            WebToBAssessmentResult.ReverseProxy reverseProxy = new WebToBAssessmentResult.ReverseProxy();
            reverseProxy.setName(item);
            reverseProxy.setPathPrefix(getValue("status", (Map) source.get(item)));
            reverseProxy.setServerPathPrefix(getValue("ServerPathPrefix", (Map) source.get(item)));
            reverseProxy.setServerAddress(getValue("ServerAddress", (Map) source.get(item)));
            reverseProxy.setSetHostHeader(getValue("SetHostHeader", (Map) source.get(item)));
            reverseProxy.setSetVhostName(getValue("VHostName", (Map) source.get(item)));

            targetList.add(reverseProxy);
        }

        return targetList;
    }

    private List<WebToBAssessmentResult.Access> getAccess(Map<String, Object> source) {
        List<WebToBAssessmentResult.Access> targetList = new ArrayList<>();

        for (String item : source.keySet()) {
            WebToBAssessmentResult.Access access = new WebToBAssessmentResult.Access();
            access.setName(item);
            access.setOrder(getValue("Order", (Map) source.get(item)));
            access.setDeny(getValue("Allow", (Map) source.get(item)));

            targetList.add(access);
        }

        return targetList;
    }

    private List<WebToBAssessmentResult.SSL> getSsl(Map<String, Object> source) {
        List<WebToBAssessmentResult.SSL> targetList = new ArrayList<>();

        for (String item : source.keySet()) {
            WebToBAssessmentResult.SSL ssl = new WebToBAssessmentResult.SSL();
            ssl.setName(item);
            ssl.setCaCertificateFile(getValue("CACertificateFile", (Map) source.get(item)));
            ssl.setCaCertificatePath(getValue("CACertificatePath", (Map) source.get(item)));
            ssl.setCertificateChainFile(getValue("CertificateChainFile", (Map) source.get(item)));
            ssl.setCertificateFile(getValue("CertificateFile", (Map) source.get(item)));
            ssl.setCertificateKeyFile(getValue("CertificateKeyFile", (Map) source.get(item)));
            ssl.setProtocols(getValue("Protocols", (Map) source.get(item)));
            ssl.setPassPhraseDialog(getValue("PassPhraseDialog", (Map) source.get(item)));
            ssl.setRequiredCiphers(getValue("RequiredCiphers", (Map) source.get(item)));

            targetList.add(ssl);
        }
        return targetList;
    }

    private List<WebToBAssessmentResult.Vhost> getVhost(Map<String, Object> source) {
        List<WebToBAssessmentResult.Vhost> targetList = new ArrayList<>();

        for (String item : source.keySet()) {
            WebToBAssessmentResult.Vhost vhost = new WebToBAssessmentResult.Vhost();
            vhost.setVhostName(item);
            vhost.setDocRoot(getValue("DOCROOT", (Map) source.get(item)));
            vhost.setEnvFile(getValue("EnvFile", (Map) source.get(item)));
            vhost.setErrorLog(getValue("ERRORLOG", (Map) source.get(item)));
            vhost.setIconDir(getValue("IconDir", (Map) source.get(item)));
            vhost.setIndexName(getValue("IndexName", (Map) source.get(item)));
            vhost.setLogging(getValue("LOGGING", (Map) source.get(item)));
            vhost.setNodeName(getValue("NODENAME", (Map) source.get(item)));
            vhost.setUserDir(getValue("UserDir", (Map) source.get(item)));
            vhost.setUsrLogDir(getValue("UsrLogDir", (Map) source.get(item)));
            vhost.setHostName(getValue("Hostname", (Map) source.get(item)));
            vhost.setHostAlias(getValue("HostAlias", (Map) source.get(item)));
            vhost.setErrorDocument(getValue("ERRORDOCUMENT", (Map) source.get(item)));
            vhost.setServiceOrder(getValue("SERVICEORDER", (Map) source.get(item)));
            vhost.setUrlRewrite(getValue("URLREWRITE", (Map) source.get(item)));
            vhost.setUrlRewriteConfig(getValue("URLREWRITECONFIG", (Map) source.get(item)));
            vhost.setKeepAliveTimeout(getValue("KeepAliveTimeout", (Map) source.get(item)));
            vhost.setSslFlag(getValue("SSLFLAG", (Map) source.get(item)));
            vhost.setSslName(getValue("SSLNAME", (Map) source.get(item)));

            targetList.add(vhost);
        }
        return targetList;
    }

    private List<WebToBAssessmentResult.Alias> getAlias(Map<String, Object> source) {
        List<WebToBAssessmentResult.Alias> targetList = new ArrayList<>();

        for (String item : source.keySet()) {
            WebToBAssessmentResult.Alias alias = new WebToBAssessmentResult.Alias();
            alias.setName(item);
            alias.setUri(getValue("URI", (Map) source.get(item)));
            alias.setRealPath(getValue("RealPath", (Map) source.get(item)));

            targetList.add(alias);
        }
        return targetList;
    }

    private List<WebToBAssessmentResult.Logging> getLogging(Map<String, Object> source) {
        List<WebToBAssessmentResult.Logging> targetList = new ArrayList<>();

        for (String item : source.keySet()) {
            WebToBAssessmentResult.Logging logging = new WebToBAssessmentResult.Logging();
            logging.setName(item);
            logging.setFormat(getValue("Format", (Map) source.get(item)));
            logging.setFileName(getValue("Filename", (Map) source.get(item)));
            logging.setOption(getValue("Option", (Map) source.get(item)));

            targetList.add(logging);
        }
        return targetList;
    }

    private List<WebToBAssessmentResult.Uri> getUri(Map<String, Object> source) {
        List<WebToBAssessmentResult.Uri> targetList = new ArrayList<>();

        for (String item : source.keySet()) {
            WebToBAssessmentResult.Uri uri = new WebToBAssessmentResult.Uri();
            uri.setName(item);
            uri.setUri(getValue("Uri", (Map) source.get(item)));
            uri.setSvrType(getValue("SvrType", (Map) source.get(item)));
            uri.setSvrName(getValue("SvrName", (Map) source.get(item)));
            uri.setMatch(getValue("Match", (Map) source.get(item)));
            uri.setAccessName(getValue("AccessName", (Map) source.get(item)));
            uri.setVhostName(getValue("VhostName", (Map) source.get(item)));
            uri.setGotoExt(getValue("GotoExt", (Map) source.get(item)));

            targetList.add(uri);
        }
        return targetList;
    }

    private List<WebToBAssessmentResult.HthThread> getHthThread(Map<String, Object> source) {
        List<WebToBAssessmentResult.HthThread> targetList = new ArrayList<>();

        for (String item : source.keySet()) {
            WebToBAssessmentResult.HthThread hthThread = new WebToBAssessmentResult.HthThread();
            hthThread.setName(item);
            hthThread.setWorkerThreads(getValue("WorkerThreads", (Map) source.get(item)));
            targetList.add(hthThread);
        }
        return targetList;
    }

    private List<WebToBAssessmentResult.Server> getServers(Map<String, Object> source) {
        List<WebToBAssessmentResult.Server> targetList = new ArrayList<>();

        for (String item : source.keySet()) {
            WebToBAssessmentResult.Server server = new WebToBAssessmentResult.Server();
            server.setName(item);
            server.setSvgName(getValue("SvgName", (Map) source.get(item)));
            server.setMinProc(getValue("Minproc", (Map) source.get(item)));
            server.setMaxProc(getValue("MaxProc", (Map) source.get(item)));
            server.setAsqCount(getValue("ASQCount", (Map) source.get(item)));
            server.setHttpOutBufSize(getValue("HttpOutbufSize", (Map) source.get(item)));
            server.setHttpInBufSize(getValue("HttpInbufSize", (Map) source.get(item)));
            server.setFlowControl(getValue("FlowControl", (Map) source.get(item)));
            server.setSchedule(getValue("Schedule", (Map) source.get(item)));
            server.setOptions(getValue("Options", (Map) source.get(item)));


            targetList.add(server);
        }
        return targetList;
    }

    private List<WebToBAssessmentResult.SvrGroup> getSvrGroups(Map<String, Object> source) {
        List<WebToBAssessmentResult.SvrGroup> targetList = new ArrayList<>();

        for (String item : source.keySet()) {
            WebToBAssessmentResult.SvrGroup svrGroup = new WebToBAssessmentResult.SvrGroup();
            svrGroup.setName(item);
            svrGroup.setSvrType(getValue("SvrType", (Map) source.get(item)));
            svrGroup.setNodeName(getValue("NODENAME", (Map) source.get(item)));
            svrGroup.setNodeName(getValue("VHostName", (Map) source.get(item)));

            targetList.add(svrGroup);
        }
        return targetList;
    }

    private List<WebToBAssessmentResult.Node> getNodes(Map<String, Object> source) {
        List<WebToBAssessmentResult.Node> targetList = new ArrayList<>();
        for (String item : source.keySet()) {
            WebToBAssessmentResult.Node node = new WebToBAssessmentResult.Node();
            node.setName(item);
            node.setPort(getValue("Port", (Map) source.get(item)));
            node.setShmKey(getValue("SHMKEY", (Map) source.get(item)));
            node.setWebTobDir(getValue("WebtoBDir", (Map) source.get(item)));
            node.setHth(getValue("HTH", (Map) source.get(item)));
            node.setErrorLog(getValue("ErrorLog", (Map) source.get(item)));
            node.setLogging(getValue("Logging", (Map) source.get(item)));
            node.setSysLog(getValue("SysLog", (Map) source.get(item)));
            node.setDocRoot(getValue("Docroot", (Map) source.get(item)));
            node.setJsvPort(getValue("JsvPort", (Map) source.get(item)));
            node.setUser(getValue("User", (Map) source.get(item)));
            node.setGroup(getValue("Group", (Map) source.get(item)));

            node.setNodeName(getValue("NODENAME", (Map) source.get(item)));
            node.setErrorDocument(getValue("ERRORDOCUMENT", (Map) source.get(item)));
            node.setOptions(getValue("Options", (Map) source.get(item)));
            node.setServiceOrder(getValue("ServiceOrder", (Map) source.get(item)));
            node.setJsvPort(getValue("JSVPORT", (Map) source.get(item)));

            node.setIpcPerm(getValue("IPCPERM", (Map) source.get(item)));
            node.setUrlRewrite(getValue("URLREWRITE", (Map) source.get(item)));
            node.setUrlRewriteConfig(getValue("URLREWRITECONFIG", (Map) source.get(item)));
            node.setRpafHeader(getValue("RPAFHeader", (Map) source.get(item)));
            node.setKeepAliveTimeout(getValue("KeepAliveTimeout", (Map) source.get(item)));
            node.setCacheEntry(getValue("CacheEntry", (Map) source.get(item)));
            node.setMaxCacheMemorySize(getValue("MaxCacheMemorySize", (Map) source.get(item)));

            node.setCacheMaxFileSize(getValue("CacheMaxFileSize", (Map) source.get(item)));
            node.setCacheRefreshImage(getValue("CacheRefreshImage", (Map) source.get(item)));
            node.setCacheRefreshHtml(getValue("CacheRefreshHtml", (Map) source.get(item)));
            node.setCacheRefreshDir(getValue("CacheRefreshDir", (Map) source.get(item)));
            node.setCacheRefreshJsv(getValue("CacheRefreshJsv", (Map) source.get(item)));
            node.setForceCacheModificationCheck(getValue("ForceCacheModificationCheck", (Map) source.get(item)));
            node.setDosBlock(getValue("DOSBlock", (Map) source.get(item)));
            node.setDosBlockTableSize(getValue("DOSBlockTableSize", (Map) source.get(item)));
            node.setDosBlockPageCount(getValue("DOSBlockPageCount", (Map) source.get(item)));
            node.setDosBlockPageInterval(getValue("DOSBlockPageInterval", (Map) source.get(item)));
            node.setDosBlockSiteCount(getValue("DOSBlockSiteCount", (Map) source.get(item)));
            node.setDosBlockSiteInterval(getValue("DOSBlockSiteInterval", (Map) source.get(item)));
            node.setDosBlockPeriod(getValue("DOSBlockPeriod", (Map) source.get(item)));
            node.setDosBlockWhiteList(getValue("DOSBlockWhiteList", (Map) source.get(item)));

            targetList.add(node);
        }
        return targetList;
    }

    private String getValue(String key, Map<String, Object> map) {
        if (map != null) {
            if (map.containsKey(key.toUpperCase())) {
                if (map.get(key.toUpperCase()) instanceof String) {
                    return map.get(key.toUpperCase()) + StringUtils.EMPTY;
                }
            }
        }
        return StringUtils.EMPTY;
    }

    private WebToBAssessmentResult.Domain getDomain(Map<String, Object> source) {
        WebToBAssessmentResult.Domain domain = new WebToBAssessmentResult.Domain();
        domain.setName(source.keySet().toArray()[0] != null ? source.keySet().toArray()[0] + StringUtils.EMPTY : StringUtils.EMPTY);
        return domain;
    }

    private void analyzeEngine(String version, MiddlewareInventory middleware, WebToBAssessmentResult assessmentResult, TargetHost targetHost, GetInfoStrategy strategy) throws InterruptedException {
        WebToBAssessmentResult.Engine engine = new WebToBAssessmentResult.Engine();
        engine.setVersion(version);
        engine.setPath(middleware.getEngineInstallationPath());
        engine.setVendor("Tmax");
        engine.setName("WebToB");

//        String runUser = SSHUtil.executeCommand(targetHost, "ps -ef | grep -v grep | grep -e wsm -e htl -e hth | awk '{print $1}' | uniq | tr -d '\\n'");
        String runUser = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_RUN_USER, commandConfig, strategy);
        engine.setRunUser(runUser);
        engine.setScannedDate(new Date());
        assessmentResult.setEngine(engine);
    }

    private Map<String, Map<String, Object>> parseContents(String contents) {
        Map<String, Map<String, Object>> sbjMap = new HashMap<>();
        Map<String, Object> attrMap = null;
        Map<String, String> curItmMap = null;
        String pre_subject = null;

        for (String line : contents.split(StringUtils.SPACE)) {
            if (StringUtils.isEmpty(line))
                continue;

            if (line.startsWith("*")) {
                String cur_subject = line.replace("*", StringUtils.EMPTY).toUpperCase();
                pre_subject = cur_subject;
                attrMap = new HashMap<>();
                sbjMap.put(pre_subject, attrMap);
            } else {
                if (line.contains("=")) {
                    String key = line.split("=")[0].toUpperCase();
                    if (line.split("=").length > 1) {
                        if (curItmMap.containsKey(key)) {
                            String value = curItmMap.get(key);
                            curItmMap.put(key, line.split("=")[1] + "," + value);
                        } else {
                            curItmMap.put(key, line.split("=")[1]);
                        }
                    } else {
                        curItmMap.put(key, StringUtils.EMPTY);
                    }
                } else if (!line.equals(StringUtils.EMPTY)) {
                    curItmMap = new HashMap<>();
                    attrMap.put(line, curItmMap);
                }
            }
        }

        return sbjMap;
    }

    private String reformattingContents(String contents, GetInfoStrategy strategy) {

        String[] attLines = contents.split(strategy.getCarriageReturn());
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < attLines.length; idx++) {
            Pattern ignore = Pattern.compile("^#+\\S.+");
            Matcher ignoreMatcher = ignore.matcher(attLines[idx].trim());
            if (ignoreMatcher.find())
                continue;

            Pattern p = Pattern.compile("\"(.+?)\"");
            Matcher m = p.matcher(attLines[idx]);

            String temp = null;
            int i = 0;
            HashMap<String, String> tempMap = new HashMap<>();
            while (m.find()) {
                i += 1;
                attLines[idx] = attLines[idx].replace(m.group(), String.format("temp%s", i));
                temp = m.group().replaceAll("\\s+,+\\s|\\s,|,\\s|,", ",");
                tempMap.put(String.format("temp%s", i), temp);
            }

            attLines[idx] = attLines[idx].replace(",", StringUtils.EMPTY);

            for (String key : tempMap.keySet()) {
                attLines[idx] = attLines[idx].replace(key, tempMap.get(key));
            }

            if (attLines[idx].contains("#")) {
                attLines[idx] = attLines[idx].substring(0, attLines[idx].indexOf("#"));
            }

            if (!"".equals(attLines[idx])) {
                if (attLines[idx].charAt(attLines[idx].length() - 1) == ',') {
                    attLines[idx] = attLines[idx].substring(0, attLines[idx].lastIndexOf(","));
                }
                sb.append(attLines[idx]).append(" ");
            }
        }

        contents = sb.toString();

        contents = contents.replaceAll("\\t", StringUtils.EMPTY);
        contents = contents.replaceAll("=\\s+|\\s+=\\s+|\\s+=", "=");
        contents = contents.replaceAll("\\r", StringUtils.EMPTY);
        contents = contents.replaceAll("\"", StringUtils.EMPTY);

        return contents;
    }

    private String readWebToBConfigFile(String engineHome, TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {
        String separator = strategy.getSeparator();
        String http_m = engineHome + separator + "config" + separator + "http.m";
        String https_m = engineHome + separator + "config" + separator + "https.m";
        String wsEngine_m = engineHome + separator + "config" + separator + "ws_engine.m";
        String contents = null;
        if (StringUtils.isNotEmpty(middleware.getConfigFilePath())) {
            File f = new File(middleware.getConfigFilePath());
            if (f.isDirectory()) {
                List<String> configs = List.of("http.m");
                for (String config : configs) {
                    log.debug("Find {} file in [{}]", config, middleware.getConfigFilePath());
                    String targetFilePath = AbstractMiddlewareAssessment.findFileAbsolutePath(f.getAbsolutePath(), config);
                    if (StringUtils.isNotEmpty(targetFilePath)) {
                        File confFile = new File(targetFilePath);
                        if (confFile.exists()) {
                            contents = readUploadedFile(confFile.getAbsolutePath());
                        }
                    }
                }
            } else {
                if (middleware.getConfigFilePath().contains("http.m")) {
                    contents = readUploadedFile(middleware.getConfigFilePath());
                }
            }
        } else if (fileExists(targetHost, http_m, commandConfig, strategy)) {
            contents = AbstractMiddlewareAssessment.getFileContents(targetHost, http_m, commandConfig, strategy);
        } else if (fileExists(targetHost, wsEngine_m, commandConfig, strategy)) {
            contents = AbstractMiddlewareAssessment.getFileContents(targetHost, wsEngine_m, commandConfig, strategy);
        } else if (fileExists(targetHost, https_m, commandConfig, strategy)) {
            contents = AbstractMiddlewareAssessment.getFileContents(targetHost, https_m, commandConfig, strategy);
        }

        return contents;
    }
}