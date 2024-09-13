package io.playce.roro.common.windows;

import io.playce.roro.common.util.WinRmUtils;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TomcatExtract implements ServiceMiddlewareExtract {

    @Override
    public String getRegistryParameterByServiceName(TargetHost targetHost, String serviceName) {
        final String tomcatRegistryCommand = "reg query \"HKLM\\SOFTWARE\\Wow6432Node\\Apache Software Foundation\" /s /f \"" + serviceName + "\" /V \"#searchTxt#\" | findstr /i \"#searchTxt#\"";
        final String jvmPathCommand = tomcatRegistryCommand.replaceAll("#searchTxt#", "Jvm");
        final String vmOptionCommand = tomcatRegistryCommand.replaceAll("#searchTxt#", "Options");

        String registryParameters = "";

        try {
            String javaPathResult = WinRmUtils.executeCommand(targetHost, jvmPathCommand).trim().replaceAll("\\s+", " ");
            String vmOptionsResult = WinRmUtils.executeCommand(targetHost, vmOptionCommand).trim().replaceAll("\\s+", " ");

            // 공백으로 Split한 후 3번째 이후의 모든 데이터를 가져온다.
            String[] javaPathResultArray = getRegistrySplitThirdData(javaPathResult);
            String[] vmOptionResultArray = getRegistrySplitThirdData(vmOptionsResult);

            String javaPath = "";
            if (javaPathResultArray.length > 2) {
                javaPath = String.format("\"%s\" ", javaPathResultArray[2]);
            }

            String vmOptions = "";
            if (vmOptionResultArray.length > 2) {
                vmOptions = vmOptionResultArray[2]
                        .replaceAll("=", "=\"")
                        .replaceAll("\\\\0", "\" ");
                vmOptions += "\"";
            }

            registryParameters += javaPath;
            registryParameters += vmOptions;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return registryParameters;
    }

    // Tomcat이 서비스로 등록된 경우.
    public List<TaskListResult> getTomcatProcessList(TargetHost targetHost) {
        final String tomcatServiceName = "tomcat";

        return getProcessListByServiceName(targetHost, tomcatServiceName);
    }

}