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

package io.playce.roro.api.domain.inventory.controller;

import io.playce.roro.api.domain.inventory.service.UserNetworkFilterService;
import io.playce.roro.common.dto.inventory.user.networkfilter.UserNetworkFilterDto;
import io.playce.roro.common.dto.inventory.user.networkfilter.UserNetworkFilterKey;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/projects/{projectId}/inventory/topology/user/networkfilters/{type}/{typeId}", produces = APPLICATION_JSON_VALUE)
@Slf4j
public class UserNetworkFilterController {
    private final UserNetworkFilterService service;

    @Operation(summary = "사용자 네트워크 필터 목록 조회", description = "사용자 네트워크 필터 목록을 조회한다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserNetworkFilterDto> getList(@PathVariable Long projectId, @PathVariable String type, @PathVariable Long typeId) {
        return service.getList(projectId, type, typeId);
    }

    @Operation(summary = "사용자 네트워크 필터 저장", description = "사용자 네트워크 필터를 저장한다.")
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public UserNetworkFilterKey add(@PathVariable Long projectId, @PathVariable String type, @PathVariable Long typeId, @RequestBody UserNetworkFilterDto userNetworkFilterDto) {
        return service.save(type, typeId, userNetworkFilterDto);
    }
}