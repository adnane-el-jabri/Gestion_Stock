package Serveur;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/gestionstock";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private Connection connection;

    // Instance unique (Singleton)
    private static DatabaseConnection instance;

    // Constructeur privé pour empêcher l’instanciation externe
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données !");
            e.printStackTrace();
        }
    }

    // Méthode d’accès au Singleton
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // Accès à la connexion
    public Connection getConnection() {
        return connection;
    }
}
