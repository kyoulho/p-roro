 package io.playce.roro.api.domain.assessment.database;

 import com.fasterxml.jackson.databind.ObjectMapper;
 import io.playce.roro.common.dto.assessment.DatabaseDto;
 import io.playce.roro.common.util.GeneralCipherUtil;
 import io.playce.roro.db.asmt.constant.DBConstants;
 import io.playce.roro.db.asmt.oracle.OracleAssessment;
 import io.playce.roro.scheduler.component.impl.DatabaseAssessmentProcess;
 import org.junit.jupiter.api.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
 import org.springframework.boot.test.context.SpringBootTest;
 import org.springframework.transaction.annotation.Transactional;

 @AutoConfigureMockMvc
 @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 public class OracleTest {

     @Autowired
     private ObjectMapper objectMapper;
     @Autowired
     private OracleAssessment oracleAssessment;

     @Autowired
     private DatabaseAssessmentProcess databaseAssessmentProcess;

     @Test
     @Transactional
     void Oracle_테스트() throws Exception {
         DatabaseDto database = new DatabaseDto();
         database.setProjectId(1L);
         database.setDatabaseId(28L);
         database.setUserName("system");
         database.setPassword(GeneralCipherUtil.encrypt("jan01jan"));
         database.setDatabaseType(DBConstants.DATABASE_TYPE_ORACLE);
         database.setDatabaseServiceName("ORCL");
         database.setAllScanYn("N");
         database.setJdbcUrl("jdbc:oracle:thin:@192.168.1.108:1521:orcl");

         Object result = oracleAssessment.assessment(database);
         System.out.println(objectMapper.writeValueAsString(result));

         databaseAssessmentProcess.runPostProcessing(null, database, result);

     }

 }
