package io.playce.roro.db.asmt.mariadb;

import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.exception.ConnectionErrorException;
import io.playce.roro.db.asmt.AbstractDBAssessment;
import io.playce.roro.db.asmt.mariadb.config.MariaDbDataSource;
import io.playce.roro.db.asmt.mariadb.service.MariaDbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component("MARIADBAssessment")
@RequiredArgsConstructor
public class MariaDbAssessment extends AbstractDBAssessment {

    private final MariaDbDataSource mariaDbDataSource;
    private final MariaDbService mariaDbService;

    @Override
    protected DataSource getDataSource(DatabaseDto database) {
        try {
            return mariaDbDataSource.getDataSource(database);
        } catch (Exception e) {
            throw new ConnectionErrorException(e.getMessage());
        }
    }

    @Override
    public Object assessment(DatabaseDto database) {
        final DataSource dataSource = getDataSource(database);

        return mariaDbService.getAssessment(dataSource, database);
    }

}
