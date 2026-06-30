package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    private static final String URL = "jdbc:mysql://localhost:3306/ConectaVoluntario";

    public static Connection conectar(String usuario, String senha) {
        Connection conexao = null;
        try {            Class.forName("com.mysql.cj.jdbc.Driver");
            
            conexao = DriverManager.getConnection(URL, usuario, senha);
            System.out.println("Sucesso: Conectado ao banco como '" + usuario + "'");
            
        } catch (ClassNotFoundException e) {
            System.out.println("Erro FATAL: Driver do MySQL (JDBC) não encontrado!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Erro de Credencial ou Banco Offline para o usuário: " + usuario);
            e.printStackTrace();
        }
        return conexao;
    }

    public static void desconectar(Connection conexao) {
        if (conexao != null) {
            try {
                conexao.close();
                System.out.println("Conexão encerrada com sucesso.");
            } catch (SQLException e) {
                System.out.println("Erro ao fechar a conexão.");
                e.printStackTrace();
            }
        }
    }
}