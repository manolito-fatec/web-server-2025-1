package com.manolito.dashflow.controller.dw;

import com.manolito.dashflow.repository.dw.UserRepository;
import com.manolito.dashflow.service.application.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/user")
    @ResponseBody
    public ResponseEntity<Long> getUserIdByOriginalId(@RequestParam String originalId) {
        Long userId = userRepository.getUserIdByOriginalId(originalId);
        if (userId != null) {
            return ResponseEntity.ok(userId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Busca todos os usuários de um projeto", description = "Faz uma requisição ao DB e retorna uma lista de usuários do data warehouse para um projeto dado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total de usuários de um projeto extraido com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    public ResponseEntity<?> getUsersByProjectId(
            @Parameter(description = "Id do projeto", required = true) @PathVariable("projectId") String projectId
    ) {
        try {
            return ResponseEntity.ok().body(userService.getUsersByProjectId(projectId));
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }

    @GetMapping("/paginated")
    @Operation(summary = "Busca uma lista paginada de usuários", description = "Busca uma lista paginada de usuários da aplicação em seus respectivos projetos de uma ferramenta. Admins são excluídos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuários paginados extraídos com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros paginados inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar usuários paginados")
    })
    public ResponseEntity<?> getUsersPaginated(
            @Parameter(description = "Numero da pagina (1-based)", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Numero de itens por pagina (default = 10)", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            return ResponseEntity.ok(userService.getUsersPaginated(page, size));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error retrieving users: " + e.getMessage());
        }
    }
}
