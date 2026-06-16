---
title: "ADR-001 — Stack: Java + JDBC + PostgreSQL + Maven"
type: adr
tags: [projeto, pi2a, adr, arquitetura, stack, java, postgresql, agenda-tjgo]
status: aceito
criado: 2026-06-15
atualizado: 2026-06-15
---

> [!info] Onde isto se encaixa
> ADR de decisão de arquitetura. Cobre o **núcleo Java + console**. Hub em [README](README.md).

> [!note] GUI fora de escopo
> A proposta da disciplina exige **menu de console** — por isso a entrega avaliada é o console (sem GUI).

# ADR-001 — Stack do projeto

**Status:** aceito · **Data:** 2026-06-15

## Contexto

Projeto acadêmico **individual** (Projeto Integrador II-A, PUC Goiás) — um CRUD de cadastro de contatos do TJGO, entregue até **21/06/2026**. A nota depende de o professor **rodar o sistema na máquina dele sem dor de dependência** e de o **`dump.sql` rodar de primeira** (aulas síncronas 1 e 3). A proposta original é uma "Agenda Telefônica" CRUD em Java, com **menu de console** (não GUI), e permite explicitamente **MySQL ou PostgreSQL**.

Atributos de qualidade priorizados (rigor de projeto pequeno):
1. **Reprodutibilidade** — rodar em máquina limpa sem ajuste manual.
2. **Aderência à proposta** — Java, console, BD relacional, CRUD com relacionamento entre tabelas.
3. **Simplicidade** — mínimo de partes móveis para um trabalho individual no prazo.

## Decisão

| Camada | Escolha |
|--------|---------|
| Linguagem | **Java 17+** |
| Acesso a dados | **JDBC puro** (driver `org.postgresql`), sem ORM |
| Banco | **PostgreSQL 18** |
| Build | **Maven** (abre nativo no NetBeans e resolve o driver automaticamente) |
| IDE | **NetBeans** |
| Estrutura | pacotes `model` / `dao` / `app` + `MenuConsole` (menu de texto) — ver [v0.1-classes](v0.1-classes.md) |

**Por que PostgreSQL:** preferência pessoal + o professor declarou em aula que usa Postgres com os alunos (*"eu vou usar o Postgres... entra no postgresql.org"* — aula 2) + a proposta permite MySQL ou PostgreSQL.

**Por que Maven:** resolve o driver JDBC via `pom.xml` automaticamente — o professor faz `git clone` e o build baixa a dependência, atendendo o RNF01 (rodar em máquina limpa). Evita o vexame de "faltou o `.jar` do driver no classpath".

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|-------------|------------------------|
| **MySQL** | Permitido pela proposta, mas preferência pessoal por Postgres + o professor usa Postgres com a turma. |
| **JPA / Hibernate** | Overhead de configuração e risco de setup (mappings, dialect, versões) — contra o objetivo de rodar de primeira na máquina do professor. JDBC puro é mais transparente e didático para um CRUD. |
| **Swing / GUI** | Fora de escopo — a proposta pede **menu de console**. GUI consumiria tempo sem pontuar. |
| **4 tabelas separadas** de telefone/e-mail (telefone_unidade, telefone_contato, ...) | Mais verboso e redundante que o **arco exclusivo com CHECK** (uma tabela `telefone` e uma `email`, cada linha com exatamente um dono). Ver [v0.1-data-model](v0.1-data-model.md). |
| **Gradle** | Maven abre nativo no NetBeans sem plugin extra; menos atrito para o ambiente da disciplina. |
| **SQLite / banco embarcado** | Não exige instalar SGBD, mas foge da proposta (relacionamento entre tabelas, dump SQL de SGBD relacional) e do que o professor avalia. |

## Consequências

**Positivas**
- Setup reproduzível: `git clone` → `mvn` resolve tudo → run.
- JDBC puro deixa o SQL visível — bom para explicar "pra dev novato" no vídeo.
- Aderência total à proposta (console, CRUD, relacionamento entre tabelas, dump `.sql`).

**Negativas (aceitas)**
- JDBC puro tem mais boilerplate que um ORM (abrir/fechar conexão, mapear `ResultSet` na mão) — aceitável e até didático no escopo.
- PostgreSQL precisa estar instalado na máquina-alvo — mitigado pelo passo-a-passo do README e pelo `dump.sql` testado do zero (Fase 3).
- Credenciais de conexão ficam num ponto único (`ConexaoFactory`) — documentar no README como ajustar.
