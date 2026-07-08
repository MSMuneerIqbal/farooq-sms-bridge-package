package pk.farooq.smsbridge;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NetworkClient {
    public static String getJson(String urlStr, String deviceId, String deviceSecret) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(12000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (deviceId != null && !deviceId.isEmpty()) conn.setRequestProperty("X-Device-Id", deviceId);
        if (deviceSecret != null && !deviceSecret.isEmpty()) conn.setRequestProperty("X-Device-Secret", deviceSecret);
        int code = conn.getResponseCode();
        String body = readBody(conn, code);
        if (code < 200 || code >= 300) throw new RuntimeException("HTTP " + code + ": " + body);
        return body;
    }

    public static String postJson(String urlStr, JSONObject body, String deviceId, String deviceSecret) throws Exception {
        byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(12000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Accept", "application/json");
        if (deviceId != null && !deviceId.isEmpty()) conn.setRequestProperty("X-Device-Id", deviceId);
        if (deviceSecret != null && !deviceSecret.isEmpty()) conn.setRequestProperty("X-Device-Secret", deviceSecret);
        try (OutputStream os = conn.getOutputStream()) { os.write(payload); }
        int code = conn.getResponseCode();
        String response = readBody(conn, code);
        if (code < 200 || code >= 300) throw new RuntimeException("HTTP " + code + ": " + response);
        return response;
    }

    private static String readBody(HttpURLConnection conn, int code) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream(),
                StandardCharsets.UTF_8
        ));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        return sb.toString();
    }
}
