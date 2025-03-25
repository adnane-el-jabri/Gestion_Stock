package Model;

import java.io.Serializable;
import  java.sql.Date;

public class Commande implements Serializable {
    private int id_commande;
    private Date date_commande;
    private float total_commande;
    private int quantite_commande;
    private  String status;

    public Commande(int id, Date date, float total, int quantite, String status) {
        this.id_commande = id;
        this.date_commande = date;
        this.total_commande = total;
        this.quantite_commande = quantite;
        this.status = status;
    }

    public void setId_commande(int id) {
        this.id_commande = id;
    }
    public void setDate_commande(Date date) {
        this.date_commande = date;
    }
    public void setTotal_commande(float total) {
        this.total_commande = total;
    }
    public void setQuantite_commande(int quantite) {

    }
    public void setStatus(String status) {
        this.status = status;
    }
    public int getId_commande() {
        return id_commande;
    }
    public Date getDate_commande() {
        return date_commande;
    }
    public float getTotal_commande() {
        return total_commande;
    }
    public int getQuantite_commande() {
        return quantite_commande;
    }
    public String getStatus() {
        return status;
    }


}
