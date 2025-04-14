package Serveur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServerCentral {
    public static void main(String[] args) {
        try {
            // Créer les instances des services
            ArticleServicesImpl articleService = new ArticleServicesImpl();
            CommandeServicesImpl commandeService = new CommandeServicesImpl();

            // Exporter les objets
            IArticleServices stubArticle = (IArticleServices) UnicastRemoteObject.exportObject(articleService, 0);
            ICommandeServices stubCommande = (ICommandeServices) UnicastRemoteObject.exportObject(commandeService, 0);

            // Créer le registre sur le port 1099
            Registry registry = LocateRegistry.createRegistry(2099);

            // Binder les deux services
            registry.bind("ArticleServices", stubArticle);
            registry.bind("CommandeServices", stubCommande);

            System.out.println("Serveur Central démarré avec succès sur le port 2099 !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
