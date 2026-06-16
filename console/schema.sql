-- ============================================================================
--  Agenda de Contatos TJGO — schema.sql (somente DDL)  ·  PostgreSQL 18
--  Cria as 5 tabelas de domínio + índices. Idempotente: pode rodar várias vezes.
--
--  Uso:  createdb agenda
--        psql -d agenda -f schema.sql
--
--  Para schema + dados de exemplo, use dump.sql (autocontido).
--  Ordem: unidade -> contato -> (ALTER FK responsável) -> lotacao -> telefone -> email
-- ============================================================================

-- Reexecução limpa (RNF02 — "dou F5 e não pode dar erro"). CASCADE derruba as FKs.
DROP TABLE IF EXISTS email    CASCADE;
DROP TABLE IF EXISTS telefone CASCADE;
DROP TABLE IF EXISTS lotacao  CASCADE;
DROP TABLE IF EXISTS contato  CASCADE;
DROP TABLE IF EXISTS unidade  CASCADE;

-- 1) UNIDADE — órgão / unidade prisional / outro ponto de contato.
--    FK do responsável é adicionada depois (referência circular com contato).
CREATE TABLE unidade (
    id             SERIAL       PRIMARY KEY,
    nome           VARCHAR(120) NOT NULL,
    tipo           VARCHAR(20)  NOT NULL
                   CHECK (tipo IN ('UNIDADE_PRISIONAL', 'ORGAO', 'OUTRO')),
    endereco       VARCHAR(200),
    responsavel_id INTEGER,                       -- FK adicionada via ALTER abaixo
    criado_em      TIMESTAMP    NOT NULL DEFAULT now()
);

-- 2) CONTATO — pessoa ligada a unidades. Lotação é N:N (tabela lotacao).
CREATE TABLE contato (
    id        SERIAL       PRIMARY KEY,
    nome      VARCHAR(120) NOT NULL,
    cargo     VARCHAR(80),
    criado_em TIMESTAMP    NOT NULL DEFAULT now()
);

-- 3) FK do responsável (arco unidade -> contato), agora que CONTATO existe.
ALTER TABLE unidade
    ADD CONSTRAINT fk_unidade_responsavel
    FOREIGN KEY (responsavel_id) REFERENCES contato(id) ON DELETE SET NULL;

-- 4) LOTACAO — entidade associativa do N:N unidade <-> contato.
--    PK composta evita duplicar o mesmo vínculo; 'desde' é o dado do relacionamento.
CREATE TABLE lotacao (
    unidade_id INTEGER NOT NULL REFERENCES unidade(id) ON DELETE CASCADE,
    contato_id INTEGER NOT NULL REFERENCES contato(id) ON DELETE CASCADE,
    desde      DATE    NOT NULL DEFAULT current_date,
    PRIMARY KEY (unidade_id, contato_id)
);

-- 5) TELEFONE — pertence a EXATAMENTE uma unidade OU um contato (arco exclusivo).
--    numero = só dígitos com DDI (ex.: 5562999999999); máscara fica na apresentação.
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

-- 6) EMAIL — mesmo arco exclusivo; tipo opcional.
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

-- 7) Índices: FKs (joins) + nome em minúsculo (busca ILIKE).
CREATE INDEX idx_lotacao_contato     ON lotacao(contato_id);
CREATE INDEX idx_unidade_responsavel ON unidade(responsavel_id);
CREATE INDEX idx_telefone_unidade    ON telefone(unidade_id);
CREATE INDEX idx_telefone_contato    ON telefone(contato_id);
CREATE INDEX idx_email_unidade       ON email(unidade_id);
CREATE INDEX idx_email_contato       ON email(contato_id);
CREATE INDEX idx_unidade_nome        ON unidade(lower(nome));
CREATE INDEX idx_contato_nome        ON contato(lower(nome));
