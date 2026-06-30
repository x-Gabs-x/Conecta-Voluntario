package DAO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Voluntario;

public class VoluntarioDAO {
    
    public void cadastrarVoluntario(Voluntario voluntario, String usuarioLogado, String senhaBanco) {
        String sql = "INSERT INTO Voluntarios (Nome, CPF, Email, DataNascimento, Cidade, Interesses) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstm = null;

        try {
            conn = Conexao.conectar(usuarioLogado, senhaBanco);
            
            if (conn != null) {
                pstm = conn.prepareStatement(sql);
                
                pstm.setString(1, voluntario.getNome());
                pstm.setString(2, voluntario.getCpf());
                pstm.setString(3, voluntario.getEmail());
                pstm.setString(4, voluntario.getDataNascimento());
                pstm.setString(5, voluntario.getCidade());
                pstm.setString(6, voluntario.getInteresses());

                pstm.execute();
                System.out.println("Sucesso: Voluntário '" + voluntario.getNome() + "' cadastrado no banco!");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao cadastrar voluntário: " + e.getMessage());
        } finally {
            try {
                if (pstm != null) pstm.close();
                Conexao.desconectar(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Voluntario> listarVoluntarios(String usuarioLogado, String senhaBanco) {
        String sql = "SELECT * FROM Voluntarios";
        List<Voluntario> listaVoluntarios = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rset = null;

        try {
            conn = Conexao.conectar(usuarioLogado, senhaBanco);
            
            if (conn != null) {
                pstm = conn.prepareStatement(sql);
                rset = pstm.executeQuery();

                while (rset.next()) {
                    Voluntario v = new Voluntario();
                    v.setIdUsuario(rset.getInt("ID_Voluntario"));
                    v.setNome(rset.getString("Nome"));
                    v.setCpf(rset.getString("CPF"));
                    v.setEmail(rset.getString("Email"));
                    v.setDataNascimento(rset.getString("DataNascimento"));
                    v.setCidade(rset.getString("Cidade"));
                    v.setInteresses(rset.getString("Interesses"));

                    listaVoluntarios.add(v);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar voluntários: " + e.getMessage());
        } finally {
            try {
                if (rset != null) rset.close();
                if (pstm != null) pstm.close();
                Conexao.desconectar(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return listaVoluntarios;
    }
    
    public boolean registrarInscricao(int idVoluntario, int idOportunidade, String usuarioLogado, String senhaBanco) {
        String sql = "{CALL sp_RegistrarInscricao(?, ?)}";
        Connection conn = null;
        CallableStatement cstm = null;

        try {
            conn = Conexao.conectar(usuarioLogado, senhaBanco);
            
            if (conn != null) {
                cstm = conn.prepareCall(sql);
                cstm.setInt(1, idVoluntario);
                cstm.setInt(2, idOportunidade);
                cstm.execute();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Transação bloqueada pelo banco: " + e.getMessage());
            return false; 
        } finally {
            try {
                if (cstm != null) cstm.close();
                Conexao.desconectar(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public boolean finalizarParticipacao(int idInscricao, int horas, String usuarioLogado, String senhaBanco) {
        String sql = "{CALL sp_FinalizarParticipacao(?, ?)}";
        java.sql.Connection conn = null;
        java.sql.CallableStatement cstm = null;
        try {
            conn = Conexao.conectar(usuarioLogado, senhaBanco);
            if (conn != null) {
                cstm = conn.prepareCall(sql);
                cstm.setInt(1, idInscricao);
                cstm.setInt(2, horas);
                cstm.execute();
                return true;
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Erro ao lançar horas: " + e.getMessage());
            return false;
        } finally {
            try { if (cstm != null) cstm.close(); Conexao.desconectar(conn); } catch (Exception e) {}
        }
        return false;
    }
    public String cadastrarUsuarioEndereco(String nome, String email, String tipo, String senha, String logradouro, String bairro, String cidade, String uf, String usuarioLogado, String senhaBanco) {
        String sql = "{CALL sp_transacao_cadastro_completo(?, ?, ?, ?, ?, ?, ?, ?)}";
        java.sql.Connection conn = null;
        java.sql.CallableStatement cstm = null;
        try {
            conn = Conexao.conectar(usuarioLogado, senhaBanco);
            if (conn != null) {
                cstm = conn.prepareCall(sql);
                cstm.setString(1, nome);
                cstm.setString(2, email);
                cstm.setString(3, tipo);
                cstm.setString(4, senha);
                cstm.setString(5, logradouro);
                cstm.setString(6, bairro);
                cstm.setString(7, cidade);
                cstm.setString(8, uf);
                cstm.execute();
                return "Sucesso";
            }
        } catch (java.sql.SQLException e) {
            return e.getMessage(); 
        } finally {
            try { if (cstm != null) cstm.close(); Conexao.desconectar(conn); } catch (Exception e) {}
        }
        return "Erro de conexão";
    }
    public String cadastrarVaga(int idOrg, String titulo, String descricao, int idArea, int maxVoluntarios, String usuarioLogado, String senhaBanco) {
        String sql = "INSERT INTO Oportunidades (ID_Org, Titulo, Descricao, ID_Area, Max_Voluntarios) VALUES (?, ?, ?, ?, ?)";
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstm = null;
        try {
            conn = Conexao.conectar(usuarioLogado, senhaBanco);
            if (conn != null) {
                pstm = conn.prepareStatement(sql);
                pstm.setInt(1, idOrg);
                pstm.setString(2, titulo);
                pstm.setString(3, descricao);
                pstm.setInt(4, idArea);
                pstm.setInt(5, maxVoluntarios);
                pstm.execute();
                return "Sucesso";
            }
        } catch (java.sql.SQLException e) {
            return e.getMessage(); 
        } finally {
            try { if (pstm != null) pstm.close(); Conexao.desconectar(conn); } catch (Exception e) {}
        }
        return "Erro de conexão";
    }
    // Método Exclusivo para o TESTE DE FOGO (Armadilha do Iniciante)
    public void excluirOportunidade(int idOportunidade, String usuarioLogado, String senhaBanco) {
        String sql = "DELETE FROM Oportunidades WHERE ID_Oportunidade = ?";
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstm = null;
        try {
            conn = Conexao.conectar(usuarioLogado, senhaBanco);
            if (conn != null) {
                pstm = conn.prepareStatement(sql);
                pstm.setInt(1, idOportunidade);
                pstm.executeUpdate();
                javax.swing.JOptionPane.showMessageDialog(null, 
                    "Oportunidade excluída com sucesso! (Trigger de log acionada no banco)", 
                    "Sucesso", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (java.sql.SQLException e) {
javax.swing.JOptionPane.showMessageDialog(null, 
                "ERRO: Acesso Negado!\nSeu perfil de usuário não tem permissão para excluir registros do sistema.\n\nDetalhe do Banco: " + e.getMessage(), 
                "Bloqueio de Segurança (DCL)", javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            try { if (pstm != null) pstm.close(); Conexao.desconectar(conn); } catch (Exception e) {}
        }
    }
}