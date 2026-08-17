package Solicitacao.Material.Hhtec.Service;

import Solicitacao.Material.Hhtec.Exceptions.SemPermissaoException;
import Solicitacao.Material.Hhtec.Exceptions.SolicitacaoNaoEncontradaException;
import Solicitacao.Material.Hhtec.Entity.SolicitacaoEpi;
import Solicitacao.Material.Hhtec.Entity.Usuario;
import Solicitacao.Material.Hhtec.Repository.SolicitacaoEpiRepository;
import Solicitacao.Material.Hhtec.Repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

   @Autowired
   private UsuarioRepository usuarioRepository;

   @Autowired
   private SolicitacaoEpiRepository solicitacaoEpiRepository;


   public Usuario buscarPorId(Integer idUsuario){
       if (idUsuario == null) return null;
       return usuarioRepository.findById(idUsuario).orElse(null);
   }

   @Transactional
   public List<SolicitacaoEpi> listarSolicitacoesUsuario(Integer idUsuario){

       Usuario usuario = usuarioRepository.findById(idUsuario)
               .orElseThrow(() -> new SolicitacaoNaoEncontradaException("Usuário não encontrado"));

       return usuario.getSolicitacoesEpi();
   }

   @Transactional
    public List<SolicitacaoEpi> listarSolicitacoesGestor(Integer idUsuario){

       Usuario usuario = usuarioRepository.findById(idUsuario)
               .orElseThrow(()-> new SolicitacaoNaoEncontradaException("Solicitações não encontradas"));

       if(usuario.getPermGestor().equals("ST") || usuario.getPermGestor().equals("TI")){
           return solicitacaoEpiRepository.findAll();
       }
          else {
           throw new SemPermissaoException("Você não tem permissão");
       }

       }

   }

