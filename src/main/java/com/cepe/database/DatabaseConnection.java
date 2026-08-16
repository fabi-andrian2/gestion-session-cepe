package com.cepe.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton de connexion JDBC à PostgreSQL.
 * Charge sa configuration depuis le fichier {@code db.properties} situé dans {@code src/main/resources}.
 */
public final class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String PROPERTIES_FILE = "db.properties";

    private static volatile DatabaseConnection instance;
    private final String url;
    private final String user;
    private final String password;

    /**
     * Constructeur privé (pattern Singleton).
     * Charge les propriétés de connexion depuis le fichier de ressources.
     */
    private DatabaseConnection() {
        Properties props = loadProperties();
        this.url = props.getProperty("db.url");
        this.user = props.getProperty("db.user");
        this.password = props.getProperty("db.password");

        if (this.url == null || this.user == null || this.password == null) {
            throw new IllegalStateException(
                "Propriétés de connexion manquantes dans " + PROPERTIES_FILE +
                ". Attendu : db.url, db.user, db.password"
            );
        }

        // Chargement explicite du driver (optionnel avec JDBC 4.0+, mais explicite et robuste)
        try {
            Class.forName("org.postgresql.Driver");
            LOGGER.info("Driver PostgreSQL chargé avec succès.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL introuvable dans le classpath.", e);
        }
    }

    /**
     * Retourne l'instance unique de {@code DatabaseConnection} (double-checked locking).
     *
     * @return l'instance Singleton
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Ouvre et retourne une nouvelle connexion JDBC à la base PostgreSQL.
     * L'appelant est responsable de la fermeture de la connexion.
     *
     * @return une connexion JDBC active
     * @throws SQLException en cas d'échec de connexion
     */
    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
        LOGGER.log(Level.FINE, "Nouvelle connexion JDBC ouverte : {0}", connection);
        return connection;
    }

    /**
     * Charge le fichier de propriétés depuis le classpath.
     *
     * @return les propriétés chargées
     */
    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new IllegalStateException(
                    "Fichier de propriétés introuvable : " + PROPERTIES_FILE +
                    ". Veuillez le créer dans src/main/resources/"
                );
            }
            props.load(input);
            LOGGER.info("Fichier de propriétés chargé : " + PROPERTIES_FILE);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger le fichier de propriétés.", e);
        }
        return props;
    }
}