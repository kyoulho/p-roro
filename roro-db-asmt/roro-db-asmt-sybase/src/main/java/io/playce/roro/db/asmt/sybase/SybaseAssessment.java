package io.playce.roro.db.asmt.sybase;

import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.exception.ConnectionErrorException;
import io.playce.roro.db.asmt.AbstractDBAssessment;
import io.playce.roro.db.asmt.sybase.config.SybaseDataSource;
import io.playce.roro.db.asmt.sybase.service.SybaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component("SYBASEAssessment")
@RequiredArgsConstructor
public class SybaseAssessment extends AbstractDBAssessment {

    private final SybaseDataSource dataSourceFactory;

    private final SybaseService sybaseService;

    @Override
    protected DataSource getDataSource(DatabaseDto database) {
        try {
            return dataSourceFactory.getDataSource(database);
        } catch (Exception e) {
            throw new ConnectionErrorException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object assessment(DatabaseDto database) {
        final DataSource dataSource = getDataSource(database);

        return sybaseService.getAssessment(dataSource, database);
    }
}
