package osaki.silverfish;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJson;

public class Util {
    public static void SendWebhook(String message, String webhook_url, Logger logger) {
        try {
            URL url = URI.create(webhook_url).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonPayload = "{\"content\":\"" + escapeJson(message) + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }

            connection.getInputStream().close();
            connection.disconnect();

        } catch (Exception e) {
            logger.warning("Failed to send webhook: " + e.getMessage());
        }
    }

    public static boolean ValidateWehbook(String url) {
        return url != null && !url.isBlank() && !url.equals("PUT_WEBHOOK_HERE");
    }
}
