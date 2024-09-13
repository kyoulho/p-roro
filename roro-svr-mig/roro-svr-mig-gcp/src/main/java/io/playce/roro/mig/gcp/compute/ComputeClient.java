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
package io.playce.roro.mig.gcp.compute;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.ListProjectsResponse;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.*;
import com.google.api.services.dns.Dns;
import com.google.api.services.dns.model.PoliciesListResponse;
import com.google.api.services.dns.model.PolicyNetwork;
import io.playce.roro.common.dto.migration.MigrationProcessDto;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.mig.gcp.common.BaseClient;
import io.playce.roro.mig.gcp.enums.ResourceType;
import io.playce.roro.mig.gcp.enums.network.DirectionType;
import io.playce.roro.mig.gcp.enums.network.FilterType;
import io.playce.roro.mig.gcp.enums.network.SubnetConfig;
import io.playce.roro.mig.gcp.enums.network.TargetType;
import io.playce.roro.mig.gcp.model.GCPConfigDto;
import io.playce.roro.mig.gcp.model.GCPRequestGenerator;
import io.playce.roro.mig.gcp.model.GCPVolume;
import io.playce.roro.mig.gcp.model.firewall.FirewallRule;
import io.playce.roro.mig.gcp.model.network.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
public class ComputeClient extends BaseClient {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    private Compute compute;
    private Dns dns;

    /**
     * Instantiates a new Compute client.
     *
     * @param projectId  the project id
     * @param accountKey the account key
     */
    public ComputeClient(String projectId, String accountKey) {
        super(projectId, accountKey, ComputeScopes.all());

        compute = new Compute.Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName(applicationName)
                .build();

        resourceManager = new CloudResourceManager.Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName(applicationName)
                .build();

        dns = new Dns.Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName(applicationName)
                .build();
    }

    /**
     * Gets projects.
     *
     * @return the projects
     *
     * @throws Exception the io exception
     */
    public GCPConfigDto.Projects getProjects() throws Exception {
        CloudResourceManager.Projects.List request = resourceManager.projects().list();

        GCPConfigDto.Projects projectList = new GCPConfigDto.Projects();
        List<GCPConfigDto.Project> detailList = new ArrayList<>();
        ListProjectsResponse response;
        try {
            do {
                response = request.execute();
                if (response.getProjects() == null) {
                    continue;
                }
                for (com.google.api.services.cloudresourcemanager.model.Project project : response.getProjects()) {
                    GCPConfigDto.Project detail = new GCPConfigDto.Project();
                    detail.setId(project.getProjectId());
                    detail.setName(project.getName());
                    detailList.add(detail);
                }
                request.setPageToken(response.getNextPageToken());
            } while (response.getNextPageToken() != null);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() != 403) {
                throw e;
            }
            projectList.setMessage(e.getDetails().getMessage());
        } catch (Exception e) {
            log.debug("Unhandled exception occurred while execute get GCP projects. [Reason] : ", e.getMessage());
            //ignore
        } finally {
            if (detailList.size() < 1) {
                com.google.api.services.compute.model.Project currentProject = compute.projects().get(projectId).execute();
                GCPConfigDto.Project detail = new GCPConfigDto.Project();
                detail.setId(projectId);
                detail.setName(currentProject.getName());
                detailList.add(detail);
            }
            projectList.setProjectList(detailList);
        }

        return projectList;
    }

    /**
     * Gets machine image list.
     *
     * @param gcpProjectId
     * @param search
     *
     * @return
     *
     * @throws IOException
     */
    public List<MachineImage> getMachineImageList(String gcpProjectId, String search) throws IOException {
        MachineImageList machineImages = null;
        if (search != null && search.length() > 1) {
            machineImages = compute.machineImages().list(gcpProjectId).setFilter("id=" + search).execute();
        } else {
            machineImages = compute.machineImages().list(gcpProjectId).execute();
        }
        if (machineImages.getItems() == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(machineImages.getItems());
        }
    }

    /**
     * Gets availablity region list.
     *
     * @return the availablity region list
     *
     * @throws Exception the io exception
     */
    public List<String> getAvailableRegionList() throws Exception {
        RegionList regionList = compute.regions().list(projectId).execute();

        List<String> rgList = regionList.getItems().stream()
                .filter(rg -> rg.getStatus().equals("UP"))
                .map(region1 -> region1.getName()).collect(Collectors.toList());

        return rgList;
    }

    /**
     * Gets availablity zone list.
     *
     * @param filterRegion the filter region
     *
     * @return the availablity zone list
     *
     * @throws Exception the io exception
     */
    public Map<String, List<String>> getAvailableZoneList(String filterRegion) throws Exception {
        ZoneList zoneList = compute.zones().list(projectId).execute();

        List<String> availableRegionList = this.getAvailableRegionList();

        Map<String, List<String>> azMap = new HashMap<>();
        for (Zone az : zoneList.getItems()) {
            if (az.getStatus().equals("UP")) {
                String rg = availableRegionList.stream()
                        .filter(r -> az.getRegion().endsWith(r)).findFirst().orElse(null);

                if (rg == null) {
                    continue;
                }

                List<String> azList = null;
                if (!azMap.containsKey(rg)) {
                    azList = new ArrayList<>();
                    azList.add(az.getName());
                    azMap.put(rg, azList);
                } else {
                    azList = azMap.get(rg);
                    azList.add(az.getName());
                }
            }
        }

        if (filterRegion != null) {
            azMap.keySet().removeIf(rg -> !rg.equals(filterRegion));
        }

        return azMap;
    }

    /**
     * Gets availablity machine type list.
     *
     * @param zone the zone
     *
     * @return the availablity machine type list
     *
     * @throws Exception the io exception
     */
    public List<MachineType> getAvailableMachineTypeList(String zone) throws Exception {
        List<MachineType> machineList = new ArrayList<>();
        MachineTypeList machineTypeList = compute.machineTypes().list(projectId, zone).execute();
        machineTypeList.getItems().forEach(machineType -> machineList.add(machineType));

        return machineList;
    }

    /**
     * Gets networks.
     *
     * @param search the search
     *
     * @return the networks
     *
     * @throws Exception the io exception
     */
    public List<NetworkDetail> getNetworks(String search) throws Exception {
        NetworkList networkList = compute.networks().list(projectId).execute();
        PoliciesListResponse policiesList = dns.policies().list(projectId).execute();

        List<com.google.api.services.dns.model.Policy> policies = policiesList.getPolicies();
        List<NetworkDetail> networkDetailList = new ArrayList<>();
        for (Network network : networkList.getItems()) {
            NetworkDetail networkDetail = null;

            if (search != null) {
                if (network.getName().indexOf(search) > -1 ||
                        network.getId().toString().indexOf(search) > -1) {
                    networkDetail = getNetworkDetail(network);
                }
            } else {
                networkDetail = getNetworkDetail(network);
            }

            for (com.google.api.services.dns.model.Policy dnsPolicy : policies) {
                PolicyNetwork policyNetwork = dnsPolicy.getNetworks().stream()
                        .filter(nw -> nw.getNetworkUrl().equals(network.getSelfLink()))
                        .findFirst().orElse(null);
                if (policyNetwork != null) {
                    networkDetail.setPolicyDnsNetwork(dnsPolicy.getName());
                }
            }

            networkDetailList.add(networkDetail);
        }

        return networkDetailList;
    }

    /**
     * Gets network.
     *
     * @param id the id
     *
     * @return the network
     *
     * @throws Exception the exception
     */
    public NetworkDetail getNetwork(String id) throws Exception {
        Network network = compute.networks().get(projectId, id).execute();
        return getNetworkDetail(network);
    }

    private NetworkDetail getNetworkDetail(Network network) {
        NetworkDetail networkDetail = new NetworkDetail();
        networkDetail.setNetworkId(network.getId().toString());
        networkDetail.setNetworkName(network.getName());
        networkDetail.setAutoCreateSubnetWorks(network.getAutoCreateSubnetworks());
        networkDetail.setDescription(network.getDescription());
        networkDetail.setSubnets(network.getSubnetworks());
        networkDetail.setMtu(network.getOrDefault("mtu", 1460).toString());
        networkDetail.setSelfLink(network.getSelfLink());
        NetworkRoutingConfig routingConfig = network.getRoutingConfig();
        RoutingConfig routingConfigEntity = new RoutingConfig();
        routingConfigEntity.setRoutingMode(routingConfig.getRoutingMode());
        networkDetail.setRoutingConfig(routingConfigEntity);
        return networkDetail;
    }

    /**
     * Gets operation state.
     *
     * @param operationId  the operation id
     * @param resourceType the resource type
     * @param location     the location
     *
     * @return the operation state
     *
     * @throws Exception the io exception
     */
    public Operation getOperationState(String operationId, ResourceType resourceType, String location) throws Exception {
        Operation response = null;
        switch (resourceType) {
            case GLOBAL:
                response = compute.globalOperations().get(projectId, operationId).execute();
                break;
            case REGIONAL:
                response = compute.regionOperations().get(projectId, location, operationId).execute();
                break;
            case ZONAL:
                response = compute.zoneOperations().get(projectId, location, operationId).execute();
                break;
        }

        return response;
    }

    /**
     * Waiting operation.
     *
     * @param operation    the operation
     * @param resourceType the resource type
     * @param location     the location
     *
     * @throws Exception the io exception
     */
    private void waitingOperation(Operation operation, ResourceType resourceType, String location) throws Exception {
        while (true) {
            try {
                Operation response = getOperationState(operation.getId().toString(), resourceType, location);
                if (response.getError() != null) {
                    throw new RoRoException("Operation failed with error : " + response.getError().getErrors().get(0).getMessage());
                }
                if ("DONE".equals(response.getStatus()) && response.getProgress() == 100) {
                    break;
                }

                Thread.sleep(2000);

            } catch (Exception e) {
                log.debug("Unhandled exception occurred while status check failed. [Reason] : ", e.getMessage());
                throw e;
            }
        }
    }

    /**
     * Create network network.
     *
     * @param networkDetail the network detail
     *
     * @return the network
     *
     * @throws Exception the exception
     */
    public void createNetwork(NetworkDetail networkDetail) throws Exception {
        try {
            Network network = new Network();
            network.setName(networkDetail.getNetworkName());
            network.setAutoCreateSubnetworks(networkDetail.getAutoCreateSubnetWorks());
            if (networkDetail.getMtu() != null) {
                network.set("mtu", Integer.parseInt(networkDetail.getMtu()));
            }
            network.setDescription(networkDetail.getDescription());

            /*Routing Mode*/
            NetworkRoutingConfig routingConfig = new NetworkRoutingConfig();
            routingConfig.setRoutingMode(networkDetail.getRoutingConfig().getRoutingMode());
            network.setRoutingConfig(routingConfig);

            Operation response = compute.networks().insert(projectId, network).execute();
            waitingOperation(response, ResourceType.GLOBAL, null);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Update network.
     *
     * @param networkId     the network id
     * @param networkDetail the network detail
     *
     * @throws Exception the exception
     */
    public void updateNetwork(String networkId, NetworkDetail networkDetail) throws Exception {
        try {
            Network network = compute.networks().get(projectId, networkId).execute();

            /*
             * 서브넷 생성 모드 수정 시 다른 필드요소는 수정 불가.
             * Custom mode로 변환 시 Auto mode수정 불가능.
             *
             * 수정 요청 후 요청상태가 DONE일때 다음 수정내용을 반영 할 수 있다.
             * */
            if (network.getAutoCreateSubnetworks()) {
                if (!networkDetail.getAutoCreateSubnetWorks()) {
                    Operation response = compute.networks().switchToCustomMode(projectId, network.getName()).execute();
                    waitingOperation(response, ResourceType.GLOBAL, null);
                }
            }

            NetworkRoutingConfig routingConfig = new NetworkRoutingConfig();
            routingConfig.setRoutingMode(networkDetail.getRoutingConfig().getRoutingMode());
            network.setRoutingConfig(routingConfig);
            network.set("mtu", null);

            compute.networks().patch(projectId, networkId, network).execute();

            //ToDo: Static IP, Firewall, Routing, Peer, privateConnection
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Delete network.
     *
     * @param networkId the network id
     *
     * @throws Exception the exception
     */
    public void deleteNetwork(String networkId) throws Exception {

        Network network = compute.networks().get(projectId, networkId).execute();

        if (network.getSubnetworks() != null && !network.getAutoCreateSubnetworks()) {
            for (String subnetLink : network.getSubnetworks()) {
                int regionIdx = subnetLink.lastIndexOf("regions/");
                int subnetIdx = subnetLink.lastIndexOf("subnetworks/");
                String region = subnetLink.substring(regionIdx, subnetIdx - 1).split("/")[1];
                String subnet = subnetLink.substring(subnetIdx).split("/")[1];
                // String subnet = subnetLink.substring(subnetIdx, subnetLink.length()).split("/")[1];

                compute.subnetworks().delete(projectId, region, subnet).execute();
            }
        }

        Operation response = compute.networks().delete(projectId, networkId).execute();
        waitingOperation(response, ResourceType.GLOBAL, null);
    }

    /**
     * Gets all subnet list.
     *
     * @param projectId the project id
     * @param search    the search
     *
     * @return the all subnet list
     */
    @SneakyThrows(Exception.class)
    public List<SubnetDetail> getAllSubnetList(String projectId, String search) {
        ObjectMapper objectMapper = new ObjectMapper();

        SubnetworkAggregatedList subnetworkAggregatedList = compute.subnetworks().aggregatedList(projectId).execute();
        NetworkList networkList = compute.networks().list(projectId).execute();

        List<SubnetWorkDto.SubnetWork> subnetWorkList = new ArrayList<>();
        List<SubnetDetail> subnetDetailList = new ArrayList<>();

        for (Map.Entry<String, SubnetworksScopedList> subnetworksScopedListMap : subnetworkAggregatedList.getItems().entrySet()) {
            String tempSubnetWorks = subnetworksScopedListMap.getValue().toString();
            SubnetWorkDto.SubnetWorks subnetWorks = objectMapper.readValue(tempSubnetWorks, new TypeReference<SubnetWorkDto.SubnetWorks>() {});
            subnetWorkList.addAll(subnetWorks.getSubnetworks());
        }

        for (SubnetWorkDto.SubnetWork subnetWork : subnetWorkList) {
            Network network = networkList.getItems()
                    .stream()
                    .filter(net -> subnetWork.getNetwork().equals(net.getSelfLink()))
                    .findFirst().orElse(null);

            if (network != null) {
                SubnetDetail detail;
                if (StringUtils.isEmpty(search)) {
                    detail = getSubnetDetail(subnetWork, network);
                    subnetDetailList.add(detail);
                } else {
                    if (network.getName().contains(search) || network.getId().toString().contains(search)) {
                        detail = getSubnetDetail(subnetWork, network);
                        subnetDetailList.add(detail);
                    }
                }
            }

        }

        return subnetDetailList;
    }

    private SubnetDetail getSubnetDetail(SubnetWorkDto.SubnetWork subnetWork, Network network) {
        SubnetDetail subnetDetail = new SubnetDetail();
        subnetDetail.setId(subnetWork.getId());
        subnetDetail.setName(subnetWork.getName());
        subnetDetail.setNetworkId(network.getId().toString());
        subnetDetail.setNetworkName(network.getName());
        subnetDetail.setState(subnetWork.getState());
        subnetDetail.setIpCidrRange(subnetWork.getIpCidrRange());
        subnetDetail.setGatewayAddress(subnetWork.getGatewayAddress());
        subnetDetail.setPurpose(subnetWork.getPurpose());
        subnetDetail.setRole(subnetWork.getRole());
        subnetDetail.setState(subnetWork.getState());
        subnetDetail.setRegion(subnetWork.getRegion().substring(subnetWork.getRegion().lastIndexOf("/") + 1));
        subnetDetail.setPrivateIpGoogleAccess(subnetWork.getPrivateIpGoogleAccess());
        subnetDetail.setDescription(subnetWork.getDescription());
        subnetDetail.setFingerprint(subnetWork.getFingerprint());
        subnetDetail.setAutoCreateSubnetworks(network.getAutoCreateSubnetworks());

        return subnetDetail;
    }

    /**
     * Gets subnet list.
     *
     * @param region the region
     * @param search the search
     *
     * @return the subnet list
     *
     * @throws Exception the exception
     */
    public List<SubnetDetail> getSubnetList(String region, String search) throws Exception {
        SubnetworkList subnetworkList = compute.subnetworks().list(projectId, region)
                .setFilter("purpose = PRIVATE").execute();

        List<SubnetDetail> subnetDetailList = new ArrayList<>();
        NetworkList networkList = compute.networks().list(projectId).execute();
        for (Subnetwork subnet : subnetworkList.getItems()) {
            Network network = networkList.getItems()
                    .stream()
                    .filter(net -> subnet.getNetwork().equals(net.getSelfLink()))
                    .findFirst().orElse(null);

            if (network != null) {
                SubnetDetail detail;
                if (StringUtils.isEmpty(search)) {
                    detail = getSubnetDetail(subnet, network);
                    subnetDetailList.add(detail);
                } else {
                    if (network.getName().contains(search) || network.getId().toString().contains(search)) {
                        detail = getSubnetDetail(subnet, network);
                        subnetDetailList.add(detail);
                    }
                }
            }

        }

        return subnetDetailList;
    }

    /**
     * Gets subnet.
     *
     * @param region the region
     * @param id     the id
     *
     * @return the subnet
     *
     * @throws Exception the exception
     */
    public SubnetDetail getSubnet(String region, String id) throws Exception {
        Subnetwork subnetwork = compute.subnetworks().get(projectId, region, id).execute();
        NetworkList networkList = compute.networks().list(projectId).execute();

        Network network = networkList.getItems()
                .stream()
                .filter(net -> subnetwork.getNetwork().equals(net.getSelfLink()))
                .findFirst().orElse(null);

        return getSubnetDetail(subnetwork, network);
    }

    private SubnetDetail getSubnetDetail(Subnetwork subnet, Network network) {
        SubnetDetail subnetDetail = new SubnetDetail();
        subnetDetail.setId(subnet.getId().toString());
        subnetDetail.setName(subnet.getName());
        subnetDetail.setNetworkId(network.getId().toString());
        subnetDetail.setNetworkName(network.getName());
        subnetDetail.setState(subnet.getState());
        subnetDetail.setIpCidrRange(subnet.getIpCidrRange());
        subnetDetail.setGatewayAddress(subnet.getGatewayAddress());
        subnetDetail.setPurpose(subnet.getPurpose());
        subnetDetail.setRole(subnet.getRole());
        subnetDetail.setState(subnet.getState());
        subnetDetail.setRegion(subnet.getRegion().substring(subnet.getRegion().lastIndexOf("/") + 1));
        subnetDetail.setPrivateIpGoogleAccess(subnet.getPrivateIpGoogleAccess());
        subnetDetail.setDescription(subnet.getDescription());
        subnetDetail.setFingerprint(subnet.getFingerprint());

        return subnetDetail;
    }

    /**
     * Create subnetworks.
     *
     * @param subnetDetail the subnet detail
     *
     * @throws Exception the exception
     */
    public void createSubnetWorks(SubnetDetail subnetDetail) throws Exception {
        try {
            Subnetwork subnetwork = new Subnetwork();
            subnetwork.setName(subnetDetail.getName());
            subnetwork.setNetwork("global/networks/" + subnetDetail.getNetworkId());
            subnetwork.setRegion(subnetDetail.getRegion());
            subnetwork.setIpCidrRange(subnetDetail.getIpCidrRange());
            subnetwork.setPurpose(subnetDetail.getPurpose());
            if (!subnetDetail.getPurpose().equals(SubnetConfig.Purpose.PRIVATE.name())) {
                subnetwork.setRole(subnetDetail.getRole());
            } else {
                subnetwork.setPrivateIpGoogleAccess(subnetDetail.getPrivateIpGoogleAccess());
            }

            Operation response = compute.subnetworks().insert(projectId, subnetDetail.getRegion(), subnetwork).execute();
            waitingOperation(response, ResourceType.REGIONAL, subnetDetail.getRegion());
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Update subnet works.
     *
     * @param subnetDetail the subnet detail
     *
     * @throws Exception the exception
     */
    public void updateSubnetWorks(SubnetDetail subnetDetail) throws Exception {
        try {
            Subnetwork subnetwork = compute.subnetworks().get(projectId,
                    subnetDetail.getRegion(), subnetDetail.getId()).execute();

            if (subnetwork.getPurpose().equals(SubnetConfig.Purpose.INTERNAL_HTTPS_LOAD_BALANCER.toString())) {
                //Load balancer can't
            } else {
                if (subnetDetail.getPrivateIpGoogleAccess() != null) {
                    compute.subnetworks().setPrivateIpGoogleAccess(projectId, subnetDetail.getRegion(), subnetDetail.getId()
                                    , new SubnetworksSetPrivateIpGoogleAccessRequest().setPrivateIpGoogleAccess(subnetDetail.getPrivateIpGoogleAccess()))
                            .execute();
                }

                if (!subnetwork.getIpCidrRange().equals(subnetDetail.getIpCidrRange())) {
                    compute.subnetworks().expandIpCidrRange(projectId, subnetDetail.getRegion(), subnetDetail.getId()
                            , new SubnetworksExpandIpCidrRangeRequest().setIpCidrRange(subnetDetail.getIpCidrRange())).execute();
                }
            }
        } catch (Exception e) {
            // ignore
            throw e;
        }
    }

    /**
     * Delete subnet.
     *
     * @param region   the region
     * @param subnetId the subnet id
     *
     * @throws Exception the exception
     */
    public void deleteSubnet(String region, String subnetId) throws Exception {
        Operation response = compute.subnetworks().delete(projectId, region, subnetId).execute();
        waitingOperation(response, ResourceType.REGIONAL, region);
    }

    /**
     * Gets firewall list.
     *
     * @param search the search
     *
     * @return the firewall list
     *
     * @throws Exception the exception
     */
    public List<FirewallDetail> getFirewallList(String search) throws Exception {
        FirewallList list = compute.firewalls().list(projectId).execute();

        List<FirewallDetail> firewallDetailList = new ArrayList<>();
        for (Firewall fw : list.getItems()) {
            String networkName = getNetworkName(fw.getNetwork());
            Network network = compute.networks().get(projectId, networkName).execute();
            FirewallDetail detail = null;
            if (search != null) {
                if (network.getName().indexOf(search) > -1 || network.getId().toString().indexOf(search) > -1) {
                    detail = getFirewallDetail(fw, network);
                    firewallDetailList.add(detail);
                }
            } else {
                detail = getFirewallDetail(fw, network);
                firewallDetailList.add(detail);
            }
        }
        return firewallDetailList;
    }

    private FirewallDetail getFirewallDetail(Firewall fw, Network network) {
        FirewallDetail detail = new FirewallDetail();
        detail.setFirewallId(fw.getId().toString());
        detail.setFirewallName(fw.getName());
        detail.setDescription(fw.getDescription());
        detail.setDirection(fw.getDirection());
        detail.setNetworkId(network.getId().toString());
        detail.setNetworkName(network.getName());
        detail.setPriority(fw.getPriority().toString());
        detail.setEnforcement(fw.getDisabled());

        if (fw.getDirection().equals(DirectionType.INGRESS.name())) {
            IngressInfo info = new IngressInfo();
            info.setSourceRanges(new ArrayList<>());
            info.setSourceServiceAccounts(new ArrayList<>());
            info.setSourceTags(new ArrayList<>());
            info.setTargetServiceAccounts(new ArrayList<>());
            info.setTargetTags(new ArrayList<>());

            if (fw.getTargetTags() != null) {
                info.setTargetType(TargetType.TAG.name());
                info.setTargetTags(fw.getTargetTags());
                if (fw.getSourceRanges() != null) {
                    info.setSourceFilter(FilterType.IPRANGE.getAlias());
                    info.setSourceRanges(fw.getSourceRanges());
                } else if (fw.getSourceTags() != null) {
                    info.setSourceFilter(FilterType.TAG.getAlias());
                    info.setSourceTags(fw.getSourceTags());
                }
            } else if (fw.getTargetServiceAccounts() != null) {
                info.setTargetType(TargetType.SERVICEACCOUNT.name());
                info.setTargetServiceAccounts(fw.getTargetServiceAccounts());

                if (fw.getSourceRanges() != null) {
                    info.setSourceFilter(FilterType.IPRANGE.getAlias());
                    info.setSourceRanges(fw.getSourceRanges());
                } else {
                    info.setSourceFilter(FilterType.SERVICEACCOUNT.getAlias());
                    info.setSourceServiceAccounts(fw.getSourceServiceAccounts());
                }

            } else {
                info.setTargetType(TargetType.ALL.name());
                if (fw.getSourceRanges() != null) {
                    info.setSourceFilter(FilterType.IPRANGE.getAlias());
                    info.setSourceRanges(fw.getSourceRanges());
                } else if (fw.getSourceTags() != null) {
                    info.setSourceFilter(FilterType.TAG.getAlias());
                    info.setSourceTags(fw.getSourceTags());
                } else {
                    info.setSourceFilter(FilterType.SERVICEACCOUNT.getAlias());
                    info.setSourceServiceAccounts(fw.getSourceServiceAccounts());
                }

            }
            detail.setIngressInfo(info);
        } else {
            EgressInfo info = new EgressInfo();
            info.setDestinationRanges(new ArrayList<>());
            info.setTargetServiceAccounts(new ArrayList<>());
            info.setTargetTags(new ArrayList<>());
            if (fw.getTargetTags() != null) {
                info.setTargetType(TargetType.TAG.name());
                info.setTargetTags(fw.getTargetTags());
                if (fw.getDestinationRanges() != null) {
                    info.setDestinationFilter(FilterType.IPRANGE.getAlias());
                    info.setDestinationRanges(fw.getDestinationRanges());
                }
            } else if (fw.getTargetServiceAccounts() != null) {
                info.setTargetType(TargetType.SERVICEACCOUNT.name());
                info.setTargetServiceAccounts(fw.getTargetServiceAccounts());

                if (fw.getDestinationRanges() != null) {
                    info.setDestinationFilter(FilterType.IPRANGE.getAlias());
                    info.setDestinationRanges(fw.getDestinationRanges());
                }
            } else {
                info.setTargetType(TargetType.ALL.name());
                if (fw.getDestinationRanges() != null) {
                    info.setDestinationFilter(FilterType.IPRANGE.getAlias());
                    info.setDestinationRanges(fw.getDestinationRanges());
                }
            }
            detail.setEgressInfo(info);
        }

        // Set port info & action type
        List<SpecifiedPort> specifiedPorts = new ArrayList<>();
        if (fw.getAllowed() != null) {
            for (Firewall.Allowed allowed : fw.getAllowed()) {
                SpecifiedPort ports = new SpecifiedPort();
                ports.setIpProtocol(allowed.getIPProtocol());
                if (allowed.getPorts() != null) {
                    ports.setPorts(allowed.getPorts());
                } else {
                    ports.setPorts(new ArrayList<>());
                }
                specifiedPorts.add(ports);
            }
            detail.setActionType("Allow");
        } else {
            if (fw.getDenied() != null) {
                for (Firewall.Denied denied : fw.getDenied()) {
                    SpecifiedPort ports = new SpecifiedPort();
                    ports.setIpProtocol(denied.getIPProtocol());
                    if (denied.getPorts() != null) {
                        ports.setPorts(denied.getPorts());
                    } else {
                        ports.setPorts(new ArrayList<>());
                    }
                    specifiedPorts.add(ports);
                }
            }
            detail.setActionType("Deny");
        }
        detail.setSpecifiedPorts(specifiedPorts);

        return detail;
    }

    /**
     * Gets network name.
     *
     * @param networkUrl the network url
     *
     * @return the network name
     */
    public String getNetworkName(String networkUrl) {
        int networkIdx = networkUrl.lastIndexOf("networks/");
        String networkName = networkUrl.substring(networkIdx).split("/")[1];
        return networkName;
    }


    /**
     * Create firewall rule.
     *
     * @param detail the detail
     *
     * @return the string
     *
     * @throws Exception the exception
     */
    public Operation createFirewallRule(FirewallDetail detail) throws Exception {
        Firewall firewall = new Firewall();
        firewall.setName(detail.getFirewallName());
        firewall.setNetwork("global/networks/" + detail.getNetworkName());
        firewall.setDescription(detail.getDescription());
        firewall.setPriority(Integer.parseInt(detail.getPriority()));
        firewall.setDisabled(detail.getEnforcement());
        firewall.setDirection(detail.getDirection());


        if (detail.getDirection().equals(DirectionType.INGRESS.name())) {
            IngressInfo info = detail.getIngressInfo();
            if (info.getTargetType().equals(TargetType.ALL.name())) {
                //
            } else if (info.getTargetType().equals(TargetType.TAG.name())) {
                firewall.setTargetTags(info.getTargetTags());
            } else {
                firewall.setTargetServiceAccounts(info.getTargetServiceAccounts());
            }

            if (info.getSourceFilter().equals(FilterType.IPRANGE.getAlias())) {
                firewall.setSourceRanges(info.getSourceRanges());
            } else if (info.getSourceFilter().equals(FilterType.TAG.getAlias())) {
                firewall.setSourceTags(info.getSourceTags());
            } else if (info.getSourceFilter().equals(FilterType.SERVICEACCOUNT.getAlias())) {
                firewall.setSourceServiceAccounts(info.getSourceServiceAccounts());
            }
        } else {
            EgressInfo info = detail.getEgressInfo();
            if (info.getTargetType().equals(TargetType.ALL.name())) {
                //
            } else if (info.getTargetType().equals(TargetType.TAG.name())) {
                firewall.setTargetTags(info.getTargetTags());
            } else {
                firewall.setTargetServiceAccounts(info.getTargetServiceAccounts());
            }

            if (info.getDestinationFilter().equals(FilterType.IPRANGE.getAlias())) {
                firewall.setDestinationRanges(info.getDestinationRanges());
            }
        }

        //Set port info.
        if (detail.getActionType().equals("ALLOW")) {
            List<Firewall.Allowed> alloweds = new ArrayList<>();
            for (SpecifiedPort ports : detail.getSpecifiedPorts()) {
                if ("all".equals(ports.getIpProtocol().toLowerCase())) {
                    alloweds.add(new Firewall.Allowed().setIPProtocol(ports.getIpProtocol()));
                } else {
                    alloweds.add(new Firewall.Allowed().setIPProtocol(ports.getIpProtocol()).setPorts(ports.getPorts()));
                }
            }
            firewall.setAllowed(alloweds);
        } else if (detail.getActionType().equals("DENY")) {
            List<Firewall.Denied> denied = new ArrayList<>();
            for (SpecifiedPort ports : detail.getSpecifiedPorts()) {
                if ("all".equals(ports.getIpProtocol().toLowerCase())) {
                    denied.add(new Firewall.Denied().setIPProtocol(ports.getIpProtocol()));
                } else {
                    denied.add(new Firewall.Denied().setIPProtocol(ports.getIpProtocol()).setPorts(ports.getPorts()));
                }
            }
            firewall.setDenied(denied);
        }

        Operation response = compute.firewalls().insert(projectId, firewall).execute();
        waitingOperation(response, ResourceType.GLOBAL, null);
        return response;
    }

    /**
     * Update firewall rule.
     *
     * @param firewallId the firewall id
     * @param detail     the detail
     *
     * @throws Exception the exception
     */
    public void updateFirewallRule(String firewallId, FirewallDetail detail) throws Exception {
        Firewall firewall = compute.firewalls().get(projectId, firewallId).execute();
        firewall.setDescription(detail.getDescription());
        firewall.setPriority(Integer.parseInt(detail.getPriority()));
        firewall.setDisabled(detail.getEnforcement());

        firewall.setTargetTags(null);
        firewall.setTargetServiceAccounts(null);
        firewall.setSourceTags(null);
        firewall.setSourceServiceAccounts(null);
        firewall.setSourceRanges(null);

        if (!firewall.getDirection().equals(detail.getDirection())) {
            throw new RoRoException("[GCP] Direction cannot be changed ");
        }

        if ((firewall.getAllowed() == null && detail.getActionType().equals("ALLOW"))
                || (firewall.getDenied() == null && detail.getActionType().equals("DENY"))
        ) {
            throw new RoRoException("[GCP] ActionType cannot be changed ");
        }

        //Set port info.
        if (detail.getActionType().equals("ALLOW")) {
            List<Firewall.Allowed> alloweds = new ArrayList<>();
            for (SpecifiedPort ports : detail.getSpecifiedPorts()) {
                if ("all".equals(ports.getIpProtocol().toLowerCase())) {
                    alloweds.add(new Firewall.Allowed().setIPProtocol(ports.getIpProtocol()));
                } else {
                    alloweds.add(new Firewall.Allowed().setIPProtocol(ports.getIpProtocol()).setPorts(ports.getPorts()));
                }
            }
            firewall.setAllowed(alloweds);
        } else if (detail.getActionType().equals("DENY")) {
            List<Firewall.Denied> denied = new ArrayList<>();
            for (SpecifiedPort ports : detail.getSpecifiedPorts()) {
                if ("all".equals(ports.getIpProtocol().toLowerCase())) {
                    denied.add(new Firewall.Denied().setIPProtocol(ports.getIpProtocol()));
                } else {
                    denied.add(new Firewall.Denied().setIPProtocol(ports.getIpProtocol()).setPorts(ports.getPorts()));
                }
            }
            firewall.setDenied(denied);
        }

        Operation response = compute.firewalls().patch(projectId, firewallId, firewall).execute();
        waitingOperation(response, ResourceType.GLOBAL, null);

        if (detail.getDirection().equals(DirectionType.INGRESS.name())) {
            IngressInfo info = detail.getIngressInfo();
            if (info.getTargetType().equals(TargetType.ALL.name())) {
                //
            } else if (info.getTargetType().equals(TargetType.TAG.name())) {
                firewall.setTargetTags(info.getTargetTags());
            } else {
                firewall.setTargetServiceAccounts(info.getTargetServiceAccounts());
            }

            if (info.getSourceFilter().equals(FilterType.IPRANGE.getAlias())) {
                firewall.setSourceRanges(info.getSourceRanges());
            } else if (info.getSourceFilter().equals(FilterType.TAG.getAlias())) {
                firewall.setSourceTags(info.getSourceTags());
            } else if (info.getSourceFilter().equals(FilterType.SERVICEACCOUNT.getAlias())) {
                firewall.setSourceServiceAccounts(info.getSourceServiceAccounts());
            }
        } else {
            EgressInfo info = detail.getEgressInfo();
            if (info.getTargetType().equals(TargetType.ALL.name())) {
                //
            } else if (info.getTargetType().equals(TargetType.TAG.name())) {
                firewall.setTargetTags(info.getTargetTags());
            } else {
                firewall.setTargetServiceAccounts(info.getTargetServiceAccounts());
            }

            if (info.getDestinationFilter().equals(FilterType.IPRANGE.getAlias())) {
                firewall.setDestinationRanges(info.getDestinationRanges());
            }
        }

        compute.firewalls().update(projectId, firewallId, firewall).execute();

    }

    /**
     * Delete firewall.
     *
     * @param firewallId the firewall id
     *
     * @throws Exception the exception
     */
    public void deleteFirewall(String firewallId) throws Exception {
        Operation response = compute.firewalls().delete(projectId, firewallId).execute();
        waitingOperation(response, ResourceType.GLOBAL, null);
    }

    /**
     * Gets firewall tags.
     *
     * @param networkId the network id
     *
     * @return the firewall tags
     *
     * @throws Exception the io exception
     */
    public Map<String, List<FirewallRule>> getFirewallRule(String networkId) throws Exception {
        Network network = compute.networks().get(projectId, networkId).execute();

        List<String> testIds = new ArrayList<>();
        StringBuilder idsFilter = new StringBuilder();
        for (String id : testIds) {
            if (idsFilter.length() > 0) {
                idsFilter.append(" OR ");
            }
            idsFilter.append("id = " + id);
        }
        FirewallList tags = compute.firewalls().list(projectId)
                .setFilter(idsFilter.toString())
                .setFields("items.targetTags").execute();

        List<String> ddd = tags.getItems().stream()
                .filter(f -> f.getTargetTags() != null)
                .flatMap(f -> f.getTargetTags().stream())
                .distinct()
                .collect(Collectors.toList());

        FirewallList firewallList = compute.firewalls().list(projectId)
                .setFilter("network=\"" + network.getSelfLink() + "\"")
                .setFields("items.id, items.direction, items.name, items.targetTags").execute();

        Map<String, List<FirewallRule>> tagsMap = new HashMap<>();
        List<FirewallRule> ingressList = new ArrayList<>();
        List<FirewallRule> egressList = new ArrayList<>();
        if (!firewallList.isEmpty()) {
            for (Firewall firewall : firewallList.getItems()) {
                if (firewall.getTargetTags() != null) {
                    FirewallRule tag = new FirewallRule();
                    tag.setId(firewall.getId().toString());
                    tag.setName(firewall.getName());
                    tag.setTags(firewall.getTargetTags());
                    if (firewall.getDirection().equals(DirectionType.INGRESS.name())) {
                        ingressList.add(tag);
                    } else {
                        egressList.add(tag);
                    }
                }
            }
        }

        tagsMap.put("ingress", ingressList);
        tagsMap.put("egress", egressList);

        return tagsMap;
    }

    /**
     * <pre>
     * operationId 해당하는 ConversionTask를 취소한다.
     * </pre>
     *
     * @param operationId  the operation id
     * @param resourceType the resource type
     * @param location     the location
     *
     * @throws Exception the io exception
     */
    public void cancelOperationTask(String operationId, ResourceType resourceType, String location) throws Exception {
        switch (resourceType) {
            case GLOBAL:
                compute.globalOperations().delete(projectId, operationId).execute();
                break;
            case REGIONAL:
                compute.regionOperations().get(projectId, location, operationId).execute();
                break;
            case ZONAL:
                compute.zoneOperations().get(projectId, location, operationId).execute();
                break;
        }
    }

    /**
     * Create machine image operation.
     *
     * @param migration the migration
     *
     * @return the operation
     */
    public Operation createMachineImage(MigrationProcessDto migration) throws Exception {
        try {
            String machineImageName = migration.getInstanceName();
            String response = compute.machineImages().list(projectId)
                    .setFilter("name=" + machineImageName).setFields("items.name").setAlt("json").execute().toString();

            if (!response.equals("{}")) {
                machineImageName = machineImageName + "-" + DATE_FORMAT.format(new Date());
            }

            MachineImage requestBody = new MachineImage();
            requestBody.setName(machineImageName);
            requestBody.setSourceInstance("projects/" + projectId + "/zones/" + migration.getAvailabilityZone() + "/instances/" + migration.getInstanceName());
            return compute.machineImages().insert(projectId, requestBody).execute();
        } catch (Exception e) {
            log.debug("Unhandled exception occurred while execute create machine image. [Reason] : ", e.getMessage());
            throw e;
        }
    }

    /**
     * Gets service account client email.
     *
     * @return the service account client email
     */
    public String getServiceAccountClientEmail() {
        return clientEmail;
    }

    /**
     * Gets service account client id.
     *
     * @return the service account client id
     */
    public String getServiceAccountClientId() {
        return clientId;
    }

    /**
     * Sets iam policy.
     *
     * @param projectId the project id
     * @param zone      the zone
     * @param resouceId the resouce id
     * @param request   the request
     *
     * @throws Exception the io exception
     */
    public void setIamPolicy(String projectId, String zone, String resouceId, ZoneSetPolicyRequest request) throws Exception {
        Policy policy = compute.instances().setIamPolicy(projectId, zone, resouceId, request).execute();
    }

    /**
     * Gets machine image.
     *
     * @param projectId the project id
     * @param imageId   the image id
     *
     * @return the machine image
     *
     * @throws Exception the io exception
     */
    public MachineImage getMachineImage(String projectId, String imageId) throws Exception {
        return compute.machineImages().get(projectId, imageId).execute();
    }

    /**
     * Gets disk.
     *
     * @param projectId the project id
     * @param zoneName  the zone name
     * @param diskName  the disk name
     *
     * @return the disk
     *
     * @throws Exception the io exception
     */
    public Disk getDisk(String projectId, String zoneName, String diskName) throws Exception {
        return compute.disks().get(projectId, zoneName, diskName).execute();
    }

    /**
     * Create disks operation.
     *
     * @param gcpVolume the gcp volume
     *
     * @return the string
     */
    public Operation createDisk(String bucketName, GCPVolume gcpVolume) throws Exception {
        try {
            String diskName = bucketName + "-"
                    + gcpVolume.getMigrationId() + "-"
                    + gcpVolume.getVol().getVolumeId() + "-"
                    + DATE_FORMAT.format(new Date());
            Disk requestBody = GCPRequestGenerator.DiskRequest.generateDiskRequest(gcpVolume, diskName);
            Operation response = createDisk(projectId, gcpVolume.getZone(), requestBody);
            return response;
        } catch (Exception e) {
            log.debug("[GCP] Create disk failed. [Reason] : ", e.getMessage());
            throw e;
        }
    }

    /**
     * <pre>
     * 신규 Disk를 생성한다.
     * </pre>
     *
     * @param projectId the project id
     * @param zone      the zone
     * @param disk      the disk
     *
     * @return operation operation
     *
     * @throws Exception the io exception
     */
    public Operation createDisk(String projectId, String zone, Disk disk) throws Exception {
        return compute.disks().insert(projectId, zone, disk).execute();
    }

    private Operation attachedDisk(String projectId, String availabilityZone, String instanceId, AttachedDisk requestBody) throws Exception {
        return compute.instances().attachDisk(projectId, availabilityZone, instanceId, requestBody).execute();
    }

    /**
     * Attached disk.
     *
     * @param migration    the migration
     * @param gcpVolumeMap the gcp volume map
     */
    public void attachedDisk(MigrationProcessDto migration, Map<Long, GCPVolume> gcpVolumeMap) {
        try {
            for (MigrationProcessDto.Volume volume : migration.getVolumes()) {
                if ("N".equals(volume.getRootYn())) {
                    AttachedDisk requestBody = GCPRequestGenerator.DiskRequest.generateAttachedDiskRequest(gcpVolumeMap.get(volume.getMigrationVolumeId()));
                    Operation response = attachedDisk(projectId, migration.getAvailabilityZone(), migration.getInstanceId(), requestBody);
                    gcpVolumeMap.get(volume.getMigrationVolumeId()).setDiskUrl(response.getTargetLink());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete disk operation.
     *
     * @param projectId the project id
     * @param zone      the zone
     * @param disk      the disk
     *
     * @return the operation
     *
     * @throws Exception the io exception
     */
    public Operation deleteDisk(String projectId, String zone, String disk) throws Exception {
        return compute.disks().delete(projectId, zone, disk).execute();
    }

    /**
     * Gets attached disks.
     *
     * @param migration    the migration
     * @param gcpVolumeMap the gcp volume map
     *
     * @return the attached disks
     */
    public List<AttachedDisk> getAttachedDisks(MigrationProcessDto migration, Map<Long, GCPVolume> gcpVolumeMap) {
        List<AttachedDisk> attachedDiskList = new ArrayList<>();
        for (MigrationProcessDto.Volume volume : migration.getVolumes()) {
            if (gcpVolumeMap.get(volume.getMigrationVolumeId()) != null) {
                AttachedDisk disk = GCPRequestGenerator.DiskRequest.generateAttachedDiskRequest(gcpVolumeMap.get(volume.getMigrationVolumeId()));
                // disk.setAutoDelete(true);

                attachedDiskList.add(disk);
            }
        }
        return attachedDiskList;
    }

    /**
     * Gets net interfaces.
     *
     * @param migration the migration
     *
     * @return the net interfaces
     *
     * @throws Exception the io exception
     */
    public List<NetworkInterface> getNetInterfaces(MigrationProcessDto migration) throws Exception {
        List<NetworkInterface> netList = new ArrayList<>();

        SubnetworkList subnetList = compute.subnetworks().list(migration.getGcpProjectId(), migration.getRegion()).execute();
        NetworkList networkList = compute.networks().list(migration.getGcpProjectId()).execute();

        Network network = networkList.getItems()
                .stream()
                .filter(nw -> migration.getVpcId().equals(nw.getId().toString()))
                .findFirst()
                .orElse(null);

        Subnetwork subnetwork = subnetList.getItems()
                .stream()
                .filter(sn -> migration.getSubnetId().equals(sn.getId().toString()))
                .findFirst()
                .orElse(null);


        NetworkInterface net = new NetworkInterface();
        net.setNetwork("global/networks/" + network.getName());
        net.setSubnetwork("projects/" + migration.getGcpProjectId() + "/regions/" + migration.getRegion() + "/subnetworks/" + subnetwork.getName());
        if (migration.getPrivateIp() != null && migration.getPrivateIp().length() > 1) {
            net.setNetworkIP(migration.getPrivateIp());
        }

        netList.add(net);
        return netList;
    }

    private void existInstanceCheck(MigrationProcessDto migration) throws Exception {
        try {
            String curInstanceName = migration.getInstanceName();
            String response = compute.instances().aggregatedList(migration.getGcpProjectId())
                    .setFilter("name=" + curInstanceName).setFields("items.*.instances.name").setAlt("json").execute().toString();

            log.debug("Result check find instances in GCE : [{}]", response);
            if (!response.equals("{}")) {
                migration.setInstanceName(curInstanceName + "-" + DATE_FORMAT.format(new Date()));
                log.debug("Change instance name : [{}]", migration.getInstanceName());
            }

        } catch (Exception e) {
            log.debug("Unhandled exception occurred while execute check exist GCP instance. [Reason] : ", e.getMessage());
            throw e;
        }
    }

    /**
     * <pre>
     * 신규 VM Instance를 생성한다.
     * </pre>
     *
     * @param projectId the project id
     * @param zone      the zone
     * @param instance  the instance
     *
     * @return operation operation
     *
     * @throws Exception the io exception
     */
    public Operation createVMInstance(String projectId, String zone, Instance instance) throws Exception {
        return compute.instances().insert(projectId, zone, instance).execute();
    }

    /**
     * Sets firewalls.
     *
     * @param migration the migration
     */
    public void setFirewalls(MigrationProcessDto migration) throws Exception {
        try {
            if (migration.getSecurityGroupIds() != null) {
                StringBuilder idsFilter = new StringBuilder();
                for (String id : migration.getSecurityGroupIds()) {
                    if (idsFilter.length() > 0) {
                        idsFilter.append(" OR ");
                    }
                    idsFilter.append("id = " + id);
                }

                FirewallList firewallList = compute.firewalls().list(projectId)
                        .setFilter(idsFilter.toString())
                        .setFields("items.targetTags").execute();

                List<String> tagList = firewallList.getItems().stream()
                        .filter(f -> f.getTargetTags() != null)
                        .flatMap(f -> f.getTargetTags().stream())
                        .distinct()
                        .collect(Collectors.toList());

                Instance instance = getInstance(migration);
                Tags tags = new Tags();
                tags.setFingerprint(instance.getTags().getFingerprint());
                tags.setItems(tagList);
                compute.instances().setTags(projectId, migration.getAvailabilityZone(), migration.getInstanceName(), tags).execute();
            }
        } catch (Exception e) {
            log.debug("Unhandled exception occurred while execute set firewalls. [Reason] : ", e.getMessage());
            throw e;
        }
    }

    /**
     * Sets labels.
     *
     * @param migration the migration
     *
     * @return the labels
     */
    public void setLabels(MigrationProcessDto migration) throws Exception {
        Instance instance = getInstance(migration);
        Map<String, String> labelMap = migration.getTags().stream().collect(Collectors.toMap(tag -> tag.getTagName().toLowerCase(), tag -> tag.getTagValue().toLowerCase()));
        InstancesSetLabelsRequest request = new InstancesSetLabelsRequest();
        request.setLabelFingerprint(instance.getLabelFingerprint());
        request.setLabels(labelMap);

        try {
            compute.instances().setLabels(projectId, migration.getAvailabilityZone(), migration.getInstanceName(), request).execute();
        } catch (Exception e) {
            log.debug("Unhandled exception occurred while execute set label. [Reason] : ", e.getMessage());
            throw e;
        }
    }

    /**
     * Gets instance.
     *
     * @param migration the migration
     *
     * @return the instance
     *
     * @throws Exception the io exception
     */
    public Instance getInstance(MigrationProcessDto migration) throws Exception {
        try {
            return getInstance(migration.getAvailabilityZone(), migration.getInstanceName());
        } catch (Exception e) {
            log.debug("Unhandled exception occurred while get instance. [Reason] : ", e.getMessage());
            throw e;
        }
    }

    /**
     * Gets instance.
     *
     * @param zone         the zone
     * @param instanceName the instance name
     *
     * @return the instance
     *
     * @throws Exception the io exception
     */
    public Instance getInstance(String zone, String instanceName) throws Exception {
        return compute.instances().get(projectId, zone, instanceName).execute();
    }

    /**
     * Run instances operation.
     *
     * @param migration    the migration
     * @param gcpVolumeMap the gcp volume map
     * @param metadata     the metadata
     *
     * @return the operation
     *
     * @throws Exception the io exception
     */
    public Operation runInstances(MigrationProcessDto migration, Map<Long, GCPVolume> gcpVolumeMap, Metadata metadata) throws Exception {
        try {
            List<AttachedDisk> attachedDisks = getAttachedDisks(migration, gcpVolumeMap);
            List<NetworkInterface> netInterfaces = getNetInterfaces(migration);

            /*
             *  Check GCE VMInstance's name.
             * */
            log.debug("Check instance name : {}", migration.getInstanceName());
            existInstanceCheck(migration);

            Instance instance = GCPRequestGenerator.InstanceRequest.generateInstanceRequest(migration);
            instance.setDisks(attachedDisks);
            instance.setNetworkInterfaces(netInterfaces);
            if (metadata != null) {
                instance.setMetadata(metadata);
            }

            log.debug("[GCP] Migration [{}] instance info : {}", migration.getInventoryProcessId(), instance.toString());

            Operation response = createVMInstance(projectId, migration.getAvailabilityZone(), instance);
            waitingOperation(response, ResourceType.ZONAL, migration.getAvailabilityZone());

            setFirewalls(migration);
            setLabels(migration);

            return response;
        } catch (Exception e) {
            log.debug("Unhandled exception occurred while execute create GCP instance. [Reason] : ", e.getMessage());
            throw e;
        }
    }

    /**
     * Add access configs.
     *
     * @param migration the migration
     */
    public void addAccessConfigs(MigrationProcessDto migration) throws Exception {
        Instance instance = getInstance(migration);
        if (instance != null) {
            /* External Ip Config*/
            if ("Y".equals(migration.getEnableEipYn())) {
                try {
                    Operation response = createStaticIp(migration);

                    Address externalIp = compute.addresses().get(projectId, migration.getRegion(), response.getTargetId().toString()).execute();
                    AccessConfig externalIpRequest = new AccessConfig();
                    externalIpRequest.setName(externalIp.getName());
                    externalIpRequest.setNatIP(externalIp.getAddress());

                    log.debug("Create static ip {}, {}", externalIpRequest.getName(), externalIpRequest.getNatIP());
                    addAccessConfig(migration, externalIpRequest);

                    migration.setPublicIp(externalIpRequest.getNatIP());
                } catch (Exception e) {
                    log.debug("Unhandled exception occurred while set static ip address. [Reason] : ", e.getMessage());
                    throw e;
                }
            }
        }
    }

    /**
     * Add access config operation.
     *
     * @param migration   the migration
     * @param requestBody the request body
     *
     * @return the operation
     *
     * @throws Exception the exception
     */
    public Operation addAccessConfig(MigrationProcessDto migration, AccessConfig requestBody) throws Exception {
        Operation response = null;
        try {
            response = compute.instances().addAccessConfig(projectId, migration.getAvailabilityZone(),
                    migration.getInstanceName(), "nic0", requestBody).execute();
            waitingOperation(response, ResourceType.ZONAL, migration.getAvailabilityZone());
            Thread.sleep(5000);
        } catch (Exception e) {
            log.debug("Add access config failed. [Reason] : ", e.getMessage());
            throw e;
        }

        return response;
    }

    /**
     * Create static ip operation.
     *
     * @param migration the migration
     *
     * @return the operation
     *
     * @throws Exception the exception
     */
    public Operation createStaticIp(MigrationProcessDto migration) throws Exception {
        String instanceName = migration.getInstanceName();
        String staticIpReponse = compute.addresses().list(projectId, migration.getRegion())
                .setFilter("name=" + instanceName).setFields("items.name").setAlt("json").execute().toString();

        if (!staticIpReponse.equals("{}")) {
            instanceName = instanceName + "-" + DATE_FORMAT.format(new Date());
        }

        Address requestBody = new Address();
        requestBody.setName(instanceName);
        requestBody.setRegion(migration.getRegion());

        Operation response = compute.addresses().insert(projectId, migration.getRegion(), requestBody).execute();
        waitingOperation(response, ResourceType.REGIONAL, migration.getRegion());
        return response;
    }

    /**
     * Create disk image operation.
     *
     * @param projectId the project id
     * @param image     the image
     *
     * @return the operation
     *
     * @throws Exception the io exception
     */
    public Operation createDiskImage(String projectId, Image image) throws Exception {
        return compute.images().insert(projectId, image).execute();
    }

    /**
     * Create disk image operation.
     *
     * @param bucketName the bucket name
     * @param gcpVolume  the gcp volume
     *
     * @return the string
     */
    public Operation createDiskImage(String bucketName, GCPVolume gcpVolume) throws Exception {
        try {
            String diskImageName = bucketName + "-"
                    + gcpVolume.getMigrationId() + "-"
                    + gcpVolume.getVol().getVolumeId() + "-"
                    + DATE_FORMAT.format(new Date());
            Image.RawDisk rawDisk = GCPRequestGenerator.DiskRequest.generateRawDiskRequest(bucketName, gcpVolume.getMigrationId(), gcpVolume.getTarGz().getName());
            Image requestBody = GCPRequestGenerator.ImageRequest.generateCreateDiskImageRequest(rawDisk, diskImageName);
            Operation response = createDiskImage(projectId, requestBody);
            gcpVolume.getVol().setTaskId(response.getId().toString());

            gcpVolume.setDiskImage("global/images/" + requestBody.getName());
            gcpVolume.setOperation(response);
            gcpVolume.setResourceType(ResourceType.GLOBAL);
            return response;
        } catch (Exception e) {
            log.debug("[GCP] Create disk image failed. [Reason] : ", e.getMessage());
            throw e;
        }
    }

    /**
     * Delete instance operation.
     *
     * @param migration the migration
     *
     * @return the operation
     *
     * @throws Exception the exception
     */
    public Operation terminateInstance(MigrationProcessDto migration) throws Exception {
        Operation response = compute.instances().delete(projectId, migration.getAvailabilityZone(), migration.getInstanceId()).execute();
        waitingOperation(response, ResourceType.ZONAL, migration.getAvailabilityZone());

        if ("Y".equals(migration.getEnableEipYn())) {
            Instance instance = getInstance(migration);
            List<AccessConfig> accessConfigs = instance.getNetworkInterfaces().get(0).getAccessConfigs();
            for (AccessConfig config : accessConfigs) {
                compute.addresses().delete(projectId, migration.getRegion(), config.getName());
            }
        }
        return response;
    }
}
//end of ComputeClient.java