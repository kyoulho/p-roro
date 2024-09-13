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
 * Dong-Heon Han    Jul 20, 2023		First Draft.
 */

package io.playce.roro.k8s.handler.impl;

import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.jpa.entity.k8s.Node;
import io.playce.roro.jpa.entity.k8s.NodeAnnotation;
import io.playce.roro.jpa.entity.k8s.NodeLabel;
import io.playce.roro.jpa.repository.k8s.NodeAnnotationRepository;
import io.playce.roro.jpa.repository.k8s.NodeLabelRepository;
import io.playce.roro.jpa.repository.k8s.NodeRepository;
import io.playce.roro.k8s.enums.Base;
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
public class NodeHandler implements CommandResultHandler {
    private final NodeRepository nodeRepository;
    private final NodeAnnotationRepository nodeAnnotationRepository;
    private final NodeLabelRepository nodeLabelRepository;
    private final CommonHandler commonHandler;
    private final ParserManager parserManager;

    @RequiredArgsConstructor
    private enum R {
        kubeletVersion("/status/nodeInfo/kubeletVersion"),
        addresses("/status/addresses"),
        type("/type"),
        address("/address"),
        annotations("/metadata/annotations"),
        labels("/metadata/labels"),
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
        for (JsonNode item : items) {
            Node node = saveNodes(clusterScanId, item);
            saveNodeAnnotations(item.at(R.annotations.path), node);
            saveNodeLabels(item.at(R.labels.path), node);
        }
    }

    private void saveNodeLabels(JsonNode labels, Node node) {
        Map<String, String> nameValues = commonHandler.getNameValues(labels);
        for (String name : nameValues.keySet()) {
            NodeLabel nodeLabel = new NodeLabel();
            nodeLabel.setNodeId(node.getNodeId());
            nodeLabel.setName(name);
            nodeLabel.setValue(nameValues.get(name));
            nodeLabelRepository.save(nodeLabel);
        }
    }

    private void saveNodeAnnotations(JsonNode annotations, Node node) {
        Map<String, String> nameValues = commonHandler.getNameValues(annotations);
        for (String name : nameValues.keySet()) {
            NodeAnnotation nodeAnnotation = new NodeAnnotation();
            nodeAnnotation.setNodeId(node.getNodeId());
            nodeAnnotation.setName(name);
            nodeAnnotation.setValue(nameValues.get(name));
            nodeAnnotationRepository.save(nodeAnnotation);
        }
    }

    private Node saveNodes(Long clusterScanId, JsonNode item) {
        String name = item.at(Base.name.getPath()).textValue();
        return nodeRepository.findByClusterScanIdAndName(clusterScanId, name)
                .orElseGet(() -> {
                    Node newNode = new Node();
                    newNode.setName(name);
                    newNode.setClusterScanId(clusterScanId);
                    newNode.setKubeletVersion(item.at(R.kubeletVersion.path).textValue());
                    JsonNode addresses = item.at(R.addresses.path);
                    for (JsonNode address : addresses) {
                        var type = address.at(R.type.path).textValue();
                        if ("InternalIP".equals(type)) {
                            newNode.setInternalIp(address.at(R.address.path).textValue());
                        } else if ("ExternalIP".equals(type)) {
                            newNode.setExternalIp(address.at(R.addresses.path).textValue());
                        }
                    }
                    return nodeRepository.save(newNode);
                });
    }


    @Override
    public void setRelation(JsonNode resultData) {

    }
}