-- =====================================================================
--  TABELAS DA API DE SOLICITACAO DE EPI
--
--  RODE ISTO ANTES DE SUBIR A API PELA PRIMEIRA VEZ.
--
--  Por que na mao? Porque o ddl-auto esta em "none" de proposito: o banco
--  hhtec e do sistema PHP, e deixar o Hibernate alterar o schema dele ja
--  apagou os DEFAULT da tabela usuarios uma vez. Entao a API nao cria
--  tabela sozinha - quem cria e este arquivo.
--
--  COMO RODAR (no servidor):
--    phpMyAdmin > banco "hhtec" > aba SQL > cole tudo > Executar
--  ou pelo prompt:
--    C:\xampp\mysql\bin\mysql -u root hhtec < 0-criar-tabelas.sql
--
--  E seguro rodar mais de uma vez (IF NOT EXISTS): nao apaga nada.
-- =====================================================================

USE hhtec;

-- Cabecalho do pedido -------------------------------------------------
CREATE TABLE IF NOT EXISTS `solicitacao_epi` (
  `id`             bigint(20) NOT NULL AUTO_INCREMENT,
  `criado_em`      datetime(6) DEFAULT NULL,
  `data_aprovacao` datetime(6) DEFAULT NULL,
  `data_emissao`   date DEFAULT NULL,
  `observacoes`    varchar(255) DEFAULT NULL,
  `solicitante`    varchar(255) DEFAULT NULL,
  `status`         enum('ABERTO','TRANSFERIDO','FINALIZADO') DEFAULT NULL,
  `unidade`        enum('ADM','SAO_BERNARDO','SEDE') DEFAULT NULL,
  `usuario_id`     int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_solicitacao_epi_usuario` (`usuario_id`),
  CONSTRAINT `fk_solicitacao_epi_usuario`
      FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Itens do pedido -----------------------------------------------------
-- id e binary(16) porque a entidade usa UUID.
-- status_produto: ABERTO -> TRANSFERIDO ou COMPRADO. Quando nenhum item
-- fica ABERTO, o pedido inteiro vira FINALIZADO.
CREATE TABLE IF NOT EXISTS `itens_solicitacao_epi` (
  `id`             binary(16) NOT NULL,
  `codigo_produto` varchar(255) DEFAULT NULL,
  `descricao`      varchar(255) DEFAULT NULL,
  `observacao`     varchar(255) DEFAULT NULL,
  `id_produto`     int(11) DEFAULT NULL,
  `status_produto` varchar(20) DEFAULT 'ABERTO',
  `quantidade`     int(11) NOT NULL,
  `solicitacao_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_itens_solicitacao` (`solicitacao_id`),
  CONSTRAINT `fk_itens_solicitacao`
      FOREIGN KEY (`solicitacao_id`) REFERENCES `solicitacao_epi` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Conferencia ---------------------------------------------------------
SELECT TABLE_NAME AS tabela_criada
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = 'hhtec'
   AND TABLE_NAME IN ('solicitacao_epi','itens_solicitacao_epi');
