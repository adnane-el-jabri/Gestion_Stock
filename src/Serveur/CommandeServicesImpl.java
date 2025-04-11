package Serveur;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Date;

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
            PreparedStatement psArticle = connection.prepareStatement("SELECT * FROM article WHERE nom_article = ?");
            psArticle.setString(1, nom);
            ResultSet rsArticle = psArticle.executeQuery();

            if (!rsArticle.next()) {
                System.out.println("Article introuvable !");
                return false;
            }

            int reference = rsArticle.getInt("reference");
            float prix = rsArticle.getFloat("prix_unitaire");
            int stock = rsArticle.getInt("quantite_stocke");

            if (quantiteCommande > stock) {
                System.out.println("Quantité demandée > stock disponible");
                return false;
            }

            // Si commande n'existe pas, créer
            PreparedStatement psCheckCommande = connection.prepareStatement("SELECT * FROM commande WHERE id_commande = ?");
            psCheckCommande.setInt(1, id_commande);
            ResultSet rsCommande = psCheckCommande.executeQuery();

            if (!rsCommande.next()) {
                PreparedStatement psCreateCommande = connection.prepareStatement(
                        "INSERT INTO commande (id_commande, date_commande, total_commande, quantite_commande, status) VALUES (?, ?, 0, 0, 'en attente')");
                psCreateCommande.setInt(1, id_commande);
                psCreateCommande.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                psCreateCommande.executeUpdate();
            }

            // Check si article existe dans la commande
            PreparedStatement psCheckArticle = connection.prepareStatement(
                    "SELECT * FROM article_commande WHERE id_commande = ? AND reference = ?");
            psCheckArticle.setInt(1, id_commande);
            psCheckArticle.setInt(2, reference);
            ResultSet rsCheck = psCheckArticle.executeQuery();

            if (rsCheck.next()) {
                // Update la quantité
                PreparedStatement psUpdateArticle = connection.prepareStatement(
                        "UPDATE article_commande SET quantite = quantite + ? WHERE id_commande = ? AND reference = ?");
                psUpdateArticle.setInt(1, quantiteCommande);
                psUpdateArticle.setInt(2, id_commande);
                psUpdateArticle.setInt(3, reference);
                psUpdateArticle.executeUpdate();
            } else {
                // Insert nouvelle ligne
                PreparedStatement psInsertArticle = connection.prepareStatement(
                        "INSERT INTO article_commande (id_commande, reference, quantite) VALUES (?, ?, ?)");
                psInsertArticle.setInt(1, id_commande);
                psInsertArticle.setInt(2, reference);
                psInsertArticle.setInt(3, quantiteCommande);
                psInsertArticle.executeUpdate();
            }

            // Mise à jour du stock
            PreparedStatement psUpdateStock = connection.prepareStatement(
                    "UPDATE article SET quantite_stocke = quantite_stocke - ? WHERE reference = ?");
            psUpdateStock.setInt(1, quantiteCommande);
            psUpdateStock.setInt(2, reference);
            psUpdateStock.executeUpdate();

            // Mise à jour commande (total, quantite)
            PreparedStatement psUpdateCommande = connection.prepareStatement(
                    "UPDATE commande SET total_commande = total_commande + ?, quantite_commande = quantite_commande + ? WHERE id_commande = ?");
            psUpdateCommande.setFloat(1, prix * quantiteCommande);
            psUpdateCommande.setInt(2, quantiteCommande);
            psUpdateCommande.setInt(3, id_commande);
            psUpdateCommande.executeUpdate();

            System.out.println("Article ajouté ou mis à jour dans la commande !");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean payerCommande(int id_commande, String modepaiement) throws RemoteException {
        try {
            //Premièrement vérifier si la commande existe
            PreparedStatement checkCommande = connection.prepareStatement(
                    "SELECT id_commande FROM commande WHERE id_commande = ?"
            );
            checkCommande.setInt(1, id_commande);
            ResultSet rs = checkCommande.executeQuery();

            if (!rs.next()) {
                System.out.println("Commande inexistante avec ID : " + id_commande);
                return false; // La commande n'existe pas
            }

            //Puis faire l'update
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE commande SET status = ?, mode_paiement = ? WHERE id_commande = ?"
            );
            ps.setString(1, "Payé");
            ps.setString(2, modepaiement);
            ps.setInt(3, id_commande);

            ps.executeUpdate();
            System.out.println("Commande " + id_commande + " payée avec succès en mode " + modepaiement);
            return true; // On retourne TRUE car on considère que la commande est maintenant bien payée

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public float chiffreAffaireParDate(Date date) throws RemoteException {
        float chiffreAffaire = 0f;
        try {
            String sql = "SELECT SUM(total_commande) AS chiffre_affaire FROM commande WHERE DATE(date_commande) = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setDate(1, (java.sql.Date) date);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                chiffreAffaire = rs.getFloat("chiffre_affaire");
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL dans chiffreAffaireParDate : " + e.getMessage());
            e.printStackTrace();
        }
        return chiffreAffaire;
    }

    @Override
    public boolean commandeExiste(int id_commande) throws RemoteException {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM commande WHERE id_commande = ?");
            ps.setInt(1, id_commande);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void genererFacture(int id_commande, String magasin) throws RemoteException {
        try {
            String fileName = "facture_commande_" + id_commande + ".txt";
            FileWriter writer = new FileWriter(fileName);

            writer.write("===== Facture de la commande ID " + id_commande + " =====\n\n");
            writer.write("Magasin : " + magasin + "\n\n");

            // Affichage détaillé
            String query = "SELECT a.reference, a.nom_article, a.prix_unitaire, ca.quantite " +
                    "FROM article a JOIN article_commande ca ON a.reference = ca.reference " +
                    "WHERE ca.id_commande = ?";

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, id_commande);
            ResultSet rs = ps.executeQuery();

            writer.write(String.format("%-10s %-20s %-10s %-10s %-10s\n", "Réf", "Nom Article", "Prix", "Quantité", "Total"));
            writer.write("---------------------------------------------------------------\n");

            while (rs.next()) {
                int ref = rs.getInt("reference");
                String nom = rs.getString("nom_article");
                float prix = rs.getFloat("prix_unitaire");
                int quantite = rs.getInt("quantite");

                writer.write(String.format("%-10d %-20s %-10.2f %-10d %-10.2f\n",
                        ref, nom, prix, quantite, prix * quantite));
            }

            // Infos totales
            String totalQuery = "SELECT total_commande, quantite_commande, mode_paiement, status FROM commande WHERE id_commande = ?";
            PreparedStatement psTotal = connection.prepareStatement(totalQuery);
            psTotal.setInt(1, id_commande);
            ResultSet rsTotal = psTotal.executeQuery();

            if (rsTotal.next()) {
                float total = rsTotal.getFloat("total_commande");
                int quantiteTotale = rsTotal.getInt("quantite_commande");
                String status = rsTotal.getString("status");
                String modePaiement = rsTotal.getString("mode_paiement");

                writer.write("\n---------------------------------------------------------------\n");
                writer.write("Quantité totale : " + quantiteTotale + "\n");
                writer.write(String.format("TOTAL À PAYER : %.2f €\n", total));
                writer.write("\n---------------------------------------------------------------\n");
                writer.write("Status : " + status + "\n");
                writer.write("Mode de Paiement : " + modePaiement + "\n");
            }

            writer.write("Date : " + LocalDate.now() + "\n");
            writer.close();

            System.out.println("Facture générée avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
