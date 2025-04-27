package com.manolito.dashflow.controller.application;

import com.manolito.dashflow.dto.dw.IssueCountDto;
import com.manolito.dashflow.enums.IssuePriority;
import com.manolito.dashflow.enums.IssueSeverity;
import com.manolito.dashflow.service.application.IssuesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@Tag(name = "Issues Controller", description = "Endpoints para a consulta de issues no Data Warehouse de Issues")
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/issues")
@RequiredArgsConstructor
public class IssuesController {
    private final IssuesService issuesService;

    @GetMapping("/gestor/issue/{projectId}/{severity}/{priority}")
    @Operation(summary = "Busca a quantidade de issues por projeto, severidade e prioridade",
            description = "Retorna a contagem de issues agrupadas por tipo (bug, enhancement, question)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem de issues extraída com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Nenhuma issue encontrada para os critérios fornecidos."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor.")
    })
    public ResponseEntity<?> getIssueCounts(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable String projectId,

            @Parameter(description = "Grau de severidade da issue", required = true)
            @PathVariable String severity,

            @Parameter(description = "Prioridade da issue", required = true)
            @PathVariable String priority) {

        try {
            return ResponseEntity.ok()
                    .body(issuesService.getIssueCountsByProjectSeverityAndPriority(projectId, IssueSeverity.valueOf(severity), IssuePriority.valueOf(priority)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getMessage());
        }
    }

    @GetMapping("/gestor/current-issues/{projectId}")
    @Operation(summary = "Busca todas as issues atuais agrupadas por tipo",
            description = "Retorna a quantidade de bugs, enhancements e questions do projeto atual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem de issues agrupada extraída com sucesso."),
            @ApiResponse(responseCode = "404", description = "Nenhuma issue encontrada para o projeto informado."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor.")
    })
    public ResponseEntity<List<IssueCountDto>> getAllCurrentIssuesGroupedByType(
            @Parameter(description = "ID do projeto", required = true)
            @PathVariable String projectId) {

        try {
            List<IssueCountDto> groupedIssues = issuesService.getAllCurrentIssuesGroupedByType(projectId);
            return ResponseEntity.ok(groupedIssues);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
