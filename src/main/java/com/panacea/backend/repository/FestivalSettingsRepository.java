package com.panacea.backend.repository;

import com.panacea.backend.Entity.FestivalSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalSettingsRepository
        extends JpaRepository<FestivalSettings, Long> {
}