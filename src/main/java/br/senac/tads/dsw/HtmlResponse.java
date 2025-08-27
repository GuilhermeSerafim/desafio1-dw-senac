package br.senac.tads.dsw;

public class HtmlResponse implements OpcaoSaida {
    @Override
    public String gerarResposta(String nome, String email) {
        return "&lthtml&gt\n &ltbody&gt\n &ltp&gtNome: " + nome + "&lt/p&gt\n &ltp&gtEmail: " + email + "&lt/p&gt\n &lt/body&gt\n&lt/html&gt";
    }
}