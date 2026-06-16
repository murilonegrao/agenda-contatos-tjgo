package br.jus.tjgo.agenda.dao;

import br.jus.tjgo.agenda.model.Contato;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO da tabela {@code contato} — JDBC puro com PreparedStatement. */
public class ContatoDAO {

    public int inserir(Contato ct) throws SQLException {
        String sql = "INSERT INTO contato (nome, cargo) VALUES (?, ?)";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ct.getNome());
            ps.setString(2, ct.getCargo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Contato buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM contato WHERE id = ?";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Contato> listar() throws SQLException {
        String sql = "SELECT * FROM contato ORDER BY nome";
        List<Contato> out = new ArrayList<>();
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(mapear(rs));
            }
        }
        return out;
    }

    public void atualizar(Contato ct) throws SQLException {
        String sql = "UPDATE contato SET nome = ?, cargo = ? WHERE id = ?";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ct.getNome());
            ps.setString(2, ct.getCargo());
            ps.setInt(3, ct.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Contato id=" + ct.getId() + " não encontrado.");
            }
        }
    }

    public void remover(int id) throws SQLException {
        String sql = "DELETE FROM contato WHERE id = ?";
        try (Connection c = ConexaoFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Contato id=" + id + " não encontrado.");
            }
        }
    }

    private Contato mapear(ResultSet rs) throws SQLException {
        Contato ct = new Contato();
        ct.setId(rs.getInt("id"));
        ct.setNome(rs.getString("nome"));
        ct.setCargo(rs.getString("cargo"));
        ct.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return ct;
    }
}
