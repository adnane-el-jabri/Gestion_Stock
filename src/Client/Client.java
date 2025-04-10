package Client;

import Model.Article;
import Serveur.IArticleServices;
import Serveur.ICommandeServices;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            // Connexion au registre RMI
            Registry reg = LocateRegistry.getRegistry("localhost", 1099);

            // Récupération des stubs distants
            IArticleServices articleStub = (IArticleServices) reg.lookup("ArticleServices");
            ICommandeServices commandeStub = (ICommandeServices) reg.lookup("CommandeServices");

            Scanner sc = new Scanner(System.in);

            // === 1. Rechercher un article ===
            System.out.print("Entrez la référence de l'article à rechercher : ");
            int refRecherche = sc.nextInt();
            sc.nextLine(); // vider le buffer

            Article article = articleStub.getArticleByRef(refRecherche);
            if (article != null) {
                System.out.println("Article trouvé !");
                System.out.println("Référence : " + article.getReference());
                System.out.println("Nom       : " + article.getNom());
                System.out.println("Quantité  : " + article.getStock());
                System.out.println("Prix      : " + article.getPrix());
                System.out.println("Famille   : " + article.getFamille().getNom());
            } else {
                System.out.println("Aucun article trouvé avec la référence " + refRecherche);
            }

            // === 2. Mise à jour de quantité ===
            System.out.print("Voulez-vous modifier la quantité ? (y/n) : ");
            String reponse = sc.nextLine();
            if (reponse.equalsIgnoreCase("y")) {
                System.out.print("Nouvelle quantité pour l'article " + refRecherche + " : ");
                int nouvelleQuantite = sc.nextInt();
                sc.nextLine(); // vider le buffer
                boolean updated = articleStub.updateQuantity(refRecherche, nouvelleQuantite);
                System.out.println(updated ? "Quantité mise à jour !" : "Échec de la mise à jour.");
            }

            // === 3. Ajouter un article à une commande ===
            System.out.print("Voulez-vous ajouter un article à une commande ? (y/n) : ");
            String ajout = sc.nextLine();
            int idCommande = 0;
            if (ajout.equalsIgnoreCase("y")) {
                System.out.print("Nom de l'article à commander : ");
                String nomArticle = sc.nextLine();

                System.out.print("ID de la commande : ");
            idCommande = sc.nextInt();
                sc.nextLine(); // vider le buffer

                System.out.print("Quantité à commander : ");
                int quantite = sc.nextInt();
                sc.nextLine(); // vider le buffer

                boolean success = commandeStub.addArticleCommande(nomArticle, idCommande, quantite);
                System.out.println(success ? "Article ajouté à la commande !" : "Échec de l'ajout à la commande.");
            }


            System.out.println("Passez au paiement (Y/N) :  ");
            String reponsePaiement = sc.nextLine();
            if (reponsePaiement.equalsIgnoreCase("y")) {
                System.out.println("Vous reglez comment (c : CB, e :espece )");
                String comment = sc.nextLine();
                String mode ="";
                if (comment.equalsIgnoreCase("c")) {
                    mode = "CB";
                }else if (comment.equalsIgnoreCase("e")) {
                    mode = "Espece";
                }
                if(commandeStub.payerCommande(idCommande,mode)){
                    System.out.println("Paiement réussis");
                }

            }
            // === 4. Générer une facture ===
            System.out.print("Voulez-vous générer une facture pour une commande ? (y/n) : ");
            String reponseFacture = sc.nextLine();
            if (reponseFacture.equalsIgnoreCase("y")) {
                System.out.print("ID de la commande à facturer : ");
                int idFacture = sc.nextInt();
                sc.nextLine(); // vider le buffer
                commandeStub.genererFacture(idFacture);
            }


        } catch (Exception e) {
            System.err.println("Erreur côté client : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
