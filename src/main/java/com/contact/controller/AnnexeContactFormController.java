package com.contact.controller;

import com.contact.dao.FonctionDAO;
import com.contact.dao.LocaliteDAO;
import com.contact.dao.OrganisationDAO;
import com.contact.model.Contact;
import com.contact.model.Fonction;
import com.contact.model.Localite;
import com.contact.model.Organisation;
import com.contact.service.ContactService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class AnnexeContactFormController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private ComboBox<Fonction> cmbFonction;
    @FXML private ComboBox<Organisation> cmbOrganisation;

    // Découpage territorial limitrophe
    @FXML private ComboBox<Localite> cmbRegionLimitrophe;
    @FXML private ComboBox<Localite> cmbDistrict;
    @FXML private ComboBox<Localite> cmbCommune;
    @FXML private ComboBox<Localite> cmbFokotany;

    @FXML private TextField txtTelephone1;
    @FXML private TextField txtTelephone2;
    @FXML private TextField txtEmail;
    @FXML private TextArea txtObservation;
    @FXML private TextArea txtMotif;
    @FXML private Label lblMotifTitre;

    private final ContactService contactService = new ContactService();
    private final LocaliteDAO localiteDAO = new LocaliteDAO();
    private final OrganisationDAO organisationDAO = new OrganisationDAO();
    private final FonctionDAO fonctionDAO = new FonctionDAO();

    private Long contactIdEnCours = null;

    @FXML
    public void initialize() {
        try {
            // Charger uniquement les régions limitrophes (LIMITROPHE)
            List<Localite> regions = localiteDAO.listerToutes();
            regions.removeIf(l -> !"LIMITROPHE".equalsIgnoreCase(l.getTypeLocalite().name()));
            cmbRegionLimitrophe.setItems(FXCollections.observableArrayList(regions));

            cmbFonction.setItems(FXCollections.observableArrayList(fonctionDAO.listerToutes()));

            var orgs = organisationDAO.listerToutes();
            orgs.add(0, null);
            cmbOrganisation.setItems(FXCollections.observableArrayList(orgs));

            cmbOrganisation.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Organisation item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "Aucune structure particulière" : item.getNom());
                }
            });
            cmbOrganisation.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Organisation item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "Aucune structure particulière" : item.getNom());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- CASCADES TERRITORIALES LIMITROPHES ---

        // 1. Région Limitrophe -> District
        cmbRegionLimitrophe.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newRegion) -> {
            cmbDistrict.getItems().clear();
            cmbCommune.getItems().clear();
            cmbFokotany.getItems().clear();
            cmbDistrict.setDisable(true);
            cmbCommune.setDisable(true);
            cmbFokotany.setDisable(true);

            if (newRegion != null) {
                List<Localite> districts = localiteDAO.listerParParent(newRegion.getId());
                districts.removeIf(d -> d == null || d.getNom() == null || d.getNom().trim().isEmpty());
                cmbDistrict.setItems(FXCollections.observableArrayList(districts));
                cmbDistrict.setDisable(false);
            }
        });

        // 2. District -> Commune
        cmbDistrict.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newDistrict) -> {
            cmbCommune.getItems().clear();
            cmbFokotany.getItems().clear();
            cmbCommune.setDisable(true);
            cmbFokotany.setDisable(true);

            if (newDistrict != null) {
                List<Localite> communes = localiteDAO.listerParParent(newDistrict.getId());
                communes.removeIf(c -> c == null || c.getNom() == null || c.getNom().trim().isEmpty());
                cmbCommune.setItems(FXCollections.observableArrayList(communes));
                cmbCommune.setDisable(false);
            }
        });

        // 3. Commune -> Fokotany
        cmbCommune.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newCommune) -> {
            cmbFokotany.getItems().clear();
            cmbFokotany.setDisable(true);

            if (newCommune != null) {
                List<Localite> fokotany = localiteDAO.listerParParent(newCommune.getId());
                fokotany.removeIf(f -> f == null || f.getNom() == null || f.getNom().trim().isEmpty());
                cmbFokotany.setItems(FXCollections.observableArrayList(fokotany));
                cmbFokotany.setDisable(false);
            }
        });

        // Écouteur Fonction (Personne suspecte)
        cmbFonction.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean estSuspect = newVal.getCategorieCode() != null && newVal.getCategorieCode().equals("SUSP");
                if (estSuspect) {
                    txtMotif.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-background-color: #fdf2f2;");
                    if (lblMotifTitre != null) {
                        lblMotifTitre.setText("Motif / Signalement * (OBLIGATOIRE)");
                        lblMotifTitre.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                } else {
                    txtMotif.setStyle("");
                    if (lblMotifTitre != null) {
                        lblMotifTitre.setText("Motif (Réservé aux cas signalés / suspects)");
                        lblMotifTitre.setStyle("-fx-text-fill: #334155;");
                    }
                }
            }
        });
    }

    public void initModeModification(Contact contact) {
        this.contactIdEnCours = contact.getId();
        txtNom.setText(contact.getNom());
        txtPrenom.setText(contact.getPrenom() != null ? contact.getPrenom() : "");
        txtTelephone1.setText(contact.getTelephone1() != null ? contact.getTelephone1() : "");
        txtTelephone2.setText(contact.getTelephone2() != null ? contact.getTelephone2() : "");
        txtEmail.setText(contact.getEmail() != null ? contact.getEmail() : "");
        txtObservation.setText(contact.getObservation() != null ? contact.getObservation() : "");
        txtMotif.setText(contact.getMotif() != null ? contact.getMotif() : "");

        for (Fonction f : cmbFonction.getItems()) {
            if (f.getId().equals(contact.getFonctionId())) {
                cmbFonction.setValue(f);
                break;
            }
        }

        if (contact.getOrganisationId() != null) {
            for (Organisation org : cmbOrganisation.getItems()) {
                if (org != null && org.getId().equals(contact.getOrganisationId())) {
                    cmbOrganisation.setValue(org);
                    break;
                }
            }
        }

        // Remontée hiérarchique pour positionner Région Limitrophe, District, Commune, Fokotany
        if (contact.getLocaliteId() != null) {
            try {
                Localite loc = localiteDAO.trouverParId(contact.getLocaliteId());
                if (loc != null) {
                    // Logique de sélection selon le niveau de la localité liée au contact
                    positionnerHierarchie(loc);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void positionnerHierarchie(Localite loc) {
        try {
            String type = loc.getTypeLocalite() != null ? loc.getTypeLocalite().name() : "";
            Localite reg = null, dist = null, comm = null, fok = null;

            if (type.equalsIgnoreCase("FOKOTANY")) {
                fok = loc;
                comm = localiteDAO.trouverParId(fok.getParentId());
                if (comm != null) dist = localiteDAO.trouverParId(comm.getParentId());
                if (dist != null) reg = localiteDAO.trouverParId(dist.getParentId());
            } else if (type.equalsIgnoreCase("COMMUNE")) {
                comm = loc;
                dist = localiteDAO.trouverParId(comm.getParentId());
                if (dist != null) reg = localiteDAO.trouverParId(dist.getParentId());
            } else if (type.equalsIgnoreCase("DISTRICT")) {
                dist = loc;
                reg = localiteDAO.trouverParId(dist.getParentId());
            } else if (type.equalsIgnoreCase("LIMITROPHE")) {
                reg = loc;
            }

            if (reg != null) {
                for (Localite r : cmbRegionLimitrophe.getItems()) {
                    if (r.getId().equals(reg.getId())) { cmbRegionLimitrophe.setValue(r); break; }
                }
            }
            if (dist != null) {
                for (Localite d : cmbDistrict.getItems()) {
                    if (d.getId().equals(dist.getId())) { cmbDistrict.setValue(d); break; }
                }
            }
            if (comm != null) {
                for (Localite c : cmbCommune.getItems()) {
                    if (c.getId().equals(comm.getId())) { cmbCommune.setValue(c); break; }
                }
            }
            if (fok != null) {
                for (Localite f : cmbFokotany.getItems()) {
                    if (f.getId().equals(fok.getId())) { cmbFokotany.setValue(f); break; }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void enregistrerContact() {
        if (txtNom.getText().trim().isEmpty() || cmbFonction.getValue() == null || cmbRegionLimitrophe.getValue() == null || txtTelephone1.getText().trim().isEmpty()) {
            afficherAlerte("Champs manquants", "Veuillez remplir les champs obligatoires (* Nom, Fonction, Région Limitrophe, Téléphone).");
            return;
        }

        Fonction fonctionSelectionnee = cmbFonction.getValue();
        boolean estSuspect = fonctionSelectionnee.getCategorieCode() != null && fonctionSelectionnee.getCategorieCode().equals("SUSP");

        if (estSuspect && (txtMotif.getText() == null || txtMotif.getText().trim().isEmpty())) {
            txtMotif.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-background-color: #fdf2f2;");
            afficherAlerte("Motif obligatoire", "Le champ motif est obligatoire pour une personne suspecte.");
            return;
        }

        try {
            // Détermination de la localité la plus précise sélectionnée en cascade
            Long targetLocaliteId = cmbRegionLimitrophe.getValue().getId();
            if (!cmbDistrict.isDisabled() && cmbDistrict.getValue() != null) targetLocaliteId = cmbDistrict.getValue().getId();
            if (!cmbCommune.isDisabled() && cmbCommune.getValue() != null) targetLocaliteId = cmbCommune.getValue().getId();
            if (!cmbFokotany.isDisabled() && cmbFokotany.getValue() != null) targetLocaliteId = cmbFokotany.getValue().getId();

            Organisation orgSelected = cmbOrganisation.getValue();
            Long organisationId = (orgSelected != null) ? orgSelected.getId() : null;

            Contact contact = new Contact();
            if (contactIdEnCours != null) contact.setId(contactIdEnCours);

            contact.setNom(txtNom.getText().trim());
            contact.setPrenom(txtPrenom.getText() != null ? txtPrenom.getText().trim() : "");
            contact.setFonctionId(fonctionSelectionnee.getId());
            contact.setOrganisationId(organisationId);
            contact.setLocaliteId(targetLocaliteId);
            contact.setTelephone1(txtTelephone1.getText().trim());
            contact.setTelephone2(txtTelephone2.getText() != null ? txtTelephone2.getText().trim() : "");
            contact.setEmail(txtEmail.getText() != null ? txtEmail.getText().trim() : "");
            contact.setObservation(txtObservation.getText() != null ? txtObservation.getText().trim() : "");
            contact.setMotif(txtMotif.getText() != null ? txtMotif.getText().trim() : "");

            boolean succes = (contactIdEnCours == null) ? contactService.creerContact(contact) : contactService.modifierContact(contact);

            if (succes) {
                afficherSucces("Succès", "Le contact d'annexe a été enregistré avec succès !");
                fermerFenetre();
            }

        } catch (Exception e) {
            afficherAlerte("Erreur", "Impossible d'enregistrer :\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void fermerFenetre() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherSucces(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}