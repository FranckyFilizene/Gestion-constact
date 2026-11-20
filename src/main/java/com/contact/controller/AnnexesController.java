package com.contact.controller;

import com.contact.dao.CategorieDAO;
import com.contact.dao.FonctionDAO;
import com.contact.dao.LocaliteDAO;
import com.contact.dao.OrganisationDAO;
import com.contact.exception.ServiceException;
import com.contact.model.*;
import com.contact.service.ContactService;
import com.contact.utils.FxUtil;
import com.contact.utils.ManagedController;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AnnexesController implements ManagedController {

    // =========================================================================
    // 1. DICTIONNAIRES DE CORRESPONDANCE
    // =========================================================================
    private final Map<Long, String> categorieIdToLibelleMap = new HashMap<>();
    private final Map<Long, String> fonctionIdToLibelleMap = new HashMap<>();
    private final Map<Long, Long> fonctionIdToCategorieIdMap = new HashMap<>();

    private final Map<Long, String> localiteIdToNomMap = new HashMap<>();
    private final Map<Long, Long> localiteIdToParentIdMap = new HashMap<>();
    private final Map<Long, String> localiteIdToTypeMap = new HashMap<>();

    // =========================================================================
    // 2. COMPOSANTS FXML
    // =========================================================================
    @FXML private TableView<Contact> tableViewContacts;

    @FXML private TableColumn<Contact, String> colRegion;
    @FXML private TableColumn<Contact, String> colNom;
    @FXML private TableColumn<Contact, String> colFonction;
    @FXML private TableColumn<Contact, String> colDistrict;
    @FXML private TableColumn<Contact, String> colCommune;
    @FXML private TableColumn<Contact, String> colFokotany;
    @FXML private TableColumn<Contact, String> colTelephone;
    @FXML private TableColumn<Contact, String> colMotif;
    @FXML private TableColumn<Contact, Object> colActions;

    @FXML private ComboBox<Localite> cbFiltreRegionLimitrophe;
    @FXML private ComboBox<Localite> cbFiltreDistrict;
    @FXML private ComboBox<Localite> cbFiltreCommune;
    @FXML private ComboBox<Localite> cbFiltreFokotany;

    @FXML private Button btnExporterExcel;
    @FXML private Button btnAjouterContactAnnexe;
    @FXML private TextField txtRecherche;

    // =========================================================================
    // 3. VARIABLES D'ÉTAT & SERVICES
    // =========================================================================
    private Utilisateur utilisateurCourant;
    private final ObservableList<Contact> listeCompleteContacts = FXCollections.observableArrayList();
    private FilteredList<Contact> listeFiltree;
    private final ContactService contactService = new ContactService();
    private final LocaliteDAO localiteDAO = new LocaliteDAO();
    private final FonctionDAO fonctionDAO = new FonctionDAO();

    @FXML
    public void initialize() {
        chargerCorrespondances();
        configurerColonnesTableau();

        listeFiltree = new FilteredList<>(listeCompleteContacts, p -> true);
        SortedList<Contact> listeTriee = new SortedList<>(listeFiltree);
        listeTriee.comparatorProperty().bind(tableViewContacts.comparatorProperty());
        tableViewContacts.setItems(listeTriee);

        chargerDonneesDepuisBD();
        configurerFiltresCascades();
    }

    private void chargerCorrespondances() {
        try {
            for (Categorie cat : new CategorieDAO().listerCategorie()) {
                categorieIdToLibelleMap.put(cat.getId(), cat.getLibelle());
            }

            for (Fonction f : fonctionDAO.listerToutes()) {
                fonctionIdToLibelleMap.put(f.getId(), f.getLibelle());
                fonctionIdToCategorieIdMap.put(f.getId(), f.getCategorieId());
            }

            for (Localite loc : localiteDAO.listerToutes()) {
                localiteIdToNomMap.put(loc.getId(), loc.getNom());
                localiteIdToParentIdMap.put(loc.getId(), loc.getParentId());
                localiteIdToTypeMap.put(loc.getId(), loc.getTypeLocalite() != null ? loc.getTypeLocalite().name() : "");
            }

            // Charger uniquement les régions limitrophes (type LIMITROPHE)
            List<Localite> regionsLimitrophes = localiteDAO.listerToutes();
            regionsLimitrophes.removeIf(l -> !"LIMITROPHE".equalsIgnoreCase(l.getTypeLocalite().name()));
            cbFiltreRegionLimitrophe.setItems(FXCollections.observableArrayList(regionsLimitrophes));

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

    private String trouverNomParType(Long localiteId, String type) {
        if (localiteId == null) return "";
        Long id = trouverIdParType(localiteId, type);
        return id != null ? localiteIdToNomMap.getOrDefault(id, "") : "";
    }

    private void configurerColonnesTableau() {
        colRegion.setCellValueFactory(cd -> new SimpleStringProperty(trouverNomParType(cd.getValue().getLocaliteId(), "LIMITROPHE")));

        colNom.setCellValueFactory(cd -> {
            Contact c = cd.getValue();
            String nomComplet = (c.getNom() != null ? c.getNom() : "") + " " + (c.getPrenom() != null ? c.getPrenom() : "");
            return new SimpleStringProperty(nomComplet.trim());
        });

        colFonction.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getFonctionId() != null ? fonctionIdToLibelleMap.getOrDefault(cd.getValue().getFonctionId(), "") : ""
        ));

        colDistrict.setCellValueFactory(cd -> new SimpleStringProperty(trouverNomParType(cd.getValue().getLocaliteId(), "DISTRICT")));
        colCommune.setCellValueFactory(cd -> new SimpleStringProperty(trouverNomParType(cd.getValue().getLocaliteId(), "COMMUNE")));
        colFokotany.setCellValueFactory(cd -> new SimpleStringProperty(trouverNomParType(cd.getValue().getLocaliteId(), "FOKOTANY")));

        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone1"));

        colMotif.setCellValueFactory(cd -> {
            String motif = cd.getValue().getMotif() != null ? cd.getValue().getMotif().trim() : "";
            return new SimpleStringProperty(motif.isEmpty() ? "—" : motif);
        });

        colActions.setCellValueFactory(new PropertyValueFactory<>("id"));
        colActions.setCellFactory(this::creerCelluleAction);
        colActions.setSortable(false);
    }

    private TableCell<Contact, Object> creerCelluleAction(TableColumn<Contact, Object> column) {
        return new TableCell<>() {
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

    private void chargerDonneesDepuisBD() {
        try {
            List<Contact> contactsFromDb = contactService.listerContacts();
            // On ne garde que les contacts rattachés à une localité de type LIMITROPHE (ou enfants)
            contactsFromDb.removeIf(c -> {
                if (c.getLocaliteId() == null) return true;
                Long regId = trouverIdParType(c.getLocaliteId(), "LIMITROPHE");
                return regId == null; // Si ce n'est pas une annexe limitrophe, on l'exclut
            });
            listeCompleteContacts.setAll(contactsFromDb);
        } catch (ServiceException e) {
            FxUtil.showError("Erreur de chargement", e.getMessage());
        }
    }

    private void configurerFiltresCascades() {
        cbFiltreRegionLimitrophe.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newRegion) -> {
            cbFiltreDistrict.getItems().clear();
            cbFiltreCommune.getItems().clear();
            cbFiltreFokotany.getItems().clear();
            cbFiltreDistrict.setDisable(true);
            cbFiltreCommune.setDisable(true);
            cbFiltreFokotany.setDisable(true);

            if (newRegion != null) {
                List<Localite> districts = localiteDAO.listerParParent(newRegion.getId());
                if (!districts.isEmpty()) {
                    cbFiltreDistrict.setItems(FXCollections.observableArrayList(districts));
                    cbFiltreDistrict.setDisable(false);
                }
            }
            appliquerFiltres();
        });

        cbFiltreDistrict.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newDistrict) -> {
            cbFiltreCommune.getItems().clear();
            cbFiltreFokotany.getItems().clear();
            cbFiltreCommune.setDisable(true);
            cbFiltreFokotany.setDisable(true);

            if (newDistrict != null) {
                List<Localite> communes = localiteDAO.listerParParent(newDistrict.getId());
                if (!communes.isEmpty()) {
                    cbFiltreCommune.setItems(FXCollections.observableArrayList(communes));
                    cbFiltreCommune.setDisable(false);
                }
            }
            appliquerFiltres();
        });

        cbFiltreCommune.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newCommune) -> {
            cbFiltreFokotany.getItems().clear();
            cbFiltreFokotany.setDisable(true);

            if (newCommune != null) {
                List<Localite> fokotany = localiteDAO.listerParParent(newCommune.getId());
                if (!fokotany.isEmpty()) {
                    cbFiltreFokotany.setItems(FXCollections.observableArrayList(fokotany));
                    cbFiltreFokotany.setDisable(false);
                }
            }
            appliquerFiltres();
        });

        cbFiltreFokotany.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
        txtRecherche.textProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    private void appliquerFiltres() {
        String texteRecherche = txtRecherche.getText() != null ? txtRecherche.getText().toLowerCase().trim() : "";
        Localite regionSel = cbFiltreRegionLimitrophe.getValue();
        Localite districtSel = cbFiltreDistrict.getValue();
        Localite communeSel = cbFiltreCommune.getValue();
        Localite fokotanySel = cbFiltreFokotany.getValue();

        listeFiltree.setPredicate(contact -> {
            Long regId = trouverIdParType(contact.getLocaliteId(), "LIMITROPHE");
            Long distId = trouverIdParType(contact.getLocaliteId(), "DISTRICT");
            Long commId = trouverIdParType(contact.getLocaliteId(), "COMMUNE");
            Long fokId = trouverIdParType(contact.getLocaliteId(), "FOKOTANY");

            boolean correspondRegion = regionSel == null || (regId != null && regId.equals(regionSel.getId()));
            boolean correspondDistrict = districtSel == null || (distId != null && distId.equals(districtSel.getId()));
            boolean correspondCommune = communeSel == null || (commId != null && commId.equals(communeSel.getId()));
            boolean correspondFokotany = fokotanySel == null || (fokId != null && fokId.equals(fokotanySel.getId()));

            boolean correspondTexte = true;
            if (!texteRecherche.isEmpty()) {
                String nomComplet = ((contact.getNom() != null ? contact.getNom() : "") + " " + (contact.getPrenom() != null ? contact.getPrenom() : "")).toLowerCase();
                String tel = contact.getTelephone1() != null ? contact.getTelephone1().toLowerCase() : "";
                String fonctionTxt = contact.getFonctionId() != null ? fonctionIdToLibelleMap.getOrDefault(contact.getFonctionId(), "").toLowerCase() : "";

                correspondTexte = nomComplet.contains(texteRecherche) || tel.contains(texteRecherche) || fonctionTxt.contains(texteRecherche);
            }

            return correspondRegion && correspondDistrict && correspondCommune && correspondFokotany && correspondTexte;
        });
    }

    @FXML
    private void ouvrirFenetreNouveauContact() {
        ouvrirFormulaire(null);
    }

    private void ouvrirFenetreModification(Contact contact) {
        ouvrirFormulaire(contact);
    }

    private void ouvrirFormulaire(Contact contact) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/annexe_contact_form.fxml"));
            Parent root = loader.load();

            AnnexeContactFormController formController = loader.getController();
            if (contact != null) {
                formController.initModeModification(contact);
            }

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setOnHidden(event -> chargerDonneesDepuisBD());
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            FxUtil.showError("Erreur", "Impossible d'ouvrir le formulaire d'annexe.");
        }
    }

    private void supprimerContact(Contact contact) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer le contact : " + contact.getNom());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce contact ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    contactService.supprimerContact(contact.getId());
                    chargerDonneesDepuisBD();
                } catch (Exception e) {
                    FxUtil.showError("Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void exporterExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les Annexes en Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Excel (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName("Annexes_Limitrophes.xlsx");

        File file = fileChooser.showSaveDialog(tableViewContacts.getScene().getWindow());
        if (file == null) return;

        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Annexes Limitrophes");

            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] colonnes = {"N°", "Région", "Nom complet", "Fonction", "District", "Commune", "Fokotany", "Téléphone", "Motif"};
            for (int i = 0; i < colonnes.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(colonnes[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Contact c : tableViewContacts.getItems()) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx);
                String nomComplet = (c.getNom() != null ? c.getNom() : "") + " " + (c.getPrenom() != null ? c.getPrenom() : "");

                row.createCell(0).setCellValue(rowIdx);
                row.createCell(1).setCellValue(trouverNomParType(c.getLocaliteId(), "LIMITROPHE"));
                row.createCell(2).setCellValue(nomComplet.trim());
                row.createCell(3).setCellValue(c.getFonctionId() != null ? fonctionIdToLibelleMap.getOrDefault(c.getFonctionId(), "") : "");
                row.createCell(4).setCellValue(trouverNomParType(c.getLocaliteId(), "DISTRICT"));
                row.createCell(5).setCellValue(trouverNomParType(c.getLocaliteId(), "COMMUNE"));
                row.createCell(6).setCellValue(trouverNomParType(c.getLocaliteId(), "FOKOTANY"));
                row.createCell(7).setCellValue(c.getTelephone1() != null ? c.getTelephone1() : "");
                row.createCell(8).setCellValue(c.getMotif() != null ? c.getMotif() : "—");
                rowIdx++;
            }

            for (int i = 0; i < colonnes.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            FxUtil.showSuccess("Succès", "Exportation Excel réussie :\n" + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            FxUtil.showError("Erreur", e.getMessage());
        }
    }

    @Override
    public void initialiserDroits(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
        boolean isAdmin = (utilisateur.getRole() == Role.ADMIN);
        if (btnAjouterContactAnnexe != null) {
            btnAjouterContactAnnexe.setVisible(isAdmin);
            btnAjouterContactAnnexe.setManaged(isAdmin);
        }
        if (colActions != null) {
            colActions.setVisible(isAdmin);
        }
        tableViewContacts.refresh();
    }
}