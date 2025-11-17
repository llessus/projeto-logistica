-- #################################################
-- # PROJETO LOGÍSTICA - SCRIPT DE SETUP COMPLETO
-- # Executar este script para criar todas as tabelas,
-- # lógica avançada (Triggers/Functions) e dados iniciais.
-- #################################################

-- Garante que o banco existe e seleciona ele
CREATE DATABASE IF NOT EXISTS projeto_logistica;
USE projeto_logistica;

DROP TRIGGER IF EXISTS trg_log_novo_pedido;
DROP TRIGGER IF EXISTS trg_log_atualiza_status;
DROP TRIGGER IF EXISTS trg_gerar_codigo_pedido;
DROP FUNCTION IF EXISTS fn_gerar_rastreio;
DROP FUNCTION IF EXISTS fn_contar_pedidos_cliente;
DROP PROCEDURE IF EXISTS sp_criar_pedido;
DROP VIEW IF EXISTS vw_detalhes_usuarios;
DROP VIEW IF EXISTS vw_pedidos_pendentes;


DROP TABLE IF EXISTS pedidos;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS grupos_usuarios;
DROP TABLE IF EXISTS logs;
DROP TABLE IF EXISTS sequencia_rastreio;


-- #############################################
-- 1. CRIAÇÃO DAS TABELAS (DDL)
-- #############################################

-- Tabela de Grupos (Requisito de Permissões)
CREATE TABLE grupos_usuarios (
    id_grupo INT NOT NULL,
    nome_grupo VARCHAR(50) NOT NULL,
    descricao VARCHAR(255),
    PRIMARY KEY (id_grupo)
) ENGINE=InnoDB;

-- Tabela de Usuários (Requisito obrigatório)
CREATE TABLE usuarios (
    id_usuario BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    id_grupo INT,
    PRIMARY KEY (id_usuario),
    FOREIGN KEY (id_grupo) REFERENCES grupos_usuarios(id_grupo)
) ENGINE=InnoDB;

-- Tabela de Logs de Auditoria (Usada pelas Triggers)
CREATE TABLE logs (
    id_log BIGINT NOT NULL AUTO_INCREMENT,
    acao VARCHAR(500) NOT NULL,
    data_log DATETIME(6),
    usuario VARCHAR(50),
    PRIMARY KEY (id_log)
) ENGINE=InnoDB;

-- Tabela de Pedidos (Tabela Principal)
CREATE TABLE pedidos (
    id_pedido BIGINT NOT NULL AUTO_INCREMENT,
    nome_cliente VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    data_pedido DATETIME(6),
    codigo_rastreio VARCHAR(20) UNIQUE,
    PRIMARY KEY (id_pedido)
) ENGINE=InnoDB;

-- Tabela para Controle da Sequência do Código de Rastreio (Requisito ID Personalizado)
CREATE TABLE sequencia_rastreio (
    ano INT NOT NULL,
    ultimo_numero INT DEFAULT 0,
    PRIMARY KEY (ano)
) ENGINE=InnoDB;


-- #############################################
-- 2. CRIAÇÃO DE ÍNDICES (Otimização)
-- #############################################

CREATE INDEX idx_usuario_email ON usuarios(email);
CREATE INDEX idx_pedido_status ON pedidos(status);


-- #############################################
-- 3. INSERÇÃO DE DADOS INICIAIS (DML)
-- #############################################

-- Dados de Grupos e Usuários de Login
INSERT INTO grupos_usuarios (id_grupo, nome_grupo, descricao) VALUES 
(1, 'ADMIN', 'Acesso total ao sistema de gestão'),
(2, 'LOGISTICA', 'Visualizar e editar pedidos');

INSERT INTO usuarios (nome, email, senha, id_grupo) VALUES 
('Brendon Admin', 'brendon@admin.com', 'Brendonrussell12@', 1),
('Carlos Logistica', 'carlos@logistica.com', '123456', 2);

-- Inicializa a sequência de ID Personalizado para o ano atual
INSERT INTO sequencia_rastreio (ano, ultimo_numero) VALUES (2025, 0);


-- #############################################
-- 4. FUNÇÕES E PROCEDURES (Lógica Requisitada)
-- #############################################

DELIMITER $$

-- 4.1. FUNÇÃO: Gera o código de rastreio (Ex: LOG-2025-0001)
CREATE FUNCTION fn_gerar_rastreio() 
RETURNS VARCHAR(20)
DETERMINISTIC
BEGIN
    DECLARE novo_valor INT;
    DECLARE codigo_formatado VARCHAR(20);
    
    UPDATE sequencia_rastreio 
    SET ultimo_numero = ultimo_numero + 1 
    WHERE ano = 2025;
    
    SELECT ultimo_numero INTO novo_valor FROM sequencia_rastreio WHERE ano = 2025;
    
    SET codigo_formatado = CONCAT('LOG-2025-', LPAD(novo_valor, 4, '0'));
    
    RETURN codigo_formatado;
END$$

-- 4.2. PROCEDURE: Para criar pedidos (Lógica de Padronização)
CREATE PROCEDURE sp_criar_pedido (
    IN p_nome_cliente VARCHAR(255),
    IN p_status VARCHAR(255)
)
BEGIN
    IF p_status IS NULL OR p_status = '' THEN
        SET p_status = 'PENDENTE';
    END IF;

    INSERT INTO pedidos (data_pedido, nome_cliente, status) 
    VALUES (NOW(), p_nome_cliente, p_status);
END$$

-- 4.3. FUNCTION: Contar pedidos por cliente (Lógica de Relatório)
CREATE FUNCTION fn_contar_pedidos_cliente (p_nome_cliente VARCHAR(255)) 
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE total INT;
    SELECT COUNT(*) INTO total 
    FROM pedidos 
    WHERE nome_cliente = p_nome_cliente;
    RETURN total;
END$$

DELIMITER ;


-- #############################################
-- 5. CRIAÇÃO DE VIEWS (Relatórios)
-- #############################################

-- View 1: Detalhes do Usuário (Oculta a senha por segurança)
CREATE VIEW vw_detalhes_usuarios AS
SELECT 
    u.id_usuario,
    u.nome AS nome_usuario,
    u.email,
    g.nome_grupo AS perfil
FROM usuarios u
JOIN grupos_usuarios g ON u.id_grupo = g.id_grupo;

-- View 2: Pedidos Pendentes (Filtro Operacional)
CREATE VIEW vw_pedidos_pendentes AS
SELECT 
    id_pedido,
    codigo_rastreio,
    nome_cliente,
    data_pedido,
    status
FROM pedidos
WHERE status = 'PENDENTE'
ORDER BY data_pedido DESC;


-- #############################################
-- 6. CRIAÇÃO DE TRIGGERS (Automação e Auditoria)
-- #############################################

DELIMITER $$

-- Trigger A: PREENCHER o Código de Rastreio (antes de salvar)
CREATE TRIGGER trg_gerar_codigo_pedido
BEFORE INSERT ON pedidos
FOR EACH ROW
BEGIN
    SET NEW.codigo_rastreio = fn_gerar_rastreio();
END$$

-- Trigger B: LOG de NOVOS Pedidos (após salvar)
CREATE TRIGGER trg_log_novo_pedido
AFTER INSERT ON pedidos
FOR EACH ROW
BEGIN
    INSERT INTO logs (acao, data_log, usuario)
    VALUES (CONCAT('Novo pedido criado. ID: ', NEW.id_pedido, ' / Rastreio: ', NEW.codigo_rastreio), NOW(), 'SISTEMA');
END$$

-- Trigger C: LOG de MUDANÇA de Status
CREATE TRIGGER trg_log_atualiza_status
AFTER UPDATE ON pedidos
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO logs (acao, data_log, usuario)
        VALUES (CONCAT('Status do pedido ', OLD.codigo_rastreio, ' alterado de ', OLD.status, ' para ', NEW.status), NOW(), 'SISTEMA');
    END IF;
END$$

DELIMITER ;


-- #############################################
-- 7. INSERÇÃO DE DADOS DE TESTE (Para Apresentação)
-- #############################################
INSERT INTO pedidos (nome_cliente, status, data_pedido) VALUES 
('Tech Solutions Ltda', 'ENTREGUE', NOW() - INTERVAL 5 DAY),
('Supermercado Bom Preço', 'ENTREGUE', NOW() - INTERVAL 3 DAY),
('Construtora Horizonte', 'ENTREGUE', NOW() - INTERVAL 2 DAY),
('Hospital Santa Clara', 'PENDENTE', NOW() - INTERVAL 5 HOUR),
('Escola Mundo do Saber', 'PENDENTE', NOW() - INTERVAL 2 HOUR),
('Roberto Carlos (Pessoa Física)', 'PENDENTE', NOW() - INTERVAL 30 MINUTE),
('Farmácia Saúde Total', 'PENDENTE', NOW());

-- Exemplo de uso da Trigger de UPDATE (Marcar um como entregue)
-- Isso vai gerar um log extra automaticamente
UPDATE pedidos SET status = 'ENTREGUE' WHERE codigo_rastreio = 'LOG-2025-0004';