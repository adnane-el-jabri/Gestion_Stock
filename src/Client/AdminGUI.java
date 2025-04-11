package Client;

import Serveur.IArticleServices;
import Serveur.ICommandeServices;
import Model.Article;
import Model.Famille;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class AdminGUI extends JFrame {

    private IArticleServices articleStub;
    private ICommandeServices commandeStub;
    private String magasin;

    private JTextField refField, refAjoutField, nomAjoutField, stockAjoutField, prixAjoutField, idFamilleAjoutField, familleField, dateCAField;
    private JTextArea resultArea;

    public AdminGUI(String magasin) {
        this.magasin = magasin;

        setTitle("Espace Admin - " + magasin);
        setSize(1300, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));

        JLabel titreLabel = new JLabel("Espace Admin - Magasin : " + magasin, JLabel.CENTER);
        titreLabel.setFont(new Font("Verdana", Font.BOLD, 26));
        titreLabel.setForeground(new Color(0, 102, 204));
        titreLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        add(titreLabel, BorderLayout.NORTH);

        // Grand panel horizontal
        JPanel sectionsPanel = new JPanel();
        sectionsPanel.setLayout(new BoxLayout(sectionsPanel, BoxLayout.X_AXIS));
        sectionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sectionsPanel.setBackground(new Color(245, 245, 245));

        // Section 1 : Recherche article
        JPanel section1 = createSection();
        refField = createFieldWithLabel(section1, "Référence de l'article :");
        addButton("Chercher l'article", section1, e -> chercherArticle());
        addButton("Consulter Stock", section1, e -> consulterStock());
        addButton("Mettre à jour Stock", section1, e -> majStock());

        // Section 2 : Ajouter article
        JPanel section2 = createSection();
        refAjoutField = createFieldWithLabel(section2, "Référence (Ajout) :");
        nomAjoutField = createFieldWithLabel(section2, "Nom de l'article :");
        stockAjoutField = createFieldWithLabel(section2, "Quantité en stock :");
        prixAjoutField = createFieldWithLabel(section2, "Prix unitaire :");
        idFamilleAjoutField = createFieldWithLabel(section2, "ID Famille :");
        addButton("Ajouter l'article", section2, e -> ajouterArticle());

        // Section 3 : Recherche par famille
        JPanel section3 = createSection();
        familleField = createFieldWithLabel(section3, "Nom de la famille :");
        addButton("Rechercher par Famille", section3, e -> rechercherParFamille());

        // Section 4 : Chiffre d'affaires
        JPanel section4 = createSection();
        dateCAField = createFieldWithLabel(section4, "Date (AAAA-MM-JJ) :");
        addButton("Calculer Chiffre d'Affaire", section4, e -> calculerChiffreAffaire());

        // Section 5 : Liste articles
        JPanel section5 = createSection();
        addButton("Afficher tous les articles", section5, e -> afficherListeArticles());

        // Ajouter toutes les sections + espaces entre elles
        sectionsPanel.add(section1);
        sectionsPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        sectionsPanel.add(section2);
        sectionsPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        sectionsPanel.add(section3);
        sectionsPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        sectionsPanel.add(section4);
        sectionsPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        sectionsPanel.add(section5);

        add(new JScrollPane(sectionsPanel), BorderLayout.CENTER);
        // Résultats en bas
        resultArea = new JTextArea(10, 20);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Résultats"));
        scrollPane.setPreferredSize(new Dimension(0, 250));
        add(scrollPane, BorderLayout.SOUTH);

        try {
            Registry reg = LocateRegistry.getRegistry("localhost", 1099);
            articleStub = (IArticleServices) reg.lookup("ArticleServices");
            commandeStub = (ICommandeServices) reg.lookup("CommandeServices");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion au serveur !");
            e.printStackTrace();
        }
    }

    private JPanel createSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245));
        return panel;
    }

    private JTextField createFieldWithLabel(JPanel panel, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label);
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(200, 30));
        panel.add(field);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        return field;
    }

    private void addButton(String text, JPanel panel, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0, 123, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 10));
        button.setMaximumSize(new Dimension(200, 40));
        button.addActionListener(listener);
        panel.add(button);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void chercherArticle() {
        try {
            String refText = refField.getText().trim();
            if (refText.isEmpty()) {
                resultArea.setText("Saisissez une référence d'article !");
                return;
            }
            int ref = Integer.parseInt(refText);
            Article article = articleStub.getArticleByRef(ref);
            if (article != null) {
                resultArea.setText("Article trouvé :\n" +
                        "Réf : " + article.getReference() + "\n" +
                        "Nom : " + article.getNom() + "\n" +
                        "Stock : " + article.getStock() + "\n" +
                        "Prix : " + article.getPrix() + "\n" +
                        "Famille : " + article.getFamille().getNom());
            } else {
                resultArea.setText("Aucun article trouvé.");
            }
        } catch (Exception ex) {
            resultArea.setText("Erreur lors de la recherche d'article.");
            ex.printStackTrace();
        }
    }

    private void consulterStock() {
        try {
            String refText = refField.getText().trim();
            if (refText.isEmpty()) {
                resultArea.setText("Saisissez une référence pour consulter le stock !");
                return;
            }
            int ref = Integer.parseInt(refText);
            int quantite = articleStub.getQuantity(ref);
            resultArea.setText("Stock pour Réf " + ref + " : " + quantite + " unités.");
        } catch (Exception ex) {
            resultArea.setText("Erreur lors de la consultation du stock.");
            ex.printStackTrace();
        }
    }

    private void majStock() {
        try {
            String refText = refField.getText().trim();
            if (refText.isEmpty()) {
                resultArea.setText("Saisissez une référence pour mettre à jour !");
                return;
            }
            int ref = Integer.parseInt(refText);
            String newQuantity = JOptionPane.showInputDialog(this, "Nouvelle quantité :", "Mise à jour Stock", JOptionPane.PLAIN_MESSAGE);
            if (newQuantity == null || newQuantity.trim().isEmpty()) {
                resultArea.setText("Quantité vide, mise à jour annulée !");
                return;
            }
            int quantity = Integer.parseInt(newQuantity.trim());
            boolean updated = articleStub.updateQuantity(ref, quantity);
            if (updated) {
                resultArea.setText("Stock mis à jour pour Réf " + ref + ".");
            } else {
                resultArea.setText("Impossible de mettre à jour le stock.");
            }
        } catch (Exception ex) {
            resultArea.setText("Erreur lors de la mise à jour.");
            ex.printStackTrace();
        }
    }

    private void ajouterArticle() {
        try {
            int ref = Integer.parseInt(refAjoutField.getText().trim());
            String nom = nomAjoutField.getText().trim();
            int stock = Integer.parseInt(stockAjoutField.getText().trim());
            float prix = Float.parseFloat(prixAjoutField.getText().trim());
            int idFamille = Integer.parseInt(idFamilleAjoutField.getText().trim());

            Famille famille = new Famille(idFamille, nom);
            Article article = new Article(ref, nom, stock, prix, famille);

            boolean success = articleStub.addArticle(article);
            if (success) {
                resultArea.setText("Article ajouté !");
            } else {
                resultArea.setText("Impossible d'ajouter l'article.");
            }
        } catch (Exception ex) {
            resultArea.setText("Erreur lors de l'ajout d'article.");
            ex.printStackTrace();
        }
    }

    private void rechercherParFamille() {
        try {
            String famille = familleField.getText().trim();
            if (famille.isEmpty()) {
                resultArea.setText("Entrez un nom de famille.");
                return;
            }
            List<Article> articles = articleStub.rechercherParFamille(famille);
            StringBuilder sb = new StringBuilder("Articles de la famille " + famille + " :\n");
            for (Article a : articles) {
                sb.append("Réf : ").append(a.getReference()).append(" | Nom : ").append(a.getNom()).append(" | Stock : ").append(a.getStock()).append(" | Prix : ").append(a.getPrix()).append("\n");
            }
            resultArea.setText(sb.toString());
        } catch (Exception ex) {
            resultArea.setText("Erreur lors de la recherche par famille.");
            ex.printStackTrace();
        }
    }

    private void calculerChiffreAffaire() {
        try {
            String dateStr = dateCAField.getText().trim();
            if (dateStr.isEmpty()) {
                resultArea.setText("Entrez une date valide.");
                return;
            }
            java.sql.Date date = java.sql.Date.valueOf(dateStr);
            float chiffreAffaire = commandeStub.chiffreAffaireParDate(date);
            resultArea.setText("Chiffre d'affaires pour " + date + " : " + chiffreAffaire + " €");
        } catch (Exception ex) {
            resultArea.setText("Erreur lors du calcul du chiffre d'affaires.");
            ex.printStackTrace();
        }
    }

    private void afficherListeArticles() {
        try {
            List<Article> articles = articleStub.getArticles();
            StringBuilder sb = new StringBuilder("Tous les articles :\n");
            for (Article a : articles) {
                sb.append("Réf : ").append(a.getReference()).append(" | Nom : ").append(a.getNom()).append(" | Stock : ").append(a.getStock()).append(" | Prix : ").append(a.getPrix()).append("\n");
            }
            resultArea.setText(sb.toString());
        } catch (Exception ex) {
            resultArea.setText("Erreur lors de l'affichage de la liste.");
            ex.printStackTrace();
        }
    }
}
