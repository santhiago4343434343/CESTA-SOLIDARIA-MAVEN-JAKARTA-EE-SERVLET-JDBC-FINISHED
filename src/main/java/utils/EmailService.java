package utils;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Envio de e-mail de confirmacao de cadastro (via Mailtrap, para testes).
 *
 * <p><b>Configuracao por variaveis de ambiente</b> (com padroes de dev):</p>
 * <ul>
 *   <li>{@code MAIL_HOST}     - padrao {@code sandbox.smtp.mailtrap.io}</li>
 *   <li>{@code MAIL_PORT}     - padrao {@code 2525}</li>
 *   <li>{@code MAIL_USER}     - usuario do inbox do Mailtrap (obrigatorio)</li>
 *   <li>{@code MAIL_PASSWORD} - senha do inbox do Mailtrap (obrigatorio)</li>
 *   <li>{@code MAIL_FROM}     - remetente (padrao Cesta Solidaria)</li>
 * </ul>
 *
 * <p>Se {@code MAIL_USER}/{@code MAIL_PASSWORD} nao estiverem definidos, o envio
 * e' apenas pulado (a aplicacao continua funcionando normalmente). O envio roda
 * em uma thread separada para nao travar a resposta do cadastro, e qualquer
 * falha e' registrada no log em vez de quebrar o fluxo.</p>
 */
public class EmailService {

    private static final String HOST = env("MAIL_HOST", "sandbox.smtp.mailtrap.io");
    private static final String PORT = env("MAIL_PORT", "2525");
    private static final String USER = env("MAIL_USER", "");
    private static final String PASS = env("MAIL_PASSWORD", "");
    private static final String FROM = env("MAIL_FROM", "Cesta Solidaria <no-reply@cestasolidaria.local>");

    // Uma unica thread (daemon) processa a fila de envios em segundo plano.
    private static final ExecutorService POOL = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "email-sender");
        t.setDaemon(true);
        return t;
    });

    private EmailService() {
        // classe utilitaria: nao deve ser instanciada
    }

    /** Enfileira (em segundo plano) o e-mail de confirmacao de cadastro. */
    public static void enviarConfirmacaoAsync(String nome, String emailDestino) {
        if (USER.isBlank() || PASS.isBlank()) {
            System.out.println("[email] MAIL_USER/MAIL_PASSWORD nao configurados - envio pulado.");
            return;
        }
        POOL.submit(() -> {
            try {
                enviar(emailDestino, "Cadastro confirmado - Cesta Solidaria", corpoHtml(nome));
                System.out.println("[email] Confirmacao enviada para " + emailDestino);
            } catch (Exception e) {
                System.out.println("[email] Falha ao enviar para " + emailDestino + ": " + e.getMessage());
            }
        });
    }

    private static void enviar(String para, String assunto, String html) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USER, PASS);
            }
        });

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(para));
        msg.setSubject(assunto, "UTF-8");
        msg.setContent(html, "text/html; charset=UTF-8");
        Transport.send(msg);
    }

    /** Corpo HTML simples do e-mail de confirmacao. */
    private static String corpoHtml(String nome) {
        return "<div style=\"font-family:sans-serif;max-width:520px;margin:auto;color:#1f2937\">"
                + "<h2 style=\"color:#2563eb\">Cesta Solidaria</h2>"
                + "<p>Ola, <strong>" + escapar(nome) + "</strong>!</p>"
                + "<p>Seu cadastro foi <strong>confirmado</strong> com sucesso. Voce entrou na "
                + "lista para retirada da cesta basica. Em breve avisaremos o local e a data.</p>"
                + "<p style=\"color:#6b7280;font-size:13px\">Mensagem automatica de um projeto didatico.</p>"
                + "</div>";
    }

    /** Escapa caracteres HTML para o nome nao quebrar o corpo do e-mail. */
    private static String escapar(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String env(String chave, String padrao) {
        String v = System.getenv(chave);
        return (v == null || v.isBlank()) ? padrao : v;
    }
}
