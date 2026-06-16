package br.jus.tjgo.agenda.model;

import java.time.LocalDateTime;

/**
 * POJO de domínio — espelha a tabela {@code telefone}.
 * Arco exclusivo: pertence a EXATAMENTE um dono (unidade OU contato), garantido por CHECK.
 * {@code numero} é só dígitos com DDI (ex.: 5562999999999); a máscara fica na apresentação.
 */
public class Telefone {
    private int id;
    private String numero;
    private TipoTelefone tipo;
    private Integer unidadeId;   // wrapper: um dos dois é NULL (arco exclusivo)
    private Integer contatoId;
    private LocalDateTime criadoEm;

    public Telefone() {
    }

    public Telefone(String numero, TipoTelefone tipo) {
        this.numero = numero;
        this.tipo = tipo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public TipoTelefone getTipo() { return tipo; }
    public void setTipo(TipoTelefone tipo) { this.tipo = tipo; }

    public Integer getUnidadeId() { return unidadeId; }
    public void setUnidadeId(Integer unidadeId) { this.unidadeId = unidadeId; }

    public Integer getContatoId() { return contatoId; }
    public void setContatoId(Integer contatoId) { this.contatoId = contatoId; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    /** Formatação só-apresentação: +55 (62) 9 9999-9999 quando reconhecível; senão devolve cru. */
    public String formatado() {
        if (numero == null) return "";
        String n = numero;
        if (n.startsWith("55") && (n.length() == 12 || n.length() == 13)) {
            String ddd = n.substring(2, 4);
            String resto = n.substring(4);
            if (resto.length() == 9) { // celular
                return "+55 (%s) %s %s-%s".formatted(ddd, resto.substring(0, 1),
                        resto.substring(1, 5), resto.substring(5));
            }
            if (resto.length() == 8) { // fixo
                return "+55 (%s) %s-%s".formatted(ddd, resto.substring(0, 4), resto.substring(4));
            }
        }
        return n;
    }

    @Override
    public String toString() {
        return "[%d] %s (%s)".formatted(id, formatado(), tipo);
    }
}
