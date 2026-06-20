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
 1) Unidades   (CRUD)
 2) Contatos   (CRUD)
 3) Lotação    (vincular/desvincular contato ↔ unidade — N:N)
 4) Responsável de unidade (definir/trocar/remover)
 5) Telefones  (adicionar/listar/remover)
 6) E-mails    (adicionar/listar/remover)
 7) Buscar por nome
 0) Sair
```

Os dados do `dump.sql` já cobrem os critérios de aceite (`../v0.1-spec.md`):

| Critério | Como conferir no menu |
|----------|------------------------|
| **CA03** — unidade contactável com ZERO contatos | Opção 5/6 → listar da unidade **6** (Central de Plantão): tem telefones e e-mails, sem contatos |
| **CA04** — mesmo contato em 2 unidades | Opção 3 → "unidades do contato" **3** (Mariana): aparece nas unidades 3 e 4 |
| **CA05** — mesmo contato responsável por 2 unidades | Opção 1 → listar: unidades 3 e 4 têm Mariana como responsável |
| **CA06** — ≥ 2 telefones e ≥ 2 e-mails | Unidade 6 e contato 3 |
| **CA07** — arco exclusivo rejeitado | Tente um telefone sem dono → mensagem clara, sem stack trace |
| **CA08** — busca cruza unidades e contatos | Opção 7 → digite `ar` (volta 2 unidades + 2 contatos numa só busca) |
| **CA09** — id inexistente | Editar/remover um id que não existe → aviso amigável |

---

## Solução de problemas

| Sintoma | Causa provável | Ação |
|---------|----------------|------|
| `Não foi possível conectar ao PostgreSQL` | serviço parado ou credenciais erradas | inicie o Postgres; confira `db.properties` / `AGENDA_DB_*` |
| `database "agenda" does not exist` | banco não criado | rode `createdb agenda` (passo 2) |
| `password authentication failed` | senha do `postgres` diferente | ajuste `db.password` |
| `relation "unidade" does not exist` | dump não carregado | rode `psql -d agenda -f dump.sql` |
