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
 * Dong-Heon Han    Apr 05, 2023		First Draft.
 */

package io.playce.roro.history.event;

import io.playce.roro.common.dto.history.SubscriptionCount;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Builder @Getter
public class SubscriptionCountEvent {
    private Instant eventTime;
    private List<SubscriptionCount> list;
}