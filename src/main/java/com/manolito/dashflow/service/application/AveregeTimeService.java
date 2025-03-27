package com.manolito.dashflow.service.application;

import com.manolito.dashflow.repository.application.AveregeTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AveregeTimeService {
    private final AveregeTimeRepository averegeTimeRepository;

    public Double getAveregeTimeCard(Integer userId) {
        Optional<Double> averegeTimeCard = averegeTimeRepository.getAveregeTimeCard(userId);
        if (averegeTimeCard.isPresent()) {
            return averegeTimeCard.get();
        }
        throw new NoSuchElementException("No tasks completed");
    }
}
