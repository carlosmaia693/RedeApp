import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServidorRedeApp {
	
	static final File DATA_FILE =
        new File("C:\\ProgramData\\AgentRede\\pcs.json");

    static class PCInfo {
        String host;
        String ip;
        String mac;
        String usuario;
        long lastSeen;

        PCInfo(String host, String ip, String mac, String usuario) {
            this.host = host;
            this.ip = ip;
            this.mac = mac;
            this.usuario = usuario;
            this.lastSeen = System.currentTimeMillis();
        }
    }

    static Map<String, PCInfo> pcs = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", 8080),
                0
        );

        server.createContext("/pc", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                handlePostPC(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/pcs", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                handleGetPCs(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.setExecutor(null);
		loadData();
        server.start();

        Logger.info("server.log", "Servidor iniciado porta 8080");
		
    }
	
	static void loadData() {
		try {
			if (!DATA_FILE.exists()) return;

			String json = new String(java.nio.file.Files.readAllBytes(DATA_FILE.toPath()));

			json = json.substring(1, json.length() - 1); // remove [ ]

			if (json.trim().isEmpty()) return;

			String[] items = json.split("\\},\\{");

			for (String item : items) {

				item = item.replace("{", "").replace("}", "");

				String host = extrair(item, "host");
				String ip = extrair(item, "ip");
				String mac = extrair(item, "mac");
				String usuario = extrair(item, "usuario");

				PCInfo pc = pcs.get(host);

				if (pc == null) {
					pc = new PCInfo(host, ip, mac, usuario);
					pcs.put(host, pc);
				} else {
					pc.ip = ip;
					pc.mac = mac;
					pc.usuario = usuario;
				}

				pc.lastSeen = System.currentTimeMillis();
			}

		} catch (Exception e) {
			Logger.error("error.log", "Falha ao carregar dados", e);
		}
	}
	
	static void saveData() {
		try {
			DATA_FILE.getParentFile().mkdirs();
			StringBuilder json = new StringBuilder("[");

			boolean first = true;

			for (PCInfo pc : pcs.values()) {

				if (!first) json.append(",");

				boolean online =
						(System.currentTimeMillis() - pc.lastSeen) < 15000;

				json.append("{")
					.append("\"host\":\"").append(pc.host).append("\",")
					.append("\"ip\":\"").append(pc.ip).append("\",")
					.append("\"mac\":\"").append(pc.mac).append("\",")
					.append("\"usuario\":\"").append(pc.usuario).append("\",")
					.append("\"lastSeen\":").append(pc.lastSeen).append(",")
					.append("\"online\":").append(online)
					.append("}");

				first = false;
			}

			json.append("]");

			java.nio.file.Files.write(
					DATA_FILE.toPath(),
					json.toString().getBytes()
			);

		} catch (Exception e) {
			Logger.error("error.log", "Falha ao carregar dados", e);
		}
	}

    static void handlePostPC(HttpExchange exchange) throws IOException {

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        String host = extrair(body, "host");
        String ip = extrair(body, "ip");
        String mac = extrair(body, "mac");
        String usuario = extrair(body, "usuario");

        if (!host.isEmpty()) {
            pcs.put(host, new PCInfo(host, ip, mac, usuario));
			saveData();

            Logger.info("network.log",
				"Heartbeat recebido: " + host + " | " + ip + " | " + usuario
			);
        }

        byte[] response = "OK".getBytes();

        exchange.sendResponseHeaders(200, response.length);

        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }

    static void handleGetPCs(HttpExchange exchange) throws IOException {

        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        for (PCInfo pc : pcs.values()) {

            if (!first) json.append(",");

            json.append("{")
                    .append("\"host\":\"").append(pc.host).append("\",")
                    .append("\"ip\":\"").append(pc.ip).append("\",")
                    .append("\"mac\":\"").append(pc.mac).append("\",")
                    .append("\"usuario\":\"").append(pc.usuario).append("\",")
                    .append("\"lastSeen\":").append(pc.lastSeen)
                    .append("}");

            first = false;
        }

        json.append("]");

        byte[] response =
                json.toString().getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add(
                "Content-Type",
                "application/json"
        );

        exchange.sendResponseHeaders(200, response.length);

        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }

    static String extrair(String json, String campo) {

        String busca = "\"" + campo + "\":\"";

        int inicio = json.indexOf(busca);

        if (inicio == -1) return "";

        inicio += busca.length();

        int fim = json.indexOf("\"", inicio);

        return json.substring(inicio, fim);
    }
}