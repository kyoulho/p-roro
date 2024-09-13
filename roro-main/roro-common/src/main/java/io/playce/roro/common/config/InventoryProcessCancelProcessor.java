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
 * Dong-Heon Han    May 17, 2022		First Draft.
 */

package io.playce.roro.common.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component
@Getter
@Transactional
@Slf4j
public class InventoryProcessCancelProcessor {
    public final Object lock = new Object();
    private final Map<String, Future<Void>> futureMap = new ConcurrentHashMap<>();
    private final Map<String, Boolean> cancelMap = new ConcurrentHashMap<>();

    public void removeJobs() {
        synchronized (lock) {
            List<String> removeKeys = new ArrayList<>();
            futureMap.forEach((k, f) -> {
                if(f.isDone() || f.isCancelled()) {
                    log.debug("key: {}, done: {}, cancel: {}", k, f.isDone(), f.isCancelled());
                    removeKeys.add(k);
                }
            });

            if(!removeKeys.isEmpty()) {
                removeKeys.forEach(key -> {
                    futureMap.remove(key);
                    cancelMap.remove(key);
                });
                log.debug("future map size: {}", futureMap.size());
            }
        }
    }

    public boolean jobCancel(String key) {
        if(futureMap.containsKey(key)) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ignored) {}
            Future<Void> future = futureMap.get(key);
            boolean cancel;
            synchronized (lock) {
                cancel = future.cancel(true);
                cancelMap.put(key, true);
            }
            log.debug("Cancel Job: {}, cancel: {}", key, cancel);

            return cancel;
        }
        return false;
    }

    public void addJob(String key, Future<Void> future) {
        synchronized (lock) {
            futureMap.put(key, future);
            cancelMap.put(key, false);
        }
    }
}
