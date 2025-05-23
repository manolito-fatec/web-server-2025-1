package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.application.ExportCsvAdminDto;
import com.manolito.dashflow.repository.application.ExportCsvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;


@RequiredArgsConstructor
@Service
public class ExportCsvService {
    private final ExportCsvRepository repository;

    /**
     * Retrieves all project statistics including managers, project details, and operator/card counts.
     * <p>
     * This method fetches comprehensive project data for export, including manager information,
     * project details, and counts of operators and cards across all projects.
     * </p>
     *
     * @return List of {@link ExportCsvAdminDto} containing all project statistics
     * @throws NoSuchElementException if no project data is found in the system
     *
     * @example
     * <pre>{@code
     * // Get all project statistics for CSV export
     * List<ExportCsvAdminDto> stats = exportCsvService.getProjectsCountByUserId();
     * }</pre>
     */
    public List<ExportCsvAdminDto> getProjectsCountByUserId() {
        List<ExportCsvAdminDto> csv = repository.getAllCurrentManagerAndProjectAndQuantityOfOperatorsAndQuantityOfCard();
        if (csv.isEmpty()) {
            throw new NoSuchElementException("No data found for csv export.");
        }
        return csv;
    }
}
