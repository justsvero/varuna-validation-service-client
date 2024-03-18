package dev.svero.playground.varuna.utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Implements methods for performing HTTP requests.
 *
 * @author Sven Roeseler
 */
public class HttpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    private final SSLContext sslContext;

    /**
     * Creates a new instance.
     */
    public HttpUtils() {
        this.sslContext = null;
    }

    /**
     * Creates a new instance using the specified SSL context.
     *
     * @param sslContext SSL context.
     */
    public HttpUtils(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    /**
     * Performs a GET request.
     *
     * @param url Target URL for the request.
     * @return The server response as string.
     * @throws IOException          If an I/O error occurred.
     * @throws InterruptedException If the request was interrupted before the response was received.
     */
    public String getRequest(final String url) throws IOException, InterruptedException {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url may not be blank");
        }

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        return processRequest(request);
    }

    /**
     * Performs a POST request to the specified URL using the specified data as request body.
     *
     * @param url         Target url for request
     * @param requestData Data for request body
     * @return Server response as string
     * @throws IOException          If an I/O error occurred
     * @throws InterruptedException If the request was interrupted before the response was received
     * @throws URISyntaxException   If the specified URL is invalid
     */
    public String postRequest(final String url, final String requestData)
            throws IOException, InterruptedException, URISyntaxException {
        boolean hasBody = StringUtils.isNotBlank(requestData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .POST(hasBody ? HttpRequest.BodyPublishers.ofString(requestData)
                        : HttpRequest.BodyPublishers.noBody())
                .build();

        return processRequest(request);
    }

    /**
     * Performs a multipart POST request to the specified URL and returns the answer.
     *
     * @param url         Target URL for the request
     * @param requestData Multipart message as request data
     * @return Response from the server
     * @throws IOException If something went wrong
     */
    public String postMultipartRequest(final String url, Map<Object, Object> requestData)
            throws IOException, InterruptedException {
        return postMultipartRequest(url, requestData, null);
    }

    /**
     * Performs a multipart POST request to the specified URL and returns the answer.
     *
     * @param url         Target URL for the request
     * @param requestData Multipart message as request data
     * @param headers     Optional map with addional request headers
     * @return Response from the server
     * @throws IOException If something went wrong
     */
    public String postMultipartRequest(final String url, Map<Object, Object> requestData, Map<String, String> headers)
            throws IOException, InterruptedException {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url may not be blank");
        }

        if (requestData == null || requestData.isEmpty()) {
            throw new IllegalArgumentException("data may not be null or empty");
        }

        String boundary = new BigInteger(256, new Random()).toString();
        LOGGER.debug("Multipart boundary: {}", boundary);

        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.uri(URI.create(url));
        builder.header("Content-Type", "multipart/form-data;boundary=" + boundary);

        if (headers != null && !headers.isEmpty()) {
            for (String key : headers.keySet()) {
                String value = headers.get(key);
                builder.header(key, value);
            }
        }

        builder.POST(ofMimeMultipartData(requestData, boundary));

        HttpRequest request = builder.build();

        return processRequest(request);
    }

    /**
     * Creates a HTTP client.
     *
     * @return HTTP client.
     */
    private HttpClient createHttpClient() {
        HttpClient client;

        if (sslContext == null) {
            client = HttpClient.newBuilder().build();
        } else {
            client = HttpClient.newBuilder().sslContext(sslContext).build();
        }

        return client;
    }

    /**
     * Performs the specified request and returns the response as string if the status code was 200.
     *
     * @param request Request to perform
     * @return Response body as string
     * @throws IOException          If an I/O error happened
     * @throws InterruptedException If the request was interrupted before the response was received
     */
    public String processRequest(HttpRequest request) throws IOException, InterruptedException {
        if (request == null) {
            throw new IllegalArgumentException("request may not be null");
        }

        HttpClient client = createHttpClient();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String result;

        if (response.statusCode() == 200) {
            result = response.body();
        } else {
            LOGGER.error("HTTP request returned unexpected status: {}", response.statusCode());
            throw new RuntimeException("Unexpected status code received while processing POST request");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Raw result: {}", result);
        }

        return result;
    }

    /**
     * <a href="https://stackoverflow.com/questions/56481475/how-to-define-multiple-parameters-for-a-post-request-using-java-11-http-client">Source</a>
     *
     * @param data     Map with data
     * @param boundary Boundary for message parts
     * @return BodyPublisher instance
     * @throws IOException if something went wrong
     */
    public HttpRequest.BodyPublisher ofMimeMultipartData(Map<Object, Object> data,
                                                         String boundary) throws IOException {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("data may not be null or empty");
        }

        if (StringUtils.isBlank(boundary)) {
            throw new IllegalArgumentException("boundary may not be null or empty");
        }

        // Result request body
        List<byte[]> byteArrays = new ArrayList<>();

        // Separator with boundary
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8);

        // Iterating over data parts
        for (Map.Entry<Object, Object> entry : data.entrySet()) {

            // Opening boundary
            byteArrays.add(separator);

            // If value is type of Path (file) append content type with file name and file binaries, otherwise simply append key=value
            if (entry.getValue() instanceof Path) {
                var path = (Path) entry.getValue();
                String mimeType = Files.probeContentType(path);
                byteArrays.add(("\"" + entry.getKey() + "\"; filename=\"" + path.getFileName()
                        + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                byteArrays.add(Files.readAllBytes(path));
                byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
            } else if (entry.getValue() instanceof JSONObject) {
                String mimeType = "application/json";
                byteArrays.add(("\"" + entry.getKey() + "\"" + "\r\nContent-Type:" + mimeType + "\r\n\r\n"
                        + entry.getValue() + "\r\n")
                        .getBytes(StandardCharsets.UTF_8));
            } else {
                byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n")
                        .getBytes(StandardCharsets.UTF_8));
            }
        }

        // Closing boundary
        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));

        // Serializing as byte array
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }
}
