
package com.contact.controller;

import com.contact.dao.UtilisateurDAO;
import com.contact.model.Role;
import com.contact.model.Utilisateur;
import com.contact.utils.FxUtil;
import com.contact.utils.ManagedController;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class UtilisateurController implements ManagedController {

    @FXML private TableView<Utilisateur> tableViewUtilisateurs;

    @FXML private TableColumn<Utilisateur, Long> colId;
    @FXML private TableColumn<Utilisateur, String> colLogin;
    @FXML private TableColumn<Utilisateur, String> colNomComplet;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, String> colActif;
    @FXML private TableColumn<Utilisateur, Object> colActions;

    private final ObservableList<Utilisateur> listeUtilisateurs =
            FXCollections.observableArrayList();

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    private Utilisateur utilisateurCourant;

    @FXML
    public void initialize() {
        configurerColonnes();
        tableViewUtilisateurs.setItems(listeUtilisateurs);
        chargerUtilisateurs();
    }

    // =========================================
    // CONFIGURATION DES COLONNES
    // =========================================

    private void configurerColonnes() {

        // ID
        colId.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        // Login
        colLogin.setCellValueFactory(
                new PropertyValueFactory<>("login")
        );

        // Nom complet
        colNomComplet.setCellValueFactory(
                new PropertyValueFactory<>("nomComplet")
        );

        // Rôle
        colRole.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getRole().name()
                )
        );

        // Statut
        colActif.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().isActif() ? "Actif" : "Inactif"
                )
        );

        // Personnalisation visuelle du rôle
        colRole.setCellFactory(column -> new TableCell<>() {

            private final Label badge = new Label();

            {
                setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);

                if (empty || role == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                badge.setText(role);

                badge.getStyleClass().setAll("role-badge");

                if ("ADMIN".equals(role)) {
                    badge.getStyleClass().add("role-admin");
                } else {
                    badge.getStyleClass().add("role-lecteur");
                }

                setText(null);
                setGraphic(badge);
            }
        });

        // Personnalisation visuelle du statut
        colActif.setCellFactory(column -> new TableCell<>() {

            private final Label badge = new Label();

            {
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);

                if (empty || statut == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                badge.setText(statut);

                badge.getStyleClass().setAll("status-badge");

                if ("Actif".equals(statut)) {
                    badge.getStyleClass().add("status-active");
                } else {
                    badge.getStyleClass().add("status-inactive");
                }

                setText(null);
                setGraphic(badge);
            }
        });

        // Actions
        colActions.setCellFactory(this::creerCelluleAction);

        // Alignement
        colId.setStyle("-fx-alignment: CENTER;");
        colActif.setStyle("-fx-alignment: CENTER;");
        colActions.setStyle("-fx-alignment: CENTER;");
    }

    // =========================================
    // CHARGEMENT DES UTILISATEURS
    // =========================================

    private void chargerUtilisateurs() {

        try {
            List<Utilisateur> utilisateurs = utilisateurDAO.listerTous();

            listeUtilisateurs.setAll(
                    utilisateurs != null ? utilisateurs : List.of()
            );

            tableViewUtilisateurs.refresh();

        } catch (Exception e) {
            FxUtil.showError(
                    "Erreur",
                    "Impossible de charger les utilisateurs : " + e.getMessage()
            );
        }
    }

    // =========================================
    // CELLULE PERSONNALISÉE DES ACTIONS
    // =========================================

    private TableCell<Utilisateur, Object> creerCelluleAction(
            TableColumn<Utilisateur, Object> column) {

        return new TableCell<>() {

            private final Button btnBasculerRole =
                    new Button("Changer le rôle");

            private final FontIcon iconAction =
                    new FontIcon("fas-exchange-alt");

            private final HBox container =
                    new HBox(btnBasculerRole);

            {
                container.setAlignment(Pos.CENTER);

                iconAction.setIconSize(13);

                btnBasculerRole.setGraphic(iconAction);
                btnBasculerRole.getStyleClass().add("role-action-button");

                btnBasculerRole.setOnAction(event -> {

                    int index = getIndex();

                    if (index < 0 || index >= getTableView().getItems().size()) {
                        return;
                    }

                    Utilisateur utilisateur =
                            getTableView().getItems().get(index);

                    if (utilisateur != null) {
                        basculerRole(utilisateur);
                    }
                });
            }

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0 ||
                        getIndex() >= getTableView().getItems().size()) {

                    setGraphic(null);
                    setText(null);
                    return;
                }

                Utilisateur utilisateur =
                        getTableView().getItems().get(getIndex());

                if (utilisateur == null) {
                    setGraphic(null);
                    return;
                }

                if (utilisateur.getRole() == Role.ADMIN) {

                    btnBasculerRole.setText("Rétrograder");

                    btnBasculerRole.getStyleClass().removeAll(
                            "promote-button", "demote-button"
                    );

                    btnBasculerRole.getStyleClass().add("demote-button");

                } else {

                    btnBasculerRole.setText("Promouvoir");

                    btnBasculerRole.getStyleClass().removeAll(
                            "promote-button", "demote-button"
                    );

                    btnBasculerRole.getStyleClass().add("promote-button");
                }

                setText(null);
                setGraphic(container);
            }
        };
    }

    // =========================================
    // MODIFICATION DU RÔLE
    // =========================================

    private void basculerRole(Utilisateur utilisateur) {

        Role nouveauRole =
                utilisateur.getRole() == Role.ADMIN
                        ? Role.LECTEUR
                        : Role.ADMIN;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Modification du rôle");
        alert.setHeaderText(
                "Modifier les autorisations de " + utilisateur.getLogin()
        );

        alert.setContentText(
                "Voulez-vous vraiment attribuer le rôle "
                        + nouveauRole.name()
                        + " à cet utilisateur ?"
        );

        alert.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                try {

                    boolean succes = utilisateurDAO.mettreAJourRole(
                            utilisateur.getId(),
                            nouveauRole
                    );

                    if (succes) {

                        FxUtil.showSuccess(
                                "Modification réussie",
                                "Le rôle de l'utilisateur a été modifié."
                        );

                        chargerUtilisateurs();

                    } else {

                        FxUtil.showError(
                                "Échec",
                                "La modification du rôle n'a pas abouti."
                        );
                    }

                } catch (Exception e) {

                    FxUtil.showError(
                            "Erreur technique",
                            "Impossible de modifier le rôle : " + e.getMessage()
                    );
                }
            }
        });
    }

    // =========================================
    // UTILISATEUR CONNECTÉ
    // =========================================

    @Override
    public void initialiserDroits(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
    }
}
