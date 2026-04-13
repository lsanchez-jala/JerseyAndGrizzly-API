package product.management.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceProvider {

    public static DataSource create() {
        String host     = getEnv("DB_HOST",     "localhost");
        String port     = getEnv("DB_PORT",     "5432");
        String db       = getEnv("DB_NAME",     "appdb");
        String user     = getEnv("DB_USER",     "appuser");
        String password = getEnv("DB_PASSWORD", "secret");

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + db);
        cfg.setUsername(user);
        cfg.setPassword(password);
        cfg.setMaximumPoolSize(10);
        cfg.setAutoCommit(true);
        return new HikariDataSource(cfg);
    }

    private static String getEnv(String key, String fallback) {
        String v = System.getenv(key);
        return (v != null && !v.isBlank()) ? v : fallback;
    }
}
