package com.agronod.keycloak;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSource {

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {
        String connecString = System.getProperty("DB_JDBC_URL");
        int maxPoolSize = 2;
        try {
            maxPoolSize = Integer.parseInt(System.getProperty("DB_JDBC_POOL_SIZE"));
        } catch (NumberFormatException e) {
        }

        config.setJdbcUrl(connecString);
        // config.addDataSourceProperty( "cachePrepStmts" , "true" );
        // config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        // config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        config.setMaximumPoolSize(maxPoolSize);
        ds = new HikariDataSource(config);
    }

    private DataSource() {
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}