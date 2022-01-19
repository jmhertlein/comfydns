package com.comfydns.util.config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnvConfig {
    public static boolean isUsageReportingDisabled() {
        return isSet("USAGE_REPORTING_DISABLED");
    }

    public static String getUsageReportingDomain() {
        return getOrDefault("CDNS_USAGE_REPORTING_NAME", "comfydns.com");
    }

    public static String getUsageReportingProto() {
        return getOrDefault("CDNS_USAGE_REPORTING_PROTO", "https");
    }

    public static Path getPersistentRootPath() {
        return Path.of(get("CDNS_ROOT_PATH"));
    }

    public static boolean isPersistentRootPathSet() {
        return isSet("CDNS_ROOT_PATH");
    }

    public static int getDOHServerPort() {
        return Integer.parseInt(getOrDefault("CDNS_DOH_SERVER_PORT", "443"));
    }

    public static int getDnsServerPort() {
        return Integer.parseInt(getOrDefault("CDNS_DNS_SERVER_PORT", "53"));
    }

    public static List<InetAddress> getAllowZoneTransferTo() throws UnknownHostException {
        String allowed = getOrDefault("CDNS_ALLOW_ZONE_TRANSFER_TO", "");
        List<InetAddress> ret = new ArrayList<>();
        for(String rawAddr : allowed.split(",")) {
            InetAddress addr = InetAddress.getByName(rawAddr);
            ret.add(addr);
        }

        return ret;
    }

    /**
     *
     * @return
     * @throws RuntimeException if the environment isn't populated correctly
     */
    public static DBConfig buildDBConfig() {
        String host = getOrDefault("CDNS_DB_HOST", "localhost");
        String name = getOrDefault("CDNS_DB_NAME", "comfydns");

        Optional<String> password;
        if(isSet("CDNS_DB_PW_FILE")) {
            try {
                password = Optional.of(Files.readString(Path.of(get("CDNS_DB_PW_FILE"))));
            } catch (IOException e) {
                throw new RuntimeException("Error reading password file " + get("CDNS_DB_PW_FILE"), e);
            }
        } else if(isSet("CDNS_DB_PW")) {
            password = Optional.of(get("CDNS_DB_PW"));
        } else {
            password = Optional.empty();
        }

        return password.map(s -> new DBConfig(host, name, s))
                .orElseGet(() -> new DBConfig(host, name));
    }

    public static boolean isDoubleCheckingEnabled() {
        return isSet("CDNS_DOUBLE_CHECK_SERVER");
    }

    public static String getDoubleCheckServer() {
        return get("CDNS_DOUBLE_CHECK_SERVER");
    }



    private static String getOrDefault(String env, String dflt) {
        String getenv = System.getenv(env);
        if(getenv == null || getenv.isBlank()) {
            return dflt;
        }

        return getenv;
    }

    private static String get(String env) {
        return System.getenv(env);
    }

    private static boolean isSet(String env) {
        String getenv = System.getenv(env);
        return getenv != null && !getenv.isBlank();
    }

    public static int getMetricsServerPort() {
        return Integer.parseInt(getOrDefault("CDNS_METRICS_SERVER_PORT", "33200"));
    }

    public static String getDOHServerCertificateFile() {
        return get("CDNS_DOH_TLS_CERT_FILE_PATH");
    }

    public static String getDOHServerKeyFile() {
        return get("CDNS_DOH_TLS_KEY_FILE_PATH");
    }

    public static boolean getDOHUsesTLS() {
        return !isSet("CDNS_DOH_USE_PLAIN_HTTP");
    }
}
