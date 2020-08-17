package br.com.teste.clientes.model.repository;

import br.com.teste.clientes.model.entity.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class ClienteRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    ClienteRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um cliente na base com o cpf informado")
    public void returnTrueWhenCPFExistsTest() {

        String cpf = "47442993001";
        Cliente cliente = Cliente.builder().nome("Fulano").cpf(cpf).build();
        entityManager.persist(cliente);

        boolean exists = repository.existsByCpf(cpf);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falso quando não existir um cliente na base com o cpf informado")
    public void returnFalseWhenCPFDoesntExistsTest() {

        String cpf = "47442993001";
        boolean exists = repository.existsByCpf(cpf);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um cliente por id")
    public void findByIdTest() {

        Cliente cliente = Cliente.builder().nome("Fulano").cpf("47442993001").build();
        entityManager.persist(cliente);

        Optional<Cliente> foundCliente = repository.findById(cliente.getId());

        assertThat(foundCliente.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um cliente")
    public void saveClienteTest() {

        Cliente cliente = Cliente.builder().nome("Fulano").cpf("47442993001").build();

        Cliente savedCliente = repository.save(cliente);

        assertThat(savedCliente.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um cliente")
    public void deleteClienteTest() {

        Cliente cliente = Cliente.builder().nome("Fulano").cpf("47442993001").build();
        entityManager.persist(cliente);
        Cliente foundCliente = entityManager.find(Cliente.class, cliente.getId());

        repository.delete(foundCliente);

        Cliente deletedCliente = entityManager.find(Cliente.class, cliente.getId());
        assertThat(deletedCliente).isNull();
    }
}
