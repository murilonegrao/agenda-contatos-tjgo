-- ============================================================================
--  Agenda de Contatos TJGO — dump.sql (DDL + dados de exemplo)  ·  PostgreSQL 18
--  AUTOCONTIDO: cria as tabelas e popula com dados realistas do TJGO.
--  Roda de primeira em banco vazio (RNF02 — "dou F5 e não pode dar erro").
--  Idempotente: pode reexecutar (faz DROP das tabelas antes).
--
--  Uso:  createdb agenda
--        psql -d agenda -f dump.sql
--
--  Os dados exercitam os critérios de aceite:
--    CA03 — "Central de Plantão Judiciário": unidade com telefones/e-mails e ZERO contatos
--    CA04 — Mariana (contato 3) lotada em DUAS unidades (3 e 4)
--    CA05 — Mariana (contato 3) responsável por DUAS unidades (3 e 4)
--    CA06 — Central de Plantão e a contato Mariana têm >= 2 telefones e >= 2 e-mails
--    RN05 — Carlos é responsável pela unidade 5 sem estar lotado nela
-- ============================================================================

-- ---------------------------------------------------------------- DDL --------
DROP TABLE IF EXISTS email    CASCADE;
DROP TABLE IF EXISTS telefone CASCADE;
DROP TABLE IF EXISTS lotacao  CASCADE;
DROP TABLE IF EXISTS contato  CASCADE;
DROP TABLE IF EXISTS unidade  CASCADE;

CREATE TABLE unidade (
    id             SERIAL       PRIMARY KEY,
    nome           VARCHAR(120) NOT NULL,
    tipo           VARCHAR(20)  NOT NULL
                   CHECK (tipo IN ('UNIDADE_PRISIONAL', 'ORGAO', 'OUTRO')),
    endereco       VARCHAR(200),
    responsavel_id INTEGER,
    criado_em      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE contato (
    id        SERIAL       PRIMARY KEY,
    nome      VARCHAR(120) NOT NULL,
    cargo     VARCHAR(80),
    criado_em TIMESTAMP    NOT NULL DEFAULT now()
);

ALTER TABLE unidade
    ADD CONSTRAINT fk_unidade_responsavel
    FOREIGN KEY (responsavel_id) REFERENCES contato(id) ON DELETE SET NULL;

CREATE TABLE lotacao (
    unidade_id INTEGER NOT NULL REFERENCES unidade(id) ON DELETE CASCADE,
    contato_id INTEGER NOT NULL REFERENCES contato(id) ON DELETE CASCADE,
    desde      DATE    NOT NULL DEFAULT current_date,
    PRIMARY KEY (unidade_id, contato_id)
);

CREATE TABLE telefone (
    id         SERIAL      PRIMARY KEY,
    numero     VARCHAR(15) NOT NULL CHECK (numero ~ '^[0-9]{8,15}$'),
    tipo       VARCHAR(15) NOT NULL
               CHECK (tipo IN ('CELULAR', 'FIXO', 'WHATSAPP', 'PLANTAO')),
    unidade_id INTEGER     REFERENCES unidade(id) ON DELETE CASCADE,
    contato_id INTEGER     REFERENCES contato(id) ON DELETE CASCADE,
    criado_em  TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT chk_telefone_arco_exclusivo
        CHECK ( (unidade_id IS NOT NULL)::int + (contato_id IS NOT NULL)::int = 1 )
);

CREATE TABLE email (
    id         SERIAL       PRIMARY KEY,
    endereco   VARCHAR(150) NOT NULL,
    tipo       VARCHAR(30),
    unidade_id INTEGER      REFERENCES unidade(id) ON DELETE CASCADE,
    contato_id INTEGER      REFERENCES contato(id) ON DELETE CASCADE,
    criado_em  TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT chk_email_arco_exclusivo
        CHECK ( (unidade_id IS NOT NULL)::int + (contato_id IS NOT NULL)::int = 1 )
);

CREATE INDEX idx_lotacao_contato     ON lotacao(contato_id);
CREATE INDEX idx_unidade_responsavel ON unidade(responsavel_id);
CREATE INDEX idx_telefone_unidade    ON telefone(unidade_id);
CREATE INDEX idx_telefone_contato    ON telefone(contato_id);
CREATE INDEX idx_email_unidade       ON email(unidade_id);
CREATE INDEX idx_email_contato       ON email(contato_id);
CREATE INDEX idx_unidade_nome        ON unidade(lower(nome));
CREATE INDEX idx_contato_nome        ON contato(lower(nome));

-- ---------------------------------------------------------------- DADOS -------

-- 1) UNIDADES (responsável definido depois, quando os contatos existirem).
INSERT INTO unidade (id, nome, tipo, endereco) VALUES
  (1, 'Tribunal de Justiça do Estado de Goiás', 'ORGAO',             'Rua 19, Centro, Goiânia/GO'),
  (2, 'Vara de Execuções Penais de Goiânia',    'ORGAO',             'Av. Olinda, Park Lozandes, Goiânia/GO'),
  (3, 'Presídio de Aparecida de Goiânia',       'UNIDADE_PRISIONAL', 'Aparecida de Goiânia/GO'),
  (4, 'Complexo Prisional de Goiânia',          'UNIDADE_PRISIONAL', 'Goiânia/GO'),
  (5, 'Casa de Prisão Provisória de Goiânia',   'UNIDADE_PRISIONAL', 'Goiânia/GO'),
  (6, 'Central de Plantão Judiciário',          'OUTRO',             'Fórum de Goiânia/GO');

-- 2) CONTATOS (pessoas).
INSERT INTO contato (id, nome, cargo) VALUES
  (1, 'Ana Paula Ribeiro',     'Juíza de Direito'),
  (2, 'Carlos Eduardo Souza',  'Diretor de Unidade'),
  (3, 'Mariana Castro Lima',   'Agente de Plantão'),
  (4, 'João Pedro Alves',      'Assessor Jurídico'),
  (5, 'Fernanda Gomes',        'Escrivã'),
  (6, 'Roberto Dias',          'Agente Penitenciário');

-- 3) RESPONSÁVEIS (RF04). Mariana responde por 2 unidades (CA05);
--    Carlos responde pela unidade 5 sem estar lotado nela (RN05).
UPDATE unidade SET responsavel_id = 1 WHERE id = 1;   -- Ana
UPDATE unidade SET responsavel_id = 5 WHERE id = 2;   -- Fernanda
UPDATE unidade SET responsavel_id = 3 WHERE id = 3;   -- Mariana
UPDATE unidade SET responsavel_id = 3 WHERE id = 4;   -- Mariana (mesma pessoa)
UPDATE unidade SET responsavel_id = 2 WHERE id = 5;   -- Carlos (não lotado aqui)
-- unidade 6 (Central de Plantão) fica SEM responsável e SEM contatos (CA03).

-- 4) LOTAÇÃO N:N (RF03). Mariana lotada em 2 unidades (CA04).
INSERT INTO lotacao (unidade_id, contato_id, desde) VALUES
  (1, 1, DATE '2023-02-01'),   -- Ana no TJGO
  (1, 4, DATE '2024-03-15'),   -- João no TJGO
  (2, 5, DATE '2022-08-10'),   -- Fernanda na VEP
  (3, 3, DATE '2021-05-20'),   -- Mariana no Presídio de Aparecida
  (4, 3, DATE '2024-01-10'),   -- Mariana também no Complexo (mesma pessoa, 2 unidades)
  (4, 2, DATE '2020-11-01'),   -- Carlos no Complexo
  (5, 6, DATE '2023-09-05');   -- Roberto na Casa de Prisão Provisória

-- 5) TELEFONES (RF05). Arco exclusivo: cada linha tem unidade_id OU contato_id.
--    Central de Plantão (unidade 6) tem 2 telefones (CA03/CA06).
INSERT INTO telefone (id, numero, tipo, unidade_id, contato_id) VALUES
  (1, '556232090000',  'PLANTAO',  6, NULL),   -- Central de Plantão (fixo)
  (2, '5562999990000', 'WHATSAPP', 6, NULL),   -- Central de Plantão (whatsapp)
  (3, '556232161000',  'FIXO',     1, NULL),   -- TJGO
  (4, '5562999991111', 'CELULAR',  3, NULL),   -- Presídio de Aparecida
  (5, '5562999992222', 'CELULAR',  NULL, 3),   -- Mariana (celular)
  (6, '556232234567',  'FIXO',     NULL, 3),   -- Mariana (fixo) — 2 números (CA06)
  (7, '5562988887777', 'CELULAR',  NULL, 1),   -- Ana
  (8, '5562988886666', 'WHATSAPP', NULL, 2);   -- Carlos

-- 6) E-MAILS (RF06). Central de Plantão (unidade 6) e Mariana (contato 3) têm 2 cada (CA06).
INSERT INTO email (id, endereco, tipo, unidade_id, contato_id) VALUES
  (1, 'plantao@tjgo.jus.br',          'institucional', 6, NULL),
  (2, 'plantao.urgencia@tjgo.jus.br', 'institucional', 6, NULL),
  (3, 'contato@tjgo.jus.br',          'institucional', 1, NULL),
  (4, 'presidio.aparecida@dgap.go.gov.br', NULL,       3, NULL),
  (5, 'mariana.lima@tjgo.jus.br',     'institucional', NULL, 3),
  (6, 'mariana.castro@gmail.com',     'pessoal',       NULL, 3),
  (7, 'ana.ribeiro@tjgo.jus.br',      'institucional', NULL, 1),
  (8, 'carlos.souza@dgap.go.gov.br',  NULL,            NULL, 2);

-- 7) Reposiciona as sequências SERIAL após inserts com id explícito
--    (senão o próximo INSERT da aplicação colidiria com ids já usados).
SELECT setval('unidade_id_seq',  (SELECT max(id) FROM unidade));
SELECT setval('contato_id_seq',  (SELECT max(id) FROM contato));
SELECT setval('telefone_id_seq', (SELECT max(id) FROM telefone));
SELECT setval('email_id_seq',    (SELECT max(id) FROM email));
