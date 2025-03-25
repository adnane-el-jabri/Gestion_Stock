package Serveur;

import Model.Article;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IArticleServices extends Remote {
    List<Article> getArticles() throws RemoteException;
    boolean addArticle(Article article) throws RemoteException;
    Article getArticleByRef(int ref) throws RemoteException;

}
