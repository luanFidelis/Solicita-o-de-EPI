package Solicitacao.Material.Hhtec.Service;

import Solicitacao.Material.Hhtec.Exceptions.ProdutoNaoEncontradoException;
import Solicitacao.Material.Hhtec.Exceptions.SemPermissaoException;
import Solicitacao.Material.Hhtec.Exceptions.SolicitacaoNaoEncontradaException;
import Solicitacao.Material.Hhtec.Dto.EditarStatusDto;
import Solicitacao.Material.Hhtec.Entity.ItemSolicitacao;
import Solicitacao.Material.Hhtec.Entity.Produto;
import Solicitacao.Material.Hhtec.Entity.SolicitacaoEpi;
import Solicitacao.Material.Hhtec.Enum.Status;
import Solicitacao.Material.Hhtec.Enum.StatusProduto;
import Solicitacao.Material.Hhtec.Repository.ItemSolicitacaoRepository;
import Solicitacao.Material.Hhtec.Repository.ProdutoRepository;
import Solicitacao.Material.Hhtec.Repository.SolicitacaoEpiRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SolicitacaoEpiService {

    private SolicitacaoEpiRepository solicitacaoEpiRepository;
    private ProdutoRepository produtoRepository;
    private ItemSolicitacaoRepository itemSolicitacaoRepository;
      public SolicitacaoEpiService(SolicitacaoEpiRepository solicitacaoEpiRepository,  ProdutoRepository produtoRepository,  ItemSolicitacaoRepository itemSolicitacaoRepository) {
          this.solicitacaoEpiRepository = solicitacaoEpiRepository;
          this.produtoRepository = produtoRepository;
          this.itemSolicitacaoRepository = itemSolicitacaoRepository;
      }





public Optional<SolicitacaoEpi> editarSolicitacao(SolicitacaoEpi solicitacaoEpi){

    Optional<SolicitacaoEpi> buscar =  solicitacaoEpiRepository.findById(solicitacaoEpi.getId());
    if(buscar.isEmpty()){
        return Optional.empty();
    }
    if(buscar.get().getStatus() == Status.TRANSFERIDO){

        return Optional.empty();
    }


SolicitacaoEpi editar = buscar.get();

    solicitacaoEpiRepository.save(editar);
    return Optional.of(editar);

}

public void editarStatus(EditarStatusDto editarStatusDto){

  SolicitacaoEpi solicitacao = solicitacaoEpiRepository.findById(editarStatusDto.idSolicitacao())
          .orElseThrow(() ->  new SolicitacaoNaoEncontradaException("Solicitação não encontrada"));

  if(editarStatusDto.permGestor().equals("ST") ||editarStatusDto.permGestor().equals("TI")){
      solicitacao.setStatus(editarStatusDto.status());
      solicitacaoEpiRepository.save(solicitacao);
  }

   else {
      throw new SemPermissaoException("Você não tem permissão");
  }
}

    @Transactional
    public Produto transferirProdutos(Long idSolicitacao, Integer idProdutoOrigem) {
        return transferirProdutos(idSolicitacao, idProdutoOrigem, null);
    }

    @Transactional
    public Produto transferirProdutos(Long idSolicitacao, Integer idProdutoOrigem, UUID idItem) {

        SolicitacaoEpi solicitacaoEpi = solicitacaoEpiRepository
                .findById(idSolicitacao)
                .orElseThrow(() ->
                        new SolicitacaoNaoEncontradaException(
                                "Solicitação não encontrada"
                        )
                );

        Produto produtoQueVaiEnviar = produtoRepository
                .findById(idProdutoOrigem)
                .orElseThrow(() ->
                        new ProdutoNaoEncontradoException(
                                "Produto de origem não encontrado"
                        )
                );


        boolean achouItem = false;

        for (ItemSolicitacao item : solicitacaoEpi.getItemSolicitacao()) {

            // pediram um item específico? os outros ficam como estão
            if (idItem != null && !idItem.equals(item.getId())) {
                continue;
            }
            achouItem = true;

            if (item.getIdProduto() == null) {
                continue;
            }

            Produto produtoQueVaiReceber = produtoRepository
                    .findById(item.getIdProduto())
                    .orElseThrow(() ->
                            new ProdutoNaoEncontradoException(
                                    "Produto de recebimento não encontrado"
                            )
                    );

            if (produtoQueVaiEnviar.getQuantidadeEstoque() < item.getQuantidade()) {
                throw new RuntimeException(
                        "Estoque insuficiente para o produto " +
                                produtoQueVaiEnviar.getId()
                );
            }

            produtoQueVaiEnviar.setQuantidadeEstoque(
                    produtoQueVaiEnviar.getQuantidadeEstoque()
                            - item.getQuantidade()
            );

            produtoQueVaiReceber.setQuantidadeEstoque(
                    produtoQueVaiReceber.getQuantidadeEstoque()
                            + item.getQuantidade()
            );

            item.setStatusProduto(StatusProduto.TRANSFERIDO);
        }

        if (idItem != null && !achouItem) {
            throw new ProdutoNaoEncontradoException("Item não encontrado neste pedido");
        }

        return produtoQueVaiEnviar;
    }


    public void mudarStatusProduto(UUID idProduto) {

          ItemSolicitacao buscarItem = itemSolicitacaoRepository
                  .findById(idProduto)
                  .orElseThrow(()-> new ProdutoNaoEncontradoException("Produto não encontrado no Banco"));

          buscarItem.setStatusProduto(StatusProduto.COMPRADO);
          itemSolicitacaoRepository.save(buscarItem);
    }


    public void mudarFinalizado (Long idSolicitacao) {
        SolicitacaoEpi solicitacaoEpi = solicitacaoEpiRepository.findById(idSolicitacao)
                .orElseThrow(()-> new SolicitacaoNaoEncontradaException("Solicitação não encontrada"));

        for(ItemSolicitacao itemSolicitacao : solicitacaoEpi.getItemSolicitacao()){

            if(itemSolicitacao.getStatusProduto() == null
                    || itemSolicitacao.getStatusProduto() == StatusProduto.ABERTO){
                throw new SolicitacaoNaoEncontradaException("Ainda há itens em aberto");
            }
        }


        solicitacaoEpi.setStatus(Status.FINALIZADO);
        solicitacaoEpiRepository.save(solicitacaoEpi);
    }

    public List<Produto> produtosRegiao(String regiao){

          return produtoRepository.findAllByRegiaoContainingIgnoreCase(regiao);

    }
}

