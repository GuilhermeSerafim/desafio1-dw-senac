package br.senac.tads.dsw;

// Guilherme da Silva Serafim, STADSCAS3NA

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Exemplo obtido em
 * https://www.baeldung.com/java-serversocket-simple-http-server
 * Alterado com apoio do Github Copilot para tratamento do request body
 *
 * @author fernando.tsuda
 */
public class SimpleWebServer {

	private final int port;
	private static final int THREAD_POOL_SIZE = 10;

	public SimpleWebServer(int port) {
		this.port = port;
	}

	public static void main(String[] args) throws IOException {
		int port = 8080;
		SimpleWebServer server = new SimpleWebServer(port);
		server.start();
	}

	public void start() throws IOException {

		// // PARA JAVA 21+ - usa virtual threads
		// ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor();

		ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Server started na porta " + port);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				threadPool.execute(() -> handleClient(clientSocket));
			}
		}
	}

	private void handleClient(Socket clientSocket) {
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8))) {

			String requestLine = "";
			StringBuilder requestHeaders = new StringBuilder();
			boolean collectinRequestLine = true;

			int contentLength = 0;
			String requestInputLine;
			while ((requestInputLine = in.readLine()) != null) {
				if (collectinRequestLine) {
					requestLine = requestInputLine;
					collectinRequestLine = false;
					continue;
				}
				if (requestInputLine.isEmpty())
					break;
				requestHeaders.append(requestInputLine).append("\r\n");
				if (requestInputLine.toLowerCase().startsWith("content-length:")) {
					contentLength = Integer.parseInt(requestInputLine.split(":", 2)[1].trim());
				}
			}
			String header = requestHeaders.toString();

			String body = "";
			if (contentLength > 0) {
				char[] bodyChars = new char[contentLength];
				in.read(bodyChars, 0, contentLength);
				body = new String(bodyChars);
			}

			String requestMessage = requestLine + "\r\n" + header + "\r\n" + body;

			HtmlResponse htmlResponse = new HtmlResponse();
			JsonResponse jsonResponse = new JsonResponse();

			// --- URL + query params (usa seu helper parseQuery) ---
			String url = requestLine != null ? requestLine.split(" ")[1] : "";
			if (url.isEmpty())
				return;
			Map<String, String> qp = parseQuery(url);
			String nome = qp.getOrDefault("nome", "");
			String email = qp.getOrDefault("email", "");

			// --- Accept básico (pega só o 1º tipo) ---
			String accept = "text/html";
			for (String line : header.split("\r\n")) {
				if (line.toLowerCase(Locale.ROOT).startsWith("accept:")) {
					String v = line.substring(7).trim().toLowerCase(Locale.ROOT);
					accept = v.split(",")[0].split(";")[0].trim();
					break;
				}
			}

			// --- Gera payload de acordo com Accept (NUNCA use MessageFormat no JSON) ---
			String responsePayload;
			boolean wantsJson = "application/json".equalsIgnoreCase(accept);
			if (wantsJson) {
				responsePayload = jsonResponse.gerarResposta(nome, email); // DEVE ser JSON puro
			} else {
				responsePayload = htmlResponse.gerarResposta(nome, email); // string “humana”
			}

			// --- Monta body final ---
			String responseBody;
			if (wantsJson) {
				// JSON puro na resposta, sem HTML
				responseBody = responsePayload.trim();
			} else {
				// Página HTML com request e o “formato escolhido”
				String bodyOutTemplate = """
						<!doctype html>
						<html>
						  <head>
						    <meta charset="UTF-8">
						    <title>TADS DSW</title>
						  </head>
						  <body>
						    <h1>Exemplo Servidor Web Java</h1>
						    <p>Teste alteração</p>
						    <hr>
						    <h2>Mensagem Request</h2>
						    <pre>{0}</pre>
						    <h2>Mensagem no formato escolhido {1}</h2>
						    <pre>{2}</pre>
						    <hr>
						  </body>
						</html>
						""";
				// ATENÇÃO: usar MessageFormat SÓ no template HTML.
				// Coloque o payload no <pre> já “como texto”, então não renderize tags.
				String formato = "HTML"; // rótulo exibido
				String safeRequest = requestMessage.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
						.replace("\"", "&quot;").replace("'", "&#39;");
				String safePayload = responsePayload.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
						.replace("\"", "&quot;").replace("'", "&#39;");
				responseBody = MessageFormat.format(bodyOutTemplate, safeRequest, formato, safePayload).trim();
			}

			// --- Headers + comprimento em BYTES (UTF-8) ---
			byte[] bodyBytes = responseBody.getBytes(StandardCharsets.UTF_8);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
			ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

			out.write("HTTP/1.1 200 OK\r\n");
			out.write("Date: " + formatter.format(now) + "\r\n");
			out.write("Server: Custom Server\r\n");
			out.write("Content-Type: " + (wantsJson ? "application/json" : "text/html") + "; charset=UTF-8\r\n");
			out.write("Content-Length: " + bodyBytes.length + "\r\n");
			out.write("Connection: close\r\n");
			out.write("\r\n");
			out.flush(); // garante headers no socket

			out.write(responseBody);
			out.flush();

		} catch (IOException ex) {
			System.err.println("Error handling client " + ex.getMessage());
		} finally {
			if (clientSocket != null) {
				try {
					clientSocket.close();
				} catch (IOException ex) {
					System.err.println("Error handling client " + ex.getMessage());
				}
			}
		}
	}

	/**
	 * Faz o parsing da query string a partir de um caminho de URL e retorna um mapa
	 * de parâmetros.
	 * <p>
	 * Exemplo:
	 * 
	 * <pre>
	 * parseQuery("/path?nome=Fulano&email=fulano%40mail.com")
	 * // -> {"nome":"Fulano", "email":"fulano@mail.com"}
	 * </pre>
	 *
	 * Regras de parsing:
	 * <ul>
	 * <li>Se não houver '?', ou se ele for o último caractere, retorna um mapa
	 * vazio.</li>
	 * <li>Os pares são separados por '&'.</li>
	 * <li>Cada par pode estar no formato "chave=valor" ou apenas "chave" (neste
	 * caso, valor = "").</li>
	 * <li>Chave e valor são decodificados com {@code URLDecoder} usando UTF-8
	 * (percent-encoding).</li>
	 * <li>Se a mesma chave aparecer mais de uma vez, o último valor sobrescreve os
	 * anteriores.</li>
	 * </ul>
	 *
	 * @param urlPath caminho da URL possivelmente contendo uma query string (ex.:
	 *                {@code "/?a=1&b=2"})
	 * @return mapa imutável com os parâmetros decodificados (chave -> valor); vazio
	 *         se não houver query
	 */
	private static Map<String, String> parseQuery(String urlPath) {
		Map<String, String> map = new HashMap<>();
		int q = urlPath.indexOf('?');
		if (q < 0 || q == urlPath.length() - 1)
			return map;

		String qs = urlPath.substring(q + 1);
		for (String pair : qs.split("&")) {
			if (pair.isEmpty())
				continue;
			int eq = pair.indexOf('=');
			String key, val;
			if (eq >= 0) {
				key = pair.substring(0, eq);
				val = pair.substring(eq + 1);
			} else {
				key = pair; // sem '=', valor vazio
				val = "";
			}
			key = URLDecoder.decode(key, StandardCharsets.UTF_8);
			val = URLDecoder.decode(val, StandardCharsets.UTF_8);
			map.put(key, val);
		}
		return map;
	}

}
