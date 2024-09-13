package io.playce.roro.common.windows;

import io.playce.roro.common.util.WinRmUtils;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class JeusExtract implements ServiceMiddlewareExtract {

    @Override
    public String getRegistryParameterByServiceName(TargetHost targetHost, String serviceName) {
        final String registryCommand = "reg query \"HKLM\\SYSTEM\\CurrentControlSet\\Services\\#serviceName#\" /s /f \"parameter\" /V \"#searchTxt#\" | findstr /i \"#searchTxt#\"";

        String javaPathCommand = registryCommand.replaceAll("#serviceName#", serviceName).replaceAll("#searchTxt#", "java.home");
        String vmOptionsCommand = registryCommand.replaceAll("#serviceName#", serviceName).replaceAll("#searchTxt#", "jeus.boot");

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

    public List<TaskListResult> getJeusProcessList(TargetHost targetHost) {
        final String jeusServiceName = "jeusservice-jeus";

        return getProcessListByServiceName(targetHost, jeusServiceName);
    }

}
