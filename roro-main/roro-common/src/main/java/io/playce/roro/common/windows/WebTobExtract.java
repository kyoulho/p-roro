package io.playce.roro.common.windows;

import io.playce.roro.common.util.WinRmUtils;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class WebTobExtract implements ServiceMiddlewareExtract {

    @Override
    public String getRegistryParameterByServiceName(TargetHost targetHost, String serviceName) {
        final String registryCommand = "reg query \"HKLM\\SYSTEM\\CurrentControlSet\\Services\\#serviceName#\" /s /f \"parameter\" /V \"#searchTxt#\" | findstr /i \"#searchTxt#\"";

        String webTobDirCommand = registryCommand.replaceAll("#serviceName#", serviceName).replaceAll("#searchTxt#", "WEBTOBDIR");

        String registryParameters = "";

        try {
            String webToDirResult = WinRmUtils.executeCommand(targetHost, webTobDirCommand).trim().replaceAll("\\s+", " ");
            String[] webToDirResultArray = getRegistrySplitThirdData(webToDirResult);
            String webToDirPath = webToDirResultArray.length > 2 ? String.format("WEBTOBDIR=\"%s\"", webToDirResultArray[2]) : "";

            registryParameters += webToDirPath;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return registryParameters;
    }

    public List<TaskListResult> getWebTobProcessList(TargetHost targetHost) {
        final String webTobServiceName = "WebtoB";

        return getProcessListByServiceName(targetHost, webTobServiceName);
    }

}
