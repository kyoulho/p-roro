package io.playce.roro.common.util.support.parser;

import io.playce.roro.common.util.support.JdbcProperty;

import java.net.URI;
import java.util.Map;

public class SybaseURLParser implements JdbcUrlParser {

    @Override
    public JdbcProperty parse(String jdbcUrl) {
        int DEFAULT_PORT = 5000;

        if (!jdbcUrl.toLowerCase().startsWith("jdbc:sybase")) {
            return null;
        }

        return JdbcProperty.builder()
                .type(DataBaseConstants.DATABASE_TYPE_SYBASE)
                .host(getHost(jdbcUrl))
                .port(getPort(jdbcUrl) == -1 ? DEFAULT_PORT : getPort(jdbcUrl))
                .database(getDbInstance(jdbcUrl))
                .params(null)
                .build();
    }

    @Override
    public String getHost(String jdbcUrl) {
        String url = convertUrl(jdbcUrl);

        // 파싱을 편하게 하기 위애 Protocol을 붙인다.
        URI uri = URI.create("http://" + url);

        return uri.getHost();
    }

    @Override
    public Integer getPort(String jdbcUrl) {
        String url = convertUrl(jdbcUrl);

        // 파싱을 편하게 하기 위애 Protocol을 붙인다.
        URI uri = URI.create("http://" + url);

        return uri.getPort();
    }

    @Override
    public String getDbInstance(String jdbcUrl) {
        Map<String, String> paramMap = getParams(jdbcUrl);

        for (Map.Entry<String, String> elem : paramMap.entrySet()) {
            if (elem.getKey().equalsIgnoreCase("ServiceName")) {
                return paramMap.get(elem.getKey());
            }
        }

        return "";
    }

    private String convertUrl(String jdbcUrl) {
        String url;

        if (jdbcUrl.toLowerCase().contains("tds")) {
            url = jdbcUrl.split(":", 4)[3];
        } else {
            url = jdbcUrl.split(":", 3)[2];
        }

        return url;
    }

}
