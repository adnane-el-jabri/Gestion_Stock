package Serveur;

import Model.Commande;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;

public interface ICommandeServices extends Remote {
    public boolean addArticleCommande(String nomArticle, int id_commande, int quantiteCommande) throws RemoteException;
    public void genererFacture(int id_commande, String magasin) throws RemoteException;
    public boolean payerCommande(int id_commande,String modepaiement) throws RemoteException;
    float chiffreAffaireParDate(Date date) throws RemoteException;
    boolean commandeExiste(int id_commande) throws RemoteException;


}
