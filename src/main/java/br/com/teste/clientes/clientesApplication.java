package br.com.teste.clientes;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;

import br.com.teste.clientes.model.entity.Cliente;
import br.com.teste.clientes.model.repository.ClienteRepository;

@SpringBootApplication
public class clientesApplication {

	public static void main(String[] args) {
		SpringApplication.run(clientesApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

}
