package io.playce.roro.db.asmt.oracle.service;

import io.opentracing.contrib.jdbc.ConnectionInfo;
import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.util.JdbcURLParser;
import io.playce.roro.db.asmt.oracle.dto.Database;
import io.playce.roro.db.asmt.oracle.dto.OracleDto;
import io.playce.roro.db.asmt.oracle.mapper.OracleMapper;
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
public class OracleService {

    private final OracleMapper oracleMapper;

    public OracleDto getAssessment(DataSource dataSource, DatabaseDto databaseDto) {

        List<Database> databases = null;
        try {
            ConnectionInfo connectionInfo = JdbcURLParser.getJdbcUrlConnectionInfo(databaseDto.getJdbcUrl());
            String databaseName = connectionInfo == null ? "" : connectionInfo.getDbInstance();

            if (StringUtils.isEmpty(databaseName)) {
                log.warn("Database is Empty. Check jdbcUrl database name.");
            }

            databases = oracleMapper.getDatabases(dataSource, databaseName, databaseDto.getAllScanYn());
        } catch (Exception e) {
            log.error("SQLException occurred while getDatabases.", e);
        }

        if (CollectionUtils.isNotEmpty(databases)) {
            for (Database database : databases) {
                log.debug("Oracle DB scan start for [{}]", database.getName());
                database.setObjectSummary(oracleMapper.getObjectSummary(dataSource, database.getName()));
                database.setTables(oracleMapper.getTables(dataSource, database.getName()));
                database.setViews(oracleMapper.getViews(dataSource, database.getName()));
                database.setMaterializedViews(oracleMapper.getMaterializedViews(dataSource, database.getName()));
                database.setIndexes(oracleMapper.getIndexes(dataSource, database.getName()));
                database.setProcedures(oracleMapper.getProcedures(dataSource, database.getName()));
                database.setPackages(oracleMapper.getPackages(dataSource, database.getName()));
                database.setPackageBodies(oracleMapper.getPackageBodies(dataSource, database.getName()));
                database.setFunctions(oracleMapper.getFunctions(dataSource, database.getName()));
                database.setQueues(oracleMapper.getQueues(dataSource, database.getName()));
                database.setTriggers(oracleMapper.getTriggers(dataSource, database.getName()));
                database.setTypes(oracleMapper.getTypes(dataSource, database.getName()));
                database.setSequences(oracleMapper.getSequences(dataSource, database.getName()));
                database.setSynonyms(oracleMapper.getSynonyms(dataSource, database.getName()));
                database.setJobs(oracleMapper.getJobs(dataSource, database.getName()));
            }
        }

        return OracleDto.builder()
                .instance(oracleMapper.getInstance(dataSource))
                .sga(oracleMapper.getSga(dataSource))
                .dataFiles(oracleMapper.getDataFiles(dataSource))
                .controlFiles(oracleMapper.getControlFiles(dataSource))
                .logFiles(oracleMapper.getLogFiles(dataSource))
                .tableSpaces(oracleMapper.getTableSpaces(dataSource))
                .parameters(oracleMapper.getParameters(dataSource))
                .segment(oracleMapper.getSegment(dataSource))
                .users(oracleMapper.getUsers(dataSource))
                .publicSynonyms(oracleMapper.getPublicSynonyms(dataSource))
                .dbLinks(oracleMapper.getDbLinks(dataSource))
                .databases(databases)
                .build();
    }

}
