package com.manolito.dashflow.service.application;

import com.manolito.dashflow.repository.application.AverageTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AverageTimeService {
    private final AverageTimeRepository averageTimeRepository;

    public Double getAverageTimeCard(Integer userId) {
        Optional<Double> averageTimeCard = averageTimeRepository.getAverageTimeCard(userId);
        if (averageTimeCard.isPresent()) {
            return averageTimeCard.get();
        }
        throw new NoSuchElementException("No tasks completed");
    }
}
