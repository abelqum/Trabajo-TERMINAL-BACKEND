package com.gestionmicroempresas.backend.repository;

import com.gestionmicroempresas.backend.entity.Task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TaskRepository
        extends JpaRepository<Task, Long>,
        JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndTenant_Id(
            Long id,
            Long tenantId
    );


    List<Task> findAllByTenant_IdOrderByIdAsc(
            Long tenantId
    );
}