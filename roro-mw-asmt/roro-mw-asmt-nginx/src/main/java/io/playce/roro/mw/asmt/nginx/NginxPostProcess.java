package io.playce.roro.mw.asmt.nginx;

import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.mw.asmt.AbstractMiddlewarePostProcess;
import io.playce.roro.mw.asmt.dto.DiscApplication;
import io.playce.roro.mw.asmt.dto.DiscInstanceInterface;
import io.playce.roro.mw.asmt.dto.DiscMiddlewareInstance;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult.Instance;
import io.playce.roro.mw.asmt.nginx.dto.NginxAssessmentResult;
import io.playce.roro.mw.asmt.nginx.dto.NginxAssessmentResult.Server;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("NGINXPostProcessor")
@Slf4j
public class NginxPostProcess extends AbstractMiddlewarePostProcess {

    @Override
    public List<DiscMiddlewareInstance> getDiscoveredMiddlewareInstances(MiddlewareAssessmentResult result, GetInfoStrategy strategy) {
        if (result == null) {
            return null;
        }

        NginxAssessmentResult nginxAssessmentResult = (NginxAssessmentResult) result;
        NginxAssessmentResult.Engine engine = (NginxAssessmentResult.Engine) nginxAssessmentResult.getEngine();
        NginxAssessmentResult.Instance instance = (NginxAssessmentResult.Instance) nginxAssessmentResult.getInstance();

        List<DiscMiddlewareInstance> discMiddlewareInstances = new ArrayList<>();

        DiscMiddlewareInstance discMiddlewareInstance = new DiscMiddlewareInstance();

        List<String> serverNames = new ArrayList<>();
        for (Server server : instance.getHttp().getServers()) {
            if (StringUtils.isNotEmpty(server.getServerName())) {
                serverNames.add(server.getServerName());
            }
        }

        String configInstancePath = engine.getConfigPath();
        String instanceName;

        try {
            instanceName = configInstancePath.substring(configInstancePath.lastIndexOf(strategy.getSeparator()) + 1);
            instanceName = instanceName.substring(0, instanceName.lastIndexOf("."));
        } catch (Exception e) {
            instanceName = "nginx";
        }

        discMiddlewareInstance.setMiddlewareInstanceName(instanceName);
        discMiddlewareInstance.setMiddlewareInstancePath(instance.getGeneral().getInstallHome());
        discMiddlewareInstance.setMiddlewareInstanceDetailDivision(instance.getGeneral().getInstallHome());
        discMiddlewareInstance.setMiddlewareConfigPath(null);

        List<String> portList = new ArrayList<>();
        List<String> protocolList = new ArrayList<>();

        List<Integer> listenPort = instance.getGeneral().getListenPort();

        if (CollectionUtils.isNotEmpty(listenPort)) {
            for (int port : listenPort) {
                portList.add(String.valueOf(port));
                if (String.valueOf(port).equals("443")) {
                    protocolList.add("SSL");
                } else {
                    protocolList.add("HTTP");
                }
            }
        }

//        if (instance.getGeneral().isSsl()) {
//            protocolList.add("SSL");
//        }

//        protocolList = protocolList.stream().distinct().collect(Collectors.toList());

        discMiddlewareInstance.setMiddlewareInstanceServicePort(String.join(",", portList));
        discMiddlewareInstance.setMiddlewareInstanceProtocol(String.join(",", protocolList));
        discMiddlewareInstance.setJavaVersion(NOT_JAVA);
        discMiddlewareInstance.setRunningUser(engine.getRunUser());
        discMiddlewareInstance.setRuuning(StringUtils.isNotEmpty(engine.getRunUser()));

        discMiddlewareInstances.add(discMiddlewareInstance);

        return discMiddlewareInstances;
    }

    @Override
    public List<DiscInstanceInterface> getDiscoveredInstanceInterfaces(Instance instance, DiscMiddlewareInstance discInstance) {
        return new ArrayList<>();
    }

    @Override
    public List<DiscApplication> getDiscoveredApplications(InventoryProcessQueueItem item, Instance instance, DiscMiddlewareInstance discInstance, GetInfoStrategy strategy) {
        return new ArrayList<>();
    }

    @Override
    public String getEngineVersion(MiddlewareAssessmentResult result) {
        if (result == null) {
            return null;
        }

        NginxAssessmentResult.Engine engine = (NginxAssessmentResult.Engine) result.getEngine();
        return engine.getVersion();
    }
}