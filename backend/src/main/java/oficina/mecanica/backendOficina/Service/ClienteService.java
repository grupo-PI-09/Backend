package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.Model.ClienteModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteService {

    private List<ClienteModel> listaClientes = new ArrayList<>();
    private Long proximoId = 1L;

    public List<ClienteModel> listarTodos() {
        return listaClientes;
    }

    public ClienteModel salvar(ClienteModel cliente) {
        cliente.setId(proximoId++);
        listaClientes.add(cliente);
        return cliente;
    }

    public List<ClienteModel> buscarPorNome(String nome) {
        return listaClientes.stream().filter(c -> c.getNome().toLowerCase().contains(nome.toLowerCase())).toList();
    }

    public boolean excluir(Long id) {
        return listaClientes.removeIf(c -> c.getId().equals(id));
    }

    public Optional<ClienteModel> buscarPorId(Long id) {
        return listaClientes.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public ClienteModel atualizar(Long id, ClienteModel dadosAtualizados) {
        Optional<ClienteModel> editarCliente = buscarPorId(id);

        if (editarCliente.isPresent()) {
            ClienteModel cliente = editarCliente.get();

            cliente.setNome(dadosAtualizados.getNome());
            cliente.setEmail(dadosAtualizados.getEmail());
            cliente.setTelefone(dadosAtualizados.getTelefone());
            cliente.setVeiculos(dadosAtualizados.getVeiculos());

            return cliente;
        }
        return null;
    }
}
