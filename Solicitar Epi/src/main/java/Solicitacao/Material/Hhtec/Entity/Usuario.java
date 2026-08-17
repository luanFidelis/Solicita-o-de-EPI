package Solicitacao.Material.Hhtec.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "usuario", length = 50)
    private String usuario;

    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @Column(
            name = "status_acesso"
    )
    private String statusAcesso;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    @Column(name = "acesso_ti")
    private Byte acessoTi;

    @Column(name = "acesso_compras")
    private Byte acessoCompras;

    @Column(name = "acesso_estoque")
    private Byte acessoEstoque;

    @Column(name = "forcar_logout")
    private Byte forcarLogout;

    @Column(name = "perm_gestor", length = 60)
    private String permGestor;

    @Column(name = "acesso_adm")
    private Integer acessoAdm;

    @Column(
            name = "acesso_frota",
            nullable = false
    )
    private Byte acessoFrota;

    @Column(name = "unidade", length = 50)
    private String unidade;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(name = "permissoes_json", columnDefinition = "CLOB")
    private String permissoesJson;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<SolicitacaoEpi> solicitacoesEpi;
}