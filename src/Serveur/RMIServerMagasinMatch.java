package Serveur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServerMagasinMatch {
    public static void main(String[] args) {
        try {
            // Connexion au registre RMI du serveur central (port 2099)
            Registry registryCentral = LocateRegistry.getRegistry("localhost", 2099);

            // Récupération des interfaces des services distants disponibles depuis le serveur central
            IArticleServices centralArticleService = (IArticleServices) registryCentral.lookup("ArticleServices");
            ICommandeServices centralCommandeService = (ICommandeServices) registryCentral.lookup("CommandeServices");

            // Création des délégués pour le magasin Match, qui délèguent les appels au serveur central
            ArticleServicesDelegue articleService = new ArticleServicesDelegue(centralArticleService);
            CommandeServicesDelegue commandeService = new CommandeServicesDelegue(centralCommandeService);

            // Exportation des objets distants pour qu'ils soient accessibles via RMI
            IArticleServices stubArticle = (IArticleServices) UnicastRemoteObject.exportObject(articleService, 0);
            ICommandeServices stubCommande = (ICommandeServices) UnicastRemoteObject.exportObject(commandeService, 0);

            // Création d’un registre RMI local pour le magasin Match, sur le port 1101
            Registry registry = LocateRegistry.createRegistry(1101);

            // Enregistrement des services distants dans le registre RMI local du magasin
            registry.bind("ArticleServices", stubArticle);
            registry.bind("CommandeServices", stubCommande);

            // Message de confirmation
            System.out.println("Serveur Magasin Match démarré sur le port 1101 !");
        } catch (Exception e) {
            // Affichage d’éventuelles erreurs
            e.printStackTrace();
        }
    }
}
