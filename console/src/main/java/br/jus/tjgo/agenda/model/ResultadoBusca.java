package br.jus.tjgo.agenda.model;

/**
 * Linha do resultado da busca por nome (RF07) — unifica unidades e contatos.
 * {@code origem} indica de qual tabela veio ("UNIDADE" ou "CONTATO").
 */
public record ResultadoBusca(int id, String nome, String origem) {

    @Override
    public String toString() {
        return "%-8s [%d] %s".formatted(origem, id, nome);
    }
}
