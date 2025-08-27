package br.senac.tads.dsw;

public class HtmlResponse implements OpcaoSaida {
    @Override
    public String gerarResposta(String nome, String email) {
        return "<html>\n <body>\n <p>Nome: " + nome + "</p>\n <p>Email: " + email + "</p>\n </body>\n</html>";
    }
}