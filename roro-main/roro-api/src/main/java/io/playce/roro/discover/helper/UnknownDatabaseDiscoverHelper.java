/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Hoon Oh       1월 26, 2022            First Draft.
 */
package io.playce.roro.discover.helper;

import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1006;
import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.discover.database.dto.UnknownDatabaseDto;
import io.playce.roro.jpa.entity.DiscoveredInstanceMaster;
import io.playce.roro.mybatis.domain.discovered.DiscoveredInstanceMapper;
import io.playce.roro.mybatis.domain.inventory.database.DatabaseMapper;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import io.playce.roro.svr.asmt.dto.common.network.ListenPort;
import io.playce.roro.svr.asmt.dto.common.network.PortList;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnknownDatabaseDiscoverHelper {
    private final String oracleProcessName = "tnslsnr"; // Oracle Listener
    private final String mysqlProcessName = "mysqld";
    private final String mariaDbPathName = "mariadb";
    private final String tiberoProcessName = "tblistener";
    private final List<String> sybaseProcessName = List.of("dataserver", "sqlsrvr");
    private final String mssqlProcessName = "sqlservr";
    private final String postgresqlProcessName = "postgres";

    private final DatabaseMapper databaseMapper;
    private final DiscoveredInstanceMapper discoveredInstanceMapper;
    private final DiscoveredInstanceManager discoveredInstanceManager;

    public Integer getDatabasePort(PortList portList, String pid) {

        List<String> ports = portList.getListen().stream()
                .filter(p -> p.getPid().equals(pid))
                .map(ListenPort::getPort)
                .collect(Collectors.toList());

        if (ports.size() >= 1) {
            return Integer.parseInt(ports.get(0));
        } else {
            return null;
        }
    }

    public void extract(InventoryProcessConnectionInfo connectionInfo, ServerAssessmentResult result) {

        List<UnknownDatabaseDto> unknownDatabaseDtos = getUnknownDatabase(result.getProcesses(), result.getPortList());

        for (UnknownDatabaseDto unknownDatabaseDto : unknownDatabaseDtos) {
            log.debug("unknownDatabaseDto : {}", unknownDatabaseDto);

            if (!checkDuplicateDatabase(connectionInfo, unknownDatabaseDto)) {
                if (checkDuplicateDiscoveredInstance(connectionInfo, unknownDatabaseDto)) {
                    registDiscoveredInstance(connectionInfo, unknownDatabaseDto);
                }
            } else {
                log.debug("Unknown database composed of address [{}] and port [{}] is already registered", unknownDatabaseDto.getConnectionUrl(), unknownDatabaseDto.getPort());
            }
        }
    }

    private synchronized void registDiscoveredInstance(InventoryProcessConnectionInfo connectionInfo, UnknownDatabaseDto unknownDatabaseDto) {
        DiscoveredInstanceMaster instance = new DiscoveredInstanceMaster();
        instance.setDiscoveredIpAddress(connectionInfo.getRepresentativeIpAddress());
        instance.setRegistDatetime(new Date());
        instance.setInventoryTypeCode(Domain1001.DBMS.name());
        instance.setInventoryRegistTypeCode(Domain1006.DISC.name());
        instance.setProjectId(connectionInfo.getProjectId());
        instance.setFinderInventoryId(connectionInfo.getInventoryId());
        instance.setDeleteYn(CommonConstants.NO);
        instance.setInventoryDetailTypeCode(unknownDatabaseDto.getType());
        // 서버에서 발견되었을 경우 DB이름은 알 수가 없다.
        instance.setDiscoveredDetailDivision(unknownDatabaseDto.getPort() + "|");

        discoveredInstanceManager.saveUnknownServer(instance);
    }

    private boolean checkDuplicateDiscoveredInstance(InventoryProcessConnectionInfo connectionInfo, UnknownDatabaseDto unknownDatabaseDto) {
        int count = discoveredInstanceMapper.selectDuplicatedDiscoveredDatabaseCount(connectionInfo.getProjectId(), connectionInfo.getRepresentativeIpAddress(), unknownDatabaseDto.getPort() + "|");

        log.debug("checkDuplicateDiscoveredInstance - projectId : {}, ip : {}, port : {}", connectionInfo.getProjectId(), connectionInfo.getRepresentativeIpAddress(), unknownDatabaseDto.getPort());
        log.debug("discovered duplicated Instance count : {}", count);

        return count == 0;
    }

    private boolean checkDuplicateDatabase(InventoryProcessConnectionInfo connectionInfo, UnknownDatabaseDto unknownDatabaseDto) {
        int count = databaseMapper.selectDuplicateDatabaseInventory(
                connectionInfo.getProjectId(), connectionInfo.getInventoryId(), unknownDatabaseDto.getPort());

        return count > 0;
    }

    private List<UnknownDatabaseDto> getUnknownDatabase(List<Process> processes, PortList portList) {
        List<UnknownDatabaseDto> unknownDatabaseDtos = new ArrayList<>();

        for (Process process : processes) {
            if (CollectionUtils.isNotEmpty(process.getCmd())) {
                try {
                    Domain1013 dbType = getDatabaseType(process);
                    if (dbType == null)
                        continue;

                    UnknownDatabaseDto unknownDatabaseDto = new UnknownDatabaseDto();
                    unknownDatabaseDto.setType(dbType.name());
                    unknownDatabaseDto.setPort(getDatabasePort(portList, process.getPid()));

                    log.debug("Process unknownDatabaseDto --> {}", unknownDatabaseDto);

                    if (StringUtils.isNotEmpty(unknownDatabaseDto.getType()) && unknownDatabaseDto.getPort() != null) {
                        unknownDatabaseDtos.add(unknownDatabaseDto);
                    }
                } catch (Exception e) {
                    log.error("Unhandled error occured [{}]", process, e);
                }
            }

        }
        return unknownDatabaseDtos;
    }

    private Domain1013 getDatabaseType(Process process) {
        // DB 마다 여러개의 프로세스를 사용할 수 있지만 Listening 하고 있는 Process는 하나이기 때문에
        // Listening 하고 있는 Process로 판단한다.
        if (process.getCmd().stream().anyMatch(x -> x.toLowerCase().contains(oracleProcessName))) {
            return Domain1013.ORACLE;
        } else if (process.getCmd().stream().anyMatch(x -> x.toLowerCase().contains(mysqlProcessName) && x.toLowerCase().contains(mariaDbPathName))) {
            // MariaDB, MySQL은 ProcessName이 같아서 추가적으로 Path에 mariadb 가 있는지 확인한다.
            return Domain1013.MARIADB;
        } else if (process.getCmd().stream().anyMatch(x -> x.toLowerCase().contains(mysqlProcessName))) {
            return Domain1013.MYSQL;
        } else if (process.getCmd().stream().anyMatch(x -> x.toLowerCase().contains(tiberoProcessName))) {
            return Domain1013.TIBERO;
        } else if (process.getCmd().stream().anyMatch(x -> isContainProcess(x.toLowerCase(), sybaseProcessName))) {
            return Domain1013.SYBASE;
        } else if (process.getCmd().stream().anyMatch(x -> x.toLowerCase().contains(mssqlProcessName))) {
            return Domain1013.MSSQL;
        } else if (process.getCmd().stream().anyMatch(x -> x.toLowerCase().contains(postgresqlProcessName))) {
            return Domain1013.POSTGRE;
        }
        return null;
    }

    private boolean isContainProcess(String command, List<String> compareDatabaseProcessName) {
        for (String processName : compareDatabaseProcessName) {
            if (command.contains(processName)) {
                return true;
            }
        }
        return false;
    }
}
//end of UnknownDatabaseDiscoverManager.java