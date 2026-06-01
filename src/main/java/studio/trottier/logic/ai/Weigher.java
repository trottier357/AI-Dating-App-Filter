package studio.trottier.logic.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Weigher {
    private final String REFERENCE;
    private final String MODEL;
    private final HttpClient HTTP = HttpClient.newHttpClient();

    public Weigher(String model, String reference){
        this.MODEL = model;
        this.REFERENCE = reference;
    }

    public float score(String description){
        String body = """
            {
              "model": "%s",
              "stream": false,
              "options": { "temperature": 0 },
              "prompt": "%s"
            }
            """.formatted(MODEL, escape(buildPrompt(description)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try{
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            String modelText = extractField(response.body(), "response");
            return parseScore(modelText);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helpers below.

    private String buildPrompt(String person){
        return "You are scoring how well a person matches a target vibe.\n\n"
                + "TARGET VIBE:\n" + REFERENCE + "\n\n"
                + "PERSON TO SCORE:\n" + person + "\n\n"
                + "Rate from 0 to 100 how strongly the person matches the target "
                + "vibe. 0 means the complete opposite of the vibe. 50 means "
                + "unrelated or mixed. 100 means a near-perfect match. A person "
                + "who is the OPPOSITE of the target must score below 20.\n"
                + "Respond with ONLY the integer. No words, no explanation.";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private float parseScore(String text) {
        var m = java.util.regex.Pattern.compile("\\d+").matcher(text);
        if(m.find()){
            return Float.parseFloat(m.group()) / 100.0f;
        }
        throw new IllegalStateException("No number in response: " + text);
    }

    private static String extractField(String json, String field) {
        String key = "\"" + field + "\":\"";
        int start = json.indexOf(key);
        if (start < 0) throw new IllegalStateException("Bad response: " + json);
        start += key.length();
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\') { sb.append(json.charAt(++i)); continue; }
            if (c == '"') break;
            sb.append(c);
        }
        return sb.toString();
    }
}
