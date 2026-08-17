package Solicitacao.Material.Hhtec.Dto;


import Solicitacao.Material.Hhtec.Enum.Status;

import jakarta.validation.constraints.NotNull;


public record EditarStatusDto(


        Long idSolicitacao,


        Status status,

        String nomeUsuario,

        String permGestor
) {
}
