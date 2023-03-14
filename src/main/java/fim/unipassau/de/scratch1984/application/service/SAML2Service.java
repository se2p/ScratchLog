package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.spring.configuration.SAML2Properties;
import fim.unipassau.de.scratch1984.util.enums.Language;
import fim.unipassau.de.scratch1984.util.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A service providing methods related to user authentication via SAML2. This class has been adapted from the Artemis
 * project.
 */
@Service
@Profile("saml2")
public class SAML2Service {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SAML2Service.class);

    /**
     * The user repository to use for user queries.
     */
    private final UserRepository userRepository;

    /**
     * The SAML2 properties for communicating with the IdP and extracting the necessary user information from the SAML2
     * authentication.
     */
    private final SAML2Properties properties;

    /**
     * Pattern used to extract only parts of attribute values.
     */
    private final Map<String, Pattern> extractionPattern;

    /**
     * Constructs a SAML2 service with the given dependencies.
     *
     * @param userRepository The {@link UserRepository} to use.
     * @param properties The {@link SAML2Properties} to use.
     */
    @Autowired
    public SAML2Service(final UserRepository userRepository, final SAML2Properties properties) {
        this.userRepository = userRepository;
        this.properties = properties;
        this.extractionPattern = Map.ofEntries(Map.entry(properties.getKey(),
                Pattern.compile(properties.getValuePattern())));
    }

    /**
     * Authenticates the user from the given {@link Saml2AuthenticatedPrincipal} by extracting the username and
     * retrieving the user from the database. If no corresponding user exists, a new on is created by extracting the
     * necessary information from the principal. Finally, a {@link UsernamePasswordAuthenticationToken} containing the
     * user information is returned.
     *
     * @param principal The {@link Saml2AuthenticatedPrincipal}.
     * @return The authentication token used to finish the authentication process.
     * @throws IllegalStateException if a user logging in for the first time could not be persisted.
     */
    public Authentication handleAuthentication(final Saml2AuthenticatedPrincipal principal) {
        String username = substituteAttributes(properties.getUsernamePattern(), principal);
        Optional<User> optionalUser = userRepository.findUserByUsername(username);
        User user;

        if (optionalUser.isEmpty()) {
            user = createUserFromAuth(username, principal);

            if (user.getId() == null) {
                LOGGER.error("Could not save new user " + username + " authenticated with SAML2!");
                throw new IllegalStateException("Could not save new user " + username + " authenticated with SAML2!");
            }
        } else {
            user = optionalUser.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }

        return new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
    }

    /**
     * Creates a new user in the database from the information contained in the given
     * {@link Saml2AuthenticatedPrincipal}.
     *
     * @param username The new user's username.
     * @param principal The principal containing the rest of the necessary information.
     * @return The newly created user.
     */
    private User createUserFromAuth(final String username, final Saml2AuthenticatedPrincipal principal) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(substituteAttributes(properties.getEmailPattern(), principal));
        user.setActive(true);
        user.setRole(Role.PARTICIPANT);
        user.setLanguage(Language.ENGLISH);
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Extracts the information for the given attribute string from the {@link Saml2AuthenticatedPrincipal}, if present.
     *
     * @param input The desired attribute.
     * @param principal The authenticated principal.
     * @return The attribute value.
     */
    private String substituteAttributes(final String input, final Saml2AuthenticatedPrincipal principal) {
        String output = input;

        for (String key : principal.getAttributes().keySet()) {
            final String escapedKey = Pattern.quote(key);
            output = output.replaceAll("\\{" + escapedKey + "\\}", getAttributeValue(principal, key));
        }

        return output.replaceAll("\\{[^\\}]*?\\}", "");
    }

    /**
     * Retrieves the value for the given key from the principal through pattern matching.
     *
     * @param principal The {@link Saml2AuthenticatedPrincipal}.
     * @param key The key for retrieving the value.
     * @return The attribute value.
     */
    private String getAttributeValue(final Saml2AuthenticatedPrincipal principal, final String key) {
        final String value = principal.getFirstAttribute(key);

        if (value == null) {
            return "";
        }

        final Pattern pattern = extractionPattern.get(key);

        if (pattern == null) {
            return value;
        }

        final Matcher matcher = pattern.matcher(value);

        if (matcher.matches()) {
            return matcher.group(SAML2Properties.ATTRIBUTE_VALUE_EXTRACTION_GROUP_NAME);
        } else {
            return value;
        }
    }

}
