package Serveur;

import java.rmi.RemoteException;
import java.sql.*;
import java.sql.Date;

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
    public boolean addCommande(int id, Date date, float total, int quantite, String status) throws RemoteException {
        try{
            PreparedStatement pr = connection.prepareStatement("INSERT into commande values (?,?,?,?,?)");
            pr.setInt(1, id);
            pr.setDate(2,date);
            pr.setFloat(3,total);
            pr.setInt(4,quantite);
            pr.setString(5,status);
            return pr.executeUpdate()>0;


        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
