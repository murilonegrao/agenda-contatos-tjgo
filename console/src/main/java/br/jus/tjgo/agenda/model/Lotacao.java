package br.jus.tjgo.agenda.model;

import java.time.LocalDate;

/**
 * Entidade associativa do N:N {@code unidade ↔ contato} — espelha a tabela {@code lotacao}.
 * PK composta (unidadeId, contatoId); {@code desde} é o atributo do relacionamento.
 */
public class Lotacao {
    private int unidadeId;
    private int contatoId;
    private LocalDate desde;

    public Lotacao() {
    }

    public Lotacao(int unidadeId, int contatoId, LocalDate desde) {
        this.unidadeId = unidadeId;
        this.contatoId = contatoId;
        this.desde = desde;
    }

    public int getUnidadeId() { return unidadeId; }
    public void setUnidadeId(int unidadeId) { this.unidadeId = unidadeId; }

    public int getContatoId() { return contatoId; }
    public void setContatoId(int contatoId) { this.contatoId = contatoId; }

    public LocalDate getDesde() { return desde; }
    public void setDesde(LocalDate desde) { this.desde = desde; }
}
