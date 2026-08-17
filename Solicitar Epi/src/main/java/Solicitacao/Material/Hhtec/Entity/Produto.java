package Solicitacao.Material.Hhtec.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "produtos")
@Getter
@Setter
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo_h", length = 20)
    private String codigoH;

    @Column(name = "nome_completos", length = 150)
    private String nomeCompletos;

    @Column(name = "ca", length = 50)
    private String ca;

    @Column(name = "quantidade_estoque")
    private Integer quantidadeEstoque;

    @Column(name = "estoque_minimo")
    private Integer estoqueMinimo;

    @Column(name = "imagem_path", length = 255)
    private String imagemPath;

    @Column(name = "ca_numero", length = 50)
    private String caNumero;

    @Column(
            name = "tipo_qr",
            nullable = false
    )
    private String tipoQr;

    @Column(name = "entrada")
    private Integer entrada;

    @Column(name = "saida")
    private Integer saida;

    @Column(name = "regiao", length = 50)
    private String regiao;

    @Column(name = "ativo")
    private Byte ativo;
}
