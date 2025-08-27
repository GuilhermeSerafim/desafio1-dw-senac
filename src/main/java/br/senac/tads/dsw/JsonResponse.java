package br.senac.tads.dsw;

public class JsonResponse implements OpcaoSaida {
    @Override
    public String gerarResposta(String nome, String email) {
        return "{\n \"nome\": \"" + nome + "\",\n \"email\": \"" + email + "\"\n}";
    }
}