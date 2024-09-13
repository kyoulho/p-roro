package io.playce.roro.db.asmt.postgresql;

import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.exception.ConnectionErrorException;
import io.playce.roro.db.asmt.AbstractDBAssessment;
import io.playce.roro.db.asmt.postgresql.config.PostgreSqlDataSource;
import io.playce.roro.db.asmt.postgresql.service.PostgreSqlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component("POSTGREAssessment")
@RequiredArgsConstructor
public class PostgreSqlAssessment extends AbstractDBAssessment {

    private final PostgreSqlDataSource postgreSqlDataSource;
    private final PostgreSqlService postgreSqlService;

    @Override
    protected DataSource getDataSource(DatabaseDto database) {
        try {
            return postgreSqlDataSource.getDataSource(database);
        } catch (Exception e) {
            throw new ConnectionErrorException(e.getMessage());
        }
    }

    @Override
    public Object assessment(DatabaseDto database) {
        final DataSource dataSource = getDataSource(database);

        return postgreSqlService.getAssessment(dataSource, database);
    }
}
