package Solicitacao.Material.Hhtec.Repository;

import Solicitacao.Material.Hhtec.Entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {


    List<Produto> findAllByRegiaoContainingIgnoreCase(String regiao);
}
