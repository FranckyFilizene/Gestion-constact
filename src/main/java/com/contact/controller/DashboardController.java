package com.contact.controller;

import com.contact.model.Role;
import com.contact.model.Utilisateur;
import com.contact.security.Session;
import com.contact.service.AuthService;
import com.contact.utils.FxUtil;
import com.contact.utils.ManagedController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class DashboardController {

    @FXML private BorderPane rootPane;
    @FXML private VBox contentArea;

    @FXML private Label lblNomUtilisateur;
    @FXML private Label lblRoleUtilisateur;
    @FXML private Label txtAdmin;
    @FXML private Label lblSectionSecurite;

    @FXML private Button btnAdminUtilisateurs;
    @FXML private Button btnAdminJournal;
    @FXML private Button btnPersonnesSuspectes;
    @FXML private Button btnCloseWindow;

    private final AuthService authService = new AuthService();

    private Utilisateur utilisateurConnecte;
    private Button activeButton;

    // ==========================================
    // INITIALISATION
    // ==========================================

    @FXML
    public void initialize() {

        utilisateurConnecte =
                Session.getInstance().getUtilisateurConnecte();

        if (utilisateurConnecte == null) {
            deconnecter();
            return;
        }

        afficherProfilUtilisateur();
        configurerInterfaceSelonRole();

        // Page d'accueil par défaut
        chargerTousContacts();
    }

    // ==========================================
    // FERMETURE DE L'APPLICATION (BOUTON X)
    // ==========================================

    @FXML
    private void fermerApplication() {
        if (btnCloseWindow != null) {
            Stage stage = (Stage) btnCloseWindow.getScene().getWindow();
            stage.close();
        }
    }

    // ==========================================
    // PROFIL UTILISATEUR
    // ==========================================

    private void afficherProfilUtilisateur() {

        if (lblNomUtilisateur != null) {
            lblNomUtilisateur.setText(
                    utilisateurConnecte.getNomComplet()
            );
        }

        if (lblRoleUtilisateur != null) {
            lblRoleUtilisateur.setText(
                    utilisateurConnecte.getRole().name()
            );
        }
    }

    // ==========================================
    // GESTION DES AUTORISATIONS
    // ==========================================

    private boolean estAdministrateur() {

        return utilisateurConnecte != null
                && utilisateurConnecte.getRole() == Role.ADMIN;
    }

    private void configurerInterfaceSelonRole() {

        if (estAdministrateur()) {
            return;
        }

        masquerElement(btnAdminUtilisateurs);
        masquerElement(btnAdminJournal);
        masquerElement(txtAdmin);
        masquerElement(btnPersonnesSuspectes);
        masquerElement(lblSectionSecurite);
    }

    private void masquerElement(javafx.scene.Node element) {

        if (element != null) {
            element.setVisible(false);
            element.setManaged(false);
        }
    }

    // ==========================================
    // CHARGEMENT DYNAMIQUE DES PAGES
    // ==========================================

    public Object chargerPages(String chemin, String nomModule) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(chemin)
            );

            Parent page = loader.load();

            Object controller = loader.getController();

            if (utilisateurConnecte != null
                    && controller instanceof ManagedController managedController) {

                managedController.initialiserDroits(
                        utilisateurConnecte
                );
            }

            contentArea.getChildren().setAll(page);

            return controller;

        } catch (IOException | RuntimeException e) {

            FxUtil.showError(
                    "Erreur de chargement",
                    "Impossible de charger le module "
                            + nomModule + "\n" + e.getMessage()
            );

            e.printStackTrace();

            return null;
        }
    }

    // ==========================================
    // NAVIGATION ACTIVE
    // ==========================================

    private void setActive(Button clicked) {

        if (clicked == null) {
            return;
        }

        String activeClass = "menu-button-active";

        if (activeButton != null) {
            activeButton.getStyleClass().remove(activeClass);
        }

        if (!clicked.getStyleClass().contains(activeClass)) {
            clicked.getStyleClass().add(activeClass);
        }

        activeButton = clicked;
    }

    // ==========================================
    // NAVIGATION : CONTACTS
    // ==========================================

    @FXML
    private void chargerTousContacts() {

        chargerPages(
                "/view/contacts_view.fxml",
                "Tous les contacts"
        );
    }

    @FXML
    private void chargerAdministration() {

        ContactListController controller =
                (ContactListController) chargerPages(
                        "/view/contacts_view.fxml",
                        "Administration"
                );

        if (controller != null) {
            controller.selectionnerFiltreMenu("Administration");
        }
    }

    @FXML
    private void chargerKMD() {

        ContactListController controller =
                (ContactListController) chargerPages(
                        "/view/contacts_view.fxml",
                        "KMD (Komity Dina)"
                );

        if (controller != null) {
            controller.selectionnerFiltreMenu("KMD (Komity Dina)");
        }
    }

    @FXML
    private void chargerMaires() {

        ContactListController controller =
                (ContactListController) chargerPages(
                        "/view/contacts_view.fxml",
                        "Maires"
                );

        if (controller != null) {
            controller.selectionnerFiltreMenu("Maires");
        }
    }

    @FXML
    private void chargerForcesOrdre() {

        ContactListController controller =
                (ContactListController) chargerPages(
                        "/view/contacts_view.fxml",
                        "Gendarmerie"
                );

        if (controller != null) {
            controller.selectionnerFiltreMenu("Gendarmerie");
        }
    }

    @FXML
    private void chargerAnnexe() {
        setActive((Button) contentArea.getScene().lookup(".menu-button"));
        chargerPages(
                "/view/annexes_view.fxml",
                "Annexes & Régions Limitrophes"
        );
    }

    // ==========================================
    // NAVIGATION : SÉCURITÉ
    // ==========================================

    @FXML
    private void chargerPersonnesSuspectes() {

        if (!verifierAdministrateur()) {
            return;
        }

        ContactListController controller =
                (ContactListController) chargerPages(
                        "/view/contacts_view.fxml",
                        "Personnes suspectes"
                );

        if (controller != null) {
            controller.selectionnerFiltreMenu("Personnes suspectes");
        }
    }

    @FXML
    private void chargerControleEffectifs() {

        chargerPages(
                "/view/controle_effectifs_view.fxml",
                "Contrôle des effectifs"
        );
    }

    // ==========================================
    // NAVIGATION : ADMINISTRATION
    // ==========================================

    private boolean verifierAdministrateur() {

        if (!estAdministrateur()) {

            FxUtil.showError(
                    "Accès refusé",
                    "Cette section est réservée aux administrateurs."
            );

            return false;
        }

        return true;
    }

    @FXML
    private void chargerUtilisateurs() {

        if (!verifierAdministrateur()) {
            return;
        }

        setActive(btnAdminUtilisateurs);
        chargerPages(
                "/view/utilisateurs_view.fxml",
                "Gestion des utilisateurs"
        );
    }

    @FXML
    private void chargerJournal() {

        if (!verifierAdministrateur()) {
            return;
        }

        setActive(btnAdminJournal);
        chargerPages(
                "/view/journal.fxml",
                "Journal d'audit"
        );
    }

    // ==========================================
    // FENÊTRE À PROPOS
    // ==========================================

    @FXML
    private void ouvrirAPropos() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(
                            getClass().getResource("/view/apropos.fxml")
                    )
            );

            Parent root = loader.load();

            Stage stage = new Stage();

            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setTitle("À propos - Système de Sécurité Dina");
            stage.setScene(new Scene(root));

            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException | NullPointerException e) {

            FxUtil.showError(
                    "Erreur",
                    "Impossible d'ouvrir la page À propos : "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // ==========================================
    // DÉCONNEXION
    // ==========================================

    private void chargerIconeApplication(Stage stage) {
        try (InputStream iconStream = getClass().getResourceAsStream("/images/logo.png")) {
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
                return;
            }
        } catch (Exception e) {
            System.err.println("Impossible de charger /images/logo.png : " + e.getMessage());
        }

        try (InputStream icoStream = getClass().getResourceAsStream("/images/logo.ico")) {
            if (icoStream != null) {
                stage.getIcons().add(new Image(icoStream));
            }
        } catch (Exception e) {
            System.err.println("Impossible de charger /images/logo.ico : " + e.getMessage());
        }
    }

    @FXML
    private void deconnecter() {

        authService.deconnecter();

        try {

            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(
                            getClass().getResource("/view/login.fxml")
                    )
            );

            Parent root = loader.load();

            Stage dashboardStage = (Stage) rootPane.getScene().getWindow();
            dashboardStage.close();

            Stage loginStage = new Stage();
            loginStage.initStyle(StageStyle.TRANSPARENT);

            chargerIconeApplication(loginStage);

            Scene scene = new Scene(root, 900, 540);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            loginStage.setTitle("Gestion des Contacts - Atsimo-Andrefana");
            loginStage.setScene(scene);
            loginStage.setResizable(false);
            loginStage.centerOnScreen();
            loginStage.show();

        } catch (IOException | NullPointerException e) {

            FxUtil.showError(
                    "Erreur",
                    "Impossible de retourner à l'écran de connexion : "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}