package io.playce.roro.db.asmt.mysql.service;

import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.db.asmt.mysql.dto.Database;
import io.playce.roro.db.asmt.mysql.dto.Instance;
import io.playce.roro.db.asmt.mysql.dto.MySqlDto;
import io.playce.roro.db.asmt.mysql.dto.Table;
import io.playce.roro.db.asmt.mysql.mapper.MySqlMapper;
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
public class MySqlService {

    private final MySqlMapper mySqlMapper;

    public MySqlDto getAssessment(final DataSource dataSource, DatabaseDto databaseDto) {
        Map<String, String> variableMap = mySqlMapper.getVariables(dataSource);
        Instance instance = getInstance(dataSource, variableMap);

        List<Database> databases = null;
        try {
            databases = mySqlMapper.getDatabases(dataSource, dataSource.getConnection().getCatalog(), databaseDto.getAllScanYn());
        } catch (SQLException e) {
            log.error("SQLException occurred while getDatabases.", e);
        }

        if (CollectionUtils.isNotEmpty(databases)) {
            for (Database database : databases) {
                database.setTableDataUsages(mySqlMapper.getTableDataUsage(dataSource, database.getName()));
                database.setIndexUsages(mySqlMapper.getIndexUsages(dataSource, database.getName()));
                database.setTables(getTable(dataSource, database.getName()));
                database.setViews(mySqlMapper.getViews(dataSource, database.getName()));
                database.setIndexes(mySqlMapper.getIndex(dataSource, database.getName()));
                database.setProcedures(mySqlMapper.getProcedure(dataSource, database.getName()));
                database.setFunctions(mySqlMapper.getFunction(dataSource, database.getName()));
                database.setTriggers(mySqlMapper.getTrigger(dataSource, database.getName()));
                database.setEvents(mySqlMapper.getEvent(dataSource, database.getName()));
            }
        }

        instance.setDbSizeMb(mySqlMapper.getDbSizeMb(databases));

        return MySqlDto.builder()
                .instance(instance)
                .users(mySqlMapper.getUser(dataSource))
                .dbLinks(mySqlMapper.getDbLink(dataSource))
                .databases(databases)
                .build();
    }

    private Instance getInstance(DataSource dataSource, Map<String, String> variableMap) {
        Instance instance = new Instance();

        instance.setHostName(variableMap.get("hostname"));
        instance.setVersion(variableMap.get("version"));
        if(instance.getVersion().startsWith("5.7") || instance.getVersion().startsWith("8")) {
            instance.setStartupTime(mySqlMapper.getStartUpTime2(dataSource));
        } else {
            instance.setStartupTime(mySqlMapper.getStartUpTime(dataSource));
        }

        return instance;
    }

    private List<Table> getTable(DataSource dataSource, String databaseName) {
        List<Table> tables = mySqlMapper.getTable(dataSource, databaseName);

        for (Table table : tables) {
            table.setDdlScript(mySqlMapper.getTableScript(dataSource, table.getDatabaseName() + "." + table.getTableName()));
        }

        return tables;
    }

}