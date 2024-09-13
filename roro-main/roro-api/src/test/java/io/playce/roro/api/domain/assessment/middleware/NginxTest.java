package io.playce.roro.api.domain.assessment.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.asmt.windows.impl.WindowsAssessment;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.discover.helper.UnknownMiddlewareDiscoverHelper;
import io.playce.roro.mw.asmt.dto.DiscMiddlewareInstance;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.nginx.NginxAssessment;
import io.playce.roro.mw.asmt.nginx.NginxPostProcess;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mybatis.domain.inventory.server.ServerMapper;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import io.playce.roro.svr.asmt.dto.result.WindowsAssessmentResult;
import io.playce.roro.svr.asmt.redhat.impl.RedHatServerAssessment;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NginxTest {

    @Autowired
    public WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServerMapper serverMapper;

    @Autowired
    private RedHatServerAssessment REDHATAssessment;

    @Autowired
    private NginxAssessment NGINXAssessment;

    @Autowired
    private WindowsAssessment windowsAssessment;

    @Autowired
    private UnknownMiddlewareDiscoverHelper unknownMiddlewareDiscoverHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NginxPostProcess nginxPostProcess;

//    @Autowired
//    private NginxAssessment NGINXAssessment;

    //    public static final String confPath = "/Users/jhbaek/Downloads/backup/nginx.conf";
    public static final String confPath = "/Users/jhbaek/Downloads/nginxConfig/nginx.conf";

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(springSecurity())  // Security 사용 시 등록
                .build();
    }

    @Test
    @Transactional
    void Nginx_서버테스트() throws Exception {
        InventoryProcessConnectionInfo connectionInfo =
                 // serverMapper.selectServerConnectionInfoByInventoryProcessId(995L);
               // serverMapper.selectServerConnectionInfoByInventoryProcessId(222L);
              serverMapper.selectServerConnectionInfoByInventoryProcessId(1L);
        TargetHost targetHost = InventoryProcessConnectionInfo.targetHost(connectionInfo);
      ServerAssessmentResult result = REDHATAssessment.assessment(targetHost);

         // WindowsAssessmentResult result = windowsAssessment.assessment(targetHost);

        GetInfoStrategy strategy = GetInfoStrategy.getStrategy(connectionInfo.getWindowsYn().equals("Y"));

        unknownMiddlewareDiscoverHelper.extract(connectionInfo, result, strategy);
//        System.out.println(result);
//
//        System.out.println(connectionInfo);
//        System.out.println(targetHost);

    }

    @Test
    void NGINX_분석테스트() throws Exception {
        TargetHost targetHost = new TargetHost();
       targetHost.setIpAddress("192.168.4.61");
       targetHost.setPort(22);
       targetHost.setUsername("root");
       targetHost.setPassword("jan01jan");
//
        MiddlewareInventory middleware = new MiddlewareInventory();
       middleware.setDomainHomePath("/usr/local/openresty/nginx/conf/nginx.conf");
//        middleware.setDomainHomePath("/etc/nginx/nginx.conf");
       middleware.setInventoryName("Nginx");
       middleware.setInventoryDetailTypeCode("NGINX");
       middleware.setEngineInstallationPath("/usr/sbin");


         // targetHost.setIpAddress("192.168.1.107");
         // targetHost.setUsername("Administrator");
         // targetHost.setPassword("P@ssw0rd");
         // targetHost.setPort(5985);
         // middleware.setDomainHomePath("C:\\nginx-1.23.3\\conf\\nginx.conf");
         // middleware.setInventoryName("Nginx");
         // middleware.setInventoryDetailTypeCode("NGINX");
         // middleware.setEngineInstallationPath("C:\\nginx-1.23.3");

        GetInfoStrategy strategy = GetInfoStrategy.getStrategy(false);

        MiddlewareAssessmentResult result = NGINXAssessment.assessment(targetHost, middleware, strategy);


//        List<DiscMiddlewareInstance> discMiddlewareInstances = nginxPostProcess.getDiscoveredMiddlewareInstances(result, strategy);
//
//        for (DiscMiddlewareInstance temp : discMiddlewareInstances) {
//            System.out.println(temp.getMiddlewareInstanceServicePort());
//            System.out.println(temp.getMiddlewareInstanceProtocol());
//        }

//       System.out.println(objectMapper.writeValueAsString(result));
    }

    public static void main(String[] args) throws IOException {

        GetInfoStrategy strategy = GetInfoStrategy.getStrategy(false);

        String configInstancePath = "/opt/nginx-test/nginx.conf";
        String instanceName;

        try {
            instanceName = configInstancePath.substring(configInstancePath.lastIndexOf(strategy.getSeparator()) + 1);
            instanceName = instanceName.substring(0, instanceName.lastIndexOf("."));
        } catch (Exception e) {
            e.printStackTrace();
            instanceName = "nginx";
        }

        System.out.println(instanceName);

        String temp = "C:\\nginx-1.23.3/conf/nginx.conf";
        System.out.println(temp.replaceAll("/", "\\\\"));

//        System.out.println(getEvents(objectMapper1, conf));
//        System.out.println(objectMapper1.writeValueAsString(getHttp(objectMapper1, conf)));
//        System.out.println(getServer(conf));
//        System.out.println(getUpstream(conf));


    }

}
