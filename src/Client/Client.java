package Client;

public class Client {
    public static void main(String[] args) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                AccueilGUI accueil = new AccueilGUI();
                accueil.setVisible(true);
            });

    }
}
