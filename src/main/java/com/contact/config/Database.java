
package com.contact.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class Database {

    private static HikariDataSource dataSource;

    // Initialisation du pool de connexions
    static {
        try {
            Properties prop = chargerConfiguration();

            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(prop.getProperty("db.url"));
            config.setUsername(prop.getProperty("db.user"));
            config.setPassword(prop.getProperty("db.password"));

            // Options de performance
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(10000);

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur lors de l'initialisation du pool de connexions : "
                            + e.getMessage(), e
            );
        }
    }

    // Chargement de la configuration
    private static Properties chargerConfiguration() throws IOException {

        Properties prop = new Properties();

        // Recherche prioritaire du fichier externe
        Path dossierApplication = getApplicationDirectory();

        Path[] cheminsExternes = {
                dossierApplication.resolve("db.properties"),
                dossierApplication.resolve("config").resolve("db.properties")
        };

        for (Path chemin : cheminsExternes) {

            File fichier = chemin.toFile();

            System.out.println(
                    "[DB] Recherche du fichier : " + fichier.getAbsolutePath()
            );

            if (fichier.exists() && fichier.isFile()) {

                try (InputStream input = new FileInputStream(fichier)) {

                    prop.load(input);

                    System.out.println(
                            "[DB] Configuration externe chargée : "
                                    + fichier.getAbsolutePath()
                    );

                    System.out.println(
                            "[DB] URL utilisée : " + prop.getProperty("db.url")
                    );

                    return prop;
                }
            }
        }

        // Repli sur les ressources internes
        System.out.println(
                "[DB] Aucun fichier externe trouvé. Recherche dans les ressources..."
        );

        try (InputStream input =
                     Database.class.getClassLoader()
                             .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new IOException(
                        "Impossible de trouver db.properties, "
                                + "ni à l'extérieur ni dans les ressources."
                );
            }

            prop.load(input);

            System.out.println(
                    "[DB] Configuration chargée depuis les ressources internes."
            );

            return prop;
        }
    }

    // Détermination du dossier de l'application
    private static Path getApplicationDirectory() {

        // Dossier transmis par le lanceur jpackage
        String appDirectory = System.getProperty("app.dir");

        if (appDirectory != null && !appDirectory.isBlank()) {
            return Paths.get(appDirectory)
                    .toAbsolutePath()
                    .normalize();
        }

        // Répertoire du projet pendant le développement
        return Paths.get("")
                .toAbsolutePath()
                .normalize();
    }

    // Fournit une connexion à la base de données
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Fermeture du pool de connexions
    public static void close() {

        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
