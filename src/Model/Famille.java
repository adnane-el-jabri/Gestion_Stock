package Model;

import java.io.Serializable;

public class Famille implements Serializable {
    private int id;
    private String nom;

    public Famille(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {}
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
