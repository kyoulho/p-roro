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
 * SangCheon Park   May 24, 2022		    First Draft.
 */
package io.playce.roro.common.cancel;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class InventoryProcessCancelInfo {

    private static ConcurrentMap<Long, Object> cancelMap = ExpiringMap.builder()
            .maxSize(1000)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .expiration(60, TimeUnit.MINUTES)
            .build();

    public static Boolean hasCancelRequest(Long inventoryProcessId) {
        return cancelMap.containsKey(inventoryProcessId);
    }

    public static void addCancelRequest(Long inventoryProcessId, Object obj) {
        cancelMap.put(inventoryProcessId, obj);
    }

    public static void removeCancelRequest(Long inventoryProcessId) {
        cancelMap.remove(inventoryProcessId);
    }
}