import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class AgentRede {

    static final String SERVER_URL = "http://10.224.10.45:8080/pc";

    public static void main(String[] args) {

        while (true) {
            try {
                enviarHeartbeat();
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void enviarHeartbeat() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();

            String host = localHost.getHostName();
            String ip = localHost.getHostAddress();
            String mac = getMacAddress(localHost);
            String usuario = System.getProperty("user.name");

            String json =
                    "{"
                    + "\"host\":\"" + host + "\","
                    + "\"ip\":\"" + ip + "\","
                    + "\"mac\":\"" + mac + "\","
                    + "\"usuario\":\"" + usuario + "\""
                    + "}";

            URL url = new URL(SERVER_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = con.getResponseCode();

            System.out.println("Heartbeat enviado: " + responseCode);

            con.disconnect();

        } catch (Exception e) {
            System.out.println("Falha ao enviar heartbeat");
        }
    }

    static String getMacAddress(InetAddress ip) {
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            byte[] mac = network.getHardwareAddress();

            if (mac == null) return "N/A";

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s",
                        mac[i],
                        (i < mac.length - 1) ? "-" : ""));
            }

            return sb.toString();

        } catch (Exception e) {
            return "N/A";
        }
    }
}