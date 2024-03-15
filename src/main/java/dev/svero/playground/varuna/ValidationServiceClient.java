package dev.svero.playground.helloworld;

import dev.svero.playground.helloworld.models.ValidationServiceConfiguration;
import dev.svero.playground.helloworld.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements methods for accessing the DATA Varuna Validation Services.
 *
 * @author Sven Roeseler
 */
public class ValidationServiceClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationServiceClient.class);

    private final HttpUtils httpClient;
    private final String baseUrl;
    private final String endPoint;

    /**
     * Creates a new instance.
     *
     * @param httpUtils HTTP utils instance.
     */
    public ValidationServiceClient(HttpUtils httpUtils, final String baseUrl, final String endPoint) {
        if (httpUtils == null) {
            throw new IllegalArgumentException("httpUtils may not be null");
        }

        if (StringUtils.isAnyBlank(baseUrl, endPoint)) {
            throw new IllegalArgumentException("Neither baseUrl nor endPoint may be blank");
        }

        this.httpClient = httpUtils;
        this.baseUrl = baseUrl;
        this.endPoint = endPoint;
    }

    /**
     * Validates the specified signature and optional the signed document.
     *
     * @param authorizationToken Authorization token (from KeyCloak)
     * @param configuration Validation configuration
     * @param signature File with signature to check
     * @param document Optional file with the signed document
     */
    public void validate(final String authorizationToken, final ValidationServiceConfiguration configuration,
                         File signature, File document) {
        Map<Object, Object> data = new HashMap<>();

        data.put("signature", signature);

        if (document != null) {
            data.put("content", document);
        }

        if (configuration != null) {
            data.put("jsonConfig", new JSONObject(configuration));
        }

        Map<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put("Authorization", "Bearer " + authorizationToken);

        final String url = this.baseUrl + this.endPoint;

        try {
            String response = httpClient.postMultipartRequest(url, data, additionalHeaders);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Could not successfully perform the request to the validation service", e);
        }
    }
}
