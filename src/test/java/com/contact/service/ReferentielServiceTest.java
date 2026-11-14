package com.contact.service;

import com.contact.dao.CategorieDAO;
import com.contact.dao.FonctionDAO;
import com.contact.dao.UtilisateurDAO;
import com.contact.model.Categorie;
import com.contact.model.Fonction;
import com.contact.model.Role;
import com.contact.model.Utilisateur;
import com.contact.security.Session;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReferentielServiceTest {

    private static ReferentielService referentielService;
    private static CategorieDAO categorieDAO;
    private static AuthService authService;

    private static final String ADMIN_LOGIN = "admin_ref";
    private static final String ADMIN_PASS = "Admin123!";

    @BeforeAll
    static void initialiser() {
        referentielService = new ReferentielService();
        categorieDAO = new CategorieDAO();
        authService = new AuthService();

        // 1. S'assurer qu'un compte ADMIN de test existe en base
        UtilisateurDAO utilDAO = new UtilisateurDAO();
        if (utilDAO.rechercherParLogin(ADMIN_LOGIN) == null) {
            Utilisateur admin = new Utilisateur();
            admin.setLogin(ADMIN_LOGIN);
            admin.setNomComplet("Admin Référentiel");
            admin.setMotDePasseHash(BCrypt.hashpw(ADMIN_PASS, BCrypt.gensalt()));
            admin.setRole(Role.ADMIN);
            admin.setActif(true);
            utilDAO.inserer(admin);
        }

        // 2. Insérer une catégorie de test si la table est vide
        if (categorieDAO.listerCategorie().isEmpty()) {
            Categorie cat = new Categorie();
            cat.setCode("CAT_TEST");
            cat.setLibelle("Catégorie Test");
            cat.setSensible(false);
            categorieDAO.inserer(cat);
        }
    }

    @AfterEach
    void nettoyerSession() {
        Session.getInstance().fermerSession();
    }

    @Test
    @Order(1)
    @DisplayName("1. Lister les catégories et les fonctions")
    void testListerReferentiels() {
        var categories = referentielService.listerToutesCategories();
        assertNotNull(categories, "La liste des catégories ne doit pas être nulle");

        var fonctions = referentielService.listerToutesFonctions();
        assertNotNull(fonctions, "La liste des fonctions ne doit pas être nulle");
    }

    @Test
    @Order(2)
    @DisplayName("2. Refus de création d'une fonction sans être ADMIN")
    void testCreationFonctionRefuseeSansAdmin() {
        Fonction f = new Fonction();
        f.setLibelle("Fonction Non Autorisée");
        f.setNiveauCible("COMMUNE");

        boolean cree = referentielService.creerFonction(f);
        assertFalse(cree, "Un utilisateur non connecté ne doit pas pouvoir créer de fonction");
    }

    @Test
    @Order(3)
    @DisplayName("3. Création réussie d'une fonction par un ADMIN")
    void testCreationFonctionParAdmin() {
        // Connexion en tant qu'ADMIN
        boolean connecte = authService.connecter(ADMIN_LOGIN, ADMIN_PASS);
        assertTrue(connecte, "L'admin doit pouvoir se connecter");

        var categories = referentielService.listerToutesCategories();
        assertFalse(categories.isEmpty(), "Il doit y avoir au moins une catégorie");

        Fonction f = new Fonction();
        f.setLibelle("Maire de Test");
        f.setCategorieId(categories.get(0).getId());
        f.setNiveauCible("COMMUNE");
        f.setActif(true);

        boolean cree = referentielService.creerFonction(f);
        assertTrue(cree, "L'ADMIN doit pouvoir créer une nouvelle fonction");
    }
}