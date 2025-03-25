package Serveur;

import Model.Commande;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Date;

public interface ICommandeServices extends Remote {
    public boolean addCommande(int id,Date date, float total, int quantite, String status) throws RemoteException;

}
