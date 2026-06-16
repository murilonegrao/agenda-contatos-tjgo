package br.jus.tjgo.agenda.dao;

import br.jus.tjgo.agenda.model.Email;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** DAO da tabela {@code email} — mesmo arco exclusivo do telefone (RF06). */
public class EmailDAO {

    public int inserir(Email e) throws SQLException {
        String sql = "INSERT INTO email (endereco, tipo, unidade_id, contato_id) VALUES (?, ?, ?, ?)";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getEndereco());
            ps.setString(2, e.getTipo());
            setNullableInt(ps, 3, e.getUnidadeId());
            setNullableInt(ps, 4, e.getContatoId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<Email> listarPorUnidade(int unidadeId) throws SQLException {
        return listar("unidade_id", unidadeId);
    }

    public List<Email> listarPorContato(int contatoId) throws SQLException {
        return listar("contato_id", contatoId);
    }

    public void remover(int id) throws SQLException {
        String sql = "DELETE FROM email WHERE id = ?";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("E-mail id=" + id + " não encontrado.");
            }
        }
    }

    private List<Email> listar(String coluna, int donoId) throws SQLException {
        String sql = "SELECT * FROM email WHERE " + coluna + " = ? ORDER BY id";
        List<Email> out = new ArrayList<>();
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, donoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapear(rs));
                }
            }
        }
        return out;
    }

    private void setNullableInt(PreparedStatement ps, int idx, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, Types.INTEGER);
        } else {
            ps.setInt(idx, value);
        }
    }

    private Email mapear(ResultSet rs) throws SQLException {
        Email e = new Email();
        e.setId(rs.getInt("id"));
        e.setEndereco(rs.getString("endereco"));
        e.setTipo(rs.getString("tipo"));
        int uni = rs.getInt("unidade_id");
        e.setUnidadeId(rs.wasNull() ? null : uni);
        int ct = rs.getInt("contato_id");
        e.setContatoId(rs.wasNull() ? null : ct);
        e.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return e;
    }
}
