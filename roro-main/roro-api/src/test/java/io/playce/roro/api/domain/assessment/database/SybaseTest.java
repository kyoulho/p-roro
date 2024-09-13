package io.playce.roro.api.domain.assessment.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.db.asmt.constant.DBConstants;
import io.playce.roro.db.asmt.sybase.SybaseAssessment;
import io.playce.roro.scheduler.component.impl.DatabaseAssessmentProcess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SybaseTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SybaseAssessment sybaseAssessment;

    @Autowired
    private DatabaseAssessmentProcess databaseAssessmentProcess;

    @Test
    @Transactional
    void Sybase_테스트() throws Exception {
        DatabaseDto database = new DatabaseDto();
        database.setProjectId(1L);
        database.setDatabaseId(236L);
        database.setUserName("sa");
        database.setDatabaseType(DBConstants.DATABASE_TYPE_SYBASE);
        database.setPassword(GeneralCipherUtil.encrypt("jan01jan"));
        database.setDatabaseServiceName("pubs3");
        database.setAllScanYn("Y");
        database.setJdbcUrl("jdbc:sybase:Tds:192.168.4.70:5000/pubs3");

        Object result = sybaseAssessment.assessment(database);
        System.out.println(objectMapper.writeValueAsString(result));

        databaseAssessmentProcess.runPostProcessing(null, database, result);


    }

}
