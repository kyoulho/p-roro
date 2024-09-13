 package io.playce.roro.api.domain.assessment.database;

 import com.fasterxml.jackson.databind.ObjectMapper;
 import io.playce.roro.common.dto.assessment.DatabaseDto;
 import io.playce.roro.common.util.GeneralCipherUtil;
 import io.playce.roro.db.asmt.constant.DBConstants;
 import io.playce.roro.db.asmt.mssql.MsSqlAssessment;
 import io.playce.roro.scheduler.component.impl.DatabaseAssessmentProcess;
 import org.junit.jupiter.api.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
 import org.springframework.boot.test.context.SpringBootTest;
 import org.springframework.transaction.annotation.Transactional;

 @AutoConfigureMockMvc
 @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 public class MsSqlTest {

     @Autowired
     private MsSqlAssessment msSqlAssessment;

     @Autowired
     private DatabaseAssessmentProcess databaseAssessmentProcess;

     @Test
     @Transactional
     void MSSQL_테스트() throws Exception {

         DatabaseDto database = new DatabaseDto();
         database.setProjectId(1L);
         database.setDatabaseId(73L);
         database.setUserName("sa");
         database.setPassword(GeneralCipherUtil.encrypt("jan01jan"));
         database.setDatabaseType(DBConstants.DATABASE_TYPE_MSSQL);
         database.setDatabaseServiceName("master");
         database.setAllScanYn("Y");
         database.setJdbcUrl("jdbc:sqlserver://192.168.1.157:1433;DatabaseName=master");

         ObjectMapper objectMapper = new ObjectMapper();

         Object result = msSqlAssessment.assessment(database);
         System.out.println(objectMapper.writeValueAsString(result));
         databaseAssessmentProcess.runPostProcessing(null, database, result);

     }

 }
