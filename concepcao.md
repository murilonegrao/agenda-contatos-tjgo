---
title: "Concepção — Agenda de Contatos TJGO"
type: project-doc
tags: [projeto, pi2a, concepcao, visao, agenda-tjgo]
status: v0.1
criado: 2026-06-15
atualizado: 2026-06-15
---

> [!info] Onde isto se encaixa
> Etapa de **concepção** (antes dos requisitos): verifica *se o projeto deve acontecer* e fixa problema, ator e fronteira de escopo. Hub em [README](README.md).

# O problema

Hoje os contatos das **unidades prisionais e órgãos** com que o plantão do TJGO precisa falar vivem espalhados em **grupo de WhatsApp e numa planilha trocada a cada plantão**. Consequências:

- **Volátil** — a planilha é reenviada e versionada na mão; some, diverge, fica desatualizada.
- **Sem dono** — ninguém é responsável por manter; cada plantonista tem a "sua" versão.
- **Sem estrutura** — número de plantão, responsável pela unidade e contatos pessoais se misturam no mesmo campo de texto.

Quando estoura uma urgência no plantão, achar *o telefone certo da unidade certa* depende de sorte e de rolar histórico de WhatsApp.

# O ator

> [!note] Ator
> - **Operador de plantão (console):** servidor do TJGO que faz a manutenção do cadastro (CRUD completo) via menu de texto. É o perfil avaliado na disciplina.

# A visão

Um **cadastro estruturado** de contatos do plantão, numa aplicação **console Java sobre PostgreSQL**:

- **Console (Java)**: CRUD completo por menu de texto. É a entrega da disciplina (prazo 21/06).

Regras de domínio:

- Cada **unidade** (prisional, órgão ou outro ponto) é uma entidade própria, contactável **mesmo sem nenhuma pessoa cadastrada** (tem seus próprios telefones/e-mails).
- Cada **contato** (pessoa) pode estar lotado em **uma ou mais unidades** (N:N) e ser **responsável** por uma ou mais unidades.
- Telefones e e-mails são **normalizados** (uma unidade ou pessoa pode ter vários), cada um pertencendo a exatamente um dono.

O valor central: **encontrar rápido o contato certo de uma unidade**, com a informação num lugar estruturado e durável em vez de um print de WhatsApp.

# Fora de escopo

> [!danger] Lista OUT — escrita de propósito para evitar scope creep
> - **Importação** de planilha ou histórico de WhatsApp
> - Interface gráfica / web / app mobile (a entrega é por menu de console)
> - Relatórios, exportações, dashboards
> - Auditoria, soft delete, histórico de alterações

# Por que é viável

- Domínio pequeno e conhecido (4 entidades, 1 ator, CRUD).
- Stack dominada e exigida pela disciplina ([ADR-001-stack](ADR-001-stack.md)).
- Cabe folgado no prazo (entrega 21/06/2026) como projeto individual.
- Atende item a item o conteúdo programático: requisitos, casos de uso, BD lógico/físico, DML/CRUD e relacionamento entre tabelas.
