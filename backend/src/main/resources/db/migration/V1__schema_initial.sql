-- =====================================================================
--  Schéma initial - Gestion de stock
-- =====================================================================

CREATE TABLE utilisateur (
    id              BIGSERIAL PRIMARY KEY,
    nom_complet     VARCHAR(120) NOT NULL,
    login           VARCHAR(60)  NOT NULL UNIQUE,
    mot_de_passe    VARCHAR(100) NOT NULL,
    role            VARCHAR(30)  NOT NULL,
    actif           BOOLEAN      NOT NULL DEFAULT TRUE,
    date_creation   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE categorie (
    id   BIGSERIAL PRIMARY KEY,
    nom  VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE produit (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(50)    NOT NULL UNIQUE,
    code_barre      VARCHAR(50)    UNIQUE,
    designation     VARCHAR(200)   NOT NULL,
    categorie_id    BIGINT         REFERENCES categorie(id),
    unite           VARCHAR(20)    NOT NULL DEFAULT 'pièce',
    prix_achat      NUMERIC(14,2)  NOT NULL DEFAULT 0,
    prix_vente      NUMERIC(14,2)  NOT NULL DEFAULT 0,
    taux_tva        NUMERIC(5,2)   NOT NULL DEFAULT 0,
    seuil_alerte    INTEGER        NOT NULL DEFAULT 0,
    stock_actuel    INTEGER        NOT NULL DEFAULT 0 CHECK (stock_actuel >= 0),
    emplacement     VARCHAR(100),
    actif           BOOLEAN        NOT NULL DEFAULT TRUE,
    date_creation   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE mouvement_stock (
    id              BIGSERIAL PRIMARY KEY,
    produit_id      BIGINT       NOT NULL REFERENCES produit(id),
    type            VARCHAR(10)  NOT NULL CHECK (type IN ('ENTREE', 'SORTIE')),
    motif           VARCHAR(30)  NOT NULL,
    quantite        INTEGER      NOT NULL CHECK (quantite > 0),
    stock_apres     INTEGER      NOT NULL,
    reference       VARCHAR(100),
    commentaire     VARCHAR(500),
    utilisateur_id  BIGINT       NOT NULL REFERENCES utilisateur(id),
    date_mouvement  TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_mouvement_produit ON mouvement_stock(produit_id);
CREATE INDEX idx_mouvement_date    ON mouvement_stock(date_mouvement);

CREATE TABLE client (
    id          BIGSERIAL PRIMARY KEY,
    nom         VARCHAR(150) NOT NULL,
    telephone   VARCHAR(30),
    adresse     VARCHAR(255)
);

CREATE TABLE facture (
    id              BIGSERIAL PRIMARY KEY,
    numero          VARCHAR(30)   NOT NULL UNIQUE,
    client_id       BIGINT        REFERENCES client(id),
    date_facture    TIMESTAMP     NOT NULL DEFAULT NOW(),
    total_ht        NUMERIC(14,2) NOT NULL,
    total_tva       NUMERIC(14,2) NOT NULL,
    total_ttc       NUMERIC(14,2) NOT NULL,
    mode_paiement   VARCHAR(20)   NOT NULL,
    statut          VARCHAR(20)   NOT NULL DEFAULT 'VALIDEE',
    motif_annulation VARCHAR(255),
    utilisateur_id  BIGINT        NOT NULL REFERENCES utilisateur(id)
);
CREATE INDEX idx_facture_date ON facture(date_facture);

CREATE TABLE ligne_facture (
    id              BIGSERIAL PRIMARY KEY,
    facture_id      BIGINT        NOT NULL REFERENCES facture(id) ON DELETE CASCADE,
    produit_id      BIGINT        NOT NULL REFERENCES produit(id),
    quantite        INTEGER       NOT NULL CHECK (quantite > 0),
    prix_unitaire   NUMERIC(14,2) NOT NULL,
    taux_tva        NUMERIC(5,2)  NOT NULL,
    remise          NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_ligne     NUMERIC(14,2) NOT NULL
);

-- Compteur de numéros de facture par année, SANS TROU : la ligne est verrouillée
-- pendant la transaction et l'incrément est annulé si la vente échoue.
CREATE TABLE numerotation_facture (
    annee           INTEGER PRIMARY KEY,
    dernier_numero  INTEGER NOT NULL
);

-- Journal d'audit : en écriture seule pour l'application (aucune mise à jour / suppression)
CREATE TABLE journal_audit (
    id              BIGSERIAL PRIMARY KEY,
    date_action     TIMESTAMP    NOT NULL DEFAULT NOW(),
    utilisateur_login VARCHAR(60),
    action          VARCHAR(50)  NOT NULL,
    entite          VARCHAR(50),
    entite_id       BIGINT,
    details         TEXT,
    adresse_ip      VARCHAR(50)
);
CREATE INDEX idx_audit_date  ON journal_audit(date_action);
CREATE INDEX idx_audit_login ON journal_audit(utilisateur_login);

INSERT INTO categorie (nom) VALUES ('Général');
