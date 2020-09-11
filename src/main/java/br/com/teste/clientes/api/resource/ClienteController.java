package br.com.teste.clientes.api.resource;

import java.util.List;
import java.util.stream.Collectors;

import br.com.teste.clientes.api.dto.ClienteDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.teste.clientes.config.InternacionalizacaoConfig;
import br.com.teste.clientes.model.entity.Cliente;
import br.com.teste.clientes.service.ClienteService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin("http://localhost:4200")
@Slf4j
@Api("API de Clientes")
public class ClienteController {

	private ClienteService service;

	private ModelMapper modelMapper;

	public ClienteController(ClienteService service, ModelMapper modelMapper) {
		this.service = service;
		this.modelMapper = modelMapper;
	}

	@Autowired
	private InternacionalizacaoConfig messages;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Adicionar um cliente")
	@ApiResponses({
			@ApiResponse(code = 201, message = "Cliente adicionado com sucesso")
	})
	public ClienteDTO create(@RequestBody @Valid ClienteDTO dto) {
		log.info("Criando um cliente com cpf {} e nome {}", dto.getCpf(), dto.getNome() );
		Cliente entity = modelMapper.map(dto, Cliente.class);

		entity = service.save(entity);
		return modelMapper.map(entity, ClienteDTO.class);
	}

	@GetMapping("{id}")
	@ApiOperation("Obter cliente pelo id")
	public ClienteDTO get(@PathVariable Long id) {
		log.info("Obtendo detalhes do cliente com id {} ", id);
		return service.getById(id).map(cliente -> modelMapper.map(cliente, ClienteDTO.class))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						messages.getMessage("cliente.inexistente")));
	}
	
	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation("Deletar cliente pelo id")
	public void delete(@PathVariable Long id) {
		log.info("Deletando cliente com id {} ", id);
		Cliente cliente = service.getById(id)
				.orElseThrow(() ->
						new ResponseStatusException(HttpStatus.NOT_FOUND,
								messages.getMessage("cliente.inexistente")));
		service.delete(cliente);
	}
	
	@PutMapping("{id}")
	@ApiOperation("Atualizar um cliente")
	public ClienteDTO update(@PathVariable Long id, @RequestBody @Valid ClienteDTO dto) {
		log.info("Atualizando cliente com id {} ", id);
		return service.getById(id).map(cliente -> {
			cliente.setCpf(dto.getCpf());
			cliente.setNome(dto.getNome());
			cliente = service.update(cliente);
			return modelMapper.map(cliente, ClienteDTO.class);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
				messages.getMessage("cliente.inexistente")));
	}

	@GetMapping
	@ApiOperation("Busca de clientes pelos parâmetros")
	public Page<ClienteDTO> find(ClienteDTO dto, Pageable pageRequest) {
		log.info("Utilizando a busca avançada de clientes");
		Cliente filter = modelMapper.map(dto, Cliente.class);
		Page<Cliente> result = service.find(filter, pageRequest);
		List<ClienteDTO> list = result.getContent()
				.stream()
				.map(entity -> modelMapper.map(entity, ClienteDTO.class))
				.collect(Collectors.toList());
		return new PageImpl<ClienteDTO>(list, pageRequest, result.getTotalElements());
	}
}

