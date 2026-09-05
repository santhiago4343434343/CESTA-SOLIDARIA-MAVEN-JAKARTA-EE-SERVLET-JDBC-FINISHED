package com.example.newproject004;

import classes.Pessoa;
import dao.PessoaDAO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Endpoint REST de Pessoa (JSON), servido pelo Jersey em /api/pessoas.
 * A senha nunca aparece na resposta por causa do @JsonbTransient no modelo.
 *
 * <pre>
 *   GET /api/pessoas        -> lista de pessoas em JSON
 *   GET /api/pessoas/{id}   -> uma pessoa (ou 404)
 * </pre>
 */
@Path("/pessoas")
public class PessoaResource {

    private final PessoaDAO dao = new PessoaDAO();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Pessoa> listar() {
        return dao.listar(null, 0, 100);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscar(@PathParam("id") int id) {
        Pessoa p = dao.buscarPorId(id);
        if (p == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"erro\":\"Pessoa nao encontrada\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.ok(p).build();
    }
}
