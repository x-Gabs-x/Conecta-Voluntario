package model;

public class Voluntario extends Usuario {
    
    private String cpf;
    private String dataNascimento;
    private String cidade;
    private String interesses;

    public Voluntario() {
        super();
    }

    public Voluntario(int idUsuario, String nome, String email, String senhaHash, String tipoUsuario, 
                      String cpf, String dataNascimento, String cidade, String interesses) {
        super(idUsuario, nome, email, senhaHash, tipoUsuario);
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.cidade = cidade;
        this.interesses = interesses;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getInteresses() {
        return interesses;
    }

    public void setInteresses(String interesses) {
        this.interesses = interesses;
    }

    @Override
    public String toString() {
        return "Voluntario [" + getNome() + " | CPF: " + cpf + " | Interesses: " + interesses + "]";
    }
}