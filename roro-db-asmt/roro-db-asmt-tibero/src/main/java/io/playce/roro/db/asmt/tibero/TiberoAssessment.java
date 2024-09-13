package io.playce.roro.db.asmt.tibero;

import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.exception.ConnectionErrorException;
import io.playce.roro.db.asmt.AbstractDBAssessment;
import io.playce.roro.db.asmt.tibero.config.TiberoDataSource;
import io.playce.roro.db.asmt.tibero.service.TiberoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component("TIBEROAssessment")
@RequiredArgsConstructor
public class TiberoAssessment extends AbstractDBAssessment {

    private final TiberoDataSource tiberoDataSource;

    private final TiberoService tiberoService;

    @Override
    protected DataSource getDataSource(DatabaseDto database) {
        try {
            return tiberoDataSource.getDataSource(database);
        } catch (Exception e) {
            throw new ConnectionErrorException(e.getMessage());
        }
    }

    @Override
    public Object assessment(DatabaseDto database) {
        final DataSource dataSource = getDataSource(database);

        return tiberoService.getAssessment(dataSource, database);
    }
}
