package Serveur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServerMagasinG20 {
    public static void main(String[] args) {
        try {
            // Connexion au registre du serveur central sur le port 2099
            Registry registryCentral = LocateRegistry.getRegistry("localhost", 2099);

            // Récupération des services centraux depuis le registre
            IArticleServices centralArticleService = (IArticleServices) registryCentral.lookup("ArticleServices");
            ICommandeServices centralCommandeService = (ICommandeServices) registryCentral.lookup("CommandeServices");

            // Création des délégués qui transmettent les appels au serveur central
            ArticleServicesDelegue articleService = new ArticleServicesDelegue(centralArticleService);
            CommandeServicesDelegue commandeService = new CommandeServicesDelegue(centralCommandeService);

            // Exportation des objets distants (génération des stubs RMI)
            IArticleServices stubArticle = (IArticleServices) UnicastRemoteObject.exportObject(articleService, 0);
            ICommandeServices stubCommande = (ICommandeServices) UnicastRemoteObject.exportObject(commandeService, 0);

            // Création du registre RMI local pour le magasin G20 sur le port 1102
            Registry registry = LocateRegistry.createRegistry(1102);

            // Enregistrement des stubs dans le registre RMI local du magasin
            registry.bind("ArticleServices", stubArticle);
            registry.bind("CommandeServices", stubCommande);

            System.out.println("Serveur Magasin G20 démarré sur le port 1102 !");
        } catch (Exception e) {
            // En cas d’erreur, affichage de la pile d’exécution
            e.printStackTrace();
        }
    }
}
