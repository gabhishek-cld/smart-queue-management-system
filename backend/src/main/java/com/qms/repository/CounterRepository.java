package com.qms.repository;

import com.qms.model.entity.Counter;
import com.qms.model.enums.CounterStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounterRepository extends JpaRepository<Counter, Long> {
    List<Counter> findByStatus(CounterStatus status);
}
