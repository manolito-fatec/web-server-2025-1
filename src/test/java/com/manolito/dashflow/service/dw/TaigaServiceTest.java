package com.manolito.dashflow.service.dw;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class TaigaServiceTest {

    @Autowired
    private TaigaService taigaService;
    
    @Test
    void testAutheticateUserThenSaveInDatabase() {
        taigaService.authenticateTaiga("gabguska", "aluno123");
    }
}