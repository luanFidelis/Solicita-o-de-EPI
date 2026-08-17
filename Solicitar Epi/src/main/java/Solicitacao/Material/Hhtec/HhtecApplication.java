package Solicitacao.Material.Hhtec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// NÃO remova o scanBasePackages. Testado: sem ele o Spring Data registra
// "Found 0 JPA repository interfaces" e a app não sobe (nenhum repositório é injetado);
// com ele encontra os 4 normalmente.
@SpringBootApplication(scanBasePackages = "Solicitacao")
public class HhtecApplication {

	public static void main(String[] args) {
		SpringApplication.run(HhtecApplication.class, args);
	}

}
