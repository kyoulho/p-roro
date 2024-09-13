package io.playce.roro.mw.asmt.nginx.dto;

import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class NginxAssessmentResult extends MiddlewareAssessmentResult {

    @Getter
    @Setter
    @ToString
    public static class Engine extends MiddlewareAssessmentResult.Engine {
        private String path;
        private String configPath;
        private String name;
        private String version;
        private String runUser;
    }

    @Getter
    @Setter
    @ToString
    public static class Instance extends MiddlewareAssessmentResult.Instance {

        private List<ConfigFile> configFiles;
        private General general;
        private Events events;
        private Http http;
        private Stream stream;

    }

    @Getter
    @Setter
    @ToString
    public static class ConfigFile {
        private String path;
        private String content;
    }


    @Getter
    @Setter
    @ToString
    public static class General {
        private String vendor;
        private String solutionName;
        private String solutionVersion;
        private String installHome;
        private List<Integer> listenPort;
        private boolean ssl;
        private Date scannedDate;

        private String user;
        private String workerProcesses;
        private String errorLog;
        private String pid;
        private Integer workerRlimitNofile;
        private List<String> include;

    }

    @Getter
    @Setter
    @ToString
    public static class Events {
        private String use;
        private Integer workerConnections;
        private Integer workerAioRequests;
        private String acceptMutex;
        private String acceptMutexDelay;
        private String multiAccept;
    }

    @Getter
    @Setter
    @ToString
    public static class Stream {
        private List<Upstream> upstreams;
        private List<Server> servers;
    }

    @Getter
    @Setter
    @ToString
    public static class Http {
        private List<String> include;
        private String index;
        private String defaultType;
        private List<String> logFormat;
        private String accessLog;
        private String sendfile;
        private String tcpNopush;
        private Integer serverNamesHashBucketSize;
        private List<Server> servers;
        private List<Upstream> upstreams;

    }

    @Getter
    @Setter
    @ToString
    public static class Server {
        private List<String> listen;
        private String serverName;
        private String accessLog;
        private String root;
        private Ssl ssl;
        private Proxy proxy;
        private List<Location> locations;

        @Getter
        @Setter
        @ToString
        public static class Proxy {
            private List<String> proxySetHeader;
            private String proxyPass;
            private String proxyBuffers;
            private String proxyBufferSize;
            private String proxyReadTimeout;
            private String proxyCache;
            private String proxyCacheRevalidate;
            private String proxyCacheMinUses;
            private String proxyCacheUseStale;
            private String proxyCacheLock;
            private String proxyHttpVersion;
        }

        @Getter
        @Setter
        @ToString
        public static class Ssl {
            private String ssl;
            private String sslCertificate;
            private String sslCertificateKey;
            private String sslCiphers;
            private String sslPreferServerCiphers;
            private String sslClientCertificate;
            private String sslSessionCache;
            private String sslSessionTicketKey;
            private String sslSessionTickets;
            private String sslSessionTimeout;
        }

        @Getter
        @Setter
        @ToString
        public static class Location {
            private String uri;
            private String root;
            private String expires;
            private String fastcgiPass;
            private Proxy proxy;
        }

    }

    @Getter
    @Setter
    @ToString
    public static class Upstream {
        private String name;
        private List<Server> servers;

        @Getter
        @Setter
        @ToString
        public static class Server {
            private String address;
            private String option;
        }

    }

}