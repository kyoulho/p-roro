/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       11월 04, 2021            First Draft.
 */
package io.playce.roro.api.domain.common.controller;

import io.playce.roro.api.domain.common.service.CodeService;
import io.playce.roro.api.domain.common.service.LabelService;
import io.playce.roro.api.domain.common.service.ProductLifecycleRulesService;
import io.playce.roro.api.domain.common.service.UserService;
import io.playce.roro.api.domain.inventory.service.ServerService;
import io.playce.roro.common.dto.common.User;
import io.playce.roro.common.dto.common.code.CodeDomain;
import io.playce.roro.common.dto.common.label.Label;
import io.playce.roro.common.dto.productlifecycle.ProductLifecycleRulesResponse;
import io.playce.roro.common.dto.subscription.Subscription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@RestController
@RequestMapping(value = "/api/common")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class CommonController {

    private final UserService userService;
    private final CodeService codeService;
    private final LabelService labelService;
    private final ServerService serverService;

    private final ProductLifecycleRulesService productLifecycleRulesService;

    @Operation(summary = "Subscription 조회", description = "Subscription 정보를 조회한다.")
    @GetMapping("subscription")
    public ResponseEntity<?> getSubscription() {
        Subscription subscription = serverService.getSubscriptionWithUsedCount();
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("codes")
    @ResponseStatus(HttpStatus.OK)
    public List<CodeDomain> getCodes(@RequestParam(value = "keyword", required = false) String keyword) {
        return codeService.getCodeDomains(keyword);
    }

    @PatchMapping("user/password")
    @ResponseStatus(HttpStatus.OK)
    public void setPassword(@RequestBody User.PasswordChangeRequest passwordChangeRequest) {
        userService.updatePassword(passwordChangeRequest);
    }

    @GetMapping("users")
    //TODO change return type
    public void getUsers(@RequestParam(value = "keyword", required = false) String keyword) {
    }

    @GetMapping("labels")
    @ResponseStatus(HttpStatus.OK)
    public List<Label.LabelDetailResponse> getLabels(@RequestParam(value = "keyword", required = false) String keyword) {
        return labelService.getLabelList(keyword);
    }

    @PostMapping("labels")
    @ResponseStatus(HttpStatus.CREATED)
    public Label.LabelResponse setLabel(@RequestBody Label.LabelRequest label) {
        return labelService.createLabel(label);
    }

    @GetMapping("product-lifecycle-rules")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductLifecycleRulesResponse> getProjectLifecycleRules() {
            return productLifecycleRulesService.getProductLifecycleRulesAndVersions();
    }

    @GetMapping("product-lifecycle-rules/{productLifecycleRulesId}")
    @ResponseStatus(HttpStatus.OK)
    public ProductLifecycleRulesResponse getProjectLifecycleRule(@PathVariable Long productLifecycleRulesId) {
        return productLifecycleRulesService.getProductLifecycleRuleAndVersion(productLifecycleRulesId);
    }
}
//end of CommonController.java
