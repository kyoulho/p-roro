package io.playce.roro.db.asmt.mariadb.service;

import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.db.asmt.mariadb.dto.Database;
import io.playce.roro.db.asmt.mariadb.dto.Instance;
import io.playce.roro.db.asmt.mariadb.dto.MariaDbDto;
import io.playce.roro.db.asmt.mariadb.dto.Table;
import io.playce.roro.db.asmt.mariadb.mapper.MariaDbMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MariaDbService {

    private final MariaDbMapper mariaDbMapper;

    public MariaDbDto getAssessment(DataSource dataSource, DatabaseDto databaseDto) {
        Map<String, String> variableMap = mariaDbMapper.getVariables(dataSource);
        Instance instance = getInstance(dataSource, variableMap);

        // 버전에 따라 다음과 같은 형태의 버전이 표시됨
        // 10.3.28-MariaDB
        // 10.6.8-MariaDB-1:10.6.8+maria~focal
        // 10.9.3-MariaDB-1:10.9.3+maria~ubu2204
        String versionStr = instance.getVersion().substring(0, instance.getVersion().indexOf("-"));
        final float version = Float.parseFloat(versionStr.substring(0, versionStr.lastIndexOf(".")));

        List<Database> databases = null;

        try {
            databases = mariaDbMapper.getDatabases(dataSource, dataSource.getConnection().getCatalog(), databaseDto.getAllScanYn());
        } catch (SQLException e) {
            log.error("SQLException occurred while getDatabases.", e);
        }

        if (CollectionUtils.isNotEmpty(databases)) {
            for (Database database : databases) {
                database.setTableDataUsages(mariaDbMapper.getTableDataUsage(dataSource, database.getName()));
                database.setIndexUsages(mariaDbMapper.getIndexUsages(dataSource, database.getName(), version));
                database.setTables(getTable(dataSource, database.getName()));
                database.setViews(mariaDbMapper.getViews(dataSource, database.getName()));
                database.setIndexes(mariaDbMapper.getIndex(dataSource, database.getName()));
                database.setProcedures(mariaDbMapper.getProcedure(dataSource, database.getName()));
                database.setFunctions(mariaDbMapper.getFunction(dataSource, database.getName()));
                database.setTriggers(mariaDbMapper.getTrigger(dataSource, database.getName()));
                database.setEvents(mariaDbMapper.getEvent(dataSource, database.getName()));
            }
        }

        instance.setDbSizeMb(mariaDbMapper.getDbSizeMb(databases));

        return MariaDbDto.builder()
                .instance(instance)
                .users(mariaDbMapper.getUser(dataSource, version))
                .dbLinks(mariaDbMapper.getDbLink(dataSource))
                .databases(databases)
                .build();
    }

    private Instance getInstance(DataSource dataSource, Map<String, String> variableMap) {
        Instance instance = new Instance();

        instance.setHostName(variableMap.get("hostname"));
        instance.setVersion(variableMap.get("version"));
        instance.setStartupTime(mariaDbMapper.getStartUpTime(dataSource));

        return instance;
    }

    private List<Table> getTable(DataSource dataSource, String databaseName) {
        List<Table> tables = mariaDbMapper.getTable(dataSource, databaseName);

        for (Table table : tables) {
            // Database 이름에 특수문자 및 예약어 등이 포함된 경우 쿼리 실행 시 에러가 발생. 따라서 ``(Back Quote) 로 묶어준다.
            table.setDdlScript(mariaDbMapper.getTableScript(dataSource, "`" + table.getDatabaseName() + "`." + table.getTableName()));
        }

        return tables;
    }
}
