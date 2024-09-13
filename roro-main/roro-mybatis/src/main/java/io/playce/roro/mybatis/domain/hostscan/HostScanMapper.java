package io.playce.roro.mybatis.domain.hostscan;

import io.playce.roro.common.dto.hostscan.DiscoveredHostDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostScanMapper {
    List<DiscoveredHostDto> selectDiscoveredHostAndRegisteredServers(Long projectId, Long scanHistoryId);
    List<DiscoveredHostDto> selectIpAddressAndOsName(Long scanHistoryId);
}
