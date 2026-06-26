package model;

public class GestorONG extends Usuario {
    
    private String cnpj;
    private String nomeInstituicao;
    private String telefone;
    private String cidade;

    public GestorONG() {
        super();
    }

    public GestorONG(int idUsuario, String nome, String email, String senhaHash, String tipoUsuario, 
                     String cnpj, String nomeInstituicao, String telefone, String cidade) {
        super(idUsuario, nome, email, senhaHash, tipoUsuario);
        this.cnpj = cnpj;
        this.nomeInstituicao = nomeInstituicao;
        this.telefone = telefone;
        this.cidade = cidade;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getNomeInstituicao() {
        return nomeInstituicao;
    }

    public void setNomeInstituicao(String nomeInstituicao) {
        this.nomeInstituicao = nomeInstituicao;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    @Override
    public String toString() {
        return "Gestor ONG [" + getNome() + " | Instituição: " + nomeInstituicao + " | CNPJ: " + cnpj + "]";
    }
}