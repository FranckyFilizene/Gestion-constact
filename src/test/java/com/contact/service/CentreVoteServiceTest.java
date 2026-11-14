package com.contact.service;

import com.contact.dao.LocaliteDAO;
import com.contact.dao.UtilisateurDAO;
import com.contact.model.CentreVote;
import com.contact.model.Localite;
import com.contact.model.Role;
import com.contact.model.Utilisateur;
import com.contact.security.Session;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CentreVoteServiceTest {

    private static CentreVoteService centreVoteService;
    private static AuthService authService;
    private static UtilisateurDAO utilisateurDAO;

    private static final String ADMIN_LOGIN = "admin_cv";
    private static final String ADMIN_PASS = "Admin123!";
    private static final String LECTEUR_LOGIN = "lecteur_cv";
    private static final String LECTEUR_PASS = "Lecteur123!";

    private static Long communeIdTest;

    @BeforeAll
    static void initialiser() {
        centreVoteService = new CentreVoteService();
        authService = new AuthService();
        utilisateurDAO = new UtilisateurDAO();

        // 1. Créer un ADMIN de test s'il n'existe pas
        if (utilisateurDAO.rechercherParLogin(ADMIN_LOGIN) == null) {
            Utilisateur admin = new Utilisateur();
            admin.setLogin(ADMIN_LOGIN);
            admin.setNomComplet("Admin CentreVote");
            admin.setMotDePasseHash(BCrypt.hashpw(ADMIN_PASS, BCrypt.gensalt()));
            admin.setRole(Role.ADMIN);
            admin.setActif(true);
            utilisateurDAO.inserer(admin);
        }

        // 2. Créer un LECTEUR de test s'il n'existe pas
        if (utilisateurDAO.rechercherParLogin(LECTEUR_LOGIN) == null) {
            Utilisateur lecteur = new Utilisateur();
            lecteur.setLogin(LECTEUR_LOGIN);
            lecteur.setNomComplet("Lecteur CentreVote");
            lecteur.setMotDePasseHash(BCrypt.hashpw(LECTEUR_PASS, BCrypt.gensalt()));
            lecteur.setRole(Role.LECTEUR);
            lecteur.setActif(true);
            utilisateurDAO.inserer(lecteur);
        }

        // 3. Récupérer une localité existante pour servir de commune_id
        LocaliteDAO localiteDAO = new LocaliteDAO();
        List<Localite> localites = localiteDAO.listerToutes();
        if (!localites.isEmpty()) {
            communeIdTest = localites.get(0).getId();
        } else {
            // Fallback si la base est vide (ID par défaut)
            communeIdTest = 1L;
        }
    }

    @AfterEach
    void nettoyerSession() {
        Session.getInstance().fermerSession();
    }

    @Test
    @Order(1)
    @DisplayName("1. Refus de création d'un centre de vote par un LECTEUR")
    void testCreerCentreVoteRefusePourLecteur() {
        // Connexion en tant que LECTEUR
        boolean connecte = authService.connecter(LECTEUR_LOGIN, LECTEUR_PASS);
        assertTrue(connecte);
        assertFalse(Session.getInstance().estAdmin());

        CentreVote cv = new CentreVote();
        cv.setNom("EPP Centre Test");
        cv.setCommuneId(communeIdTest);
        cv.setNombreBureaux(3);
        cv.setNombreElecteur(500);
        cv.setActif(true);

        boolean resultat = centreVoteService.creerCentreVote(cv);
        assertFalse(resultat, "Un LECTEUR ne doit pas pouvoir créer un centre de vote");
    }

    @Test
    @Order(2)
    @DisplayName("2. Création réussie d'un centre de vote par un ADMIN")
    void testCreerCentreVoteReussiByAdmin() {
        // Connexion en tant qu'ADMIN
        boolean connecte = authService.connecter(ADMIN_LOGIN, ADMIN_PASS);
        assertTrue(connecte);
        assertTrue(Session.getInstance().estAdmin());

        CentreVote cv = new CentreVote();
        cv.setNom("EPP Centre Test Admin");
        cv.setCommuneId(communeIdTest);
        cv.setNombreBureaux(5);
        cv.setNombreElecteur(1200);
        cv.setObservation("Centre principal de test");
        cv.setActif(true);

        boolean resultat = centreVoteService.creerCentreVote(cv);
        assertTrue(resultat, "Un ADMIN doit pouvoir créer un centre de vote");
    }
}