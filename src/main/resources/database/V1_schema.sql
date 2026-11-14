-- Extensions nécessaires pour la recherche textuelle rapide et sans accents
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Table des localités (Région, Districts, Communes, Fokotany, Régions limitrophes)
CREATE TABLE IF NOT EXISTS localite (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(150) NOT NULL,
    type_localite   VARCHAR(25) NOT NULL CHECK (type_localite IN ('REGION','DISTRICT','COMMUNE','FOKOTANY','LIMITROPHE')),
    parent_id       BIGINT REFERENCES localite(id) ON DELETE CASCADE,
    code_source     VARCHAR(50),
    actif           BOOLEAN NOT NULL DEFAULT TRUE
    );

-- Table des catégories (correspondant aux menus et aux niveaux)
CREATE TABLE IF NOT EXISTS categorie (
    id      BIGSERIAL PRIMARY KEY,
    code    VARCHAR(50) NOT NULL UNIQUE,
    libelle VARCHAR(100) NOT NULL,
    sensible BOOLEAN NOT NULL DEFAULT FALSE
    );

-- Table des fonctions
CREATE TABLE IF NOT EXISTS fonction (
    id           BIGSERIAL PRIMARY KEY,
    libelle      VARCHAR(120) NOT NULL,
    categorie_id BIGINT NOT NULL REFERENCES categorie(id),
    niveau_cible VARCHAR(25) NOT NULL CHECK (niveau_cible IN ('REGION','DISTRICT','COMMUNE','FOKOTANY','AUTRE')),
    actif        BOOLEAN NOT NULL DEFAULT TRUE
    );

-- Table des organisations (Brigades, Compagnies, Postes, Organismes)
CREATE TABLE IF NOT EXISTS organisation (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(150) NOT NULL,
    categorie_id    BIGINT NOT NULL REFERENCES categorie(id),
    type_structure  VARCHAR(50),
    localite_id     BIGINT NOT NULL REFERENCES localite(id),
    actif           BOOLEAN NOT NULL DEFAULT TRUE
    );

-- Table principale des contacts
CREATE TABLE IF NOT EXISTS contact (
    id                BIGSERIAL PRIMARY KEY,
    nom               VARCHAR(120) NOT NULL,
    prenom            VARCHAR(120),
    fonction_id       BIGINT NOT NULL REFERENCES fonction(id),
    organisation_id   BIGINT REFERENCES organisation(id),
    localite_id       BIGINT NOT NULL REFERENCES localite(id),
    telephone_1       VARCHAR(30),
    telephone_2       VARCHAR(30),
    email             VARCHAR(160),
    observation       TEXT,
    motif             TEXT, -- Réservé à l'ADMIN pour les personnes suspectes
    actif             BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Table spécifique pour les maires
CREATE TABLE IF NOT EXISTS maire (
    id               BIGSERIAL PRIMARY KEY,
    contact_id       BIGINT NOT NULL REFERENCES contact(id) ON DELETE CASCADE,
    code_commune     VARCHAR(50),
    type_com         VARCHAR(30), -- urbaine ou rurale
    categorie_com    VARCHAR(30), -- ex: CU 1, CR 2
    genre            VARCHAR(15),
    statut_election  VARCHAR(50), -- nouveau élu ou réélu
    observation      TEXT
    );

-- Table spécifique pour les membres KMD
CREATE TABLE IF NOT EXISTS kmd_membre (
    id            BIGSERIAL PRIMARY KEY,
    contact_id    BIGINT NOT NULL REFERENCES contact(id) ON DELETE CASCADE,
    niveau_kmd    VARCHAR(30),
    role_kmd      VARCHAR(60),
    numero_ordre  INTEGER
    );

-- Table des centres de vote (remplace les bureaux de vote)
CREATE TABLE IF NOT EXISTS centre_vote (
    id               BIGSERIAL PRIMARY KEY,
    nom              VARCHAR(180) NOT NULL,
    commune_id       BIGINT NOT NULL REFERENCES localite(id),
    fokotany_id      BIGINT REFERENCES localite(id), -- facultatif
    nombre_bureaux   SMALLINT CHECK (nombre_bureaux > 0),
    nombre_electeurs INTEGER CHECK (nombre_electeurs >= 0),
    observation      TEXT,
    actif            BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (commune_id, nom)
    );

-- Règles d'effectif pour les alertes (ex: 4 vigiles par fokotany)
CREATE TABLE IF NOT EXISTS regle_effectif (
    id             BIGSERIAL PRIMARY KEY,
    fonction_id    BIGINT NOT NULL REFERENCES fonction(id),
    type_localite  VARCHAR(25) NOT NULL CHECK (type_localite IN ('REGION','DISTRICT','COMMUNE','FOKOTANY')),
    effectif_prevu SMALLINT NOT NULL CHECK (effectif_prevu > 0),
    UNIQUE (fonction_id, type_localite)
    );

-- Table des utilisateurs (ADMIN et LECTEUR)
CREATE TABLE IF NOT EXISTS utilisateur (
    id                BIGSERIAL PRIMARY KEY,
    login             VARCHAR(50) NOT NULL UNIQUE,
    nom_complet       VARCHAR(120),
    mot_de_passe_hash VARCHAR(100) NOT NULL,
    role              VARCHAR(10) NOT NULL DEFAULT 'LECTEUR' CHECK (role IN ('LECTEUR','ADMIN')),
    actif             BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Journal d'activité (Audit)
CREATE TABLE IF NOT EXISTS journal (
    id             BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateur(id),
    action         VARCHAR(20) NOT NULL CHECK (action IN ('CONNEXION','CONSULTATION','CREATION','MODIFICATION','DESACTIVATION','SUPPRESSION')),
    table_cible    VARCHAR(40),
    id_cible       BIGINT,
    details        TEXT,
    date_action    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Index pour optimiser les performances de recherche
CREATE INDEX IF NOT EXISTS idx_localite_parent  ON localite(parent_id);
CREATE INDEX IF NOT EXISTS idx_contact_localite ON contact(localite_id);
CREATE INDEX IF NOT EXISTS idx_contact_fonction ON contact(fonction_id);
CREATE INDEX IF NOT EXISTS idx_contact_nom_trgm ON contact USING gin (lower(nom) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_centre_commune   ON centre_vote(commune_id);
CREATE INDEX IF NOT EXISTS idx_journal_date     ON journal(date_action);