package fim.unipassau.de.scratch1984.spring.authentication;

import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.AuthenticationProvider;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom authentication provider for the application.
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    /**
     * The user service for accessing user information.
     */
    private final UserService userService;

    /**
     * The password encoder for hashing user passwords.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a new custom authentication provider with the given dependencies.
     *
     * @param userService The user service to use.
     * @param passwordEncoder The password encoder to use.
     */
    @Autowired
    public CustomAuthenticationProvider(final UserService userService, final PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticates a user with the given authentication and grants authorities based on the user's role.
     *
     * @param authentication The authentication information.
     * @return The new {@link UsernamePasswordAuthenticationToken} containing the authorities granted to the user.
     * @throws AuthenticationException If the user cannot be authenticated.
     */
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        Set<GrantedAuthority> authorities = new HashSet<>();
        UserDTO userDTO = userService.getUser(name);

        if (userDTO != null && userDTO.getRole() != null && userDTO.isActive()) {
            if (userDTO.getRole() == UserDTO.Role.PARTICIPANT || userDTO.getRole() == UserDTO.Role.ADMIN) {
                GrantedAuthority grantedAuthorityUser = new SimpleGrantedAuthority(Constants.ROLE_PARTICIPANT);
                authorities.add(grantedAuthorityUser);
            }

            if (userDTO.getRole() == UserDTO.Role.ADMIN) {
                GrantedAuthority grantedAuthorityAdmin = new SimpleGrantedAuthority(Constants.ROLE_ADMIN);
                authorities.add(grantedAuthorityAdmin);
            }
        }

        return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(),
                authorities);
    }

    /**
     * Returns whether the given authentication class is supported by the application.
     *
     * @param authentication The class of the authentication.
     * @return {@code true} if the given class is supported, or {@code false} if not.
     */
    @Override
    public boolean supports(final Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
