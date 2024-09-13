 package io.playce.roro.api.domain.assessment.database;

 import com.fasterxml.jackson.databind.ObjectMapper;
 import io.playce.roro.common.dto.assessment.DatabaseDto;
 import io.playce.roro.common.util.GeneralCipherUtil;
 import io.playce.roro.db.asmt.constant.DBConstants;
 import io.playce.roro.db.asmt.tibero.TiberoAssessment;
 import io.playce.roro.scheduler.component.impl.DatabaseAssessmentProcess;
 import org.junit.jupiter.api.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
 import org.springframework.boot.test.context.SpringBootTest;
 import org.springframework.transaction.annotation.Transactional;

 @AutoConfigureMockMvc
 @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 public class TiberoTest {

     @Autowired
     private ObjectMapper objectMapper;

     @Autowired
     private TiberoAssessment tiberoAssessment;

     @Autowired
     private DatabaseAssessmentProcess databaseAssessmentProcess;

     @Test
     @Transactional
     void 티베로_테스트() throws Exception {
         DatabaseDto database = new DatabaseDto();
         database.setProjectId(1L);
         database.setDatabaseId(79L);
         database.setUserName("sys");
         database.setPassword(GeneralCipherUtil.encrypt("tibero"));
         database.setDatabaseType(DBConstants.DATABASE_TYPE_TIBERO);
         database.setDatabaseServiceName("tibero");
         database.setAllScanYn("Y");
         database.setJdbcUrl("jdbc:tibero:thin:@192.168.4.70:8629:tibero");

         Object result = tiberoAssessment.assessment(database);

         System.out.println(objectMapper.writeValueAsString(result));
//
         databaseAssessmentProcess.runPostProcessing(null, database, result);

     }

 }
