package Serveur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer {
    public static void main(String[] args) {
        try {
            ArticleServicesImpl service = new ArticleServicesImpl();

            IArticleServices stub = (IArticleServices) UnicastRemoteObject.exportObject(service, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("ArticleServices", stub);
            System.out.println("Server ready");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
