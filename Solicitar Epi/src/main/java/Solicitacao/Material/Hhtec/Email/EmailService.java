package Solicitacao.Material.Hhtec.Email;

import Solicitacao.Material.Hhtec.Entity.ItemSolicitacao;
import Solicitacao.Material.Hhtec.Entity.SolicitacaoEpi;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // mesma conta que o sistema PHP já usa para disparar aviso
    @Value("${spring.mail.username:sistema@hhtec.com.br}")
    private String remetente;

    public void enviarEmail(String para, String assunto, String corpo) {
        if (para == null || para.isBlank()) {
            System.out.println(">>> [LOG] E-mail ignorado: destinatário vazio.");
            return;
        }
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom(remetente);
            mensagem.setTo(para);
            mensagem.setSubject(assunto);
            mensagem.setText(corpo);

            mailSender.send(mensagem);
            System.out.println(">>> [LOG] E-mail enviado com sucesso para: " + para);
        } catch (Exception e) {
            System.err.println(">>> [ERRO] Falha ao enviar e-mail: " + e.getMessage());
        }
    }

    /** Igual ao de cima, mas com corpo em HTML (o aviso de EPI é formatado). */
    public void enviarHtml(String para, String assunto, String html) {
        if (para == null || para.isBlank()) {
            System.out.println(">>> [LOG] E-mail ignorado: destinatário vazio.");
            return;
        }
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper ajuda = new MimeMessageHelper(mensagem, false, "UTF-8");
            ajuda.setFrom(remetente, "Sistema HHTEC - Segurança do Trabalho");
            ajuda.setTo(para);
            ajuda.setSubject(assunto);
            ajuda.setText(html, true);          // true = HTML

            mailSender.send(mensagem);
            System.out.println(">>> [LOG] E-mail enviado com sucesso para: " + para);
        } catch (Exception e) {
            System.err.println(">>> [ERRO] Falha ao enviar e-mail: " + e.getMessage());
        }
    }

    // ---- Aviso de solicitação nova, no mesmo espírito do salvar.php do PHP ----

    @Value("${hhtec.email.gestora:}")
    private String emailGestora;

    @Value("${hhtec.email.ativo:true}")
    private boolean envioAtivo;

    /**
     * Avisa a gestora que entrou um pedido novo.
     * Nunca deixa uma falha de e-mail derrubar a solicitação: o pedido já foi gravado,
     * o aviso é secundário.
     */
    public void avisarNovaSolicitacao(SolicitacaoEpi solicitacao) {
        if (!envioAtivo || solicitacao == null) return;

        try {
            enviarHtml(emailGestora,
                    "EPI #" + solicitacao.getId() + " · PEDIDO DE MATERIAL — "
                            + nvl(solicitacao.getSolicitante()),
                    montarHtml(solicitacao));

        } catch (Exception e) {
            System.err.println(">>> [ERRO] Falha ao montar o aviso da solicitação: " + e.getMessage());
        }
    }

    private String nvl(Object v) { return v == null ? "-" : v.toString(); }

    /** Escapa o que vem do usuário: sem isso um "&" ou "<" quebra o HTML do e-mail. */
    private String esc(Object v) {
        if (v == null) return "-";
        return v.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String unidadeLegivel(Object u) {
        String s = u == null ? "" : u.toString();
        if (s.equals("SAO_BERNARDO")) return "SBC &middot; 114 SBC-BOMBAS";
        if (s.equals("SEDE"))         return "BASE &middot; 001 ADM. HHTEC";
        return s.isBlank() ? "-" : esc(s);
    }

    // Identidade PRÓPRIA, para nunca ser confundido com a Solicitação de Compras (SC),
    // que é roxa e fala em "SC #". Aqui: preto com faixa de advertência amarela,
    // rótulo de EPI e numeração "EPI #". Tudo em tabela com estilo inline, que é o
    // que os clientes de e-mail entendem.
    private String montarHtml(SolicitacaoEpi s) {
        List<ItemSolicitacao> itens = s.getItemSolicitacao() == null ? List.of() : s.getItemSolicitacao();

        String emissao = "-";
        try {
            if (s.getDataEmissao() != null)
                emissao = s.getDataEmissao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ignore) { emissao = nvl(s.getDataEmissao()); }

        StringBuilder linhas = new StringBuilder();
        int n = 0;
        for (ItemSolicitacao i : itens) {
            String zebra = (++n % 2 == 0) ? "#f7f7f5" : "#ffffff";
            String cod = (i.getCodigoProduto() == null || i.getCodigoProduto().isBlank())
                    ? "<span style='color:#9a9a9a;font-style:italic'>sem cadastro</span>"
                    : "<span style='font-family:Consolas,monospace;background:#f0efec;border:1px solid #e2e0dc;padding:1px 6px;border-radius:3px'>"
                      + esc(i.getCodigoProduto()) + "</span>";
            String obs = (i.getObservacao() == null || i.getObservacao().isBlank())
                    ? "" : "<div style='color:#6b6b6b;font-size:12px;margin-top:3px'>" + esc(i.getObservacao()) + "</div>";

            linhas.append("<tr style='background:").append(zebra).append("'>")
                  .append("<td style='padding:10px 14px;border-bottom:1px solid #ecebe8;font-size:13px'>").append(cod).append("</td>")
                  .append("<td style='padding:10px 14px;border-bottom:1px solid #ecebe8;font-size:14px;color:#1a1a1a'>")
                  .append(esc(i.getDescricao())).append(obs).append("</td>")
                  .append("<td style='padding:10px 14px;border-bottom:1px solid #ecebe8;text-align:center;font-size:16px;font-weight:bold;color:#111'>")
                  .append(nvl(i.getQuantidade())).append("</td></tr>");
        }
        if (n == 0) {
            linhas.append("<tr><td colspan='3' style='padding:16px;text-align:center;color:#9a9a9a'>Sem itens.</td></tr>");
        }

        String obsGeral = (s.getObservacoes() == null || s.getObservacoes().isBlank()) ? "" :
                "<tr><td colspan='2' style='padding:12px 18px 0'>"
              + "<div style='background:#fffbea;border-left:4px solid #f0b429;padding:10px 12px;font-size:13px;color:#5c4813'>"
              + "<b>Observação:</b> " + esc(s.getObservacoes()) + "</div></td></tr>";

        return ""
        + "<div style='background:#eceae6;padding:22px 12px;font-family:Arial,Helvetica,sans-serif'>"
        + "<table role='presentation' cellpadding='0' cellspacing='0' border='0' width='100%' style='max-width:620px;margin:0 auto;background:#ffffff;border:1px solid #dcdad5'>"

        // faixa de advertência: a assinatura visual do módulo de EPI
        + "<tr><td style='height:6px;background:repeating-linear-gradient(45deg,#f0b429 0 14px,#1c1c1c 14px 28px);font-size:0;line-height:0'>&nbsp;</td></tr>"

        // cabeçalho preto
        + "<tr><td style='background:#1c1c1c;padding:18px 22px'>"
        +   "<div style='color:#f0b429;font-size:11px;letter-spacing:2px;font-weight:bold'>SEGURANÇA DO TRABALHO &middot; EPI</div>"
        +   "<div style='color:#ffffff;font-size:21px;font-weight:bold;margin-top:4px'>PEDIDO DE MATERIAL</div>"
        + "</td></tr>"

        // aviso que diferencia de compras
        + "<tr><td style='background:#f0b429;padding:7px 22px;color:#241c00;font-size:12px;font-weight:bold'>"
        +   "Este pedido é do estoque de EPI &mdash; não é uma Solicitação de Compras (SC)."
        + "</td></tr>"

        + "<tr><td style='padding:18px 22px 6px;color:#3a3a3a;font-size:14px;line-height:1.5'>"
        +   "<b>" + esc(s.getSolicitante()) + "</b> abriu um pedido de material. "
        +   "Ele fica <b>em aberto</b> até você transferir do estoque ou marcar como comprado."
        + "</td></tr>"

        // dados do pedido
        + "<tr><td style='padding:12px 18px 0'>"
        +   "<table role='presentation' cellpadding='0' cellspacing='0' border='0' width='100%' style='background:#f7f7f5;border:1px solid #e6e4e0'>"
        +     "<tr>"
        +       "<td style='padding:12px 14px;font-size:13px;color:#6b6b6b'>Pedido</td>"
        +       "<td style='padding:12px 14px;font-size:20px;font-weight:bold;color:#1c1c1c;text-align:right'>EPI #" + s.getId() + "</td>"
        +     "</tr>"
        +     "<tr><td style='padding:0 14px 10px;font-size:13px;color:#6b6b6b'>Unidade</td>"
        +       "<td style='padding:0 14px 10px;font-size:14px;color:#1a1a1a;text-align:right;font-weight:bold'>" + unidadeLegivel(s.getUnidade()) + "</td></tr>"
        +     "<tr><td style='padding:0 14px 12px;font-size:13px;color:#6b6b6b'>Emissão</td>"
        +       "<td style='padding:0 14px 12px;font-size:14px;color:#1a1a1a;text-align:right'>" + emissao + "</td></tr>"
        +   "</table>"
        + "</td></tr>"
        + obsGeral

        // itens
        + "<tr><td style='padding:18px 18px 0'>"
        +   "<div style='font-size:11px;letter-spacing:1.5px;color:#6b6b6b;font-weight:bold;margin-bottom:8px'>ITENS PEDIDOS (" + itens.size() + ")</div>"
        +   "<table role='presentation' cellpadding='0' cellspacing='0' border='0' width='100%' style='border:1px solid #e6e4e0;border-collapse:collapse'>"
        +     "<tr style='background:#1c1c1c'>"
        +       "<th align='left' style='padding:9px 14px;color:#f0b429;font-size:11px;letter-spacing:1px'>CÓD</th>"
        +       "<th align='left' style='padding:9px 14px;color:#f0b429;font-size:11px;letter-spacing:1px'>DESCRIÇÃO</th>"
        +       "<th align='center' style='padding:9px 14px;color:#f0b429;font-size:11px;letter-spacing:1px'>QTD</th>"
        +     "</tr>"
        +     linhas
        +   "</table>"
        + "</td></tr>"

        + "<tr><td style='padding:18px 22px 22px;color:#6b6b6b;font-size:13px'>"
        +   "Para resolver, abra <b>Materiais &rsaquo; Solicitação de EPI</b> no sistema."
        + "</td></tr>"

        + "<tr><td style='background:#f7f7f5;border-top:1px solid #e6e4e0;padding:11px 22px;text-align:center;color:#8a8a8a;font-size:11px'>"
        +   "NÃO RESPONDA &mdash; AVISO AUTOMÁTICO DO SISTEMA HHTEC &middot; MÓDULO SEGURANÇA DO TRABALHO"
        + "</td></tr>"

        + "</table></div>";
    }
}