package io.playce.roro.db.asmt.mssql;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.exception.ConnectionErrorException;
import io.playce.roro.db.asmt.AbstractDBAssessment;
import io.playce.roro.db.asmt.mssql.config.MsSqlDataSource;
import io.playce.roro.db.asmt.mssql.service.MsSqlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component("MSSQLAssessment")
@RequiredArgsConstructor
public class MsSqlAssessment extends AbstractDBAssessment {

    private final ObjectMapper objectMapper;
    private final MsSqlDataSource msSqlDataSource;
    private final MsSqlService msSqlService;

    @Override
    protected DataSource getDataSource(DatabaseDto database) {
        try {
            return msSqlDataSource.getDataSource(database);
        } catch (Exception e) {
            throw new ConnectionErrorException(e.getMessage());
        }
    }

    @Override
    public Object assessment(DatabaseDto database) {
        final DataSource dataSource = getDataSource(database);

        return msSqlService.getAssessment(dataSource, database);
    }
}
