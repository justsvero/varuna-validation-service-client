package dev.svero.playground.varuna;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.svero.playground.varuna.models.ValidationServiceConfiguration;
import dev.svero.playground.varuna.utils.HttpUtils;
import dev.svero.playground.varuna.utils.KeyStoreUtils;
import dev.svero.playground.varuna.utils.SSLUtils;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
	private static final String PROPERTY_CONFIGURATION = "configuration";
	private static final String ENVIRONMENT_CONFIGURATION = "VERUNA_CLIENT_CONFIG_FILE";

	private static final JWTUtils JWT_UTILS = new JWTUtils();
	private static final KeyStoreUtils KEY_STORE_UTILS = new KeyStoreUtils();
	private static final SSLUtils SSL_UTILS = new SSLUtils();

	/**
	 * Entry point for running the application.
	 *
	 * @param args String array with command-line arguments.
	 */
	public static void main(String[] args) {
		try {
			final CommandLine commandLine = parseCommandLine(args);

			String filename = getConfigurationFilename(commandLine);
			LOGGER.debug("Reading settings from \"{}\"", filename);

			Configuration configuration = new Configuration();
			if (!configuration.init(filename)) {
				LOGGER.error("Could not get the configuration");
				System.exit(1);
			}

			final String keyStoreFilename = configuration.getString("keystore.filename", true);
			final String keyStorePassword = configuration.getString("keystore.password", true);
			final String keyStoreType = configuration.getString("keystore.type", "PKCS12");
			KeyStore keyStore = KEY_STORE_UTILS.loadKeyStore(keyStoreFilename, keyStorePassword, keyStoreType);

			final String trustStoreFilename = configuration.getString("truststore.filename", true);
			final String trustStorePassword = configuration.getString("truststore.password", true);
			final String trustStoreType = configuration.getString("truststore.type", "PKCS12");
			KeyStore trustStore = KEY_STORE_UTILS.loadKeyStore(trustStoreFilename, trustStorePassword, trustStoreType);

			SSLContext sslContext = SSL_UTILS.createSSLContext(keyStore, keyStorePassword, trustStore);

			final String privateKeyAlias = configuration.getString("keystore.private_key.alias", true);
			final String privateKeyPassword = configuration.getString("keystore.private_key.password", true);
			final PrivateKey privateKey = KEY_STORE_UTILS.getKey(keyStore, privateKeyAlias, privateKeyPassword);
			if (privateKey == null) {
				throw new IllegalStateException("No private key found in specified keystore");
			}

			String keyCloakBaseUrl = configuration.getString("keycloak.baseUrl", true);
			if (keyCloakBaseUrl.endsWith("/")) {
				keyCloakBaseUrl = keyCloakBaseUrl.substring(0, keyCloakBaseUrl.lastIndexOf('/'));
			}
			final String keyCloakRealm = configuration.getString("keycloak.realm", true);

			final String issuer = configuration.getString("keycloak.issuer", true);
			final String audience = String.format("%s/realms/%s", keyCloakBaseUrl, keyCloakRealm);
			final String subject = configuration.getString("keycloak.subject", true);
			final String jwt = JWT_UTILS.generateJwt(issuer, audience, subject, privateKey);

			HttpUtils httpUtils = new HttpUtils(sslContext);

			KeyCloakClient keyCloakClient = new KeyCloakClient(httpUtils, keyCloakBaseUrl, keyCloakRealm);
			String accessToken = keyCloakClient.getAccessToken(jwt);
			LOGGER.debug("Token: {}", accessToken);

			final String validationServiceBaseUrl = configuration.getString("validationService.baseUrl", true);
			final String validationServiceEndPoint = configuration.getString("validationService.endPoint", true);

			ValidationServiceClient validationServiceClient = new ValidationServiceClient(httpUtils,
					validationServiceBaseUrl, validationServiceEndPoint);

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
	 * @param commandLine CommandLine instance
	 * @return Path and name of the properties file
	 */
	private static String getConfigurationFilename(CommandLine commandLine) {
		String filename;

		if (commandLine.hasOption('c')) {
			LOGGER.debug("Using specified filename");
			filename = commandLine.getOptionValue('c');
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

	/**
	 * Builds the available command-line options.
	 *
	 * @return Options instance
	 */
	private static Options buildCliOptions() {
		Options options = new Options();

		options.addOption("c", "configuration", true, "Name and path with the application properties");
		options.addOption("f", "file", true, "Path and name of the signed document");
		options.addOption("s", "signature", true, "Path and name of the signature file");

		return options;
	}

	/**
	 * Parses the command-line and returns an object with all specified parameters.
	 *
	 * @param args String array with command-line arguments
	 * @return Object with parsed parameters
	 */
	private static CommandLine parseCommandLine(String[] args) {
		final Options options = buildCliOptions();
		final CommandLineParser parser = new DefaultParser();

		CommandLine commandLine;

		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException ex) {
			LOGGER.error("Could not parse the command-line arguments", ex);
			throw new RuntimeException("An error occurred while parsing the command line", ex);
		}

		return commandLine;
	}
}