/*
 * Copyright 2023 The playce-roro-k8s-assessment Project.
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
 * Dong-Heon Han    Jul 13, 2023		First Draft.
 */

package io.playce.roro.k8s.core.impl;

import io.playce.roro.common.util.K8SCommandUtil;
import io.playce.roro.k8s.core.CommandRunner;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ToString
public class LocalRunner implements CommandRunner {
    @Override
    public String run(String command, String configPath) {
        String result;
        try {
            command = command.replaceAll("KUBECONFIG", configPath);
            result = K8SCommandUtil.execute(command);
        } catch (Exception e) {
            log.error("command: {}, error: {}", command, e.getMessage());
            return null;
        }
        return result;
    }
}