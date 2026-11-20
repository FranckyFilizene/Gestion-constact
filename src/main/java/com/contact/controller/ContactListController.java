package com.contact.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import com.contact.dao.CategorieDAO;
import com.contact.dao.FonctionDAO;
import com.contact.dao.LocaliteDAO;
import com.contact.dao.OrganisationDAO;
import com.contact.exception.ServiceException;
import com.contact.model.Categorie;
import com.contact.model.Contact;
import com.contact.model.Fonction;
import com.contact.model.Localite;
import com.contact.model.Organisation;
import com.contact.model.Role;
import com.contact.model.Utilisateur;
import com.contact.service.ContactService;
import com.contact.utils.FxUtil;
import com.contact.utils.ManagedController;

// Imports Apache POI pour Excel
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ContactListController implements ManagedController {

    // =========================================================================
    // 1. DICTIONNAIRES DE CORRESPONDANCE
    // =========================================================================
    private final Map<Long, Long> orgToCategorieIdMap = new HashMap<>();
    private final Map<Long, String> categorieIdToLibelleMap = new HashMap<>();
    private final Map<Long, String> fonctionIdToLibelleMap = new HashMap<>();
    private final Map<Long, Long> fonctionIdToCategorieIdMap = new HashMap<>();

    private final Map<Long, String> localiteIdToNomMap = new HashMap<>();
    private final Map<Long, Long> localiteIdToParentIdMap = new HashMap<>();
    private final Map<Long, String> localiteIdToTypeMap = new HashMap<>();
    private final Map<Long, String> localiteIdToCodeMap = new HashMap<>();

    // Nouveaux dictionnaires pour Type et Catégorie de Commune
    private final Map<Long, String> localiteIdToTypeComMap = new HashMap<>();
    private final Map<Long, String> localiteIdToCategorieComMap = new HashMap<>();

    // =========================================================================
    // 2. COMPOSANTS FXML
    // =========================================================================
    @FXML private TableView<Contact> tableViewContacts;

    @FXML private TableColumn<Contact, String> colCodeDistrict;
    @FXML private TableColumn<Contact, String> colNom;
    @FXML private TableColumn<Contact, String> colFonction;
    @FXML private TableColumn<Contact, String> colDistrict;
    @FXML private TableColumn<Contact, String> colCommune;
    @FXML private TableColumn<Contact, String> colFokotany;
    @FXML private TableColumn<Contact, String> colTelephone;
    @FXML private TableColumn<Contact, String> colMotif;
    @FXML private TableColumn<Contact, Object> colActions;
    @FXML private TableColumn<Contact, String> colVillage;

    @FXML private ComboBox<String> cbFiltreMenu;
    @FXML private ComboBox<Fonction> cbFiltreFonction;
    @FXML private ComboBox<Localite> cbFiltreDistrict;
    @FXML private ComboBox<Localite> cbFiltreCommune;
    @FXML private ComboBox<Localite> cbFiltreFokotany;

    @FXML private Button btnExporterExcel;
    @FXML private Button btnExporterPdf;
    @FXML private Button btnAjouterContact;

    @FXML private TextField txtRecherche;

    // =========================================================================
    // 3. VARIABLES D'ÉTAT & SERVICES
    // =========================================================================
    private Utilisateur utilisateurCourant;
    private final ObservableList<Contact> listeCompleteContacts = FXCollections.observableArrayList();
    private FilteredList<Contact> listeFiltree;
    private final ContactService contactService;
    private final LocaliteDAO localiteDAO = new LocaliteDAO();
    private final FonctionDAO fonctionDAO = new FonctionDAO();

    public ContactListController() {
        this.contactService = new ContactService();
    }

    // =========================================================================
    // 4. INITIALISATION DU CONTRÔLEUR
    // =========================================================================
    @FXML
    public void initialize() {
        chargerCorrespondancesCategories();
        configurerColonnesTableau();

        listeFiltree = new FilteredList<>(listeCompleteContacts, p -> true);
        SortedList<Contact> listeTriee = new SortedList<>(listeFiltree);
        listeTriee.comparatorProperty().bind(tableViewContacts.comparatorProperty());
        tableViewContacts.setItems(listeTriee);

        chargerDonneesDepuisBD();
        configurerFiltresCascades();
    }

    // =========================================================================
    // 5. GESTION DES FENÊTRES
    // =========================================================================
    @FXML
    private void ouvrirFenetreNouveauContact() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/contact_form.fxml")));
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setOnHidden(event -> chargerDonneesDepuisBD());
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            FxUtil.showError("Erreur", "Impossible d'ouvrir le formulaire de création.");
        }
    }

    private void ouvrirFenetreModification(Contact contact) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/contact_form.fxml"));
            Parent root = loader.load();

            ContactFormController formController = loader.getController();
            formController.initModeModification(contact);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setOnHidden(event -> chargerDonneesDepuisBD());
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            FxUtil.showError("Erreur", "Impossible d'ouvrir le formulaire de modification.");
        }
    }

    private void supprimerContact(Contact contact) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le contact : " + contact.getNom() + " "
                + (contact.getPrenom() != null ? contact.getPrenom() : ""));
        alert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement ce contact ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean succes = contactService.supprimerContact(contact.getId());
                    if (succes) {
                        FxUtil.showSuccess("Succès", "Le contact a été supprimé avec succès.");
                        chargerDonneesDepuisBD();
                    } else {
                        FxUtil.showError("Erreur", "La suppression a échoué.");
                    }
                } catch (Exception e) {
                    FxUtil.showError("Erreur", "Impossible de supprimer le contact :\n" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // =========================================================================
    // 6. CHARGEMENT DES DONNÉES DE RÉFÉRENCE
    // =========================================================================
    private void chargerCorrespondancesCategories() {
        try {
            CategorieDAO catDAO = new CategorieDAO();
            for (Categorie cat : catDAO.listerCategorie()) {
                categorieIdToLibelleMap.put(cat.getId(), cat.getLibelle());
            }

            OrganisationDAO orgDAO = new OrganisationDAO();
            for (Organisation org : orgDAO.listerToutes()) {
                if (org.getCategorieId() != null) {
                    orgToCategorieIdMap.put(org.getId(), org.getCategorieId());
                }
            }

            for (Fonction f : fonctionDAO.listerToutes()) {
                fonctionIdToLibelleMap.put(f.getId(), f.getLibelle());
                fonctionIdToCategorieIdMap.put(f.getId(), f.getCategorieId());
            }

            cbFiltreDistrict.setItems(FXCollections.observableArrayList(localiteDAO.listerDistricts()));

            for (Localite loc : localiteDAO.listerToutes()) {
                localiteIdToNomMap.put(loc.getId(), loc.getNom());
                localiteIdToParentIdMap.put(loc.getId(), loc.getParentId());
                localiteIdToTypeMap.put(loc.getId(), loc.getTypeLocalite() != null ? loc.getTypeLocalite().name() : "");
                if (loc.getCodeDistrict() != null) {
                    localiteIdToCodeMap.put(loc.getId(), loc.getCodeDistrict());
                }
                if (loc.getTypeCom() != null) {
                    localiteIdToTypeComMap.put(loc.getId(), loc.getTypeCom());
                }
                if (loc.getCategorieCom() != null) {
                    localiteIdToCategorieComMap.put(loc.getId(), loc.getCategorieCom());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Long trouverIdParType(Long localiteId, String type) {
        Long current = localiteId;
        while (current != null) {
            if (type.equalsIgnoreCase(localiteIdToTypeMap.get(current))) {
                return current;
            }
            current = localiteIdToParentIdMap.get(current);
        }
        return null;
    }

    private String trouverNomDistrictParent(Long localiteId) {
        if (localiteId == null) return "Inconnu";
        Long districtId = trouverIdParType(localiteId, "DISTRICT");
        if (districtId != null) {
            return localiteIdToNomMap.get(districtId);
        }
        return localiteIdToNomMap.getOrDefault(localiteId, "Inconnu");
    }

    private String trouverNomParType(Long localiteId, String type) {
        if (localiteId == null) return "";
        Long id = trouverIdParType(localiteId, type);
        return id != null ? localiteIdToNomMap.getOrDefault(id, "") : "";
    }

    private String trouverCodeDistrict(Long localiteId) {
        if (localiteId == null) return "";
        Long districtId = trouverIdParType(localiteId, "DISTRICT");
        return districtId != null ? localiteIdToCodeMap.getOrDefault(districtId, "") : "";
    }

    private String trouverTypeCom(Long localiteId) {
        if (localiteId == null) return "";
        Long communeId = trouverIdParType(localiteId, "COMMUNE");
        if (communeId != null && localiteIdToTypeComMap.containsKey(communeId)) {
            return localiteIdToTypeComMap.get(communeId);
        }
        return localiteIdToTypeComMap.getOrDefault(localiteId, "");
    }

    private String trouverCategorieCom(Long localiteId) {
        if (localiteId == null) return "";
        Long communeId = trouverIdParType(localiteId, "COMMUNE");
        if (communeId != null && localiteIdToCategorieComMap.containsKey(communeId)) {
            return localiteIdToCategorieComMap.get(communeId);
        }
        return localiteIdToCategorieComMap.getOrDefault(localiteId, "");
    }

    // =========================================================================
    // 7. CONFIGURATION DES COLONNES DU TABLEAU
    // =========================================================================
    private void configurerColonnesTableau() {
        colCodeDistrict.setCellValueFactory(cd ->
                new SimpleStringProperty(trouverCodeDistrict(cd.getValue().getLocaliteId())));

        colNom.setCellValueFactory(cd -> {
            Contact c = cd.getValue();
            String nomComplet = (c.getNom() != null ? c.getNom() : "") + " " + (c.getPrenom() != null ? c.getPrenom() : "");
            return new SimpleStringProperty(nomComplet.trim());
        });

        colFonction.setCellValueFactory(cd -> {
            Contact c = cd.getValue();
            String libelle = c.getFonctionId() != null
                    ? fonctionIdToLibelleMap.getOrDefault(c.getFonctionId(), "Inconnu") : "";
            return new SimpleStringProperty(libelle);
        });

        colDistrict.setCellValueFactory(cd -> {
            Contact c = cd.getValue();
            return new SimpleStringProperty(c.getLocaliteId() != null ? trouverNomDistrictParent(c.getLocaliteId()) : "");
        });

        colCommune.setCellValueFactory(cd ->
                new SimpleStringProperty(trouverNomParType(cd.getValue().getLocaliteId(), "COMMUNE")));

        colFokotany.setCellValueFactory(cd -> {
            Contact c = cd.getValue();
            String fokotanyStr = "";
            if (c.getLocaliteId() != null) {
                Long fId = trouverIdParType(c.getLocaliteId(), "FOKOTANY");
                if (fId != null) {
                    fokotanyStr = localiteIdToNomMap.getOrDefault(fId, "");
                }
            }
            if (fokotanyStr.trim().isEmpty()) {
                fokotanyStr = "—";
            }
            return new SimpleStringProperty(fokotanyStr);
        });

        colVillage.setCellValueFactory(cd -> {
            Contact c = cd.getValue();
            String villageStr = "";
            if (c.getLocaliteId() != null && "VILLAGE".equalsIgnoreCase(localiteIdToTypeMap.get(c.getLocaliteId()))) {
                villageStr = localiteIdToNomMap.getOrDefault(c.getLocaliteId(), "");
            }
            if (villageStr.trim().isEmpty()) {
                villageStr = "— (Chef-lieu / FKT)";
            }
            return new SimpleStringProperty(villageStr);
        });

        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone1"));

        colMotif.setCellValueFactory(cd -> {
            String motif = cd.getValue().getMotif() != null ? cd.getValue().getMotif().trim() : "";
            if (motif.isEmpty()) {
                motif = "—";
            }
            return new SimpleStringProperty(motif);
        });

        colMotif.setCellFactory(column -> new TableCell<Contact, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else if (item.equals("—")) {
                    setText(item);
                    setStyle("-fx-text-fill: #94a3b8;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                }
            }
        });

        colActions.setCellValueFactory(new PropertyValueFactory<>("id"));
        colActions.setCellFactory(this::creerCelluleAction);
        colActions.setSortable(false);
    }

    private TableCell<Contact, Object> creerCelluleAction(TableColumn<Contact, Object> column) {
        return new TableCell<Contact, Object>() {
            private final Button btnModifier = creerBoutonAction(FontAwesomeSolid.PENCIL_ALT, "edit-button", "Modifier");
            private final Button btnSupprimer = creerBoutonAction(FontAwesomeSolid.TRASH_ALT, "delete-button", "Supprimer");
            private final HBox container = new HBox(6, btnModifier, btnSupprimer);

            {
                container.setAlignment(Pos.CENTER);
                btnModifier.setOnAction(event -> {
                    Contact c = getTableRow() != null ? getTableRow().getItem() : null;
                    if (c != null) ouvrirFenetreModification(c);
                });
                btnSupprimer.setOnAction(event -> {
                    Contact c = getTableRow() != null ? getTableRow().getItem() : null;
                    if (c != null) supprimerContact(c);
                });
            }

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    boolean admin = utilisateurCourant != null && utilisateurCourant.getRole() == Role.ADMIN;
                    setGraphic(admin ? container : null);
                }
                setText(null);
            }
        };
    }

    private Button creerBoutonAction(FontAwesomeSolid icone, String styleClass, String tooltip) {
        FontIcon icon = new FontIcon(icone);
        icon.setIconSize(13);
        Button b = new Button();
        b.setGraphic(icon);
        b.setTooltip(new Tooltip(tooltip));
        b.getStyleClass().addAll("action-button", styleClass);
        return b;
    }

    // =========================================================================
    // 8. CHARGEMENT DES CONTACTS ET FILTRES EN CASCADE
    // =========================================================================
    private void chargerDonneesDepuisBD() {
        try {
            List<Contact> contactsFromDb = contactService.listerContacts();
            if (contactsFromDb != null) {
                contactsFromDb.removeIf(c -> {
                    if (c.getLocaliteId() == null) return false;
                    return trouverIdParType(c.getLocaliteId(), "LIMITROPHE") != null;
                });
            }
            listeCompleteContacts.setAll(contactsFromDb != null ? contactsFromDb : List.of());
        } catch (ServiceException e) {
            FxUtil.showError("Erreur de chargement", e.getMessage());
        }
    }

    private void configurerFiltresCascades() {
        cbFiltreMenu.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newMenu) -> {
            cbFiltreFonction.getItems().clear();
            try {
                List<Fonction> toutesFonc = fonctionDAO.listerToutes();
                toutesFonc.removeIf(f -> f.getCategorieCode() != null && f.getCategorieCode().equals("ANN"));

                if (newMenu == null || newMenu.equals("Tous les menus")) {
                    cbFiltreFonction.getItems().addAll(toutesFonc);
                } else if (newMenu.equalsIgnoreCase("Personnes suspectes")) {
                    toutesFonc.removeIf(f -> f.getCategorieCode() == null || !f.getCategorieCode().equals("SUSP"));
                    cbFiltreFonction.getItems().addAll(toutesFonc);
                } else {
                    toutesFonc.removeIf(f -> {
                        if (f.getCategorieId() == null) return true;
                        String libelleCat = categorieIdToLibelleMap.get(f.getCategorieId());
                        return libelleCat == null || !libelleCat.equalsIgnoreCase(newMenu);
                    });
                    cbFiltreFonction.getItems().addAll(toutesFonc);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            cbFiltreFonction.setValue(null);
            appliquerFiltres();
        });

        cbFiltreDistrict.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newDistrict) -> {
            cbFiltreCommune.getItems().clear();
            cbFiltreFokotany.getItems().clear();
            cbFiltreCommune.setDisable(true);
            cbFiltreFokotany.setDisable(true);

            if (newDistrict != null) {
                List<Localite> communes = localiteDAO.listerParParent(newDistrict.getId());
                communes.removeIf(c -> c == null || c.getNom() == null || c.getNom().trim().isEmpty());
                if (!communes.isEmpty()) {
                    cbFiltreCommune.setItems(FXCollections.observableArrayList(communes));
                    cbFiltreCommune.setDisable(false);
                }
            }
            cbFiltreCommune.setValue(null);
            cbFiltreFokotany.setValue(null);
            appliquerFiltres();
        });

        cbFiltreCommune.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newCommune) -> {
            cbFiltreFokotany.getItems().clear();
            cbFiltreFokotany.setDisable(true);

            if (newCommune != null) {
                List<Localite> fokotany = localiteDAO.listerParParent(newCommune.getId());
                fokotany.removeIf(f -> f == null || f.getNom() == null || f.getNom().trim().isEmpty());
                if (!fokotany.isEmpty()) {
                    cbFiltreFokotany.setItems(FXCollections.observableArrayList(fokotany));
                    adapterFiltreFokotanySelonFonction(cbFiltreFonction.getValue());
                }
            }
            cbFiltreFokotany.setValue(null);
            appliquerFiltres();
        });

        cbFiltreFonction.valueProperty().addListener((obs, oldVal, fonctionSel) -> {
            adapterFiltreFokotanySelonFonction(fonctionSel);
            appliquerFiltres();
        });

        cbFiltreFokotany.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
        txtRecherche.textProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    private void adapterFiltreFokotanySelonFonction(Fonction fonction) {
        if (fonction == null || fonction.getNiveauCible() == null) {
            if (cbFiltreCommune.getValue() != null) {
                cbFiltreFokotany.setDisable(false);
                cbFiltreFokotany.setPromptText("Fokotany");
            }
            return;
        }

        String niveau = fonction.getNiveauCible().toUpperCase();
        if (niveau.equals("COMMUNE") || niveau.equals("DISTRICT") || niveau.equals("REGION")) {
            cbFiltreFokotany.getSelectionModel().clearSelection();
            cbFiltreFokotany.setDisable(true);
            cbFiltreFokotany.setPromptText("— (Non applicable)");
        } else {
            if (cbFiltreCommune.getValue() != null) {
                cbFiltreFokotany.setDisable(false);
                cbFiltreFokotany.setPromptText("Fokotany");
            }
        }
    }

    private void appliquerFiltres() {
        String texteRecherche = txtRecherche.getText() != null ? txtRecherche.getText().toLowerCase().trim() : "";
        String menuSel = cbFiltreMenu.getValue();
        Fonction fonctionSel = cbFiltreFonction.getValue();
        Localite districtSel = cbFiltreDistrict.getValue();
        Localite communeSel = cbFiltreCommune.getValue();
        Localite fokotanySel = cbFiltreFokotany.getValue();

        listeFiltree.setPredicate(contact -> {
            boolean correspondMenu = true;
            if (menuSel != null && !menuSel.equals("Tous les menus")) {
                if (menuSel.equalsIgnoreCase("Personnes suspectes")) {
                    boolean estSuspectMotif = contact.getMotif() != null && !contact.getMotif().trim().isEmpty();
                    boolean estSuspectFonction = contact.getFonctionId() != null
                            && fonctionIdToLibelleMap.getOrDefault(contact.getFonctionId(), "").toLowerCase().contains("suspect");
                    correspondMenu = estSuspectMotif || estSuspectFonction;
                } else {
                    Long catId = null;
                    if (contact.getFonctionId() != null) {
                        catId = fonctionIdToCategorieIdMap.get(contact.getFonctionId());
                    }
                    if (catId == null && contact.getOrganisationId() != null) {
                        catId = orgToCategorieIdMap.get(contact.getOrganisationId());
                    }
                    String libCat = catId != null ? categorieIdToLibelleMap.get(catId) : null;
                    correspondMenu = libCat != null && libCat.equalsIgnoreCase(menuSel);
                }
            }

            boolean correspondFonction = true;
            if (fonctionSel != null) {
                correspondFonction = contact.getFonctionId() != null && contact.getFonctionId().equals(fonctionSel.getId());
            }

            Long dId = trouverIdParType(contact.getLocaliteId(), "DISTRICT");
            Long cId = trouverIdParType(contact.getLocaliteId(), "COMMUNE");
            Long fId = trouverIdParType(contact.getLocaliteId(), "FOKOTANY");
            String dName = dId != null ? localiteIdToNomMap.getOrDefault(dId, "") : "";
            String cName = cId != null ? localiteIdToNomMap.getOrDefault(cId, "") : "";
            String fName = fId != null ? localiteIdToNomMap.getOrDefault(fId, "") : "";

            boolean correspondDistrict = districtSel == null || (dId != null && dId.equals(districtSel.getId()));
            boolean correspondCommune = communeSel == null || (cId != null && cId.equals(communeSel.getId()));

            boolean correspondFokotany = true;
            if (fokotanySel != null && !cbFiltreFokotany.isDisabled()) {
                correspondFokotany = fId != null && fId.equals(fokotanySel.getId());
            }

            boolean correspondTexte = true;
            if (!texteRecherche.isEmpty()) {
                String nomComplet = ((contact.getNom() != null ? contact.getNom() : "") + " "
                        + (contact.getPrenom() != null ? contact.getPrenom() : "")).toLowerCase();
                String tel1 = contact.getTelephone1() != null ? contact.getTelephone1().toLowerCase() : "";
                String tel2 = contact.getTelephone2() != null ? contact.getTelephone2().toLowerCase() : "";
                String email = contact.getEmail() != null ? contact.getEmail().toLowerCase() : "";
                String obs = contact.getObservation() != null ? contact.getObservation().toLowerCase() : "";
                String motif = contact.getMotif() != null ? contact.getMotif().toLowerCase() : "";
                String fonctionTxt = contact.getFonctionId() != null
                        ? fonctionIdToLibelleMap.getOrDefault(contact.getFonctionId(), "").toLowerCase() : "";

                correspondTexte = nomComplet.contains(texteRecherche)
                        || tel1.contains(texteRecherche)
                        || tel2.contains(texteRecherche)
                        || email.contains(texteRecherche)
                        || obs.contains(texteRecherche)
                        || motif.contains(texteRecherche)
                        || fonctionTxt.contains(texteRecherche)
                        || dName.toLowerCase().contains(texteRecherche)
                        || cName.toLowerCase().contains(texteRecherche)
                        || fName.toLowerCase().contains(texteRecherche);
            }

            return correspondMenu && correspondFonction && correspondDistrict
                    && correspondCommune && correspondFokotany && correspondTexte;
        });
    }

    public void selectionnerFiltreMenu(String nomMenu) {
        if (cbFiltreMenu.getItems().contains(nomMenu)) {
            cbFiltreMenu.setValue(nomMenu);
        }
    }

    @Override
    public void initialiserDroits(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
        boolean isAdmin = utilisateur != null && utilisateur.getRole() == Role.ADMIN;

        if (btnAjouterContact != null) {
            btnAjouterContact.setVisible(isAdmin);
            btnAjouterContact.setManaged(isAdmin);
        }
        if (colActions != null) {
            colActions.setVisible(isAdmin);
        }

        chargerChoixMenu(isAdmin);
        tableViewContacts.refresh();
    }

    private void chargerChoixMenu(boolean isAdmin) {
        cbFiltreMenu.getItems().clear();
        cbFiltreMenu.getItems().add("Tous les menus");
        cbFiltreMenu.getItems().addAll(
                "Administration",
                "KMD (Komity Dina)",
                "Maires",
                "Gendarmerie",
                "Police",
                "Militaires"
        );

        if (isAdmin) {
            cbFiltreMenu.getItems().add("Personnes suspectes");
        }

        cbFiltreMenu.setValue("Tous les menus");
    }

    // =========================================================================
    // 9. EXPORTATION EXCEL COMPLÈTE (14 COLONNES)
    // =========================================================================
    @FXML
    private void exporterExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Excel (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName("Annuaire_Contacts_Raa.xlsx");
        File file = fileChooser.showSaveDialog(tableViewContacts.getScene().getWindow());

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
                Sheet sheet = workbook.createSheet("Contacts");
                sheet.setAutobreaks(true);

                // Styles de base
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                headerStyle.setFont(headerFont);

                CellStyle titleStyle = workbook.createCellStyle();
                titleStyle.setAlignment(HorizontalAlignment.CENTER);
                titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 14);
                titleStyle.setFont(titleFont);

                CellStyle dataStyle = workbook.createCellStyle();
                dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                // Titre
                Row titleRow = sheet.createRow(0);
                titleRow.createCell(0).setCellValue("ANNUAIRE DES CONTACTS - RÉGION ATSIMO-ANDREFANA");
                titleRow.getCell(0).setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));

                // En-têtes (14 colonnes)
                Row headerRow = sheet.createRow(2);
                String[] colonnes = {
                        "N°", "Code", "Nom complet", "Fonction",
                        "District", "Commune", "Type Com", "Cat. Com", "Fokotany",
                        "Village", "Téléphone 1", "E-mail", "Observation", "Motif"
                };

                for (int i = 0; i < colonnes.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(colonnes[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Remplissage des données
                ObservableList<Contact> items = tableViewContacts.getItems();
                int rowIndex = 3;
                int numero = 1;

                for (Contact c : items) {
                    Row row = sheet.createRow(rowIndex++);

                    String codeDist = trouverCodeDistrict(c.getLocaliteId());
                    String nomComplet = ((c.getNom() != null ? c.getNom() : "") + " " + (c.getPrenom() != null ? c.getPrenom() : "")).trim();
                    String fonctionStr = c.getFonctionId() != null ? fonctionIdToLibelleMap.getOrDefault(c.getFonctionId(), "") : "";
                    String districtStr = trouverNomDistrictParent(c.getLocaliteId());
                    String communeStr = trouverNomParType(c.getLocaliteId(), "COMMUNE");
                    String typeComStr = trouverTypeCom(c.getLocaliteId());
                    String catComStr = trouverCategorieCom(c.getLocaliteId());

                    Long fId = trouverIdParType(c.getLocaliteId(), "FOKOTANY");
                    String fokotanyStr = fId != null ? localiteIdToNomMap.getOrDefault(fId, "—") : "—";

                    String villageStr = "";
                    if (c.getLocaliteId() != null && "VILLAGE".equalsIgnoreCase(localiteIdToTypeMap.get(c.getLocaliteId()))) {
                        villageStr = localiteIdToNomMap.getOrDefault(c.getLocaliteId(), "");
                    }
                    if (villageStr.trim().isEmpty()) {
                        villageStr = "—";
                    }

                    String tel1 = c.getTelephone1() != null ? c.getTelephone1() : "";
                    String email = c.getEmail() != null && !c.getEmail().trim().isEmpty() ? c.getEmail().trim() : "—";
                    String observation = c.getObservation() != null && !c.getObservation().trim().isEmpty() ? c.getObservation().trim() : "—";
                    String motif = c.getMotif() != null && !c.getMotif().trim().isEmpty() ? c.getMotif().trim() : "—";

                    Object[] valeurs = {
                            numero++, codeDist, nomComplet, fonctionStr,
                            districtStr, communeStr, typeComStr.isEmpty() ? "—" : typeComStr, catComStr.isEmpty() ? "—" : catComStr, fokotanyStr,
                            villageStr, tel1, email, observation, motif
                    };

                    for (int i = 0; i < valeurs.length; i++) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(valeurs[i].toString());
                        cell.setCellStyle(dataStyle);
                    }
                }

                // Ajustement auto des largeurs
                int[] widths = {6, 12, 22, 18, 14, 14, 10, 10, 14, 14, 14, 20, 22, 18};
                for (int i = 0; i < widths.length; i++) {
                    sheet.setColumnWidth(i, widths[i] * 256);
                }

                workbook.write(fos);
                FxUtil.showSuccess("Exportation réussie", "Le fichier Excel a été généré avec succès.");

            } catch (Exception e) {
                e.printStackTrace();
                FxUtil.showError("Erreur d'exportation", "Impossible d'exporter le fichier Excel :\n" + e.getMessage());
            }
        }
    }

    // =========================================================================
    // 10. EXPORTATION PDF COMPLÈTE (14 COLONNES EN PAYSAGE)
    // =========================================================================
    @FXML
    private void exporterPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));
        fileChooser.setInitialFileName("Annuaire_Contacts_Raa.pdf");
        File file = fileChooser.showSaveDialog(tableViewContacts.getScene().getWindow());

        if (file != null) {
            try {
                com.itextpdf.kernel.colors.Color navy =
                        new com.itextpdf.kernel.colors.DeviceRgb(23, 37, 84);
                com.itextpdf.kernel.colors.Color borderColor =
                        new com.itextpdf.kernel.colors.DeviceRgb(203, 213, 225);
                com.itextpdf.kernel.colors.Color alternateRowColor =
                        new com.itextpdf.kernel.colors.DeviceRgb(239, 246, 255);
                float[] largeurs = {
                        0.6f, 1.0f, 2.0f, 1.8f,
                        1.3f, 1.3f, 0.9f, 0.9f, 1.3f,
                        1.3f, 1.3f, 1.8f, 2.0f, 1.6f
                };
                String[] entetes = {
                        "N°", "Code", "Nom", "Fonction",
                        "District", "Commune", "Type", "Cat.", "Fokotany",
                        "Village", "Téléphone", "E-mail", "Observation", "Motif"
                };

                com.itextpdf.kernel.pdf.PdfWriter writer =
                        new com.itextpdf.kernel.pdf.PdfWriter(file);
                com.itextpdf.kernel.pdf.PdfDocument pdf =
                        new com.itextpdf.kernel.pdf.PdfDocument(writer);
                com.itextpdf.layout.Document document =
                        new com.itextpdf.layout.Document(
                                pdf, com.itextpdf.kernel.geom.PageSize.A3.rotate());
                document.setMargins(24, 20, 24, 20);

                com.itextpdf.kernel.font.PdfFont regularFont =
                        com.itextpdf.kernel.font.PdfFontFactory.createFont(
                                com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
                com.itextpdf.kernel.font.PdfFont boldFont =
                        com.itextpdf.kernel.font.PdfFontFactory.createFont(
                                com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);

                document.add(new com.itextpdf.layout.element.Paragraph(
                        "ANNUAIRE DES CONTACTS - RÉGION ATSIMO-ANDREFANA")
                        .setFont(boldFont)
                        .setFontSize(15)
                        .setTextAlignment(
                                com.itextpdf.layout.properties.TextAlignment.CENTER));

                com.itextpdf.layout.element.Table table =
                        new com.itextpdf.layout.element.Table(largeurs)
                                .useAllAvailableWidth();
                for (String entete : entetes) {
                    table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                            .add(new com.itextpdf.layout.element.Paragraph(entete)
                                    .setFont(boldFont).setFontSize(7))
                            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                            .setBackgroundColor(navy)
                            .setTextAlignment(
                                    com.itextpdf.layout.properties.TextAlignment.CENTER)
                            .setPadding(5));
                }

                ObservableList<Contact> items = tableViewContacts.getItems();
                int numero = 1;
                int rowNumber = 0;

                for (Contact c : items) {
                    String codeDist = trouverCodeDistrict(c.getLocaliteId());
                    String nomComplet = ((c.getNom() != null ? c.getNom() : "") + " " + (c.getPrenom() != null ? c.getPrenom() : "")).trim();
                    String fonctionStr = c.getFonctionId() != null ? fonctionIdToLibelleMap.getOrDefault(c.getFonctionId(), "") : "";
                    String districtStr = trouverNomDistrictParent(c.getLocaliteId());
                    String communeStr = trouverNomParType(c.getLocaliteId(), "COMMUNE");
                    String typeComStr = trouverTypeCom(c.getLocaliteId());
                    String catComStr = trouverCategorieCom(c.getLocaliteId());

                    Long fId = trouverIdParType(c.getLocaliteId(), "FOKOTANY");
                    String fokotanyStr = fId != null ? localiteIdToNomMap.getOrDefault(fId, "—") : "—";

                    String villageStr = "";
                    if (c.getLocaliteId() != null && "VILLAGE".equalsIgnoreCase(localiteIdToTypeMap.get(c.getLocaliteId()))) {
                        villageStr = localiteIdToNomMap.getOrDefault(c.getLocaliteId(), "");
                    }
                    if (villageStr.trim().isEmpty()) {
                        villageStr = "—";
                    }

                    String tel1 = c.getTelephone1() != null ? c.getTelephone1() : "";
                    String email = c.getEmail() != null && !c.getEmail().trim().isEmpty() ? c.getEmail().trim() : "—";
                    String observation = c.getObservation() != null && !c.getObservation().trim().isEmpty() ? c.getObservation().trim() : "—";
                    String motif = c.getMotif() != null && !c.getMotif().trim().isEmpty() ? c.getMotif().trim() : "—";

                    String[] valeurs = {
                            String.valueOf(numero++), codeDist, nomComplet, fonctionStr,
                            districtStr, communeStr, typeComStr.isEmpty() ? "—" : typeComStr, catComStr.isEmpty() ? "—" : catComStr, fokotanyStr,
                            villageStr, tel1, email, observation, motif
                    };

                    for (String val : valeurs) {
                        com.itextpdf.layout.element.Cell cell =
                                new com.itextpdf.layout.element.Cell()
                                        .add(new com.itextpdf.layout.element.Paragraph(val)
                                                .setFont(regularFont)
                                                .setFontSize(6));
                        cell.setVerticalAlignment(
                                com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);
                        cell.setPadding(4);
                        cell.setBorder(new com.itextpdf.layout.borders.SolidBorder(
                                borderColor, 0.5f));
                        if (rowNumber % 2 == 1) {
                            cell.setBackgroundColor(alternateRowColor);
                        }
                        table.addCell(cell);
                    }
                    rowNumber++;
                }

                document.add(table);
                document.close();
                FxUtil.showSuccess("Exportation réussie", "Le fichier PDF a été généré avec succès.");

            } catch (Exception e) {
                e.printStackTrace();
                FxUtil.showError("Erreur d'exportation", "Impossible d'exporter le fichier PDF :\n" + e.getMessage());
            }
        }
    }
}