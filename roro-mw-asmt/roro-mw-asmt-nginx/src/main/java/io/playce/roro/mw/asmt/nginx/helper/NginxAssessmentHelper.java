package io.playce.roro.mw.asmt.nginx.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.odiszapc.nginxparser.*;
import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.exception.UnauthorizedException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.AbstractMiddlewareAssessment;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.nginx.dto.NginxAssessmentResult;
import io.playce.roro.mw.asmt.nginx.dto.NginxAssessmentResult.*;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.mw.asmt.util.WasAnalyzerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.playce.roro.common.util.StringUtil.splitToArrayByCrlf;

@Slf4j
@Component
@RequiredArgsConstructor
public class NginxAssessmentHelper {

    private final NginxParseHelper nginxParseHelper;

    private final CommandConfig commandConfig;

    public InputStream getConfigFileContentInputStream(TargetHost targetHost, GetInfoStrategy strategy,
                                                       String instancePath) throws InterruptedException {


        String configFileContent = MWCommonUtil.getExecuteResult(targetHost, COMMAND.CAT, commandConfig, strategy, instancePath);

        if (StringUtils.isNotEmpty(configFileContent)) {
            String tempString = removeCommentLine(configFileContent);

            // 원본 파일은 저장하고 리턴값은 주석 제거.
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), instancePath, configFileContent, CommonProperties.getWorkDir(), strategy);

            return new ByteArrayInputStream(convertMapContent(tempString).getBytes());
        } else {
            log.debug("[{}] is Empty. Please check the read config files.", instancePath);
            throw new UnauthorizedException("Nginx config file(" + instancePath + ") read failed. Please check file is exist and has permission to read at \"" +
                    targetHost.getUsername() + "@" + targetHost.getIpAddress() + "\"");
        }

    }

    public InputStream getIncludeFileAppendContent(NgxConfig conf, Map<String, String> includeFileContentMap) {
        List<String> includeFileKey = new ArrayList<>(includeFileContentMap.keySet());
        Map<Integer, String> tempMap = new HashMap<>();

        String configContent = new NgxDumper(conf).dump();

        for (String fileKey : includeFileKey) {
            String[] configArrays = splitToArrayByCrlf(configContent);
            int lineNumber = 1;
            for (String line : configArrays) {
                // include 된 행을 찾으면 해당 Line number를 기록한다.
                if (line.contains("include") && line.contains(fileKey)) {
                    tempMap.put(lineNumber, includeFileContentMap.get(fileKey));
                }
                lineNumber++;
            }
        }

        // nginx.conf include된 해당 라인 번호에 include된 내용을 넣는다.
        List<Integer> includeLineNumber = new ArrayList<>(tempMap.keySet());
        StringBuffer sb = new StringBuffer();
        String[] configArrays = splitToArrayByCrlf(configContent);
        int lineIndex = 1;
        for (String line : configArrays) {
            if (includeLineNumber.contains(lineIndex)) {
                sb.append(tempMap.get(lineIndex)).append("\r\n");
            } else {
                sb.append(line).append("\r\n");
            }
            lineIndex++;
        }

        return new ByteArrayInputStream(convertMapContent(sb.toString()).getBytes());
    }

    public Engine getEngine(TargetHost targetHost,
                            GetInfoStrategy strategy, String enginePath, String instancePath) throws InterruptedException {
        NginxAssessmentResult.Engine engine = new Engine();
        String separator = strategy.getSeparator();

        String nginxExecutePath = enginePath + separator + "nginx";

        engine.setName("nginx");
        engine.setVersion(getVersion(commandConfig, strategy, targetHost, nginxExecutePath));
        engine.setRunUser(getRunUser(commandConfig, strategy, targetHost));
        engine.setPath(enginePath);
        engine.setConfigPath(instancePath);

        return engine;
    }

    public General getAdditionalGeneralInfo(General general, Instance instance, NginxAssessmentResult.Engine engine) {
        general.setVendor(engine.getName());
        general.setSolutionName(Domain1013.NGINX.enname());
        general.setSolutionVersion(engine.getVersion());
        general.setInstallHome(engine.getPath());

        List<String> tempListenServerPorts = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(instance.getHttp().getServers())) {
            for (Server server : instance.getHttp().getServers()) {
                if (CollectionUtils.isNotEmpty(server.getListen())) {
                    tempListenServerPorts.addAll(server.getListen());
                }
            }
        }

        if (instance.getStream() != null && CollectionUtils.isNotEmpty(instance.getStream().getServers())) {
            for (Server server : instance.getStream().getServers()) {
                if (CollectionUtils.isNotEmpty(server.getListen())) {
                    tempListenServerPorts.addAll(server.getListen());
                }
            }
        }

        List<Integer> listenPort = new ArrayList<>();

        // 여러개의 공백을 하나의 공백으로 변환한 후 Split한다.
        String nginxListenPort = String.join(" ", tempListenServerPorts).replaceAll("\\s+", " ");
        String[] nginxListenPortArray = nginxListenPort.split(" ");

        for (String tempListenPort : nginxListenPortArray) {
            if (isPortNumber(tempListenPort)) {
                listenPort.add(Integer.parseInt(tempListenPort));
            } else if (tempListenPort.contains(":")) {
                String tempPortNumber = tempListenPort.substring(tempListenPort.lastIndexOf(":")).replace(":", StringUtils.EMPTY);
                if (isPortNumber(tempPortNumber)) {
                    listenPort.add(Integer.parseInt(tempPortNumber));
                }
            }
        }

        // 중복 Port 제거.
        general.setListenPort(listenPort.stream().distinct().collect(Collectors.toList()));

        boolean isSsl = false;

        // 서버 쪽 SSL Setting 확인.
        for (Server server : instance.getHttp().getServers()) {
            if (server.getSsl() != null) {
                isSsl = true;
                break;
            }
        }

        if (!isSsl) {
            if (nginxListenPort.toLowerCase().contains("ssl") || nginxListenPort.contains("443")) {
                isSsl = true;
            }
        }

        general.setSsl(isSsl);
        general.setScannedDate(new Date());

        return general;
    }

    /**
     * map 속성에 "" 로 묶여 있지 않으면 묶어준다.
     **/
    public static String convertMapContent(String contents) {
        try {
            List<String> lines = contents.lines().collect(Collectors.toList());

            String line;
            boolean isMap = false;
            List<Integer> mapIdx = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                line = lines.get(i);

                if (line.trim().startsWith("map")) {
                    isMap = true;
                    continue;
                }

                if (isMap) {
                    if (line.trim().equals("}")) {
                        isMap = false;
                        continue;
                    }

                    mapIdx.add(i);
                }
            }

            for (int idx : mapIdx) {
                line = lines.get(idx);

                int spaceCount = line.replaceAll("[^ ]", "").length();
                String[] values = line.trim().split(StringUtils.SPACE);

                if (!values[0].equals("default") && !values[0].startsWith("\"") && !values[0].endsWith("\"")) {
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < spaceCount; i++) {
                        sb.append(StringUtils.SPACE);
                    }
                    sb.append("\"").append(values[0]).append("\"").append(StringUtils.SPACE).append(values[1]);

                    lines.set(idx, sb.toString());
                }
            }

            return lines.stream().collect(Collectors.joining("\n"));

        } catch (Exception e) {
            return contents;
        }
    }

    private String removeCommentLine(String content) {
        StringBuffer sb = new StringBuffer();

        if (StringUtils.isNotEmpty(content)) {
            String[] configContentArray = splitToArrayByCrlf(content);
            // 주석 Line 제거.
            for (String configContent : configContentArray) {
                if (!configContent.trim().startsWith("#")) {
                    sb.append(configContent).append("\r\n");
                }
            }
        }

        return sb.toString();
    }

    private String getVersion(CommandConfig commandConfig, GetInfoStrategy strategy, TargetHost targetHost, String nginxExecutePath) throws InterruptedException {
        String version = MWCommonUtil.getExecuteResult(targetHost, COMMAND.NGINX_VERSION, commandConfig, strategy, nginxExecutePath);
        String[] lineArrays = splitToArrayByCrlf(version);

        for (String line : lineArrays) {
            if (line.contains("version")) {
                return line.substring(line.indexOf("/") + 1);
            }
        }

        return StringUtils.EMPTY;
    }

    private String getRunUser(CommandConfig commandConfig, GetInfoStrategy strategy, TargetHost targetHost) throws InterruptedException {
        String runUser;

        if (strategy.isWindows()) {
            runUser = MWCommonUtil.getExecuteResult(targetHost, COMMAND.RUN_USER, commandConfig, strategy, "nginx");
        } else {
            String pid = MWCommonUtil.getExecuteResult(targetHost, COMMAND.NGINX_MASTER_PROCESS_PID, commandConfig, strategy);
            String pidCommand = "sudo ps -ef | grep worker | grep " + pid + " | awk '{print $1}' | head -1";
            runUser = SSHUtil.executeCommand(targetHost, pidCommand);
        }

        return runUser;
    }

    @SuppressWarnings("DuplicatedCode")
    public Map<String, String> getIncludeFileContent(TargetHost targetHost, ObjectMapper objectMapper, GetInfoStrategy strategy, NgxConfig conf,
                                                     String instancePath) throws InterruptedException {
        // 먼저 root와 http block에 있는 include 파일을 구한다.
        NginxAssessmentResult.Instance instance = new NginxAssessmentResult.Instance();
        instance.setGeneral(nginxParseHelper.getGeneral(objectMapper, conf));
        instance.setHttp(nginxParseHelper.getHttp(objectMapper, conf));

        Map<String, String> includeFileContentMap = new HashMap<>();

        List<ConfigFile> configFiles = new ArrayList<>();

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
            // mime type은 제외.
            if (!includeFile.contains("mime")) {
                String includeMapKey = includeFile;
                StringBuffer sb = new StringBuffer();

                if (!isAbsolutePath(includeFile)) {
                    // nginx.conf 파일 path에서 시작.
                    String nginxConfParentPath = instancePath.substring(0, instancePath.lastIndexOf(strategy.getSeparator()));
                    includeFile = nginxConfParentPath + strategy.getSeparator() + includeFile;
                }

                if (includeFile.contains("*")) {
                    String fileList = MWCommonUtil.getExecuteResult(targetHost, COMMAND.FILE_LIST_ONE_LINE, commandConfig, strategy, includeFile);
                    String[] fileArrays = splitToArrayByCrlf(fileList);
                    for (String filePath : fileArrays) {
                        sb.append(removeCommentLine(getConfigFile(targetHost, strategy, commandConfig, filePath).getContent())).append("\r\n");
                    }
                } else {
                    if (AbstractMiddlewareAssessment.fileExists(targetHost, includeFile, commandConfig, strategy)) {
                        sb.append(removeCommentLine(getConfigFile(targetHost, strategy, commandConfig, includeFile).getContent()));
                    }
                }

                includeFileContentMap.put(includeMapKey, sb.toString());
            }
        }

        return includeFileContentMap;
    }

    private boolean isAbsolutePath(String filePath) {
        String windowsRootPathRegx = "^[a-zA-Z]:\\\\(((?![<>:\"/\\\\|?*]).)+((?<![ .])\\\\)?)*$";
        return filePath.startsWith("/") || Pattern.matches(windowsRootPathRegx, filePath);
    }

    private ConfigFile getConfigFile(TargetHost targetHost, GetInfoStrategy strategy, CommandConfig commandConfig, String filePath) throws InterruptedException {
        ConfigFile includeConfigFile = new ConfigFile();

        if (AbstractMiddlewareAssessment.fileExists(targetHost, filePath, commandConfig, strategy)) {
            includeConfigFile.setPath(filePath);
            includeConfigFile.setContent(nginxParseHelper.getCatResult(targetHost, strategy, filePath));
        }

        return includeConfigFile;
    }

    private boolean isPortNumber(String portNumber) {
        return portNumber.matches("([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])");
    }

}