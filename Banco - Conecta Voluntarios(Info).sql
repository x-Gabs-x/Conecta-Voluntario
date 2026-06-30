USE ConectaVoluntario;

INSERT INTO Areas_Atuacao (NomeArea, Descricao) VALUES 
('Educação', 'Aulas de reforço escolar e alfabetização.'),
('Meio Ambiente', 'Ações de reflorestamento e reciclagem.');

INSERT INTO Voluntarios (Nome, CPF, Email, DataNascimento, Cidade, Interesses) VALUES
('Carlos Silva', '11122233344', 'carlos@email.com', '1995-05-12', 'Campina Grande', 'Educação'),
('Ana Oliveira', '55566677788', 'ana@email.com', '1998-09-20', 'Campina Grande', 'Meio Ambiente'),
('Beatriz Costa', '99988877766', 'beatriz@email.com', '2000-01-15', 'João Pessoa', 'Educação');

INSERT INTO Organizacoes (NomeInstituicao, CNPJ, Email, Cidade, Telefone) VALUES
('ONG Viver Bem', '12345678000199', 'contato@viverbem.org', 'Campina Grande', '83988887777');

INSERT INTO Usuarios (Nome, Email, TipoUsuario, SenhaHash) VALUES
('Admin Conecta', 'admin@conecta.com', 'Administrador', 'hash_secure_123');

-- Não inserimos vagas (Oportunidades) aqui por conta da Trigger de horário estar ligada e bloquearia o INSERT!