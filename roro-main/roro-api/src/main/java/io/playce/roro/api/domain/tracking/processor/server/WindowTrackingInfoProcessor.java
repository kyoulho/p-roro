package io.playce.roro.api.domain.tracking.processor.server;

import io.playce.roro.common.enums.TrackingKey;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.jpa.entity.TrackingInfo;
import io.playce.roro.jpa.repository.TrackingInfoRepository;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import io.playce.roro.svr.asmt.dto.result.WindowsAssessmentResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WindowTrackingInfoProcessor extends ServerTrackingInfoProcessor {

    public WindowTrackingInfoProcessor(TrackingInfoRepository trackingInfoRepository) {
        super(trackingInfoRepository);
    }

    @Override
    protected void saveFirewall(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo firewall = new TrackingInfo();
        firewall.setInventoryProcessId(inventoryProcessId);
        firewall.setTrackingKey(TrackingKey.FIREWALL);
        WindowsAssessmentResult assessmentResult = (WindowsAssessmentResult) result;
        firewall.setContent(JsonUtil.writeValueAsStringExcludeFields(
                assessmentResult.getWindowsResult().getFirewalls(),
                "displayName",
                "description"
        ));
        trackingInfoRepository.save(firewall);
    }

    @Override
    protected void saveHost(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo host = new TrackingInfo();
        host.setInventoryProcessId(inventoryProcessId);
        host.setTrackingKey(TrackingKey.HOST);
        var assessmentResult = (WindowsAssessmentResult) result;
        host.setContent(JsonUtil.writeValueAsString(
                assessmentResult.getWindowsResult().getHosts().getMappings()));
        trackingInfoRepository.save(host);
    }

    @Override
    protected void savePackages(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo packages = new TrackingInfo();
        WindowsAssessmentResult windowsResult = (WindowsAssessmentResult) result;
        var installedSoftware = windowsResult.getWindowsResult().getInstalledSoftware();
        List<String> names = installedSoftware.stream().map(WindowsAssessmentResult.InstalledSoftware::getDisplayName).collect(Collectors.toList());

        packages.setInventoryProcessId(inventoryProcessId);
        packages.setTrackingKey(TrackingKey.PACKAGE);
        packages.setContent(JsonUtil.writeValueAsString(names));
        trackingInfoRepository.save(packages);
    }

    @Override
    protected void saveRouteTables(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo routeTable = new TrackingInfo();
        routeTable.setInventoryProcessId(inventoryProcessId);
        routeTable.setTrackingKey(TrackingKey.ROUTE);
        var assessmentResult = (WindowsAssessmentResult) result;
        routeTable.setContent(JsonUtil.writeValueAsString(
                assessmentResult.getWindowsResult().getRoutes()));
        trackingInfoRepository.save(routeTable);
    }

    @Override
    public boolean isSupported(ServerAssessmentResult result) {
        return result instanceof WindowsAssessmentResult;
    }
}
