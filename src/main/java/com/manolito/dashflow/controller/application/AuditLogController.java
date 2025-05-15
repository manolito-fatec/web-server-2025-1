package com.manolito.dashflow.controller.application;

import com.manolito.dashflow.dto.application.AuditLogDto;
import com.manolito.dashflow.dto.application.AuditLogFilterRequestDto;
import com.manolito.dashflow.service.application.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

@Tag(name = "Audit Logs", description = "Audit Logs Management APIs")
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditLogService auditLogService;

    @Operation(summary = "Busca de logs com parâmetros opcionais de usuário, resposta e período")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca de logs bem-sucedida."),
            @ApiResponse(responseCode = "400", description = "Requisição e/ou parâmetros mal formulados."),
            @ApiResponse(responseCode = "401", description = "Não autorizado, realize o login."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor no cadastro de usuário.")
    })
    @GetMapping
    public ResponseEntity<Page<AuditLogDto>> getAuditLogs(
            @Parameter(description = "Parâmetro opcional para filtro pelo ID do usuário da aplicação")
            @RequestParam(required = false) Integer userId,

            @Parameter(description = "Parâmetro opcional para filtro pela resposta da requisição")
            @RequestParam(required = false) Integer responseStatus,

            @Parameter(description = "Parâmetro opcional para filtro a partir de uma data")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Parâmetro opcional para filtro até uma data")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @Parameter(description = "Parâmetro para busca da página da resposta, default = 0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Parâmetro para definir a quantidade de itens de uma página da resposta, default = 20, max = 100")
            @RequestParam(defaultValue = "20") @Min(20) @Max(100) int size) {

        AuditLogFilterRequestDto filterRequest = AuditLogFilterRequestDto.builder()
                .userId(userId)
                .responseStatus(responseStatus)
                .startDate(startDate)
                .endDate(endDate)
                .page(page)
                .size(size)
                .build();

        Page<AuditLogDto> result = auditLogService.getFilteredAuditLogs(filterRequest);
        return ResponseEntity.ok(result);
    }
}
