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

package io.playce.roro.k8s.handler;

import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.jpa.entity.k8s.*;
import io.playce.roro.jpa.entity.pk.NamespaceObjectLinkId;
import io.playce.roro.jpa.repository.k8s.NamespaceObjectLinkRepository;
import io.playce.roro.jpa.repository.k8s.*;
import io.playce.roro.k8s.enums.Base;
import io.playce.roro.k8s.enums.Dict;
import io.playce.roro.k8s.enums.Link;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommonHandler  {
    private final K8sObjectRepository k8sObjectRepository;
    private final ObjectOriginJsonRepository objectOriginJsonRepository;
    private final ObjectLabelRepository objectLabelRepository;
    private final ObjectAnnotationRepository objectAnnotationRepository;
    private final NamespaceObjectLinkRepository namespaceObjectLinkRepository;
    private final NamespaceRepository namespaceRepository;
    private final NodeRepository nodeRepository;

    public K8sObject saveObject(Long clusterScanId, JsonNode node) {
        Map<Base, String> valueMap = extractObject(node);
        String uid = valueMap.get(Base.uid);
        K8sObject k8sObject = k8sObjectRepository.findByClusterScanIdAndUid(clusterScanId, uid).orElse(new K8sObject());
        k8sObject.setClusterScanId(clusterScanId);
        k8sObject.setKind(valueMap.get(Base.kind));
        k8sObject.setName(valueMap.get(Base.name));
        k8sObject.setUid(uid);
        return k8sObjectRepository.save(k8sObject);
    }

    private static Map<Base, String> extractObject(JsonNode node) {
        Map<Base, String> valueMap = new HashMap<>();
        for(Base object: Base.values()) {
            JsonNode at = node.at(object.getPath());
            String value = at.textValue();
            valueMap.put(object, value);
        }
        return valueMap;
    }


    public void saveObjectOriginJson(String objectOriginJson, Long k8sObjectId) {
        ObjectOriginJson objectOriginJsonEntity = objectOriginJsonRepository.findById(k8sObjectId).orElse(new ObjectOriginJson());
        objectOriginJsonEntity.setObjectId(k8sObjectId);
        objectOriginJsonEntity.setJson(objectOriginJson);
        objectOriginJsonRepository.save(objectOriginJsonEntity);
    }

    /**
     * set object, object_annotation, object_label, namespace_object_link
     */
    public void setObjectInfoWithNamespaceLink(Long clusterScanId, JsonNode resultData) {
        JsonNode items = resultData.get("items");
        for(JsonNode element: items) {
            setObjectInfo(clusterScanId, element);
        }
    }

    public Long setObjectInfo(Long clusterScanId, JsonNode element) {
        K8sObject k8sObject = saveObject(clusterScanId, element);
        Long objectId = k8sObject.getObjectId();
        saveObjectOriginJson(element.toString(), objectId);
        saveObjectAnnotations(element.at(Dict.annotations.getPath()), objectId);
        saveObjectLabels(element.at(Dict.labels.getPath()), objectId);
        String namespaceName = element.at(Link.namespace.getPath()).textValue();
        Namespace namespace = namespaceRepository.findByClusterScanIdAndName(clusterScanId, namespaceName).orElse(new Namespace());
        Long namespaceId = namespace.getNamespaceId();
        if(namespaceId != null) {
            connectNamespace(namespaceId, objectId);
        }
        return objectId;
    }

    public void saveObjectAnnotations(JsonNode nodes, Long k8sObjectId) {
        Map<String, String> result = getNameValues(nodes);
        objectAnnotationRepository.deleteByObjectId(k8sObjectId);
        for(String name: result.keySet()) {
            String value = result.get(name);
            ObjectAnnotation objectAnnotation = new ObjectAnnotation();
            objectAnnotation.setObjectId(k8sObjectId);
            objectAnnotation.setName(name);
            objectAnnotation.setValue(value);
            objectAnnotationRepository.save(objectAnnotation);
        }
    }

    public Map<String, String> getNameValues(JsonNode nodes) {
        Map<String, String> result = new HashMap<>();
        Iterator<String> fields = nodes.fieldNames();
        while(fields.hasNext()) {
            String name = fields.next();
            JsonNode node = nodes.get(name);
            String value = node.textValue();
            result.put(name, value);
        }
        return result;
    }

    public void saveObjectLabels(JsonNode nodes, Long k8sObjectId) {
        Map<String, String> result = getNameValues(nodes);
        objectLabelRepository.deleteByObjectId(k8sObjectId);
        for(String name: result.keySet()) {
            String value = result.get(name);
            ObjectLabel objectLabel = new ObjectLabel();
            objectLabel.setObjectId(k8sObjectId);
            objectLabel.setName(name);
            objectLabel.setValue(value);
            objectLabelRepository.save(objectLabel);
        }
    }

    public void connectNamespace(Long namespaceId, Long objectId) {
        NamespaceObjectLinkId id = getNamespaceObjectLinkId(namespaceId, objectId);
        NamespaceObjectLink namespaceObjectLink = getNamespaceObjectLink(id);
        namespaceObjectLinkRepository.save(namespaceObjectLink);
    }

    @NotNull
    private static NamespaceObjectLink getNamespaceObjectLink(NamespaceObjectLinkId id) {
        NamespaceObjectLink namespaceObjectLink = new NamespaceObjectLink();
        namespaceObjectLink.setNamespaceObjectLinkId(id);
        return namespaceObjectLink;
    }

    @NotNull
    private static NamespaceObjectLinkId getNamespaceObjectLinkId(Long namespaceId, Long objectId) {
        NamespaceObjectLinkId id = new NamespaceObjectLinkId();
        id.setNamespaceId(namespaceId);
        id.setObjectId(objectId);
        return id;
    }

    public Map<String, String> getRecord(JsonNode namespace, Map<String, String> jsonPaths) {
        Map<String, String> record = new HashMap<>();
        for (String key : jsonPaths.keySet()) {
            String path = jsonPaths.get(key);
            JsonNode col = namespace.at(path);
            String value = col.textValue();
            record.put(key, value);
        }
        return record;
    }

    @Nullable
    public Node getNode(Long objectId, Long clusterScanId, String nodeName) {
        Optional<Node> nodeOptional = nodeRepository.findByClusterScanIdAndName(clusterScanId, nodeName);
        if(nodeOptional.isEmpty()) {
            log.error("Node information not found. objectId: {}, clusterScanId: {}", objectId, clusterScanId);
            return null;
        }
        return nodeOptional.get();
    }

}