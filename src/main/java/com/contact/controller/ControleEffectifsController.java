package com.contact.controller;

import com.contact.model.EffectifStatut;
import com.contact.model.Utilisateur;
import com.contact.dao.RegleEffectifDAO;
import com.contact.utils.FxUtil;
import com.contact.utils.ManagedController;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class ControleEffectifsController implements ManagedController {

    @FXML private TableView<EffectifStatut> tableEffectifs;
    @FXML private TableColumn<EffectifStatut, String> colLocalite;
    @FXML private TableColumn<EffectifStatut, String> colTypeLocalite;
    @FXML private TableColumn<EffectifStatut, String> colFonction;
    @FXML private TableColumn<EffectifStatut, Integer> colPrevu;
    @FXML private TableColumn<EffectifStatut, Integer> colActuel;
    @FXML private TableColumn<EffectifStatut, Integer> colManquant;

    // Éléments des 4 cartes KPI
    @FXML private Label lblTotalPostes;
    @FXML private Label lblPostesComplets;
    @FXML private Label lblPostesDeficit;
    @FXML private Label lblPersonnesSuspectes;

    private final RegleEffectifDAO regleEffectifDAO = new RegleEffectifDAO();
    private final ObservableList<EffectifStatut> listeStatuts = FXCollections.observableArrayList();
    private Utilisateur utilisateurCourant;

    @FXML
    public void initialize() {
        configurerColonnes();
        configurerLignesColorees();
        chargerDonnees();
    }

    private void configurerColonnes() {
        colLocalite.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNomLocalite()));
        colTypeLocalite.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTypeLocalite() != null ? cell.getValue().getTypeLocalite().name() : ""));
        colFonction.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLibelleFonction()));
        colPrevu.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getEffectifPrevu()).asObject());
        colActuel.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getEffectifActuel()).asObject());
        colManquant.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getManquants()).asObject());
    }

    private void configurerLignesColorees() {
        tableEffectifs.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(EffectifStatut item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.getManquants() > 0) {
                    setStyle("-fx-background-color: #fee2e2;"); // Rouge clair (Déficit)
                } else {
                    setStyle("-fx-background-color: #f0fdf4;"); // Vert clair (Complet)
                }
            }
        });
    }

    private void chargerDonnees() {
        try {
            List<EffectifStatut> statuts = regleEffectifDAO.calculerStatutsEffectifs();
            listeStatuts.setAll(statuts);
            tableEffectifs.setItems(listeStatuts);

            // Calcul des indicateurs pour les cartes KPI
            int total = statuts.size();
            int complets = 0;
            int deficit = 0;

            for (EffectifStatut s : statuts) {
                if (s.getManquants() > 0) {
                    deficit++;
                } else {
                    complets++;
                }
            }

            lblTotalPostes.setText(String.valueOf(total));
            lblPostesComplets.setText(String.valueOf(complets));
            lblPostesDeficit.setText(String.valueOf(deficit));

            // Comptage des personnes suspectes depuis la base
            int nbSuspectes = compterPersonnesSuspectes();
            lblPersonnesSuspectes.setText(String.valueOf(nbSuspectes));

        } catch (Exception e) {
            FxUtil.showError("Erreur", "Impossible de charger le contrôle des effectifs : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int compterPersonnesSuspectes() {
        String sql = "SELECT COUNT(*) FROM contact c JOIN fonction f ON c.fonction_id = f.id WHERE LOWER(f.libelle) LIKE '%suspecte%' AND c.actif = TRUE";
        try (Connection conn = com.contact.config.Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void initialiserDroits(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
    }
}