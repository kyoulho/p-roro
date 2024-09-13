package io.playce.roro.api.domain.tracking.processor.server;

import io.playce.roro.common.enums.TrackingKey;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.jpa.entity.TrackingInfo;
import io.playce.roro.jpa.repository.TrackingInfoRepository;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import io.playce.roro.svr.asmt.dto.common.Package;
import io.playce.roro.svr.asmt.dto.result.DebianAssessmentResult;
import io.playce.roro.svr.asmt.dto.result.SolarisAssessmentResult;
import io.playce.roro.svr.asmt.dto.result.UbuntuAssessmentResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ServerTrackingInfoProcessor implements IServerTrackingInfoProcessor {
    protected final TrackingInfoRepository trackingInfoRepository;

    @Override
    public void saveTrackingInfo(Long inventoryProcessId, ServerAssessmentResult result) {
        saveCpuModel(inventoryProcessId, result);
        saveCpuCores(inventoryProcessId, result);
        saveCpuCount(inventoryProcessId, result);
        saveMemory(inventoryProcessId, result);
        saveKernel(inventoryProcessId, result);
        saveOS(inventoryProcessId, result);
        saveBios(inventoryProcessId, result);
        saveHost(inventoryProcessId, result);
        saveNetworkInterface(inventoryProcessId, result);
        saveFileSystem(inventoryProcessId, result);
        saveUser(inventoryProcessId, result);
        saveGroup(inventoryProcessId, result);
        savePackages(inventoryProcessId, result);
        saveVG(inventoryProcessId, result);
        saveFirewall(inventoryProcessId, result);
        saveRouteTables(inventoryProcessId, result);
        saveDNS(inventoryProcessId, result);
    }

    @Override
    public boolean isSupported(ServerAssessmentResult result) {
        return result instanceof DebianAssessmentResult || result instanceof SolarisAssessmentResult || result instanceof UbuntuAssessmentResult;
    }

    protected void saveCpuModel(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo cpuModel = new TrackingInfo();
        cpuModel.setInventoryProcessId(inventoryProcessId);
        cpuModel.setTrackingKey(TrackingKey.CPU_MODEL);
        cpuModel.setContent(result.getCpu().getProcessor());
        trackingInfoRepository.save(cpuModel);
    }

    protected void saveCpuCores(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo cpuCores = new TrackingInfo();
        cpuCores.setInventoryProcessId(inventoryProcessId);
        cpuCores.setTrackingKey(TrackingKey.CPU_CORES);
        cpuCores.setContent(result.getCpu().getProcessorCores());
        trackingInfoRepository.save(cpuCores);
    }

    protected void saveCpuCount(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo cpuCount = new TrackingInfo();
        cpuCount.setInventoryProcessId(inventoryProcessId);
        cpuCount.setTrackingKey(TrackingKey.CPU_COUNT);
        cpuCount.setContent(result.getCpu().getProcessorCount());
        trackingInfoRepository.save(cpuCount);
    }

    protected void saveMemory(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo memory = new TrackingInfo();
        memory.setInventoryProcessId(inventoryProcessId);
        memory.setTrackingKey(TrackingKey.MEMORY_SIZE);
        memory.setContent(result.getMemory().getMemTotalMb());
        trackingInfoRepository.save(memory);
    }

    protected void saveKernel(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo kernel = new TrackingInfo();
        kernel.setInventoryProcessId(inventoryProcessId);
        kernel.setTrackingKey(TrackingKey.KERNEL_VERSION);
        kernel.setContent(result.getKernel());
        trackingInfoRepository.save(kernel);
    }

    protected void saveOS(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo os = new TrackingInfo();
        os.setInventoryProcessId(inventoryProcessId);
        os.setTrackingKey(TrackingKey.OS_VERSION);
        os.setContent(result.getDistributionRelease());
        trackingInfoRepository.save(os);
    }

    protected void saveBios(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo bios = new TrackingInfo();
        bios.setInventoryProcessId(inventoryProcessId);
        bios.setTrackingKey(TrackingKey.BIOS_VERSION);
        bios.setContent(result.getBiosVersion());
        trackingInfoRepository.save(bios);
    }

    protected void saveHost(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo host = new TrackingInfo();
        host.setInventoryProcessId(inventoryProcessId);
        host.setTrackingKey(TrackingKey.HOST);
        host.setContent(JsonUtil.writeValueAsString(
                result.getHosts().getMappings()));
        trackingInfoRepository.save(host);
    }

    protected void saveNetworkInterface(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo networkInterface = new TrackingInfo();
        networkInterface.setInventoryProcessId(inventoryProcessId);
        networkInterface.setTrackingKey(TrackingKey.NETWORK_INTERFACE);
        networkInterface.setContent(JsonUtil.writeValueAsStringExcludeFields(result.getInterfaces(), "rxBytes/s", "txBytes/s"));
        trackingInfoRepository.save(networkInterface);
    }

    protected void saveFileSystem(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo fileSystem = new TrackingInfo();
        fileSystem.setInventoryProcessId(inventoryProcessId);
        fileSystem.setTrackingKey(TrackingKey.FILE_SYSTEM);
        fileSystem.setContent(JsonUtil.writeValueAsString(result.getPartitions()));
        trackingInfoRepository.save(fileSystem);
    }

    protected void saveUser(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo user = new TrackingInfo();
        user.setInventoryProcessId(inventoryProcessId);
        user.setTrackingKey(TrackingKey.USER);
        user.setContent(JsonUtil.writeValueAsString(result.getUsers()));
        trackingInfoRepository.save(user);
    }

    protected void saveGroup(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo group = new TrackingInfo();
        group.setInventoryProcessId(inventoryProcessId);
        group.setTrackingKey(TrackingKey.GROUP);
        group.setContent(JsonUtil.writeValueAsString(result.getGroups()));
        trackingInfoRepository.save(group);
    }

    protected void savePackages(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo packages = new TrackingInfo();
        packages.setInventoryProcessId(inventoryProcessId);
        packages.setTrackingKey(TrackingKey.PACKAGE);
        List<String> names = result.getPackages().stream().map(Package::getName).collect(Collectors.toList());
        packages.setContent(JsonUtil.writeValueAsString(names));
        trackingInfoRepository.save(packages);
    }

    protected void saveVG(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo vgs = new TrackingInfo();
        vgs.setInventoryProcessId(inventoryProcessId);
        vgs.setTrackingKey(TrackingKey.VG);
        vgs.setContent(StringUtils.EMPTY);
        trackingInfoRepository.save(vgs);
    }

    protected void saveFirewall(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo firewall = new TrackingInfo();
        firewall.setInventoryProcessId(inventoryProcessId);
        firewall.setTrackingKey(TrackingKey.FIREWALL);
        firewall.setContent(JsonUtil.writeValueAsString(result.getFirewall()));
        trackingInfoRepository.save(firewall);
    }

    protected void saveRouteTables(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo routeTables = new TrackingInfo();
        routeTables.setInventoryProcessId(inventoryProcessId);
        routeTables.setTrackingKey(TrackingKey.ROUTE);
        routeTables.setContent(JsonUtil.writeValueAsString(result.getRouteTables()));
        trackingInfoRepository.save(routeTables);
    }

    protected void saveDNS(Long inventoryProcessId, ServerAssessmentResult result) {
        TrackingInfo dns = new TrackingInfo();
        dns.setInventoryProcessId(inventoryProcessId);
        dns.setTrackingKey(TrackingKey.DNS);
        dns.setContent(JsonUtil.writeValueAsString(result.getDns()));
        trackingInfoRepository.save(dns);
    }
}
