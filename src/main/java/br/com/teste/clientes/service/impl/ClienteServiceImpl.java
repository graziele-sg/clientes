package br.com.teste.clientes.service.impl;

import br.com.teste.clientes.config.InternacionalizacaoConfig;
import br.com.teste.clientes.exception.BusinessException;
import br.com.teste.clientes.model.entity.Cliente;
import br.com.teste.clientes.model.repository.ClienteRepository;
import br.com.teste.clientes.service.ClienteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ClienteServiceImpl implements ClienteService {

    private ClienteRepository repository;

    public ClienteServiceImpl(ClienteRepository repository) {
        this.repository = repository;
    }

    @Autowired
    private InternacionalizacaoConfig messages;

    @Override
    public Cliente save(Cliente cliente) {
        if (repository.existsByCpf(cliente.getCpf())) {
            throw new BusinessException(messages.getMessage("campo.cpf.ja.cadastrado"));
        }
        return repository.save(cliente);
    }

    @Override
    public Optional<Cliente> getById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public void delete(Cliente cliente) {
        String mensagem = messages.getMessage("cliente.id.nulo");
        if (cliente == null || cliente.getId() == null) {
            log.warn(mensagem);
            throw new IllegalArgumentException(mensagem);
        }
        this.repository.delete(cliente);
    }

    @Override
    public Cliente update(Cliente cliente) {
        String mensagem = messages.getMessage("cliente.id.nulo");
        if (cliente == null || cliente.getId() == null) {
            log.warn(mensagem);
            throw new IllegalArgumentException(mensagem);
        }
        return this.repository.save(cliente);
    }

    @Override
    public Page<Cliente> find(Cliente filter, Pageable pageRequest) {
        Example<Cliente> example =
                Example.of(filter, ExampleMatcher.matching().withIgnoreCase().withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        return repository.findAll(example, pageRequest);
    }
}
