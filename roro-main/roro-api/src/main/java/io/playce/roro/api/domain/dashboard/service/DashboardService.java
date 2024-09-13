package io.playce.roro.api.domain.dashboard.service;

import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.jpa.entity.Dashboard;
import io.playce.roro.jpa.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public String getDashboard(Long projectId) {
        Dashboard dashboard = dashboardRepository.findByProjectIdAndUserId(projectId, WebUtil.getUserId());

        if (dashboard != null) {
            return StringUtils.defaultString(dashboard.getConfigContents());
        } else {
            return "";
        }
    }

    @Transactional
    public void createConfigContents(Long projectId, String configContents) {
        Dashboard dashboard = dashboardRepository.findByProjectIdAndUserId(projectId, WebUtil.getUserId());

        if (dashboard == null) {
            Dashboard createDashboard = new Dashboard();
            createDashboard.setProjectId(projectId);
            createDashboard.setConfigContents(configContents);
            createDashboard.setUserId(WebUtil.getUserId());

            dashboardRepository.save(createDashboard);
        } else {
            dashboard.setConfigContents(configContents);

            dashboardRepository.save(dashboard);
        }

    }

}
