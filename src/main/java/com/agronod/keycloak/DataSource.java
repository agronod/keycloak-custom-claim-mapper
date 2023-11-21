// package com.agronod.keycloak;

// import java.sql.Connection;
// import java.sql.SQLException;
// import java.util.HashMap;

// import com.zaxxer.hikari.HikariConfig;
// import com.zaxxer.hikari.HikariDataSource;

// public class DataSource {

//     private static HikariConfig config = new HikariConfig();
//     private static HashMap<String, HikariDataSource> ds;

//     private static HikariDataSource createDatasource(String connectionString) {

//         int maxPoolSize = 2;
//         try {
//             maxPoolSize = Integer.parseInt(System.getProperty("DB_JDBC_POOL_SIZE"));
//         } catch (NumberFormatException e) {
//         }

//         config.setJdbcUrl(connectionString);
//         // config.addDataSourceProperty( "cachePrepStmts" , "true" );
//         // config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
//         // config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
//         config.setMaximumPoolSize(maxPoolSize);
//         return new HikariDataSource(config);
//     }

//     private DataSource() {
//     }

//     public static Connection getConnection(String connString) throws SQLException {
//         HikariDataSource datasource = ds.get(connString);
//         if (datasource == null) {

//         }
//         return ds.getConnection();
//     }
// }

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
                    // int maxPoolSize = 2; // Default pool size, adjust as needed

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
