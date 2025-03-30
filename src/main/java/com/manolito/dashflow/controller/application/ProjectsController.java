package com.manolito.dashflow.controller.application;

import com.manolito.dashflow.service.application.ProjectsService;
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

@Tag(name = "Projects Controller", description = "Endpoints para a consulta de projetos no Data Warehouse de Tasks")
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectsController {
    private final ProjectsService projectsService;

    @GetMapping("/get-count/{userId}")
    @Operation(summary = "Busca o total de projetos no qual um usuário da aplicação pertence", description = "Faz uma requisição ao DB e retorna o total de Projetos associados a um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total de projetos extraido com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Projetos para o usuário não existem."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    public ResponseEntity<?> getTotalProjectsByUserId(
            @Parameter(description = "Id do usuário", required = true) @PathVariable("userId") Integer userId
    ) {
        try {
            return ResponseEntity.ok().body(projectsService.getProjectsCountByUserId(userId));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }
}
