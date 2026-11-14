package com.contact.service;

import com.contact.dao.LocaliteDAO;
import com.contact.dao.LocaliteSeeder;
import com.contact.dao.UtilisateurDAO;
import com.contact.model.Localite;
import com.contact.model.Role;
import com.contact.model.TypeLocalite;
import com.contact.model.Utilisateur;
import com.contact.security.Session;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LocaliteServiceTest {

    private static LocaliteService localiteService;
    private static LocaliteDAO localiteDAO;
    private static AuthService authService;

    private static final String ADMIN_LOGIN = "admin_localite";
    private static final String ADMIN_PASS = "Admin123!";

    @BeforeAll
    static void initialiser() {
        localiteService = new LocaliteService();
        localiteDAO = new LocaliteDAO();
        authService = new AuthService();

        // 1. Lancer le seeder pour s'assurer que la région et les 9 districts existent
        LocaliteSeeder.seedDatabase();

        // 2. S'assurer qu'un compte ADMIN existe pour tester les autorisations de création
        UtilisateurDAO utilDAO = new UtilisateurDAO();
        if (utilDAO.rechercherParLogin(ADMIN_LOGIN) == null) {
            Utilisateur admin = new Utilisateur();
            admin.setLogin(ADMIN_LOGIN);
            admin.setNomComplet("Admin Localité");
            admin.setMotDePasseHash(BCrypt.hashpw(ADMIN_PASS, BCrypt.gensalt()));
            admin.setRole(Role.ADMIN);
            admin.setActif(true);
            utilDAO.inserer(admin);
        }
    }

    @AfterEach
    void nettoyerSession() {
        Session.getInstance().fermerSession();
    }

    @Test
    @Order(1)
    @DisplayName("1. Vérifier la présence de la région Atsimo Andrefana et des 9 districts")
    void testSeedingEtLectureDistricts() {
        List<Localite> toutes = localiteService.listeToutes();
        assertFalse(toutes.isEmpty(), "La table des localités ne doit pas être vide");

        // Trouver la région
        Localite region = toutes.stream()
                .filter(l -> l.getTypeLocalite() == TypeLocalite.REGION)
                .findFirst()
                .orElse(null);

        assertNotNull(region, "La région Atsimo Andrefana doit exister");
        assertEquals("Atsimo Andrefana", region.getNom());

        // Récupérer les enfants (les districts) via la méthode par parent
        List<Localite> districts = localiteService.listerParParent(region.getId());
        assertEquals(9, districts.size(), "Il doit y avoir exactement 9 districts rattachés à la région");
    }

    @Test
    @Order(2)
    @DisplayName("2. Refus de création d'une localité sans être ADMIN")
    void testCreationLocaliteRefuseeSansAdmin() {
        // Pas de session ouverte -> Utilisateur non connecté
        Localite nouvelleCommune = new Localite();
        nouvelleCommune.setNom("Commune Test");
        nouvelleCommune.setTypeLocalite(TypeLocalite.COMMUNE);
        nouvelleCommune.setParentId(1L);

        boolean resultat = localiteService.creerLocalite(nouvelleCommune);
        assertFalse(resultat, "Un utilisateur non connecté ne doit pas pouvoir créer de localité");
    }

    @Test
    @Order(3)
    @DisplayName("3. Création réussie d'une commune par un ADMIN")
    void testCreationCommuneParAdmin() {
        // 1. Connexion en tant qu'ADMIN
        boolean connecte = authService.connecter(ADMIN_LOGIN, ADMIN_PASS);
        assertTrue(connecte, "L'admin doit pouvoir se connecter");
        assertTrue(Session.getInstance().estAdmin());

        // 2. Récupérer l'ID du premier district (ex: Toliara I) pour y rattacher la commune
        List<Localite> districts = localiteService.listerParType(TypeLocalite.DISTRICT);
        assertFalse(districts.isEmpty());
        Long districtId = districts.get(0).getId();

        // 3. Créer une nouvelle commune
        Localite nouvelleCommune = new Localite();
        nouvelleCommune.setNom("Commune Rurale Test");
        nouvelleCommune.setTypeLocalite(TypeLocalite.COMMUNE);
        nouvelleCommune.setParentId(districtId);
        nouvelleCommune.setCodeSource("TEST_01");

        boolean cree = localiteService.creerLocalite(nouvelleCommune);
        assertTrue(cree, "L'ADMIN doit pouvoir créer une nouvelle commune");
    }
}