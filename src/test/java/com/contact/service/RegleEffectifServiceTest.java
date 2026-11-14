package com.contact.service;

import com.contact.dao.UtilisateurDAO;
import com.contact.model.RegleEffectif;
import com.contact.model.Role;
import com.contact.model.TypeLocalite;
import com.contact.model.Utilisateur;
import com.contact.security.Session;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegleEffectifServiceTest {

    private static RegleEffectifService regleEffectifService;
    private static AuthService authService;
    private static UtilisateurDAO utilisateurDAO;

    private static final String ADMIN_LOGIN = "admin_regle";
    private static final String ADMIN_PASS = "Admin123!";
    private static final String LECTEUR_LOGIN = "lecteur_regle";
    private static final String LECTEUR_PASS = "Lecteur123!";

    @BeforeAll
    static void initialiser() {
        regleEffectifService = new RegleEffectifService();
        authService = new AuthService();
        utilisateurDAO = new UtilisateurDAO();

        // 1. Créer un ADMIN de test s'il n'existe pas
        if (utilisateurDAO.rechercherParLogin(ADMIN_LOGIN) == null) {
            Utilisateur admin = new Utilisateur();
            admin.setLogin(ADMIN_LOGIN);
            admin.setNomComplet("Admin Règle");
            admin.setMotDePasseHash(BCrypt.hashpw(ADMIN_PASS, BCrypt.gensalt()));
            admin.setRole(Role.ADMIN);
            admin.setActif(true);
            utilisateurDAO.inserer(admin);
        }

        // 2. Créer un LECTEUR de test s'il n'existe pas
        if (utilisateurDAO.rechercherParLogin(LECTEUR_LOGIN) == null) {
            Utilisateur lecteur = new Utilisateur();
            lecteur.setLogin(LECTEUR_LOGIN);
            lecteur.setNomComplet("Lecteur Règle");
            lecteur.setMotDePasseHash(BCrypt.hashpw(LECTEUR_PASS, BCrypt.gensalt()));
            lecteur.setRole(Role.LECTEUR);
            lecteur.setActif(true);
            utilisateurDAO.inserer(lecteur);
        }
    }

    @AfterEach
    void nettoyerSession() {
        Session.getInstance().fermerSession();
    }

    @Test
    @Order(1)
    @DisplayName("1. Refus de création d'une règle d'effectif par un LECTEUR")
    void testCreerRegleRefusePourLecteur() {
        // Connexion en tant que LECTEUR
        boolean connecte = authService.connecter(LECTEUR_LOGIN, LECTEUR_PASS);
        assertTrue(connecte);
        assertFalse(Session.getInstance().estAdmin());

        RegleEffectif regle = new RegleEffectif();
        regle.setTypeLocalite(TypeLocalite.COMMUNE);
        regle.setEffectifPrevu(5);

        boolean resultat = regleEffectifService.creerRegle(regle);
        assertFalse(resultat, "Un LECTEUR ne doit pas pouvoir créer une règle d'effectif");
    }

    @Test
    @Order(2)
    @DisplayName("2. Création réussie d'une règle d'effectif par un ADMIN")
    void testCreerRegleReussiParAdmin() {
        // Connexion en tant qu'ADMIN
        boolean connecte = authService.connecter(ADMIN_LOGIN, ADMIN_PASS);
        assertTrue(connecte);
        assertTrue(Session.getInstance().estAdmin());

        RegleEffectif regle = new RegleEffectif();
        regle.setFonctionId(1L);
        regle.setTypeLocalite(TypeLocalite.COMMUNE);
        regle.setEffectifPrevu(10);

        boolean resultat = regleEffectifService.creerRegle(regle);
        assertTrue(resultat, "Un ADMIN doit pouvoir créer une règle d'effectif");
    }

    @Test
    @Order(3)
    @DisplayName("3. Modification et suppression d'une règle par un ADMIN")
    void testModifierEtSupprimerRegleParAdmin() {
        // Connexion en tant qu'ADMIN
        authService.connecter(ADMIN_LOGIN, ADMIN_PASS);
        assertTrue(Session.getInstance().estAdmin());

        // Récupérer la liste pour trouver la règle créée
        List<RegleEffectif> regles = regleEffectifService.listerToutes();
        assertFalse(regles.isEmpty());

        RegleEffectif derniereRegle = regles.get(regles.size() - 1);

        // Modification
        derniereRegle.setEffectifPrevu(15);
        boolean modifie = regleEffectifService.modifierRegle(derniereRegle);
        assertTrue(modifie, "L'ADMIN doit pouvoir modifier la règle d'effectif");

        // Suppression
        boolean supprime = regleEffectifService.supprimerRegle(derniereRegle.getId());
        assertTrue(supprime, "L'ADMIN doit pouvoir supprimer la règle d'effectif");
    }
}