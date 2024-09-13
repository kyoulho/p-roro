package io.playce.roro.api.domain.inventory.service.helper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.Process;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.*;
import io.playce.roro.asmt.windows.impl.WindowsAssessment;
import io.playce.roro.jpa.entity.ServerSummary;
import io.playce.roro.svr.asmt.dto.result.WindowsAssessmentResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.*;

import static io.playce.roro.common.util.JsonUtil.isJsonArray;

@Component
@RequiredArgsConstructor
public class WindowsAssessmentHelper {

    private final WindowsAssessment WINDOWSAssessment;

    // 수동업로드는 서버에 접속할 수 없기 때문에 Middleware 검출이나 ThirdParty 솔루션을 검출하지 못한다.
    @SneakyThrows
    public WindowsAssessmentResult getAssessment(JSONObject jsonObject) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        String systemInformation = jsonObject.get("systemInformation").toString();
        String environment = jsonObject.get("environment").toString();
        String cpu = jsonObject.get("cpu").toString();
        String network = jsonObject.get("networks").toString();
        String dns = jsonObject.get("dns").toString();
        String route = jsonObject.get("routes").toString();
        String port = jsonObject.get("ports").toString();
        String firewall = jsonObject.get("firewalls").toString();
        String disk = jsonObject.get("disks").toString();
        String installedSoftware = jsonObject.get("installedSoftware").toString();
        String process = jsonObject.get("process").toString();
        String service = jsonObject.get("services").toString();
        String timezone = jsonObject.get("timezone").toString();
        String schedule = jsonObject.get("schedules").toString();
        String localUser = jsonObject.get("localUsers").toString();
        String localGroupUsers = jsonObject.get("localGroupUsers").toString();

        WindowsResult windowsResult = WindowsResult.builder()
                .systemInformation(getSystemInformation(objectMapper, systemInformation))
                .environment(getEnvironment(objectMapper, environment))
                .cpu(getCpu(objectMapper, cpu))
                .networks(getNetworks(objectMapper, network))
                .dns(getDns(objectMapper, dns))
                .routes(getRoutes(objectMapper, route))
                .ports(getPorts(objectMapper, port))
                .firewalls(getFirewalls(objectMapper, firewall))
                .disks(getDisks(objectMapper, disk))
                .installedSoftware(getInstalledSoftware(objectMapper, installedSoftware))
                .process(getProcess(objectMapper, process))
                .services(getServices(objectMapper, service))
                .timezone(getTimezone(objectMapper, timezone))
                .schedules(getSchedules(objectMapper, schedule))
                .localUsers(getLocalUsers(objectMapper, localUser))
                .localGroupUsers(getLocalGroupUsers(objectMapper, localGroupUsers))
                .build();

        WindowsAssessmentResult windowsAssessmentResult =
                WINDOWSAssessment.convertWindowsResultToWindowsAssessmentResult(windowsResult, objectMapper);

        return windowsAssessmentResult;
    }

    @SneakyThrows
    private SystemInformation getSystemInformation(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            return objectMapper.readValue(result, SystemInformation.class);
        }

        return new SystemInformation();
    }

    @SneakyThrows
    private Environment getEnvironment(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            return objectMapper.readValue(result, Environment.class);
        }

        return new Environment();
    }

    @SneakyThrows
    private Cpu getCpu(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            Cpu cpu;

            if (isJsonArray(result)) {
                List<Cpu> tempCpu = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Cpu[].class)));
                cpu = tempCpu.get(0);
                cpu.setSockets(tempCpu.size() + "");
            } else {
                cpu = (objectMapper.readValue(result, Cpu.class));
                cpu.setSockets("1");
            }
            return cpu;
        }

        return new Cpu();
    }

    @SneakyThrows
    private List<Network> getNetworks(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            List<Network> networks = new ArrayList<>();

            if (isJsonArray(result)) {
                networks = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Network[].class)));
            } else {
                networks.add(objectMapper.readValue(result, Network.class));
            }

            return networks;
        }

        return new ArrayList<>();
    }

    @SneakyThrows
    private List<Dns> getDns(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            List<Dns> dns = new ArrayList<>();

            if (isJsonArray(result)) {
                dns = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Dns[].class)));
            } else {
                dns.add(objectMapper.readValue(result, Dns.class));
            }

            return dns;
        }

        return new ArrayList<>();
    }

    @SneakyThrows
    private List<Route> getRoutes(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            List<Route> routes = new ArrayList<>();

            if (isJsonArray(result)) {
                routes = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Route[].class)));
            } else {
                routes.add(objectMapper.readValue(result, Route.class));
            }

            return routes;
        }

        return new ArrayList<>();
    }

    @SneakyThrows
    private List<Port> getPorts(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            List<Port> ports = new ArrayList<>();

            if (isJsonArray(result)) {
                ports = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Port[].class)));
            } else {
                ports.add(objectMapper.readValue(result, Port.class));
            }

            for (Port tempPort : ports) {
                tempPort.setType("Manual");
            }

            return ports;
        }

        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private List<Firewall> getFirewalls(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            List<Firewall> firewalls = new ArrayList<>();

            if (isJsonArray(result)) {
                firewalls = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Firewall[].class)));
            } else {
                firewalls.add(objectMapper.readValue(result, Firewall.class));
            }

            for (Firewall firewall : firewalls) {
                if (firewall.getTempLocalPort() instanceof Map) {
                    Map<String, List<String>> localPortMap = (Map<String, List<String>>) firewall.getTempLocalPort();
                    firewall.setLocalPort(localPortMap.get("value"));
                } else {
                    if (firewall.getTempLocalPort() != null) {
                        firewall.setLocalPort(Collections.singletonList(firewall.getTempLocalPort().toString()));
                    }
                }

                if (firewall.getTempRemotePort() instanceof Map) {
                    Map<String, List<String>> remotePortMap = (Map<String, List<String>>) firewall.getTempRemotePort();
                    firewall.setRemotePort(remotePortMap.get("value"));
                } else {
                    if (firewall.getTempRemotePort() != null) {
                        firewall.setRemotePort(Collections.singletonList(firewall.getTempRemotePort().toString()));
                    }
                }

                if (firewall.getTempRemoteAddress() instanceof Map) {
                    Map<String, List<String>> remoteAddressMap = (Map<String, List<String>>) firewall.getTempRemoteAddress();
                    firewall.setRemoteAddress(remoteAddressMap.get("value"));
                } else {
                    if (firewall.getTempRemoteAddress() != null) {
                        firewall.setRemoteAddress(Collections.singletonList(firewall.getTempRemoteAddress().toString()));
                    }
                }
            }

            return firewalls;
        }

        return new ArrayList<>();
    }

    @SneakyThrows
    private List<Disk> getDisks(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            List<Disk> disks = new ArrayList<>();

            if (isJsonArray(result)) {
                disks = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Disk[].class)));
            } else {
                disks.add(objectMapper.readValue(result, Disk.class));
            }

            return disks;
        }

        return new ArrayList<>();
    }

    @SneakyThrows
    private List<InstalledSoftware> getInstalledSoftware(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            List<InstalledSoftware> installedSoftwares = new ArrayList<>();

            if (isJsonArray(result)) {
                installedSoftwares = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, InstalledSoftware[].class)));
            } else {
                installedSoftwares.add(objectMapper.readValue(result, InstalledSoftware.class));
            }

            return installedSoftwares;
        }

        return new ArrayList<>();
    }

    @SneakyThrows
    private List<Process> getProcess(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            List<Process> processes = new ArrayList<>();

            if (isJsonArray(result)) {
                processes = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Process[].class)));
            } else {
                processes.add(objectMapper.readValue(result, Process.class));
            }

            return processes;
        }

        return new ArrayList<>();
    }

    @SneakyThrows
    private List<WindowsAssessmentDto.Service> getServices(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            List<WindowsAssessmentDto.Service> services = new ArrayList<>();

            if (isJsonArray(result)) {
                services = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, WindowsAssessmentDto.Service[].class)));
            } else {
                services.add(objectMapper.readValue(result, WindowsAssessmentDto.Service.class));
            }

            return services;
        }

        return new ArrayList<>();
    }

    @SneakyThrows
    private Timezone getTimezone(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            return objectMapper.readValue(result, Timezone.class);
        }

        return new Timezone();
    }

    @SneakyThrows
    private List<Schedule> getSchedules(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            List<Schedule> schedules = new ArrayList<>();

            if (isJsonArray(result)) {
                schedules = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Schedule[].class)));
            } else {
                schedules.add(objectMapper.readValue(result, Schedule.class));
            }

            return schedules;
        }

        return new ArrayList<>();
    }

    @SneakyThrows
    private List<LocalUser> getLocalUsers(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            List<LocalUser> localUsers = new ArrayList<>();

            if (isJsonArray(result)) {
                localUsers = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, LocalUser[].class)));
            } else {
                localUsers.add(objectMapper.readValue(result, LocalUser.class));
            }

            return localUsers;
        }

        return new ArrayList<>();
    }

    @SneakyThrows
    private List<LocalGroupUser> getLocalGroupUsers(ObjectMapper objectMapper, String result) {
        if (StringUtils.isNotEmpty(result)) {
            List<LocalGroupUser> localGroupUsers = new ArrayList<>();

            if (isJsonArray(result)) {
                localGroupUsers = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, LocalGroupUser[].class)));
            } else {
                localGroupUsers.add(objectMapper.readValue(result, LocalGroupUser.class));
            }

            return localGroupUsers;
        }

        return new ArrayList<>();
    }

    @SneakyThrows
    public static ServerSummary convertServerSummary(Object summaryObject) {
        WindowsResult windowsResult = (WindowsResult) summaryObject;

        ServerSummary serverSummary = new ServerSummary();
        serverSummary.setHostName(windowsResult.getSystemInformation().getHostName());
        serverSummary.setVendorName(windowsResult.getSystemInformation().getSystemManufacturer());
        serverSummary.setCpuCount(Integer.parseInt(windowsResult.getCpu().getCores()) * Integer.parseInt(windowsResult.getCpu().getSockets()));
        serverSummary.setCpuCoreCount(Integer.parseInt(windowsResult.getCpu().getCores()));
        serverSummary.setCpuSocketCount(Integer.parseInt(windowsResult.getCpu().getSockets()));
        serverSummary.setCpuArchitecture(windowsResult.getSystemInformation().getSystemType());
        serverSummary.setOsKernel(StringUtils.substringBefore(windowsResult.getSystemInformation().getOsVersion(), " "));
        serverSummary.setOsName(windowsResult.getSystemInformation().getOsName());
        serverSummary.setOsFamily(windowsResult.getSystemInformation().getOsManufacturer());
        serverSummary.setMemSize(Integer.parseInt(StringUtils.substringBefore(windowsResult.getSystemInformation().getTotalPhysicalMemory().replaceAll(",", ""), " ")));
        serverSummary.setSwapSize(Integer.parseInt(StringUtils.substringBefore(windowsResult.getSystemInformation().getVirtualMemoryMaxSize().replaceAll(",", ""), " ")));

        return serverSummary;
    }

}
