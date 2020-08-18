package br.com.teste.clientes.api.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

import br.com.teste.clientes.api.dto.ClienteDTO;
import br.com.teste.clientes.exception.BusinessException;
import br.com.teste.clientes.service.ClienteService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.teste.clientes.config.InternacionalizacaoConfig;
import br.com.teste.clientes.model.entity.Cliente;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = ClienteController.class)
@AutoConfigureMockMvc
public class ClienteControllerTest {

	static final String CLIENTE_API = "/api/clientes";

	@Autowired
	MockMvc mvc;

	@MockBean
	ClienteService service;

	@MockBean
	InternacionalizacaoConfig messages;

	@Test
	@DisplayName("Deve criar um cliente com sucesso")
	public void createClienteTest() throws Exception {
		//cenário
		Long id = 1L;
		String nome = "Fulano";
		String cpf = "47442993001";
		LocalDate data = LocalDate.now();
		String dataEsperada = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		ClienteDTO dto = new ClienteDTO(null, nome, cpf, null);
		Cliente clienteSalvo = new Cliente(id, nome, cpf, data);

		BDDMockito.given(service.save(Mockito.any(Cliente.class))).willReturn(clienteSalvo);

		//execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(CLIENTE_API)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto));

		//verificação
		mvc.perform(request).andExpect(status().isCreated())
				.andExpect(jsonPath("id").value(clienteSalvo.getId()))
				.andExpect(jsonPath("nome").value(clienteSalvo.getNome()))
				.andExpect(jsonPath("cpf").value(clienteSalvo.getCpf()))
				.andExpect(jsonPath("dataCadastro", is(dataEsperada)));
	}

	@Test
	@DisplayName("Deve lançar erro de validação quando campos obrigatórios não forem preenchidos")
	public void createInvalidClienteTest() throws Exception {

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(CLIENTE_API)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(asJsonString(new ClienteDTO()));

		mvc.perform(request).andExpect(status().isBadRequest())
				.andExpect(jsonPath("errors", hasSize(2)))
				.andExpect(jsonPath("errors[0]", Matchers.containsString("obrigatorio")))
				.andExpect(jsonPath("errors[1]", Matchers.containsString("obrigatorio")));
		//mensagensErro
		//{campo.nome.obrigatorio}
		//{campo.cpf.obrigatorio}
	}

	@Test
	@DisplayName("Deve lançar erro de validação quando o cpf for inválido")
	public void CreateClienteInvalidCpfTest() throws Exception {

		ClienteDTO dto = new ClienteDTO(null, "Fulano", "11111111", null);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(CLIENTE_API)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto));

		mvc.perform(request).andExpect(status().isBadRequest())
				.andExpect(jsonPath("errors", hasSize(1)))
				.andExpect(jsonPath("errors[0]").value("{campo.cpf.invalido}"));
	}

	@Test
	@DisplayName("Deve lançar erro ao tentar cadastrar um um cliente com cpf já cadastrado")
	public void createClienteWithDuplicatedCpfTest() throws Exception {

		String mensagemErro = "campo.cpf.ja.cadastrado";
		ClienteDTO dto = new ClienteDTO(null, "Fulano", "47442993001", null);
		BDDMockito.given(service.save(Mockito.any(Cliente.class)))
				.willThrow(new BusinessException(mensagemErro));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(CLIENTE_API)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto));

		mvc.perform(request).andExpect(status().isBadRequest())
				.andExpect(jsonPath("errors", hasSize(1)))
				.andExpect(jsonPath("errors[0]").value(mensagemErro));
	}

	@Test
	@DisplayName("Deve buscar um cliente pelo id")
	void getClienteByIdTest() throws Exception {
		Long id = 1L;
		LocalDate data = LocalDate.now();
		String dataEsperada = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		Cliente cliente = new Cliente(id, "Fulano", "47442993001", data);
		BDDMockito.given(service.getById(id)).willReturn(Optional.of(cliente));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(CLIENTE_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isOk())
				.andExpect(jsonPath("id").value(cliente.getId()))
				.andExpect(jsonPath("nome").value(cliente.getNome()))
				.andExpect(jsonPath("cpf").value(cliente.getCpf()))
				.andExpect(jsonPath("dataCadastro").value(dataEsperada));
	}

	@Test
	@DisplayName("Deve retornar resource not found quando o cliente procurado não existir")
	public void clienteNotFoundTest() throws Exception {

		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(CLIENTE_API.concat("/" + 1))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Deve deletar um cliente")
	public void deleteClienteTest() throws Exception {

		Long id = 1L;
		BDDMockito.given(service.getById(Mockito.anyLong()))
				.willReturn(Optional.of(Cliente.builder().id(id).build()));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(CLIENTE_API.concat("/" + id));

		mvc.perform(request).andExpect(status().isNoContent());

	}

	@Test
	@DisplayName("Deve retornar resource not found quando não encontrar o cliente para deletar")
	public void deleteInexistenClienteTest() throws Exception {

		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(CLIENTE_API.concat("/" + 1));

		mvc.perform(request).andExpect(status().isNotFound());

	}

	@Test
	@DisplayName("Deve atualizar um livro")
	public void updateBookTest() throws Exception {

		Long id = 1L;
		LocalDate data = LocalDate.now();
		String dataEsperada = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		Cliente updatingCliente = new Cliente(id, "Fulano", "47442993001", null);
		Cliente updatedCliente = new Cliente(id, "Cicrano", "08607652028", data);
		ClienteDTO dto = new ClienteDTO(updatingCliente.getId(),updatingCliente.getNome(),
				updatingCliente.getCpf(), updatingCliente.getDataCadastro());
		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(updatingCliente));
		BDDMockito.given(service.update(updatingCliente)).willReturn(updatedCliente);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(CLIENTE_API.concat("/" + id))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto));

		mvc.perform(request).andExpect(status().isOk())
				.andExpect(status().isOk())
				.andExpect(jsonPath("id").value(updatedCliente.getId()))
				.andExpect(jsonPath("nome").value(updatedCliente.getNome()))
				.andExpect(jsonPath("cpf").value(updatedCliente.getCpf()));
				//.andExpect(jsonPath("dataCadastro").value(updatedCliente.getDataCadastro()));
	}

	@Test
	@DisplayName("Deve filtrar clientes")
	public void findClienteTest() throws Exception {

		Long id = 1L;
		LocalDate data = LocalDate.now();
		String dataEsperada = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		int pagePR = 0;
		int sizePR = 100;
		Cliente cliente = new Cliente(id, "Fulano", "47442993001", data);

		BDDMockito.given(service.find(Mockito.any(Cliente.class), Mockito.any(Pageable.class)))
				.willReturn(new PageImpl<Cliente>(Arrays.asList(cliente), PageRequest.of(0, 100), 1));

		String queryString = String.format("?nome=%s&page=0&size=100", cliente.getNome());
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(CLIENTE_API.concat(queryString))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isOk())
				.andExpect(status().isOk())
				.andExpect(jsonPath("content", Matchers.hasSize(1)))
				.andExpect(jsonPath("totalElements").value(1))
				.andExpect(jsonPath("pageable.pageSize").value(sizePR))
				.andExpect(jsonPath("pageable.pageNumber").value(pagePR));
	}

	static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
