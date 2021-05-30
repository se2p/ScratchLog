package fim.unipassau.de.scratch1984;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main class starting the application.
 */
@SpringBootApplication
@EnableScheduling
public class Scratch1984Application {

    /**
     * Main class starting the application with the given arguments.
     *
     * @param args The arguments passed on application startup.
     */
    public static void main(final String[] args) {
        SpringApplication.run(Scratch1984Application.class, args);
    }

}
