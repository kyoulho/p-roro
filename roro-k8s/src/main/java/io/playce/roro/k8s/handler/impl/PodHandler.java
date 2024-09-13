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
import com.fasterxml.jackson.databind.node.MissingNode;
import io.playce.roro.jpa.entity.k8s.Node;
import io.playce.roro.jpa.entity.k8s.*;
import io.playce.roro.jpa.repository.k8s.*;
import io.playce.roro.k8s.handler.CommandResultHandler;
import io.playce.roro.k8s.handler.CommonHandler;
import io.playce.roro.k8s.parser.Parser;
import io.playce.roro.k8s.parser.ParserManager;
import io.playce.roro.k8s.util.K8sJsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PodHandler implements CommandResultHandler {
    private final CommonHandler commonHandler;
    private final ParserManager parserManager;

    private final PodRepository podRepository;
    private final PodVolumeRepository podVolumeRepository;
    private final ContainerRepository containerRepository;
    private final ContainerPortRepository containerPortRepository;
    private final ContainerVolumeRepository containerVolumeRepository;

    @RequiredArgsConstructor
    private enum R {
        podIP("/status/podIP"),
        nodeName("/spec/nodeName"),
        podIPs("/status/podIPs"),
        volumes("/spec/volumes"),
        containers("/spec/containers"),
        image("image"),
        name("name"),
        ports("ports"),
        volumeMounts("volumeMounts"),
        mountPath("mountPath"),
        containerPort("containerPort"),
        protocol("protocol"),
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
        for (JsonNode pod: items) {
            Long objectId = commonHandler.setObjectInfo(clusterScanId, pod);
            savePodInfo(pod, objectId, clusterScanId);
            savePodVolumeInfo(objectId, pod);
            saveContainerInfo(objectId, pod);
        }
    }

    private void saveContainerInfo(Long objectId, JsonNode pod) {
        JsonNode jsonContainers = pod.at(R.containers.path);
        if(jsonContainers instanceof MissingNode) return;

        for(JsonNode jsonContainer: jsonContainers) {
            String name = K8sJsonUtil.getStringValue(jsonContainer, R.name.path);
            Optional<Container> containerEntity = containerRepository.findByObjectIdAndName(objectId, name);
            if(containerEntity.isPresent()) {
                Long containerId = containerEntity.get().getContainerId();
                containerPortRepository.deleteByContainerId(containerId);
                containerVolumeRepository.deleteByContainerId(containerId);
            }
        }

        containerRepository.deleteByObjectId(objectId);
        for(JsonNode jsonContainer: jsonContainers) {
            Container savedContainer = saveContainer(objectId, jsonContainer);
            Long containerId = savedContainer.getContainerId();
            saveContainerVolumes(objectId, containerId, jsonContainer);
            savePorts(containerId, jsonContainer);
        }
    }

    private void savePorts(Long containerId, JsonNode jsonContainer) {
       JsonNode jsonPorts = jsonContainer.get(R.ports.path);
       if(jsonPorts == null) return;

       for(JsonNode jsonPort: jsonPorts) {
           savePort(containerId, jsonPort);
       }
    }

    private void savePort(Long containerId, JsonNode jsonPort) {
        Integer port = K8sJsonUtil.getIntValue(jsonPort, R.containerPort.path);
        String protocol = K8sJsonUtil.getStringValue(jsonPort, R.protocol.path);
        ContainerPort containerPort = new ContainerPort();
        containerPort.setContainerPort(port);
        containerPort.setProtocol(protocol);
        containerPort.setContainerId(containerId);
        containerPortRepository.save(containerPort);
    }

    private void saveContainerVolumes(Long objectId, Long containerId, JsonNode jsonContainer) {
        JsonNode jsonVolumeMounts = jsonContainer.get(R.volumeMounts.path);
        if(jsonVolumeMounts == null) return;

        for(JsonNode jsonVolume: jsonVolumeMounts) {
            String name = K8sJsonUtil.getStringValue(jsonVolume, R.name.path);
            Optional<PodVolume> optionalPodVolume = podVolumeRepository.findByObjectIdAndName(objectId, name);
            Long podVolumeId = optionalPodVolume.map(PodVolume::getPodVolumeId).orElse(null);
            saveContainerVolume(containerId, jsonVolume, name, podVolumeId);
        }
    }

    private void saveContainerVolume(Long containerId, JsonNode jsonVolume, String name, Long podVolumeId) {
        String mountPath = K8sJsonUtil.getStringValue(jsonVolume, R.mountPath.path);
        ContainerVolume containerVolume = new ContainerVolume();
        containerVolume.setMountPath(mountPath);
        containerVolume.setName(name);
        containerVolume.setContainerId(containerId);
        containerVolume.setPodVolumeId(podVolumeId);
        containerVolumeRepository.save(containerVolume);
    }

    private Container saveContainer(Long objectId, JsonNode jsonContainer) {
        String name = K8sJsonUtil.getStringValue(jsonContainer, R.name.path);
        String image = K8sJsonUtil.getStringValue(jsonContainer, R.image.path);
        Container container = new Container();
        container.setImage(image);
        container.setName(name);
        container.setObjectId(objectId);
        return containerRepository.save(container);
    }

    private void savePodVolumeInfo(Long objectId, JsonNode jsonPod) {
        JsonNode jsonVolumes = jsonPod.at(R.volumes.path);
        if(jsonVolumes instanceof MissingNode) return;

        podVolumeRepository.deleteByObjectId(objectId);
        for(JsonNode jsonVolume: jsonVolumes) {
            savePodVolume(objectId, jsonVolume);
        }
    }

    private void savePodVolume(Long objectId, JsonNode jsonVolume) {
        PodVolume podVolume = new PodVolume();
        String name = K8sJsonUtil.getStringValue(jsonVolume, R.name.path);
        podVolume.setName(name);
        podVolume.setObjectId(objectId);
        podVolumeRepository.save(podVolume);
    }

    private void savePodInfo(JsonNode pod, Long objectId, Long clusterScanId) {
        Map<String, String> podRecord = commonHandler.getRecord(pod, Map.of(
                R.podIP.name(), R.podIP.path,
                R.nodeName.name(),R.nodeName.path
        ));
        Node node = commonHandler.getNode(objectId, clusterScanId, podRecord.get(R.nodeName.name()));
        if (node == null) return;

        savePod(objectId, podRecord, node.getNodeId());
    }

    private void savePod(Long objectId, Map<String, String> podRecord, Long nodeId) {
        Pod pod = podRepository.findById(objectId).orElse(new Pod());
        pod.setObjectId(objectId);
        pod.setClusterIp(podRecord.get(R.podIP.name()));
        pod.setNodeId(nodeId);
        podRepository.save(pod);
    }

    @Override
    public void setRelation(JsonNode resultData) {
        //todo
    }
}