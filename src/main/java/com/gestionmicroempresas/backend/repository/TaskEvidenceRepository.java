package com.gestionmicroempresas.backend.repository;

import com.gestionmicroempresas.backend.entity.TaskEvidence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskEvidenceRepository
        extends JpaRepository<TaskEvidence, Long> {

    List<TaskEvidence>
    findAllByTenant_IdAndTask_IdOrderByCreatedAtDescIdDesc(
            Long tenantId,
            Long taskId
    );


    Optional<TaskEvidence>
    findByIdAndTenant_IdAndTask_Id(
            Long evidenceId,
            Long tenantId,
            Long taskId
    );


    Optional<TaskEvidence>
    findByTenant_IdAndFileName(
            Long tenantId,
            String fileName
    );


    List<TaskEvidence> findAllByTenant_IdOrderByIdAsc(
            Long tenantId
    );
}