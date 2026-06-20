---
title: "ADR-001 — Stack: Java + JDBC + PostgreSQL + Maven"
type: adr
tags: [agenda-tjgo, adr, arquitetura, stack, java, postgresql]
status: aceito
criado: 2026-06-15
atualizado: 2026-06-20
---

> [!info] Onde isto se encaixa
> Registro da decisão de arquitetura (ADR) do núcleo Java + console. Visão geral em [README](README.md).

> [!note] Sem GUI
> A interface da v0.1 é um **menu de console** — não há GUI. A camada de serviço fica isolada para permitir outras interfaces no futuro, se necessário.

# ADR-001 — Stack do projeto

**Status:** aceito · **Data:** 2026-06-15

## Contexto

A aplicação é um CRUD de cadastro de contatos do TJGO (unidades, pessoas, telefones, e-mails) com relacionamento entre tabelas. Dois objetivos guiam a escolha de stack:

1. **Reprodutibilidade** — qualquer pessoa deve conseguir clonar o repositório e rodar em máquina limpa, sem ajuste manual de dependências.
2. **Simplicidade** — o mínimo de partes móveis para um projeto pequeno, mantido por uma pessoa.

Atributos de qualidade priorizados:
1. **Reprodutibilidade** — rodar em máquina limpa sem configuração extra.
2. **Aderência ao domínio** — Java, console, banco relacional, CRUD com relacionamento entre tabelas.
3. **Simplicidade** — poucas dependências, build direto.

## Decisão

| Camada | Escolha |
|--------|---------|
| Linguagem | **Java 17+** |
| Acesso a dados | **JDBC puro** (driver `org.postgresql`), sem ORM |
| Banco | **PostgreSQL 18** |
| Build | **Maven** (resolve o driver automaticamente e abre nativo no NetBeans) |
| IDE | **NetBeans** (qualquer IDE com suporte a Maven serve) |
| Estrutura | pacotes `model` / `dao` / `service` + `app.MenuConsole` — ver [v0.1-classes](v0.1-classes.md) |

**Por que PostgreSQL:** banco relacional maduro, gratuito e amplamente usado; o `dump.sql` em `psql` é simples de distribuir e reexecutar. MySQL atenderia igualmente bem; Postgres foi preferência.

**Por que Maven:** resolve o driver JDBC via `pom.xml` automaticamente — um `git clone` seguido de `mvn` baixa a dependência sozinho (RNF01, rodar em máquina limpa). Evita o clássico problema de "faltou o `.jar` do driver no classpath".

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|-------------|------------------------|
| **MySQL** | Atenderia bem; Postgres foi escolhido por preferência e pela facilidade do `dump.sql` autocontido. |
| **JPA / Hibernate** | Overhead de configuração e risco de setup (mappings, dialect, versões) — contra o objetivo de rodar de primeira. JDBC puro é mais transparente e didático para um CRUD pequeno. |
| **Swing / GUI** | Fora de escopo — a v0.1 é um menu de console. GUI consumiria tempo sem agregar ao núcleo. |
| **4 tabelas separadas** de telefone/e-mail (telefone_unidade, telefone_contato, ...) | Mais verboso e redundante que o **arco exclusivo com CHECK** (uma tabela `telefone` e uma `email`, cada linha com exatamente um dono). Ver [v0.1-data-model](v0.1-data-model.md). |
| **Gradle** | Maven abre nativo no NetBeans sem plugin extra; menos atrito de setup. |
| **SQLite / banco embarcado** | Dispensaria instalar um SGBD, mas foge do objetivo de demonstrar relacionamento entre tabelas e dump SQL de um SGBD relacional. |

## Consequências

**Positivas**
- Setup reproduzível: `git clone` → `mvn` resolve tudo → run.
- JDBC puro deixa o SQL visível — fácil de ler e entender o que cada operação faz.
- Aderência ao domínio (console, CRUD, relacionamento entre tabelas, dump `.sql`).

**Negativas (aceitas)**
- JDBC puro tem mais boilerplate que um ORM (abrir/fechar conexão, mapear `ResultSet` na mão) — aceitável e até didático no escopo.
- PostgreSQL precisa estar instalado na máquina-alvo — mitigado pelo passo a passo do [console/README](console/README.md) e pelo `dump.sql` testado do zero.
- Credenciais de conexão ficam num ponto único (`ConexaoFactory`) — documentado no README como ajustar.
