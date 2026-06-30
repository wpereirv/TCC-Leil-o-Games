package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.model.Conversa;
import com.leilao.leilao_games.model.Mensagem;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.ConversaService;
import com.leilao.leilao_games.service.MensagemService;
import com.leilao.leilao_games.service.ProdutoService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.leilao.leilao_games.service.NotificacaoService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private ConversaService conversaService;

    @Autowired
    private MensagemService mensagemService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private NotificacaoService notificacaoService;

   @GetMapping("/chat")
public String listarConversas(
        HttpSession session,
        Model model) {

    Usuario usuarioLogado =
            (Usuario) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null) {
        return "redirect:/login";
    }

    List<Conversa> conversas =
            conversaService.buscarPorUsuario(
                    usuarioLogado.getId()
            );

    Map<Long, Long> mensagensNaoLidas =
            new HashMap<>();

    Map<Long, Mensagem> ultimasMensagens =
            new HashMap<>();

    for (Conversa conversa : conversas) {

        long quantidade =
                mensagemService.contarNaoLidas(
                        conversa.getId(),
                        usuarioLogado.getId()
                );

        mensagensNaoLidas.put(
                conversa.getId(),
                quantidade
        );

        ultimasMensagens.put(
                conversa.getId(),
                mensagemService.buscarUltimaMensagem(
                        conversa.getId()
                )
        );
    }

    model.addAttribute("conversas", conversas);

    model.addAttribute(
            "mensagensNaoLidas",
            mensagensNaoLidas
    );

    model.addAttribute(
            "ultimasMensagens",
            ultimasMensagens
    );

    model.addAttribute(
            "usuarioLogado",
            usuarioLogado
    );

    return "conversas";
}

    @GetMapping("/chat/iniciar/{produtoId}")
    public String iniciarConversa(
            @PathVariable Long produtoId,
            HttpSession session) {

        Usuario usuarioLogado =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Produto produto = produtoService.buscarPorId(produtoId);

        if (produto == null) {
            return "redirect:/leiloes";
        }

        if (produto.getUsuario() == null) {
            return "redirect:/produto/" + produtoId;
        }

        // Impede o vendedor de iniciar uma conversa consigo mesmo.
        if (produto.getUsuario().getId()
                .equals(usuarioLogado.getId())) {

            return "redirect:/produto/" + produtoId;
        }

        Conversa conversa =
                conversaService.buscarOuCriar(
                        produto,
                        usuarioLogado
                );

        return "redirect:/chat/" + conversa.getId();
    }

    @GetMapping("/chat/{conversaId}")
    public String abrirConversa(
            @PathVariable Long conversaId,
            HttpSession session,
            Model model) {

        Usuario usuarioLogado =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Conversa conversa =
                conversaService.buscarPorId(conversaId);

        if (conversa == null) {
            return "redirect:/";
        }

      if (!participaDaConversa(conversa, usuarioLogado)) {
    return "redirect:/";
}

        mensagemService.marcarComoLidas(
        conversaId,
        usuarioLogado.getId()
        );

        model.addAttribute("conversa", conversa);

        model.addAttribute(
                "mensagens",
                mensagemService.buscarPorConversa(conversaId)
        );

        model.addAttribute("usuarioLogado", usuarioLogado);

        return "chat";
    }

    @PostMapping("/chat/enviar")
public String enviarMensagem(
        @RequestParam Long conversaId,
        @RequestParam String texto,
        HttpSession session) {

    Usuario usuarioLogado =
            (Usuario) session.getAttribute(
                    "usuarioLogado"
            );

    if (usuarioLogado == null) {
        return "redirect:/login";
    }

    Conversa conversa =
            conversaService.buscarPorId(
                    conversaId
            );

    if (conversa == null
            || !participaDaConversa(
                    conversa,
                    usuarioLogado
            )) {

        return "redirect:/";
    }

    if (texto == null) {
        return "redirect:/chat/" + conversaId;
    }

    String textoLimpo = texto.trim();

    if (textoLimpo.isEmpty()
            || textoLimpo.length() > 2000) {

        return "redirect:/chat/" + conversaId;
    }

    Mensagem mensagem = new Mensagem();

    mensagem.setConversa(conversa);
    mensagem.setRemetente(usuarioLogado);
    mensagem.setTexto(textoLimpo);

    mensagemService.salvar(mensagem);

    Usuario destinatario;

    if (conversa.getComprador()
            .getId()
            .equals(usuarioLogado.getId())) {

        destinatario =
                conversa.getVendedor();

    } else {

        destinatario =
                conversa.getComprador();
    }

    if (destinatario != null) {

        notificacaoService.criar(
                destinatario,
                "MENSAGEM",
                "Você recebeu uma nova mensagem de "
                        + usuarioLogado.getNome()
                        + ".",
                "/chat/" + conversaId
        );
    }

    return "redirect:/chat/" + conversaId;
}

private boolean participaDaConversa(
        Conversa conversa,
        Usuario usuario) {

    Long usuarioId = usuario.getId();

    return (
            conversa.getComprador() != null
            && conversa.getComprador()
                    .getId()
                    .equals(usuarioId)
    ) || (
            conversa.getVendedor() != null
            && conversa.getVendedor()
                    .getId()
                    .equals(usuarioId)
    );
}

}