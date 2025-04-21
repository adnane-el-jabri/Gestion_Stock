package Serveur;

import Model.Article;
import Model.Famille;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleServicesImpl  implements IArticleServices {
    private Connection connection;

    public ArticleServicesImpl() throws RemoteException {
        super();
        try {
            connection = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Article> getArticles() throws RemoteException {
        // Initialisation d'une liste pour stocker les articles récupérés depuis la base
        List<Article> articles = new ArrayList<>();

        try {
            // Création d'une instruction SQL pour exécuter la requête de sélection
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM article");

            // Parcours des résultats de la requête
            while (rs.next()) {
                // Extraction des champs de l'article depuis le ResultSet
                int reference = rs.getInt("reference");
                String nom = rs.getString("nom_article");
                int quantite = rs.getInt("quantite_stocke");
                float prix = rs.getFloat("prix_unitaire");
                int id_famille = rs.getInt("id_famille");

                // Préparation de la requête pour récupérer la famille associée à l'article
                PreparedStatement familleStmt = connection.prepareStatement(
                        "SELECT * FROM famille WHERE id_famille = ?");
                familleStmt.setInt(1, id_famille);
                ResultSet rsFamille = familleStmt.executeQuery();

                // Initialisation de l'objet Famille à null (au cas où aucune famille ne correspond)
                Famille famille = null;

                // Si une famille correspond à l'identifiant, on la construit
                if (rsFamille.next()) {
                    String nomFamille = rsFamille.getString("nom_famille");
                    famille = new Famille(id_famille, nomFamille);
                }

                // Création de l'objet Article avec ses données et la famille associée
                Article article = new Article(reference, nom, quantite, prix, famille);

                // Ajout de l'article à la liste finale
                articles.add(article);
            }

        } catch (SQLException e) {
            // Affichage d'une erreur en cas de problème avec la base de données
            e.printStackTrace();
        }

        // Retour de la liste des articles
        return articles;
    }
    @Override
    public boolean addArticle(Article article) throws RemoteException {
        try {
            // Préparation de la requête d'insertion dans la table `article`
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO article (reference, nom_article, quantite_stocke, prix_unitaire, id_famille) " +
                            "VALUES (?, ?, ?, ?, ?)");

            // Injection des valeurs de l'article dans la requête
            ps.setInt(1, article.getReference());               // Clé primaire
            ps.setString(2, article.getNom());                  // Nom de l'article
            ps.setInt(3, article.getStock());                   // Quantité en stock
            ps.setFloat(4, article.getPrix());                  // Prix unitaire
            ps.setInt(5, article.getFamille().getId());         // ID de la famille associée

            // Exécution de la requête ; retourne true si au moins une ligne est insérée
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // En cas d'erreur SQL (clé en double, violation contrainte, etc.)
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public int getQuantity(int reference) throws RemoteException {
        int quantity = 0;

        try {
            // Préparation de la requête pour récupérer un article par sa référence
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM article WHERE reference = ?"
            );
            stmt.setInt(1, reference); // Injection du paramètre dans la requête

            ResultSet rs = stmt.executeQuery(); // Exécution de la requête

            // Si un article est trouvé, on récupère la quantité stockée
            while (rs.next()) {
                quantity = rs.getInt("quantite_stocke");
            }

            // Retour de la quantité trouvée (0 si non trouvé)
            return quantity;

        } catch (Exception e) {
            // Affichage de l’erreur (à remplacer par du logging dans une vraie app)
            e.printStackTrace();
        }

        // En cas d'erreur, on retourne 0 (ce qui peut être confus si l'article existe vraiment)
        return quantity;
    }


    @Override
    public boolean updateQuantity(int reference, int quantity) throws RemoteException {
        try {
            // Préparation de la requête pour mettre à jour la quantité stockée d’un article donné
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE article SET quantite_stocke = ? WHERE reference = ?"
            );
            ps.setInt(1, quantity);   // Nouvelle quantité
            ps.setInt(2, reference);  // Référence de l’article à mettre à jour

            // Exécute la mise à jour et retourne true si au moins une ligne a été modifiée
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            // Affiche l’erreur (dans une vraie app : logger au lieu de `printStackTrace`)
            e.printStackTrace();
        }

        // En cas d’erreur, retourne false
        return false;
    }
    @Override
    public Article getArticleByRef(int ref) throws RemoteException {
        Article article = null;

        // Affichage dans la console pour suivre la recherche (utile pour le debug)
        System.out.println("Recherche de l'article avec référence : " + ref);

        try {
            // Préparation de la requête SQL pour récupérer l'article par sa référence
            String sql = "SELECT * FROM article WHERE reference = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, ref);

            // Exécution de la requête
            ResultSet rs = ps.executeQuery();

            // Vérifie si un article a été trouvé
            if (rs.next()) {
                // Récupération des informations de l'article
                int reference = rs.getInt("reference");
                String nom = rs.getString("nom_article");
                int quantite = rs.getInt("quantite_stocke");
                float prix = rs.getFloat("prix_unitaire");
                int id_famille = rs.getInt("id_famille");

                Famille famille = null;

                // Requête pour récupérer les informations de la famille associée
                PreparedStatement familleStmt = connection.prepareStatement(
                        "SELECT * FROM famille WHERE id_famille = ?"
                );
                familleStmt.setInt(1, id_famille);
                ResultSet rsFamille = familleStmt.executeQuery();

                if (rsFamille.next()) {
                    String nomFamille = rsFamille.getString("nom_famille");
                    famille = new Famille(id_famille, nomFamille); // Création de l'objet famille
                }

                // Création de l'objet Article avec toutes les informations récupérées
                article = new Article(reference, nom, quantite, prix, famille);
                System.out.println("Article construit : " + article); // Pour vérification visuelle en console

            } else {
                // Aucun article correspondant trouvé
                System.out.println("Aucun article trouvé avec la référence : " + ref);
            }

        } catch (SQLException e) {
            // Affiche l'erreur SQL complète pour aider au debug
            System.err.println("Erreur SQL dans getArticleByRef : " + e.getMessage());
            e.printStackTrace();
        }

        // Retourne l'article trouvé ou null si aucun n’a été trouvé ou en cas d’erreur
        return article;
    }
    @Override
    public List<Article> rechercherParFamille(String familleNom) throws RemoteException {
        // Liste qui contiendra tous les articles trouvés
        List<Article> articles = new ArrayList<>();

        try {
            // Requête SQL : sélectionne les articles appartenant à une famille donnée
            // ET dont la quantité en stock est supérieure à 0
            String sql = """
            SELECT a.*
            FROM article a
            JOIN famille f ON a.id_famille = f.id_famille
            WHERE f.nom_famille = ? AND a.quantite_stocke > 0
        """;

            // Préparation de la requête avec le nom de la famille en paramètre
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, familleNom);

            // Exécution de la requête
            ResultSet rs = ps.executeQuery();

            // Parcours des résultats retournés
            while (rs.next()) {
                // Récupération des informations de l'article
                int reference = rs.getInt("reference");
                String nom = rs.getString("nom_article");
                int quantite = rs.getInt("quantite_stocke");
                float prix = rs.getFloat("prix_unitaire");
                int id_famille = rs.getInt("id_famille");

                // Création de l'objet Famille avec le nom fourni (pas besoin de le recharger depuis la DB ici)
                Famille famille = new Famille(id_famille, familleNom);

                // Création de l'objet Article
                Article article = new Article(reference, nom, quantite, prix, famille);

                // Ajout à la liste des résultats
                articles.add(article);
            }

        } catch (SQLException e) {
            // En cas d’erreur SQL, on affiche un message d'erreur détaillé
            System.err.println("Erreur SQL dans rechercherParFamille : " + e.getMessage());
            e.printStackTrace();
        }

        // Retourne la liste des articles trouvés (peut être vide si aucun résultat)
        return articles;
    }

    @Override
    public boolean updatePrix(int reference, float prix) throws RemoteException {
        try {
            // Préparation de la requête SQL pour mettre à jour le prix unitaire d’un article spécifique
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE article SET prix_unitaire = ? WHERE reference = ?"
            );

            // Injection des paramètres dans la requête : nouveau prix et référence de l’article ciblé
            ps.setFloat(1, prix);        // Nouveau prix
            ps.setInt(2, reference);     // Référence de l'article à mettre à jour

            // Exécution de la mise à jour
            // Retourne true si au moins une ligne a été modifiée
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // En cas d'erreur SQL, on affiche la pile pour faciliter le débogage
            e.printStackTrace();
        }

        // Si une erreur survient, on retourne false pour signaler l’échec
        return false;
    }


}