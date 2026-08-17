package Solicitacao.Material.Hhtec.Repository;

import Solicitacao.Material.Hhtec.Entity.ItemSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemSolicitacaoRepository extends JpaRepository<ItemSolicitacao, UUID> {
}
