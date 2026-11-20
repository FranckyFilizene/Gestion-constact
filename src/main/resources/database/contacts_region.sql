--
-- PostgreSQL database dump
--

\restrict fnWFu8t0chv4aBP07EnibWEGWDED91LJPLc6tDkogZdRTWTmmIH7PzxmbhNZhf5

-- Dumped from database version 18.4
-- Dumped by pg_dump version 18.4

-- Started on 2026-11-18 09:38:09

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 3 (class 3079 OID 49255)
-- Name: pg_trgm; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;


--
-- TOC entry 5148 (class 0 OID 0)
-- Dependencies: 3
-- Name: EXTENSION pg_trgm; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pg_trgm IS 'text similarity measurement and index searching based on trigrams';


--
-- TOC entry 2 (class 3079 OID 49248)
-- Name: unaccent; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;


--
-- TOC entry 5149 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION unaccent; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION unaccent IS 'text search dictionary that removes accents';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 226 (class 1259 OID 49337)
-- Name: categorie; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.categorie (
    id bigint NOT NULL,
    code character varying(50) NOT NULL,
    libelle character varying(100) NOT NULL,
    sensible boolean DEFAULT false NOT NULL
);


ALTER TABLE public.categorie OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 49336)
-- Name: categorie_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.categorie_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.categorie_id_seq OWNER TO postgres;

--
-- TOC entry 5150 (class 0 OID 0)
-- Dependencies: 225
-- Name: categorie_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.categorie_id_seq OWNED BY public.categorie.id;


--
-- TOC entry 238 (class 1259 OID 49457)
-- Name: centre_vote; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.centre_vote OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 49456)
-- Name: centre_vote_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.centre_vote_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.centre_vote_id_seq OWNER TO postgres;

--
-- TOC entry 5151 (class 0 OID 0)
-- Dependencies: 237
-- Name: centre_vote_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.centre_vote_id_seq OWNED BY public.centre_vote.id;


--
-- TOC entry 232 (class 1259 OID 49393)
-- Name: contact; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.contact OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 49392)
-- Name: contact_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.contact_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.contact_id_seq OWNER TO postgres;

--
-- TOC entry 5152 (class 0 OID 0)
-- Dependencies: 231
-- Name: contact_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.contact_id_seq OWNED BY public.contact.id;


--
-- TOC entry 228 (class 1259 OID 49351)
-- Name: fonction; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fonction (
    id bigint NOT NULL,
    libelle character varying(120) NOT NULL,
    categorie_id bigint NOT NULL,
    niveau_cible character varying(25) NOT NULL,
    actif boolean DEFAULT true NOT NULL,
    CONSTRAINT fonction_niveau_cible_check CHECK (((niveau_cible)::text = ANY ((ARRAY['REGION'::character varying, 'DISTRICT'::character varying, 'COMMUNE'::character varying, 'FOKOTANY'::character varying, 'AUTRE'::character varying])::text[])))
);


ALTER TABLE public.fonction OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 49350)
-- Name: fonction_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fonction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fonction_id_seq OWNER TO postgres;

--
-- TOC entry 5153 (class 0 OID 0)
-- Dependencies: 227
-- Name: fonction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fonction_id_seq OWNED BY public.fonction.id;


--
-- TOC entry 242 (class 1259 OID 49507)
-- Name: journal; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.journal OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 49506)
-- Name: journal_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.journal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.journal_id_seq OWNER TO postgres;

--
-- TOC entry 5154 (class 0 OID 0)
-- Dependencies: 241
-- Name: journal_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.journal_id_seq OWNED BY public.journal.id;


--
-- TOC entry 236 (class 1259 OID 49443)
-- Name: kmd_membre; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.kmd_membre (
    id bigint NOT NULL,
    contact_id bigint NOT NULL,
    niveau_kmd character varying(30),
    role_kmd character varying(60),
    numero_ordre integer
);


ALTER TABLE public.kmd_membre OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 49442)
-- Name: kmd_membre_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.kmd_membre_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.kmd_membre_id_seq OWNER TO postgres;

--
-- TOC entry 5155 (class 0 OID 0)
-- Dependencies: 235
-- Name: kmd_membre_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.kmd_membre_id_seq OWNED BY public.kmd_membre.id;


--
-- TOC entry 222 (class 1259 OID 49167)
-- Name: localite; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.localite OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 49166)
-- Name: localite_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.localite_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.localite_id_seq OWNER TO postgres;

--
-- TOC entry 5156 (class 0 OID 0)
-- Dependencies: 221
-- Name: localite_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.localite_id_seq OWNED BY public.localite.id;


--
-- TOC entry 234 (class 1259 OID 49427)
-- Name: maire; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.maire OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 49426)
-- Name: maire_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.maire_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.maire_id_seq OWNER TO postgres;

--
-- TOC entry 5157 (class 0 OID 0)
-- Dependencies: 233
-- Name: maire_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.maire_id_seq OWNED BY public.maire.id;


--
-- TOC entry 230 (class 1259 OID 49370)
-- Name: organisation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.organisation (
    id bigint NOT NULL,
    nom character varying(150) NOT NULL,
    categorie_id bigint NOT NULL,
    type_structure character varying(50),
    localite_id bigint NOT NULL,
    actif boolean DEFAULT true NOT NULL
);


ALTER TABLE public.organisation OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 49369)
-- Name: organisation_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.organisation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.organisation_id_seq OWNER TO postgres;

--
-- TOC entry 5158 (class 0 OID 0)
-- Dependencies: 229
-- Name: organisation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.organisation_id_seq OWNED BY public.organisation.id;


--
-- TOC entry 240 (class 1259 OID 49487)
-- Name: regle_effectif; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.regle_effectif (
    id bigint NOT NULL,
    fonction_id bigint NOT NULL,
    type_localite character varying(25) NOT NULL,
    effectif_prevu smallint NOT NULL,
    CONSTRAINT regle_effectif_effectif_prevu_check CHECK ((effectif_prevu > 0)),
    CONSTRAINT regle_effectif_type_localite_check CHECK (((type_localite)::text = ANY ((ARRAY['REGION'::character varying, 'DISTRICT'::character varying, 'COMMUNE'::character varying, 'FOKOTANY'::character varying])::text[])))
);


ALTER TABLE public.regle_effectif OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 49486)
-- Name: regle_effectif_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.regle_effectif_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.regle_effectif_id_seq OWNER TO postgres;

--
-- TOC entry 5159 (class 0 OID 0)
-- Dependencies: 239
-- Name: regle_effectif_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.regle_effectif_id_seq OWNED BY public.regle_effectif.id;


--
-- TOC entry 224 (class 1259 OID 49230)
-- Name: utilisateur; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.utilisateur OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 49229)
-- Name: utilisateur_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.utilisateur_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.utilisateur_id_seq OWNER TO postgres;

--
-- TOC entry 5160 (class 0 OID 0)
-- Dependencies: 223
-- Name: utilisateur_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.utilisateur_id_seq OWNED BY public.utilisateur.id;


--
-- TOC entry 4921 (class 2604 OID 49340)
-- Name: categorie id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categorie ALTER COLUMN id SET DEFAULT nextval('public.categorie_id_seq'::regclass);


--
-- TOC entry 4933 (class 2604 OID 49460)
-- Name: centre_vote id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.centre_vote ALTER COLUMN id SET DEFAULT nextval('public.centre_vote_id_seq'::regclass);


--
-- TOC entry 4927 (class 2604 OID 49396)
-- Name: contact id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contact ALTER COLUMN id SET DEFAULT nextval('public.contact_id_seq'::regclass);


--
-- TOC entry 4923 (class 2604 OID 49354)
-- Name: fonction id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fonction ALTER COLUMN id SET DEFAULT nextval('public.fonction_id_seq'::regclass);


--
-- TOC entry 4937 (class 2604 OID 49510)
-- Name: journal id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal ALTER COLUMN id SET DEFAULT nextval('public.journal_id_seq'::regclass);


--
-- TOC entry 4932 (class 2604 OID 49446)
-- Name: kmd_membre id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.kmd_membre ALTER COLUMN id SET DEFAULT nextval('public.kmd_membre_id_seq'::regclass);


--
-- TOC entry 4915 (class 2604 OID 49170)
-- Name: localite id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.localite ALTER COLUMN id SET DEFAULT nextval('public.localite_id_seq'::regclass);


--
-- TOC entry 4931 (class 2604 OID 49430)
-- Name: maire id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.maire ALTER COLUMN id SET DEFAULT nextval('public.maire_id_seq'::regclass);


--
-- TOC entry 4925 (class 2604 OID 49373)
-- Name: organisation id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.organisation ALTER COLUMN id SET DEFAULT nextval('public.organisation_id_seq'::regclass);


--
-- TOC entry 4936 (class 2604 OID 49490)
-- Name: regle_effectif id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.regle_effectif ALTER COLUMN id SET DEFAULT nextval('public.regle_effectif_id_seq'::regclass);


--
-- TOC entry 4917 (class 2604 OID 49233)
-- Name: utilisateur id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utilisateur ALTER COLUMN id SET DEFAULT nextval('public.utilisateur_id_seq'::regclass);


--
-- TOC entry 4955 (class 2606 OID 49349)
-- Name: categorie categorie_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categorie
    ADD CONSTRAINT categorie_code_key UNIQUE (code);


--
-- TOC entry 4957 (class 2606 OID 49347)
-- Name: categorie categorie_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categorie
    ADD CONSTRAINT categorie_pkey PRIMARY KEY (id);


--
-- TOC entry 4972 (class 2606 OID 49475)
-- Name: centre_vote centre_vote_commune_id_nom_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.centre_vote
    ADD CONSTRAINT centre_vote_commune_id_nom_key UNIQUE (commune_id, nom);


--
-- TOC entry 4974 (class 2606 OID 49473)
-- Name: centre_vote centre_vote_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.centre_vote
    ADD CONSTRAINT centre_vote_pkey PRIMARY KEY (id);


--
-- TOC entry 4963 (class 2606 OID 49410)
-- Name: contact contact_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contact
    ADD CONSTRAINT contact_pkey PRIMARY KEY (id);


--
-- TOC entry 4959 (class 2606 OID 49363)
-- Name: fonction fonction_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fonction
    ADD CONSTRAINT fonction_pkey PRIMARY KEY (id);


--
-- TOC entry 4982 (class 2606 OID 49520)
-- Name: journal journal_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal
    ADD CONSTRAINT journal_pkey PRIMARY KEY (id);


--
-- TOC entry 4970 (class 2606 OID 49450)
-- Name: kmd_membre kmd_membre_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.kmd_membre
    ADD CONSTRAINT kmd_membre_pkey PRIMARY KEY (id);


--
-- TOC entry 4949 (class 2606 OID 49178)
-- Name: localite localite_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.localite
    ADD CONSTRAINT localite_pkey PRIMARY KEY (id);


--
-- TOC entry 4968 (class 2606 OID 49436)
-- Name: maire maire_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.maire
    ADD CONSTRAINT maire_pkey PRIMARY KEY (id);


--
-- TOC entry 4961 (class 2606 OID 49381)
-- Name: organisation organisation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.organisation
    ADD CONSTRAINT organisation_pkey PRIMARY KEY (id);


--
-- TOC entry 4977 (class 2606 OID 49500)
-- Name: regle_effectif regle_effectif_fonction_id_type_localite_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.regle_effectif
    ADD CONSTRAINT regle_effectif_fonction_id_type_localite_key UNIQUE (fonction_id, type_localite);


--
-- TOC entry 4979 (class 2606 OID 49498)
-- Name: regle_effectif regle_effectif_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.regle_effectif
    ADD CONSTRAINT regle_effectif_pkey PRIMARY KEY (id);


--
-- TOC entry 4951 (class 2606 OID 49247)
-- Name: utilisateur utilisateur_login_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utilisateur
    ADD CONSTRAINT utilisateur_login_key UNIQUE (login);


--
-- TOC entry 4953 (class 2606 OID 49245)
-- Name: utilisateur utilisateur_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utilisateur
    ADD CONSTRAINT utilisateur_pkey PRIMARY KEY (id);


--
-- TOC entry 4975 (class 1259 OID 49530)
-- Name: idx_centre_commune; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_centre_commune ON public.centre_vote USING btree (commune_id);


--
-- TOC entry 4964 (class 1259 OID 49528)
-- Name: idx_contact_fonction; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_contact_fonction ON public.contact USING btree (fonction_id);


--
-- TOC entry 4965 (class 1259 OID 49527)
-- Name: idx_contact_localite; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_contact_localite ON public.contact USING btree (localite_id);


--
-- TOC entry 4966 (class 1259 OID 49529)
-- Name: idx_contact_nom_trgm; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_contact_nom_trgm ON public.contact USING gin (lower((nom)::text) public.gin_trgm_ops);


--
-- TOC entry 4980 (class 1259 OID 49531)
-- Name: idx_journal_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_journal_date ON public.journal USING btree (date_action);


--
-- TOC entry 4947 (class 1259 OID 49526)
-- Name: idx_localite_parent; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_localite_parent ON public.localite USING btree (parent_id);


--
-- TOC entry 4992 (class 2606 OID 49476)
-- Name: centre_vote centre_vote_commune_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.centre_vote
    ADD CONSTRAINT centre_vote_commune_id_fkey FOREIGN KEY (commune_id) REFERENCES public.localite(id);


--
-- TOC entry 4993 (class 2606 OID 49481)
-- Name: centre_vote centre_vote_fokotany_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.centre_vote
    ADD CONSTRAINT centre_vote_fokotany_id_fkey FOREIGN KEY (fokotany_id) REFERENCES public.localite(id);


--
-- TOC entry 4987 (class 2606 OID 49411)
-- Name: contact contact_fonction_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contact
    ADD CONSTRAINT contact_fonction_id_fkey FOREIGN KEY (fonction_id) REFERENCES public.fonction(id);


--
-- TOC entry 4988 (class 2606 OID 49421)
-- Name: contact contact_localite_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contact
    ADD CONSTRAINT contact_localite_id_fkey FOREIGN KEY (localite_id) REFERENCES public.localite(id);


--
-- TOC entry 4989 (class 2606 OID 49416)
-- Name: contact contact_organisation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contact
    ADD CONSTRAINT contact_organisation_id_fkey FOREIGN KEY (organisation_id) REFERENCES public.organisation(id);


--
-- TOC entry 4984 (class 2606 OID 49364)
-- Name: fonction fonction_categorie_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fonction
    ADD CONSTRAINT fonction_categorie_id_fkey FOREIGN KEY (categorie_id) REFERENCES public.categorie(id);


--
-- TOC entry 4995 (class 2606 OID 49521)
-- Name: journal journal_utilisateur_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal
    ADD CONSTRAINT journal_utilisateur_id_fkey FOREIGN KEY (utilisateur_id) REFERENCES public.utilisateur(id);


--
-- TOC entry 4991 (class 2606 OID 49451)
-- Name: kmd_membre kmd_membre_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.kmd_membre
    ADD CONSTRAINT kmd_membre_contact_id_fkey FOREIGN KEY (contact_id) REFERENCES public.contact(id) ON DELETE CASCADE;


--
-- TOC entry 4983 (class 2606 OID 49179)
-- Name: localite localite_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.localite
    ADD CONSTRAINT localite_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.localite(id) ON DELETE CASCADE;


--
-- TOC entry 4990 (class 2606 OID 49437)
-- Name: maire maire_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.maire
    ADD CONSTRAINT maire_contact_id_fkey FOREIGN KEY (contact_id) REFERENCES public.contact(id) ON DELETE CASCADE;


--
-- TOC entry 4985 (class 2606 OID 49382)
-- Name: organisation organisation_categorie_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.organisation
    ADD CONSTRAINT organisation_categorie_id_fkey FOREIGN KEY (categorie_id) REFERENCES public.categorie(id);


--
-- TOC entry 4986 (class 2606 OID 49387)
-- Name: organisation organisation_localite_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.organisation
    ADD CONSTRAINT organisation_localite_id_fkey FOREIGN KEY (localite_id) REFERENCES public.localite(id);


--
-- TOC entry 4994 (class 2606 OID 49501)
-- Name: regle_effectif regle_effectif_fonction_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.regle_effectif
    ADD CONSTRAINT regle_effectif_fonction_id_fkey FOREIGN KEY (fonction_id) REFERENCES public.fonction(id);


-- Completed on 2026-11-18 09:38:09

--
-- PostgreSQL database dump complete
--

\unrestrict fnWFu8t0chv4aBP07EnibWEGWDED91LJPLc6tDkogZdRTWTmmIH7PzxmbhNZhf5

