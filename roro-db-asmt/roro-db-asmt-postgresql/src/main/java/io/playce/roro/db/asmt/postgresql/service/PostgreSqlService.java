package io.playce.roro.db.asmt.postgresql.service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.opentracing.contrib.jdbc.ConnectionInfo;
import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.common.util.JdbcURLParser;
import io.playce.roro.db.asmt.constant.DBConstants;
import io.playce.roro.db.asmt.postgresql.dto.Database;
import io.playce.roro.db.asmt.postgresql.dto.PostgreSqlDto;
import io.playce.roro.db.asmt.postgresql.mapper.PostgreSqlMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class PostgreSqlService {

    private final PostgreSqlMapper postgreSqlMapper;

    public Object getAssessment(DataSource dataSource, DatabaseDto databaseDto) {
        List<Database> databases = new ArrayList<>();

        ConnectionInfo connectionInfo = JdbcURLParser.getJdbcUrlConnectionInfo(databaseDto.getJdbcUrl());
        String databaseName = connectionInfo == null ? "" : connectionInfo.getDbInstance();

        if (StringUtils.isEmpty(databaseName)) {
            log.warn("Database is Empty. Check jdbcUrl database name.");
        } else {
            try {
                databases = postgreSqlMapper.getDatabases(dataSource, databaseName, databaseDto.getAllScanYn());
            } catch (Exception e) {
                log.error("SQLException occurred while getDatabases.", e);
            }
        }

        for (Database database : databases) {
            log.debug("PostgreSQL DB scan start for [{}]", database.getName());
            DataSource changeDataSource = getChangeDataSource(databaseDto, connectionInfo, database.getName());

            database.setTables(postgreSqlMapper.getTables(changeDataSource));
            database.setViews(postgreSqlMapper.getViews(changeDataSource));
            database.setIndexes(postgreSqlMapper.getIndexes(changeDataSource));
            database.setProcedures(postgreSqlMapper.getProcedures(changeDataSource));
            database.setFunctions(postgreSqlMapper.getFunctions(changeDataSource));
            database.setSequences(postgreSqlMapper.getSequences(changeDataSource));
            database.setTriggers(postgreSqlMapper.getTriggers(changeDataSource));
        }

        return PostgreSqlDto.builder()
                .instance(postgreSqlMapper.getInstance(dataSource))
                .users(postgreSqlMapper.getUsers(dataSource))
                .databases(databases)
                .build();
    }

    private DataSource getChangeDataSource(DatabaseDto databaseDto, ConnectionInfo connectionInfo, String databaseName) {
        String prefixJdbcUrl = "jdbc:postgresql://";

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(DBConstants.POSTGRESQL_DRIVER_CLASS_NAME);
        hikariConfig.setJdbcUrl(prefixJdbcUrl + connectionInfo.getDbPeer() + "/" + databaseName);
        hikariConfig.setUsername(databaseDto.getUserName());
        hikariConfig.setPassword(GeneralCipherUtil.decrypt(databaseDto.getPassword()));

        return new HikariDataSource(hikariConfig);
    }

}
