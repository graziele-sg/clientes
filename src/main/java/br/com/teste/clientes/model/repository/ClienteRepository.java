package br.com.teste.clientes.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.teste.clientes.model.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long>{

    boolean existsByCpf(String cpf);
}
