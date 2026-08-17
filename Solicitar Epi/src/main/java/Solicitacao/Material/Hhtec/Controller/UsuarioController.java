package Solicitacao.Material.Hhtec.Controller;

import Solicitacao.Material.Hhtec.Entity.SolicitacaoEpi;
import Solicitacao.Material.Hhtec.Entity.Usuario;
import Solicitacao.Material.Hhtec.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(name = "/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/listarUsuario")
    public List<SolicitacaoEpi> listarUsuario(@RequestBody Integer id){
        return usuarioService.listarSolicitacoesUsuario(id);
    }

    @GetMapping("/listarGestor")
    public List<SolicitacaoEpi> listarGestor(@RequestBody Integer id){
        return usuarioService.listarSolicitacoesGestor(id);
    }

    // ---- Mesmas consultas, com o id no CAMINHO ----
    // As duas de cima usam @GetMapping + @RequestBody: GET com corpo é fora do
    // padrão HTTP e o fetch() do navegador nem consegue enviar. Estas aqui existem
    // para qualquer cliente conseguir chamar (a tela em /static usa estas).
    // As de cima ficam no lugar para não quebrar quem já as consome.

    @GetMapping("/usuario/{id}/solicitacoes")
    public List<SolicitacaoEpi> solicitacoesDoUsuario(@PathVariable Integer id){
        return usuarioService.listarSolicitacoesUsuario(id);
    }

    @GetMapping("/gestor/{id}/solicitacoes")
    public List<SolicitacaoEpi> solicitacoesDoGestor(@PathVariable Integer id){
        return usuarioService.listarSolicitacoesGestor(id);
    }
}
