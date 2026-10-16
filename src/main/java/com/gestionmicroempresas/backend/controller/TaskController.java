package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.request.CreateTaskRequest;
import com.gestionmicroempresas.backend.dto.request.MoveTaskRequest;
import com.gestionmicroempresas.backend.dto.request.TaskFeedbackRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateTaskRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateTaskStatusRequest;

import com.gestionmicroempresas.backend.dto.response.TaskEvidenceResponse;
import com.gestionmicroempresas.backend.dto.response.TaskResponse;
import com.gestionmicroempresas.backend.dto.response.TasksResponse;

import com.gestionmicroempresas.backend.service.TaskService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tareas")
public class TaskController {

    private final TaskService taskService;


    public TaskController(
            TaskService taskService
    ) {

        this.taskService =
                taskService;
    }


    // =====================================================
    // GET TASKS
    // =====================================================

    @GetMapping
    public ResponseEntity<TasksResponse> getTasks(

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestParam(
                    name = "alcance",
                    defaultValue = "TODAS"
            )
            String scope,

            @RequestParam(
                    name = "usuarioId",
                    required = false
            )
            Long userId,

            @RequestParam(
                    name = "estado",
                    required = false
            )
            String status
    ) {

        return ResponseEntity.ok(

                taskService.getTasks(

                        getUserId(jwt),

                        scope,

                        userId,

                        status
                )
        );
    }


    // =====================================================
    // GET TASK
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id
    ) {

        return ResponseEntity.ok(

                taskService.getTask(

                        getUserId(jwt),

                        id
                )
        );
    }


    // =====================================================
    // CREATE
    // =====================================================

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(

            @AuthenticationPrincipal
            Jwt jwt,

            @Valid
            @RequestBody
            CreateTaskRequest request
    ) {

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(

                        taskService.createTask(

                                getUserId(jwt),

                                request
                        )
                );
    }


    // =====================================================
    // UPDATE
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateTaskRequest request
    ) {

        return ResponseEntity.ok(

                taskService.updateTask(

                        getUserId(jwt),

                        id,

                        request
                )
        );
    }


    // =====================================================
    // DELETE
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id
    ) {

        taskService.deleteTask(

                getUserId(jwt),

                id
        );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // STATUS
    // =====================================================

    @PatchMapping("/{id}/estado")
    public ResponseEntity<TaskResponse> changeStatus(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateTaskStatusRequest request
    ) {

        return ResponseEntity.ok(

                taskService.changeStatus(

                        getUserId(jwt),

                        id,

                        request
                )
        );
    }


    // =====================================================
    // MOVE
    // =====================================================

    @PatchMapping("/{id}/mover")
    public ResponseEntity<TaskResponse> moveTask(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            MoveTaskRequest request
    ) {

        return ResponseEntity.ok(

                taskService.moveTask(

                        getUserId(jwt),

                        id,

                        request
                )
        );
    }


    // =====================================================
    // FEEDBACK
    // =====================================================

    @PostMapping("/{id}/retroalimentacion")
    public ResponseEntity<TaskResponse> giveFeedback(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            TaskFeedbackRequest request
    ) {

        return ResponseEntity.ok(

                taskService.giveFeedback(

                        getUserId(jwt),

                        id,

                        request
                )
        );
    }


    // =====================================================
    // GET EVIDENCE
    // =====================================================

    @GetMapping("/{id}/evidencias")
    public ResponseEntity<List<TaskEvidenceResponse>> getEvidence(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id
    ) {

        return ResponseEntity.ok(

                taskService.getEvidence(

                        getUserId(jwt),

                        id
                )
        );
    }


    // =====================================================
    // UPLOAD EVIDENCE
    // =====================================================

    @PostMapping(
            value = "/{id}/evidencias",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<List<TaskEvidenceResponse>> uploadEvidence(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @RequestPart("archivos")
            List<MultipartFile> files
    ) {

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(

                        taskService.uploadEvidence(

                                getUserId(jwt),

                                id,

                                files
                        )
                );
    }


    // =====================================================
    // DELETE EVIDENCE
    // =====================================================

    @DeleteMapping(
            "/{id}/evidencias/{evidenciaId}"
    )
    public ResponseEntity<Void> deleteEvidence(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @PathVariable(
                    name = "evidenciaId"
            )
            Long evidenceId
    ) {

        taskService.deleteEvidence(

                getUserId(jwt),

                id,

                evidenceId
        );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // JWT USER ID
    // =====================================================

    private Long getUserId(
            Jwt jwt
    ) {

        Number userId =
                jwt.getClaim(
                        "uid"
                );


        if (userId == null) {

            throw new IllegalStateException(
                    "El token no contiene el identificador del usuario."
            );
        }


        return userId.longValue();
    }
}