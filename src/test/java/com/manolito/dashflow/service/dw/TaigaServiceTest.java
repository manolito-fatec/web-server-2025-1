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
    void testAuthenticateTaiga() {
        String username = "gabguska";
        String password = "aluno123";
//        String authToken = taigaService.authenticateTaiga(username, password);
//        assertNotNull(authToken, "O token de autenticação não deve ser nulo");
    }

    @Test
    void testFetchTaigaDataWithMocks() {
        when(taigaService.handleProjects()).thenReturn(mockDataset());

        Dataset<Row> projects = taigaService.handleProjects();
        assertNotNull(projects, "A lista de projetos não deve ser nula");

        Dataset<Row> userStories = taigaService.handleUserStories();
        assertNotNull(userStories, "A lista de user stories não deve ser nula");

        Dataset<Row> tasks = taigaService.handleTasks();
        assertNotNull(tasks, "A lista de tasks não deve ser nula");

        Dataset<Row> issues = taigaService.handleIssues();
        assertNotNull(issues, "A lista de issues não deve ser nula");
    }

    private Dataset<Row> mockDataset() {
        return mock(Dataset.class);
    }

    @Test
    void testAutheticateUserThenSaveInDatabase() {
        taigaService.authenticateTaiga("gabguska", "aluno123");
    }
}