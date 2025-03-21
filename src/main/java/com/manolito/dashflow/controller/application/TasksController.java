package com.manolito.dashflow.controller.application;

import com.manolito.dashflow.service.application.TasksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Tag(name = "Tasks Controller", description = "Endpoints para a consulta de tasks no Data Warehouse de Tasks")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TasksController {

    private final TasksService tasksService;

    @GetMapping("/get-count/{userId}")
    @Operation(summary = "Busca o total de tasks de um usuário da aplicação", description = "Faz uma requisição ao DB e retorna o total de Tasks associadas a um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total de tasks extraido com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Tasks para o usuário não existem."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    public ResponseEntity<?> getTotalTasksByUserId(
            @Parameter(description = "Id do usuário", required = true) @PathVariable Integer userId
    ) {
        try {
            return ResponseEntity.ok().body(tasksService.getTaskCountByOperatorId(userId));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.internalServerError().body("Internal Server Error " + runtimeException.getMessage());
        }
    }
}
