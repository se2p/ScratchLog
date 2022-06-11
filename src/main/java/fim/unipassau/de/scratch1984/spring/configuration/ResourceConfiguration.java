package fim.unipassau.de.scratch1984.spring.configuration;

import fim.unipassau.de.scratch1984.util.ApplicationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The resource configuration for Spring.
 */
@Configuration
public class ResourceConfiguration implements WebMvcConfigurer {

    /**
     * Registers all the resource handlers needed to serve static resources at runtime.
     *
     * @param registry The registry storing the necessary information.
     */
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("/webjars/");
    }

    /**
     * Registers all the view controllers to forward to templates at runtime.
     *
     * @param registry The registry storing the necessary information.
     */
    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addRedirectViewController("/login/saml2", "/saml2/login");
    }

    /**
     * Configures the {@link CorsRegistry} to accept cross-origin requests from the specified URL.
     *
     * @param registry The registry storing the necessary information.
     */
    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/store/*").allowedMethods("GET",
                "POST").allowedOrigins(ApplicationProperties.GUI_BASE_URL);
        registry.addMapping("/login/saml2").allowedMethods("GET",
                "POST").allowedOrigins(ApplicationProperties.SAML2_BASE_URL);
        registry.addMapping("/saml2/**").allowedMethods("GET",
                "POST").allowedOrigins(ApplicationProperties.SAML2_BASE_URL);
    }

}
