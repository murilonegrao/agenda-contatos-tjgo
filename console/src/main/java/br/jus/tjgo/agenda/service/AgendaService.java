package br.jus.tjgo.agenda.service;

import br.jus.tjgo.agenda.dao.ConexaoFactory;
import br.jus.tjgo.agenda.dao.ContatoDAO;
import br.jus.tjgo.agenda.dao.EmailDAO;
import br.jus.tjgo.agenda.dao.LotacaoDAO;
import br.jus.tjgo.agenda.dao.TelefoneDAO;
import br.jus.tjgo.agenda.dao.UnidadeDAO;
import br.jus.tjgo.agenda.model.Contato;
import br.jus.tjgo.agenda.model.Email;
import br.jus.tjgo.agenda.model.ResultadoBusca;
import br.jus.tjgo.agenda.model.Telefone;
import br.jus.tjgo.agenda.model.Unidade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Camada de regras de negócio (RF08): orquestra os DAOs, valida entradas e
 * traduz {@link SQLException} em {@link AgendaException} com mensagem amigável.
 *
 * <p>É o <b>ponto único de lógica</b> reusado pelo console e (na v0.2) pela API web —
 * por isso a validação mora aqui, não no {@code MenuConsole}.
 */
public class AgendaService {

    private final UnidadeDAO unidadeDAO = new UnidadeDAO();
    private final ContatoDAO contatoDAO = new ContatoDAO();
    private final LotacaoDAO lotacaoDAO = new LotacaoDAO();
    private final TelefoneDAO telefoneDAO = new TelefoneDAO();
    private final EmailDAO emailDAO = new EmailDAO();

    // ----------------------------------------------------------------- Unidade

    public int salvarUnidade(Unidade u) {
        validarTextoObrigatorio(u.getNome(), "nome da unidade");
        if (u.getTipo() == null) {
            throw new AgendaException("Informe o tipo da unidade.");
        }
        try {
            if (u.getId() == 0) {
                return unidadeDAO.inserir(u);
            }
            unidadeDAO.atualizar(u);
            return u.getId();
        } catch (SQLException e) {
            throw traduzir(e, "salvar unidade");
        }
    }

    public Unidade buscarUnidade(int id) {
        try {
            Unidade u = unidadeDAO.buscarPorId(id);
            if (u == null) {
                throw new AgendaException("Unidade id=" + id + " não encontrada.");
            }
            return u;
        } catch (SQLException e) {
            throw traduzir(e, "buscar unidade");
        }
    }

    public List<Unidade> listarUnidades() {
        try {
            return unidadeDAO.listar();
        } catch (SQLException e) {
            throw traduzir(e, "listar unidades");
        }
    }

    public void removerUnidade(int id) {
        try {
            unidadeDAO.remover(id);
        } catch (SQLException e) {
            throw traduzir(e, "remover unidade");
        }
    }

    /** Define / troca / remove (responsavelId = null) o responsável da unidade (RF04). */
    public void definirResponsavel(int unidadeId, Integer responsavelId) {
        try {
            unidadeDAO.definirResponsavel(unidadeId, responsavelId);
        } catch (SQLException e) {
            throw traduzir(e, "definir responsável");
        }
    }

    // ----------------------------------------------------------------- Contato

    public int salvarContato(Contato ct) {
        validarTextoObrigatorio(ct.getNome(), "nome do contato");
        try {
            if (ct.getId() == 0) {
                return contatoDAO.inserir(ct);
            }
            contatoDAO.atualizar(ct);
            return ct.getId();
        } catch (SQLException e) {
            throw traduzir(e, "salvar contato");
        }
    }

    public Contato buscarContato(int id) {
        try {
            Contato ct = contatoDAO.buscarPorId(id);
            if (ct == null) {
                throw new AgendaException("Contato id=" + id + " não encontrado.");
            }
            return ct;
        } catch (SQLException e) {
            throw traduzir(e, "buscar contato");
        }
    }

    public List<Contato> listarContatos() {
        try {
            return contatoDAO.listar();
        } catch (SQLException e) {
            throw traduzir(e, "listar contatos");
        }
    }

    public void removerContato(int id) {
        try {
            contatoDAO.remover(id);
        } catch (SQLException e) {
            throw traduzir(e, "remover contato");
        }
    }

    // ----------------------------------------------------------------- Lotação

    public void vincularLotacao(int unidadeId, int contatoId) {
        try {
            lotacaoDAO.vincular(unidadeId, contatoId);
        } catch (SQLException e) {
            throw traduzir(e, "vincular lotação");
        }
    }

    public void desvincularLotacao(int unidadeId, int contatoId) {
        try {
            lotacaoDAO.desvincular(unidadeId, contatoId);
        } catch (SQLException e) {
            throw traduzir(e, "desvincular lotação");
        }
    }

    public List<Contato> contatosDaUnidade(int unidadeId) {
        try {
            return lotacaoDAO.listarContatosDaUnidade(unidadeId);
        } catch (SQLException e) {
            throw traduzir(e, "listar contatos da unidade");
        }
    }

    public List<Unidade> unidadesDoContato(int contatoId) {
        try {
            return lotacaoDAO.listarUnidadesDoContato(contatoId);
        } catch (SQLException e) {
            throw traduzir(e, "listar unidades do contato");
        }
    }

    // ----------------------------------------------------------- Telefone / Email

    public int addTelefone(Telefone t) {
        validarArcoExclusivo(t.getUnidadeId(), t.getContatoId(), "telefone");
        validarNumero(t.getNumero());
        if (t.getTipo() == null) {
            throw new AgendaException("Informe o tipo do telefone.");
        }
        try {
            return telefoneDAO.inserir(t);
        } catch (SQLException e) {
            throw traduzir(e, "adicionar telefone");
        }
    }

    public List<Telefone> telefonesDaUnidade(int unidadeId) {
        try {
            return telefoneDAO.listarPorUnidade(unidadeId);
        } catch (SQLException e) {
            throw traduzir(e, "listar telefones da unidade");
        }
    }

    public List<Telefone> telefonesDoContato(int contatoId) {
        try {
            return telefoneDAO.listarPorContato(contatoId);
        } catch (SQLException e) {
            throw traduzir(e, "listar telefones do contato");
        }
    }

    public void removerTelefone(int id) {
        try {
            telefoneDAO.remover(id);
        } catch (SQLException e) {
            throw traduzir(e, "remover telefone");
        }
    }

    public int addEmail(Email e) {
        validarArcoExclusivo(e.getUnidadeId(), e.getContatoId(), "e-mail");
        validarTextoObrigatorio(e.getEndereco(), "endereço de e-mail");
        if (!e.getEndereco().contains("@")) {
            throw new AgendaException("E-mail inválido: falta '@' em \"" + e.getEndereco() + "\".");
        }
        try {
            return emailDAO.inserir(e);
        } catch (SQLException ex) {
            throw traduzir(ex, "adicionar e-mail");
        }
    }

    public List<Email> emailsDaUnidade(int unidadeId) {
        try {
            return emailDAO.listarPorUnidade(unidadeId);
        } catch (SQLException e) {
            throw traduzir(e, "listar e-mails da unidade");
        }
    }

    public List<Email> emailsDoContato(int contatoId) {
        try {
            return emailDAO.listarPorContato(contatoId);
        } catch (SQLException e) {
            throw traduzir(e, "listar e-mails do contato");
        }
    }

    public void removerEmail(int id) {
        try {
            emailDAO.remover(id);
        } catch (SQLException e) {
            throw traduzir(e, "remover e-mail");
        }
    }

    // ------------------------------------------------------------------- Busca

    /** Busca por nome (RF07) — unidades e contatos via {@code ILIKE '%termo%'}. */
    public List<ResultadoBusca> buscarPorNome(String termo) {
        if (termo == null || termo.isBlank()) {
            throw new AgendaException("Informe um termo de busca.");
        }
        String sql = """
                SELECT id, nome, 'UNIDADE' AS origem FROM unidade WHERE nome ILIKE ?
                UNION ALL
                SELECT id, nome, 'CONTATO' AS origem FROM contato WHERE nome ILIKE ?
                ORDER BY origem, nome""";
        String like = "%" + termo.trim() + "%";
        List<ResultadoBusca> out = new ArrayList<>();
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new ResultadoBusca(
                            rs.getInt("id"), rs.getString("nome"), rs.getString("origem")));
                }
            }
        } catch (SQLException e) {
            throw traduzir(e, "buscar por nome");
        }
        return out;
    }

    // ------------------------------------------------------------- Validações

    private void validarTextoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new AgendaException("O campo \"" + campo + "\" é obrigatório.");
        }
    }

    /** RN01: telefone/e-mail tem exatamente um dono — espelha o CHECK, mas com mensagem clara (CA07). */
    private void validarArcoExclusivo(Integer unidadeId, Integer contatoId, String entidade) {
        boolean temUnidade = unidadeId != null;
        boolean temContato = contatoId != null;
        if (temUnidade == temContato) {
            throw new AgendaException("O " + entidade
                    + " deve pertencer a EXATAMENTE um dono: uma unidade OU um contato.");
        }
    }

    /** RN08: número é só dígitos com DDI (8 a 15 dígitos). */
    private void validarNumero(String numero) {
        if (numero == null || !numero.matches("^[0-9]{8,15}$")) {
            throw new AgendaException("Número inválido: use apenas dígitos com DDI "
                    + "(ex.: 5562999999999), 8 a 15 dígitos. Recebido: \"" + numero + "\".");
        }
    }

    /**
     * Traduz uma {@link SQLException} em mensagem de domínio. Usa o SQLState do PostgreSQL
     * para reconhecer os erros mais comuns (FK, CHECK, PK duplicada) — RF08 / CA07 / CA09.
     */
    private AgendaException traduzir(SQLException e, String operacao) {
        String estado = e.getSQLState();
        String msg = switch (estado == null ? "" : estado) {
            case "23505" -> "Registro duplicado: esse vínculo/valor já existe.";
            case "23503" -> "Operação viola uma referência (registro relacionado inexistente "
                    + "ou ainda em uso).";
            case "23514" -> "Valor rejeitado por uma regra do banco (CHECK): "
                    + "verifique tipo, formato do número ou o dono do telefone/e-mail.";
            case "23502" -> "Falta um campo obrigatório (NOT NULL).";
            case "08001", "08006", "08003" ->
                    "Não foi possível conectar ao PostgreSQL. Confira se o banco está no ar "
                    + "e as credenciais em db.properties (ou variáveis AGENDA_DB_*).";
            default -> "Falha ao " + operacao + ": " + e.getMessage();
        };
        return new AgendaException(msg, e);
    }
}
