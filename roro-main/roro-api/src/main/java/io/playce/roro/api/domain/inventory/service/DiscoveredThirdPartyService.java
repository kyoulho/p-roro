/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * SangCheon Park   Oct 04, 2022		    First Draft.
 */
package io.playce.roro.api.domain.inventory.service;

import io.playce.roro.common.dto.inventory.thirdparty.DiscoveredThirdPartyDto;
import io.playce.roro.common.dto.inventory.thirdparty.DiscoveredThirdPartyResponse;
import io.playce.roro.common.dto.inventory.thirdparty.DiscoveredThirdPartyResponse.DiscoveredThirdPartyInventory;
import io.playce.roro.common.dto.inventory.thirdparty.DiscoveredThirdPartyResponse.DiscoveryType;
import io.playce.roro.common.dto.thirdparty.ThirdPartyDiscoveryResult;
import io.playce.roro.jpa.entity.DiscoveredThirdParty;
import io.playce.roro.jpa.repository.DiscoveredThirdPartyRepository;
import io.playce.roro.mybatis.domain.inventory.service.ServiceMapper;
import io.playce.roro.mybatis.domain.thirdparty.ThirdPartyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static io.playce.roro.common.util.support.DistinctByKey.distinctByKey;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscoveredThirdPartyService {

    private final DiscoveredThirdPartyRepository discoveredThirdPartyRepository;
    private final ThirdPartyMapper thirdPartyMapper;
    private final ServiceMapper serviceMapper;
    private final DiscoveredThirdPartyExcelExporter discoveredThirdPartyExcelExporter;

    private final ModelMapper modelMapper;

    public void addDiscoveredThirdParties(Long inventoryProcessId, List<ThirdPartyDiscoveryResult> thirdPartySolutions) {
        for (ThirdPartyDiscoveryResult thirdPartyDiscoveryResult : thirdPartySolutions) {
            for (ThirdPartyDiscoveryResult.ThirdPartyDiscoveryDetail detail : thirdPartyDiscoveryResult.getDiscoveryDetails()) {
                DiscoveredThirdParty discoveredThirdParty = new DiscoveredThirdParty();
                discoveredThirdParty.setInventoryProcessId(inventoryProcessId);
                discoveredThirdParty.setThirdPartySearchTypeId(detail.getThirdPartySearchTypeId());
                discoveredThirdParty.setFindContents(detail.getValue());

                discoveredThirdPartyRepository.save(discoveredThirdParty);
            }
        }
    }

    public List<DiscoveredThirdPartyResponse> getProjectThirdParties(Long projectId, String serviceIds) {
        List<DiscoveredThirdPartyResponse> discoveredThirdPartyResponses = new ArrayList<>();

        List<DiscoveredThirdPartyDto> discoveredThirdPartyDtos =
                thirdPartyMapper.selectProjectDiscoveredThirdParty(projectId, convertServiceId(serviceIds));

        for (DiscoveredThirdPartyDto discoveredThirdPartyDto : discoveredThirdPartyDtos) {
            DiscoveredThirdPartyResponse discoveredThirdPartyResponse = new DiscoveredThirdPartyResponse();

            List<DiscoveredThirdPartyDto> discoveredThirdPartyDtoList =
                    thirdPartyMapper.selectProjectThirdParties(projectId, convertServiceId(serviceIds), discoveredThirdPartyDto.getThirdPartySolutionId());

            // 해당 솔루션에 해당하는 Inventory 중복을 제거한다.
            List<DiscoveredThirdPartyDto> distinctByInventoryIds = discoveredThirdPartyDtoList.stream()
                    .filter(distinctByKey(p -> String.valueOf(p.getInventoryId())))
                    .collect(Collectors.toList());

            // Inventory 정보를 Setting한다.
            List<DiscoveredThirdPartyInventory> discoveredThirdPartyInventories = new ArrayList<>();
            for (DiscoveredThirdPartyDto distinctInventory : distinctByInventoryIds) {
                DiscoveredThirdPartyInventory discoveredThirdPartyInventory = modelMapper.map(distinctInventory, DiscoveredThirdPartyInventory.class);
                discoveredThirdPartyInventory.setDiscoveredDatetime(distinctInventory.getModifyDatetime());

                discoveredThirdPartyInventory.setServices(serviceMapper.getServiceSummaries(distinctInventory.getInventoryId()));

                // inventory id에 해당하는 search Type 가져오기.
                List<DiscoveredThirdPartyDto> searchTypes = discoveredThirdPartyDtoList.stream()
                        .filter(p -> Objects.equals(p.getInventoryId(), distinctInventory.getInventoryId()))
                        .collect(Collectors.toList());

                searchTypes = searchTypes.stream().distinct().collect(Collectors.toList());

                // Search type에 따라서 Map으로 변환 (DiscoveryType으로 변환하기 위해서이다.)
                Map<String, List<String>> searchTypeMap = new HashMap<>();
                for (DiscoveredThirdPartyDto searchType : searchTypes) {
                    if (CollectionUtils.isEmpty(searchTypeMap.get(searchType.getSearchType()))) {
                        searchTypeMap.put(searchType.getSearchType(), Collections.singletonList(searchType.getFindContents()));
                    } else {
                        List<String> findContents = new ArrayList<>(searchTypeMap.get(searchType.getSearchType()));
                        findContents.add(searchType.getFindContents());
                        searchTypeMap.put(searchType.getSearchType(), findContents);
                    }
                }

                List<DiscoveryType> discoveryTypes = new ArrayList<>();
                searchTypeMap.forEach((strKey, strValue) -> {
                    DiscoveryType discoveryType = new DiscoveryType();
                    discoveryType.setSearchType(strKey);
                    discoveryType.setFindContents(strValue);
                    discoveryType.setDisplayOrder(getSearchTypeOrder(strKey));
                    discoveryTypes.add(discoveryType);
                });

                discoveredThirdPartyInventory.setDiscoveryTypes(discoveryTypes.stream()
                        .sorted(Comparator.comparing(DiscoveryType::getDisplayOrder))
                        .collect(Collectors.toList()));
                discoveredThirdPartyInventories.add(discoveredThirdPartyInventory);
            }

            discoveredThirdPartyInventories.sort(Comparator.comparing(DiscoveredThirdPartyInventory::getDiscoveredDatetime).reversed());

            discoveredThirdPartyResponse.setThirdPartySolutionName(discoveredThirdPartyDto.getThirdPartySolutionName());
            discoveredThirdPartyResponse.setDiscoveredThirdSolutionCount(distinctByInventoryIds.size());
            discoveredThirdPartyResponse.setDiscoveredThirdPartyInventories(discoveredThirdPartyInventories);
            discoveredThirdPartyResponses.add(discoveredThirdPartyResponse);
        }

        return discoveredThirdPartyResponses;
    }

    public List<DiscoveredThirdPartyResponse> getServerThirdParties(Long projectId, Long serverId) {
        List<DiscoveredThirdPartyResponse> discoveredThirdPartyResponses = new ArrayList<>();

        List<DiscoveredThirdPartyDto> discoveredThirdPartyDtos =
                thirdPartyMapper.selectServerDiscoveredThirdParty(projectId, serverId);

        for (DiscoveredThirdPartyDto discoveredThirdPartyDto : discoveredThirdPartyDtos) {
            DiscoveredThirdPartyResponse discoveredThirdPartyResponse = new DiscoveredThirdPartyResponse();

            List<DiscoveredThirdPartyDto> discoveredThirdPartyDtoList =
                    thirdPartyMapper.selectServerThirdParties(projectId, serverId, discoveredThirdPartyDto.getThirdPartySolutionId());

            // 해당 솔루션에 해당하는 Inventory 중복을 제거한다.
            List<DiscoveredThirdPartyDto> distinctByInventoryIds = discoveredThirdPartyDtoList.stream()
                    .filter(distinctByKey(p -> String.valueOf(p.getInventoryId())))
                    .collect(Collectors.toList());

            // Inventory 정보를 Setting한다.
            List<DiscoveredThirdPartyInventory> discoveredThirdPartyInventories = new ArrayList<>();
            for (DiscoveredThirdPartyDto distinctInventory : distinctByInventoryIds) {
                DiscoveredThirdPartyInventory discoveredThirdPartyInventory = modelMapper.map(distinctInventory, DiscoveredThirdPartyInventory.class);
                discoveredThirdPartyInventory.setDiscoveredDatetime(distinctInventory.getModifyDatetime());

                discoveredThirdPartyInventory.setServices(serviceMapper.getServiceSummaries(distinctInventory.getInventoryId()));

                // inventory id에 해당하는 search Type 가져오기.
                List<DiscoveredThirdPartyDto> searchTypes = discoveredThirdPartyDtoList.stream()
                        .filter(p -> Objects.equals(p.getInventoryId(), distinctInventory.getInventoryId()))
                        .collect(Collectors.toList());

                // Search type에 따라서 Map으로 변환 (DiscoveryType으로 변환하기 위해서이다.)
                Map<String, List<String>> searchTypeMap = new HashMap<>();
                for (DiscoveredThirdPartyDto searchType : searchTypes) {
                    if (CollectionUtils.isEmpty(searchTypeMap.get(searchType.getSearchType()))) {
                        searchTypeMap.put(searchType.getSearchType(), Collections.singletonList(searchType.getFindContents()));
                    } else {
                        List<String> findContents = new ArrayList<>(searchTypeMap.get(searchType.getSearchType()));
                        findContents.add(searchType.getFindContents());
                        searchTypeMap.put(searchType.getSearchType(), findContents);
                    }
                }

                List<DiscoveryType> discoveryTypes = new ArrayList<>();
                searchTypeMap.forEach((strKey, strValue) -> {
                    DiscoveryType discoveryType = new DiscoveryType();
                    discoveryType.setSearchType(strKey);
                    discoveryType.setFindContents(strValue);
                    discoveryType.setDisplayOrder(getSearchTypeOrder(strKey));
                    discoveryTypes.add(discoveryType);
                });

                discoveredThirdPartyInventory.setDiscoveryTypes(discoveryTypes.stream()
                        .sorted(Comparator.comparing(DiscoveryType::getDisplayOrder))
                        .collect(Collectors.toList()));
                discoveredThirdPartyInventories.add(discoveredThirdPartyInventory);
            }

            discoveredThirdPartyResponse.setThirdPartySolutionName(discoveredThirdPartyDto.getThirdPartySolutionName());
            discoveredThirdPartyResponse.setDiscoveredThirdSolutionCount(distinctByInventoryIds.size());
            discoveredThirdPartyResponse.setDiscoveredThirdPartyInventories(discoveredThirdPartyInventories);
            discoveredThirdPartyResponses.add(discoveredThirdPartyResponse);
        }

        return discoveredThirdPartyResponses;
    }

    public ByteArrayOutputStream excelExport(Long projectId, String serviceIds, Long serverId) {
        List<DiscoveredThirdPartyResponse> discoveredThirdPartyResponses;

        if (serverId == null) {
            discoveredThirdPartyResponses = getProjectThirdParties(projectId, serviceIds);
        } else {
            discoveredThirdPartyResponses = getServerThirdParties(projectId, serverId);
        }

        return discoveredThirdPartyExcelExporter.createExcelReport(discoveredThirdPartyResponses);
    }

    private List<Long> convertServiceId(String serviceIds) {
        if (StringUtils.isEmpty(serviceIds)) {
            return null;
        }

        List<Long> serviceIdList = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(serviceIds, ",");
        while (st.hasMoreTokens()) {
            serviceIdList.add(Long.parseLong(st.nextToken()));
        }

        return serviceIdList;
    }

    private int getSearchTypeOrder(String searchType) {
        if (searchType.equals("PROCESS")) {
            return 1;
        } else if (searchType.equals("RUNUSER")) {
            return 2;
        } else if (searchType.equals("PKG")) {
            return 3;
        } else if (searchType.equals("SVC")) {
            return 4;
        } else if (searchType.equals("CMD")) {
            return 5;
        } else if (searchType.equals("PORT")) {
            return 6;
        } else if (searchType.equals("SCHEDULE")) {
            return 7;
        } else {
            return 0;
        }
    }

}