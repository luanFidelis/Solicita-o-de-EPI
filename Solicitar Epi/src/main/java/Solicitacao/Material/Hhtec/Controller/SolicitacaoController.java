package Solicitacao.Material.Hhtec.Controller;

import Solicitacao.Material.Hhtec.Dto.EditarStatusDto;
import Solicitacao.Material.Hhtec.Dto.Solicitacaodto;
import Solicitacao.Material.Hhtec.Email.EmailService;
import Solicitacao.Material.Hhtec.Entity.Produto;
import Solicitacao.Material.Hhtec.Entity.SolicitacaoEpi;
import Solicitacao.Material.Hhtec.Enum.Status;
import Solicitacao.Material.Hhtec.Repository.ProdutoRepository;
import Solicitacao.Material.Hhtec.Repository.SolicitacaoEpiRepository;
import Solicitacao.Material.Hhtec.Service.SolicitacaoEpiService;
import Solicitacao.Material.Hhtec.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/solicitarEpi")
public class SolicitacaoController {

    @Autowired
    private SolicitacaoEpiRepository solicitacaoEpiRepository;
    @Autowired
    private SolicitacaoEpiService solicitacaoEpiService;

    @Autowired
    private UsuarioService  usuarioService;

    @Autowired
    private EmailService emailService;




    @PostMapping("/solicitar")
    public ResponseEntity<SolicitacaoEpi> solicitar(@RequestBody @Valid Solicitacaodto dto) {

            SolicitacaoEpi solicitacaoEpi = new SolicitacaoEpi(
                    dto.dataEmissao(),
                    dto.solicitante(),
                    dto.observacoes(),
                    dto.unidade(),
                    dto.itemSolicitacao(),
                    Status.ABERTO,
                    usuarioService.buscarPorId(dto.usuarioId())
            );
            solicitacaoEpiRepository.save(solicitacaoEpi);
            emailService.avisarNovaSolicitacao(solicitacaoEpi);

            return ResponseEntity.ok(solicitacaoEpi);


    }

    @GetMapping("listarRegiao")

    public List<Produto> listarRegiao(String regiao){
        return solicitacaoEpiService.produtosRegiao(regiao);
    }


    @PostMapping("/editarStatus")

    public ResponseEntity<Void> editarStatus(@RequestBody @Valid EditarStatusDto dto) {
            solicitacaoEpiService.editarStatus(dto);
            return ResponseEntity.ok().build();


    }


    @PostMapping("/editarSolicitacao")

    public ResponseEntity<?> editarSolicitacao(@RequestBody @Valid  SolicitacaoEpi solicitacao) {
        var retorno = solicitacaoEpiService.editarSolicitacao(solicitacao);
        return retorno.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }


    @PostMapping("/transferir")

    public ResponseEntity<SolicitacaoEpi> transferir(@RequestBody Long idSolicitacaoEpi, Integer idProdutoOrigem, @RequestParam(required = false) UUID idItem) {

        solicitacaoEpiService.transferirProdutos(idSolicitacaoEpi, idProdutoOrigem, idItem);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/mudarStatusProduto")

    public ResponseEntity<Void> mudarStatusProdutoComprado(@RequestBody UUID idProduto) {

        solicitacaoEpiService.mudarStatusProduto(idProduto);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/mudarFinalizado")
    public ResponseEntity<Void> mudarFinalizado(@RequestBody Long idProduto) {

        solicitacaoEpiService.mudarFinalizado(idProduto);
        return ResponseEntity.ok().build();
    }




}
