package Serveur;

import java.rmi.RemoteException;
import java.util.Date;

public class CommandeServicesDelegue implements ICommandeServices {
    private final ICommandeServices centralCommandeService;

    public CommandeServicesDelegue(ICommandeServices centralCommandeService) {
        this.centralCommandeService = centralCommandeService;
    }

    @Override
    public boolean addArticleCommande(String nomArticle, int id_commande, int quantiteCommande) throws RemoteException {
        return centralCommandeService.addArticleCommande(nomArticle, id_commande, quantiteCommande);
    }

    @Override
    public void genererFacture(int id_commande, String magasin) throws RemoteException {
        centralCommandeService.genererFacture(id_commande, magasin);
    }

    @Override
    public boolean payerCommande(int id_commande, String modepaiement) throws RemoteException {
        return centralCommandeService.payerCommande(id_commande, modepaiement);
    }

    @Override
    public float chiffreAffaireParDate(Date date) throws RemoteException {
        return centralCommandeService.chiffreAffaireParDate(date);
    }

    @Override
    public boolean commandeExiste(int id_commande) throws RemoteException {
        return centralCommandeService.commandeExiste(id_commande);
    }
}
