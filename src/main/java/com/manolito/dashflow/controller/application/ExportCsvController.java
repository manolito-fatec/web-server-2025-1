package com.manolito.dashflow.controller.application;

import com.manolito.dashflow.service.application.ExportCsvService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Tag(name = "Export CSV Controller", description = "Endpoints para a busca de dados de projetos dentro do Data Warehouse")
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/export-csv")
@RequiredArgsConstructor
public class ExportCsvController {
    private final ExportCsvService service;

    @GetMapping
    @Operation(summary = "Obter estatísticas de projetos para exportação CSV",
            description = """
            Recupera dados completos de todos os projetos incluindo:
            - Nome do projeto
            - Nome do gerente responsável
            - Quantidade de operadores
            - Quantidade de cards/tarefas
            
            Os dados são formatados para exportação em CSV com fins administrativos.
            """)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = """
                Operação bem-sucedida. Retorna uma lista com:
                - Nome do projeto (Project)
                - Nome do gerente (Manager)
                - Quantidade de operadores (quantityOfOperators)
                - Quantidade de cards (quantityOfCards)
                """
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nenhum dado de projeto encontrado para exportação"
            ),
            @ApiResponse(
                    responseCode = "408",
                    description = "Tempo limite da requisição excedido"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                Erro interno no servidor. Possíveis causas:
                - Falha na conexão com o banco de dados
                - Erro inesperado no processamento dos dados
                """
            )
    })
    public ResponseEntity<?> getCsvExport() {
        try {
            return ResponseEntity.ok().body(service.getProjectsCountByUserId());
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error " + runtimeException.getMessage());
        }
    }
}
