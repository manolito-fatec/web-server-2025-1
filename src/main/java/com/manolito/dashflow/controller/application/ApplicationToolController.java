package com.manolito.dashflow.controller.application;

import com.manolito.dashflow.dto.application.ApplicationToolDto;
import com.manolito.dashflow.dto.application.ApplicationUserDto;
import com.manolito.dashflow.service.application.ApplicationToolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tool")
public class ApplicationToolController {
    private final ApplicationToolService toolService;

    @Operation(summary = "Busca todas as ferramentas disponíveis na aplicação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ferramentas encontradas com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    @GetMapping("/get-all")
    public ResponseEntity<List<ApplicationToolDto>> getTools() {
        return ResponseEntity.ok(toolService.getAllTools());
    }
}
