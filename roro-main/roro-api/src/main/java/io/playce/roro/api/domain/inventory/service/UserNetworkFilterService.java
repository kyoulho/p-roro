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
 * Dong-Heon Han    Mar 11, 2022		First Draft.
 */

package io.playce.roro.api.domain.inventory.service;

import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.common.dto.inventory.user.networkfilter.UserNetworkFilterDto;
import io.playce.roro.common.dto.inventory.user.networkfilter.UserNetworkFilterKey;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.jpa.entity.UserNetworkFilter;
import io.playce.roro.jpa.entity.pk.UserNetworkFilterId;
import io.playce.roro.jpa.repository.UserNetworkFilterRepository;
import io.playce.roro.mybatis.domain.inventory.network.UserNetworkFilterMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Service
@RequiredArgsConstructor
public class UserNetworkFilterService {
    private final UserNetworkFilterMapper mapper;
    private final UserNetworkFilterRepository userNetworkFilterRepository;

    public List<UserNetworkFilterDto> getList(Long projectId, String type, Long typeId) {
        return mapper.selectList(projectId, type, typeId, WebUtil.getUserId());
    }

    public UserNetworkFilterKey save(String type, Long typeId, UserNetworkFilterDto userNetworkFilterDto) {
        UserNetworkFilterId id = getUserNetworkFilterId(type, typeId);
        UserNetworkFilter userNetworkFilter = getUserNetworkFilter(userNetworkFilterDto, id);

        userNetworkFilterRepository.save(userNetworkFilter);
        return getUserNetworkFilterKey(userNetworkFilter);
    }

    @NotNull
    private UserNetworkFilterKey getUserNetworkFilterKey(UserNetworkFilter userNetworkFilter) {
        UserNetworkFilterKey key = new UserNetworkFilterKey();
        UserNetworkFilterId id = userNetworkFilter.getId();
        key.setResourceTypeCode(id.getResourceTypeCode());
        key.setResourceId(id.getResourceId());
        key.setResourceId(userNetworkFilter.getNetworkFilterId());
        return key;
    }

    @NotNull
    private UserNetworkFilter getUserNetworkFilter(UserNetworkFilterDto userNetworkFilterDto, UserNetworkFilterId id) {
        UserNetworkFilter userNetworkFilter = new UserNetworkFilter();
        userNetworkFilter.setId(id);
        userNetworkFilter.setWhitelist(JsonUtil.writeValueAsString(userNetworkFilterDto.getWhitelist()));
        userNetworkFilter.setBlacklist(JsonUtil.writeValueAsString(userNetworkFilterDto.getBlacklist()));
        userNetworkFilter.setHideNodes(JsonUtil.writeValueAsString(userNetworkFilterDto.getHideNodes()));
        userNetworkFilter.setNetworkFilterId(userNetworkFilterDto.getNetworkFilterId());
        return userNetworkFilter;
    }

    @NotNull
    private UserNetworkFilterId getUserNetworkFilterId(String type, Long typeId) {
        UserNetworkFilterId id = new UserNetworkFilterId();
        id.setUserId(WebUtil.getUserId());
        id.setResourceTypeCode(type);
        id.setResourceId(typeId);
        return id;
    }

    public void delete(String type, Long typeId, Long networkFilterId) {
        UserNetworkFilterId id = getUserNetworkFilterId(type, typeId);
        userNetworkFilterRepository.deleteById(id);
    }
}