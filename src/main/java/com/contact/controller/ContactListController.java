package com.contact.controller;

import com.contact.exception.ServiceException;
import com.contact.model.Contact;
import com.contact.model.Role;
import com.contact.model.Fonction;
import com.contact.model.Localite;
import com.contact.dao.LocaliteDAO;
import com.contact.dao.FonctionDAO;
import com.contact.model.Categorie;
import com.contact.model.Organisation;
import com.contact.dao.CategorieDAO;
import com.contact.dao.OrganisationDAO;
import com.contact.model.Utilisateur;
import com.contact.utils.FxUtil;
import com.contact.service.ContactService;
import com.contact.utils.ManagedController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
import javafx.scene.control.cell.PropertyValueFactory;

public class ContactListController implements ManagedController {

    //Pour faire le pont entre l'organisme de contact et sa categories
    private java.util.Map<Long, Long> orgToCategorieIdMap = new java.util.HashMap<>();
    private java.util.Map<Long, String> categorieIdToLibelleMap = new java.util.HashMap<>();
    private java.util.Map<Long, String> fonctionIdToLibelleMap = new java.util.HashMap<>();
    // Pour faire le pont entre l'ID de la localité et son nom (ex: 3 -> "Sakaraha")
    private java.util.Map<Long , String> localiteIdToNomMap = new java.util.HashMap<>();
    // Pour stocker la correspondance Nom du District -> ID de la localité
    private java.util.Map<String, Long> districtNameToIdMap = new java.util.HashMap<>();

    @FXML private Button btnAjouterContact;

    // -> On utilise bien ton modèle Contact ici :
    @FXML private TableView<Contact> tableViewContacts;

    // -> Correspondance avec les fx:id de ton fichier FXML contacts_view.fxml
    @FXML private TableColumn<Contact, String> colNom;
    @FXML private TableColumn<Contact, String> colFonction;
    @FXML private TableColumn<Contact, String> colDistrict;
    @FXML private TableColumn<Contact, String> colTelephone;
    @FXML private TableColumn<Contact, String> colObservation;
    @FXML private TableColumn<?, ?> colActions;

    @FXML private ComboBox<String> cbFiltreMenu;
    @FXML private ComboBox<String> cbFiltreDistrict;
    @FXML private TextField txtRecherche;

    private Utilisateur utilisateurCourant;
    private ObservableList<Contact> listeCompleteContacts = FXCollections.observableArrayList();
    private FilteredList<Contact> listeFiltree;

    private final ContactService contactService;
    public ContactListController(){
        this.contactService = new ContactService();
    }

    @FXML
    public void initialize() {
        // 1. Initialiser les choix du ComboBox des menus
        cbFiltreMenu.getItems().addAll(
                "Tous les menus",
                "Administration",
                "KMD (Komity Dina)",
                "Maires",
                "Gendarmerie",
                "Police",
                "Militaires",
                "Annexe (Limitrophe)",
                "Personnes suspectes"
        );
        cbFiltreMenu.setValue("Tous les menus");

        cbFiltreDistrict.getItems().add("Tous les districts");
        cbFiltreDistrict.setValue("Tous les districts");

        // 1. Charger les correspondances Organisations <-> Catégories depuis la BD
        chargerCorrespondancesCategories();

        // 2. Lier les colonnes du TableView aux propriétés de ton modèle Contact
        configurerColonnesTableau();

        // 3. Charger les données (depuis ta base de données)
        chargerDonneesDepuisBD();

        // 4. Configurer le filtrage dynamique combiné (Recherche + Menu)
        configurerFiltres();
    }

    private void chargerCorrespondancesCategories(){
        try{

            //charger les categorie
            CategorieDAO catDAO = new CategorieDAO();
            for(Categorie cat : catDAO.listerCategorie()){
                categorieIdToLibelleMap.put(cat.getId(), cat.getLibelle());
            }

            //charger les organisation
            OrganisationDAO orgDAO = new OrganisationDAO();
            for (Organisation org : orgDAO.listerToutes()){
                if(org.getCategorieId() != null){
                    orgToCategorieIdMap.put(org.getId(), org.getCategorieId());
                }
            }

            //charger les fonction
            FonctionDAO foncDAO = new FonctionDAO();
            for(Fonction fonc : foncDAO.listerToutes()){
                fonctionIdToLibelleMap.put(fonc.getId(), fonc.getLibelle());
            }

            //charger le localite || Charger les districts (Localités de type DISTRICT)
            LocaliteDAO locDAO = new LocaliteDAO();
            cbFiltreDistrict.getItems().clear();
            cbFiltreDistrict.getItems().add("Tous les districts");

            for(Localite loc : locDAO.listerToutes()){
                localiteIdToNomMap.put(loc.getId(), loc.getNom());
                //Si la localité est un district, on lajoute au combobox de des districts
                if(loc.getTypeLocalite() != null && loc.getTypeLocalite().name().equalsIgnoreCase("DISTRICT")){
                    cbFiltreDistrict.getItems().add(loc.getNom());
                    districtNameToIdMap.put(loc.getNom(), loc.getId());
                }
            }

            cbFiltreDistrict.setValue("Tous les districts");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configurerColonnesTableau() {
        // Associe chaque colonne JavaFX aux getters de ta classe Contact.java
        // Note: Pour le nom complet, on combine le nom et le prénom si tu veux, ou juste getNom()
        colNom.setCellValueFactory(cellData -> {
            Contact c = cellData.getValue();
            String nomComplet = (c.getNom() != null ? c.getNom() : "") + " " + (c.getPrenom() != null ? c.getPrenom() : "");
            return new javafx.beans.property.SimpleStringProperty(nomComplet.trim());
        });


        // on va affichier le libelle correspond a l'id mais pas l'id
        colFonction.setCellValueFactory( cellData ->{
            Contact c = cellData.getValue();
            String libelleFocntion = "";
            if(c.getFonctionId() != null){
                // On va chercher dans notre "dictionnaire" le texte correspondant à l'ID de la fonction
                libelleFocntion = fonctionIdToLibelleMap.getOrDefault(c.getFonctionId(), "Inconnu");
            }
            // On retourne le texte pour qu'il s'affiche proprement dans la colonne du TableView
            return new javafx.beans.property.SimpleStringProperty(libelleFocntion);
        });

        // on va affichier le libelle correspond a l'id mais pas l'id
        colDistrict.setCellValueFactory(cellData ->{
            Contact c = cellData.getValue();
            String nomLocalite = "";
            if(c.getLocaliteId() != null){
                nomLocalite = localiteIdToNomMap.getOrDefault(c.getLocaliteId(), "Inconnu");
            }
            return new javafx.beans.property.SimpleStringProperty(nomLocalite);
        });

        //OBSERVATION/MOTIF : Affichage avec texte en Rouge pour les profils sensible
        colObservation.setCellValueFactory(cellData ->{
            Contact c = cellData.getValue();
            String texte = (c.getMotif() != null && !c.getMotif().isEmpty() ? c.getMotif() : (c.getObservation() != null ? c.getObservation() : ""));

            return new javafx.beans.property.SimpleStringProperty(texte);
        });

        //Personnalisation graphique de la cellule pour forcer la couleur ROUGER VIVE  pour les profil sensible
        colObservation.setCellFactory(column ->new TableCell<Contact ,String>(){
            @Override
            protected void updateItem(String item, boolean empty){
                super.updateItem(item, empty);
                if(empty || item == null || item.isEmpty()){
                    setText(null);
                    setStyle("");
                }else{
                    setText(item);
                    setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                }
            }
        });

        // On affiche telephone1 dans la colonne téléphone
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone1"));
    }

    private void chargerDonneesDepuisBD() {
        try {
            // On vide la liste au cas où
            listeCompleteContacts.clear();

            // On récupère la liste via ton service (qui gère déjà la sécurité et le filtrage admin/lecteur)
            List<Contact> contactsFromDb = contactService.listerContacts();

            if (contactsFromDb != null) {
                listeCompleteContacts.addAll(contactsFromDb);
            }

        } catch (ServiceException e) {
            FxUtil.showError("Erreur de chargement", e.getMessage());
        }

        // Le FilteredList enveloppe ta liste observable pour le filtrage en temps réel
        listeFiltree = new FilteredList<>(listeCompleteContacts, p -> true);
        tableViewContacts.setItems(listeFiltree);
    }

    private void configurerFiltres() {
        // Écouteur sur le champ de recherche et le ComboBox
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());
        cbFiltreMenu.valueProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());
        cbFiltreDistrict.valueProperty().addListener((observable, oldValue, newValue )-> appliquerFiltres());
    }

    private void appliquerFiltres() {
        String texteRecherche = txtRecherche.getText() != null ? txtRecherche.getText().toLowerCase().trim() : "";
        String menuSelectionne = cbFiltreMenu.getValue();
        String districtSelectionne = cbFiltreDistrict.getValue();

        listeFiltree.setPredicate(contact -> {
            //1.Filtre par Menu / Categorie
            boolean correspondMenu = true;
            if (menuSelectionne != null && !menuSelectionne.equals("Tous les menus")) {
                if(contact.getOrganisationId() != null){
                    Long catId = orgToCategorieIdMap.get(contact.getOrganisationId());
                    if(catId != null){
                        String libelleCat = categorieIdToLibelleMap.get(catId);
                        //on compte le libelle de la categorie avec ce qui est selectionné
                        correspondMenu = libelleCat != null && libelleCat.equalsIgnoreCase(menuSelectionne);
                    }else{
                        correspondMenu = false;
                    }
                }else {
                    correspondMenu = false;
                }
            }

            //Filtre par district
            boolean correspondDistrict = true;
            if(districtSelectionne != null && !districtSelectionne.equals("Tous les districts")){
                if(contact.getLocaliteId() != null){
                    //on recuperer le nom de la localité du contact grace a notre map
                    String nomLocaliteContact = localiteIdToNomMap.get(contact.getLocaliteId());
                    correspondDistrict = nomLocaliteContact != null && nomLocaliteContact.equalsIgnoreCase(districtSelectionne);
                }else {
                    correspondDistrict = false;
                }
            }

            // Filtre par Texte de recherche multi-mots sur ton modèle Contact
            boolean correspondTexte = true;
            if (!texteRecherche.isEmpty()) {
                String nomComplet = (contact.getNom() != null ? contact.getNom() : "") + " " + (contact.getPrenom() != null ? contact.getPrenom() : "");
                nomComplet = nomComplet.toLowerCase();

                String tel1 = contact.getTelephone1() != null ? contact.getTelephone1().toLowerCase() : "";
                String tel2 = contact.getTelephone2() != null ? contact.getTelephone2().toLowerCase() : "";
                String email = contact.getEmail() != null ? contact.getEmail().toLowerCase() : "";
                String obs = contact.getObservation() != null ? contact.getObservation().toLowerCase() : "";
                String motif = contact.getMotif() != null ? contact.getMotif().toLowerCase() : "";

                String nomFonction = contact.getFonctionId() != null ? fonctionIdToLibelleMap.getOrDefault(contact.getFonctionId(), "").toLowerCase() : "";
                String nomLocalite = contact.getLocaliteId() != null ? localiteIdToNomMap.getOrDefault(contact.getLocaliteId(), "").toLowerCase() : "";

                boolean matchNom = nomComplet.contains(texteRecherche);
                boolean matchTel = tel1.contains(texteRecherche) || tel2.contains(texteRecherche);
                boolean matchEmail = email.contains(texteRecherche);
                boolean matchObs = obs.contains(texteRecherche) || motif.contains(texteRecherche);
                boolean matchFonction = nomFonction.contains(texteRecherche);
                boolean matchLocalite = nomLocalite.contains(texteRecherche);


                // Si le texte tapé correspond à l'un de ces critères, c'est validé !
                correspondTexte = matchNom || matchTel || matchEmail || matchObs || matchFonction || matchLocalite;
            }

            return correspondMenu && correspondTexte && correspondDistrict;
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
        boolean isAdmin = (utilisateur.getRole() == Role.ADMIN);

        if (btnAjouterContact != null) {
            btnAjouterContact.setVisible(isAdmin);
            btnAjouterContact.setManaged(isAdmin);
        }
        if (colActions != null) {
            colActions.setVisible(isAdmin);
        }
    }

    @FXML
    private void handleAjouter() {
        if (utilisateurCourant.getRole() != Role.ADMIN) {
            FxUtil.showError("Accès refusé", "Seuls les administrateurs peuvent ajouter de nouveaux contacts.");
            return;
        }
        // Logique d'ouverture du formulaire d'ajout
    }
}