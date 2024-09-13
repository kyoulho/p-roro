package io.playce.roro.db.asmt.mssql.service;

import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.db.asmt.mssql.dto.Database;
import io.playce.roro.db.asmt.mssql.dto.Instance;
import io.playce.roro.db.asmt.mssql.dto.MsSqlDto;
import io.playce.roro.db.asmt.mssql.mapper.MsSqlMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MsSqlService {

    private final MsSqlMapper msSqlMapper;

    public MsSqlDto getAssessment(DataSource dataSource, DatabaseDto databaseDto) {

        Instance instance = msSqlMapper.getInstances(dataSource);

        List<Database> databases = null;
        try {
            databases = msSqlMapper.getDatabases(dataSource, dataSource.getConnection().getCatalog(), databaseDto.getAllScanYn());
        } catch (SQLException e) {
            log.error("SQLException occurred while getDatabases.", e);
        }

        if (CollectionUtils.isNotEmpty(databases)) {
            for (Database database : databases) {
                database.setObjectSummaries(msSqlMapper.getObjectSummary(dataSource, database.getName()));
                database.setTables(msSqlMapper.getTables(dataSource, database.getName()));
                database.setViews(msSqlMapper.getViews(dataSource, database.getName()));
                database.setIndexes(msSqlMapper.getIndexes(dataSource, database.getName()));
                database.setProcedures(msSqlMapper.getProcedures(dataSource, database.getName()));
                database.setFunctions(msSqlMapper.getFunctions(dataSource, database.getName()));
                database.setQueues(msSqlMapper.getQueues(dataSource, database.getName()));
                database.setTriggers(msSqlMapper.getTriggers(dataSource, database.getName()));
                database.setSequences(msSqlMapper.getSequences(dataSource, database.getName(), instance.getProductVersion()));
                database.setSynonyms(msSqlMapper.getSynonyms(dataSource, database.getName()));

            }
        }

        instance.setDbSizeMb(msSqlMapper.getDbSizeMb(dataSource, databases));

        return MsSqlDto.builder()
                .instance(instance)
                .memories(msSqlMapper.getMemories(dataSource))
                .dataFiles(msSqlMapper.getDataFiles(dataSource))
                .users(msSqlMapper.getUsers(dataSource))
                .dbLinks(msSqlMapper.getDbLinks(dataSource))
                .databases(databases)
                .build();
    }

}
