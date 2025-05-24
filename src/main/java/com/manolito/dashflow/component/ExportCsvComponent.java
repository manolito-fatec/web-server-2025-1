package com.manolito.dashflow.component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.manolito.dashflow.dto.application.ExportCsvAdminDto;
import com.manolito.dashflow.dto.application.ExportCsvManagerDto;
import com.manolito.dashflow.dto.application.TableAdminDto;
import com.manolito.dashflow.repository.application.ExportCsvRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExportCsvComponent {

    private final ExportCsvRepository exportRepository;
    private static final String CSV_HEADER_ADMIN = "Project,Manager,QuantityOfOperators,QuantityOfCards\n";
    private static final String CSV_HEADER_MANAGER = "Operator, Project, QuantityOfCards\n";

    /**
     * Generates a CSV string from a list of {@link ExportCsvDto} objects.
     * Each row in the resulting CSV represents one {@code ExportCsvDto}, with columns
     * for project, manager, quantity of operators, and quantity of cards. The method
     * also prepends a header row defined by the {@code CSV_HEADER} constant.
     * 
     * @param csvRows a list of {@code ExportCsvDto} objects representing the data rows
     * @return a {@code String} containing the generated CSV content
     */
    public String generateCsvAdmin()
    {
        StringBuilder csvManagerContent = new StringBuilder();
        csvManagerContent.append(CSV_HEADER_ADMIN);
        List<ExportCsvAdminDto> csvRows = exportRepository.getAllCurrentManagerAndProjectAndQuantityOfOperatorsAndQuantityOfCard();
        if(!csvRows.isEmpty())
        for (ExportCsvAdminDto rows : csvRows)
        {
            csvManagerContent.append(rows.getProject() == null ? "" : rows.getProject() ).append(",")
                      .append(rows.getManager()== null ? "" : rows.getManager()).append(",")
                      .append(rows.getQuantityOfOperators() == null ? "" : rows.getQuantityOfOperators()).append(",")
                      .append(rows.getQuantityOfCards() == null ? "" : rows.getQuantityOfCards()).append("\n");
        }
        return csvManagerContent.toString();
    }

    /**
     * Generates a CSV string from a list of {@link ExportCsvManagerDto} objects.
     * Each row in the resulting CSV represents one {@code ExportCsvManagerDto}, with columns
     * for operator, project, and quantity of cards. The method also prepends a header row
     * defined by the {@code CSV_HEADER_MANAGER} constant.
     *
     * @param csvRows a list of {@code ExportCsvManagerDto} objects representing the data rows
     * @return a {@code String} containing the generated CSV content
     */
    public String generateCsvManager()
    {
        StringBuilder csvManagerContent = new StringBuilder();
        csvManagerContent.append(CSV_HEADER_MANAGER);
        List<ExportCsvManagerDto> csvRows = exportRepository.getAllCurrentOperatorProjectAndQuantityOfCard();
        if (!csvRows.isEmpty())
        {
            for (ExportCsvManagerDto rows : csvRows)
            {
                csvManagerContent.append(rows.getOperator() == null ? "" : rows.getOperator()).append(",")
                        .append(rows.getProject() == null ? "" : rows.getProject()).append(",")
                        .append(rows.getQuantityOfCards() == null ? "" : rows.getQuantityOfCards()).append("\n");
            }
        }
        return csvManagerContent.toString();
    }

    /**
     * Generates a list of administrative data for display in a table.
     *
     * @return a list of {@link TableAdminDto} containing the project name, manager,
     *         and number of operators.
     */
    public List<TableAdminDto> generateDataTable ()
    {
        return exportRepository.getAllCurrentManagerAndProjectAndQuantityOfOperatorsAndQuantityOfCard().stream()
                .map(dt -> new TableAdminDto(dt.getProject(), dt.getManager(), dt.getQuantityOfOperators()))
                .collect(Collectors.toList());
    }
}
