package com.manolito.dashflow.repository.application;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AveregeTimeRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Optional<Long> getAveregeTimeCard() {
        String sql = "SELECT AVG(completed.date_date - started.date_date) AS avarege_time " +
                "FROM dw_tasks.fact_tasks ft " +
                "JOIN dw_tasks.dates started ON ft.started_at = started.date_id " +
                "JOIN dw_tasks.dates completed ON ft.completed_at = completed.date_id " +
                "WHERE ft.completed_at IS NOT NULL";

        Map<String, Object> params = new HashMap<>();
        try {
            Long result = jdbcTemplate.queryForObject(sql, params, Long.class);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
