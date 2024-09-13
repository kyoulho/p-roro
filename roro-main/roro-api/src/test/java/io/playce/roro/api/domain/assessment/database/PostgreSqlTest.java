package io.playce.roro.api.domain.assessment.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.db.asmt.constant.DBConstants;
import io.playce.roro.db.asmt.postgresql.PostgreSqlAssessment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostgreSqlTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostgreSqlAssessment POSTGREAssessment;

    @Test
    @Transactional
    void PostgreSql_테스트() throws Exception {
        DatabaseDto database = new DatabaseDto();
        database.setProjectId(1L);
        database.setDatabaseId(73L);
        database.setUserName("postgres");
        database.setPassword(GeneralCipherUtil.encrypt("jan01jan"));
        database.setDatabaseType(DBConstants.DATABASE_TYPE_POSTGRESQL);
        database.setDatabaseServiceName("dellstore2");
        database.setAllScanYn("N");
        database.setJdbcUrl("jdbc:postgresql://192.168.4.61:5432/dellstore2");

        ObjectMapper objectMapper = new ObjectMapper();
        Object result = POSTGREAssessment.assessment(database);

        System.out.println(objectMapper.writeValueAsString(result));

    }

}
