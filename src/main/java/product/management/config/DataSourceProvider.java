package product.management.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.util.Properties;

public class DataSourceProvider {

    public static DataSource create(Properties properties) {
        String url      = properties.getProperty("jersey.datasource.url");
        String user     = properties.getProperty("jersey.datasource.username");
        String password = properties.getProperty("jersey.datasource.password");

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
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
