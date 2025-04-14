package Serveur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServerMagasinG20 {
    public static void main(String[] args) {
        try {
            Registry registryCentral = LocateRegistry.getRegistry("localhost", 2099);
            IArticleServices centralArticleService = (IArticleServices) registryCentral.lookup("ArticleServices");
            ICommandeServices centralCommandeService = (ICommandeServices) registryCentral.lookup("CommandeServices");

            ArticleServicesDelegue articleService = new ArticleServicesDelegue(centralArticleService);
            CommandeServicesDelegue commandeService = new CommandeServicesDelegue(centralCommandeService);

            IArticleServices stubArticle = (IArticleServices) UnicastRemoteObject.exportObject(articleService, 0);
            ICommandeServices stubCommande = (ICommandeServices) UnicastRemoteObject.exportObject(commandeService, 0);

            Registry registry = LocateRegistry.createRegistry(1102);
            registry.bind("ArticleServices", stubArticle);
            registry.bind("CommandeServices", stubCommande);

            System.out.println("Serveur Magasin G20 démarré sur le port 1102 !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
