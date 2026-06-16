package br.jus.tjgo.agenda.dao;

import br.jus.tjgo.agenda.model.Telefone;
import br.jus.tjgo.agenda.model.TipoTelefone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** DAO da tabela {@code telefone} — arco exclusivo garantido por CHECK no banco (RF05). */
public class TelefoneDAO {

    public int inserir(Telefone t) throws SQLException {
        String sql = "INSERT INTO telefone (numero, tipo, unidade_id, contato_id) VALUES (?, ?, ?, ?)";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getNumero());
            ps.setString(2, t.getTipo().name());
            setNullableInt(ps, 3, t.getUnidadeId());
            setNullableInt(ps, 4, t.getContatoId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<Telefone> listarPorUnidade(int unidadeId) throws SQLException {
        return listar("unidade_id", unidadeId);
    }

    public List<Telefone> listarPorContato(int contatoId) throws SQLException {
        return listar("contato_id", contatoId);
    }

    public void remover(int id) throws SQLException {
        String sql = "DELETE FROM telefone WHERE id = ?";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Telefone id=" + id + " não encontrado.");
            }
        }
    }

    private List<Telefone> listar(String coluna, int donoId) throws SQLException {
        String sql = "SELECT * FROM telefone WHERE " + coluna + " = ? ORDER BY id";
        List<Telefone> out = new ArrayList<>();
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

    private Telefone mapear(ResultSet rs) throws SQLException {
        Telefone t = new Telefone();
        t.setId(rs.getInt("id"));
        t.setNumero(rs.getString("numero"));
        t.setTipo(TipoTelefone.valueOf(rs.getString("tipo")));
        int uni = rs.getInt("unidade_id");
        t.setUnidadeId(rs.wasNull() ? null : uni);
        int ct = rs.getInt("contato_id");
        t.setContatoId(rs.wasNull() ? null : ct);
        t.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return t;
    }
}
