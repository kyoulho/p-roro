package io.playce.roro.common.util.support.parser;

import io.opentracing.contrib.jdbc.ConnectionInfo;
import io.opentracing.contrib.jdbc.parser.ConnectionURLParser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CustomTiberoURLParser implements ConnectionURLParser {

    public static final String DB_TYPE = "tibero";
    public static final String PREFIX_THIN = "jdbc:tibero:thin:";
    public static final String PREFIX_OCI = "jdbc:tibero:oci:";
    public static final int DEFAULT_PORT = 8629;
    private static final Pattern EASY_CONNECT_PATTERN = Pattern.compile(
            "(?<username>.*)@(?<ldap>ldap:)?(//)?(?<host>[^:/]+)(?<port>:[0-9]+)?(?<service>[:/][^:/]+)?(?<server>:[^:/]+)?(?<instance>/[^:/]+)?");

    @Override
    public ConnectionInfo parse(final String url) {
        if (url != null) {
            String lowerCaseUrl = url.toLowerCase();
            if ((lowerCaseUrl.startsWith(PREFIX_THIN) || lowerCaseUrl.startsWith(PREFIX_OCI))) {
                String trimmedURL;
                if (lowerCaseUrl.startsWith(PREFIX_THIN)) {
                    trimmedURL = url.substring(PREFIX_THIN.length());
                } else {
                    trimmedURL = url.substring(PREFIX_OCI.length());
                }
                TiberoConnectionInfo connectionInfo = parseTnsName(trimmedURL);
                if (connectionInfo == null) {
                    connectionInfo = parseEasyConnect(trimmedURL);
                }
                if (connectionInfo != null) {
                    return new ConnectionInfo.Builder(connectionInfo.getDbPeer()) //
                            .dbType(DB_TYPE) //
                            .dbInstance(connectionInfo.getDbInstance()) //
                            .build();
                }
            }
        }
        return null;
    }

    private TiberoConnectionInfo parseTnsName(final String url) {
        final String hosts = parseDatabaseHostsFromTnsUrl(url);
        if (hosts != null) {
            final int idxServiceName = url.toUpperCase().indexOf("DATABASE_NAME");
            final int start = url.indexOf('=', idxServiceName) + 1;
            final int end = url.indexOf(")", start);
            final String serviceName = url.substring(start, end);
            return new TiberoConnectionInfo() //
                    .setDbPeer(hosts) //
                    .setDbInstance(serviceName);
        }
        return null;
    }

    public static String parseDatabaseHostsFromTnsUrl(String url) {
        int beginIndex = url.toUpperCase().indexOf("DESCRIPTION");
        if (beginIndex == -1) {
            return null;
        }
        List<String> hosts = new ArrayList<String>();

        do {
            int hostStartIndex = url.toUpperCase().indexOf("HOST", beginIndex);
            if (hostStartIndex == -1) {
                break;
            }
            int equalStartIndex = url.indexOf("=", hostStartIndex);
            int hostEndIndex = url.indexOf(")", hostStartIndex);
            String host = url.substring(equalStartIndex + 1, hostEndIndex);

            int port = DEFAULT_PORT;
            int portStartIndex = url.toUpperCase().indexOf("PORT", hostEndIndex);
            int portEndIndex = url.length();
            if (portStartIndex != -1) {
                int portEqualStartIndex = url.indexOf("=", portStartIndex);
                portEndIndex = url.indexOf(")", portEqualStartIndex);
                port = Integer.parseInt(url.substring(portEqualStartIndex + 1, portEndIndex).trim());
            }
            hosts.add(host.trim() + ":" + port);
            beginIndex = portEndIndex;
        } while (true);
        return join(",", hosts);
    }

    private static String join(String delimiter, List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0, len = list.size(); i < len; i++) {
            if (i == (len - 1)) {
                builder.append(list.get(i));
            } else {
                builder.append(list.get(i)).append(delimiter);
            }
        }
        return builder.toString();
    }

    /**
     * Implementation according to https://www.oracle.com/technetwork/database/enterprise-edition/oraclenetservices-neteasyconnect-133058.pdf
     *
     * @param url the url without the oracle jdbc prefix
     * @return the oracle connection info if the url could be parsed, or null otherwise.
     */
    public static TiberoConnectionInfo parseEasyConnect(final String url) {
        final Matcher matcher = EASY_CONNECT_PATTERN.matcher(url);
        if (matcher.matches()) {
            final TiberoConnectionInfo result = new TiberoConnectionInfo();
            final String host = matcher.group("host");
            final String portGroup = matcher.group("port");
            final int dbPort =
                    portGroup != null ? Integer.parseInt(portGroup.substring(1)) : DEFAULT_PORT;
            result.setDbPeer(host + ":" + dbPort);
            final String service = matcher.group("service");
            if (service != null) {
                result.setDbInstance(service.substring(1));
            } else {
                result.setDbInstance(host);
            }
            return result;
        }
        return null;
    }

    public static class TiberoConnectionInfo {
        private String dbInstance;
        private String dbPeer;

        public String getDbInstance() {
            return dbInstance;
        }

        public TiberoConnectionInfo setDbInstance(final String dbInstance) {
            this.dbInstance = dbInstance;
            return this;
        }

        public String getDbPeer() {
            return dbPeer;
        }

        public TiberoConnectionInfo setDbPeer(final String dbPeer) {
            this.dbPeer = dbPeer;
            return this;
        }
    }

}