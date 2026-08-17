package Solicitacao.Material.Hhtec.Entity;

import Solicitacao.Material.Hhtec.Enum.StatusProduto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@Entity
@Table(name = "itens_solicitacao_epi")
public class ItemSolicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String codigoProduto;

    private String descricao;

    private Integer quantidade;

    private String observacao;

    private Integer idProduto;


    @Enumerated(EnumType.STRING)
    private StatusProduto statusProduto = StatusProduto.ABERTO;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "solicitacao_id")
    private SolicitacaoEpi solicitacaoEpi;
}
