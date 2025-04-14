package Serveur;

import Model.Article;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IArticleServices extends Remote {
    List<Article> getArticles() throws RemoteException;
    boolean addArticle(Article article) throws RemoteException;
    int getQuantity(int reference) throws RemoteException;
    boolean updateQuantity(int reference, int quantity) throws RemoteException;
    public Article getArticleByRef(int ref) throws RemoteException;
    List<Article> rechercherParFamille(String famille) throws RemoteException;
    boolean updatePrix(int reference, float prix) throws RemoteException;

}
