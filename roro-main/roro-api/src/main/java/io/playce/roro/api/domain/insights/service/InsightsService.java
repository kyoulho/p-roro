/*
 * Copyright 2023 The playce-roro-v3 Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * SangCheon Park   Jan 11, 2023		    First Draft.
 */
package io.playce.roro.api.domain.insights.service;

import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.dto.insights.*;
import io.playce.roro.common.dto.productlifecycle.ProductLifecycleRulesVersionResponse;
import io.playce.roro.common.insights.LifecycleVersionManager;
import io.playce.roro.jpa.entity.InventoryLifecycleVersionLink;
import io.playce.roro.jpa.repository.InventoryLifecycleVersionLinkRepository;
import io.playce.roro.mybatis.domain.insights.InsightMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InsightsService {
    private final InsightMapper insightMapper;
    private final InsightsExcelExporter excelExporter;
    private final LifecycleVersionManager lifecycleVersionManager;
    private final InventoryLifecycleVersionLinkRepository inventoryLifecycleVersionLinkRepository;

    public LifecycleResponse getLifecycleResponse(Long projectId, Long inventoryId, String type) {
        return insightMapper.selectLifecycleResponse(projectId, inventoryId, type).orElse(new LifecycleResponse());
    }

    private List<ProductLifecycleRulesVersionResponse> getSolutionAllVersions(String solutionName) {
        switch (solutionName) {
            case "ORACLE":
                solutionName = "Oracle Database";
                break;
            case "Apache Tomcat":
                solutionName = "Tomcat";
                break;
        }
        return insightMapper.selectProductLifecycleRulesVersionsBySolutionName(solutionName);
    }

    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public ByteArrayOutputStream getInsightsReport(Long projectId, Integer within, String serviceIds) {
        InsightListDto insights = getInsights(projectId, within, serviceIds, true);
        return excelExporter.createExcelReport(projectId, within, insights);
    }

    public BillboardResponse getBillboard(Long projectId, Integer within, String serviceIds) {
        InsightListDto insights = getInsights(projectId, within, serviceIds, false);
        var eolEnded = insights.getAllList().stream().filter(insightDto -> "ended".equals(insightDto.getEol())).collect(groupingBy(InsightDto::getSolutionType, counting()));
        var eolWithin = insights.getAllList().stream().filter(insightDto -> "tbe".equals(insightDto.getEol())).collect(groupingBy(InsightDto::getSolutionType, counting()));
        var eosEnded = insights.getAllList().stream().filter(insightDto -> "ended".equals(insightDto.getEos())).collect(groupingBy(InsightDto::getSolutionType, counting()));
        var eosWithin = insights.getAllList().stream().filter(insightDto -> "tbe".equals(insightDto.getEos())).collect(groupingBy(InsightDto::getSolutionType, counting()));

        BillboardResponse billboardResponse = new BillboardResponse();
        billboardResponse.setEol(settingMap(eolEnded));
        billboardResponse.setEos(settingMap(eosEnded));
        billboardResponse.setEolWithin(settingMap(eolWithin));
        billboardResponse.setEosWithin(settingMap(eosWithin));
        return billboardResponse;
    }

    private Map<String, Long> settingMap(Map<String, Long> map) {
        Map<String, Long> result = new HashMap<>();
        result.put(Domain1001.SVR.name(), map.get("Server") != null ? map.get("Server") : 0);
        result.put(Domain1001.MW.name(), map.get("Middleware") != null ? map.get("Middleware") : 0);
        result.put(Domain1001.APP.name(), map.get("Java Application") != null ? map.get("Java Application") : 0);
        result.put(Domain1001.DBMS.name(), map.get("Database") != null ? map.get("Database") : 0);
        return result;
    }


    public void createInventoryLifecycleVersionLink(Long inventoryId, Domain1001 inventoryTypeCode, String solutionName, String solutionVersion, String jdkVendor, String jdkVersion) {
        InventoryLifecycleVersionLink link = new InventoryLifecycleVersionLink();
        link.setInventoryId(inventoryId);

        switch (inventoryTypeCode) {
            case SVR:
            case DBMS: {
                List<ProductLifecycleRulesVersionResponse> solutionAllVersions = getSolutionAllVersions(solutionName);
                ProductLifecycleRulesVersionResponse versionResponse = lifecycleVersionManager.getVersionResponse(solutionVersion, solutionAllVersions);
                link.setProductVersionId(versionResponse.getProductLifecycleRulesVersionId());
            }
            break;
            case MW: {
                List<ProductLifecycleRulesVersionResponse> solutionAllVersions = getSolutionAllVersions(solutionName);
                ProductLifecycleRulesVersionResponse versionResponse = lifecycleVersionManager.getVersionResponse(solutionVersion, solutionAllVersions);
                link.setProductVersionId(versionResponse.getProductLifecycleRulesVersionId());
            }
            case APP: {
                if (jdkVendor == null) {
                    break;
                }

                if (jdkVendor.contains("Red Hat")) {
                    jdkVendor = "Oracle";
                }
                List<ProductLifecycleRulesVersionResponse> jdkAllVersions = getSolutionAllVersions(String.format("Java By %s", jdkVendor));
                ProductLifecycleRulesVersionResponse jdkVersionResponse = lifecycleVersionManager.getVersionResponse(jdkVersion, jdkAllVersions);
                link.setJavaVersionId(jdkVersionResponse.getProductLifecycleRulesVersionId());
            }
            break;
        }
        inventoryLifecycleVersionLinkRepository.save(link);
    }

    public BillboardDetailResponse getBillboardDetail(Long projectId) {
        List<BillboardDetail> list = insightMapper.selectBillboardDetails(projectId, getStartOfDay(new Date()));
        Map<String, List<BillboardDetail>> map = list.stream().collect(groupingBy(BillboardDetail::getInventoryTypeCode));

        BillboardDetailResponse response = new BillboardDetailResponse();
        response.setServer(map.get(Domain1001.SVR.name()));
        response.setMiddleware(map.get(Domain1001.MW.name()));
        response.setDatabase(map.get(Domain1001.DBMS.name()));
        response.setJava(map.get(Domain1001.APP.name()));
        return response;
    }

    public InsightListDto getInsights(Long projectId, Integer within, String serviceIds, boolean toExcel) {
        List<String> serviceIdList = null;
        if (StringUtils.isNotEmpty(serviceIds)) {
            serviceIdList = Arrays.asList(serviceIds.split(","));
        }
        InsightListDto insightListDto = new InsightListDto();

        Map<String, List<InsightDto>> map = insightMapper.selectInsights(projectId, within, getStartOfDay(new Date()), serviceIdList, toExcel)
                .stream()
                .collect(groupingBy(InsightDto::getSolutionType));

        insightListDto.setOperatingSystems(map.get("Server"));
        insightListDto.setJava(map.get("Java Application"));
        insightListDto.setDatabases(map.get("Database"));
        insightListDto.setMiddlewares(map.get("Middleware"));

        return insightListDto;
    }
}
//end of InsightsService.java