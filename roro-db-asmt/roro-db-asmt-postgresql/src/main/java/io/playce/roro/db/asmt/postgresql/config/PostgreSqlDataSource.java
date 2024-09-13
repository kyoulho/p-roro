package io.playce.roro.db.asmt.postgresql.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.db.asmt.constant.DBConstants;
import io.playce.roro.db.asmt.factory.DataSourceFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class PostgreSqlDataSource implements DataSourceFactory {

    @Override
    public DataSource getDataSource(DatabaseDto database) {
        if (database.getDatabaseType().equals(DBConstants.DATABASE_TYPE_POSTGRESQL)) {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName(DBConstants.POSTGRESQL_DRIVER_CLASS_NAME);
            hikariConfig.setJdbcUrl(database.getJdbcUrl());
            hikariConfig.setUsername(database.getUserName());
            hikariConfig.setPassword(GeneralCipherUtil.decrypt(database.getPassword()));

            return new HikariDataSource(hikariConfig);
        } else {
            return null;
        }
    }
}