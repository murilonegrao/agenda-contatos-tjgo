package br.jus.tjgo.agenda.model;

import java.time.LocalDateTime;

/**
 * POJO de domínio — espelha a tabela {@code unidade}.
 * Sem regra de banco: a validação fica no SGBD (CHECK/FK) e na camada service.
 */
public class Unidade {
    private int id;
    private String nome;
    private TipoUnidade tipo;
    private String endereco;
    private Integer responsavelId;   // wrapper: NULL = sem responsável
    private LocalDateTime criadoEm;

    public Unidade() {
    }

    public Unidade(String nome, TipoUnidade tipo, String endereco) {
        this.nome = nome;
        this.tipo = tipo;
        this.endereco = endereco;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public TipoUnidade getTipo() { return tipo; }
    public void setTipo(TipoUnidade tipo) { this.tipo = tipo; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public Integer getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Integer responsavelId) { this.responsavelId = responsavelId; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    @Override
    public String toString() {
        return "[%d] %s (%s)%s".formatted(
                id, nome, tipo,
                endereco == null || endereco.isBlank() ? "" : " — " + endereco);
    }
}
