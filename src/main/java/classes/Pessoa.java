package classes;

import jakarta.json.bind.annotation.JsonbTransient;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Modelo (POJO) da entidade Pessoa: apenas os dados + helpers estaticos
 * de senha. Todo o acesso ao banco de dados fica em {@code dao.PessoaDAO}.
 */
public class Pessoa {

    private int idPessoa;
    private String nomePessoa;
    private String email;
    private String senha;
    private String status = "aguardando"; // aguardando | retirado
    private String papel = "USER";        // USER | ADMIN

    public int getIdPessoa() { return idPessoa; }
    public void setIdPessoa(int idPessoa) { this.idPessoa = idPessoa; }

    public String getNomePessoa() { return nomePessoa; }
    public void setNomePessoa(String nomePessoa) { this.nomePessoa = nomePessoa; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /**
     * A senha (hash) NUNCA deve sair pela API REST. O @JsonbTransient impede
     * que o serializador JSON (JSON-B) inclua este campo na resposta.
     */
    @JsonbTransient
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    /** Situacao na fila da cesta: "aguardando" ou "retirado". */
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /** Papel de autorizacao: "USER" (beneficiario) ou "ADMIN". */
    public String getPapel() { return papel; }
    public void setPapel(String papel) { this.papel = papel; }

    /** Atalho: true se este usuario e' administrador. */
    public boolean isAdmin() { return "ADMIN".equals(papel); }

    /** Gera o hash BCrypt de uma senha em texto puro (com salt aleatorio). */
    public static String gerarHash(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt());
    }

    /** Verifica se a senha em texto puro corresponde ao hash BCrypt armazenado. */
    public static boolean verificarSenha(String senhaDigitada, String hashArmazenado) {
        if (hashArmazenado == null || hashArmazenado.isEmpty()) {
            return false;
        }
        return BCrypt.checkpw(senhaDigitada, hashArmazenado);
    }
}

