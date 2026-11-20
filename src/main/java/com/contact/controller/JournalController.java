package com.contact.controller;

import com.contact.dao.UtilisateurDAO;
import com.contact.exception.ServiceException;
import com.contact.model.ActionJournal;
import com.contact.model.JournalEntry;
import com.contact.model.Role;
import com.contact.model.Utilisateur;
import com.contact.service.JournalService;
import com.contact.utils.FxUtil;
import com.contact.utils.ManagedController;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JournalController implements ManagedController {

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML private ComboBox<String> cbFiltreAction;
    @FXML private TextField txtRecherche;
    @FXML private Button btnActualiser;

    @FXML private TableView<JournalEntry> tableJournal;
    @FXML private TableColumn<JournalEntry, String> colDate;
    @FXML private TableColumn<JournalEntry, String> colUtilisateur;
    @FXML private TableColumn<JournalEntry, String> colAction;
    @FXML private TableColumn<JournalEntry, String> colTable;
    @FXML private TableColumn<JournalEntry, String> colIdCible;
    @FXML private TableColumn<JournalEntry, String> colDetails;

    private final JournalService journalService = new JournalService();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    private final ObservableList<JournalEntry> donnees = FXCollections.observableArrayList();
    private FilteredList<JournalEntry> donneesFiltrees;
    private final Map<Long, String> utilisateurNomsMap = new HashMap<>();

    @FXML
    public void initialize() {
        configurerColonnes();
        configurerFiltres();

        donneesFiltrees = new FilteredList<>(donnees, e -> true);
        tableJournal.setItems(donneesFiltrees);

        chargerUtilisateursMap();
        actualiserJournal();
    }

    @Override
    public void initialiserDroits(Utilisateur utilisateur) {
        boolean estAdmin = utilisateur != null && utilisateur.getRole() == Role.ADMIN;
        if (tableJournal != null) {
            tableJournal.setDisable(!estAdmin);
        }
        if (btnActualiser != null) {
            btnActualiser.setDisable(!estAdmin);
        }
    }

    private void chargerUtilisateursMap() {
        utilisateurNomsMap.clear();
        try {
            for (Utilisateur u : utilisateurDAO.listerTous()) {
                String label = (u.getNomComplet() != null && !u.getNomComplet().isBlank())
                        ? u.getNomComplet() + " (" + u.getLogin() + ")"
                        : u.getLogin();
                utilisateurNomsMap.put(u.getId(), label);
            }
        } catch (Exception ignored) {
            // Repli automatique sur l'ID si non disponible
        }
    }

    private void configurerColonnes() {
        colDate.setCellValueFactory(cd -> {
            JournalEntry j = cd.getValue();
            if (j == null || j.getDateAction() == null) {
                return new SimpleStringProperty("—");
            }
            return new SimpleStringProperty(j.getDateAction().format(FORMAT_DATE));
        });

        colUtilisateur.setCellValueFactory(cd -> {
            Long uid = cd.getValue() != null ? cd.getValue().getUtilisateurId() : null;
            if (uid == null) {
                return new SimpleStringProperty("Système");
            }
            return new SimpleStringProperty(utilisateurNomsMap.getOrDefault(uid, "Utilisateur #" + uid));
        });

        colAction.setCellValueFactory(cd -> {
            ActionJournal act = cd.getValue() != null ? cd.getValue().getAction() : null;
            return new SimpleStringProperty(act != null ? act.name() : "—");
        });

        colAction.setCellFactory(column -> new TableCell<JournalEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                switch (item) {
                    case "SUPPRESSION", "DESACTIVATION" ->
                            setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    case "CREATION" ->
                            setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    case "MODIFICATION" ->
                            setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    case "CONNEXION", "DECONNEXION" ->
                            setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                    default ->
                            setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
                }
            }
        });

        colTable.setCellValueFactory(cd -> {
            String tbl = cd.getValue() != null ? cd.getValue().getTableCible() : null;
            return new SimpleStringProperty(tbl != null && !tbl.isBlank() ? tbl : "—");
        });

        colIdCible.setCellValueFactory(cd -> {
            Long idCible = cd.getValue() != null ? cd.getValue().getIdCible() : null;
            if (idCible == null || idCible == 0L) {
                return new SimpleStringProperty("—");
            }
            return new SimpleStringProperty(String.valueOf(idCible));
        });

        colDetails.setCellValueFactory(cd -> {
            String det = cd.getValue() != null ? cd.getValue().getDetails() : null;
            return new SimpleStringProperty(det != null && !det.isBlank() ? det : "—");
        });
    }

    private void configurerFiltres() {
        cbFiltreAction.getItems().add("Toutes les actions");
        for (ActionJournal act : ActionJournal.values()) {
            cbFiltreAction.getItems().add(act.name());
        }
        cbFiltreAction.setValue("Toutes les actions");

        cbFiltreAction.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
    }

    private void appliquerFiltres() {
        if (donneesFiltrees == null) return;

        String actionSel = cbFiltreAction.getValue();
        String recherche = txtRecherche.getText() != null
                ? txtRecherche.getText().trim().toLowerCase()
                : "";

        donneesFiltrees.setPredicate(entry -> {
            boolean okAction = true;
            if (actionSel != null && !"Toutes les actions".equals(actionSel)) {
                okAction = entry.getAction() != null && actionSel.equals(entry.getAction().name());
            }

            boolean okRecherche = true;
            if (!recherche.isEmpty()) {
                String userStr = entry.getUtilisateurId() != null
                        ? utilisateurNomsMap.getOrDefault(entry.getUtilisateurId(), String.valueOf(entry.getUtilisateurId())).toLowerCase()
                        : "système";
                String tableStr = entry.getTableCible() != null ? entry.getTableCible().toLowerCase() : "";
                String detailsStr = entry.getDetails() != null ? entry.getDetails().toLowerCase() : "";
                String actionStr = entry.getAction() != null ? entry.getAction().name().toLowerCase() : "";
                String idStr = entry.getIdCible() != null ? String.valueOf(entry.getIdCible()) : "";

                okRecherche = userStr.contains(recherche)
                        || tableStr.contains(recherche)
                        || detailsStr.contains(recherche)
                        || actionStr.contains(recherche)
                        || idStr.contains(recherche);
            }

            return okAction && okRecherche;
        });
    }

    @FXML
    private void actualiserJournal() {
        try {
            chargerUtilisateursMap();
            List<JournalEntry> liste = journalService.listerJournal();
            donnees.setAll(liste != null ? liste : List.of());
        } catch (ServiceException e) {
            FxUtil.showError("Journal d'audit", e.getMessage());
        }
    }
}