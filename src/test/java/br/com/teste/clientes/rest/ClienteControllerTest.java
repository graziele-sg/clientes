package br.com.teste.clientes.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.teste.clientes.config.InternacionalizacaoConfig;
import br.com.teste.clientes.model.entity.Cliente;

@AutoConfigureMockMvc
@SpringBootTest
public class ClienteControllerTest {
	@MockBean
	private ClienteController service;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private InternacionalizacaoConfig messages;

	@Test
	@DisplayName("Retornar todos os clientes")
	void testObterTodosSucess() throws Exception {
		LocalDate data = LocalDate.now();
		String dataEsperada = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

		Cliente cliente1 = new Cliente(1, "Fulano", "47442993001", data);
		Cliente cliente2 = new Cliente(2, "Beltrano", "09109481001", data);
		doReturn(Lists.newArrayList(cliente1, cliente2)).when(service).obterTodos();

		mockMvc.perform(get("/api/clientes")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id", is(1))).andExpect(jsonPath("$[0].nome", is("Fulano")))
				.andExpect(jsonPath("$[0].cpf", is("47442993001")))
				.andExpect(jsonPath("$[0].dataCadastro", is(dataEsperada))).andExpect(jsonPath("$[1].id", is(2)))
				.andExpect(jsonPath("$[1].nome", is("Beltrano"))).andExpect(jsonPath("$[1].cpf", is("09109481001")))
				.andExpect(jsonPath("$[1].dataCadastro", is(dataEsperada)));
	}

	@Test
	@DisplayName("Salvar um cliente")
	void testSalvar() throws Exception {
		LocalDate data = LocalDate.now();
		String dataEsperada = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

		Cliente clienteToPost = new Cliente(null, "Fulano", "47442993001", null);
		Cliente clienteToReturn = new Cliente(1, "Fulano", "47442993001", data);
		doReturn(clienteToReturn).when(service).salvar(any());

		mockMvc.perform(
				post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(asJsonString(clienteToPost)))
				.andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id", is(1))).andExpect(jsonPath("$.nome", is("Fulano")))
				.andExpect(jsonPath("$.cpf", is("47442993001")))
				.andExpect(jsonPath("$.dataCadastro", is(dataEsperada)));
	}

	@Test
	@DisplayName("Procurar um cliente pelo id")
	void testAcharPorId() throws Exception {
		LocalDate data = LocalDate.now();
		String dataEsperada = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		int id = 1;

		Cliente cliente1 = new Cliente(id, "Fulano", "47442993001", data);
		doReturn(cliente1).when(service).acharPorId(id);

		mockMvc.perform(get("/api/clientes/{id}", id)).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id", is(id)))
				.andExpect(jsonPath("$.nome", is("Fulano"))).andExpect(jsonPath("$.cpf", is("47442993001")))
				.andExpect(jsonPath("$.dataCadastro", is(dataEsperada)));
	}

	@Test
	@DisplayName("Retornar a mensagem de Not Found quando procurar um cliente inexistente")
	void testAcharPorIdNotFound() throws Exception {
		int id = 1;
		String mensagem = messages.getMessage("cliente.inexistente");

		doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, mensagem)).when(service).acharPorId(id);

		mockMvc.perform(get("/api/clientes/{id}", id)).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Deletar um cliente")
	void testDeletar() throws Exception {
		int id = 1;
		doNothing().when(service).deletar(id);

		mockMvc.perform(delete("/api/clientes/{id}", id)).andExpect(status().isNoContent());		
		assertDoesNotThrow( () -> service.deletar(id) );

	}

	@Test
	@DisplayName("Atualizar um cliente")
	void testAtualizar() throws Exception {
		LocalDate data = LocalDate.now();
		int id = 1;	
		Cliente clienteToPut = new Cliente(null, "Fulano", "47442993001", null);
		Cliente clienteToReturnFindBy = new Cliente(id, "Fulano", "47442993001", data);
		Cliente clienteToReturnSave = new Cliente(id, "Fulano certo", "47442993001", data);
		
		doReturn(clienteToReturnFindBy).when(service).acharPorId(id);
		doReturn(clienteToReturnSave).when(service).salvar(any());
		
		mockMvc.perform(put("/api/clientes/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(clienteToPut)))		
				.andExpect(status().isNoContent());		
	}
	
	static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
