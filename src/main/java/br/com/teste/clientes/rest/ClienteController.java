package br.com.teste.clientes.rest;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
import br.com.teste.clientes.model.repository.ClienteRepository;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin("http://localhost:4200")
public class ClienteController {

	private final ClienteRepository repository;

	@Autowired
	public ClienteController(ClienteRepository repository) {
		this.repository = repository;
	}
	
	 @Autowired
	 private InternacionalizacaoConfig messages;
	
	@GetMapping
	public List<Cliente> obterTodos(){
		return repository.findAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Cliente salvar(@RequestBody @Valid Cliente cliente) {
		return repository.save(cliente);
	}

	@GetMapping("{id}")
	public Cliente acharPorId(@PathVariable Integer id) {
		return repository
				.findById(id)
				.orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
						messages.getMessage("cliente.inexistente")));
	}
	
	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletar(@PathVariable Integer id) {
		repository
				.findById(id)
				.map(cliente -> {
					repository.delete(cliente);
					return Void.TYPE;
				})
				.orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
						messages.getMessage("cliente.inexistente")));
	}
	
	@PutMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void atualizar(@PathVariable Integer id, @RequestBody @Valid Cliente clienteAtualizado) {
		repository
				.findById(id)
				.map(cliente -> {
					cliente.setNome(clienteAtualizado.getNome());
					cliente.setCpf(clienteAtualizado.getCpf());
									
					return repository.save(cliente);
				})
				.orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}
}

