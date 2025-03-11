package com.si4lr_http_b.bff.repository;

import com.si4lr_http_b.bff.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long> {
    Optional<Area> findByTaskId(String taskId);
}
