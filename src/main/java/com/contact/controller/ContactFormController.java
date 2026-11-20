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

public class ContactFormController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private ComboBox<Fonction> cmbFonction;
    @FXML private ComboBox<Organisation> cmbOrganisation;

    @FXML private ComboBox<Localite> cmbDistrict;
    @FXML private ComboBox<Localite> cmbCommune;
    @FXML private ComboBox<Localite> cmbFokotany;
    @FXML private ComboBox<Localite> cmbVillage;

    @FXML private TextField txtCodeDistrict;
    @FXML private TextField txtCodeCommune;
    @FXML private TextField txtCodeFokotany;
    @FXML private TextField txtTypeCom;
    @FXML private TextField txtCategorieCom;

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
            // CHARGER UNIQUEMENT LES 9 DISTRICTS DE LA RÉGION ATSIMO-ANDREFANA
            cmbDistrict.setItems(FXCollections.observableArrayList(localiteDAO.listerDistrictsRaa()));

            // CHARGER LES FONCTIONS POUR RAA SAUF LES FONCTIONS D'ANNEXE (ANN)
            List<Fonction> fonctionsRaa = fonctionDAO.listerToutes();
            fonctionsRaa.removeIf(f -> f.getCategorieCode() != null && f.getCategorieCode().equals("ANN"));
            cmbFonction.setItems(FXCollections.observableArrayList(fonctionsRaa));

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

            cmbCommune.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, commune) -> {
                if (commune != null) {
                    txtTypeCom.setText(commune.getTypeCom() != null ? commune.getTypeCom() : "");
                    txtCategorieCom.setText(commune.getCategorieCom() != null ? commune.getCategorieCom() : "");
                } else {
                    txtTypeCom.clear();
                    txtCategorieCom.clear();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- CASCADES TERRITORIALES ---
        cmbDistrict.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newDistrict) -> {
            cmbCommune.getItems().clear();
            cmbFokotany.getItems().clear();
            cmbVillage.getItems().clear();
            cmbCommune.setDisable(true);
            cmbFokotany.setDisable(true);
            cmbVillage.setDisable(true);
            txtCodeCommune.clear();
            txtCodeFokotany.clear();

            if (newDistrict != null) {
                txtCodeDistrict.setText(newDistrict.getCodeDistrict() != null ? newDistrict.getCodeDistrict() : "");
                adapterChampsSelonNiveauFonction(cmbFonction.getValue());

                List<Localite> communes = localiteDAO.listerParParent(newDistrict.getId());
                communes.removeIf(c -> c == null || c.getNom() == null || c.getNom().trim().isEmpty());
                cmbCommune.setItems(FXCollections.observableArrayList(communes));
            } else {
                txtCodeDistrict.clear();
                cmbCommune.setDisable(true);
                cmbFokotany.setDisable(true);
                cmbVillage.setDisable(true);
            }
        });

        cmbCommune.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newCommune) -> {
            cmbFokotany.getItems().clear();
            cmbVillage.getItems().clear();
            cmbFokotany.setDisable(true);
            cmbVillage.setDisable(true);
            txtCodeFokotany.clear();

            if (newCommune != null) {
                txtCodeCommune.setText(newCommune.getCodeCommune() != null ? newCommune.getCodeCommune() : "");
                adapterChampsSelonNiveauFonction(cmbFonction.getValue());

                List<Localite> fokotany = localiteDAO.listerParParent(newCommune.getId());
                fokotany.removeIf(f -> f == null || f.getNom() == null || f.getNom().trim().isEmpty());
                cmbFokotany.setItems(FXCollections.observableArrayList(fokotany));
            } else {
                txtCodeCommune.clear();
                cmbFokotany.setDisable(true);
                cmbVillage.setDisable(true);
            }
        });

        cmbFokotany.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newFokotany) -> {
            cmbVillage.getItems().clear();
            cmbVillage.setDisable(true);
            txtCodeFokotany.clear();

            if (newFokotany != null) {
                txtCodeFokotany.setText(newFokotany.getCodeFokotany() != null ? newFokotany.getCodeFokotany() : "");

                List<Localite> villages = localiteDAO.listerParParent(newFokotany.getId());
                villages.removeIf(v -> v == null || v.getNom() == null || v.getNom().trim().isEmpty());

                cmbVillage.setItems(FXCollections.observableArrayList(villages));
                cmbVillage.setDisable(false);
            } else {
                txtCodeFokotany.clear();
                cmbVillage.setDisable(true);
            }
        });

        cmbFonction.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                adapterChampsSelonNiveauFonction(newVal);

                boolean estSuspect = newVal.getCategorieCode() != null && newVal.getCategorieCode().equals("SUSP");
                if (estSuspect) {
                    txtMotif.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-background-color: #fdf2f2;");
                    if (lblMotifTitre != null) {
                        lblMotifTitre.setText("Motif / Signalement * (OBLIGATOIRE - Personne suspecte)");
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

    private void adapterChampsSelonNiveauFonction(Fonction fonction) {
        if (fonction == null || fonction.getNiveauCible() == null) {
            if (cmbDistrict.getValue() != null) cmbCommune.setDisable(false);
            if (cmbCommune.getValue() != null) cmbFokotany.setDisable(false);
            if (cmbFokotany.getValue() != null) cmbVillage.setDisable(false);
            return;
        }

        String niveau = fonction.getNiveauCible().toUpperCase();

        switch (niveau) {
            case "REGION":
            case "DISTRICT":
                cmbCommune.getSelectionModel().clearSelection();
                cmbCommune.setDisable(true);
                cmbFokotany.getSelectionModel().clearSelection();
                cmbFokotany.setDisable(true);
                cmbVillage.getSelectionModel().clearSelection();
                cmbVillage.setDisable(true);
                break;

            case "COMMUNE":
                cmbCommune.setDisable(cmbDistrict.getValue() == null);
                cmbFokotany.getSelectionModel().clearSelection();
                cmbFokotany.setDisable(true);
                cmbVillage.getSelectionModel().clearSelection();
                cmbVillage.setDisable(true);
                break;

            case "FOKOTANY":
            default:
                cmbCommune.setDisable(cmbDistrict.getValue() == null);
                cmbFokotany.setDisable(cmbCommune.getValue() == null);
                cmbVillage.setDisable(cmbFokotany.getValue() == null);
                break;
        }
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
        } else {
            cmbOrganisation.setValue(null);
        }

        if (contact.getLocaliteId() != null) {
            try {
                Localite locContact = localiteDAO.trouverParId(contact.getLocaliteId());
                if (locContact != null) {
                    String type = locContact.getTypeLocalite() != null ? locContact.getTypeLocalite().name() : "";

                    if (type.equalsIgnoreCase("VILLAGE")) {
                        Localite parentFokotany = localiteDAO.trouverParId(locContact.getParentId());
                        if (parentFokotany != null) {
                            selectionnerHierarchieDepuisFokotany(parentFokotany);
                            cmbVillage.setValue(locContact);
                        }
                    } else if (type.equalsIgnoreCase("FOKOTANY")) {
                        selectionnerHierarchieDepuisFokotany(locContact);
                    } else if (type.equalsIgnoreCase("COMMUNE")) {
                        selectionnerHierarchieDepuisCommune(locContact);
                    } else if (type.equalsIgnoreCase("DISTRICT")) {
                        for (Localite d : cmbDistrict.getItems()) {
                            if (d.getId().equals(locContact.getId())) {
                                cmbDistrict.setValue(d);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void selectionnerHierarchieDepuisFokotany(Localite fokotany) {
        try {
            Localite parentCommune = localiteDAO.trouverParId(fokotany.getParentId());
            if (parentCommune != null) {
                Localite parentDistrict = localiteDAO.trouverParId(parentCommune.getParentId());
                if (parentDistrict != null) {
                    cmbDistrict.getItems().stream().filter(d -> d.getId().equals(parentDistrict.getId())).findFirst().ifPresent(cmbDistrict::setValue);
                }
                cmbCommune.getItems().stream().filter(c -> c.getId().equals(parentCommune.getId())).findFirst().ifPresent(cmbCommune::setValue);
            }
            cmbFokotany.getItems().stream().filter(f -> f.getId().equals(fokotany.getId())).findFirst().ifPresent(cmbFokotany::setValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectionnerHierarchieDepuisCommune(Localite commune) {
        try {
            Localite parentDistrict = localiteDAO.trouverParId(commune.getParentId());
            if (parentDistrict != null) {
                cmbDistrict.getItems().stream().filter(d -> d.getId().equals(parentDistrict.getId())).findFirst().ifPresent(cmbDistrict::setValue);
            }
            cmbCommune.getItems().stream().filter(c -> c.getId().equals(commune.getId())).findFirst().ifPresent(cmbCommune::setValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void enregistrerContact() {
        if (txtNom.getText().trim().isEmpty() || cmbFonction.getValue() == null || cmbDistrict.getValue() == null || txtTelephone1.getText().trim().isEmpty()) {
            afficherAlerte("Champs manquants", "Veuillez remplir les champs obligatoires (* Nom, Fonction, District, Téléphone).");
            return;
        }

        Fonction fonctionSelectionnee = cmbFonction.getValue();
        boolean estSuspect = fonctionSelectionnee.getCategorieCode() != null && fonctionSelectionnee.getCategorieCode().equals("SUSP");

        if (estSuspect && (txtMotif.getText() == null || txtMotif.getText().trim().isEmpty())) {
            txtMotif.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-background-color: #fdf2f2;");
            afficherAlerte("Motif obligatoire", "Cette fonction est classée comme 'Personne suspecte'. Le champ motif est obligatoire.");
            return;
        }

        try {
            Long targetLocaliteId = cmbDistrict.getValue().getId();

            if (!cmbCommune.isDisabled() && cmbCommune.getValue() != null) {
                targetLocaliteId = cmbCommune.getValue().getId();
            }
            if (!cmbFokotany.isDisabled() && cmbFokotany.getValue() != null) {
                targetLocaliteId = cmbFokotany.getValue().getId();
            }

            if (!cmbVillage.isDisabled()) {
                Object villageValue = ((ComboBox<?>) cmbVillage).getValue();
                String typedVillageText = cmbVillage.getEditor().getText();

                if (villageValue instanceof Localite selectedVillage) {
                    targetLocaliteId = selectedVillage.getId();
                } else if (typedVillageText != null && !typedVillageText.trim().isEmpty() && cmbFokotany.getValue() != null) {
                    long newVillageId = localiteDAO.trouverOuCreerLocalite(typedVillageText.trim(), "VILLAGE", cmbFokotany.getValue().getId());
                    targetLocaliteId = newVillageId;
                }
            }

            Organisation orgSelected = cmbOrganisation.getValue();
            Long organisationId = (orgSelected != null) ? orgSelected.getId() : null;

            Contact contact = new Contact();
            if (contactIdEnCours != null) {
                contact.setId(contactIdEnCours);
            }
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

            boolean succes;
            if (contactIdEnCours == null) {
                succes = contactService.creerContact(contact);
            } else {
                succes = contactService.modifierContact(contact);
            }

            if (succes) {
                String messageSucces = (contactIdEnCours == null) ? "Le contact a été créé avec succès !" : "Le contact a été mis à jour avec succès !";
                afficherSucces("Succès", messageSucces);
                fermerFenetre();
            }

        } catch (Exception e) {
            afficherAlerte("Erreur", "Impossible d'enregistrer le contact :\n" + e.getMessage());
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