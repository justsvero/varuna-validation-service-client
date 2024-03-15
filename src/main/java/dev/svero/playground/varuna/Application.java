package dev.svero.playground.varuna;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.svero.playground.varuna.models.ReportConfiguration;
import dev.svero.playground.varuna.models.ValidationServiceConfiguration;
import dev.svero.playground.varuna.utils.HttpUtils;
import dev.svero.playground.varuna.utils.KeyStoreUtils;
import dev.svero.playground.varuna.utils.SSLUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.security.PrivateKey;

/**
 * Implements the entry point for the application.
 *
 * @author Sven Roeseler
 */
public class Application {
	private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldApplication.class);
	private static final String PROPERTY_CONFIGURATION = "configuration";
	private static final String ENVIRONMENT_CONFIGURATION = "VERUNA_CLIENT_CONFIG_FILE";

	private static final JWTUtils JWT_UTILS = new JWTUtils();
	private static final KeyStoreUtils KEY_STORE_UTILS = new KeyStoreUtils();
	private static final SSLUtils SSL_UTILS = new SSLUtils();

	/**
	 * Entry point for running the application.
	 *
	 * You can specify a filename as optional command-line argument. If it is present the configuration
	 * is read from it.
	 *
	 * @param args String array with command-line arguments.
	 */
	public static void main(String... args) {
		try {
			String filename = getConfigurationFilename(args);
			LOGGER.debug("Reading settings from \"{}\"", filename);

			Configuration configuration = new Configuration();
			if (!configuration.init(filename)) {
				LOGGER.error("Could not get the configuration");
				System.exit(1);
			}

			// Create the key store
			final String keyStoreFilename = configuration.getString("keystore.filename", true);
			final String keyStorePassword = configuration.getString("keystore.password", true);
			final String keyStoreType = configuration.getString("keystore.type", "PKCS12");
			KeyStore keyStore = KEY_STORE_UTILS.loadKeyStore(keyStoreFilename, keyStorePassword, keyStoreType);

			// Create the trust store
			final String trustStoreFilename = configuration.getString("truststore.filename", true);
			final String trustStorePassword = configuration.getString("truststore.password", true);
			final String trustStoreType = configuration.getString("truststore.type", "PKCS12");
			KeyStore trustStore = KEY_STORE_UTILS.loadKeyStore(trustStoreFilename, trustStorePassword, trustStoreType);

			// Create SSL context
			SSLContext sslContext = SSL_UTILS.createSSLContext(keyStore, keyStorePassword, trustStore);

			// Get the signing key for the JSON Web Tokeb
			final String privateKeyAlias = configuration.getString("keystore.private_key.alias", true);
			final String privateKeyPassword = configuration.getString("keystore.private_key.password", true);
			final PrivateKey privateKey = KEY_STORE_UTILS.getKey(keyStore, privateKeyAlias, privateKeyPassword);

			// Gets the KeyCloak settings
			final String keyCloakBaseUrl = configuration.getString("keycloak.baseUrl", true);
			final String keyCloakRealm = configuration.getString("keycloak.realm", true);

			// Generate JSON Web Token
			final String issuer = configuration.getString("keycloak.issuer", true);
			final String audience = String.format("%s/realms/%s", keyCloakBaseUrl, keyCloakRealm);
			final String subject = configuration.getString("keycloak.subject", true);
			final String jwt = JWT_UTILS.generateJwt(issuer, audience, subject, privateKey);

			// Create the HTTP client with SSL support
			HttpUtils httpUtils = new HttpUtils(sslContext);

			// Get the access token
			KeyCloakClient keyCloakClient = new KeyCloakClient(httpUtils, keyCloakBaseUrl, keyCloakRealm);
			String accessToken = keyCloakClient.getAccessToken(jwt);
			LOGGER.debug("Token: {}", accessToken);

			ValidationServiceConfiguration serviceConfiguration = new ValidationServiceConfiguration();
			serviceConfiguration.addReportConfiguration("PDF", "EN");
			serviceConfiguration.addReportConfiguration("SVR");
			serviceConfiguration.addReportConfiguration("HTML", "DE");
			serviceConfiguration.setProfile("AUTOMATIC");
			serviceConfiguration.setMaxRecursionDepth(3);

			Gson gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
			LOGGER.debug("Service configuration: {}", gsonBuilder.toJson(serviceConfiguration));
		} catch (Exception ex) {
			LOGGER.error("An error occurred", ex);
		}
	}

	/**
	 * Returns the name of the file that contains the configuration.
	 * 
	 * @param args String array with command-line arguments
	 * @return Filename
	 */
	private static String getConfigurationFilename(String... args) {
		String filename;
		if (args.length > 0 && StringUtils.isNotBlank(args[0])) {
			LOGGER.debug("Using specified filename");
			filename = args[0].trim();
		} else if (System.getProperties().containsKey(PROPERTY_CONFIGURATION)) {
			LOGGER.debug("Using information from system property");
			filename = System.getProperty(PROPERTY_CONFIGURATION);
		} else if (System.getenv(ENVIRONMENT_CONFIGURATION) != null) {
			LOGGER.debug("Using information from environment variable");
			filename = System.getenv(ENVIRONMENT_CONFIGURATION);
		} else {
			LOGGER.debug("Using default file name for configuration");
			filename = "application.properties";
		}

		return filename;
	}
}