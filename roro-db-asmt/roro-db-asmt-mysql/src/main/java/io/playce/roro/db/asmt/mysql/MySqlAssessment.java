package io.playce.roro.db.asmt.mysql;

import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.exception.ConnectionErrorException;
import io.playce.roro.db.asmt.AbstractDBAssessment;
import io.playce.roro.db.asmt.mysql.config.MySqlDataSource;
import io.playce.roro.db.asmt.mysql.service.MySqlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component("MYSQLAssessment")
@RequiredArgsConstructor
public class MySqlAssessment extends AbstractDBAssessment {

    private final MySqlDataSource mySqlDataSource;
    private final MySqlService mySqlService;

    @Override
    protected DataSource getDataSource(DatabaseDto database) {
        try {
            return mySqlDataSource.getDataSource(database);
        } catch (Exception e) {
            throw new ConnectionErrorException(e.getMessage());
        }
    }

    @Override
    public Object assessment(DatabaseDto database) {
        final DataSource dataSource = getDataSource(database);

        return mySqlService.getAssessment(dataSource, database);
    }

}
