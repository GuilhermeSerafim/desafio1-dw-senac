# Desafio - Servidor Web Java (3º Semestre Senac)

Este projeto implementa um **servidor HTTP simples** em Java, como parte da disciplina de **Desenvolvimento Web**.
O servidor recebe requisições, processa parâmetros enviados via **query string** e retorna respostas em **HTML** ou **JSON** conforme o primeiro tipo informado no header `Accept`; o código escolhe JSON quando esse tipo é `application/json`.

## Funcionalidades

* Interpretação de **requisições HTTP** (linha de requisição, cabeçalhos e corpo).
* Suporte a parâmetros via **GET** (`nome`, `email`).
* Resposta em:

  * **HTML** → página formatada com dados da requisição.
  * **JSON** → objeto JSON puro, conforme `Accept: application/json`.
* Emissão de **Content-Length** calculado em bytes UTF-8 e **Content-Type** conforme a resposta.
* **Thread pool** para tratar múltiplas conexões.

## Como executar

Pré-requisito declarado em `pom.xml`: Java 17.

1. Compile o projeto, a partir da raiz:

   ```bash
   javac -encoding UTF-8 -d target/classes src/main/java/br/senac/tads/dsw/*.java
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

## Origem e limites

O comentário de `SimpleWebServer.java` atribui a base a um exemplo Baeldung, identifica `fernando.tsuda` como autor da base e menciona apoio do GitHub Copilot no tratamento do corpo. O fonte identifica Guilherme da Silva Serafim no exercício. Consulte o arquivo `LICENSE` para a licença do repositório.

É um exercício de sockets/HTTP com tratamento simplificado de requisições, destinado ao estudo. A pasta `src/test` contém apenas um marcador `.gitkeep`.
