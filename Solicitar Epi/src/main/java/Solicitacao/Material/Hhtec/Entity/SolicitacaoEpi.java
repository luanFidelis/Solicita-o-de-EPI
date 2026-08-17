package Solicitacao.Material.Hhtec.Entity;

import Solicitacao.Material.Hhtec.Enum.Status;
import Solicitacao.Material.Hhtec.Enum.Unidade;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter

@Entity
@Table(name = "SolicitacaoEpi")
public class SolicitacaoEpi {


    public SolicitacaoEpi() {
    }

    public SolicitacaoEpi(LocalDate dataEmissao, String solicitante, String observacoes, Unidade unidade, List<ItemSolicitacao> itemSolicitacao, Status status) {
        this(dataEmissao, solicitante, observacoes, unidade, itemSolicitacao, status, null);
    }

    public SolicitacaoEpi(LocalDate dataEmissao, String solicitante, String observacoes, Unidade unidade, List<ItemSolicitacao> itemSolicitacao, Status status, Usuario usuario) {
        this.dataEmissao = dataEmissao;
        this.solicitante = solicitante;
        this.observacoes = observacoes;
        this.unidade = unidade;
        this.status = status;
        this.usuario = usuario;
        this.criadoEm = LocalDateTime.now();
        setItemSolicitacao(itemSolicitacao);
    }


    public void setItemSolicitacao(List<ItemSolicitacao> itens) {
        this.itemSolicitacao = itens;
        if (itens != null) {
            for (ItemSolicitacao item : itens) {
                item.setSolicitacaoEpi(this);
            }
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private LocalDate dataEmissao;


    private String solicitante;

    private String observacoes;

    private LocalDateTime criadoEm;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime dataAprovacao;

    @Enumerated(EnumType.STRING)
    private Unidade unidade;


    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "solicitacaoEpi", cascade = CascadeType.ALL)
    private List<ItemSolicitacao> itemSolicitacao;


}
