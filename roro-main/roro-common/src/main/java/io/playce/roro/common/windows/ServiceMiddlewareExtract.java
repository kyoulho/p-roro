package io.playce.roro.common.windows;

import io.playce.roro.common.util.WinRmUtils;
import io.playce.roro.common.util.support.TargetHost;

import java.util.ArrayList;
import java.util.List;

import static io.playce.roro.common.util.StringUtil.splitToArrayByCrlf;

public interface ServiceMiddlewareExtract {

    String getRegistryParameterByServiceName(TargetHost targetHost, String serviceName);

    default List<TaskListResult> getProcessListByServiceName(TargetHost targetHost, String serviceName) {
        final String findServiceCommand = "tasklist /svc /fi \"SERVICES eq #serviceName#*\" /fo:csv /nh";

        List<TaskListResult> tomcatProcesses = new ArrayList<>();

        try {
            String taskCommand = findServiceCommand.replaceAll("#serviceName#", serviceName);
            String taskResult = WinRmUtils.executeCommand(targetHost, taskCommand).trim();

            tomcatProcesses.addAll(getTaskListResults(taskResult));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tomcatProcesses;
    }

    // CSV 형태로 넘어온 Service 프로세스 정보를 구한다.
    default List<TaskListResult> getTaskListResults(String taskListResult) {
        List<TaskListResult> taskListResults = new ArrayList<>();
        String[] lineStr = splitToArrayByCrlf(taskListResult);

        for (String csvResult : lineStr) {
            // Example : "beasvc.exe","2504","Oracle WebLogic NodeManager (C_Oracle_Middleware_wlserver_10.3)"
            String[] serviceResultArray = csvResult.split(",");

            if (serviceResultArray.length == 3) {
                TaskListResult tempTaskListResult = new TaskListResult();

                tempTaskListResult.setImageName(serviceResultArray[0].replaceAll("\"", ""));
                tempTaskListResult.setPid(serviceResultArray[1].replaceAll("\"", ""));
                tempTaskListResult.setServiceName(serviceResultArray[2].replaceAll("\"", ""));

                taskListResults.add(tempTaskListResult);
            }

        }

        return taskListResults;
    }

    default String[] getRegistrySplitThirdData(String registryData) {
        // Example
        // CmdLine REG_SZ -Xms512m -Xmx512m -Dweblogic.ProductionModeEnabled=true -da
        return registryData.split(" ", 3);
    }

    default String convertVmOptionPath(String[] vmOptionResultArray) {
        StringBuilder vmOptions = new StringBuilder();

        if (vmOptionResultArray.length > 2) {
            // Tomcat과 다르게 \0 이 붙지 않아서 공백으로 구분함.
            // Path에는 Double Quotation 이 붙은것도 있고 안붙은것도 있음.
            String[] vmOptionsArray = vmOptionResultArray[2].split(" ");
            for (String tempVmOption : vmOptionsArray) {
                if ((tempVmOption.startsWith("-D") || tempVmOption.startsWith("-d"))
                        && !tempVmOption.contains("=\"") && tempVmOption.contains("=")) {
                    tempVmOption = tempVmOption.replaceAll("=", "=\"") + "\"";
                }
                vmOptions.append(tempVmOption);
                vmOptions.append(" ");
            }
        }

        return vmOptions.toString();
    }

}
