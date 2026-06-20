# Agenda de Contatos TJGO — Console (v0.1)

CRUD de **unidades**, **contatos**, **telefones**, **e-mails** e **lotação N:N** do TJGO.
Java 17 + JDBC puro + PostgreSQL 18, build Maven.

Arquitetura em camadas (ver `../v0.1-classes.md`):

```
br.jus.tjgo.agenda
├── model      POJOs de domínio (Unidade, Contato, Lotacao, Telefone, Email, enums)
├── dao        Data Access Objects — JDBC puro com PreparedStatement + ConexaoFactory
├── service    AgendaService — regras de negócio, busca, tradução de erros (RF08)
└── app        MenuConsole — a main(), menu de texto
```

---

## Pré-requisitos

| Ferramenta | Versão | Observação |
|------------|--------|------------|
| **JDK**    | 17+    | `java -version` |
| **Maven**  | 3.8+   | `mvn -version` (resolve o driver JDBC sozinho) |
| **PostgreSQL** | 18  | único serviço externo a instalar |

> O driver `org.postgresql` é baixado pelo Maven via `pom.xml` — **não** precisa instalar `.jar` no classpath.

---

## Passo a passo (máquina limpa, ~15 min)

### 1. Instalar o PostgreSQL
- Linux (Debian/Ubuntu): `sudo apt install postgresql`
- Windows/macOS: instalador em <https://www.postgresql.org/download/>

### 2. Criar o banco e carregar os dados
```bash
# cria o banco vazio (rode como usuário com permissão; ex.: 'sudo -u postgres' no Linux)
createdb agenda

# carrega schema + dados de exemplo (autocontido, roda de primeira)
psql -d agenda -f dump.sql
```
> Só o esquema, sem dados: `psql -d agenda -f schema.sql`.
> Ambos os scripts são **idempotentes** — pode rodar de novo sem erro (fazem `DROP` antes).

### 3. Configurar a conexão
Edite `src/main/resources/db.properties` se suas credenciais forem diferentes do padrão:
```properties
db.url=jdbc:postgresql://localhost:5432/agenda
db.user=postgres
db.password=postgres
```
Alternativa sem editar arquivo (têm prioridade sobre o `.properties`):
```bash
export AGENDA_DB_URL="jdbc:postgresql://localhost:5432/agenda"
export AGENDA_DB_USER="postgres"
export AGENDA_DB_PASSWORD="suaSenha"
```

### 4. Rodar a aplicação
Pelo Maven (mais simples):
```bash
mvn -q exec:java
```
Ou gerando o `.jar` executável (com o driver embutido):
```bash
mvn -q package
java -jar target/agenda-console.jar
```

---

## O que dá pra fazer no menu

```
 1) Unidades   (CRUD + telefones/e-mails)
 2) Contatos   (CRUD + telefones/e-mails)
 3) Lotação    (vincular/desvincular contato ↔ unidade — N:N)
 4) Responsável de unidade (definir/trocar/remover)
 5) Buscar por nome
 0) Sair
```

> **Telefones/e-mails são atributos** da unidade e do contato — não têm menu próprio. Você os adiciona no **Cadastrar** (que exige ≥1 telefone e ≥1 e-mail) e os gerencia (adicionar/remover) dentro do **Editar** da unidade/contato.

> **Consulta sem decorar id:** as opções **Listar** (1/2) e **Buscar** (5) já mostram, de cada unidade/contato, os **telefones, e-mails, responsável e lotação**.

Os dados do `dump.sql` já cobrem os critérios de aceite (`../v0.1-spec.md`):

| Critério | Como conferir no menu |
|----------|------------------------|
| **CA03** — unidade contactável com ZERO contatos | Opção 1 → Listar: **Central de Plantão** tem telefones e e-mails, sem nenhum contato |
| **CA04** — mesmo contato em 2 unidades | Opção 2 → Listar: **Mariana** aparece "lotado em" as unidades 3 e 4 (ou Opção 3 → unidades do contato 3) |
| **CA05** — mesmo contato responsável por 2 unidades | Opção 1 → Listar: unidades 3 e 4 têm Mariana como responsável |
| **CA06** — ≥ 2 telefones e ≥ 2 e-mails | Opção 1/2 → Listar: **Central de Plantão** e a contato **Mariana** têm 2 de cada |
| **CA07** — arco exclusivo (um dono) | Garantido pelo CHECK do banco + validação no service; o menu sempre amarra exatamente um dono ao telefone/e-mail |
| **CA08** — busca cruza unidades e contatos | Opção 5 → digite `ar` (volta 2 unidades + 2 contatos numa só busca) |
| **CA09** — id inexistente | Editar/remover um id que não existe → aviso amigável |
| **CA10** — cadastro pede telefone e e-mail | Opção 1 ou 2 → **Cadastrar**: ao final exige ≥1 telefone e ≥1 e-mail antes de concluir |

---

## Solução de problemas

| Sintoma | Causa provável | Ação |
|---------|----------------|------|
| `Não foi possível conectar ao PostgreSQL` | serviço parado ou credenciais erradas | inicie o Postgres; confira `db.properties` / `AGENDA_DB_*` |
| `database "agenda" does not exist` | banco não criado | rode `createdb agenda` (passo 2) |
| `password authentication failed` | senha do `postgres` diferente | ajuste `db.password` |
| `relation "unidade" does not exist` | dump não carregado | rode `psql -d agenda -f dump.sql` |
