package br.jus.tjgo.agenda.dao;

import br.jus.tjgo.agenda.model.Contato;
import br.jus.tjgo.agenda.model.Unidade;
import br.jus.tjgo.agenda.model.TipoUnidade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO da entidade associativa {@code lotacao} (N:N unidade ↔ contato, RF03). */
public class LotacaoDAO {

    public void vincular(int unidadeId, int contatoId) throws SQLException {
        String sql = "INSERT INTO lotacao (unidade_id, contato_id) VALUES (?, ?)";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, unidadeId);
            ps.setInt(2, contatoId);
            ps.executeUpdate();
        }
    }

    public void desvincular(int unidadeId, int contatoId) throws SQLException {
        String sql = "DELETE FROM lotacao WHERE unidade_id = ? AND contato_id = ?";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, unidadeId);
            ps.setInt(2, contatoId);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Vínculo unidade=" + unidadeId
                        + " / contato=" + contatoId + " não existe.");
            }
        }
    }

    public List<Contato> listarContatosDaUnidade(int unidadeId) throws SQLException {
        String sql = """
                SELECT ct.* FROM contato ct
                JOIN lotacao l ON l.contato_id = ct.id
                WHERE l.unidade_id = ?
                ORDER BY ct.nome""";
        List<Contato> out = new ArrayList<>();
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, unidadeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Contato ct = new Contato();
                    ct.setId(rs.getInt("id"));
                    ct.setNome(rs.getString("nome"));
                    ct.setCargo(rs.getString("cargo"));
                    ct.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
                    out.add(ct);
                }
            }
        }
        return out;
    }

    public List<Unidade> listarUnidadesDoContato(int contatoId) throws SQLException {
        String sql = """
                SELECT u.* FROM unidade u
                JOIN lotacao l ON l.unidade_id = u.id
                WHERE l.contato_id = ?
                ORDER BY u.nome""";
        List<Unidade> out = new ArrayList<>();
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, contatoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Unidade u = new Unidade();
                    u.setId(rs.getInt("id"));
                    u.setNome(rs.getString("nome"));
                    u.setTipo(TipoUnidade.valueOf(rs.getString("tipo")));
                    u.setEndereco(rs.getString("endereco"));
                    int resp = rs.getInt("responsavel_id");
                    u.setResponsavelId(rs.wasNull() ? null : resp);
                    u.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
                    out.add(u);
                }
            }
        }
        return out;
    }
}
