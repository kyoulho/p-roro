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
 * Dong-Heon Han    Jul 27, 2023		First Draft.
 */

package io.playce.roro.k8s.handler.impl;

import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.jpa.entity.ClusterScan;
import io.playce.roro.jpa.repository.k8s.ClusterScanRepository;
import io.playce.roro.k8s.handler.CommandResultHandler;
import io.playce.roro.k8s.parser.Parser;
import io.playce.roro.k8s.parser.ParserManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VersionHandler implements CommandResultHandler {
    private final ParserManager parserManager;
    private final ClusterScanRepository clusterScanRepository;

    @Override
    public JsonNode parse(String result, String parserName) {
        Parser parser = parserManager.getParser(parserName);
        return parser.parse(result);
    }

    private void saveVersion(Long clusterScanId, String serverVersion) {
        Optional<ClusterScan> clusterScanOptional = clusterScanRepository.findById(clusterScanId);
        clusterScanOptional.ifPresent(clusterScan -> {
            clusterScan.setServerVersion(serverVersion);
            clusterScanRepository.save(clusterScan);
        });
    }

    @Override
    public void saveData(Long clusterScanId, JsonNode resultData) {
        log.info("{}", resultData);
        for (JsonNode jsonObject : resultData) {
            JsonNode serverNode = jsonObject.get("Server Version");
            if (serverNode != null) {
                saveVersion(clusterScanId, serverNode.asText());
                break;
            }
        }
    }

    @Override
    public void setRelation(JsonNode resultData) {

    }
}