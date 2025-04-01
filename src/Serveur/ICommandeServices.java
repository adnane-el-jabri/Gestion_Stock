package Serveur;

import Model.Commande;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Date;

public interface ICommandeServices extends Remote {
    public boolean addArticleCommande(String nom, int id_commande) throws RemoteException;

}
