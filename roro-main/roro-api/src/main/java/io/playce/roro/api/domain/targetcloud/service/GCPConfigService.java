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
 * SangCheon Park   Feb 10, 2022		    First Draft.
 */
package io.playce.roro.api.domain.targetcloud.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.compute.model.MachineImage;
import com.google.api.services.compute.model.MachineType;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.common.code.Domain1009;
import io.playce.roro.jpa.entity.CredentialMaster;
import io.playce.roro.jpa.repository.CredentialMasterRepository;
import io.playce.roro.mig.gcp.auth.GCPCredentials;
import io.playce.roro.mig.gcp.compute.ComputeClient;
import io.playce.roro.mig.gcp.enums.network.SubnetConfig;
import io.playce.roro.mig.gcp.model.GCPConfigDto;
import io.playce.roro.mig.gcp.model.firewall.FirewallRule;
import io.playce.roro.mig.gcp.model.network.FirewallDetail;
import io.playce.roro.mig.gcp.model.network.NetworkDetail;
import io.playce.roro.mig.gcp.model.network.SubnetDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GCPConfigService {

    private final ModelMapper modelMapper;

    private final CredentialMasterRepository credentialMasterRepository;


    /**
     * <pre>
     * 주어진 projectId, credentialId 로부터 GCP용 Credential 객체를 생성한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     *
     * @return
     */
    private GCPCredentials getGCPCredential(Long projectId, Long credentialId) {
        CredentialMaster credentialMaster = credentialMasterRepository.findByProjectIdAndCredentialId(projectId, credentialId);

        if (credentialMaster == null || !Domain1009.GCP.name().equals(credentialMaster.getCredentialTypeCode())) {
            throw new RoRoApiException(ErrorCode.TC_CREDENTIAL_NOT_FOUND);
        }

        try {
            return new GCPCredentials(credentialMaster.getKeyFilePath());
        } catch (Exception e) {
            throw new RoRoApiException(ErrorCode.TC_KEY_FILE_NOT_FOUND);
        }
    }

    /**
     * <pre>
     * GCP ProjectId 목록 조회
     * </pre>
     *
     * @param projectId
     * @param credentialId
     * @return
     */
    private GCPCredentials gcpCredentials;

    public GCPConfigDto.Projects getProjectList(Long projectId, Long credentialId) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            ComputeClient computeClient = new ComputeClient(gcpCredentials.getProjectId(), gcpCredentials.getAccountKey());
            return computeClient.getProjects();
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get project list.", e);

            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * Gets Machine image list.
     *
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     * @param search
     *
     * @return
     */
    public List<GCPConfigDto.MachineImageResponse> getMachineImageList(Long projectId, Long credentialId, String gcpProjectId, String search) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            ComputeClient computeClient = new ComputeClient(gcpProjectId, gcpCredentials.getAccountKey());
            List<MachineImage> machineImageList = computeClient.getMachineImageList(gcpProjectId, search);

            List<GCPConfigDto.MachineImageResponse> machineImageLists = new ArrayList<>();
            for (MachineImage mi : machineImageList) {
                GCPConfigDto.MachineImageResponse machineImages = modelMapper.map(mi, GCPConfigDto.MachineImageResponse.class);
                machineImageLists.add(machineImages);
            }

            return machineImageLists;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get image list.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     *
     * @return
     */
    public List<String> getRegionList(Long projectId, Long credentialId, String gcpProjectId) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            ComputeClient computeClient = new ComputeClient(gcpProjectId, gcpCredentials.getAccountKey());
            return computeClient.getAvailableRegionList();
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get region list.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     * @param region
     *
     * @return
     */
    public List<GCPConfigDto.AvailableZoneResponse> getZoneList(Long projectId, Long credentialId, String gcpProjectId, String region) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            ComputeClient computeClient = new ComputeClient(gcpProjectId, gcpCredentials.getAccountKey());
            Map<String, List<String>> zoneList = computeClient.getAvailableZoneList(region);

            List<GCPConfigDto.AvailableZoneResponse> responseList = new ArrayList<>();
            for (String key : zoneList.keySet()) {
                GCPConfigDto.AvailableZoneResponse response = new GCPConfigDto.AvailableZoneResponse();
                response.setRegion(key);
                response.setZoneList(zoneList.get(key));
                responseList.add(response);
            }
            return responseList;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get zone list.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     * @param zone
     *
     * @return
     */
    public Object getMachineTypeList(Long projectId, Long credentialId, String gcpProjectId, String zone) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            ComputeClient computeClient = new ComputeClient(gcpProjectId, gcpCredentials.getAccountKey());
            List<MachineType> machineTypeList = computeClient.getAvailableMachineTypeList(zone);

            List<GCPConfigDto.AvailableMachineResponse> responseList = new ArrayList<>();
            for (MachineType machineType : machineTypeList) {
                GCPConfigDto.AvailableMachineResponse response = new GCPConfigDto.AvailableMachineResponse();
                // GcpMachineType gcpMachineType = GcpMachineType.findByMachine(machineType.getName());
                String machineName = machineType.getName();
                String family = machineName.substring(0, machineName.indexOf('-'));
                response.setName(machineType.getName());
                response.setMachineFamily(family);
                response.setVCPUs(machineType.getGuestCpus().toString());
                response.setMemory(String.valueOf((machineType.getMemoryMb() / 1024L)));
                responseList.add(response);
            }

            return responseList;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get machine type list.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     * @param search
     *
     * @return
     */
    public List<GCPConfigDto.NetworkResponse> getNetworkList(Long projectId, Long credentialId, String gcpProjectId, String search) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            ComputeClient computeClient = new ComputeClient(gcpProjectId, gcpCredentials.getAccountKey());
            List<NetworkDetail> detailList = computeClient.getNetworks(search);

            List<GCPConfigDto.NetworkResponse> networkResponseList = new ArrayList<>();
            for (NetworkDetail networkDetail : detailList) {
                GCPConfigDto.NetworkResponse response = new GCPConfigDto.NetworkResponse();
                response.setNetworkId(networkDetail.getNetworkId());
                response.setNetworkName(networkDetail.getNetworkName());
                response.setMtu(networkDetail.getMtu());
                response.setSubnetCount(networkDetail.getSubnets() != null ? networkDetail.getSubnets().size() : 0);
                response.setRoutingMode(networkDetail.getRoutingConfig().getRoutingMode());
                response.setAutoCreateSubnetWorks(networkDetail.getAutoCreateSubnetWorks());
                response.setDnsPolicy(networkDetail.getPolicyDnsNetwork() != null ? networkDetail.getPolicyDnsNetwork() : "None");
                networkResponseList.add(response);
            }
            return networkResponseList;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get network list.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param networkRequest
     */
    public void createNetwork(Long projectId, Long credentialId, GCPConfigDto.NetworkCreateRequest networkRequest) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            NetworkDetail networkDetail = modelMapper.map(networkRequest, NetworkDetail.class);

            ComputeClient computeClient = new ComputeClient(networkRequest.getGcpProjectId(), gcpCredentials.getAccountKey());
            computeClient.createNetwork(networkDetail);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create network.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param networkId
     * @param networkRequest
     */
    public void updateNetwork(Long projectId, Long credentialId, String networkId, GCPConfigDto.NetworkUpdateRequest networkRequest) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            NetworkDetail networkDetail = modelMapper.map(networkRequest, NetworkDetail.class);

            ComputeClient computeClient = new ComputeClient(networkRequest.getGcpProjectId(), gcpCredentials.getAccountKey());
            computeClient.updateNetwork(networkId, networkDetail);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while update network.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param networkId
     * @param gcpProjectId
     */
    public void deleteNetwork(Long projectId, Long credentialId, String networkId, String gcpProjectId) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            ComputeClient computeClient = new ComputeClient(gcpProjectId, gcpCredentials.getAccountKey());
            computeClient.deleteNetwork(networkId);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while delete network.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     * @param region
     * @param search
     *
     * @return
     */
    public List<GCPConfigDto.SubnetResponse> getSubnetList(Long projectId, Long credentialId, String gcpProjectId, String region, String search) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            List<GCPConfigDto.SubnetResponse> subnetResponseList = new ArrayList<>();
            ComputeClient computeClient = new ComputeClient(gcpProjectId, gcpCredentials.getAccountKey());

            List<SubnetDetail> detailList;

            if (StringUtils.isEmpty(region)) {
                detailList = computeClient.getAllSubnetList(gcpProjectId, search);
            } else {
                detailList = computeClient.getSubnetList(region, search);
            }

            for (SubnetDetail subnetDetail : detailList) {
                GCPConfigDto.SubnetResponse response = modelMapper.map(subnetDetail, GCPConfigDto.SubnetResponse.class);
                if (SubnetConfig.Purpose.PRIVATE.name().equals(subnetDetail.getPurpose())) {
                    response.setLoadBalancing(false);
                } else {
                    response.setLoadBalancing(true);
                }

                subnetResponseList.add(response);
            }

            return subnetResponseList;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get subnet list.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param createRequest
     */
    public void createSubnet(Long projectId, Long credentialId, GCPConfigDto.SubnetWorkCreateRequest createRequest) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            if (createRequest.getIpCidrRange().equals("10.128.0.0/9")) {
                throw new Exception("Already reserved by GCP");
            }

            SubnetDetail subnetDetail = modelMapper.map(createRequest, SubnetDetail.class);

            ComputeClient computeClient = new ComputeClient(createRequest.getGcpProjectId(), gcpCredentials.getAccountKey());
            computeClient.createSubnetWorks(subnetDetail);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create subnet.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param subnetId
     * @param updateRequest
     */
    public void updateSubnet(Long projectId, Long credentialId, String subnetId, GCPConfigDto.SubnetWorkUpdateRequest updateRequest) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            if (updateRequest.getIpCidrRange().equals("10.128.0.0/9")) {
                throw new Exception("Already reserved by GCP");
            }

            SubnetDetail subnetDetail = modelMapper.map(updateRequest, SubnetDetail.class);
            subnetDetail.setId(subnetId);

            ComputeClient computeClient = new ComputeClient(updateRequest.getGcpProjectId(), gcpCredentials.getAccountKey());
            computeClient.updateSubnetWorks(subnetDetail);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while update subnet.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param subnetId
     * @param gcpProjectId
     * @param region
     */
    public void deleteSubnet(Long projectId, Long credentialId, String subnetId, String gcpProjectId, String region) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            ComputeClient computeClient = new ComputeClient(gcpProjectId, gcpCredentials.getAccountKey());
            computeClient.deleteSubnet(region, subnetId);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while delete subnet.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     * @param search
     *
     * @return
     */
    public List<GCPConfigDto.FirewallResponse> getFirewallRuleList(Long projectId, Long credentialId, String gcpProjectId, String search) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            ComputeClient computeClient = new ComputeClient(gcpProjectId, gcpCredentials.getAccountKey());
            List<FirewallDetail> firewallList = computeClient.getFirewallList(search);

            List<GCPConfigDto.FirewallResponse> networkResponseList = new ArrayList<>();
            for (FirewallDetail fd : firewallList) {

                GCPConfigDto.FirewallResponse fr = modelMapper.map(fd, GCPConfigDto.FirewallResponse.class);
                if (fd.getIngressInfo() != null) {
                    fr.setTargetType(fd.getIngressInfo().getTargetType());
                    fr.setTargetServiceAccount(fd.getIngressInfo().getTargetServiceAccounts());
                    fr.setTargetTags(fd.getIngressInfo().getTargetTags());
                    fr.setSourceFilter(fd.getIngressInfo().getSourceFilter());
                    fr.setSourceRanges(fd.getIngressInfo().getSourceRanges());
                    fr.setSourceTags(fd.getIngressInfo().getSourceTags());
                    fr.setSourceServiceAccount(fd.getIngressInfo().getSourceServiceAccounts());
                }
                if (fd.getEgressInfo() != null) {
                    fr.setTargetType(fd.getEgressInfo().getTargetType());
                    fr.setTargetServiceAccount(fd.getEgressInfo().getTargetServiceAccounts());
                    fr.setTargetTags(fd.getEgressInfo().getTargetTags());
                    fr.setDestinationFilter(fd.getEgressInfo().getDestinationFilter());
                    fr.setDestinationRanges(fd.getEgressInfo().getDestinationRanges());
                }

                List<GCPConfigDto.SpecifiedPort> specifiedPorts = new ArrayList<>();
                for (io.playce.roro.mig.gcp.model.network.SpecifiedPort port : fd.getSpecifiedPorts()) {
                    specifiedPorts.add(GCPConfigDto.SpecifiedPort.builder()
                            .ipProtocol(port.getIpProtocol()).ports(port.getPorts()).build());
                }
                fr.setSpecifiedPorts(specifiedPorts);
                networkResponseList.add(fr);
            }
            return networkResponseList;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get firewall list.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param gcpProjectId
     * @param networkId
     *
     * @return
     */
    public GCPConfigDto.FirewallRule getFirewallRuleTagList(Long projectId, Long credentialId, String gcpProjectId, String networkId) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            ComputeClient computeClient = new ComputeClient(gcpProjectId, gcpCredentials.getAccountKey());
            Map<String, List<FirewallRule>> tagMap = computeClient.getFirewallRule(networkId);

            List<Object> ingressTag = modelMapper.map(tagMap.get("ingress"), List.class);
            List<Object> egressTag = modelMapper.map(tagMap.get("egress"), List.class);

            GCPConfigDto.FirewallRule firewallTag = new GCPConfigDto.FirewallRule();
            firewallTag.setIngressTags(ingressTag);
            firewallTag.setEgressTags(egressTag);

            return firewallTag;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get firewall Tag list.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param createRequest
     */
    public void createFirewallRule(Long projectId, Long credentialId, GCPConfigDto.FirewallCreateRequest createRequest) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            FirewallDetail firewallDetail = modelMapper.map(createRequest, FirewallDetail.class);

            ComputeClient computeClient = new ComputeClient(createRequest.getGcpProjectId(), gcpCredentials.getAccountKey());
            computeClient.createFirewallRule(firewallDetail);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create firewall.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param firewallId
     * @param updateRequest
     */
    public void updateFirewallRule(Long projectId, Long credentialId, String firewallId, GCPConfigDto.FirewallUpdateRequest updateRequest) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            FirewallDetail firewallDetail = modelMapper.map(updateRequest, FirewallDetail.class);

            ComputeClient computeClient = new ComputeClient(updateRequest.getGcpProjectId(), gcpCredentials.getAccountKey());
            computeClient.updateFirewallRule(firewallId, firewallDetail);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while update firewall.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param firewallId
     * @param gcpProjectId
     */
    public void deleteFirewallRule(Long projectId, Long credentialId, String firewallId, String gcpProjectId) {
        gcpCredentials = getGCPCredential(projectId, credentialId);

        try {
            ComputeClient computeClient = new ComputeClient(gcpProjectId, gcpCredentials.getAccountKey());
            computeClient.deleteFirewall(firewallId);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while delete firewall.", e);
            if (e instanceof GoogleJsonResponseException) {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, ((GoogleJsonResponseException) e).getDetails().getMessage());
            } else {
                throw new RoRoApiException(ErrorCode.TC_GCP_GCE_ERROR, e.getMessage());
            }
        }

    }
}
//end of GCPConfigService.java