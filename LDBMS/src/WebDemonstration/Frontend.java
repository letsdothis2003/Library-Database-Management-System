/**
 * File Path: src/main/java/WebDemonstration/Frontend.java
 */
package WebDemonstration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Spring Boot application.
 * This class tells the server to start the web engine and scan for @RestController classes.
 */
@SpringBootApplication
public class Frontend {
    public static void main(String[] args) {
        SpringApplication.run(Frontend.class, args);
    }
}