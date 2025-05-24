package com.manolito.dashflow.controller.application;

import java.util.List;
import java.util.NoSuchElementException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manolito.dashflow.component.ExportCsvComponent;
import com.manolito.dashflow.dto.application.TableAdminDto;

@Tag(name = "Export Controller", description = "Export de arquivo CSV")
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/export")
public class ReportExportController {

    @Autowired
    private ExportCsvComponent exportCsvComponent;

    @GetMapping("/admin/csv")
    @Operation(summary = "Exportação de dados do dashboard do Admin", description = "Realiza a exportação de um arquivo CSV contendo informações relacionadas ao dashBoard do admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exportação de CSV do Administrador concluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar exportar o aquivo.")
    })
    public ResponseEntity<?> generateCsvFileAdmin() {
        try {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "paulinho.csv");
        byte[] csvBytes = exportCsvComponent.generateCsvAdmin().getBytes();
        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }

    @GetMapping("/manager/csv")
    @Operation(summary = "Exportação de dados do dashboard do Gestor", description = "Realiza a exportação de um arquivo CSV contendo informações relacionadas ao dashBoard do Gestor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exportação de CSV do Gestor concluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar exportar o aquivo.")
    })
    public ResponseEntity<?> generateCsvFileManagaer() {
        try {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "paulinho.csv");
        byte[] csvBytes = exportCsvComponent.generateCsvManager().getBytes();
        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }

    @GetMapping("/admin/table")
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error " + runtimeException.getMessage());
        }
    }
}
