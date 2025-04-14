package Serveur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServerMagasinAuchan {
    public static void main(String[] args) {
        try {
            // Se connecter au serveur central
            Registry registryCentral = LocateRegistry.getRegistry("localhost", 2099);
            IArticleServices centralArticleService = (IArticleServices) registryCentral.lookup("ArticleServices");
            ICommandeServices centralCommandeService = (ICommandeServices) registryCentral.lookup("CommandeServices");

            // Créer les services magasin qui vont utiliser le central
            ArticleServicesDelegue articleService = new ArticleServicesDelegue(centralArticleService);
            CommandeServicesDelegue commandeService = new CommandeServicesDelegue(centralCommandeService);

            IArticleServices stubArticle = (IArticleServices) UnicastRemoteObject.exportObject(articleService, 0);
            ICommandeServices stubCommande = (ICommandeServices) UnicastRemoteObject.exportObject(commandeService, 0);

            // Création du registre pour le magasin sur 1100
            Registry registry = LocateRegistry.createRegistry(1100);
            registry.bind("ArticleServices", stubArticle);
            registry.bind("CommandeServices", stubCommande);

            System.out.println("Serveur Magasin Auchan démarré sur le port 1100 !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
