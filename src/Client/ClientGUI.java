package Client;

import Serveur.IArticleServices;
import Serveur.ICommandeServices;
import Model.Article;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class ClientGUI extends JFrame {

    private IArticleServices articleStub;
    private ICommandeServices commandeStub;
    private String magasin;

    private JTextField refField, nomArticleField, idCommandeField, quantiteField;
    private JTextArea resultArea;
    private JTable panierTable;
    private DefaultTableModel panierModel;

    private List<Article> articlesSelectionnes = new ArrayList<>();
    private List<Integer> quantitesSelectionnees = new ArrayList<>();

    private int idCommandeCourante;
    private float totalCommande = 0f;

    public ClientGUI(String magasin) {
        this.magasin = magasin;

        setTitle("Espace Client - " + magasin);
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JLabel titreLabel = new JLabel("Espace Client - Magasin : " + magasin, JLabel.CENTER);
        titreLabel.setFont(new Font("Verdana", Font.BOLD, 26));
        titreLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        add(titreLabel, BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        refField = createLabeledField(panel, "Référence de l'article :");
        nomArticleField = createLabeledField(panel, "Nom de l'article :");
        idCommandeField = createLabeledField(panel, "ID de la commande :");
        quantiteField = createLabeledField(panel, "Quantité à commander :");

        addButton("Chercher l'article", panel, e -> chercherArticle());
        addButton("Ajouter au panier", panel, e -> ajouterAuPanier());
        addButton("Payer la commande", panel, e -> payerCommande());
        addButton("Générer Facture", panel, e -> genererFacture());

        add(new JScrollPane(panel), BorderLayout.WEST);

        String[] colonnes = {"Référence", "Nom", "Quantité", "Prix U.", "Total Ligne"};
        panierModel = new DefaultTableModel(colonnes, 0);
        panierTable = new JTable(panierModel);
        panierTable.setRowHeight(30);
        panierTable.setFont(new Font("Arial", Font.PLAIN, 14));
        panierTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));

        add(new JScrollPane(panierTable), BorderLayout.CENTER);

        resultArea = new JTextArea(5, 20);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Arial", Font.PLAIN, 14));
        resultArea.setBorder(BorderFactory.createTitledBorder("Résultat"));
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        try {
            Registry reg = LocateRegistry.getRegistry("localhost", 1099);
            articleStub = (IArticleServices) reg.lookup("ArticleServices");
            commandeStub = (ICommandeServices) reg.lookup("CommandeServices");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion au serveur RMI !");
            e.printStackTrace();
        }
    }

    private JTextField createLabeledField(JPanel panel, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label);
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(field);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        return field;
    }

    private void addButton(String text, JPanel panel, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0, 123, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.addActionListener(listener);
        panel.add(button);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void chercherArticle() {
        try {
            int ref = Integer.parseInt(refField.getText());
            Article article = articleStub.getArticleByRef(ref);
            if (article != null) {
                resultArea.setText("Article trouvé :\n" +
                        "Réf : " + article.getReference() +
                        " | Nom : " + article.getNom() +
                        " | Quantité : " + article.getStock() +
                        " | Prix : " + article.getPrix() +
                        " | Famille : " + article.getFamille().getNom());
            } else {
                resultArea.setText("Aucun article trouvé avec cette référence !");
            }
        } catch (Exception ex) {
            resultArea.setText("Erreur de recherche !");
            ex.printStackTrace();
        }
    }

    private void ajouterAuPanier() {
        try {
            String nom = nomArticleField.getText();
            idCommandeCourante = Integer.parseInt(idCommandeField.getText());
            int quantite = Integer.parseInt(quantiteField.getText());

            Article article = null;
            for (Article a : articleStub.getArticles()) {
                if (a.getNom().equalsIgnoreCase(nom)) {
                    article = a;
                    break;
                }
            }

            if (article == null) {
                resultArea.setText("Article non trouvé !");
                return;
            }

            articlesSelectionnes.add(article);
            quantitesSelectionnees.add(quantite);

            panierModel.addRow(new Object[]{
                    article.getReference(),
                    article.getNom(),
                    quantite,
                    article.getPrix(),
                    quantite * article.getPrix()
            });

            totalCommande += quantite * article.getPrix();
            resultArea.setText("Article ajouté au panier. Total actuel : " + totalCommande + " €");

        } catch (Exception ex) {
            resultArea.setText("Erreur d'ajout au panier !");
            ex.printStackTrace();
        }
    }

    private void payerCommande() {
        if (articlesSelectionnes.isEmpty()) {
            resultArea.setText("Aucun article sélectionné !");
            return;
        }

        String modePaiement = JOptionPane.showInputDialog(this, "Entrez le mode de paiement (Carte, Espèces, PayPal...)", "Paiement", JOptionPane.PLAIN_MESSAGE);

        if (modePaiement == null || modePaiement.trim().isEmpty()) {
            resultArea.setText("Paiement annulé : mode de paiement non renseigné !");
            return;
        }

        try {
            boolean commandeExiste = commandeStub.commandeExiste(idCommandeCourante);

            if (!commandeExiste) {
                commandeStub.addArticleCommande(articlesSelectionnes.get(0).getNom(), idCommandeCourante, 0);
            }

            for (int i = 0; i < articlesSelectionnes.size(); i++) {
                Article art = articlesSelectionnes.get(i);
                int qte = quantitesSelectionnees.get(i);
                commandeStub.addArticleCommande(art.getNom(), idCommandeCourante, qte);
            }

            boolean success = commandeStub.payerCommande(idCommandeCourante, modePaiement);

            if (success) {
                resultArea.setText("Paiement effectué avec succès pour la commande ID : " + idCommandeCourante +
                        "\nMode de Paiement : " + modePaiement);
                panierModel.setRowCount(0);
                articlesSelectionnes.clear();
                quantitesSelectionnees.clear();
                totalCommande = 0f;
            } else {
                resultArea.setText("Paiement échoué : commande introuvable !");
            }

        } catch (Exception e) {
            resultArea.setText("Erreur lors du paiement !");
            e.printStackTrace();
        }
    }

    private void genererFacture() {
        try {
            commandeStub.genererFacture(idCommandeCourante, magasin);
            resultArea.setText("Facture générée : facture_commande_" + idCommandeCourante + ".txt");
        } catch (Exception e) {
            resultArea.setText("Erreur lors de la génération de la facture !");
            e.printStackTrace();
        }
    }
}