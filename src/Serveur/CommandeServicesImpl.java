package Serveur;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;
import java.sql.Date;
import java.util.Scanner;

public class CommandeServicesImpl implements ICommandeServices{
    private Connection connection;

    public CommandeServicesImpl() throws RemoteException {
        super();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestionstock", "root", "root");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean addArticleCommande(String nom, int id_commande, int quantiteCommande) throws RemoteException {
        try {
            // Recherche de l'article
            PreparedStatement psArticle = connection.prepareStatement(
                    "SELECT * FROM article WHERE nom_article = ?");
            psArticle.setString(1, nom);
            ResultSet rsArticle = psArticle.executeQuery();

            if (!rsArticle.next()) {
                System.out.println("Article avec le nom '" + nom + "' introuvable !");
                return false;
            }

            int reference = rsArticle.getInt("reference");
            float prix = rsArticle.getFloat("prix_unitaire");
            int stock = rsArticle.getInt("quantite_stocke");

            if (quantiteCommande > stock) {
                System.out.println("Quantité demandée supérieure au stock disponible !");
                return false;
            }

            // Vérifie si la commande existe
            PreparedStatement psCheckCommande = connection.prepareStatement(
                    "SELECT * FROM commande WHERE id_commande = ?");
            psCheckCommande.setInt(1, id_commande);
            ResultSet rsCommande = psCheckCommande.executeQuery();

            if (!rsCommande.next()) {
                // Crée une nouvelle commande
                PreparedStatement psCreateCommande = connection.prepareStatement(
                        "INSERT INTO commande (id_commande, date_commande, total_commande, quantite_commande, status) VALUES (?, ?, ?, ?, ?)");
                psCreateCommande.setInt(1, id_commande);
                psCreateCommande.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                psCreateCommande.setFloat(3, 0);
                psCreateCommande.setInt(4, 0);
                psCreateCommande.setString(5, "en attente");
                psCreateCommande.executeUpdate();
                System.out.println("Nouvelle commande créée avec l'id : " + id_commande);
            }

            // Ajout article_commande
            PreparedStatement psInsertCommandeArticle = connection.prepareStatement(
                    "INSERT INTO article_commande (id_commande, reference) VALUES (?, ?)");
            psInsertCommandeArticle.setInt(1, id_commande);
            psInsertCommandeArticle.setInt(2, reference);
            psInsertCommandeArticle.executeUpdate();

            // Mise à jour stock
            PreparedStatement psUpdateStock = connection.prepareStatement(
                    "UPDATE article SET quantite_stocke = quantite_stocke - ? WHERE reference = ?");
            psUpdateStock.setInt(1, quantiteCommande);
            psUpdateStock.setInt(2, reference);
            psUpdateStock.executeUpdate();

            // Mise à jour commande
            PreparedStatement psUpdateCommande = connection.prepareStatement(
                    "UPDATE commande SET total_commande = total_commande + ?, quantite_commande = quantite_commande + ? WHERE id_commande = ?");
            psUpdateCommande.setFloat(1, prix * quantiteCommande);
            psUpdateCommande.setInt(2, quantiteCommande);
            psUpdateCommande.setInt(3, id_commande);
            psUpdateCommande.executeUpdate();

            System.out.println("Article ajouté avec succès à la commande !");
            return true;

        } catch (SQLException e) {
            System.err.println("Erreur SQL dans addArticleCommande : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void genererFacture(int id_commande) throws RemoteException {
        try {
            String fileName = "facture_commande_" + id_commande + ".txt";
            FileWriter writer = new FileWriter(fileName);

            writer.write("===== 🧾 Facture de la commande ID " + id_commande + " =====\n\n");

            // 1. Détails des articles
            String query = """
        SELECT a.reference, a.nom_article, a.prix_unitaire
        FROM article a
        JOIN article_commande ca ON a.reference = ca.reference
        WHERE ca.id_commande = ?
        """;

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, id_commande);
            ResultSet rs = ps.executeQuery();

            writer.write(String.format("%-10s %-20s %-10s\n", "Réf", "Nom Article", "Prix"));
            writer.write("--------------------------------------------------\n");

            while (rs.next()) {
                int ref = rs.getInt("reference");
                String nom = rs.getString("nom_article");
                float prix = rs.getFloat("prix_unitaire");

                writer.write(String.format("%-10d %-20s %-10.2f\n", ref, nom, prix));
            }

            // 2. Total et quantite
            String totalQuery = "SELECT total_commande, quantite_commande FROM commande WHERE id_commande = ?";
            PreparedStatement psTotal = connection.prepareStatement(totalQuery);
            psTotal.setInt(1, id_commande);
            ResultSet rsTotal = psTotal.executeQuery();

            if (rsTotal.next()) {
                float total = rsTotal.getFloat("total_commande");
                int quantiteTotale = rsTotal.getInt("quantite_commande");

                writer.write("\n--------------------------------------------------\n");
                writer.write("Quantité totale : " + quantiteTotale + "\n");
                writer.write(String.format("TOTAL À PAYER : %.2f €\n", total));
            }

            writer.close();
            System.out.println("Facture générée dans le fichier : " + fileName);

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération de la facture : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
