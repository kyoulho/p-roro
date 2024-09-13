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
 * SangCheon Park   Feb 25, 2022		    First Draft.
 */
package io.playce.roro.api.domain.network.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.dto.network.CIDRBlockInfoDto;
import io.playce.roro.common.dto.network.NetworkFilterCreateRequest;
import io.playce.roro.common.dto.network.NetworkFilterResponse;
import io.playce.roro.common.dto.network.NetworkFilterSimpleResponse;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.jpa.entity.NetworkFilterMaster;
import io.playce.roro.jpa.entity.UserAccess;
import io.playce.roro.jpa.repository.NetworkFilterMasterRepository;
import io.playce.roro.jpa.repository.UserAccessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
@Transactional
public class NetworkFiltersService {

    private final ModelMapper modelMapper = new ModelMapper();
    private final NetworkFilterMasterRepository networkFilterRepository;
    private final UserAccessRepository userAccessRepository;

    /**
     * @param projectId
     *
     * @return
     */
    public List<NetworkFilterResponse> getNetworkFilters(Long projectId) {
        List<NetworkFilterMaster> networkFilterMasterList = networkFilterRepository.findAllByProjectId(projectId);

        List<NetworkFilterResponse> networkFilterResponseList = new ArrayList<>();
        for (NetworkFilterMaster networkFilterMaster : networkFilterMasterList) {
            NetworkFilterResponse response = modelMapper.map(networkFilterMaster, NetworkFilterResponse.class);
            setResponse(networkFilterMaster, response);
            networkFilterResponseList.add(response);
        }

        return networkFilterResponseList;
    }

    /**
     * @param projectId
     * @param networkFilterId
     *
     * @return
     */
    public NetworkFilterResponse getNetworkFilter(Long projectId, Long networkFilterId) {
        NetworkFilterMaster networkFilterMaster = networkFilterRepository.findByProjectIdAndNetworkFilterId(projectId, networkFilterId)
                .orElseThrow(() -> new ResourceNotFoundException("Network filter Id: " + networkFilterId + " Not Found in Project ID : " + projectId));

        NetworkFilterResponse networkFilterResponse = modelMapper.map(networkFilterMaster, NetworkFilterResponse.class);
        setResponse(networkFilterMaster, networkFilterResponse);

        return networkFilterResponse;
    }

    /**
     * @param projectId
     * @param request
     *
     * @return
     */
    public NetworkFilterSimpleResponse createNetworkFilter(Long projectId, NetworkFilterCreateRequest request) {
        List<NetworkFilterMaster> networkFilterMasterList = networkFilterRepository.findByProjectIdAndNetworkFilterName(projectId, request.getNetworkFilterName());
        if (networkFilterMasterList.size() > 0) {
            throw new RoRoApiException(ErrorCode.NETWORKFILTER_DUPLICATED_NAME);
        }

        NetworkFilterMaster networkFilterMaster = modelMapper.map(request, NetworkFilterMaster.class);
        setCIDRInfo(request, networkFilterMaster);

        networkFilterMaster.setProjectId(projectId);
        networkFilterMaster.setDeleteYn(Domain101.N.name());
        networkFilterMaster.setRegistDatetime(new Date());
        networkFilterMaster.setRegistUserId(WebUtil.getUserId());
        networkFilterMaster.setModifyDatetime(new Date());
        networkFilterMaster.setModifyUserId(WebUtil.getUserId());

        networkFilterMaster = networkFilterRepository.save(networkFilterMaster);

        NetworkFilterSimpleResponse response = new NetworkFilterSimpleResponse();
        response.setNetworkFilterId(networkFilterMaster.getNetworkFilterId());
        response.setNetworkFilterName(networkFilterMaster.getNetworkFilterName());

        return response;
    }

    /**
     * @param projectId
     * @param networkFilterId
     * @param request
     *
     * @return
     */
    public NetworkFilterSimpleResponse modifyNetworkFilter(Long projectId, Long networkFilterId, NetworkFilterCreateRequest request) {
        NetworkFilterMaster networkFilterMaster = networkFilterRepository.findByProjectIdAndNetworkFilterId(projectId, networkFilterId)
                .orElseThrow(() -> new ResourceNotFoundException("Network filter Id: " + networkFilterId + " Not Found in Project ID : " + projectId));

        List<NetworkFilterMaster> networkFilterMasterList = networkFilterRepository.findByProjectIdAndNetworkFilterIdNotAndNetworkFilterName(projectId, networkFilterId, request.getNetworkFilterName());
        if (networkFilterMasterList.size() > 0) {
            throw new RoRoApiException(ErrorCode.NETWORKFILTER_DUPLICATED_NAME);
        }

        modelMapper.map(request, networkFilterMaster);
        setCIDRInfo(request, networkFilterMaster);

        networkFilterMaster.setModifyUserId(WebUtil.getUserId());
        networkFilterMaster.setModifyDatetime(new Date());

        NetworkFilterSimpleResponse response = new NetworkFilterSimpleResponse();
        response.setNetworkFilterId(networkFilterMaster.getNetworkFilterId());
        response.setNetworkFilterName(networkFilterMaster.getNetworkFilterName());

        return response;
    }

    /**
     * @param projectId
     * @param networkFilterId
     */
    public void deleteNetworkFilter(Long projectId, Long networkFilterId) {
        NetworkFilterMaster networkFilterMaster = networkFilterRepository.findByProjectIdAndNetworkFilterId(projectId, networkFilterId)
                .orElseThrow(() -> new ResourceNotFoundException("Network filter Id: " + networkFilterId + " Not Found in Project ID : " + projectId));

        networkFilterMaster.setDeleteYn(Domain101.Y.name());
        networkFilterMaster.setModifyUserId(WebUtil.getUserId());
        networkFilterMaster.setModifyDatetime(new Date());
    }

    /**
     * @param filter
     * @param res
     */
    private void setResponse(NetworkFilterMaster filter, NetworkFilterResponse res) {
        JsonNode whiteListNode = JsonUtil.readTree(filter.getWhitelist());
        JsonNode balickListNode = JsonUtil.readTree(filter.getBlacklist());

        TypeReference<List<CIDRBlockInfoDto>> type = new TypeReference<>() {};
        res.setWhitelist(JsonUtil.treeToObj(whiteListNode, type));
        res.setBlacklist(JsonUtil.treeToObj(balickListNode, type));
        res.setWhitelistCount(whiteListNode.size());
        res.setBlacklistCount(balickListNode.size());

        // set registUserLoginId and modifyUserLoginId
        Long userId = filter.getRegistUserId();
        UserAccess userAccess = userAccessRepository.findById(userId).orElse(null);

        if (userAccess != null) {
            res.setRegistUserLoginId(userAccess.getUserLoginId());
        }

        if (userId == filter.getModifyUserId() && userAccess != null) {
            res.setModifyUserLoginId(userAccess.getUserLoginId());
        } else {
            userId = filter.getModifyUserId();
            userAccess = userAccessRepository.findById(userId).orElse(null);

            if (userAccess != null) {
                res.setModifyUserLoginId(userAccess.getUserLoginId());
            }
        }
    }

    /**
     * @param request
     * @param networkFilterMaster
     */
    private void setCIDRInfo(NetworkFilterCreateRequest request, NetworkFilterMaster networkFilterMaster) {
        try {
            networkFilterMaster.setWhitelist(JsonUtil.objToJson(request.getWhitelist()));
            networkFilterMaster.setBlacklist(JsonUtil.objToJson(request.getBlacklist()));
        } catch (IOException e) {
            log.error("Unhandled exception while parse white list and black list.", e);
            throw new IllegalArgumentException("White List or Black List is invalid.");
        }
    }
}
//end of NetworkFiltersService.java