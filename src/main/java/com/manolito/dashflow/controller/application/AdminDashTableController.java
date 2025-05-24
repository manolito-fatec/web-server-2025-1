package com.manolito.dashflow.controller.application;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manolito.dashflow.component.ExportCsvComponent;
import com.manolito.dashflow.dto.application.TableAdminDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "DashBoard admin", description = "controleer para a tela de dash do admin")
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/admin/dash")
public class AdminDashTableController
{
    @Autowired
    private ExportCsvComponent exportCsvComponent;
    private static final String INTERNAL_SERVER_ERROR  = "Internal Server Error ";
    @GetMapping("/table")
    @Operation(summary = "tabela de dados do dashboard do Admin", description = "Busca a lista de dados da tabela na tela de admin ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Envio de dados para tabela administrador concluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<?> generateDataTable() {
        try {
            List<TableAdminDto> listDataTable = exportCsvComponent.generateDataTable();
            return ResponseEntity.status(HttpStatus.OK).body(listDataTable);
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(INTERNAL_SERVER_ERROR + runtimeException.getMessage());
        }
    }
}
