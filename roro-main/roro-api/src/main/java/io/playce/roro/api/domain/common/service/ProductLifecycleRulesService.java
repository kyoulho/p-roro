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
package io.playce.roro.api.domain.common.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.domain.common.aop.SubscriptionManager;
import io.playce.roro.common.dto.productlifecycle.ProductLifecycleRulesResponse;
import io.playce.roro.common.dto.subscription.Subscription;
import io.playce.roro.common.dto.subscription.SubscriptionStausType;
import io.playce.roro.common.dto.subscription.SubscriptionType;
import io.playce.roro.mybatis.domain.insights.InsightMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductLifecycleRulesService {
    private final InsightMapper insightMapper;

    public List<ProductLifecycleRulesResponse> getProductLifecycleRulesAndVersions() {
        checkSubscription();
        return insightMapper.selectProductLifecycleRulesAndVersions();
    }

    public ProductLifecycleRulesResponse getProductLifecycleRuleAndVersion(Long productLifecycleRules) {
        checkSubscription();
        return insightMapper.selectProductLifecycleRuleAndVersion(productLifecycleRules);
    }

    private void checkSubscription() {
        Subscription subscription = SubscriptionManager.getSubscription();
        if (subscription.getType().equals(SubscriptionType.TRIAL) || !subscription.getSubscriptionStausType().equals(SubscriptionStausType.SUBSCRIPTION_VALID)) {
            throw new RoRoApiException(ErrorCode.SUBSCRIPTION_NOT_ALLOWED2);
        }
    }
}
