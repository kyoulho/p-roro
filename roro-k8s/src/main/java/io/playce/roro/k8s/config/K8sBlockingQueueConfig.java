/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * Dong-Heon Han    Dec 09, 2021		First Draft.
 */

package io.playce.roro.k8s.config;

import io.playce.roro.common.dto.k8s.CommandProcessorRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Configuration
@RequiredArgsConstructor
@Setter @Getter
public class K8sBlockingQueueConfig {
    private final K8sConfig k8sConfig;

    @Bean
    public BlockingQueue<CommandProcessorRequest> k8sQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Bean
    public Executor k8sTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        K8sConfig.K8sThread thread = k8sConfig.getThread();
        executor.setCorePoolSize(thread.getCorePoolSize());
        executor.setMaxPoolSize(thread.getMaxPoolSize());
        executor.setQueueCapacity(thread.getQueueCapacity());
        executor.setThreadNamePrefix(thread.getThreadNamePrefix());
        executor.initialize();
        return executor;
    }
}