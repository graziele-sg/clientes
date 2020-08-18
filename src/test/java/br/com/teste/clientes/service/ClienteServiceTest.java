package br.com.teste.clientes.service;

import br.com.teste.clientes.config.InternacionalizacaoConfig;
import br.com.teste.clientes.exception.BusinessException;
import br.com.teste.clientes.model.entity.Cliente;
import br.com.teste.clientes.model.repository.ClienteRepository;
import br.com.teste.clientes.service.impl.ClienteServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class ClienteServiceTest {
    ClienteService service;

    @MockBean
    ClienteRepository repository;

    @BeforeEach
    public void SetUp() {
        this.service = new ClienteServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um cliente")
    public void saveClienteTest() {
        Long id = 1L;
        String nome = "Fulano";
        String cpf = "47442993001";
        LocalDate data = LocalDate.now();
        Cliente clienteSaving = Cliente.builder().nome(nome).cpf(cpf).build();
        Cliente clienteSaved = new Cliente(id, nome, cpf, data);

        Mockito.when(repository.existsByCpf(Mockito.anyString())).thenReturn(false);
        Mockito.when(repository.save(clienteSaving)).thenReturn(clienteSaved);

        Cliente savedCliente = service.save(clienteSaving);

        assertThat(savedCliente.getId()).isEqualTo(id);
        assertThat(savedCliente.getNome()).isEqualTo(nome);
        assertThat(savedCliente.getCpf()).isEqualTo(cpf);
        assertThat(savedCliente.getDataCadastro()).isEqualTo(data);
    }

    @Test
    @DisplayName("Deve lançar o erro BusinessException ao tentar salvar um cliente com cpf duplicado")
    public void shoulNotSaveAClienteWithDuplicatedCpfTest() {
        String mensagemErro = "campo.cpf.ja.cadastrado";
        Cliente cliente = Cliente.builder().nome("Fulano").cpf("47442993001").build();
        Mockito.when(repository.existsByCpf(Mockito.anyString())).thenThrow( new BusinessException(mensagemErro));

        Throwable exception = Assertions.catchThrowable(() -> service.save(cliente));

        assertThat(exception).isInstanceOfAny(BusinessException.class).hasMessage("campo.cpf.ja.cadastrado");
        Mockito.verify(repository, Mockito.never()).save(cliente);
    }

    @Test
    @DisplayName("Deve obter um cliente por id")
    public void getByIdTest() {

        Long id = 1L;
        Cliente cliente = new Cliente(1L, "Fulano", "47442993001", null);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(cliente));

        Optional<Cliente> foundCliente = service.getById(id);

        assertThat(foundCliente.isPresent()).isTrue();
        assertThat(foundCliente.get().getId()).isEqualTo(cliente.getId());
        assertThat(foundCliente.get().getNome()).isEqualTo(cliente.getNome());
        assertThat(foundCliente.get().getCpf()).isEqualTo(cliente.getCpf());
        assertThat(foundCliente.get().getDataCadastro()).isEqualTo(cliente.getDataCadastro());
    }

    @Test
    @DisplayName("Deve retornar empty ao obter um cliente por id quando ele não existe na base")
    public void ClienteNotFoundByIdTest() {

        Long id = 1L;
        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Cliente> cliente = service.getById(id);

        assertThat(cliente.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve deletar um cliente")
    public void deleteClienteTest() {

        Cliente cliente = Cliente.builder().id(1L).build();

        assertDoesNotThrow( () -> service.delete(cliente) );

        Mockito.verify(repository, Mockito.times(1)).delete(cliente);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar um cliente inexistente")
    public void deleteInvalidClienteTest() {

        Cliente cliente = new Cliente();

        InternacionalizacaoConfig messagesMock = Mockito.mock(InternacionalizacaoConfig.class);
        Mockito.when(messagesMock.getMessage(anyString())).thenReturn("cliente.id.nulo");
        ReflectionTestUtils.setField( service, "messages", messagesMock);

        assertThrows(IllegalArgumentException.class, () -> service.delete(cliente) );
        Mockito.verify(repository, Mockito.never()).delete(cliente);
    }

    @Test
    @DisplayName("Deve atualizar um cliente")
    public void updateClienteTest() {

        Long id = 1L;
        LocalDate data = LocalDate.now();
        Cliente updatingCliente = new Cliente(id, "Fulano", "47442993001", data);
        Cliente updatedCliente = new Cliente(id, "Cicrano", "08607652028", data);

        Mockito.when(repository.save(updatingCliente)).thenReturn(updatedCliente);

        Cliente cliente = service.update(updatingCliente);

        assertThat(cliente.getId()).isEqualTo(updatedCliente.getId());
        assertThat(cliente.getNome()).isEqualTo(updatedCliente.getNome());
        assertThat(cliente.getCpf()).isEqualTo(updatedCliente.getCpf());
        assertThat(cliente.getDataCadastro()).isEqualTo(updatedCliente.getDataCadastro());
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar um cliente inexistente")
    public void updateInvalidClienteTest() {

        Cliente cliente = new Cliente();

        InternacionalizacaoConfig messagesMock = Mockito.mock(InternacionalizacaoConfig.class);
        Mockito.when(messagesMock.getMessage(anyString())).thenReturn("cliente.id.nulo");
        ReflectionTestUtils.setField( service, "messages", messagesMock);

        assertThrows(IllegalArgumentException.class, () -> service.update(cliente) );

        Mockito.verify(repository, Mockito.never()).save(cliente);
    }

    @Test
    @DisplayName("Deve filtrar clientes pela propriedades")
    public void findClienteTest() {

        int pagePR = 0;
        int sizePR = 10;
        Cliente cliente = new Cliente(1L, "Fulano", "47442993001", null);
        PageRequest pageRequest = PageRequest.of(pagePR,sizePR);
        List<Cliente> lista = Arrays.asList(cliente);
        Page<Cliente> page = new PageImpl<Cliente>(lista, pageRequest, 1);
        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Cliente> result = service.find(cliente, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(pagePR);
        assertThat(result.getPageable().getPageSize()).isEqualTo(sizePR);
    }

}
