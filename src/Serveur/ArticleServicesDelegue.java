package Serveur;

import Model.Article;

import java.rmi.RemoteException;
import java.util.List;

public class ArticleServicesDelegue implements IArticleServices {
    private final IArticleServices centralArticleService;

    public ArticleServicesDelegue(IArticleServices centralArticleService) {
        this.centralArticleService = centralArticleService;
    }

    @Override
    public List<Article> getArticles() throws RemoteException {
        return centralArticleService.getArticles();
    }

    @Override
    public boolean addArticle(Article article) throws RemoteException {
        return centralArticleService.addArticle(article);
    }

    @Override
    public int getQuantity(int reference) throws RemoteException {
        return centralArticleService.getQuantity(reference);
    }

    @Override
    public boolean updateQuantity(int reference, int quantity) throws RemoteException {
        return centralArticleService.updateQuantity(reference, quantity);
    }

    @Override
    public Article getArticleByRef(int ref) throws RemoteException {
        return centralArticleService.getArticleByRef(ref);
    }

    @Override
    public List<Article> rechercherParFamille(String famille) throws RemoteException {
        return centralArticleService.rechercherParFamille(famille);
    }
    @Override
    public boolean updatePrix(int reference, float prix) throws RemoteException {
        return centralArticleService.updatePrix(reference, prix);
    }

}
