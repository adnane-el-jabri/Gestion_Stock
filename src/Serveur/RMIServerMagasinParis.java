package Serveur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
public class RMIServerMagasinParis {
    public static void main(String[] args) {
        try {
            // Connexion au registre RMI du serveur central (port 2099)
            Registry registryCentral = LocateRegistry.getRegistry("localhost", 2099);

            // Récupération des services distants centralisés
            IArticleServices centralArticleService = (IArticleServices) registryCentral.lookup("ArticleServices");
            ICommandeServices centralCommandeService = (ICommandeServices) registryCentral.lookup("CommandeServices");

            // Création des objets délégués pour ce magasin (utilisent les services du central)
            ArticleServicesDelegue articleService = new ArticleServicesDelegue(centralArticleService);
            CommandeServicesDelegue commandeService = new CommandeServicesDelegue(centralCommandeService);

            // Exportation des objets RMI (création des stubs)
            IArticleServices stubArticle = (IArticleServices) UnicastRemoteObject.exportObject(articleService, 0);
            ICommandeServices stubCommande = (ICommandeServices) UnicastRemoteObject.exportObject(commandeService, 0);

            // Création d'un registre RMI local pour le magasin (port 1100)
            Registry registry = LocateRegistry.createRegistry(1100);

            // Enregistrement des stubs dans le registre
            registry.bind("ArticleServices", stubArticle);
            registry.bind("CommandeServices", stubCommande);

            System.out.println("Serveur Magasin Paris démarré sur le port 1100 !");
        } catch (Exception e) {
            // En cas d'erreur, affichage de la pile d'exception
            e.printStackTrace();
        }
    }
}
