package com.manolito.dashflow.service.dw;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class TaigaServiceTest {

    @Autowired
    private TaigaService taigaService;

    @Test
    void testAuthenticateTaiga() {
        String username = "gabguska";
        String password = "aluno123";
        String authToken = taigaService.authenticateTaiga(username, password);
        assertNotNull(authToken, "O token de autenticação não deve ser nulo");
    }
}