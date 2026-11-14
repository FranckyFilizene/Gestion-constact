package com.contact.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class Database {

    private static HikariDataSource dataSource;

    static {
        try (InputStream input = Database.class.getClassLoader().getResourceAsStream("db.properties")){
            Properties prop = new Properties();
            if(input == null) {
                throw new RuntimeException("Impossible de trouver le fichier db.propertie dans les ressources");
            }
            prop.load(input);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(prop.getProperty("db.url"));
            config.setUsername(prop.getProperty("db.user"));
            config.setPassword(prop.getProperty("db.password"));

            //Options de performance recommandées;
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(10000);

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            throw new RuntimeException("Error lors de l'initialisation du pool de connexions de la base de données : " + e.getMessage(), e);
        }
    }

    //Founit une connexion active a la base de données.
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    //Ferme proprement le pool de connexion a l'arret de l'application
    public static  void close(){
        if(dataSource != null && !dataSource.isClosed()){
            dataSource.close();;
        }
    }
}
