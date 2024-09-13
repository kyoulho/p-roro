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
 * Dong-Heon Han    Jul 19, 2023		First Draft.
 */

package io.playce.roro.k8s.scheduler;

import io.playce.roro.common.dto.k8s.CommandProcessorRequest;
import io.playce.roro.k8s.core.CommandProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
public class K8sScanManager {
    private final BlockingQueue<CommandProcessorRequest> k8sQueue;
    private final CommandProcessor commandProcessor;

    @Async("k8sTaskExecutor")
    public Future<Void> run() throws InterruptedException {
        CommandProcessorRequest request = k8sQueue.take();
        log.debug("dequeue clusterId: {}, runner: {}", request.getK8sClusterId(), request.getRunnerName());

        commandProcessor.process(request);
        return null;
    }
}