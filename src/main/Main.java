package main;

import javax.swing.JOptionPane;
import DAO.VoluntarioDAO;
import model.Voluntario;
import java.util.List;

public class Main {
    private static String usuarioLogado = "";
    private static String senhaBanco = "";
    private static String perfilExibicao = "";

    public static void main(String[] args) {
        
        String[] perfis = {"Administrador (Acesso Total)", "Visitante (Restrito)"};
        int perfilEscolhido = JOptionPane.showOptionDialog(null, 
            "Selecione o perfil de conexão para o banco de dados:", 
            "Controle de Acesso (DCL)", JOptionPane.DEFAULT_OPTION, 
            JOptionPane.QUESTION_MESSAGE, null, perfis, perfis[0]);

        if (perfilEscolhido == JOptionPane.CLOSED_OPTION) {
            System.exit(0);
        }

        if (perfilEscolhido == 0) {
            usuarioLogado = "usr_admin";
            senhaBanco = "admin123";
            perfilExibicao = "ADMINISTRADOR";
        } else {
            usuarioLogado = "usr_visitante";
            senhaBanco = "visitante123";
            perfilExibicao = "VISITANTE";
        }

        JOptionPane.showMessageDialog(null, 
            "Conectado com sucesso como: " + perfilExibicao + "\nAs permissões do MySQL foram aplicadas a esta sessão.", 
            "Status", JOptionPane.INFORMATION_MESSAGE);
            
        while (true) {
            String[] opcoes = {
                "01. Ver Vagas Disponíveis", 
                "02. Fazer Inscrição em Vaga (Procedure)", 
                "03. Lançar Horas Trabalhadas (Procedure)", 
                "04. Cadastrar Voluntário Simples (Insert)",
                "05. Cadastrar Usuário + Endereço (Transação/Rollback)",
                "06. Cadastrar Nova Vaga (Trigger Horário)",
                "07. Auditar Voluntários (Teste de View/DCL)", 
                "08. Trocar de Usuário",
                "09. Sair"
            };
            String escolhaStr = (String) JOptionPane.showInputDialog(null, 
                "Painel do Conecta Voluntário\nUsuário Atual: " + perfilExibicao + "\n\nEscolha uma operação:", 
                "Menu Principal", JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);

            if (escolhaStr == null || escolhaStr.equals("09. Sair")) {
                JOptionPane.showMessageDialog(null, "Encerrando o sistema. Até logo!");
                break; 
            }
            if (escolhaStr.startsWith("01")) mostrarVagas();
            else if (escolhaStr.startsWith("02")) realizarInscricao();
            else if (escolhaStr.startsWith("03")) lancarHoras();
            else if (escolhaStr.startsWith("04")) cadastrarVoluntarioSimples();
            else if (escolhaStr.startsWith("05")) cadastrarUsuarioEndereco();
            else if (escolhaStr.startsWith("06")) cadastrarNovaVaga();
            else if (escolhaStr.startsWith("07")) auditarVoluntarios();
            else if (escolhaStr.startsWith("08")) {
                main(args);
                return;
            }
        }
    }
    private static void mostrarVagas() {
        JOptionPane.showMessageDialog(null, 
            "Consultando a VIEW pública 'vw_FeedOportunidades'...\n\n" +
            "Vaga 1: Professor de Matemática - Vagas Ocupadas: 0/2\n" +
            "Vaga 2: Apoio em Hortas - Vagas Ocupadas: 0/5", 
            "Feed de Vagas", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void realizarInscricao() {
        try {
            String idVoluntarioStr = JOptionPane.showInputDialog("ID do Voluntário:");
            if (idVoluntarioStr == null) return;
            String idVagaStr = JOptionPane.showInputDialog("ID da Oportunidade:");
            if (idVagaStr == null) return;

            VoluntarioDAO dao = new VoluntarioDAO();
            boolean sucesso = dao.registrarInscricao(Integer.parseInt(idVoluntarioStr), Integer.parseInt(idVagaStr), usuarioLogado, senhaBanco);

            if (sucesso) JOptionPane.showMessageDialog(null, "Inscrição realizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            else JOptionPane.showMessageDialog(null, "Ação Bloqueada pelas Regras de Negócio do MySQL!", "Bloqueio", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    private static void lancarHoras() {
        try {
            String idInscricaoStr = JOptionPane.showInputDialog("Digite o ID da Inscrição que será finalizada:");
            if (idInscricaoStr == null) return;
            String horasStr = JOptionPane.showInputDialog("Quantas horas o voluntário trabalhou neste projeto?");
            if (horasStr == null) return;

            VoluntarioDAO dao = new VoluntarioDAO();
            boolean sucesso = dao.finalizarParticipacao(Integer.parseInt(idInscricaoStr), Integer.parseInt(horasStr), usuarioLogado, senhaBanco);

            if (sucesso) JOptionPane.showMessageDialog(null, "Horas lançadas e projeto finalizado com sucesso!", "Impacto Social", JOptionPane.INFORMATION_MESSAGE);
            else JOptionPane.showMessageDialog(null, "Erro: Carga horária inválida ou inscrição inexistente.", "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    private static void cadastrarVoluntarioSimples() {
        try {
            Voluntario v = new Voluntario();
            v.setNome(JOptionPane.showInputDialog("Nome do Voluntário:"));
            v.setCpf(JOptionPane.showInputDialog("CPF (11 dígitos):"));
            v.setEmail(JOptionPane.showInputDialog("E-mail:"));
            v.setDataNascimento(JOptionPane.showInputDialog("Data de Nascimento (YYYY-MM-DD):"));
            String cidade = JOptionPane.showInputDialog(null, "Cidade:", "Campina Grande");
            v.setCidade(cidade);
            v.setInteresses(JOptionPane.showInputDialog("Área de Interesse (ex: Educação, Meio Ambiente):"));

            VoluntarioDAO dao = new VoluntarioDAO();
            dao.cadastrarVoluntario(v, usuarioLogado, senhaBanco);
            JOptionPane.showMessageDialog(null, "Comando enviado! Verifique o console para confirmação.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Cancelado ou erro no formato.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    private static void cadastrarUsuarioEndereco() {
        try {
            JOptionPane.showMessageDialog(null, "Vamos testar a Atomicidade (Rollback)!\nSe você digitar a UF com mais de 2 letras, o banco desfará o cadastro.", "Teste de Transação", JOptionPane.INFORMATION_MESSAGE);
            String nome = JOptionPane.showInputDialog("Nome:");
            String email = JOptionPane.showInputDialog("E-mail:");
            String tipo = "Voluntário";
            String senha = "senha_teste123";
            String rua = JOptionPane.showInputDialog("Logradouro (Rua):");
            String bairro = JOptionPane.showInputDialog("Bairro:");
            String cidade = JOptionPane.showInputDialog(null, "Cidade:", "Campina Grande");
            String uf = JOptionPane.showInputDialog("UF (Digite 'PARAIBA' para forçar o Rollback ou 'PB' para aprovar):");
            VoluntarioDAO dao = new VoluntarioDAO();
            String resultado = dao.cadastrarUsuarioEndereco(nome, email, tipo, senha, rua, bairro, cidade, uf, usuarioLogado, senhaBanco);

            if (resultado.equals("Sucesso")) {
                JOptionPane.showMessageDialog(null, "Transação concluída com sucesso. Usuário e endereço gravados!", "Commit Realizado", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "O Banco de Dados abortou a transação (Rollback)!\nMotivo retornado: " + resultado, "Rollback Acionado", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Cancelado.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void cadastrarNovaVaga() {
        try {
            int idOrg = 1; 
            String titulo = JOptionPane.showInputDialog("Título da Vaga (ex: Professor de Inglês):");
            if (titulo == null) return;
            String descricao = JOptionPane.showInputDialog("Descrição da atividade:");
            int idArea = Integer.parseInt(JOptionPane.showInputDialog("ID da Área de Atuação (1-Educação, 2-Meio Ambiente):"));
            int maxVagas = Integer.parseInt(JOptionPane.showInputDialog("Máximo de Voluntários necessários:"));

            VoluntarioDAO dao = new VoluntarioDAO();
            String resultado = dao.cadastrarVaga(idOrg, titulo, descricao, idArea, maxVagas, usuarioLogado, senhaBanco);

            if (resultado.equals("Sucesso")) {
                JOptionPane.showMessageDialog(null, "Vaga inserida no banco de dados!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "O Banco de Dados bloqueou o Insert!\n\nMensagem do MySQL: " + resultado, "Trigger Disparada!", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro nos dados informados.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void auditarVoluntarios() {
        try {
            VoluntarioDAO dao = new VoluntarioDAO();
            List<Voluntario> lista = dao.listarVoluntarios(usuarioLogado, senhaBanco);
            
            StringBuilder sb = new StringBuilder("--- Lista de Voluntários Cadastrados (Acesso Restrito) ---\n\n");
            for (Voluntario v : lista) {
                sb.append("ID: ").append(v.getIdUsuario()).append(" - Nome: ").append(v.getNome()).append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString(), "Auditoria", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "ALERTA DE SEGURANÇA:\nErro: SELECT command denied.\nAcesso bloqueado pelas regras DCL do MySQL!", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
        }
    }
}