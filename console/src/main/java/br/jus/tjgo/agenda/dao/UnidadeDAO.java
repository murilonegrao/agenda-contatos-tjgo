package br.jus.tjgo.agenda.dao;

import br.jus.tjgo.agenda.model.TipoUnidade;
import br.jus.tjgo.agenda.model.Unidade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO da tabela {@code unidade} — JDBC puro com PreparedStatement. */
public class UnidadeDAO {

    public int inserir(Unidade u) throws SQLException {
        String sql = "INSERT INTO unidade (nome, tipo, endereco) VALUES (?, ?, ?)";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getTipo().name());
            ps.setString(3, u.getEndereco());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Unidade buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM unidade WHERE id = ?";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Unidade> listar() throws SQLException {
        String sql = "SELECT * FROM unidade ORDER BY nome";
        List<Unidade> out = new ArrayList<>();
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(mapear(rs));
            }
        }
        return out;
    }

    public void atualizar(Unidade u) throws SQLException {
        String sql = "UPDATE unidade SET nome = ?, tipo = ?, endereco = ? WHERE id = ?";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getTipo().name());
            ps.setString(3, u.getEndereco());
            ps.setInt(4, u.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Unidade id=" + u.getId() + " não encontrada.");
            }
        }
    }

    public void remover(int id) throws SQLException {
        String sql = "DELETE FROM unidade WHERE id = ?";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Unidade id=" + id + " não encontrada.");
            }
        }
    }

    /** Define, troca ou remove (responsavelId = null) o responsável da unidade (RF04). */
    public void definirResponsavel(int unidadeId, Integer responsavelId) throws SQLException {
        String sql = "UPDATE unidade SET responsavel_id = ? WHERE id = ?";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (responsavelId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, responsavelId);
            }
            ps.setInt(2, unidadeId);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Unidade id=" + unidadeId + " não encontrada.");
            }
        }
    }

    private Unidade mapear(ResultSet rs) throws SQLException {
        Unidade u = new Unidade();
        u.setId(rs.getInt("id"));
        u.setNome(rs.getString("nome"));
        u.setTipo(TipoUnidade.valueOf(rs.getString("tipo")));
        u.setEndereco(rs.getString("endereco"));
        int resp = rs.getInt("responsavel_id");
        u.setResponsavelId(rs.wasNull() ? null : resp);
        u.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return u;
    }
}
