package mdex.service;

import mdex.model.KeySwitch;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * This class implements the KeySwitchService using the HttpClient library to send Http Request to the Firebase Realtime Database REST API.
 */

@Named("firebaseHttpClientKeySwitchService")
@ApplicationScoped
public class FirebaseHttpClientKeySwitchService implements KeySwitchService {
    /**
     * The base URL to the Firebase Realtime Database that is defined in `src/main/resources/META-INF/microprofile-config.properties` file.
     */
    @Inject
    @ConfigProperty(name = "firebase.rtdb.KeySwitch.base.url")
    private String firebaseRtdbBaseUrl;

    /**
     * The URL to the Firebase Realtime Database to access all data.
     */
    private String _jsonAllDataPath;

    /**
     * HttpClient is native Java library for sending Http Request to a web server
     */
    private HttpClient httpClient;

    /**
     * Jsonb is used for converting Java objects to a JSON string or visa-versa
     */
    private Jsonb jsonb;

    @PostConstruct
    private void init() {
        httpClient = HttpClient.newHttpClient();
        jsonb = JsonbBuilder.create();
    }

    /**
     * Pushing currentKeySwitch data to Firebase Realtime Database using the REST API
     *
     * @link <a href="https://firebase.google.com/docs/reference/rest/database">Firebase Realtime Database REST API</a>
     */
    @Override
    public KeySwitch createKeySwitch(KeySwitch keySwitch) {
        // Build the url path to object to create
        _jsonAllDataPath = String.format("%s/%s.json", firebaseRtdbBaseUrl, KeySwitch.class.getSimpleName());
        // Convert the Java object to a JSON string using JSONB
        String requestBodyJson = jsonb.toJson(keySwitch);

        // Create a Http Request for sending a Http POST request to push new data
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(_jsonAllDataPath))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson, StandardCharsets.UTF_8))
                .build();
        try {
            // Send the Http Request
            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            // Check if the Http Request response is successful
            if (httpResponse.statusCode() == 200) {
                // Get the body of the Http Response
                var responseBodyJson = httpResponse.body();
                // Convert the JSON String to a JsonObject
                JsonObject responseJsonObject = jsonb.fromJson(responseBodyJson, JsonObject.class);
                // Set the unique key name for this object
                keySwitch.setId(responseJsonObject.getString("name"));

            } else {
                String errorMessage = String.format("Create was not successful with status code: %s", httpResponse.statusCode());
                throw new RuntimeException(errorMessage);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return keySwitch;
    }

    @Override
    public Optional<KeySwitch> getKeySwitchById(String id) {
        // Build the url path to object to update
        String _jsonSingleDataPath = String.format("%s/%s/%s.json",
                firebaseRtdbBaseUrl, KeySwitch.class.getSimpleName(), id);
        try {
            // Create an GET Http Request to fetch all data
            var httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(_jsonSingleDataPath))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            // Send the GET Http Request
            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            // Check if the Http Request was successful
            if (httpResponse.statusCode() == 200) {
                // Get the body of the Http Response
                var responseBodyJson = httpResponse.body();
                if (!responseBodyJson.equals("null")) {
                    // Convert the responseBodyJson to an LinkedHashMap<String, KeySwitch>
                    KeySwitch responseData = jsonb.fromJson(responseBodyJson, KeySwitch.class);
                    responseData.setId(id);
                    return Optional.of(responseData);
                } else {
                    throw new RuntimeException(String.format("KeySwitch with id of %s not found", id));
                }

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<KeySwitch> getAllKeySwitchs() {
        // Build the url path to object to get all data
        _jsonAllDataPath = String.format("%s/%s.json", firebaseRtdbBaseUrl, KeySwitch.class.getSimpleName());
        // Create an GET Http Request to fetch all data
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(_jsonAllDataPath))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        try {
            // Send the GET Http Request
            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            // Check if the Http Request was successful
            if (httpResponse.statusCode() == 200) {
                // Get the body of the Http Response
                var responseBodyJson = httpResponse.body();
                // Convert the responseBodyJson to an LinkedHashMap<String, KeySwitch>
                LinkedHashMap<String, KeySwitch> responseData = jsonb.fromJson(responseBodyJson, new LinkedHashMap<String, KeySwitch>() {
                }.getClass().getGenericSuperclass());
                if (responseData != null) {
                    // Convert the LinkedHashMap<String, KeySwitch> to List<KeySwitch>
                    return responseData.entrySet()
                            .stream()
                            .map(item -> {
                                var currentKeySwitch = new KeySwitch();
                                currentKeySwitch.setId(item.getKey());

                                currentKeySwitch.setSwitchType(item.getValue().getSwitchType());
                                currentKeySwitch.setSwitchName(item.getValue().getSwitchName());
                                currentKeySwitch.setSwitchTravel(item.getValue().getSwitchTravel());
                                currentKeySwitch.setCompany(item.getValue().getCompany());
                                currentKeySwitch.setActuationForce(item.getValue().getActuationForce());

                                return currentKeySwitch;
                            })
                            .toList();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return List.of();
    }

    /**
     * Writing currentKeySwitch data to Firebase Realtime Database using the REST API
     *
     * @link <a href="https://firebase.google.com/docs/reference/rest/database">Firebase Realtime Database REST API</a>
     */
    @Override
    public KeySwitch updateKeySwitch(KeySwitch keySwitch) {
        // Build the url path to object to update
        String _jsonSingleDataPath = String.format("%s/%s/%s.json",
                firebaseRtdbBaseUrl, KeySwitch.class.getSimpleName(), keySwitch.getId());

        // Convert the Java object to a JSON string using JSONB
        String requestBodyJson = jsonb.toJson(keySwitch);

        // Create and Http Request to send an HTTP PUT request to write over existing data
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(_jsonSingleDataPath))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBodyJson, StandardCharsets.UTF_8))
                .build();
        try {
            // Send the Http Request
            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            // Check if the Http Response was successful
            if (httpResponse.statusCode() != 200) {
                String errorMessage = String.format("Update was not successful with status code: %s", httpResponse.statusCode());
                throw new RuntimeException(errorMessage);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return keySwitch;
    }

    /**
     * Remove data from Firebase Realtime Database using the REST API
     *
     * @link <a href="https://firebase.google.com/docs/reference/rest/database">Firebase Realtime Database REST API</a>
     */
    @Override
    public void deleteKeySwitchById(String id) {
        // Build the URL path of the Json object to delete
        String _jsonSingleDataPath = String.format("%s/%s/%s.json",
                firebaseRtdbBaseUrl, KeySwitch.class.getSimpleName(), id);
        // Create an DELETE Http Request
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(_jsonSingleDataPath))
                .DELETE()
                .build();
        try {
            // Send the DELETE Http Request
            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            // Check if the Http Response was successful
            if (httpResponse.statusCode() != 200) {
                String errorMessage = String.format("Delete was not successful with status code: %s", httpResponse.statusCode());
                throw new RuntimeException(errorMessage);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}