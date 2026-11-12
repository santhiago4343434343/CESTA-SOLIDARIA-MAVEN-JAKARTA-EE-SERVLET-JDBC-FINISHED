package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import classes.Pessoa;
import utils.Conexao;

/**
 * DAO (Data Access Object) da Pessoa: concentra todo o SQL/JDBC.
 * O controlador (Servlet) fala com esta classe, nunca direto com o banco.
 *
 * <p>
 * Todas as consultas usam PreparedStatement com parametros (?), o que
 * protege contra SQL Injection.
 * </p>
 */
public class PessoaDAO {

    /** Insere uma pessoa; a senha e' gravada com hash BCrypt. Papel padrao USER. */
    public boolean inserir(Pessoa p) {
        String sql = "INSERT INTO pessoa (nomepessoa, email, senha, status, papel) "
                + "VALUES (?, ?, ?, ?, ?)";
        String hash = Pessoa.gerarHash(p.getSenha());
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setString(1, p.getNomePessoa());
            stm.setString(2, p.getEmail());
            stm.setString(3, hash);
            stm.setString(4, p.getStatus() == null ? "aguardando" : p.getStatus());
            stm.setString(5, p.getPapel() == null ? "USER" : p.getPapel());
            stm.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir: " + e.getMessage());
            return false;
        }
    }

    /** Atualiza APENAS nome e e-mail de uma pessoa (dados pessoais). */
    public boolean atualizar(Pessoa p) {
        String sql = "UPDATE pessoa SET nomepessoa = ?, email = ? WHERE idpessoa = ?";
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setString(1, p.getNomePessoa());
            stm.setString(2, p.getEmail());
            stm.setInt(3, p.getIdPessoa());
            stm.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar: " + e.getMessage());
            return false;
        }
    }

    /** Atualiza apenas o status (aguardando / retirado). Acao de ADMIN. */
    public boolean atualizarStatus(int id, String status) {
        String sql = "UPDATE pessoa SET status = ? WHERE idpessoa = ?";
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setString(1, status);
            stm.setInt(2, id);
            stm.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar status: " + e.getMessage());
            return false;
        }
    }

    /** Troca a senha (grava novo hash BCrypt). Usado na area "Meu cadastro". */
    public boolean atualizarSenha(int id, String senhaPlain) {
        String sql = "UPDATE pessoa SET senha = ? WHERE idpessoa = ?";
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setString(1, Pessoa.gerarHash(senhaPlain));
            stm.setInt(2, id);
            stm.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar senha: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gera/guarda o token de reset para o e-mail (expira em N minutos).
     * Retorna true se o e-mail existe (uma linha afetada).
     */
    public boolean salvarTokenReset(String email, String token, int minutos) {
        String sql = "UPDATE pessoa SET reset_token = ?, "
                + "reset_expira = DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE email = ?";
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setString(1, token);
            stm.setInt(2, minutos);
            stm.setString(3, email);
            return stm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao salvar token de reset: " + e.getMessage());
            return false;
        }
    }

    /** Retorna a pessoa dona de um token AINDA VÁLIDO (não expirado), ou null. */
    public Pessoa buscarPorTokenValido(String token) {
        String sql = "SELECT idpessoa, nomepessoa, email, status, papel FROM pessoa "
                + "WHERE reset_token = ? AND reset_expira > NOW()";
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setString(1, token);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar por token: " + e.getMessage());
        }
        return null;
    }

    /**
     * Redefine a senha com um token válido e LIMPA o token (não serve 2x).
     * Retorna true se o token era válido e a senha foi trocada.
     */
    public boolean redefinirSenhaPorToken(String token, String novaSenhaPlain) {
        String sql = "UPDATE pessoa SET senha = ?, reset_token = NULL, reset_expira = NULL "
                + "WHERE reset_token = ? AND reset_expira > NOW()";
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setString(1, Pessoa.gerarHash(novaSenhaPlain));
            stm.setString(2, token);
            return stm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao redefinir senha: " + e.getMessage());
            return false;
        }
    }

    /**
     * Guarda o token de confirmacao de uma conta recem-criada.
     * Retorna true se o e-mail existe (uma linha afetada).
     */
    public boolean salvarTokenConfirmacao(String email, String token) {
        String sql = "UPDATE pessoa SET confirmacao_token = ? WHERE email = ?";
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setString(1, token);
            stm.setString(2, email);
            return stm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao salvar token de confirmacao: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ativa a conta cujo token bate e LIMPA o token (nao serve 2x).
     * Retorna true se o token era valido.
     */
    public boolean confirmarEmailPorToken(String token) {
        String sql = "UPDATE pessoa SET email_confirmado = 1, confirmacao_token = NULL "
                + "WHERE confirmacao_token = ?";
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setString(1, token);
            return stm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao confirmar e-mail: " + e.getMessage());
            return false;
        }
    }

    /** Retorna true se a conta do e-mail ja esta confirmada (ativa). */
    public boolean emailConfirmado(String email) {
        String sql = "SELECT email_confirmado FROM pessoa WHERE email = ?";
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setString(1, email);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() && rs.getBoolean("email_confirmado");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar confirmacao: " + e.getMessage());
            return false;
        }
    }

    /** Retorna o hash da senha de um id (para conferir a senha atual). */
    public String buscarHashSenha(int id) {
        String sql = "SELECT senha FROM pessoa WHERE idpessoa = ?";
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setInt(1, id);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("senha");
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar hash: " + e.getMessage());
        }
        return null;
    }

    /** Exclui uma pessoa pelo id. Acao de ADMIN. */
    public boolean excluir(int id) {
        String sql = "DELETE FROM pessoa WHERE idpessoa = ?";
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setInt(1, id);
            stm.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir: " + e.getMessage());
            return false;
        }
    }

    /** Lista pessoas com busca opcional (nome/e-mail) e paginacao. */
    public List<Pessoa> listar(String filtro, int offset, int limit) {
        boolean temFiltro = filtro != null && !filtro.isBlank();
        StringBuilder sql = new StringBuilder(
                "SELECT idpessoa, nomepessoa, email, status, papel FROM pessoa");
        if (temFiltro) {
            sql.append(" WHERE nomepessoa LIKE ? OR email LIKE ?");
        }
        sql.append(" ORDER BY idpessoa LIMIT ? OFFSET ?");

        List<Pessoa> lista = new ArrayList<>();
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (temFiltro) {
                String like = "%" + filtro.trim() + "%";
                stm.setString(i++, like);
                stm.setString(i++, like);
            }
            stm.setInt(i++, limit);
            stm.setInt(i, offset);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar: " + e.getMessage());
        }
        return lista;
    }

    /** Conta quantas pessoas atendem ao filtro (para as paginas). */
    public int contar(String filtro) {
        boolean temFiltro = filtro != null && !filtro.isBlank();
        String sql = "SELECT COUNT(*) FROM pessoa"
                + (temFiltro ? " WHERE nomepessoa LIKE ? OR email LIKE ?" : "");
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            if (temFiltro) {
                String like = "%" + filtro.trim() + "%";
                stm.setString(1, like);
                stm.setString(2, like);
            }
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao contar: " + e.getMessage());
        }
        return 0;
    }

    /** Busca uma pessoa pelo id (sem a senha), ou null. */
    public Pessoa buscarPorId(int id) {
        String sql = "SELECT idpessoa, nomepessoa, email, status, papel FROM pessoa WHERE idpessoa = ?";
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setInt(1, id);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca por e-mail TRAZENDO o hash da senha. Usado no login para
     * conferir a senha com BCrypt e descobrir o papel do usuario.
     */
    public Pessoa buscarPorEmail(String email) {
        String sql = "SELECT idpessoa, nomepessoa, email, status, papel, senha FROM pessoa WHERE email = ?";
        try (Connection con = Conexao.getConnection();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setString(1, email);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    Pessoa p = mapear(rs);
                    p.setSenha(rs.getString("senha"));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar por e-mail: " + e.getMessage());
        }
        return null;
    }

    /** Converte a linha atual do ResultSet num objeto Pessoa (sem a senha). */
    private Pessoa mapear(ResultSet rs) throws SQLException {
        Pessoa p = new Pessoa();
        p.setIdPessoa(rs.getInt("idpessoa"));
        p.setNomePessoa(rs.getString("nomepessoa"));
        p.setEmail(rs.getString("email"));
        p.setStatus(rs.getString("status"));
        p.setPapel(rs.getString("papel"));
        return p;
    }
}
