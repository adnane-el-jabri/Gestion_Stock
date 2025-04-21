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
            connection = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean addArticleCommande(String nom, int id_commande, int quantiteCommande) throws RemoteException {
        try {
            // Recherche de l'article à partir de son nom
            PreparedStatement psArticle = connection.prepareStatement("SELECT * FROM article WHERE nom_article = ?");
            psArticle.setString(1, nom);
            ResultSet rsArticle = psArticle.executeQuery();

            // Si l'article n'existe pas, on retourne une erreur
            if (!rsArticle.next()) {
                System.out.println("Article introuvable !");
                return false;
            }

            int reference = rsArticle.getInt("reference");
            float prix = rsArticle.getFloat("prix_unitaire");
            int stock = rsArticle.getInt("quantite_stocke");

            // Vérifie si la quantité demandée est disponible en stock
            if (quantiteCommande > stock) {
                System.out.println("Quantité demandée > stock disponible");
                return false;
            }

            // Vérifie si la commande existe, sinon la créer
            PreparedStatement psCheckCommande = connection.prepareStatement("SELECT * FROM commande WHERE id_commande = ?");
            psCheckCommande.setInt(1, id_commande);
            ResultSet rsCommande = psCheckCommande.executeQuery();

            if (!rsCommande.next()) {
                // Création d'une nouvelle commande avec statut "en attente"
                PreparedStatement psCreateCommande = connection.prepareStatement(
                        "INSERT INTO commande (id_commande, date_commande, total_commande, quantite_commande, status) VALUES (?, ?, 0, 0, 'en attente')");
                psCreateCommande.setInt(1, id_commande);
                psCreateCommande.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                psCreateCommande.executeUpdate();
            }

            // Vérifie si l'article est déjà présent dans la commande
            PreparedStatement psCheckArticle = connection.prepareStatement(
                    "SELECT * FROM article_commande WHERE id_commande = ? AND reference = ?");
            psCheckArticle.setInt(1, id_commande);
            psCheckArticle.setInt(2, reference);
            ResultSet rsCheck = psCheckArticle.executeQuery();

            if (rsCheck.next()) {
                // Si l'article existe déjà, on met à jour la quantité commandée
                PreparedStatement psUpdateArticle = connection.prepareStatement(
                        "UPDATE article_commande SET quantite = quantite + ? WHERE id_commande = ? AND reference = ?");
                psUpdateArticle.setInt(1, quantiteCommande);
                psUpdateArticle.setInt(2, id_commande);
                psUpdateArticle.setInt(3, reference);
                psUpdateArticle.executeUpdate();
            } else {
                // Sinon, on insère une nouvelle ligne dans la table article_commande
                PreparedStatement psInsertArticle = connection.prepareStatement(
                        "INSERT INTO article_commande (id_commande, reference, quantite) VALUES (?, ?, ?)");
                psInsertArticle.setInt(1, id_commande);
                psInsertArticle.setInt(2, reference);
                psInsertArticle.setInt(3, quantiteCommande);
                psInsertArticle.executeUpdate();
            }

            // Mise à jour du stock dans la table article
            PreparedStatement psUpdateStock = connection.prepareStatement(
                    "UPDATE article SET quantite_stocke = quantite_stocke - ? WHERE reference = ?");
            psUpdateStock.setInt(1, quantiteCommande);
            psUpdateStock.setInt(2, reference);
            psUpdateStock.executeUpdate();

            // Mise à jour du total et de la quantité de la commande
            PreparedStatement psUpdateCommande = connection.prepareStatement(
                    "UPDATE commande SET total_commande = total_commande + ?, quantite_commande = quantite_commande + ? WHERE id_commande = ?");
            psUpdateCommande.setFloat(1, prix * quantiteCommande);
            psUpdateCommande.setInt(2, quantiteCommande);
            psUpdateCommande.setInt(3, id_commande);
            psUpdateCommande.executeUpdate();

            System.out.println("Article ajouté ou mis à jour dans la commande !");
            return true;

        } catch (SQLException e) {
            // Gestion des erreurs SQL
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean payerCommande(int id_commande, String modepaiement) throws RemoteException {
        try {
            // Étape 1 : Vérifier l'existence de la commande
            PreparedStatement checkCommande = connection.prepareStatement(
                    "SELECT id_commande FROM commande WHERE id_commande = ?"
            );
            checkCommande.setInt(1, id_commande);
            ResultSet rs = checkCommande.executeQuery();

            if (!rs.next()) {
                // Si la commande n'existe pas, afficher un message et retourner false
                System.out.println("Commande inexistante avec ID : " + id_commande);
                return false;
            }

            // Étape 2 : Mettre à jour le statut de la commande et enregistrer le mode de paiement
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE commande SET status = ?, mode_paiement = ? WHERE id_commande = ?"
            );
            ps.setString(1, "Payé");               // Mise à jour du statut
            ps.setString(2, modepaiement);         // Enregistrement du mode de paiement (ex: carte, espèce...)
            ps.setInt(3, id_commande);             // Cible la commande par son ID

            ps.executeUpdate(); // Exécute la mise à jour

            // Message de confirmation en console
            System.out.println("Commande " + id_commande + " payée avec succès en mode " + modepaiement);
            return true;

        } catch (SQLException e) {
            // Gestion des erreurs SQL
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public float chiffreAffaireParDate(Date date) throws RemoteException {
        float chiffreAffaire = 0f;
        try {
            // Préparation de la requête SQL pour calculer le chiffre d'affaires total à une date donnée
            String sql = "SELECT SUM(total_commande) AS chiffre_affaire FROM commande WHERE DATE(date_commande) = ?";
            PreparedStatement ps = connection.prepareStatement(sql);

            // On passe la date en paramètre (attention à bien utiliser java.sql.Date)
            ps.setDate(1, (java.sql.Date) date);
            ResultSet rs = ps.executeQuery();

            // Si un résultat est retourné, on extrait la somme
            if (rs.next()) {
                chiffreAffaire = rs.getFloat("chiffre_affaire");
            }

        } catch (SQLException e) {
            // En cas d'erreur SQL, on affiche un message d'erreur détaillé
            System.err.println("Erreur SQL dans chiffreAffaireParDate : " + e.getMessage());
            e.printStackTrace();
        }

        // Retourne le chiffre d’affaires calculé (0 si aucune commande)
        return chiffreAffaire;
    }

    @Override
    public boolean commandeExiste(int id_commande) throws RemoteException {
        try {
            // Préparation de la requête pour vérifier l'existence d'une commande par son ID
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM commande WHERE id_commande = ?"
            );
            ps.setInt(1, id_commande); // Affectation de l'ID à rechercher
            ResultSet rs = ps.executeQuery();

            // Si un résultat est trouvé, cela signifie que la commande existe
            return rs.next();

        } catch (Exception e) {
            // En cas d'erreur, on affiche la stack trace (à remplacer par un logger en production)
            e.printStackTrace();
        }

        // Retourne false si la commande n'existe pas ou en cas d'erreur
        return false;
    }
    @Override
    public void genererFacture(int id_commande, String magasin) throws RemoteException {
        try {
            // Construction du nom de fichier (ex : facture_commande_5.txt)
            String fileName = "facture_commande_" + id_commande + ".txt";
            FileWriter writer = new FileWriter(fileName);

            // En-tête de la facture
            writer.write("===== Facture de la commande ID " + id_commande + " =====\n\n");
            writer.write("Magasin : " + magasin + "\n\n");

            // Requête SQL pour obtenir les articles de la commande (quantité, prix, etc.)
            String query = "SELECT a.reference, a.nom_article, a.prix_unitaire, ca.quantite " +
                    "FROM article a JOIN article_commande ca ON a.reference = ca.reference " +
                    "WHERE ca.id_commande = ?";

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, id_commande);
            ResultSet rs = ps.executeQuery();

            // Entête du tableau d'articles
            writer.write(String.format("%-10s %-20s %-10s %-10s %-10s\n", "Réf", "Nom Article", "Prix", "Quantité", "Total"));
            writer.write("---------------------------------------------------------------\n");

            // Pour chaque article, afficher les infos + calcul du total
            while (rs.next()) {
                int ref = rs.getInt("reference");
                String nom = rs.getString("nom_article");
                float prix = rs.getFloat("prix_unitaire");
                int quantite = rs.getInt("quantite");

                writer.write(String.format("%-10d %-20s %-10.2f %-10d %-10.2f\n",
                        ref, nom, prix, quantite, prix * quantite));
            }

            // Requête pour obtenir les informations globales de la commande
            String totalQuery = "SELECT total_commande, quantite_commande, mode_paiement, status FROM commande WHERE id_commande = ?";
            PreparedStatement psTotal = connection.prepareStatement(totalQuery);
            psTotal.setInt(1, id_commande);
            ResultSet rsTotal = psTotal.executeQuery();

            // Affichage des totaux et statut
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

            // Ajout de la date du jour
            writer.write("Date : " + LocalDate.now() + "\n");
            writer.close();

            System.out.println("Facture générée avec succès !");
        } catch (Exception e) {
            // Affiche les erreurs éventuelles lors de la génération
            e.printStackTrace();
        }
    }


}
