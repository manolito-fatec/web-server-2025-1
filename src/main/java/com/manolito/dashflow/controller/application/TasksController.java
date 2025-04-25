package com.manolito.dashflow.controller.application;

import com.manolito.dashflow.service.application.TasksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Tag(name = "Tasks Controller", description = "Endpoints para a consulta de tasks no Data Warehouse de Tasks")
@RestController
@CrossOrigin(origins = "http://localhost:5173")
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }

    @GetMapping("/get-count-between/{userId}")
    @Operation(summary = "Busca o total de tasks de um usuário da aplicação dentro de um período de tempo", description = "Faz uma requisição ao DB e retorna o total de Tasks associadas a um usuário entre duas datas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total de tasks extraido com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Tasks para o usuário não existem."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    public ResponseEntity<?> getTotalTasksByUserIdBetween(
            @Parameter(description = "Id do usuario", required = true) @RequestBody Integer userId,
            @Parameter(description = "Data inicial do período", required = true) @RequestBody LocalDate startDate,
            @Parameter(description = "Data final do período", required = true) @RequestBody LocalDate endDate

    ) {
        try {
            return ResponseEntity.ok().body(tasksService.getTaskCountByOperatorIdBetween(userId, startDate, endDate));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }

    @GetMapping("/get-project-count-between/{projectId}/{startDate}/{endDate}")
    @Operation(summary = "Busca o total de tasks de um projeto dentro de um período de tempo", description = "Faz uma requisição ao DB e retorna o total de Tasks associadas a um projeto entre duas datas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total de tasks extraido com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Tasks para o usuário não existem."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    public ResponseEntity<?> getTotalTasksByProjectBetween(
            @Parameter(description = "Id do projeto", required = true) @PathVariable String projectId,
            @Parameter(description = "Data inicial do período", required = true) @PathVariable String startDate,
            @Parameter(description = "Data final do período", required = true) @PathVariable String endDate
    ) {
        try {
            return ResponseEntity.ok().body(tasksService.getCreatedAndCompletedTaskCountByProjectBetween(projectId, LocalDate.parse(startDate), LocalDate.parse(endDate)));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }

    @GetMapping("/get-count-between/{userId}/{startDate}/{endDate}")
    @Operation(summary = "Busca o total de tasks de um usuário da aplicação dentro de um período de tempo", description = "Faz uma requisição ao DB e retorna o total de Tasks associadas a um usuário entre duas datas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total de tasks extraido com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Tasks para o usuário não existem."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    public ResponseEntity<?> getTotalTasksByStatusByUserIdBetween(
            @Parameter(description = "Id do usuario", required = true) @PathVariable String userId,
            @Parameter(description = "Data inicial do período", required = true) @PathVariable String startDate,
            @Parameter(description = "Data final do período", required = true) @PathVariable String endDate

    ) {
        try {
            return ResponseEntity.ok().body(tasksService.getTaskCountByStatusByOperatorIdBetween(Integer.valueOf(userId), LocalDate.parse(startDate), LocalDate.parse(endDate)));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }
  
    @GetMapping("/average-time/{userId}")
    @Operation(summary = "Calcula a média de tempo de conclusão de tasks", description = "Faz uma requisição no BD, retornando a média de tempo que o usuário leva para concluir suas tasks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Média de tempo de conclusão de tasks extraída com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Não há tasks concluídas."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor ao tentar calcular a média de tempo.")
    })
    public ResponseEntity<?> getAverageByOperatorId(
            @Parameter(description = "id do usuário", required = true) @PathVariable Integer userId
    ) {
        try {
            return ResponseEntity.ok().body(tasksService.getAverageTimeCard(userId));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(noSuchElementException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }

    @GetMapping("/average-time-by-project/{projectId}")
    @Operation(summary = "Calcula a média de tempo de conclusão de tasks", description = "Faz uma requisição no BD, retornando a média de tempo que o usuário leva para concluir suas tasks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Média de tempo de conclusão de tasks extraída com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Não há tasks concluídas."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor ao tentar calcular a média de tempo.")
    })
    public ResponseEntity<?> getAverageTaskTimeByProjectId(
            @Parameter(description = "id do projeto", required = true) @PathVariable String projectId
    ) {
        try {
            return ResponseEntity.ok().body(tasksService.getAverageTimeCardByProjectId(projectId));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(noSuchElementException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }
  
    @GetMapping("/get-count/gestor/quantity-cards/{userId}")
    @Operation(summary = "Mostra contagem de tasks do gestor", description = "Retorna o número total de tasks em todos os projetos associados ao gestor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem de tasks retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Nenhuma task encontrada para este gestor"),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor ao tentar contar tasks")
    })
    public ResponseEntity<?> getTotalTasksForManager(
            @Parameter(description = "id do usuário gestor", required = true) @PathVariable Integer userId) {

        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
            }
            Integer totalCards = tasksService.getTotalCardsForManager(userId);
            return ResponseEntity.ok(totalCards);
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(noSuchElementException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }

    @GetMapping("/get-by-tag/{projectId}")
    @Operation(summary = "Busca o total de tasks por tags de um projeto", description = "Faz uma requisição ao DB e retorna o total de Tasks associadas a cada tag de um projeto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total de tasks por tag extraido com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Tasks não existem."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar tasks por tags de um projeto.")
    })
    public ResponseEntity<?> getTotalTasksByTagByProjectId(
            @Parameter(description = "Id do projeto", required = true) @PathVariable String projectId
    ) {
        try {
            return ResponseEntity.ok().body(tasksService.getTaskCountByTagByProjectId(projectId));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(noSuchElementException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }

    @GetMapping("/get-by-operator/{projectId}")
    @Operation(summary = "Busca o total de tasks por operador de um projeto", description = "Faz uma requisição ao DB e retorna o total de Tasks associadas a cada operador de um projeto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total de tasks por operador de um projeto extraido com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Tasks não existem."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar tasks por operador de um projeto.")
    })
    public ResponseEntity<?> getTotalTasksByOperatorByProjectId(
            @Parameter(description = "Id do projeto", required = true) @PathVariable String projectId
    ) {
        try {
            return ResponseEntity.ok().body(tasksService.getTaskCountByOperatorByProjectId(projectId));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(noSuchElementException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }

    @GetMapping("/{projectId}/reworks")
    @Operation(summary = "Busca o total de retrabalhos de um projeto", description = "Faz uma requisição ao DB e retorna o total de Tasks consideradas como retrabalhos de um projeto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total de retrabalhos de um projeto extraido com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Tasks não existem."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar tasks por operador de um projeto.")
    })
    public ResponseEntity<?> getReworksByProjectId(
            @Parameter(description = "Id do projeto", required = true) @PathVariable String projectId
    ) {
        try {
            return ResponseEntity.ok().body(tasksService.getReworksByProjectId(projectId));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(noSuchElementException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }
}
