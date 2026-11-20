package com.contact.controller;

import com.contact.exception.ServiceException;
import com.contact.model.Utilisateur;
import com.contact.security.Session;
import com.contact.service.AuthService;
import com.contact.service.UtilisateurService;
import com.contact.utils.FxUtil;
import com.contact.utils.Validator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class LoginController {

    @FXML private StackPane rootPane; // Conteneur principal pour le déplacement de la fenêtre
    @FXML private Button btnClose;    // Bouton de fermeture personnalisé
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

    // Variables pour permettre de déplacer la fenêtre sans bordure
    private double xOffset = 0;
    private double yOffset = 0;

    public LoginController() {
        this.authService = new AuthService();
        this.utilisateurService = new UtilisateurService();
    }

    @FXML
    private void initialize() {
        lblMessage.setText("");

        // Rendre la fenêtre déplaçable avec la souris depuis le fond
        if (rootPane != null) {
            rootPane.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            rootPane.setOnMouseDragged(event -> {
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void tooglePasswordVisibility() {
        boolean afficher = chkAfficherPassword.isSelected();

        if (afficher) {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);

            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
        } else {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);

            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
        }
    }

    @FXML
    private void switchMode() {
        isModeInscription = !isModeInscription;
        lblMessage.setText("");

        boxNomComplet.setVisible(isModeInscription);
        boxNomComplet.setManaged(isModeInscription);

        boxConfirmPassword.setVisible(isModeInscription);
        boxConfirmPassword.setManaged(isModeInscription);

        if (isModeInscription) {
            lblTitre.setText("Créer un Compte");
            lblSousTitre.setText("Inscrivez-vous en tant que lecteur.");
            btnAction.setText("S'inscrire");
            lblSwitchText.setText("Vous avez déjà un compte ?");
            linkSwitch.setText("Se connecter");
        } else {
            lblTitre.setText("Bienvenue !");
            lblSousTitre.setText("Connectez-vous à votre espace de gestion.");
            btnAction.setText("Se connecter");
            lblSwitchText.setText("Vous n'avez pas de compte ?");
            linkSwitch.setText("Créer un compte");
        }
    }

    @FXML
    private void handleAction() {
        lblMessage.setText("");

        if (isModeInscription) {
            traiterInscription();
        } else {
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
            boolean inscrit = utilisateurService.inscrireLecteur(login, nomComplet, password);

            if (inscrit) {
                FxUtil.showSuccess("Inscription réussie", "Votre compte lecteur a été créé. Vous pouvez maintenant vous connecter.");
                txtPassword.clear();
                txtPasswordVisible.clear();
                txtConfirmPassword.clear();
                switchMode();
            }
        } catch (ServiceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    private void ouvrirTableauDeBord(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/view/dashboard.fxml")));
            Parent root = loader.load();

            // 1. Fermer la fenêtre de login actuelle
            Stage loginStage = (Stage) btnAction.getScene().getWindow();
            loginStage.close();

            // 2. Créer une nouvelle fenêtre (Stage) pour le Dashboard avec le style transparent
            Stage dashboardStage = new Stage();
            dashboardStage.initStyle(StageStyle.TRANSPARENT);

            // Appliquer l'icône de l'application sur le nouveau Stage
            ajouterIconeStage(dashboardStage);

            Scene scene = new Scene(root, 1250, 650);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT); // Permet les coins arrondis du dashboard

            dashboardStage.setTitle("GCA Atsimo-Andrefana - Espace " + user.getRole().name() + " (" + user.getNomComplet() + ")");
            dashboardStage.setScene(scene);
            dashboardStage.setResizable(true);
            dashboardStage.centerOnScreen();
            dashboardStage.show();

        } catch (IOException e) {
            FxUtil.showError("Erreur de navigation", "Impossible de charger le tableau de bord : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ajouterIconeStage(Stage stage) {
        try (InputStream stream = getClass().getResourceAsStream("/images/logo.png")) {
            if (stream != null) {
                stage.getIcons().add(new Image(stream));
                return;
            }
        } catch (Exception ignored) {
        }

        try (InputStream icoStream = getClass().getResourceAsStream("/images/logo.ico")) {
            if (icoStream != null) {
                stage.getIcons().add(new Image(icoStream));
            }
        } catch (Exception ignored) {
        }
    }
}