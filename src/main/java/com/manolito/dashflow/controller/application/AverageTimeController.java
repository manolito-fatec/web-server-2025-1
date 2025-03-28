package com.manolito.dashflow.controller.application;
import com.manolito.dashflow.service.application.AverageTimeService;
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

@Tag(name = "Average Completion Time Card Controller", description = "Endpoints para a consulta de tempo medio de conclusao de tasks do usuario")
@RestController
@RequestMapping("/average-time")
@RequiredArgsConstructor
public class AverageTimeController {
    private AverageTimeService averageTimeService;

    @GetMapping("/get-average/{userId}")
    @Operation(summary = "Calcula a média de tempo de conclusão de taks", description = "Faz uma requisição no BD, retornando a média de tempo que o usuário leva para concluir suas tasks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Média de tempo de conclusão de tasks extraidocom sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Não há tasks concluidas."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor ao tentar buscar o local.")
    })
    public ResponseEntity<?> getAverageByOperatorId(
            @Parameter(description = "id do usuario", required = true) @PathVariable Integer userId
    ) {
        try {
            return ResponseEntity.ok().body(averageTimeService.getAverageTimeCard(userId));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(noSuchElementException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }
}
