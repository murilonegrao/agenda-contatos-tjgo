package br.jus.tjgo.agenda.model;

import java.time.LocalDateTime;

/**
 * POJO de domínio — espelha a tabela {@code email}.
 * Mesmo arco exclusivo do telefone; {@code tipo} é opcional (pode ser NULL).
 */
public class Email {
    private int id;
    private String endereco;
    private String tipo;        // opcional
    private Integer unidadeId;  // wrapper: um dos dois é NULL (arco exclusivo)
    private Integer contatoId;
    private LocalDateTime criadoEm;

    public Email() {
    }

    public Email(String endereco, String tipo) {
        this.endereco = endereco;
        this.tipo = tipo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getUnidadeId() { return unidadeId; }
    public void setUnidadeId(Integer unidadeId) { this.unidadeId = unidadeId; }

    public Integer getContatoId() { return contatoId; }
    public void setContatoId(Integer contatoId) { this.contatoId = contatoId; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    @Override
    public String toString() {
        return "[%d] %s%s".formatted(
                id, endereco,
                tipo == null || tipo.isBlank() ? "" : " (" + tipo + ")");
    }
}
