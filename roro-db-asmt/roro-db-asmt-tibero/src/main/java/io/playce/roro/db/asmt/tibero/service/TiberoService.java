package io.playce.roro.db.asmt.tibero.service;

import io.opentracing.contrib.jdbc.ConnectionInfo;
import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.JdbcURLParser;
import io.playce.roro.db.asmt.tibero.dto.Database;
import io.playce.roro.db.asmt.tibero.dto.TiberoDto;
import io.playce.roro.db.asmt.tibero.mapper.TiberoMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class TiberoService {

    private final TiberoMapper tiberoMapper;

    public TiberoDto getAssessment(DataSource dataSource, DatabaseDto databaseDto) {

        List<Database> databases = null;
        try {
            ConnectionInfo connectionInfo = JdbcURLParser.getJdbcUrlConnectionInfo(databaseDto.getJdbcUrl());
            String databaseName = connectionInfo == null ? "" : connectionInfo.getDbInstance();

            if (StringUtils.isEmpty(databaseName)) {
                log.warn("Database is Empty. Check jdbcUrl database name.");
            }

            databases = tiberoMapper.getDatabases(dataSource, databaseName, databaseDto.getAllScanYn());
        } catch (Exception e) {
            log.error("SQLException occurred while getDatabases.", e);
        }

        if (CollectionUtils.isNotEmpty(databases)) {
            for (Database database : databases) {
                database.setObjectSummary(tiberoMapper.getObjectSummary(dataSource, database.getName()));
                database.setTables(tiberoMapper.getTables(dataSource, database.getName()));
                database.setViews(tiberoMapper.getViews(dataSource, database.getName()));
                database.setMaterializedViews(tiberoMapper.getMaterializedViews(dataSource, database.getName()));
                database.setIndexes(tiberoMapper.getIndexes(dataSource, database.getName()));
                database.setProcedures(tiberoMapper.getProcedures(dataSource, database.getName()));
                database.setPackages(tiberoMapper.getPackages(dataSource, database.getName()));
                database.setPackageBodies(tiberoMapper.getPackageBodies(dataSource, database.getName()));
                database.setFunctions(tiberoMapper.getFunctions(dataSource, database.getName()));
                database.setQueues(tiberoMapper.getQueues(dataSource, database.getName()));
                database.setTriggers(tiberoMapper.getTriggers(dataSource, database.getName()));
                database.setTypes(tiberoMapper.getTypes(dataSource, database.getName()));
                database.setSequences(tiberoMapper.getSequences(dataSource, database.getName()));
                database.setSynonyms(tiberoMapper.getSynonyms(dataSource, database.getName()));
                database.setJobs(tiberoMapper.getJobs(dataSource, database.getName()));
            }
        }

        return TiberoDto.builder()
                .instance(tiberoMapper.getInstance(dataSource))
                .sga(tiberoMapper.getSga(dataSource))
                .dataFiles(tiberoMapper.getDataFiles(dataSource))
                .controlFiles(tiberoMapper.getControlFiles(dataSource))
                .logFiles(tiberoMapper.getLogFiles(dataSource))
                .tableSpaces(tiberoMapper.getTableSpaces(dataSource))
                .parameters(tiberoMapper.getParameters(dataSource))
                .segment(tiberoMapper.getSegment(dataSource))
                .users(tiberoMapper.getUsers(dataSource))
                .publicSynonyms(tiberoMapper.getPublicSynonyms(dataSource))
                .dbLinks(tiberoMapper.getDbLinks(dataSource))
                .databases(databases)
                .build();

    }
}
