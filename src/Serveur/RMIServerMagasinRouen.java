package Serveur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServerMagasinRouen {
    public static void main(String[] args) {
        try {
            // Connexion au registre RMI du serveur central (port 2099)
            Registry registryCentral = LocateRegistry.getRegistry("localhost", 2099);

            // Récupération des services distants (articles et commandes) exposés par le serveur central
            IArticleServices centralArticleService = (IArticleServices) registryCentral.lookup("ArticleServices");
            ICommandeServices centralCommandeService = (ICommandeServices) registryCentral.lookup("CommandeServices");

            // Création des délégués pour faire le lien entre ce magasin et les services centraux
            ArticleServicesDelegue articleService = new ArticleServicesDelegue(centralArticleService);
            CommandeServicesDelegue commandeService = new CommandeServicesDelegue(centralCommandeService);

            // Exportation des objets distants (génération des stubs RMI)
            IArticleServices stubArticle = (IArticleServices) UnicastRemoteObject.exportObject(articleService, 0);
            ICommandeServices stubCommande = (ICommandeServices) UnicastRemoteObject.exportObject(commandeService, 0);

            // Création du registre RMI propre à ce magasin sur le port 1103
            Registry registry = LocateRegistry.createRegistry(1103);

            // Enregistrement des stubs dans le registre RMI local
            registry.bind("ArticleServices", stubArticle);
            registry.bind("CommandeServices", stubCommande);

            // Message de confirmation
            System.out.println("Serveur Magasin Rouen démarré sur le port 1103 !");
        } catch (Exception e) {
            // Gestion des erreurs éventuelles
            e.printStackTrace();
        }
    }
}
