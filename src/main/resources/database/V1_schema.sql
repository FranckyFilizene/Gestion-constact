-- =====================================================================
-- Script SQL propre - Base de données : contacts_region (PostgreSQL)
-- Extensions, tables, séquences, contraintes, index et 4 comptes ADMIN
-- =====================================================================

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', 'public', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

-- ---------------------------------------------------------------------
-- 1. Extensions
-- ---------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;

SET default_tablespace = '';
SET default_table_access_method = heap;

-- ---------------------------------------------------------------------
-- 2. Tables et séquences
-- ---------------------------------------------------------------------

CREATE TABLE public.categorie (
                                  id bigint NOT NULL,
                                  code character varying(50) NOT NULL,
                                  libelle character varying(100) NOT NULL,
                                  sensible boolean DEFAULT false NOT NULL
);

CREATE SEQUENCE public.categorie_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.categorie_id_seq OWNED BY public.categorie.id;

CREATE TABLE public.centre_vote (
                                    id bigint NOT NULL,
                                    nom character varying(180) NOT NULL,
                                    commune_id bigint NOT NULL,
                                    fokotany_id bigint,
                                    nombre_bureaux smallint,
                                    nombre_electeurs integer,
                                    observation text,
                                    actif boolean DEFAULT true NOT NULL,
                                    date_creation timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                    CONSTRAINT centre_vote_nombre_bureaux_check CHECK ((nombre_bureaux > 0)),
                                    CONSTRAINT centre_vote_nombre_electeurs_check CHECK ((nombre_electeurs >= 0))
);

CREATE SEQUENCE public.centre_vote_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.centre_vote_id_seq OWNED BY public.centre_vote.id;

CREATE TABLE public.contact (
                                id bigint NOT NULL,
                                nom character varying(120) NOT NULL,
                                prenom character varying(120),
                                fonction_id bigint NOT NULL,
                                organisation_id bigint,
                                localite_id bigint NOT NULL,
                                telephone_1 character varying(30),
                                telephone_2 character varying(30),
                                email character varying(160),
                                observation text,
                                motif text,
                                actif boolean DEFAULT true NOT NULL,
                                date_creation timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                date_modification timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE SEQUENCE public.contact_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.contact_id_seq OWNED BY public.contact.id;

CREATE TABLE public.fonction (
                                 id bigint NOT NULL,
                                 libelle character varying(120) NOT NULL,
                                 categorie_id bigint NOT NULL,
                                 niveau_cible character varying(25) NOT NULL,
                                 actif boolean DEFAULT true NOT NULL,
                                 CONSTRAINT fonction_niveau_cible_check CHECK (((niveau_cible)::text = ANY ((ARRAY['REGION'::character varying, 'DISTRICT'::character varying, 'COMMUNE'::character varying, 'FOKOTANY'::character varying, 'AUTRE'::character varying])::text[])))
);

CREATE SEQUENCE public.fonction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.fonction_id_seq OWNED BY public.fonction.id;

CREATE TABLE public.journal (
                                id bigint NOT NULL,
                                utilisateur_id bigint NOT NULL,
                                action character varying(20) NOT NULL,
                                table_cible character varying(40),
                                id_cible bigint,
                                details text,
                                date_action timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                CONSTRAINT journal_action_check CHECK (((action)::text = ANY ((ARRAY['CONNEXION'::character varying, 'CONSULTATION'::character varying, 'CREATION'::character varying, 'MODIFICATION'::character varying, 'DESACTIVATION'::character varying, 'SUPPRESSION'::character varying])::text[])))
);

CREATE SEQUENCE public.journal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.journal_id_seq OWNED BY public.journal.id;

CREATE TABLE public.kmd_membre (
                                   id bigint NOT NULL,
                                   contact_id bigint NOT NULL,
                                   niveau_kmd character varying(30),
                                   role_kmd character varying(60),
                                   numero_ordre integer
);

CREATE SEQUENCE public.kmd_membre_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.kmd_membre_id_seq OWNED BY public.kmd_membre.id;

CREATE TABLE public.localite (
                                 id bigint NOT NULL,
                                 nom character varying(150) NOT NULL,
                                 type_localite character varying(25) NOT NULL,
                                 parent_id bigint,
                                 code_source character varying(40),
                                 actif boolean DEFAULT true NOT NULL,
                                 code_district character varying(20),
                                 code_commune character varying(20),
                                 code_fokotany character varying(20),
                                 type_com character varying(50),
                                 categorie_com character varying(50),
                                 CONSTRAINT localite_type_localite_check CHECK (((type_localite)::text = ANY ((ARRAY['REGION'::character varying, 'DISTRICT'::character varying, 'COMMUNE'::character varying, 'FOKOTANY'::character varying, 'VILLAGE'::character varying, 'LIMITROPHE'::character varying])::text[])))
);

CREATE SEQUENCE public.localite_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.localite_id_seq OWNED BY public.localite.id;

CREATE TABLE public.maire (
                              id bigint NOT NULL,
                              contact_id bigint NOT NULL,
                              code_commune character varying(50),
                              type_com character varying(30),
                              categorie_com character varying(30),
                              genre character varying(15),
                              statut_election character varying(50),
                              observation text
);

CREATE SEQUENCE public.maire_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.maire_id_seq OWNED BY public.maire.id;

CREATE TABLE public.organisation (
                                     id bigint NOT NULL,
                                     nom character varying(150) NOT NULL,
                                     categorie_id bigint NOT NULL,
                                     type_structure character varying(50),
                                     localite_id bigint NOT NULL,
                                     actif boolean DEFAULT true NOT NULL
);

CREATE SEQUENCE public.organisation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.organisation_id_seq OWNED BY public.organisation.id;

CREATE TABLE public.regle_effectif (
                                       id bigint NOT NULL,
                                       fonction_id bigint NOT NULL,
                                       type_localite character varying(25) NOT NULL,
                                       effectif_prevu smallint NOT NULL,
                                       CONSTRAINT regle_effectif_effectif_prevu_check CHECK ((effectif_prevu > 0)),
                                       CONSTRAINT regle_effectif_type_localite_check CHECK (((type_localite)::text = ANY ((ARRAY['REGION'::character varying, 'DISTRICT'::character varying, 'COMMUNE'::character varying, 'FOKOTANY'::character varying])::text[])))
);

CREATE SEQUENCE public.regle_effectif_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.regle_effectif_id_seq OWNED BY public.regle_effectif.id;

CREATE TABLE public.utilisateur (
                                    id bigint NOT NULL,
                                    login character varying(50) NOT NULL,
                                    nom_complet character varying(120),
                                    mot_de_passe_hash character varying(100) NOT NULL,
                                    role character varying(10) DEFAULT 'LECTEUR'::character varying NOT NULL,
                                    actif boolean DEFAULT true NOT NULL,
                                    date_creation timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                    CONSTRAINT utilisateur_role_check CHECK (((role)::text = ANY ((ARRAY['LECTEUR'::character varying, 'ADMIN'::character varying])::text[])))
);

CREATE SEQUENCE public.utilisateur_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.utilisateur_id_seq OWNED BY public.utilisateur.id;

-- ---------------------------------------------------------------------
-- 3. Valeurs par défaut des clés primaires (Séquences)
-- ---------------------------------------------------------------------

ALTER TABLE ONLY public.categorie ALTER COLUMN id SET DEFAULT nextval('public.categorie_id_seq'::regclass);
ALTER TABLE ONLY public.centre_vote ALTER COLUMN id SET DEFAULT nextval('public.centre_vote_id_seq'::regclass);
ALTER TABLE ONLY public.contact ALTER COLUMN id SET DEFAULT nextval('public.contact_id_seq'::regclass);
ALTER TABLE ONLY public.fonction ALTER COLUMN id SET DEFAULT nextval('public.fonction_id_seq'::regclass);
ALTER TABLE ONLY public.journal ALTER COLUMN id SET DEFAULT nextval('public.journal_id_seq'::regclass);
ALTER TABLE ONLY public.kmd_membre ALTER COLUMN id SET DEFAULT nextval('public.kmd_membre_id_seq'::regclass);
ALTER TABLE ONLY public.localite ALTER COLUMN id SET DEFAULT nextval('public.localite_id_seq'::regclass);
ALTER TABLE ONLY public.maire ALTER COLUMN id SET DEFAULT nextval('public.maire_id_seq'::regclass);
ALTER TABLE ONLY public.organisation ALTER COLUMN id SET DEFAULT nextval('public.organisation_id_seq'::regclass);
ALTER TABLE ONLY public.regle_effectif ALTER COLUMN id SET DEFAULT nextval('public.regle_effectif_id_seq'::regclass);
ALTER TABLE ONLY public.utilisateur ALTER COLUMN id SET DEFAULT nextval('public.utilisateur_id_seq'::regclass);

-- ---------------------------------------------------------------------
-- 4. Contraintes PRIMARY KEY et UNIQUE
-- ---------------------------------------------------------------------

ALTER TABLE ONLY public.categorie ADD CONSTRAINT categorie_code_key UNIQUE (code);
ALTER TABLE ONLY public.categorie ADD CONSTRAINT categorie_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.centre_vote ADD CONSTRAINT centre_vote_commune_id_nom_key UNIQUE (commune_id, nom);
ALTER TABLE ONLY public.centre_vote ADD CONSTRAINT centre_vote_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.contact ADD CONSTRAINT contact_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.fonction ADD CONSTRAINT fonction_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.journal ADD CONSTRAINT journal_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kmd_membre ADD CONSTRAINT kmd_membre_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.localite ADD CONSTRAINT localite_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.maire ADD CONSTRAINT maire_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.organisation ADD CONSTRAINT organisation_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.regle_effectif ADD CONSTRAINT regle_effectif_fonction_id_type_localite_key UNIQUE (fonction_id, type_localite);
ALTER TABLE ONLY public.regle_effectif ADD CONSTRAINT regle_effectif_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.utilisateur ADD CONSTRAINT utilisateur_login_key UNIQUE (login);
ALTER TABLE ONLY public.utilisateur ADD CONSTRAINT utilisateur_pkey PRIMARY KEY (id);

-- ---------------------------------------------------------------------
-- 5. Index
-- ---------------------------------------------------------------------

CREATE INDEX idx_centre_commune ON public.centre_vote USING btree (commune_id);
CREATE INDEX idx_contact_fonction ON public.contact USING btree (fonction_id);
CREATE INDEX idx_contact_localite ON public.contact USING btree (localite_id);
CREATE INDEX idx_contact_nom_trgm ON public.contact USING gin (lower((nom)::text) public.gin_trgm_ops);
CREATE INDEX idx_journal_date ON public.journal USING btree (date_action);
CREATE INDEX idx_localite_parent ON public.localite USING btree (parent_id);

-- ---------------------------------------------------------------------
-- 6. Contraintes FOREIGN KEY
-- ---------------------------------------------------------------------

ALTER TABLE ONLY public.centre_vote
    ADD CONSTRAINT centre_vote_commune_id_fkey FOREIGN KEY (commune_id) REFERENCES public.localite(id);

ALTER TABLE ONLY public.centre_vote
    ADD CONSTRAINT centre_vote_fokotany_id_fkey FOREIGN KEY (fokotany_id) REFERENCES public.localite(id);

ALTER TABLE ONLY public.contact
    ADD CONSTRAINT contact_fonction_id_fkey FOREIGN KEY (fonction_id) REFERENCES public.fonction(id);

ALTER TABLE ONLY public.contact
    ADD CONSTRAINT contact_localite_id_fkey FOREIGN KEY (localite_id) REFERENCES public.localite(id);

ALTER TABLE ONLY public.contact
    ADD CONSTRAINT contact_organisation_id_fkey FOREIGN KEY (organisation_id) REFERENCES public.organisation(id);

ALTER TABLE ONLY public.fonction
    ADD CONSTRAINT fonction_categorie_id_fkey FOREIGN KEY (categorie_id) REFERENCES public.categorie(id);

ALTER TABLE ONLY public.journal
    ADD CONSTRAINT journal_utilisateur_id_fkey FOREIGN KEY (utilisateur_id) REFERENCES public.utilisateur(id);

ALTER TABLE ONLY public.kmd_membre
    ADD CONSTRAINT kmd_membre_contact_id_fkey FOREIGN KEY (contact_id) REFERENCES public.contact(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.localite
    ADD CONSTRAINT localite_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.localite(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.maire
    ADD CONSTRAINT maire_contact_id_fkey FOREIGN KEY (contact_id) REFERENCES public.contact(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.organisation
    ADD CONSTRAINT organisation_categorie_id_fkey FOREIGN KEY (categorie_id) REFERENCES public.categorie(id);

ALTER TABLE ONLY public.organisation
    ADD CONSTRAINT organisation_localite_id_fkey FOREIGN KEY (localite_id) REFERENCES public.localite(id);

ALTER TABLE ONLY public.regle_effectif
    ADD CONSTRAINT regle_effectif_fonction_id_fkey FOREIGN KEY (fonction_id) REFERENCES public.fonction(id);

-- ---------------------------------------------------------------------
-- 7. Comptes ADMIN initiaux (4 administrateurs)
-- ---------------------------------------------------------------------
-- admin1 : Admin#2026!1
-- admin2 : Admin#2026!2
-- admin3 : Admin#2026!3
-- admin4 : Admin#2026!4

INSERT INTO public.utilisateur (login, nom_complet, mot_de_passe_hash, role, actif)
VALUES
    ('admin1', 'Administrateur Principal 1', '$2a$10$w.pVYxUJSn/xT3Muek.0o.QtBHKX23Kom28rbdUzV6qXR76vhfq8S', 'ADMIN', true),
    ('admin2', 'Administrateur 2', '$2a$10$wkdnDsXfGl1tojs60nvsiOq.YB9tn0njKfq8I/JdIqlQAzuSyFplq', 'ADMIN', true),
    ('admin3', 'Administrateur 3', '$2a$10$IA3PcgvNEq4bEPu8X/tYzeG5yrF9Cn65tzMRUJA8/wmcZprE5Ejou', 'ADMIN', true),
    ('admin4', 'Administrateur 4', '$2a$10$98NG28y/nYlEESqD72aP1eNg3rQbGZzVBXScuQZP5tjViUFygT6cm', 'ADMIN', true)
    ON CONFLICT (login) DO NOTHING;