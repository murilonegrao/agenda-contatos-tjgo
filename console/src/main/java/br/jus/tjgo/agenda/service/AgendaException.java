package br.jus.tjgo.agenda.service;

/**
 * Exceção de domínio (RF08): a camada service traduz {@link java.sql.SQLException}
 * e violações de regra em mensagens claras, que o {@code MenuConsole} exibe ao operador
 * sem stack trace cru (critérios CA07 e CA09).
 */
public class AgendaException extends RuntimeException {

    public AgendaException(String message) {
        super(message);
    }

    public AgendaException(String message, Throwable cause) {
        super(message, cause);
    }
}
