package io.playce.roro.api.domain.assessment.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.db.asmt.constant.DBConstants;
import io.playce.roro.db.asmt.mariadb.MariaDbAssessment;
import io.playce.roro.scheduler.component.impl.DatabaseAssessmentProcess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static io.playce.roro.common.util.ThreadLocalUtils.DB_SCAN_ERROR;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MariaDBTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MariaDbAssessment mariaDbAssessment;

    @Autowired
    private DatabaseAssessmentProcess databaseAssessmentProcess;

    @Test
    @Transactional
    void MariaDB_테스트() throws Exception {
        DatabaseDto database = new DatabaseDto();
        database.setProjectId(1L);
        database.setDatabaseId(220L);
        database.setUserName("playce");
        database.setPassword(GeneralCipherUtil.encrypt("playce"));
        database.setDatabaseType(DBConstants.DATABASE_TYPE_MARIADB);
        database.setDatabaseServiceName("rorodb");
        database.setAllScanYn("Y");
        database.setJdbcUrl("jdbc:mariadb://192.168.4.61:3306/rorodb");

        Object result = mariaDbAssessment.assessment(database);

        System.out.println(objectMapper.writeValueAsString(result));
        System.out.println("=======");
        System.out.println(ThreadLocalUtils.get(DB_SCAN_ERROR));


//         System.out.println(objectMapper.writeValueAsString(result));


       databaseAssessmentProcess.runPostProcessing(null, database, result);
    }

}
