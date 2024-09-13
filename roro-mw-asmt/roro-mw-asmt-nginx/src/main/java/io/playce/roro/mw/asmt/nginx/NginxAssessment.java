package io.playce.roro.mw.asmt.nginx;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.github.odiszapc.nginxparser.NgxConfig;
import io.playce.roro.common.exception.InsufficientException;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.MiddlewareAssessment;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.nginx.dto.NginxAssessmentResult;
import io.playce.roro.mw.asmt.nginx.dto.NginxAssessmentResult.Engine;
import io.playce.roro.mw.asmt.nginx.dto.NginxAssessmentResult.General;
import io.playce.roro.mw.asmt.nginx.helper.NginxAssessmentHelper;
import io.playce.roro.mw.asmt.nginx.helper.NginxParseHelper;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component("NGINXAssessment")
@RequiredArgsConstructor
@Slf4j
public class NginxAssessment implements MiddlewareAssessment {

    private final NginxParseHelper nginxParseHelper;
    private final NginxAssessmentHelper nginxAssessmentHelper;

    @Override
    public MiddlewareAssessmentResult assessment(TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("-- Start Nginx Analyze --");
        ObjectMapper objectMapper = new ObjectMapper();
        // 속성값이 없는 경우에는 무시한다.
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // key값이 underscore 로 설정된 것을 읽는다.
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        NginxAssessmentResult assessmentResult = new NginxAssessmentResult();

        NginxAssessmentResult.Instance instance = new NginxAssessmentResult.Instance();

        try {
            String enginePath = middleware.getEngineInstallationPath();
            String instancePath = middleware.getDomainHomePath();

            if (StringUtils.isEmpty(enginePath)) {
                throw new InsufficientException("Nginx engine install path is empty.");
            }

            if (StringUtils.isEmpty(instancePath)) {
                throw new InsufficientException("Nginx domain home is empty.");
            }

            assessmentResult.setEngine(nginxAssessmentHelper.getEngine(targetHost, strategy, enginePath, instancePath));

            // read nginx config file.
            InputStream configFileContents = nginxAssessmentHelper.getConfigFileContentInputStream(targetHost, strategy, instancePath);

            // inclue 된 file의 내용을 Map에 저장한다.
            NgxConfig conf = NgxConfig.read(configFileContents);
            Map<String, String> includeFileContentMap = nginxAssessmentHelper.getIncludeFileContent(targetHost, objectMapper, strategy, conf, instancePath);

            // append 했을 때  replace가 되기 때문에 config File은 미리 해놓는다.
            instance.setConfigFiles(nginxParseHelper.getConfigFiles(targetHost, objectMapper, strategy, conf, instancePath));

            // append한 include file을 다시 세팅한다.
            conf = NgxConfig.read(nginxAssessmentHelper.getIncludeFileAppendContent(conf, includeFileContentMap));
            instance.setEvents(nginxParseHelper.getEvents(objectMapper, conf));
            instance.setHttp(nginxParseHelper.getHttp(objectMapper, conf));
            instance.setStream(nginxParseHelper.getStream(objectMapper, conf));

            General general = nginxParseHelper.getGeneral(objectMapper, conf);
            general = nginxAssessmentHelper.getAdditionalGeneralInfo(general, instance, (Engine) assessmentResult.getEngine());
            instance.setGeneral(general);

            assessmentResult.setInstance(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return assessmentResult;
    }

}