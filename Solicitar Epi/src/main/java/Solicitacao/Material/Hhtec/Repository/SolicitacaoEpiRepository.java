package Solicitacao.Material.Hhtec.Repository;

import Solicitacao.Material.Hhtec.Entity.SolicitacaoEpi;
import org.hibernate.mapping.List;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;



@Repository
public interface SolicitacaoEpiRepository extends JpaRepository<SolicitacaoEpi, Long> {


}
