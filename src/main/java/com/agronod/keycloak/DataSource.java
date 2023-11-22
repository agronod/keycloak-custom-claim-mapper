package com.agronod.keycloak;

import java.sql.Connection;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSource {
    private static final Map<String, HikariDataSource> dataSources = new HashMap<>();

    private DataSource() {
    }

    public static Connection getConnection(String connectionString, int maxPoolSize) throws SQLException {
        HikariDataSource ds = dataSources.get(connectionString);
        if (ds == null) {
            synchronized (dataSources) {
                ds = dataSources.get(connectionString);
                if (ds == null) {
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl(connectionString);
                    // Additional config settings can be set here
                    config.setMaximumPoolSize(maxPoolSize);
                    ds = new HikariDataSource(config);
                    dataSources.put(connectionString, ds);
                }
            }
        }
        return ds.getConnection();
    }
}