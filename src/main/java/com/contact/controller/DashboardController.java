package com.contact.controller;

import com.contact.model.Role;
import com.contact.model.Utilisateur;
import com.contact.security.Session;
import com.contact.utils.FxUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class DashboardController {

    @FXML private BorderPane rootPane;
    @FXML private VBox contentArea;
    @FXML private Label lblNomUtilisateur;
    @FXML private Label lblRoleUtilisateur;
    @FXML private Label txtAdmin;

    // Boutons spécifiques admin / sécurité à filtrer selon le rôle si besoin
    @FXML private Button btnAdminUtilisateurs;
    @FXML private Button btnAdminJournal;
    @FXML private Button btnPersonnesSuspectes;
    @FXML private Label lblSectionSecurite;

    private Utilisateur utilisateurConnecte;

    @FXML
    public void initialize() {
        // 1. Récupérer l'utilisateur connecté depuis la session sécurisée
        utilisateurConnecte = Session.getInstance().getUtilisateurConnecte();

        if (utilisateurConnecte != null) {
            // Affichage des informations dans l'en-tête
            lblNomUtilisateur.setText(utilisateurConnecte.getNomComplet());
            lblRoleUtilisateur.setText(utilisateurConnecte.getRole().name());

            // 2. Adaptation de l'interface selon le rôle (ADMIN vs LECTEUR)
            configurerInterfaceSelonRole();

            //Charger la vue des contact par defaut au lancement
            chargerTousContacts();
        } else {
            // Sécurité : si aucun utilisateur en session, on force le retour au login
            deconnecter();
        }
    }

    /**
     * Adapte l'affichage et les accès selon le rôle de l'utilisateur.
     */
    private void configurerInterfaceSelonRole() {
        if (utilisateurConnecte.getRole() == Role.LECTEUR) {
            // Le LECTEUR n'a pas accès aux menus d'administration système
            if (btnAdminUtilisateurs != null) {
                btnAdminUtilisateurs.setVisible(false);
                btnAdminUtilisateurs.setManaged(false);
            }
            if (btnAdminJournal != null) {
                btnAdminJournal.setVisible(false);
                btnAdminJournal.setManaged(false);
            }
            if(txtAdmin != null){
                txtAdmin.setVisible(false);
                txtAdmin.setManaged(false);
            }

            // Note : Si les lecteurs ne doivent pas non plus voir la section "Sécurité & Doutes",
            // tu peux aussi masquer le bouton et le label de section correspondants :

            if (btnPersonnesSuspectes != null) {
                btnPersonnesSuspectes.setVisible(false);
                btnPersonnesSuspectes.setManaged(false);
            }
            if (lblSectionSecurite != null) {
                lblSectionSecurite.setVisible(false);
                lblSectionSecurite.setManaged(false);
            }

        }
        // Si c'est un ADMIN, tout reste visible et accessible.
    }


    /**
     * Charge dynamiquement une vue FXML au centre du BorderPane.
     */
    public void chargerPages(String chemin, String nomModule){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource(chemin));
            Parent page = loader.load();

            // -> C'EST CETTE PARTIE QU'IL FAUT AJOUTER/VÉRIFIER :
            // Récupérer le contrôleur de la sous-vue et lui injecter l'utilisateur connecté
            Object controller = loader.getController();
            if (controller instanceof com.contact.utils.ManagedController) {
                ((com.contact.utils.ManagedController) controller).initialiserDroits(utilisateurConnecte);
            }

            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            FxUtil.showError("Erreur de Chargement", "Impossible de charger le module " + nomModule + "\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================
    // MÉTHODES DE NAVIGATION DANS LA ZONE CENTRALE
    // ==========================================

    @FXML
    private void chargerTousContacts() {
        chargerPages("/view/contacts_view.fxml", "Tous Contacts");
        // TODO: Charger la vue des contacts dans contentArea
    }

    @FXML
    private void chargerAdministration() {
        FxUtil.showSuccess("Navigation", "Chargement des contacts administratifs...");
    }

    @FXML
    private void chargerKMD() {
        FxUtil.showSuccess("Navigation", "Chargement des membres du comité KMD...");
    }

    @FXML
    private void chargerMaires() {
        FxUtil.showSuccess("Navigation", "Chargement des maires des 9 districts...");
    }

    @FXML
    private void chargerForcesOrdre() {
        FxUtil.showSuccess("Navigation", "Chargement des forces de l'ordre...");
    }

    @FXML
    private void chargerAnnexe() {
        FxUtil.showSuccess("Navigation", "Chargement des contacts annexes et limitrophes...");
    }

    @FXML
    private void chargerPersonnesSuspectes() {
        FxUtil.showSuccess("Sécurité", "Consultation de la liste des personnes suspectes.");
    }

    @FXML
    private void chargerControleEffectifs() {
        FxUtil.showSuccess("Pilotage", "Ouverture du contrôle des effectifs...");
    }

    @FXML
    private void chargerArborescence() {
        FxUtil.showSuccess("Pilotage", "Affichage de l'arborescence territoriale des 9 districts...");
    }

    @FXML
    private void chargerUtilisateurs() {
        if (utilisateurConnecte.getRole() != Role.ADMIN) {
            FxUtil.showError("Accès refusé", "Cette section est réservée aux administrateurs.");
            return;
        }
        FxUtil.showSuccess("Navigation", "Module de gestion des utilisateurs...");
    }

    @FXML
    private void chargerJournal() {
        if (utilisateurConnecte.getRole() != Role.ADMIN) {
            FxUtil.showError("Accès refusé", "Cette section est réservée aux administrateurs.");
            return;
        }
        FxUtil.showSuccess("Navigation", "Module du journal d'audit...");
    }

    /**
     * Gère la déconnexion et retourne à l'écran de login.
     */
    @FXML
    private void deconnecter() {
        Session.getInstance().fermerSession();

        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/view/login.fxml")));
            Parent root = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root, 950, 600);
            stage.setTitle("Gestion des Contacts - Atsimo Andrefana (Connexion)");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();

        } catch (IOException e) {
            FxUtil.showError("Erreur", "Impossible de retourner à l'écran de connexion : " + e.getMessage());
            e.printStackTrace();
        }
    }
}