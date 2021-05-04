package fim.unipassau.de.scratch1984.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The resource configuration for Spring.
 */
@Configuration
@EnableWebMvc
public class ResourceConfiguration implements WebMvcConfigurer {

    /**
     * Registers all the resource handlers needed to serve static resources at runtime.
     *
     * @param registry The registry storing the necessary information.
     */
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

}
