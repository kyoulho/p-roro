package io.playce.roro.mw.asmt.nginx.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.odiszapc.nginxparser.*;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.AbstractMiddlewareAssessment;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.nginx.dto.NginxAssessmentResult.*;
import io.playce.roro.mw.asmt.nginx.dto.NginxAssessmentResult.Server.Location;
import io.playce.roro.mw.asmt.nginx.dto.NginxAssessmentResult.Server.Proxy;
import io.playce.roro.mw.asmt.nginx.dto.NginxAssessmentResult.Server.Ssl;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.mw.asmt.util.WasAnalyzerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.playce.roro.common.util.StringUtil.splitToArrayByCrlf;

@Slf4j
@Component
@RequiredArgsConstructor
public class NginxParseHelper {

    private final CommandConfig commandConfig;

    private static final String EVENTS_BLOCK = "events";
    private static final String HTTP_BLOCK = "http";
    private static final String STREAM_BLOCK = "stream";
    private static final String HTTP_SERVER_BLOCK = "server";
    private static final String HTTP_SERVER_LOCATION_BLOCK = "location";
    private static final String HTTP_UPSTREAM_BLOCK = "upstream";

    private static final List<String> MULTIPLE_PROPERTIES = List.of("listen", "log_format", "include",
            "server", "proxy_set_header");

    public General getGeneral(ObjectMapper objectMapper, NgxConfig conf) {
        MultiValueMap<String, Object> generalMap = new LinkedMultiValueMap<>();

        for (NgxEntry ngxEntry : conf.getEntries()) {
            if (NgxEntryType.fromClass(ngxEntry.getClass()) == NgxEntryType.PARAM) {
                addParamValue(generalMap, ngxEntry);
            }
        }

        Map<String, Object> tempMap = convertCollectionToSingleElement(generalMap);

        return objectMapper.convertValue(tempMap, General.class);
    }

    public Events getEvents(ObjectMapper objectMapper, NgxConfig conf) {
        MultiValueMap<String, Object> eventsMap = new LinkedMultiValueMap<>();
        Events events = new Events();

        NgxBlock ngxBlock = conf.findBlock(EVENTS_BLOCK);

        if (ngxBlock != null) {
            for (NgxEntry ngxEntry : ngxBlock.getEntries()) {
                if (NgxEntryType.fromClass(ngxEntry.getClass()) == NgxEntryType.PARAM) {
                    addParamValue(eventsMap, ngxEntry);
                }
            }

            Map<String, Object> tempMap = convertCollectionToSingleElement(eventsMap);
            events = objectMapper.convertValue(tempMap, Events.class);
        }

        return events;
    }

    public Http getHttp(ObjectMapper objectMapper, NgxConfig conf) {
        MultiValueMap<String, Object> httpMap = new LinkedMultiValueMap<>();
        Http http = new Http();

        NgxBlock ngxBlock = conf.findBlock(HTTP_BLOCK);

        if (ngxBlock != null) {
            for (NgxEntry ngxEntry : ngxBlock.getEntries()) {
                if (NgxEntryType.fromClass(ngxEntry.getClass()) == NgxEntryType.PARAM) {
                    addParamValue(httpMap, ngxEntry);
                }
            }

            http = objectMapper.convertValue(convertCollectionToSingleElement(httpMap), Http.class);

            // HTTP 안에 SERVER, UPSTREAM Block 을 구한다.
            List<Server> servers = new ArrayList<>();
            List<Upstream> upstreams = new ArrayList<>();
            // Block은 따로 구한다.
            for (NgxEntry ngxEntry : ngxBlock.getEntries()) {
                if (NgxEntryType.fromClass(ngxEntry.getClass()) == NgxEntryType.BLOCK) {
                    if (((NgxBlock) ngxEntry).getName().startsWith(HTTP_SERVER_BLOCK)) {
                        servers.add(getServer(objectMapper, (NgxBlock) ngxEntry));
                    } else if (((NgxBlock) ngxEntry).getName().startsWith(HTTP_UPSTREAM_BLOCK)) {
                        upstreams.add(getUpstream((NgxBlock) ngxEntry));
                    }
                }
            }

            http.setServers(servers);
            http.setUpstreams(upstreams);
        }

        return http;
    }

    public Stream getStream(ObjectMapper objectMapper, NgxConfig conf) {
        Stream stream = new Stream();

        NgxBlock ngxBlock = conf.findBlock(STREAM_BLOCK);

        if (ngxBlock != null) {
            // HTTP 안에 SERVER, UPSTREAM Block 을 구한다.
            List<Server> servers = new ArrayList<>();
            List<Upstream> upstreams = new ArrayList<>();
            // Block은 따로 구한다.
            for (NgxEntry ngxEntry : ngxBlock.getEntries()) {
                if (NgxEntryType.fromClass(ngxEntry.getClass()) == NgxEntryType.BLOCK) {
                    if (((NgxBlock) ngxEntry).getName().startsWith(HTTP_SERVER_BLOCK)) {
                        servers.add(getServer(objectMapper, (NgxBlock) ngxEntry));
                    } else if (((NgxBlock) ngxEntry).getName().startsWith(HTTP_UPSTREAM_BLOCK)) {
                        upstreams.add(getUpstream((NgxBlock) ngxEntry));
                    }
                }
            }

            stream.setServers(servers);
            stream.setUpstreams(upstreams);
        }

        return stream;
    }

    /**
     * Http or Stream-> Server block parse.
     *
     * @param objectMapper
     * @param ngxBlock
     * @return
     */
    public Server getServer(ObjectMapper objectMapper, NgxBlock ngxBlock) {
        Server server;
        MultiValueMap<String, Object> serverMap = new LinkedMultiValueMap<>();

        for (NgxEntry ngxEntry : ngxBlock.getEntries()) {
            if (NgxEntryType.fromClass(ngxEntry.getClass()) == NgxEntryType.PARAM) {
                addParamValue(serverMap, ngxEntry);
            }
        }

        Map<String, Object> convertMap = convertCollectionToSingleElement(serverMap);

        for (String key : convertMap.keySet()) {
            if (key.equals("server_name") && convertMap.get(key) instanceof List) {
                convertMap.put("server_name", StringUtils.join(convertMap.get(key)));
            }
        }

        server = objectMapper.convertValue(convertMap, Server.class);

        List<Location> locations = new ArrayList<>();
        for (NgxEntry ngxEntry : ngxBlock.getEntries()) {
            if (NgxEntryType.fromClass(ngxEntry.getClass()) == NgxEntryType.BLOCK) {
                if (((NgxBlock) ngxEntry).getName().startsWith(HTTP_SERVER_LOCATION_BLOCK)) {
                    locations.add(getLocation(objectMapper, (NgxBlock) ngxEntry));
                }
            }
        }

        server.setLocations(locations);
        server.setSsl(getSsl(objectMapper, convertCollectionToSingleElement(serverMap)));
        server.setProxy(getProxy(objectMapper, convertCollectionToSingleElement(serverMap)));

        return server;
    }

    public Ssl getSsl(ObjectMapper objectMapper, Map<String, Object> serverMap) {
        Map<String, Object> sslMap = new HashMap<>();

        for (String key : serverMap.keySet()) {
            if (key.toLowerCase().startsWith("ssl")) {
                sslMap.put(key, serverMap.get(key));
            }
        }

        return MapUtils.isEmpty(sslMap) ? null : objectMapper.convertValue(sslMap, Ssl.class);
    }

    public Proxy getProxy(ObjectMapper objectMapper, Map<String, Object> serverMap) {
        Map<String, Object> proxyMap = new HashMap<>();

        for (String key : serverMap.keySet()) {
            if (key.toLowerCase().startsWith("proxy")) {
                proxyMap.put(key, serverMap.get(key));
            }
        }

        return MapUtils.isEmpty(proxyMap) ? null : objectMapper.convertValue(proxyMap, Proxy.class);
    }

    public Location getLocation(ObjectMapper objectMapper, NgxBlock ngxBlock) {
        MultiValueMap<String, Object> locationMap = new LinkedMultiValueMap<>();
        Location location;

        for (NgxEntry ngxEntry : ngxBlock.getEntries()) {
            if (NgxEntryType.fromClass(ngxEntry.getClass()) == NgxEntryType.PARAM) {
                addParamValue(locationMap, ngxEntry);
            }
        }

        location = objectMapper.convertValue(convertCollectionToSingleElement(locationMap), Location.class);
        location.setUri(ngxBlock.getValue());
        location.setProxy(getProxy(objectMapper, convertCollectionToSingleElement(locationMap)));

        return location;
    }

    /**
     * Http or Stream -> upstream block parse.
     *
     * @param ngxBlock
     * @return
     */
    public Upstream getUpstream(NgxBlock ngxBlock) {
        MultiValueMap<String, Object> upstreamMap = new LinkedMultiValueMap<>();

        for (NgxEntry ngxEntry : ngxBlock.getEntries()) {
            if (NgxEntryType.fromClass(ngxEntry.getClass()) == NgxEntryType.PARAM) {
                addParamValue(upstreamMap, ngxEntry);
            }
        }

        List<Object> servers = upstreamMap.get("server");
        List<Upstream.Server> upstreamServers = new ArrayList<>();

        for (Object server : servers) {
            Upstream.Server upstreamServer = new Upstream.Server();
            if (String.valueOf(server).contains(StringUtils.SPACE)) {
                String[] tempServer = String.valueOf(server).split(" ", 2);
                upstreamServer.setAddress(tempServer[0]);
                upstreamServer.setOption(tempServer[1]);
            } else {
                upstreamServer.setAddress(String.valueOf(server));
            }
            upstreamServers.add(upstreamServer);
        }

        Upstream upstream = new Upstream();
        upstream.setName(ngxBlock.getValue());
        upstream.setServers(upstreamServers);

        return upstream;
    }

    public List<ConfigFile> getConfigFiles(TargetHost targetHost, ObjectMapper objectMapper, GetInfoStrategy strategy, NgxConfig conf,
                                           String instancePath) throws InterruptedException {
        List<ConfigFile> configFiles = new ArrayList<>();
        Instance instance = new Instance();

        instance.setGeneral(getGeneral(objectMapper, conf));
        instance.setHttp(getHttp(objectMapper, conf));

        // nginx.conf
        NgxDumper dumper = new NgxDumper(conf);
        ConfigFile configFile = new ConfigFile();
        configFile.setPath(instancePath);
        configFile.setContent(dumper.dump());

        configFiles.add(configFile);

        // include 한 config file을 읽는다.
        List<String> includeFiles = new ArrayList<>();
        includeFiles.addAll(CollectionUtils.isEmpty(instance.getGeneral().getInclude()) ? new ArrayList<>() : instance.getGeneral().getInclude());
        includeFiles.addAll(CollectionUtils.isEmpty(instance.getHttp().getInclude()) ? new ArrayList<>() : instance.getHttp().getInclude());

        for (String includeFile : includeFiles) {
            // 상대경로일 경우 절대경로로 변경해준다.
            if (!isAbsolutePath(includeFile)) {
                // nginx.conf 파일 path에서 시작.
                String nginxConfParentPath = instancePath.substring(0, instancePath.lastIndexOf(strategy.getSeparator()));
                includeFile = nginxConfParentPath + strategy.getSeparator() + includeFile;
            }

            if (includeFile.contains("*")) {
                String fileList = MWCommonUtil.getExecuteResult(targetHost, COMMAND.FILE_LIST_ONE_LINE, commandConfig, strategy, includeFile);
                String[] fileArrays = splitToArrayByCrlf(fileList);
                for (String filePath : fileArrays) {
                    configFiles.add(getConfigFile(targetHost, strategy, commandConfig, filePath));
                }
            } else {
                if (AbstractMiddlewareAssessment.fileExists(targetHost, includeFile, commandConfig, strategy)) {
                    configFiles.add(getConfigFile(targetHost, strategy, commandConfig, includeFile));
                }
            }
        }

        configFiles.removeIf(x -> StringUtils.isEmpty(x.getPath()));

        return configFiles;
    }

    /**
     * 동일한 키값 중복 처리를 위해 MultiValueMap을 사용한다.
     */
    private void addParamValue(MultiValueMap<String, Object> configMap, NgxEntry ngxEntry) {
        configMap.add(((NgxParam) ngxEntry).getName(), ((NgxParam) ngxEntry).getValue());
    }

    private Map<String, Object> convertCollectionToSingleElement(MultiValueMap<String, Object> configMap) {
        Map<String, Object> convertMap = new HashMap<>();

        // 키 값이 하나여도 여러개가 등록가능한 경우에는 List로 변경.
        for (String key : configMap.keySet()) {
            if (MULTIPLE_PROPERTIES.stream().anyMatch(key.toLowerCase()::equals) && configMap.get(key).size() == 1) {
                convertMap.put(key, List.of(configMap.get(key).get(0)));
            } else if (configMap.get(key).size() == 1) {
                convertMap.put(key, configMap.get(key).get(0));
            } else {
                convertMap.put(key, configMap.get(key));
            }
        }

        return convertMap;
    }

    public String getCatResult(TargetHost targetHost, GetInfoStrategy strategy, String fileName) throws InterruptedException {
        String command = COMMAND.CAT_QUOTATION.command(commandConfig, strategy.isWindows(), fileName);
        String result = strategy.executeCommand(targetHost, command, COMMAND.CAT_QUOTATION);
        WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), fileName, result, CommonProperties.getWorkDir(), strategy);
        return result;
    }

    private boolean isAbsolutePath(String filePath) {
        String windowsRootPathRegx = "^[a-zA-Z]:\\\\(((?![<>:\"/\\\\|?*]).)+((?<![ .])\\\\)?)*$";
        return filePath.startsWith("/") || Pattern.matches(windowsRootPathRegx, filePath);
    }

    private ConfigFile getConfigFile(TargetHost targetHost, GetInfoStrategy strategy, CommandConfig commandConfig, String filePath) throws InterruptedException {
        ConfigFile includeConfigFile = new ConfigFile();

        if (AbstractMiddlewareAssessment.fileExists(targetHost, filePath, commandConfig, strategy)) {
            includeConfigFile.setPath(filePath);
            includeConfigFile.setContent(getCatResult(targetHost, strategy, filePath));
        }

        return includeConfigFile;
    }

}
