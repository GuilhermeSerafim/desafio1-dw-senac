# Desafio - Servidor Web Java (3º Semestre Senac)

Este projeto implementa um **servidor HTTP simples** em Java, como parte da disciplina de **Desenvolvimento Web**.
O servidor recebe requisições, processa parâmetros enviados via **query string** e retorna respostas em **HTML** ou **JSON** conforme o header `Accept`.

## Funcionalidades

* Interpretação de **requisições HTTP** (linha de requisição, cabeçalhos e corpo).
* Suporte a parâmetros via **GET** (`nome`, `email`).
* Resposta em:

  * **HTML** → página formatada com dados da requisição.
  * **JSON** → objeto JSON puro, conforme `Accept: application/json`.
* Cálculo correto de **Content-Length** e **Content-Type**.
* **Thread pool** para tratar múltiplas conexões.

## Como executar

1. Compile o projeto:

   ```bash
   javac -d target/classes src/br/senac/tads/dsw/*.java
   ```
2. Inicie o servidor:

   ```bash
   java -cp target/classes br.senac.tads.dsw.SimpleWebServer
   ```
3. Acesse no navegador ou via `curl`:

   ```bash
   curl -v -H "Accept: application/json" "http://localhost:8080/?nome=Fulano&email=fulano@email.com"
   curl -v -H "Accept: text/html" "http://127.0.0.1:8080/?nome=Fulano&email=fulano@email.com"
   ```
   
Desenvolvido para a disciplina **Desenvolvimento Web - 3º Semestre Senac**.
