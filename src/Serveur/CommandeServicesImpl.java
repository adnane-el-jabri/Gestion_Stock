package Serveur;

import java.rmi.RemoteException;
import java.sql.*;
import java.sql.Date;
import java.util.Scanner;

public class CommandeServicesImpl implements ICommandeServices{
    private Connection connection;

    public CommandeServicesImpl() throws RemoteException {
        super();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestionstock", "root", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean addArticleCommande(String nom, int id_commande) throws RemoteException {
        try{


        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
