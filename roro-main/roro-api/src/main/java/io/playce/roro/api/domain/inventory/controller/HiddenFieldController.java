/*
 * Copyright 2023 The playce-roro Project.
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
 * Dong-Heon Han    Jun 02, 2023		First Draft.
 */

package io.playce.roro.api.domain.inventory.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.api.domain.inventory.service.HiddenFieldService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/inventory")
public class HiddenFieldController {
    private final HiddenFieldService service;

    @GetMapping("/hidden-fields")
    @Operation(summary = "숨김필드 목록조회", description = "인베토리 등록시 숨길필드 목록을 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    public JsonNode getHiddenFields(@PathVariable Long projectId, @RequestParam String inventoryTypeCode) {
        return service.getHiddenFields(projectId, inventoryTypeCode);
    }

    @PostMapping("/hidden-fields")
    @Operation(summary = "숨김필드 목록 저장", description = "인베토리 등록시 숨길필드 목록을 저장한다.")
    @ResponseStatus(HttpStatus.OK)
    public void saveHiddenFields(@PathVariable Long projectId, @RequestParam String inventoryTypeCode,
                                     @RequestBody JsonNode hiddenFields) {
        service.saveHiddenFields(projectId, inventoryTypeCode, hiddenFields);
    }
}