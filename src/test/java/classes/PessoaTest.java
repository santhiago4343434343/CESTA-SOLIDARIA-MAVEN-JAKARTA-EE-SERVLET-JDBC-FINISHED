package classes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de unidade para o hash e a verificação de senha (BCrypt).
 * Não acessam o banco de dados — validam apenas a lógica de segurança
 * de {@link Pessoa#gerarHash(String)} e {@link Pessoa#verificarSenha(String, String)}.
 */
class PessoaTest {

    private static final String SENHA = "MinhaSenha@123";

    @Test
    @DisplayName("A senha correta deve ser validada contra o hash")
    void senhaCorretaDeveSerValidada() {
        String hash = Pessoa.gerarHash(SENHA);
        assertTrue(Pessoa.verificarSenha(SENHA, hash));
    }

    @Test
    @DisplayName("Uma senha incorreta não deve ser validada")
    void senhaIncorretaNaoDeveSerValidada() {
        String hash = Pessoa.gerarHash(SENHA);
        assertFalse(Pessoa.verificarSenha("senhaErrada", hash));
    }

    @Test
    @DisplayName("O hash não pode ser igual à senha em texto puro e deve ter formato BCrypt")
    void hashNaoDeveSerIgualAoTextoPuro() {
        String hash = Pessoa.gerarHash(SENHA);
        assertNotEquals(SENHA, hash);
        assertTrue(hash.startsWith("$2a$"), "Hash BCrypt deve começar com $2a$");
    }

    @Test
    @DisplayName("Dois hashes da mesma senha devem ser diferentes (salt aleatório) e ambos válidos")
    void hashesDaMesmaSenhaDevemSerDiferentes() {
        String h1 = Pessoa.gerarHash(SENHA);
        String h2 = Pessoa.gerarHash(SENHA);
        assertNotEquals(h1, h2);
        assertTrue(Pessoa.verificarSenha(SENHA, h1));
        assertTrue(Pessoa.verificarSenha(SENHA, h2));
    }

    @Test
    @DisplayName("Hash nulo ou vazio deve retornar false sem lançar exceção")
    void hashNuloOuVazioDeveRetornarFalse() {
        assertFalse(Pessoa.verificarSenha("qualquer", null));
        assertFalse(Pessoa.verificarSenha("qualquer", ""));
    }
}
