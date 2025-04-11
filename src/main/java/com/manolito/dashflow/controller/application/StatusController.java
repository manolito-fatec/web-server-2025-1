package com.manolito.dashflow.controller.application;

import com.manolito.dashflow.service.application.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.NoSuchElementException;

@Tag(name = "Status Controller", description = "Endpoints para a consulta de status no Data Warehouse de Tasks")
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/status")
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    @GetMapping("/{userId}/{projectId}")
    @Operation(summary = "Busca a soma de tasks por status de um projeto do usuário",
            description = "Faz uma requisição ao DB e retorna o nome e a quantidade de status " +
                    "do projeto que o usuário referenciado pertence")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quantidade de status extraída com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Status para o projeto do usuário não existem."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    public ResponseEntity<?> getTaskCountGroupByStatusByUserIdAndProjectId(
            @Parameter(description = "Id do usuário", required = true) @PathVariable Integer userId,
            @Parameter(description = "Id do projeto", required = true) @PathVariable String projectId
    ) {
        try {
            return ResponseEntity.ok().body(statusService.getTaskCountGroupByStatusByUserIdAndProjectId(userId, projectId));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Busca a soma de tasks por status de um projeto",
            description = "Faz uma requisição ao DB e retorna o nome do status e a quantidade de tasks referentes a ela em projeto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quantidade de status extraída com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Status para o projeto não existem."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    public ResponseEntity<?> getTaskCountGroupByStatusByProjectId(
            @Parameter(description = "Id do projeto", required = true) @PathVariable String projectId
    ) {
        try {
            return ResponseEntity.ok().body(statusService.getTaskCountGroupByStatusByProjectId(projectId));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }
}
