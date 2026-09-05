package utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Pool de conexoes com o banco (HikariCP).
 *
 * <p>Um unico pool e' criado ao carregar a classe e reutilizado por toda a
 * aplicacao: cada {@link #getConnection()} empresta uma conexao do pool e o
 * {@code close()} (via try-with-resources) devolve a conexao em vez de
 * fecha-la de verdade.</p>
 *
 * <p><b>Credenciais:</b> em vez de ficarem "chumbadas" no codigo, sao lidas de
 * variaveis de ambiente (DB_URL, DB_USER, DB_PASSWORD). Se a variavel nao
 * existir, cai num valor padrao bom para desenvolvimento local. Assim o mesmo
 * .war roda em qualquer ambiente sem recompilar.</p>
 */
public class Conexao {

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(env("DB_URL", "jdbc:mysql://localhost:3306/tomcat_database"));
        config.setUsername(env("DB_USER", "root"));
        config.setPassword(env("DB_PASSWORD", "")); // ajuste conforme o seu MySQL
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10);
        config.setPoolName("tomcatDbPool");
        // Nao derruba a aplicacao se o MySQL estiver fora no boot; conecta sob demanda.
        config.setInitializationFailTimeout(-1);
        dataSource = new HikariDataSource(config);
    }

    private Conexao() {
        // classe utilitaria: nao deve ser instanciada
    }

    /** Le uma variavel de ambiente; se estiver ausente/vazia, usa o padrao. */
    private static String env(String chave, String padrao) {
        String valor = System.getenv(chave);
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }

    /** Empresta uma conexao do pool. Feche-a com try-with-resources. */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
