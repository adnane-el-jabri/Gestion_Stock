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
        List<Article> articles = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM article");

            while (rs.next()) {
                int reference = rs.getInt("reference");
                String nom = rs.getString("nom_article");
                int quantite = rs.getInt("quantite_stocke");
                float prix = rs.getFloat("prix_unitaire");
                int id_famille = rs.getInt("id_famille");

                // Requête pour récupérer la famille de l'article
                PreparedStatement familleStmt = connection.prepareStatement(
                        "SELECT * FROM famille WHERE id_famille = ?");
                familleStmt.setInt(1, id_famille);
                ResultSet rsFamille = familleStmt.executeQuery();

                Famille famille = null;
                if (rsFamille.next()) {
                    String nomFamille = rsFamille.getString("nom_famille");
                    famille = new Famille(id_famille, nomFamille);
                }

                // Création de l'article
                Article article = new Article(reference,nom, quantite, prix, famille);
                articles.add(article);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return articles;
    }

    @Override
    public boolean addArticle(Article article) throws RemoteException {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO article (reference, nom_article, quantite_stocke, prix_unitaire, id_famille) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, article.getReference());
            ps.setString(2,article.getNom());
            ps.setInt(3, article.getStock());
            ps.setFloat(4, article.getPrix());
            ps.setInt(5, article.getFamille().getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int getQuantity(int reference) throws RemoteException {
        int quantity = 0;
        try{
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM article WHERE reference = ?");
            stmt.setInt(1, reference);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                quantity = rs.getInt("quantite_stocke");
            }
            return quantity;

        }catch (Exception e){
            e.printStackTrace();
        }
        return  quantity;
    }

    @Override
    public boolean updateQuantity(int reference, int quantity) throws RemoteException {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE article set quantite_stocke = ? where reference = ?");
            ps.setInt(1, quantity);
            ps.setInt(2, reference);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Article getArticleByRef(int ref) throws RemoteException {
        Article article = null;
        System.out.println("Recherche de l'article avec référence : " + ref);
        try {
            String sql = "SELECT * FROM article WHERE reference = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, ref);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int reference = rs.getInt("reference");
                String nom = rs.getString("nom_article");
                int quantite = rs.getInt("quantite_stocke");
                float prix = rs.getFloat("prix_unitaire");
                int id_famille = rs.getInt("id_famille");
                Famille famille = null;
                PreparedStatement familleStmt = connection.prepareStatement(
                        "SELECT * FROM famille WHERE id_famille = ?");
                familleStmt.setInt(1, id_famille);
                ResultSet rsFamille = familleStmt.executeQuery();
                if (rsFamille.next()) {
                    String nomFamille = rsFamille.getString("nom_famille");
                    famille = new Famille(id_famille, nomFamille);
                }
                article = new Article(reference, nom, quantite, prix, famille);
                System.out.println("Article construit : " + article);
            } else {
                System.out.println("Aucun article trouvé avec la référence : " + ref);
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL dans getArticleByRef : " + e.getMessage());
            e.printStackTrace();
        }

        return article;
    }

    @Override
    public List<Article> rechercherParFamille(String familleNom) throws RemoteException {
        List<Article> articles = new ArrayList<>();
        try {
            String sql = """
            SELECT a.*
            FROM article a
            JOIN famille f ON a.id_famille = f.id_famille
            WHERE f.nom_famille = ? AND a.quantite_stocke > 0
        """;
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, familleNom);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int reference = rs.getInt("reference");
                String nom = rs.getString("nom_article");
                int quantite = rs.getInt("quantite_stocke");
                float prix = rs.getFloat("prix_unitaire");
                int id_famille = rs.getInt("id_famille");

                Famille famille = new Famille(id_famille, familleNom);

                Article article = new Article(reference, nom, quantite, prix, famille);
                articles.add(article);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL dans rechercherParFamille : " + e.getMessage());
            e.printStackTrace();
        }
        return articles;
    }
    @Override
    public boolean updatePrix(int reference, float prix) throws RemoteException {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE article SET prix_unitaire = ? WHERE reference = ?");
            ps.setFloat(1, prix);
            ps.setInt(2, reference);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}