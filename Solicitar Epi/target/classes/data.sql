-- =====================================================================
--  Dados de exemplo da demonstração.
--
--  Carregado automaticamente no H2 a cada inicialização. Em produção não
--  roda: o application.properties externo aponta para o MySQL real e não
--  liga o sql.init.
--
--  Dois usuários de propósito, para a tela poder alternar entre eles:
--    id 1 -> solicitante (perm_gestor NAO)  ... só vê os pedidos dele
--    id 2 -> gestora de ST (perm_gestor ST) ... vê todos e resolve
-- =====================================================================

INSERT INTO usuarios (id, nome, usuario, senha, perm_gestor, unidade,
                      acesso_estoque, acesso_ti, acesso_compras, acesso_adm,
                      acesso_frota, forcar_logout, status_acesso) VALUES
  (1, 'Ana Souza',    'ana.souza',    'demo', 'NAO', 'SAO BERNARDO', 1, 0, 0, 0, 0, 0, 'ativo'),
  (2, 'Marcos Lima',  'marcos.lima',  'demo', 'ST',  'SEDE',         1, 0, 0, 0, 0, 0, 'ativo');

-- Catálogo em duas unidades: a gestora fica na BASE e transfere para a filial.
-- 'regiao' é o que separa os estoques (o mesmo item não se repete entre elas).
INSERT INTO produtos (id, codigo_h, nome_completos, quantidade_estoque,
                      estoque_minimo, tipo_qr, regiao, ativo) VALUES
  -- BASE / matriz — é daqui que sai a transferência
  (1,  'H001', 'Luva de Raspa',                   40, 10, 'fixo',  '001 - ADM. HHTEC', 1),
  (2,  'H002', 'Capacete de Segurança Branco',    25,  5, 'fixo',  '001 - ADM. HHTEC', 1),
  (3,  'H003', 'Botina de Segurança 42',          18,  6, 'fixo',  '001 - ADM. HHTEC', 1),
  (4,  'H004', 'Óculos de Proteção Incolor',      60, 15, 'fixo',  '001 - ADM. HHTEC', 1),
  (5,  'H005', 'Protetor Auricular Plug',        120, 30, 'fixo',  '001 - ADM. HHTEC', 1),
  (6,  'H006', 'Máscara PFF2',                    80, 20, 'fixo',  '001 - ADM. HHTEC', 1),
  (7,  'H007', 'Cinto de Segurança Paraquedista',  6,  2, 'unico', '001 - ADM. HHTEC', 1),
  (8,  'H008', 'Avental de Raspa',                14,  4, 'fixo',  '001 - ADM. HHTEC', 1),

  -- FILIAL — é para cá que o material vai
  (20, 'H101', 'Luva de Raspa',                    2,  6, 'fixo',  '114 - SBC-BOMBAS', 1),
  (21, 'H102', 'Capacete de Segurança Branco',     1,  4, 'fixo',  '114 - SBC-BOMBAS', 1),
  (22, 'H103', 'Botina de Segurança 42',           0,  4, 'fixo',  '114 - SBC-BOMBAS', 1),
  (23, 'H104', 'Óculos de Proteção Incolor',       5, 10, 'fixo',  '114 - SBC-BOMBAS', 1),
  (24, 'H105', 'Protetor Auricular Plug',          8, 20, 'fixo',  '114 - SBC-BOMBAS', 1),
  (25, 'H106', 'Máscara PFF2',                     3, 15, 'fixo',  '114 - SBC-BOMBAS', 1);
