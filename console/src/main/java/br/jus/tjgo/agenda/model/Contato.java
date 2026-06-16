package br.jus.tjgo.agenda.model;

import java.time.LocalDateTime;

/**
 * POJO de domínio — espelha a tabela {@code contato}.
 * Não carrega mais {@code unidadeId}: a lotação virou N:N (ver {@link Lotacao}).
 */
public class Contato {
    private int id;
    private String nome;
    private String cargo;
    private LocalDateTime criadoEm;

    public Contato() {
    }

    public Contato(String nome, String cargo) {
        this.nome = nome;
        this.cargo = cargo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    @Override
    public String toString() {
        return "[%d] %s%s".formatted(
                id, nome,
                cargo == null || cargo.isBlank() ? "" : " — " + cargo);
    }
}
