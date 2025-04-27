package com.manolito.dashflow.controller.application;

import com.manolito.dashflow.dto.application.auth.JwtAuthenticationResponseDto;
import com.manolito.dashflow.dto.application.auth.LoginRequestDto;
import com.manolito.dashflow.dto.application.auth.SignupRequestDto;
import com.manolito.dashflow.dto.application.auth.UserExistDto;
import com.manolito.dashflow.service.application.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Cadastro de Usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor no cadastro de usuário.")
    })
    @PostMapping("/signup")
    public ResponseEntity<JwtAuthenticationResponseDto> signup(@RequestBody SignupRequestDto request) {
        JwtAuthenticationResponseDto response = authenticationService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Login de Usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar login.")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponseDto> login(@RequestBody LoginRequestDto request) {
        JwtAuthenticationResponseDto response = authenticationService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @Operation(summary = "Verificar se username de um usuário já está cadastrado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca Realizada."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor na verificação se um usuário já existe.")
    })
    @GetMapping("/get-user/{username}")
    public ResponseEntity<UserExistDto> getUserByUsername(@PathVariable String username) {
        Boolean response = authenticationService.getUserByUsername(username);
        UserExistDto exist = new UserExistDto(response);
        return ResponseEntity.status(HttpStatus.OK).body(exist);
    }
}
