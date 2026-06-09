package nl.andresilva.pcschecker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.cache.annotation.EnableCaching
public class PcsCheckerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PcsCheckerApiApplication.class, args);
	}

}
