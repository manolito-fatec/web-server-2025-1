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

    public Optional<Double> getAveregeTimeCard(Integer userId) {
        String sql = "SELECT AVG(completed.date_date - started.date_date) AS avarege_time " +
                "FROM dw_tasks.fact_tasks ft " +
                "JOIN dw_tasks.dates started ON ft.started_at = started.date_id " +
                "JOIN dw_tasks.dates completed ON ft.completed_at = completed.date_id " +
                "WHERE ft.completed_at IS NOT NULL " +
                "AND u.user_id = :userId";

        Map<String, Object> params = new HashMap<>();
        try {
            Double result = jdbcTemplate.queryForObject(sql, params, Double.class);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
