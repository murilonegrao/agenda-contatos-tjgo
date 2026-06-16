package br.jus.tjgo.agenda.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Ponto único de criação de conexões JDBC (RNF04, ADR-001).
 *
 * <p>Resolve as credenciais nesta ordem (a primeira que existir vence):
 * <ol>
 *   <li>Variáveis de ambiente {@code AGENDA_DB_URL}, {@code AGENDA_DB_USER}, {@code AGENDA_DB_PASSWORD};</li>
 *   <li>arquivo {@code db.properties} no classpath ({@code src/main/resources});</li>
 *   <li>padrões para uma instalação local típica.</li>
 * </ol>
 * Assim o professor ajusta a senha sem recompilar — basta editar {@code db.properties}
 * ou exportar as variáveis (documentado no README).
 */
public final class ConexaoFactory {

    private static final String DEFAULT_URL  = "jdbc:postgresql://localhost:5432/agenda";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASS = "postgres";

    private static final String url;
    private static final String user;
    private static final String password;

    static {
        Properties p = new Properties();
        try (InputStream in = ConexaoFactory.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException e) {
            // arquivo opcional — segue com env/padrões
            System.err.println("Aviso: não foi possível ler db.properties (" + e.getMessage() + ")");
        }

        url      = resolve("AGENDA_DB_URL",      p.getProperty("db.url"),      DEFAULT_URL);
        user     = resolve("AGENDA_DB_USER",     p.getProperty("db.user"),     DEFAULT_USER);
        password = resolve("AGENDA_DB_PASSWORD", p.getProperty("db.password"), DEFAULT_PASS);
    }

    private ConexaoFactory() {
    }

    private static String resolve(String envKey, String fileValue, String fallback) {
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) return env;
        if (fileValue != null && !fileValue.isBlank()) return fileValue;
        return fallback;
    }

    /** Abre uma nova conexão. Cabe a quem chama fechá-la (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
