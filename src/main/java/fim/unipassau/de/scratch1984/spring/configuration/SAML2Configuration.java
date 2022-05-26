package fim.unipassau.de.scratch1984.spring.configuration;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Collection;

/**
 * Configuration for SAML2 authentication. This class has been adapted from the Artemis project.
 */
@Configuration
@Order(1)
@Profile("saml2")
public class SAML2Configuration extends WebSecurityConfigurerAdapter {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private final Logger logger = LoggerFactory.getLogger(SAML2Configuration.class);

    /**
     * The SAML2 properties for communicating with the IdP and extracting the necessary user information from the SAML2
     * authentication.
     */
    private final SAML2Properties properties;

    /**
     * Constructs the SAML2 configuration with the given dependencies.
     *
     * @param properties The {@link SAML2Properties} to use.
     */
    public SAML2Configuration(final SAML2Properties properties) {
        this.properties = properties;
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Registers the information necessary to communicate with the IdP in memory from the {@link SAML2Properties}.
     *
     * @return An {@link InMemoryRelyingPartyRegistrationRepository} containing the information.
     */
    @Bean
    RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        final RelyingPartyRegistration relyingPartyRegistration = RelyingPartyRegistrations
                .fromMetadataLocation(properties.getMetadata())
                .registrationId(properties.getIdpName())
                .entityId(properties.getEntityId())
                .decryptionX509Credentials(credentialsSink -> this.addDecryptionInformation(credentialsSink,
                        properties))
                .signingX509Credentials(credentialsSink -> this.addSigningInformation(credentialsSink, properties))
                .build();
        return new InMemoryRelyingPartyRegistrationRepository(relyingPartyRegistration);
    }

    /**
     * Adds the decryption information to the in-memory repository by reading the key and certificate files, if present.
     *
     * @param credentialsSink The {@link Saml2X509Credential} to add the decryption information.
     * @param config The SAML2 properties for retrieving the file paths.
     */
    private void addDecryptionInformation(final Collection<Saml2X509Credential> credentialsSink,
                                          final SAML2Properties config) {
        if (this.filesNotExistent(config)) {
            return;
        }

        try {
            Saml2X509Credential credentials = Saml2X509Credential.decryption(readPrivateKey(config.getKeyFile()),
                    readPublicCert(config.getCertFile()));
            credentialsSink.add(credentials);
        } catch (IOException | CertificateException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Adds the signing information to the in-memory repository by reading the key and certificate files, if present.
     *
     * @param credentialsSink The {@link Saml2X509Credential} to add the signing information.
     * @param config The SAML2 properties for retrieving the file paths.
     */
    private void addSigningInformation(final Collection<Saml2X509Credential> credentialsSink,
                                       final SAML2Properties config) {
        if (this.filesNotExistent(config)) {
            return;
        }

        try {
            Saml2X509Credential credentials = Saml2X509Credential.signing(readPrivateKey(config.getKeyFile()),
                    readPublicCert(config.getCertFile()));
            credentialsSink.add(credentials);
        } catch (IOException | CertificateException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Checks if the key and certificate files exist based on the paths provided in the given properties.
     *
     * @param config The {@link SAML2Properties}.
     * @return {@code true} if the files do not exist or {@code false} otherwise.
     */
    private boolean filesNotExistent(final SAML2Properties config) {
        if (config.getCertFile() == null || config.getKeyFile() == null || config.getCertFile().isBlank()
                || config.getKeyFile().isBlank()) {
            logger.debug("No Config for SAML2");
            return true;
        }

        File keyFile = new File(config.getKeyFile());
        File certFile = new File(config.getCertFile());

        if (!keyFile.exists() || !certFile.exists()) {
            logger.error("Keyfile or Certfile for SAML[{}] does not exist.", config.getIdpName());
            return true;
        }

        return false;
    }

    /**
     * Reads the given certificate file to generate an {@link X509Certificate} to be used for communication with the
     * IdP.
     *
     * @param file The certificate file.
     * @return The {@link X509Certificate}.
     * @throws IOException If an error occurred while reading the file.
     * @throws CertificateException If an error occurred while constructing the certificate.
     */
    private static X509Certificate readPublicCert(final String file) throws IOException, CertificateException {
        try (InputStream inStream = new FileInputStream(file)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(inStream);
        }
    }

    /**
     * Reads the given key file to generate a {@link RSAPrivateKey} to be used for communicating with the IdP.
     *
     * @param file The key file.
     * @return The {@link RSAPrivateKey}.
     * @throws IOException if an error occurred while reading the file.
     */
    private RSAPrivateKey readPrivateKey(final String file) throws IOException {
        try (FileReader keyReader = new FileReader(file, StandardCharsets.UTF_8)) {
            PEMParser pemParser = new PEMParser(keyReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(pemParser.readObject());
            return (RSAPrivateKey) converter.getPrivateKey(privateKeyInfo);
        }
    }

    /**
     * Configures the URL patterns that are used for SAML2 authentication.
     *
     * @param http The {@link HttpSecurity}.
     * @throws Exception if something went wrong during access.
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                // This filter chain is only applied if the URL matches
                // Else the request is filtered by {@link SecurityConfiguration}.
                .antMatchers("/saml2/**", "/login/saml2/**")
                .and()
                .csrf()
                // Needed for SAML to work properly
                .disable()
                .authorizeRequests()
                // The request to the api is permitted and checked directly
                // This allows returning a 401 if the user is not logged in via SAML2
                // to notify the client that a login is needed.
                .antMatchers("/saml2").permitAll()
                // Every other request must be authenticated. Any request triggers a SAML2
                // authentication flow
                .anyRequest().authenticated()
                .and()
                // Processes the RelyingPartyRegistrationRepository Bean and installs the filters for SAML2
                .saml2Login()
                .and().formLogin().loginPage("/login/saml2")
                // Redirect back to the root
                .defaultSuccessUrl("/index", true);
    }

}
