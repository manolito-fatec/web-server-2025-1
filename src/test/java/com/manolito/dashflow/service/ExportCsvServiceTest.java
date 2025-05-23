package com.manolito.dashflow.service;

import com.manolito.dashflow.dto.application.ExportCsvAdminDto;
import com.manolito.dashflow.repository.application.ExportCsvRepository;
import com.manolito.dashflow.service.application.ExportCsvService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExportCsvServiceTest {

    @Mock
    private ExportCsvRepository repository;

    @InjectMocks
    private ExportCsvService exportCsvService;

    @BeforeEach
    void setUp() {
        reset(repository);
    }

    private ExportCsvAdminDto createTestDto(String project, String manager, Integer operators, Integer cards) {
        return ExportCsvAdminDto.builder()
                .Project(project)
                .Manager(manager)
                .quantityOfOperators(operators)
                .quantityOfCards(cards)
                .build();
    }

    @Test
    @DisplayName("Should return list of DTOs when repository has data")
    void shouldReturnListWhenRepositoryHasData() {
        List<ExportCsvAdminDto> mockData = List.of(
                createTestDto("Project1", "Manager1", 5, 10),
                createTestDto("Project2", "Manager2", 3, 8)
        );
        when(repository.getAllCurrentManagerAndProjectAndQuantityOfOperatorsAndQuantityOfCard())
                .thenReturn(mockData);

        List<ExportCsvAdminDto> result = exportCsvService.getProjectsCountByUserId();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Project1", result.get(0).getProject());
        assertEquals(Integer.valueOf(5), result.get(0).getQuantityOfOperators());
        verify(repository, times(1))
                .getAllCurrentManagerAndProjectAndQuantityOfOperatorsAndQuantityOfCard();
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when repository returns empty list")
    void shouldThrowWhenRepositoryReturnsEmptyList() {
        when(repository.getAllCurrentManagerAndProjectAndQuantityOfOperatorsAndQuantityOfCard())
                .thenReturn(Collections.emptyList());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> exportCsvService.getProjectsCountByUserId()
        );

        assertEquals("No data found for csv export.", exception.getMessage());
        verify(repository, times(1))
                .getAllCurrentManagerAndProjectAndQuantityOfOperatorsAndQuantityOfCard();
    }

    @Test
    @DisplayName("Should handle null values in DTO fields")
    void shouldHandleNullValuesInDtoFields() {
        List<ExportCsvAdminDto> mockData = List.of(
                createTestDto(null, null, null, null)
        );
        when(repository.getAllCurrentManagerAndProjectAndQuantityOfOperatorsAndQuantityOfCard())
                .thenReturn(mockData);

        List<ExportCsvAdminDto> result = exportCsvService.getProjectsCountByUserId();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getProject());
        assertNull(result.get(0).getManager());
        assertNull(result.get(0).getQuantityOfOperators());
        assertNull(result.get(0).getQuantityOfCards());
    }
}