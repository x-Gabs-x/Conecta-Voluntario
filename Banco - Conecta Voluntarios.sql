DROP DATABASE IF EXISTS ConectaVoluntario;
CREATE DATABASE ConectaVoluntario;
USE ConectaVoluntario;

CREATE TABLE Areas_Atuacao (
    ID_Area INT PRIMARY KEY AUTO_INCREMENT,
    NomeArea VARCHAR(100) NOT NULL,
    Descricao TEXT
);

CREATE TABLE Voluntarios (
    ID_Voluntario INT PRIMARY KEY AUTO_INCREMENT,
    Nome VARCHAR(100) NOT NULL,
    CPF CHAR(11) UNIQUE NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    DataNascimento DATE,
    Cidade VARCHAR(100),
    Interesses TEXT 
);

CREATE TABLE Organizacoes (
    ID_Org INT PRIMARY KEY AUTO_INCREMENT,
    NomeInstituicao VARCHAR(150) NOT NULL,
    CNPJ CHAR(14) UNIQUE NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    Cidade VARCHAR(100),
    Telefone VARCHAR(15)
);

CREATE TABLE Oportunidades (
    ID_Oportunidade INT PRIMARY KEY AUTO_INCREMENT,
    ID_Org INT,
    Titulo VARCHAR(150) NOT NULL,
    Descricao TEXT,
    ID_Area INT,
    Max_Voluntarios INT NOT NULL, 
    Vagas_Ocupadas INT DEFAULT 0, 
    FOREIGN KEY (ID_Org) REFERENCES Organizacoes(ID_Org),
    FOREIGN KEY (ID_Area) REFERENCES Areas_Atuacao(ID_Area)
);

CREATE TABLE Inscricoes (
    ID_Inscricao INT PRIMARY KEY AUTO_INCREMENT,
    ID_Voluntario INT,
    ID_Oportunidade INT,
    Status VARCHAR(20) DEFAULT 'Pendente', 
    DataInscricao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_Voluntario) REFERENCES Voluntarios(ID_Voluntario),
    FOREIGN KEY (ID_Oportunidade) REFERENCES Oportunidades(ID_Oportunidade),
    CONSTRAINT chk_status CHECK (Status IN ('Pendente', 'Aprovado', 'Recusado', 'Concluido', 'Cancelado'))
);

CREATE TABLE HistoricoImpacto (
    ID_Historico INT PRIMARY KEY AUTO_INCREMENT,
    ID_Voluntario INT,
    ID_Oportunidade INT,
    Horas_Trabalhadas INT NOT NULL,
    DataConclusao DATE,
    FOREIGN KEY (ID_Voluntario) REFERENCES Voluntarios(ID_Voluntario),
    FOREIGN KEY (ID_Oportunidade) REFERENCES Oportunidades(ID_Oportunidade)
);

CREATE TABLE Usuarios (
    ID_Usuario INT PRIMARY KEY AUTO_INCREMENT,
    Nome VARCHAR(100),
    Email VARCHAR(100) UNIQUE,
    TipoUsuario VARCHAR(50), 
    SenhaHash VARCHAR(255)
);

CREATE TABLE Enderecos (
    id_endereco INT PRIMARY KEY AUTO_INCREMENT,
    logradouro VARCHAR(150),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    uf CHAR(2),
    id_usuario_fk INT,
    FOREIGN KEY (id_usuario_fk) REFERENCES Usuarios(ID_Usuario)
);

CREATE TABLE LogsSistema (
    ID_Log INT PRIMARY KEY AUTO_INCREMENT,
    Usuario VARCHAR(100) DEFAULT 'Sistema', 
    Acao VARCHAR(100), 
    TabelaAfetada VARCHAR(50),
    DataHora DATETIME DEFAULT CURRENT_TIMESTAMP,
    Descricao TEXT
);

DELIMITER $$

CREATE FUNCTION fn_TotalHorasVoluntario(p_id_voluntario INT)
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE v_total INT;
    SELECT IFNULL(SUM(Horas_Trabalhadas), 0) INTO v_total
    FROM HistoricoImpacto
    WHERE ID_Voluntario = p_id_voluntario;
    RETURN v_total;
END$$

CREATE FUNCTION fn_QuantidadeProjetosConcluidos(p_id_voluntario INT)
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE v_qtd INT;
    SELECT COUNT(*) INTO v_qtd
    FROM Inscricoes
    WHERE ID_Voluntario = p_id_voluntario AND Status = 'Concluido';
    RETURN v_qtd;
END$$

CREATE FUNCTION fn_CalculoVagasDisponiveis(p_id_oportunidade INT)
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE v_disponiveis INT;
    SELECT (Max_Voluntarios - Vagas_Ocupadas) INTO v_disponiveis
    FROM Oportunidades
    WHERE ID_Oportunidade = p_id_oportunidade;
    RETURN v_disponiveis;
END$$

DELIMITER ;

CREATE VIEW vw_FeedOportunidades AS
SELECT ID_Oportunidade, Titulo, Descricao, Max_Voluntarios, Vagas_Ocupadas
FROM Oportunidades
WHERE Vagas_Ocupadas < Max_Voluntarios;

CREATE VIEW vw_CandidatosPorVaga AS
SELECT o.Titulo AS Oportunidade, v.Nome AS Voluntario, i.Status, i.DataInscricao
FROM Inscricoes i
JOIN Voluntarios v ON i.ID_Voluntario = v.ID_Voluntario
JOIN Oportunidades o ON i.ID_Oportunidade = o.ID_Oportunidade;

CREATE VIEW vw_ImpactoPorONG AS
SELECT org.NomeInstituicao, IFNULL(SUM(h.Horas_Trabalhadas), 0) AS Total_Horas
FROM Organizacoes org
LEFT JOIN Oportunidades o ON org.ID_Org = o.ID_Org
LEFT JOIN HistoricoImpacto h ON o.ID_Oportunidade = h.ID_Oportunidade
GROUP BY org.ID_Org, org.NomeInstituicao;

CREATE VIEW vw_RankingVoluntarios AS
SELECT v.Nome, fn_TotalHorasVoluntario(v.ID_Voluntario) AS Total_Horas
FROM Voluntarios v
ORDER BY Total_Horas DESC;

CREATE VIEW vw_DemandasPorArea AS
SELECT a.NomeArea, COUNT(DISTINCT o.ID_Oportunidade) AS Qtd_Oportunidades, COUNT(i.ID_Inscricao) AS Total_Inscricoes
FROM Areas_Atuacao a
LEFT JOIN Oportunidades o ON a.ID_Area = o.ID_Area
LEFT JOIN Inscricoes i ON o.ID_Oportunidade = i.ID_Oportunidade
GROUP BY a.ID_Area, a.NomeArea;

CREATE VIEW vw_LogAuditoria AS
SELECT * FROM LogsSistema
ORDER BY DataHora DESC
LIMIT 20;

DELIMITER $$

CREATE TRIGGER trg_AtualizarVagas
AFTER UPDATE ON Inscricoes
FOR EACH ROW
BEGIN
    IF OLD.Status <> 'Aprovado' AND NEW.Status = 'Aprovado' THEN
        UPDATE Oportunidades 
        SET Vagas_Ocupadas = Vagas_Ocupadas + 1
        WHERE ID_Oportunidade = NEW.ID_Oportunidade;
    END IF;
END$$

CREATE TRIGGER trg_LiberarVaga
AFTER UPDATE ON Inscricoes
FOR EACH ROW
BEGIN
    IF OLD.Status = 'Aprovado' AND NEW.Status = 'Cancelado' THEN
        UPDATE Oportunidades 
        SET Vagas_Ocupadas = Vagas_Ocupadas - 1
        WHERE ID_Oportunidade = NEW.ID_Oportunidade;
    END IF;
END$$

CREATE TRIGGER trg_AuditoriaUsuarios
AFTER UPDATE ON Usuarios
FOR EACH ROW
BEGIN
    IF OLD.SenhaHash <> NEW.SenhaHash THEN
        INSERT INTO LogsSistema (Usuario, Acao, TabelaAfetada, Descricao)
        VALUES (NEW.Nome, 'ALTERAÇÃO DE SENHA', 'Usuarios', 'O usuário alterou suas credenciais de acesso.');
    END IF;
END$$

CREATE TRIGGER trg_LogOperacoesGerais
AFTER UPDATE ON Inscricoes
FOR EACH ROW
BEGIN
    INSERT INTO LogsSistema (Usuario, Acao, TabelaAfetada, Descricao)
    VALUES ('Sistema', 'UPDATE', 'Inscricoes', 
            CONCAT('Inscrição ID ', OLD.ID_Inscricao, ' alterada de ', OLD.Status, ' para ', NEW.Status));
END$$

DELIMITER ;

DELIMITER $$

CREATE PROCEDURE sp_RegistrarInscricao(
    IN p_ID_Voluntario INT,
    IN p_ID_Oportunidade INT
)
BEGIN
    DECLARE v_vagas_disponiveis INT;
    DECLARE v_inscricoes_ativas INT;
    DECLARE v_semestre_aberto INT DEFAULT 1; 
    DECLARE v_interesse_voluntario TEXT;
    DECLARE v_area_oportunidade VARCHAR(100);
    
    START TRANSACTION;
    
    IF v_semestre_aberto = 0 THEN
        INSERT INTO LogsSistema (Acao, TabelaAfetada, Descricao) 
        VALUES ('BLOQUEIO', 'Inscricoes', 'Tentativa de inscrição barrada: Semestre fechado.');
        ROLLBACK;
    ELSE
        SELECT Interesses INTO v_interesse_voluntario FROM Voluntarios WHERE ID_Voluntario = p_ID_Voluntario;
        SELECT a.NomeArea INTO v_area_oportunidade 
        FROM Oportunidades o 
        JOIN Areas_Atuacao a ON o.ID_Area = a.ID_Area 
        WHERE o.ID_Oportunidade = p_ID_Oportunidade;

        SELECT COUNT(*) INTO v_inscricoes_ativas 
        FROM Inscricoes 
        WHERE ID_Voluntario = p_ID_Voluntario AND Status IN ('Pendente', 'Aprovado');
        
        SET v_vagas_disponiveis = fn_CalculoVagasDisponiveis(p_ID_Oportunidade);
        
        IF v_inscricoes_ativas >= 3 THEN
            INSERT INTO LogsSistema (Acao, TabelaAfetada, Descricao) 
            VALUES ('BLOQUEIO', 'Inscricoes', CONCAT('Voluntário ', p_ID_Voluntario, ' tentou exceder limite de 3 projetos ativos.'));
            ROLLBACK;
        ELSEIF v_vagas_disponiveis <= 0 THEN
            INSERT INTO LogsSistema (Acao, TabelaAfetada, Descricao) 
            VALUES ('BLOQUEIO', 'Inscricoes', CONCAT('Inscrição recusada: Oportunidade ', p_ID_Oportunidade, ' sem vagas.'));
            ROLLBACK;
        ELSEIF v_interesse_voluntario NOT LIKE CONCAT('%', v_area_oportunidade, '%') THEN
            INSERT INTO LogsSistema (Acao, TabelaAfetada, Descricao) 
            VALUES ('BLOQUEIO', 'Inscricoes', CONCAT('Voluntário ', p_ID_Voluntario, ' possui perfil incompatível com a vaga.'));
            ROLLBACK;
        ELSE
            INSERT INTO Inscricoes (ID_Voluntario, ID_Oportunidade, Status) 
            VALUES (p_ID_Voluntario, p_ID_Oportunidade, 'Pendente');
            COMMIT;
        END IF;
    END IF;
END$$

CREATE PROCEDURE sp_AprovarVoluntario(IN p_id_inscricao INT)
BEGIN
    START TRANSACTION;    
    UPDATE Inscricoes SET Status = 'Aprovado' WHERE ID_Inscricao = p_id_inscricao;
    COMMIT;
END$$

CREATE PROCEDURE sp_FinalizarParticipacao(
    IN p_id_inscricao INT, 
    IN p_horas INT
)
BEGIN
    DECLARE v_voluntario INT;
    DECLARE v_oportunidade INT;
    
    START TRANSACTION;
    
    SELECT ID_Voluntario, ID_Oportunidade INTO v_voluntario, v_oportunidade 
    FROM Inscricoes 
    WHERE ID_Inscricao = p_id_inscricao;
    
    IF p_horas <= 0 THEN
        INSERT INTO LogsSistema (Acao, TabelaAfetada, Descricao) 
        VALUES ('FALHA', 'HistoricoImpacto', 'Erro de consistência: Carga horária zerada ou inválida.');
        ROLLBACK;
    ELSE
        UPDATE Inscricoes SET Status = 'Concluido' WHERE ID_Inscricao = p_id_inscricao;
        
        INSERT INTO HistoricoImpacto (ID_Voluntario, ID_Oportunidade, Horas_Trabalhadas, DataConclusao)
        VALUES (v_voluntario, v_oportunidade, p_horas, CURDATE());
        
        UPDATE Oportunidades SET Vagas_Ocupadas = Vagas_Ocupadas - 1 WHERE ID_Oportunidade = v_oportunidade;
        
        COMMIT;
    END IF;
END$$

CREATE PROCEDURE sp_CancelarInscricao(IN p_id_inscricao INT)
BEGIN
    START TRANSACTION;
    UPDATE Inscricoes SET Status = 'Cancelado' WHERE ID_Inscricao = p_id_inscricao;
    COMMIT;
END$$

DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_trava_horario_comercial
BEFORE INSERT ON Oportunidades
FOR EACH ROW
BEGIN
    IF CURTIME() < '08:00:00' OR CURTIME() > '18:00:00' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'ERRO: Operações na tabela Oportunidades permitidas apenas em horário comercial (08h às 18h).';
    END IF;
END$$
DELIMITER ;

INSERT INTO Areas_Atuacao (NomeArea, Descricao) VALUES 
('Educação', 'Aulas de reforço escolar e alfabetização.'),
('Meio Ambiente', 'Ações de reflorestamento e reciclagem.');

INSERT INTO Voluntarios (Nome, CPF, Email, DataNascimento, Cidade, Interesses) VALUES
('Carlos Silva', '11122233344', 'carlos@email.com', '1995-05-12', 'Campina Grande', 'Educação'),
('Ana Oliveira', '55566677788', 'ana@email.com', '1998-09-20', 'Campina Grande', 'Meio Ambiente'),
('Beatriz Costa', '99988877766', 'beatriz@email.com', '2000-01-15', 'João Pessoa', 'Educação');

INSERT INTO Organizacoes (NomeInstituicao, CNPJ, Email, Cidade, Telefone) VALUES
('ONG Viver Bem', '12345678000199', 'contato@viverbem.org', 'Campina Grande', '83988887777');

INSERT INTO Oportunidades (ID_Org, Titulo, Descricao, ID_Area, Max_Voluntarios) VALUES
(1, 'Professor de Matemática', 'Reforço para crianças.', 1, 1),
(1, 'Apoio em Hortas', 'Cultivo comunitário.', 2, 5);

INSERT INTO Usuarios (Nome, Email, TipoUsuario, SenhaHash) VALUES
('Admin Conecta', 'admin@conecta.com', 'Administrador', 'hash_secure_123');

CALL sp_RegistrarInscricao(1, 1);
CALL sp_AprovarVoluntario(1);     

CALL sp_RegistrarInscricao(3, 1); 

CALL sp_RegistrarInscricao(2, 1); 

CALL sp_FinalizarParticipacao(1, 0); 

CALL sp_FinalizarParticipacao(1, 15); 

UPDATE Usuarios SET SenhaHash = 'nova_senha_cripto_789' WHERE ID_Usuario = 1;

SELECT * FROM vw_RankingVoluntarios;
SELECT * FROM vw_LogAuditoria;



CALL sp_RegistrarInscricao(2, 2);

SELECT ID_Inscricao, ID_Voluntario, Status FROM Inscricoes;

CALL sp_AprovarVoluntario(2);

CALL sp_FinalizarParticipacao(2, 12);

SELECT Titulo, Vagas_Ocupadas FROM Oportunidades WHERE ID_Oportunidade = 2;

SELECT * FROM HistoricoImpacto WHERE ID_Voluntario = 2;

CREATE USER IF NOT EXISTS 'usr_admin'@'localhost' IDENTIFIED BY 'admin123';
GRANT ALL PRIVILEGES ON ConectaVoluntario.* TO 'usr_admin'@'localhost';

CREATE USER IF NOT EXISTS 'usr_gestor_ong'@'localhost' IDENTIFIED BY 'gestor123';
GRANT SELECT, INSERT, UPDATE, DELETE ON ConectaVoluntario.* TO 'usr_gestor_ong'@'localhost';
REVOKE DELETE ON ConectaVoluntario.LogsSistema FROM 'usr_gestor_ong'@'localhost';


CREATE USER IF NOT EXISTS 'usr_iniciante'@'localhost' IDENTIFIED BY 'iniciante123';
GRANT SELECT, INSERT ON ConectaVoluntario.* TO 'usr_iniciante'@'localhost';
REVOKE DELETE ON ConectaVoluntario.Oportunidades FROM 'usr_iniciante'@'localhost';

CREATE USER IF NOT EXISTS 'usr_visitante'@'localhost' IDENTIFIED BY 'visitante123';
GRANT SELECT ON ConectaVoluntario.vw_FeedOportunidades TO 'usr_visitante'@'localhost';

FLUSH PRIVILEGES;