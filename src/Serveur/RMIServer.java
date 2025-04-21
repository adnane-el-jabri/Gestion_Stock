package Serveur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer {
    public static void main(String[] args) {
        try {
            // Création des instances des services pour les articles et les commandes
            ArticleServicesImpl service = new ArticleServicesImpl();
            CommandeServicesImpl service1 = new CommandeServicesImpl();

            // Exportation de l'objet distant ArticleServices
            IArticleServices stub = (IArticleServices) UnicastRemoteObject.exportObject(service, 0);

            // Connexion au registre RMI existant (par défaut sur le port 1099)
            Registry registry = LocateRegistry.getRegistry();

            // Enregistrement du service ArticleServices dans le registre
            registry.bind("ArticleServices", stub);

            // Exportation de l'objet distant CommandeServices
            ICommandeServices stub1 = (ICommandeServices) UnicastRemoteObject.exportObject(service1, 0);

            // Enregistrement du service CommandeServices dans le registre
            registry.bind("CommandeServices", stub1);

            // Message de confirmation
            System.out.println("Server ready");

        } catch (Exception e) {
            // Affichage de l'exception en cas d'erreur
            e.printStackTrace();
        }
    }
}
