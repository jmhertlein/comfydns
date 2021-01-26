package cafe.josh.comfydns.system;

import org.postgresql.ds.PGConnectionPoolDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.ScheduledExecutorService;

public class DatabaseUtils {
    private static final Logger log = LoggerFactory.getLogger(DatabaseUtils.class);

    private DatabaseUtils() {}

    public static SimpleConnectionPool setupPool(ScheduledExecutorService cron) throws ClassNotFoundException, SQLException, IOException {
        log.info("Starting ComfyDNS server.");
        Class.forName("org.postgresql.Driver");

        log.info("Opening connection pool to db...");
        PGConnectionPoolDataSource pgPool = new PGConnectionPoolDataSource();
        pgPool.setApplicationName("comfydns-recursor");

        pgPool.setDatabaseName("comfydns");
        pgPool.setPortNumbers(new int[]{54321});
        pgPool.setUser("comfydns");
        String passwordFileName = System.getProperty("COMFYDNS_DB_PASSWORD_FILE", "");
        String password;
        if(passwordFileName.isBlank()) {
            password = "";
        } else {
            password = Files.readString(Path.of(passwordFileName)).strip();
        }
        pgPool.setPassword(password);
        pgPool.setURL("jdbc:postgresql://" + System.getenv("COMFYDNS_DB_HOST") + "/");
        SimpleConnectionPool ret;
        ret = new SimpleConnectionPool(pgPool);
        ret.startPruning(cron);
        return ret;
    }
}
