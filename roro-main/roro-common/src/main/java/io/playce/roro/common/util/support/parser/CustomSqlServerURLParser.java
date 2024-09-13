package io.playce.roro.common.util.support.parser;

import io.opentracing.contrib.jdbc.ConnectionInfo;
import io.opentracing.contrib.jdbc.parser.ConnectionURLParser;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CustomSqlServerURLParser implements ConnectionURLParser {

    private static final int DEFAULT_PORT = 1433;

    protected String dbType() {
        return "sqlserver";
    }

    @Override
    public ConnectionInfo parse(String url) {
        String serverName = "";
        Integer port = DEFAULT_PORT;
        String dbInstance = null;
        int hostIndex = url.indexOf("://");
        if (hostIndex <= 0) {
            return null;
        }

        String[] split = url.split(";", 2);
        if (split.length > 1) {
            Map<String, String> props = parseQueryParams(split[1], ";");
            serverName = props.get("servername");
            dbInstance = props.get("databasename");
            if (props.containsKey("portnumber")) {
                String portNumber = props.get("portnumber");
                try {
                    port = Integer.parseInt(portNumber);
                } catch (NumberFormatException e) {
                }
            }
        }

        String urlServerName = split[0].substring(hostIndex + 3);
        if (!urlServerName.isEmpty()) {
            serverName = urlServerName;
        }

        int portLoc = serverName.indexOf(":");
        if (portLoc > 1) {
            port = Integer.parseInt(serverName.substring(portLoc + 1));
            serverName = serverName.substring(0, portLoc);
        }

        int instanceLoc = serverName.indexOf("\\");
        if (instanceLoc > 1) {
            serverName = serverName.substring(0, instanceLoc);
        }

        if (serverName.isEmpty()) {
            return null;
        }

        return new ConnectionInfo.Builder(serverName, port).dbType(dbType())
                .dbInstance(dbInstance).build();
    }

    private Map<String, String> parseQueryParams(String query, String separator) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> queryParams = new LinkedHashMap<>();
        String[] pairs = query.split(separator);
        for (String pair : pairs) {
            try {
                int idx = pair.indexOf("=");
                String key =
                        idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name()) : pair;
                if (!queryParams.containsKey(key)) {
                    String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder
                            .decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name()) : null;
                    queryParams.put(key.toLowerCase(), value);
                }
            } catch (UnsupportedEncodingException e) {
                // Ignore.
            }
        }
        return queryParams;
    }
}
