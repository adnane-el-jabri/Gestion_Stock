package Client;

import Model.Article;
import Model.Famille;
import Serveur.IArticleServices;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        try {
            // Récupérer le registre
            Registry reg = LocateRegistry.getRegistry(null);

            // Recherche dans le registre de l'objet distant
            IArticleServices stub = (IArticleServices) reg.lookup("ArticleServices");

            // Appel de la méthode distante à l'aide de l'objet obtenu
            for(Article article : stub.getArticles()) {
                System.out.println(article.toString());
            }
            //Famille fm = new Famille(2,"voiture");
            //System.out.println(stub.addArticle(new Article(4,"article",54,23,fm)));

        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
        }
    }
}
