package br.com.escalas;

import br.com.escalas.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
public class EscalaShekinahApplication {

	public static void main(String[] args) {
		SpringApplication.run(EscalaShekinahApplication.class, args);
	}

}
