package Solicitacao.Material.Hhtec.Dto;

import Solicitacao.Material.Hhtec.Entity.ItemSolicitacao;
import Solicitacao.Material.Hhtec.Enum.Unidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record Solicitacaodto (


                              @NotNull(message = "Informe a data de emissão.")
                              LocalDate dataEmissao,

                              @NotBlank(message = "Informe o solicitante.")
                              String solicitante,

                              String observacoes,

                              @NotNull(message = "Informe a unidade.")
                              Unidade unidade,

                              @NotEmpty(message = "Adicione Pelo menos 1 item!")
                              List<ItemSolicitacao> itemSolicitacao,

                              Integer usuarioId
){
}
