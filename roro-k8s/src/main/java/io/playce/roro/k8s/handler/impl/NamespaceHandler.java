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

package io.playce.roro.k8s.handler.impl;

import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.jpa.entity.k8s.Namespace;
import io.playce.roro.jpa.repository.k8s.NamespaceRepository;
import io.playce.roro.k8s.handler.CommandResultHandler;
import io.playce.roro.k8s.handler.CommonHandler;
import io.playce.roro.k8s.parser.Parser;
import io.playce.roro.k8s.parser.ParserManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NamespaceHandler implements CommandResultHandler {
    private final CommonHandler commonHandler;
    private final NamespaceRepository namespaceRepository;
    private final ParserManager parserManager;

    @RequiredArgsConstructor
    private enum R {
        kind("/kind"),
        name("/metadata/name"),
        uid("/metadata/uid"),
        ;
        private final String path;
    }

    @Override
    public JsonNode parse(String result, String parserName) {
        Parser parser = parserManager.getParser(parserName);
        return parser.parse(result);
    }

    @Override
    public void saveData(Long clusterScanId, JsonNode resultData) {
        JsonNode items = resultData.get("items");
        for (int i=0; i < items.size(); i++) {
            JsonNode namespace = items.get(i);
            Map<String, String> record = commonHandler.getRecord(namespace, Map.of(
                    R.kind.name(), R.kind.path,
                    R.name.name(), R.name.path,
                    R.uid.name(), R.uid.path
            ));
            saveNamespace(clusterScanId, record.get("name"));
        }
    }

    private void saveNamespace(Long clusterScanId, String name) {
        Namespace namespace = namespaceRepository.findByClusterScanIdAndName(clusterScanId, name).orElse(new Namespace());
        namespace.setName(name);
        namespace.setClusterScanId(clusterScanId);
        namespaceRepository.save(namespace);
    }

    @Override
    public void setRelation(JsonNode resultData) {
    }
}