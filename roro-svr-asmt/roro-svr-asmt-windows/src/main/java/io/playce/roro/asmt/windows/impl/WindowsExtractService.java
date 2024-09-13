package io.playce.roro.asmt.windows.impl;

import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.Process;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.Service;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.WindowsResult;
import io.playce.roro.asmt.windows.impl.factory.PowerShellParseUtil;
import io.playce.roro.common.util.WinRmUtils;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.common.windows.JeusExtract;
import io.playce.roro.common.windows.TomcatExtract;
import io.playce.roro.common.windows.WebLogicOhsExtract;
import io.playce.roro.common.windows.WebTobExtract;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Slf4j
@Component
public class WindowsExtractService {

    private final TomcatExtract tomcatExtract;
    private final WebLogicOhsExtract webLogicOhsExtract;
    private final JeusExtract jeusExtract;
    private final WebTobExtract webTobExtract;

    private final String TOMCAT = "tomcat";
    private final String WEB_LOGIC = "beasvc";
    private final String JEUS = "jeusservice-jeus";
    private final String OHS = "wlsvc"; // wlsvc or wlsvcX64
    private final String WEBTOB = "WebtoB";
    private final String NGINX = "nginx";

    private final List<String> ORACLE_MIDDLEWARE = List.of(JEUS, OHS);
    private final List<String> WEBTOB_PROCESS = Arrays.asList("wsm", "htl", "hth", "htmls", "cgis", "ssis");
    private final List<String> MIDDLEWARE_SERVICE_NAME = List.of(TOMCAT, JEUS, WEBTOB);
    private final List<String> MIDDLEWARE_PROCESS_NAME = Stream.concat(ORACLE_MIDDLEWARE.stream(), WEBTOB_PROCESS.stream()).collect(Collectors.toList());

    // 현재 실행중인 서비스 찾기.

    public WindowsResult convertMiddlewareServiceToProcess(TargetHost targetHost, WindowsResult windowsResult) {
        final String SERVICE_COMMAND = "wmic service where 'name like \"%#processName#%\" and state like \"%Running%\"' get ProcessId, Name /format:list";

        // 여러개의 Middleware 서비스 및 프로세스가 존재할 수 있다.
        List<Service> middlewareServices = getMiddlewareService(windowsResult.getServices());
        List<Process> middlewareProcesses = getMiddlewareProcess(windowsResult.getProcess());

        try {
            for (Service middlewareService : middlewareServices) {
                // CommandLine을 정보를 얻기 위해 추가적인 다른정보 조회.
                String middlewareProcess = WinRmUtils.executeCommand(targetHost, SERVICE_COMMAND.replaceAll("#processName#", middlewareService.getName()));

                String serviceName = PowerShellParseUtil.getPropertyValueForMultiLine(middlewareProcess, "Name");
                String processId = PowerShellParseUtil.getPropertyValueForMultiLine(middlewareProcess, "ProcessId");

                if (serviceName.toLowerCase().contains(TOMCAT)) {
                    log.debug("==> Find Tomcat --> Service Name : {}, ProcessID : {}", serviceName, processId);
                    windowsResult = convertTomcatServiceToProcess(targetHost, windowsResult, serviceName, processId);
                } else if (serviceName.equals(JEUS)) {
                    log.debug("==> Find Jeus --> Service Name : {}, ProcessID : {}", serviceName, processId);
                    windowsResult = convertJeusServiceToProcess(targetHost, windowsResult, serviceName, processId);
                } else if (serviceName.equals(WEBTOB)) {
                    log.debug("==> Find WebToB --> Service Name : {}, ProcessID : {}", serviceName, processId);
                    windowsResult = convertWebToBServiceToProcess(targetHost, windowsResult, serviceName, processId);
                }
            }

            // Weblogic은 서비스로 등록할 경우 서비스명을 마음대로 Custom하게 변경할 수가 있다.
            // 그래서 Process에서 찾아야 하는데 Process에서 beasvc라는 프로세스명이 있으면 서비스로 등록해서 사용하고 있다.
            // 23.03.28 Nginx 추가.
            for (Process process : middlewareProcesses) {
                if (process.getProcessName().toLowerCase().contains(WEB_LOGIC) || process.getProcessName().toLowerCase().contains(OHS)) {
                    // 웹로직인 경우 프로세스가 여러개일 수가 있다. (ex : AdminServer, NodeManager등..)
                    log.debug("==> Find Weblogic or OHS --> Process Name : {}, ProcessID : {}", process.getProcessName(), process.getId());
                    windowsResult = convertWebLogicOrOhsProcess(targetHost, windowsResult, process);
                } else if (WEBTOB_PROCESS.stream().anyMatch(process.getProcessName().toLowerCase()::contains)) {
                    log.debug("==> Find WebToB  --> Process Name : {}, ProcessID : {}", process.getProcessName(), process.getId());
                    windowsResult = convertWebToBProcess(windowsResult, process);
                } else if (process.getProcessName().toLowerCase().contains(NGINX)) {
                    log.debug("==> Find Nginx  --> Process Name : {}, ProcessID : {}", process.getProcessName(), process.getId());
                    windowsResult = convertNginxProcess(windowsResult, process);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return windowsResult;
    }

    private List<Service> getMiddlewareService(List<Service> services) {
        List<Service> middlewareServices = new ArrayList<>();

        for (Service service : services) {
            if (MIDDLEWARE_SERVICE_NAME.stream().anyMatch(service.getServiceName().toLowerCase()::contains)
                    || MIDDLEWARE_SERVICE_NAME.stream().anyMatch(service.getServiceName()::equals)) {
                middlewareServices.add(service);
            }
        }

        return middlewareServices;
    }

    private List<Process> getMiddlewareProcess(List<Process> processes) {
        List<Process> middlewareProcesses = new ArrayList<>();

        for (Process process : processes) {
            if (MIDDLEWARE_PROCESS_NAME.stream().anyMatch(process.getProcessName().toLowerCase()::contains) ||
                    process.getProcessName().toLowerCase().contains(NGINX)) {
                middlewareProcesses.add(process);
            }
        }

        return middlewareProcesses;
    }

    private WindowsResult convertTomcatServiceToProcess(TargetHost targetHost, WindowsResult windowsResult, String serviceName, String processId) throws Exception {
        for (Process process : windowsResult.getProcess()) {
            if (process.getId().equals(processId)) {
                process.setCommandLine(process.getCommandLine() + " "
                        + tomcatExtract.getRegistryParameterByServiceName(targetHost, serviceName));
            }
        }

        return windowsResult;
    }

    private WindowsResult convertJeusServiceToProcess(TargetHost targetHost, WindowsResult windowsResult, String serviceName, String processId) throws Exception {
        for (Process process : windowsResult.getProcess()) {
            if (process.getId().equals(processId)) {
                process.setCommandLine(process.getCommandLine() + " " +
                        jeusExtract.getRegistryParameterByServiceName(targetHost, serviceName));
            }
        }

        return windowsResult;
    }

    private WindowsResult convertWebToBServiceToProcess(TargetHost targetHost, WindowsResult windowsResult, String serviceName, String processId) throws Exception {
        for (Process process : windowsResult.getProcess()) {
            if (process.getId().equals(processId)) {
                process.setCommandLine(process.getCommandLine() + " " + webTobExtract.getRegistryParameterByServiceName(targetHost, serviceName));
            }
        }

        return windowsResult;
    }

    private WindowsResult convertWebLogicOrOhsProcess(TargetHost targetHost, WindowsResult windowsResult, Process process) {
        for (Process tempProcess : windowsResult.getProcess()) {
            if (tempProcess.getId().equals(process.getId())) {
                tempProcess.setCommandLine(tempProcess.getCommandLine() + " " +
                        webLogicOhsExtract.getRegistryParameterByPid(targetHost, process.getId()));
            }
        }

        return windowsResult;
    }

    private WindowsResult convertWebToBProcess(WindowsResult windowsResult, Process process) {
        for (Process tempProcess : windowsResult.getProcess()) {
            if (tempProcess.getProcessName().equals(process.getProcessName())) {
                if (process.getPath().contains("bin")) {
                    String webTobDir = process.getPath().substring(0, process.getPath().indexOf("bin") - 1);
                    tempProcess.setCommandLine(tempProcess.getCommandLine() + " " + String.format("WEBTOBDIR=\"%s\"", webTobDir));
                }
            }
        }

        return windowsResult;
    }

    private WindowsResult convertNginxProcess(WindowsResult windowsResult, Process process) {
        boolean isMasterProcess = false;

        for (Process tempProcess : windowsResult.getProcess()) {
            if (tempProcess.getProcessName().equals(process.getProcessName())) {
                if (!isMasterProcess) {
                    tempProcess.setCommandLine("nginx: master process " + tempProcess.getPath());
                    isMasterProcess = true;
                } else {
                    tempProcess.setCommandLine("nginx: worker process");
                }
            }
        }

        return windowsResult;
    }
}
