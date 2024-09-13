package io.playce.roro.db.asmt.sybase.service;

import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.db.asmt.sybase.dto.Database;
import io.playce.roro.db.asmt.sybase.dto.Instance;
import io.playce.roro.db.asmt.sybase.dto.SybaseDto;
import io.playce.roro.db.asmt.sybase.mapper.SybaseMapper;
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
public class SybaseService {

    private final SybaseMapper sybaseMapper;

    public SybaseDto getAssessment(DataSource dataSource, DatabaseDto databaseDto) {

        List<Database> databases = null;
        try {
            databases = sybaseMapper.getDatabases(dataSource, dataSource.getConnection().getCatalog(), databaseDto.getAllScanYn());
        } catch (SQLException e) {
            log.error("SQLException occurred while getDatabases.", e);
        }

        if (CollectionUtils.isNotEmpty(databases)) {
            for (Database database : databases) {
                database.setObjectSummary(sybaseMapper.getObjectSummary(dataSource, database.getName()));
                database.setTables(sybaseMapper.getTables(dataSource, database.getName()));
                database.setViews(sybaseMapper.getViews(dataSource, database.getName()));
                database.setIndexes(sybaseMapper.getIndexes(dataSource, database.getTables()));
                database.setProcedures(sybaseMapper.getProcedures(dataSource, database.getName()));
                database.setFunctions(sybaseMapper.getFunctions(dataSource, database.getName()));
                database.setTriggers(sybaseMapper.getTriggers(dataSource, database.getName()));
            }
        }

        Instance instance = sybaseMapper.getInstance(dataSource);

        if (CollectionUtils.isNotEmpty(databases)) {
            instance.setDbSizeMb(databases.stream().mapToLong(Database::getDbSize).sum());
        }

        return SybaseDto.builder()
                .instance(instance)
                .servers(sybaseMapper.getServers(dataSource))
                .memories(sybaseMapper.getMemories(dataSource))
                .devices(sybaseMapper.getDevices(dataSource))
                .segments(sybaseMapper.getSegment(dataSource))
                .users(sybaseMapper.getUsers(dataSource))
                .jobs(sybaseMapper.getJobs(dataSource))
                .databases(databases)
                .build();
    }

}
