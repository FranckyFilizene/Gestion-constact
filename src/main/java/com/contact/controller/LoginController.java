package com.contact.controller;

import  com.contact.exception.ServiceException;
import com.contact.model.Role;
import com.contact.model.Utilisateur;
import com.contact.security.Session;
import com.contact.service.AuthService;
import com.contact.service.UtilisateurService;
import com.contact.utils.Validator;
import com.contact.utils.FxUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LoginController {

    @FXML private Label lblTitre;
    @FXML private Label lblSousTitre;
    @FXML private VBox boxNomComplet;
    @FXML private TextField txtNomComplet;
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private CheckBox chkAfficherPassword;
    @FXML private VBox boxConfirmPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblMessage;
    @FXML private Button btnAction;
    @FXML private Label lblSwitchText;
    @FXML private Hyperlink linkSwitch;

    private final AuthService authService;
    private final UtilisateurService utilisateurService;
    private boolean isModeInscription = false;

    public LoginController(){
        this.authService = new AuthService();
        this.utilisateurService = new UtilisateurService();
    }

    @FXML
    private void initalize(){
        lblMessage.setText("");
    }

    /*
    * Affiche ou masque le mot de passe en claire
    * */
    @FXML
    private void tooglePasswordVisibility() {
        boolean afficher = chkAfficherPassword.isSelected();

        if (afficher) {
            // On copie le texte du PasswordField vers le TextField visible
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);

            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
        } else {
            // On copie le texte du TextField visible vers le PasswordField
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);

            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
        }
    }

    /*
    * Bascule entre le mode Connexion et le Mde INscription
    * */
    @FXML
    private void switchMode() {
        isModeInscription = !isModeInscription;
        lblMessage.setText("");

        // Affichage dynamique des champs d'inscription.
        boxNomComplet.setVisible(isModeInscription);
        boxNomComplet.setManaged(isModeInscription);

        boxConfirmPassword.setVisible(isModeInscription);
        boxConfirmPassword.setManaged(isModeInscription);

        if (isModeInscription) {
            lblTitre.setText("Créer un Compte");
            lblSousTitre.setText("Inscrivez-vous en tant que lecteur.");
            btnAction.setText("S'inscrire"); // <- Assure-toi aussi de mettre "S'inscrire" ici !
            lblSwitchText.setText("Vous avez déjà un compte ?");
            linkSwitch.setText("Se connecter");
        } else {
            lblTitre.setText("Bienvenue !");
            lblSousTitre.setText("Connectez-vous à votre espace de gestion."); // (ou lblSousTitre selon ton nom de variable)
            btnAction.setText("Se connecter");
            lblSwitchText.setText("Vous n'avez pas de compte ?");
            linkSwitch.setText("Créer un compte");
        }
    }

    /*
     * Gére le clic sur le bouton Principal (Connexion ou inscription)
     * */
    @FXML
    private void handleAction(){
        lblMessage.setText("");

        if(isModeInscription){
            traiterInscription();
        }else{
            traiterConnexion();
        }
    }

    private String getPasswordSaisi() {
        if (chkAfficherPassword.isSelected()) {
            return txtPasswordVisible.getText();
        } else {
            return txtPassword.getText();
        }
    }

    private void traiterConnexion() {
        String login = txtLogin.getText() != null ? txtLogin.getText().trim() : "";
        String password = getPasswordSaisi();

        if (login.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Veuillez saisir votre identifiant et votre mot de passe.");
            return;
        }

        try {
            boolean connecte = authService.connecter(login, password);
            if (connecte) {
                Utilisateur user = Session.getInstance().getUtilisateurConnecte();
                ouvrirTableauDeBord(user);
            }
        } catch (ServiceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    private void traiterInscription() {
        String login = txtLogin.getText() != null ? txtLogin.getText().trim() : "";
        String nomComplet = txtNomComplet.getText() != null ? txtNomComplet.getText().trim() : "";
        String password = getPasswordSaisi();
        String confirmPassword = txtConfirmPassword.getText() != null ? txtConfirmPassword.getText() : "";

        if (Validator.isNullOrEmpty(nomComplet) || Validator.isNullOrEmpty(login)
                || Validator.isNullOrEmpty(password) || Validator.isNullOrEmpty(confirmPassword)) {
            lblMessage.setText("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            lblMessage.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            // Appelle la méthode d'inscription publique (Rôle LECTEUR par défaut)
            boolean inscrit = utilisateurService.inscrireLecteur(
                    login,
                    nomComplet,
                    password
            );

            if (inscrit) {
                FxUtil.showSuccess("Inscription réussie", "Votre compte lecteur a été créé. Vous pouvez maintenant vous connecter.");
                txtPassword.clear();
                txtPasswordVisible.clear();
                txtConfirmPassword.clear();
                switchMode(); // Retour automatique vers l'écran de connexion
            }
        } catch (ServiceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    /**
     * Charge la scène principale et adapte l'affichage selon le rôle (ADMIN ou LECTEUR).
     */
    private void ouvrirTableauDeBord(Utilisateur user){
        try{
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/view/dashboard.fxml")));
            Parent root = loader.load();

            // Si DashboardController possède une méthode d'initialisation des droits :
            // DashboardController controller = loader.getController();
            // controller.configurerInterfaceSelonRole(user);

            Stage stage = (Stage) btnAction.getScene().getWindow();
            Scene scene = new Scene(root, 1250, 650);
            stage.setTitle("GCA Atsimo-Andrefana - Espace " + user.getRole().name() + " (" + user.getNomComplet() + ")");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();

        } catch (IOException e) {
            FxUtil.showError("Erreur de navigation", "Impossible de charger le tableau de bord : " + e.getMessage());
            e.printStackTrace();
        }
    }

}