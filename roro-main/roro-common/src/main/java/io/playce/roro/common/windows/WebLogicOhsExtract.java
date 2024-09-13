package io.playce.roro.common.windows;

import io.playce.roro.common.util.WinRmUtils;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class WebLogicOhsExtract implements ServiceMiddlewareExtract {

    @Override
    public String getRegistryParameterByServiceName(TargetHost targetHost, String serviceName) {
        final String registryCommand = "reg query \"HKLM\\SYSTEM\\CurrentControlSet\\Services\\#serviceName#\" /s /f \"parameter\" /V \"#searchTxt#\" | findstr /i \"#searchTxt#\"";

        String javaPathCommand = registryCommand.replaceAll("#serviceName#", serviceName).replaceAll("#searchTxt#", "JavaHome");
        String vmOptionsCommand = registryCommand.replaceAll("#serviceName#", serviceName).replaceAll("#searchTxt#", "CmdLine");

        String registryParameters = "";

        try {
            // Trim한 후 모든 공백을 하나의 공백으로 변경한다.
            String javaPathResult = WinRmUtils.executeCommand(targetHost, javaPathCommand).trim().replaceAll("\\s+", " ");
            String vmOptionsResult = WinRmUtils.executeCommand(targetHost, vmOptionsCommand).trim().replaceAll("\\s+", " ");

            String[] javaPathResultArray = getRegistrySplitThirdData(javaPathResult);
            String[] vmOptionResultArray = getRegistrySplitThirdData(vmOptionsResult);

            String javaPath = "";
            if (javaPathResultArray.length > 2) {
                javaPath = String.format("\"%s\\bin\\java\" ", javaPathResultArray[2]);
            }

            registryParameters += javaPath;
            registryParameters += convertVmOptionPath(vmOptionResultArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return registryParameters;
    }

    // 웹 로직은 서비스 이름으로 찾을 수 없다.
    // 프로세스 상에서 PID를 찾은 후
    // 서비스에 등록된 WebLogic, OHS에 대한 Registry Parameter 정보를 읽는다.
    public String getRegistryParameterByPid(TargetHost targetHost, String processId) {
        final String serviceCommandByPid = "tasklist /svc /fi  \"pid eq #processId#\" /fo:csv /nh";
        String registryParameters = "";

        try {
            // ProcessId를 이용하여 Service Name을 가져온다.
            String serviceProcessIdCommand = serviceCommandByPid.replaceAll("#processId#", processId);
            String serviceResult = WinRmUtils.executeCommand(targetHost, serviceProcessIdCommand).trim();
            log.debug("Execute Command : {}", serviceProcessIdCommand);
            log.debug("Execute Result : {}", serviceResult);

            // Example : "beasvc.exe","2504","Oracle WebLogic NodeManager (C_Oracle_Middleware_wlserver_10.3)"
            String[] serviceResultArray = serviceResult.split(",");
            String serviceName = serviceResultArray[2].replaceAll("\"", "");

            registryParameters = getRegistryParameterByServiceName(targetHost, serviceName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return registryParameters;
    }

    // WebLogic이 서비스로 등록된 경우.
    public List<TaskListResult> getWebLogicProcessList(TargetHost targetHost) {
        final String webLogicProcessName = "beasvc";

        return getProcessList(targetHost, webLogicProcessName);
    }

    // OHS가 서비스로 등록된 경우.
    public List<TaskListResult> getOhsProcessList(TargetHost targetHost) {
        final String ohsProcessName = "wlsvc"; // wlsvc or wlsvcX64

        return getProcessList(targetHost, ohsProcessName);
    }

    private List<TaskListResult> getProcessList(TargetHost targetHost, String processName) {
        List<TaskListResult> oracleProcesses = new ArrayList<>();

        // 서비스 프로세스 찾기
        // 여러개의 프로세스가 나올 수 있다.
        final String findServiceProcessCommand = "tasklist /svc /fi \"ImageName eq #processName#*\" /fo:csv /nh";

        try {
            String taskCommand = findServiceProcessCommand.replaceAll("#processName#", processName);
            String taskResult = WinRmUtils.executeCommand(targetHost, taskCommand).trim();
            log.debug("Execute Command : {}", taskCommand);
            log.debug("Execute Result : {}", taskResult);

            oracleProcesses.addAll(getTaskListResults(taskResult));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return oracleProcesses;
    }

}
