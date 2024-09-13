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

package io.playce.roro.k8s.config;

import io.playce.roro.k8s.command.enums.COMMAND_KEY;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "k8s")
@Setter @Getter
public class K8sConfig {
    private Scheduler scheduler;
    private K8sThread thread;
    private List<COMMAND_KEY> processCommand;
    private Map<COMMAND_KEY, List<Command>> commandMap;

    @Setter @Getter
    public static class Scheduler {
        private boolean enable;
        private int initDelay;
        private int interval;
    }
    @Setter @Getter
    public static class K8sThread {
        private int corePoolSize;
        private int maxPoolSize;
        private int queueCapacity;
        private String threadNamePrefix;
    }
}