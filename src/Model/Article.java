package Model;

import java.io.Serializable;

public class Article implements Serializable {

    private int reference ;
    private String nom ;
    private int quantiteStock;
    private float prix;
    private Famille famille ;

    public Article(){};

    public Article(int reference,String nom, int quantiteStock, float prix, Famille famille) {
        this.reference = reference;
        this.nom = nom;
        this.quantiteStock = quantiteStock;
        this.prix = prix;
        this.famille = famille;
    }
    public int getStock()  {
        return this.quantiteStock;
    }

    public int getReference()  {
        return this.reference;
    }
    public float getPrix()  {
        return this.prix;
    }
    public Famille getFamille()  {
        return this.famille;
    }

    public void setStock(int stock) {
        this.quantiteStock = stock;
    }
    public void setReference(int reference) {
        this.reference = reference;

    }
    public void setPrix(float prix) {
        this.prix = prix;
    }
    public void setFamille(Famille famille) {
        this.famille = famille;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public String toString(){
        return reference+" nom : "+ nom +" " +quantiteStock+" "+prix+" "+famille.getNom();
    }
}
