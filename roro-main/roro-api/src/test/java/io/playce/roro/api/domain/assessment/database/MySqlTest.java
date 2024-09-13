package io.playce.roro.api.domain.assessment.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.db.asmt.constant.DBConstants;
import io.playce.roro.db.asmt.mysql.MySqlAssessment;
import io.playce.roro.scheduler.component.impl.DatabaseAssessmentProcess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MySqlTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MySqlAssessment mySqlAssessment;

    @Autowired
    private DatabaseAssessmentProcess databaseAssessmentProcess;

    @Test
    void MySQL_테스트() throws Exception {
        DatabaseDto database = new DatabaseDto();
        database.setProjectId(1L);
        database.setDatabaseId(220L);
        database.setUserName("playce");
        database.setPassword(GeneralCipherUtil.encrypt("playce"));
        database.setDatabaseType(DBConstants.DATABASE_TYPE_MYSQL);
        database.setDatabaseServiceName("rorodb");
        database.setAllScanYn("Y");
        database.setJdbcUrl("jdbc:mysql://192.168.4.61:3306/rorodb");

        Object result = mySqlAssessment.assessment(database);
//        databaseAssessmentProcess.runPostProcessing(null, database, result);

        System.out.println(objectMapper.writeValueAsString(result));
    }

}
