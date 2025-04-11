package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AccueilGUI extends JFrame {

    private JComboBox<String> magasinComboBox;
    private JButton clientButton;
    private JButton adminButton;

    public AccueilGUI() {
        setTitle("Accueil - Heptathlon");
        setSize(600, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Titre principal
        JLabel titreLabel = new JLabel("Bienvenue chez Heptathlon", JLabel.CENTER);
        titreLabel.setFont(new Font("Verdana", Font.BOLD, 26));
        titreLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        add(titreLabel, BorderLayout.NORTH);

        // Panel centre
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(240, 240, 240));
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel sousTitre = new JLabel("Choisissez votre magasin :", JLabel.CENTER);
        sousTitre.setAlignmentX(Component.CENTER_ALIGNMENT);
        sousTitre.setFont(new Font("Arial", Font.BOLD, 18));
        centerPanel.add(sousTitre);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        magasinComboBox = new JComboBox<>(new String[]{"G20", "Auchan", "Match", "Leclerc"});
        magasinComboBox.setMaximumSize(new Dimension(200, 30));
        magasinComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(magasinComboBox);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        clientButton = new JButton("Espace Client");
        styleButton(clientButton);
        centerPanel.add(clientButton);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        adminButton = new JButton("Espace Admin");
        styleButton(adminButton);
        centerPanel.add(adminButton);

        add(centerPanel, BorderLayout.CENTER);

        // Actions sur les boutons
        clientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String magasin = (String) magasinComboBox.getSelectedItem();
                new ClientGUI(magasin).setVisible(true);
                dispose();
            }
        });

        adminButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String magasin = (String) magasinComboBox.getSelectedItem();
                new AdminGUI(magasin).setVisible(true);
                dispose();
            }
        });
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(0, 123, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(200, 40));
        button.setMaximumSize(new Dimension(200, 40));
    }
}
