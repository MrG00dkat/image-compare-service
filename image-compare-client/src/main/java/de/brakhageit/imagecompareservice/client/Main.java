package de.brakhageit.imagecompareservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Main {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

        Map<String, Object> body = new HashMap<>() {{
            put("file1", loadBase64FileContent(Paths.get("image-compare-rest-service/src/test/resources/example_1.pdf")));
            put("file2", loadBase64FileContent(Paths.get("image-compare-rest-service/src/test/resources/example_2.pdf")));
        }};

        String json = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(body);

        System.out.println("==> " + json);

        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8080/api/v1/compare"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        Integer statusCode = HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(Main::handleCompareResponse)
                .get();

        System.out.println("Status: " + statusCode);
    }

    private static String loadBase64FileContent(Path filePath) throws IOException {
        return new String(Base64.getEncoder().encode(Files.readAllBytes(filePath)));

    }

    private static int handleCompareResponse(HttpResponse<String> response) {

        try {
            Map result = OBJECT_MAPPER.readValue(response.body(), Map.class);
            String correlationId = (String) result.get("correlationId");
            Double percentageDifference = (Double) result.get("percentageDifference");
            String compareResultFile = (String) result.get("compareResultFile");

            System.out.println("Correlation-ID: " + correlationId);
            System.out.println("Difference: " + percentageDifference + "%");

            Path compareResultFilePath = Paths.get("/tmp/", "compareResultFile_" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".pdf");
            Files.write(compareResultFilePath, Base64.getDecoder().decode(compareResultFile));
            System.out.println("Compare Result File: " + compareResultFilePath.toString());

            if (Desktop.isDesktopSupported() && Files.exists(compareResultFilePath)) {
                Desktop.getDesktop().open(compareResultFilePath.toFile());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.statusCode();
    }

}
