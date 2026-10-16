package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.request.CreateTaskRequest;
import com.gestionmicroempresas.backend.dto.request.MoveTaskRequest;
import com.gestionmicroempresas.backend.dto.request.TaskFeedbackRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateTaskRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateTaskStatusRequest;

import com.gestionmicroempresas.backend.dto.response.TaskEvidenceResponse;
import com.gestionmicroempresas.backend.dto.response.TaskPermissionsResponse;
import com.gestionmicroempresas.backend.dto.response.TaskResponse;
import com.gestionmicroempresas.backend.dto.response.TasksPagePermissionsResponse;
import com.gestionmicroempresas.backend.dto.response.TasksResponse;
import com.gestionmicroempresas.backend.dto.response.UserOptionResponse;

import com.gestionmicroempresas.backend.entity.Task;
import com.gestionmicroempresas.backend.entity.TaskEvidence;
import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.enums.Role;
import com.gestionmicroempresas.backend.enums.TaskPriority;
import com.gestionmicroempresas.backend.enums.TaskScope;
import com.gestionmicroempresas.backend.enums.TaskStatus;

import com.gestionmicroempresas.backend.exception.ConflictException;
import com.gestionmicroempresas.backend.exception.ResourceNotFoundException;

import com.gestionmicroempresas.backend.repository.TaskEvidenceRepository;
import com.gestionmicroempresas.backend.repository.TaskRepository;
import com.gestionmicroempresas.backend.repository.UserRepository;

import com.gestionmicroempresas.backend.storage.TaskEvidenceStorageService;
import com.gestionmicroempresas.backend.storage.TaskEvidenceStorageService.StoredEvidence;

import jakarta.persistence.criteria.JoinType;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final TaskEvidenceRepository evidenceRepository;

    private final UserRepository userRepository;

    private final TaskEvidenceStorageService storageService;

    private final CurrentTenantService currentTenantService;


    public TaskService(
            TaskRepository taskRepository,
            TaskEvidenceRepository evidenceRepository,
            UserRepository userRepository,
            TaskEvidenceStorageService storageService,
            CurrentTenantService currentTenantService
    ) {

        this.taskRepository =
                taskRepository;

        this.evidenceRepository =
                evidenceRepository;

        this.userRepository =
                userRepository;

        this.storageService =
                storageService;

        this.currentTenantService =
                currentTenantService;
    }


    // =====================================================
    // GET TASKS
    // =====================================================

    @Transactional(readOnly = true)
    public TasksResponse getTasks(
            Long currentUserId,
            String scopeValue,
            Long userId,
            String statusValue
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        TaskScope scope =
                TaskScope.fromApiValue(
                        scopeValue
                );


        TaskStatus status =
                (
                        statusValue == null
                                ||
                                statusValue.isBlank()
                )
                        ?
                        null
                        :
                        TaskStatus.fromApiValue(
                                statusValue
                        );


        Specification<Task> specification =
                buildSpecification(
                        currentUser,
                        scope,
                        userId,
                        status
                );


        List<Task> tasks =
                taskRepository.findAll(
                        specification,

                        Sort
                                .by(
                                        Sort.Direction.ASC,
                                        "dueAt"
                                )
                                .and(
                                        Sort.by(
                                                Sort.Direction.DESC,
                                                "createdAt"
                                        )
                                )
                );


        List<TaskResponse> responses =
                tasks
                        .stream()
                        .map(
                                task ->
                                        buildTaskResponse(
                                                currentUser,
                                                task
                                        )
                        )
                        .toList();


        return new TasksResponse(
                responses,

                new TasksPagePermissionsResponse(
                        isManager(
                                currentUser
                        )
                )
        );
    }


    // =====================================================
    // GET TASK
    // =====================================================

    @Transactional(readOnly = true)
    public TaskResponse getTask(
            Long currentUserId,
            Long taskId
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Task task =
                getTaskEntity(
                        taskId
                );


        validateCanView(
                currentUser,
                task
        );


        return buildTaskResponse(
                currentUser,
                task
        );
    }


    // =====================================================
    // CREATE TASK
    // =====================================================

    @Transactional
    public TaskResponse createTask(
            Long currentUserId,
            CreateTaskRequest request
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        validateManager(
                currentUser
        );


        TaskPriority priority =
                TaskPriority.fromApiValue(
                        request.priority()
                );


        List<User> assignees =
                getUsers(
                        request.assigneeIds()
                );


        Task task =
                new Task(
                        request
                                .title()
                                .trim(),

                        normalizeDescription(
                                request.description()
                        ),

                        request.dueAt(),

                        priority,

                        currentUser
                );


        task.replaceAssignees(
                assignees
        );


        Task savedTask =
                taskRepository.saveAndFlush(
                        task
                );


        return buildTaskResponse(
                currentUser,
                savedTask
        );
    }


    // =====================================================
    // UPDATE TASK
    // =====================================================

    @Transactional
    public TaskResponse updateTask(
            Long currentUserId,
            Long taskId,
            UpdateTaskRequest request
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Task task =
                getTaskEntity(
                        taskId
                );


        if (
                !canEdit(
                        currentUser,
                        task
                )
        ) {

            throw new AccessDeniedException(
                    "No tienes permisos para editar esta tarea."
            );
        }


        List<User> assignees =
                getUsers(
                        request.assigneeIds()
                );


        task.setTitle(
                request
                        .title()
                        .trim()
        );


        task.setDescription(
                normalizeDescription(
                        request.description()
                )
        );


        task.setDueAt(
                request.dueAt()
        );


        task.setPriority(
                TaskPriority.fromApiValue(
                        request.priority()
                )
        );


        task.replaceAssignees(
                assignees
        );


        Task updatedTask =
                taskRepository.saveAndFlush(
                        task
                );


        return buildTaskResponse(
                currentUser,
                updatedTask
        );
    }


    // =====================================================
    // DELETE TASK
    // =====================================================

    @Transactional
    public void deleteTask(
            Long currentUserId,
            Long taskId
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Task task =
                getTaskEntity(
                        taskId
                );


        if (
                !canDelete(
                        currentUser,
                        task
                )
        ) {

            throw new AccessDeniedException(
                    "No tienes permisos para eliminar esta tarea."
            );
        }


        List<TaskEvidence> evidences =
                evidenceRepository
                        .findAllByTenant_IdAndTask_IdOrderByCreatedAtDescIdDesc(
                                currentUser.getTenantId(),
                                taskId
                        );


        List<String> files =
                evidences
                        .stream()
                        .map(
                                TaskEvidence::getFileName
                        )
                        .toList();


        taskRepository.delete(
                task
        );


        taskRepository.flush();


        for (String fileName : files) {

            try {

                storageService.delete(
                        fileName
                );

            } catch (RuntimeException ignored) {
            }
        }
    }


    // =====================================================
    // CHANGE STATUS
    // =====================================================

    @Transactional
    public TaskResponse changeStatus(
            Long currentUserId,
            Long taskId,
            UpdateTaskStatusRequest request
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Task task =
                getTaskEntity(
                        taskId
                );


        validateCanView(
                currentUser,
                task
        );


        TaskStatus newStatus =
                TaskStatus.fromApiValue(
                        request.status()
                );


        applyStatusChange(
                currentUser,
                task,
                newStatus
        );


        Task updatedTask =
                taskRepository.saveAndFlush(
                        task
                );


        return buildTaskResponse(
                currentUser,
                updatedTask
        );
    }


    // =====================================================
    // MOVE TASK
    // =====================================================

    @Transactional
    public TaskResponse moveTask(
            Long currentUserId,
            Long taskId,
            MoveTaskRequest request
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Task task =
                getTaskEntity(
                        taskId
                );


        validateCanView(
                currentUser,
                task
        );


        TaskPriority newPriority =
                TaskPriority.fromApiValue(
                        request.priority()
                );


        TaskStatus newStatus =
                TaskStatus.fromApiValue(
                        request.status()
                );


        boolean priorityChanged =
                task.getPriority()
                        !=
                        newPriority;


        boolean statusChanged =
                task.getStatus()
                        !=
                        newStatus;


        if (
                priorityChanged
                        &&
                        !canChangePriority(
                                currentUser,
                                task
                        )
        ) {

            throw new AccessDeniedException(
                    "No tienes permisos para modificar la prioridad de esta tarea."
            );
        }


        if (statusChanged) {

            applyStatusChange(
                    currentUser,
                    task,
                    newStatus
            );
        }


        if (priorityChanged) {

            task.setPriority(
                    newPriority
            );
        }


        Task updatedTask =
                taskRepository.saveAndFlush(
                        task
                );


        return buildTaskResponse(
                currentUser,
                updatedTask
        );
    }


    // =====================================================
    // FEEDBACK
    // =====================================================

    @Transactional
    public TaskResponse giveFeedback(
            Long currentUserId,
            Long taskId,
            TaskFeedbackRequest request
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Task task =
                getTaskEntity(
                        taskId
                );


        validateCanView(
                currentUser,
                task
        );


        if (
                task.getStatus()
                        !=
                        TaskStatus.REVIEW
        ) {

            throw new ConflictException(
                    "Solo puede retroalimentarse una tarea que está en revisión."
            );
        }


        if (
                !canReview(
                        currentUser,
                        task
                )
        ) {

            throw new AccessDeniedException(
                    "No tienes permisos para retroalimentar esta tarea."
            );
        }


        task.setFeedback(
                request
                        .comment()
                        .trim()
        );


        task.setStatus(
                TaskStatus.PENDING
        );


        task.setCompletedAt(
                null
        );


        Task updatedTask =
                taskRepository.saveAndFlush(
                        task
                );


        return buildTaskResponse(
                currentUser,
                updatedTask
        );
    }


    // =====================================================
    // GET EVIDENCE
    // =====================================================

    @Transactional(readOnly = true)
    public List<TaskEvidenceResponse> getEvidence(
            Long currentUserId,
            Long taskId
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Task task =
                getTaskEntity(
                        taskId
                );


        validateCanView(
                currentUser,
                task
        );


        return evidenceRepository
                .findAllByTenant_IdAndTask_IdOrderByCreatedAtDescIdDesc(
                        currentUser.getTenantId(),
                        taskId
                )
                .stream()
                .map(
                        evidence ->
                                buildEvidenceResponse(
                                        currentUser,
                                        task,
                                        evidence
                                )
                )
                .toList();
    }


    // =====================================================
    // UPLOAD EVIDENCE
    // =====================================================

    @Transactional
    public List<TaskEvidenceResponse> uploadEvidence(
            Long currentUserId,
            Long taskId,
            List<MultipartFile> files
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Task task =
                getTaskEntity(
                        taskId
                );


        validateCanView(
                currentUser,
                task
        );


        if (
                !canUploadEvidence(
                        currentUser,
                        task
                )
        ) {

            throw new AccessDeniedException(
                    "No tienes permisos para subir evidencias a esta tarea."
            );
        }


        if (
                files == null
                        ||
                        files.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Selecciona al menos una evidencia."
            );
        }


        List<String> storedFiles =
                new ArrayList<>();


        try {

            List<TaskEvidence> evidences =
                    new ArrayList<>();


            for (MultipartFile file : files) {

                StoredEvidence stored =
                        storageService.store(
                                file
                        );


                storedFiles.add(
                        stored.fileName()
                );


                evidences.add(
                        new TaskEvidence(
                                task,
                                currentUser,
                                stored.fileName(),
                                stored.originalName(),
                                stored.mimeType(),
                                stored.sizeBytes()
                        )
                );
            }


            List<TaskEvidence> saved =
                    evidenceRepository.saveAll(
                            evidences
                    );


            evidenceRepository.flush();


            return saved
                    .stream()
                    .map(
                            evidence ->
                                    buildEvidenceResponse(
                                            currentUser,
                                            task,
                                            evidence
                                    )
                    )
                    .toList();

        } catch (RuntimeException exception) {

            for (String fileName : storedFiles) {

                try {

                    storageService.delete(
                            fileName
                    );

                } catch (RuntimeException ignored) {
                }
            }


            throw exception;
        }
    }


    // =====================================================
    // DELETE EVIDENCE
    // =====================================================

    @Transactional
    public void deleteEvidence(
            Long currentUserId,
            Long taskId,
            Long evidenceId
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Task task =
                getTaskEntity(
                        taskId
                );


        validateCanView(
                currentUser,
                task
        );


        TaskEvidence evidence =
                evidenceRepository
                        .findByIdAndTenant_IdAndTask_Id(
                                evidenceId,
                                currentUser.getTenantId(),
                                taskId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "La evidencia solicitada no existe."
                                        )
                        );


        if (
                !canDeleteEvidence(
                        currentUser,
                        task,
                        evidence
                )
        ) {

            throw new AccessDeniedException(
                    "No tienes permisos para eliminar esta evidencia."
            );
        }


        String fileName =
                evidence.getFileName();


        evidenceRepository.delete(
                evidence
        );


        evidenceRepository.flush();


        try {

            storageService.delete(
                    fileName
            );

        } catch (RuntimeException ignored) {
        }
    }


    // =====================================================
    // FILE ACCESS
    // =====================================================

    @Transactional(readOnly = true)
    public void validateEvidenceFileAccess(
            Long currentUserId,
            String fileName
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        if (
                fileName == null
                        ||
                        fileName.isBlank()
        ) {

            throw new ResourceNotFoundException(
                    "La evidencia solicitada no existe."
            );
        }


        TaskEvidence evidence =
                evidenceRepository
                        .findByTenant_IdAndFileName(
                                currentUser.getTenantId(),
                                fileName
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "La evidencia solicitada no existe."
                                        )
                        );


        validateCanView(
                currentUser,
                evidence.getTask()
        );
    }


    // =====================================================
    // SPECIFICATION
    // =====================================================

    private Specification<Task> buildSpecification(
            User currentUser,
            TaskScope scope,
            Long requestedUserId,
            TaskStatus status
    ) {

        Long tenantId =
                currentUser.getTenantId();


        Specification<Task> specification =
                (
                        root,
                        query,
                        criteriaBuilder
                ) ->

                        criteriaBuilder.equal(
                                root
                                        .get("tenant")
                                        .get("id"),

                                tenantId
                        );


        switch (scope) {

            case ALL -> {

                if (
                        !isManager(
                                currentUser
                        )
                ) {

                    specification =
                            specification.and(
                                    (
                                            root,
                                            query,
                                            criteriaBuilder
                                    ) -> {

                                        if (query != null) {
                                            query.distinct(true);
                                        }


                                        var assignees =
                                                root.join(
                                                        "assignees",
                                                        JoinType.LEFT
                                                );


                                        return criteriaBuilder.or(
                                                criteriaBuilder.equal(
                                                        root
                                                                .get("creator")
                                                                .get("id"),

                                                        currentUser.getId()
                                                ),

                                                criteriaBuilder.equal(
                                                        assignees.get("id"),
                                                        currentUser.getId()
                                                )
                                        );
                                    }
                            );
                }
            }


            case MY_TASKS ->

                    specification =
                            specification.and(
                                    (
                                            root,
                                            query,
                                            criteriaBuilder
                                    ) -> {

                                        if (query != null) {
                                            query.distinct(true);
                                        }


                                        return criteriaBuilder.equal(
                                                root
                                                        .join("assignees")
                                                        .get("id"),

                                                currentUser.getId()
                                        );
                                    }
                            );


            case ASSIGNED_BY_ME ->

                    specification =
                            specification.and(
                                    (
                                            root,
                                            query,
                                            criteriaBuilder
                                    ) ->

                                            criteriaBuilder.equal(
                                                    root
                                                            .get("creator")
                                                            .get("id"),

                                                    currentUser.getId()
                                            )
                            );


            case USER -> {

                if (
                        !isManager(
                                currentUser
                        )
                ) {

                    throw new AccessDeniedException(
                            "No tienes permisos para consultar las tareas de otros usuarios."
                    );
                }


                if (requestedUserId == null) {

                    throw new IllegalArgumentException(
                            "Selecciona el usuario que deseas consultar."
                    );
                }


                if (
                        !userRepository
                                .existsByIdAndTenant_Id(
                                        requestedUserId,
                                        tenantId
                                )
                ) {

                    throw new ResourceNotFoundException(
                            "El usuario seleccionado no existe."
                    );
                }


                specification =
                        specification.and(
                                (
                                        root,
                                        query,
                                        criteriaBuilder
                                ) -> {

                                    if (query != null) {
                                        query.distinct(true);
                                    }


                                    return criteriaBuilder.equal(
                                            root
                                                    .join("assignees")
                                                    .get("id"),

                                            requestedUserId
                                    );
                                }
                        );
            }
        }


        if (status != null) {

            specification =
                    specification.and(
                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder.equal(
                                            root.get("status"),
                                            status
                                    )
                    );
        }


        return specification;
    }


    // =====================================================
    // STATUS WORKFLOW
    // =====================================================

    private void applyStatusChange(
            User currentUser,
            Task task,
            TaskStatus newStatus
    ) {

        if (
                task.getStatus()
                        ==
                        newStatus
        ) {

            return;
        }


        List<TaskStatus> allowedStatuses =
                getAllowedStatuses(
                        currentUser,
                        task
                );


        if (
                !allowedStatuses.contains(
                        newStatus
                )
        ) {

            throw new ConflictException(
                    "El cambio de estado solicitado no está permitido."
            );
        }


        task.setStatus(
                newStatus
        );


        if (
                newStatus ==
                        TaskStatus.REVIEW
        ) {

            task.setReviewedAt(
                    OffsetDateTime.now(
                            ZoneOffset.UTC
                    )
            );
        }


        if (
                newStatus ==
                        TaskStatus.COMPLETED
        ) {

            task.setCompletedAt(
                    OffsetDateTime.now(
                            ZoneOffset.UTC
                    )
            );

        } else {

            task.setCompletedAt(
                    null
            );
        }
    }


    private List<TaskStatus> getAllowedStatuses(
            User currentUser,
            Task task
    ) {

        List<TaskStatus> allowed =
                new ArrayList<>();


        boolean executor =
                isManager(
                        currentUser
                )
                        ||
                        isAssigned(
                                currentUser,
                                task
                        );


        boolean reviewer =
                canReview(
                        currentUser,
                        task
                );


        switch (task.getStatus()) {

            case PENDING -> {

                if (executor) {

                    allowed.add(
                            TaskStatus.IN_PROGRESS
                    );
                }
            }


            case IN_PROGRESS -> {

                if (executor) {

                    allowed.add(
                            TaskStatus.PENDING
                    );

                    allowed.add(
                            TaskStatus.REVIEW
                    );
                }
            }


            case REVIEW -> {

                if (reviewer) {

                    allowed.add(
                            TaskStatus.COMPLETED
                    );
                }
            }


            case COMPLETED -> {
            }
        }


        return allowed;
    }


    // =====================================================
    // RESPONSE
    // =====================================================

    private TaskResponse buildTaskResponse(
            User currentUser,
            Task task
    ) {

        List<UserOptionResponse> assignees =
                task
                        .getAssignees()
                        .stream()
                        .sorted(
                                (first, second) ->
                                        first
                                                .getName()
                                                .compareToIgnoreCase(
                                                        second.getName()
                                                )
                        )
                        .map(
                                UserOptionResponse::from
                        )
                        .toList();


        List<String> allowedStatuses =
                getAllowedStatuses(
                        currentUser,
                        task
                )
                        .stream()
                        .map(
                                TaskStatus::getApiValue
                        )
                        .toList();


        TaskPermissionsResponse permissions =
                new TaskPermissionsResponse(
                        canEdit(
                                currentUser,
                                task
                        ),

                        canDelete(
                                currentUser,
                                task
                        ),

                        canChangePriority(
                                currentUser,
                                task
                        ),

                        canUploadEvidence(
                                currentUser,
                                task
                        ),

                        canGiveFeedback(
                                currentUser,
                                task
                        ),

                        allowedStatuses
                );


        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueAt(),

                task
                        .getPriority()
                        .getApiValue(),

                task
                        .getStatus()
                        .getApiValue(),

                UserOptionResponse.from(
                        task.getCreator()
                ),

                assignees,

                task.getFeedback(),

                task.getReviewedAt(),

                task.getCompletedAt(),

                task.getCreatedAt(),

                task.getUpdatedAt(),

                permissions
        );
    }


    private TaskEvidenceResponse buildEvidenceResponse(
            User currentUser,
            Task task,
            TaskEvidence evidence
    ) {

        return new TaskEvidenceResponse(
                evidence.getId(),

                storageService.getPublicUrl(
                        evidence.getFileName()
                ),

                evidence.getOriginalName(),

                evidence.getMimeType(),

                evidence.getSizeBytes(),

                UserOptionResponse.from(
                        evidence.getAuthor()
                ),

                canDeleteEvidence(
                        currentUser,
                        task,
                        evidence
                ),

                evidence.getCreatedAt()
        );
    }


    // =====================================================
    // PERMISSIONS
    // =====================================================

    private boolean isManager(
            User user
    ) {

        return user.getRole()
                ==
                Role.ADMIN

                ||

                user.getRole()
                        ==
                        Role.EDITOR;
    }


    private boolean isCreator(
            User user,
            Task task
    ) {

        return task
                .getCreator()
                .getId()
                .equals(
                        user.getId()
                );
    }


    private boolean isAssigned(
            User user,
            Task task
    ) {

        return task
                .getAssignees()
                .stream()
                .anyMatch(
                        assignee ->
                                assignee
                                        .getId()
                                        .equals(
                                                user.getId()
                                        )
                );
    }


    private boolean canEdit(
            User user,
            Task task
    ) {

        return task.getStatus()
                !=
                TaskStatus.COMPLETED

                &&

                (
                        isManager(user)
                                ||
                                isCreator(
                                        user,
                                        task
                                )
                );
    }


    private boolean canDelete(
            User user,
            Task task
    ) {

        return isManager(user)
                ||
                isCreator(
                        user,
                        task
                );
    }


    private boolean canChangePriority(
            User user,
            Task task
    ) {

        return task.getStatus()
                !=
                TaskStatus.COMPLETED

                &&

                (
                        isManager(user)
                                ||
                                isCreator(
                                        user,
                                        task
                                )
                );
    }


    private boolean canReview(
            User user,
            Task task
    ) {

        return isManager(user)
                ||
                isCreator(
                        user,
                        task
                );
    }


    private boolean canGiveFeedback(
            User user,
            Task task
    ) {

        return task.getStatus()
                ==
                TaskStatus.REVIEW

                &&

                canReview(
                        user,
                        task
                );
    }


    private boolean canUploadEvidence(
            User user,
            Task task
    ) {

        return task.getStatus()
                !=
                TaskStatus.COMPLETED

                &&

                (
                        isManager(user)
                                ||
                                isAssigned(
                                        user,
                                        task
                                )
                );
    }


    private boolean canDeleteEvidence(
            User user,
            Task task,
            TaskEvidence evidence
    ) {

        if (
                task.getStatus()
                        ==
                        TaskStatus.COMPLETED
        ) {

            return false;
        }


        return isManager(user)
                ||
                evidence
                        .getAuthor()
                        .getId()
                        .equals(
                                user.getId()
                        );
    }


    // =====================================================
    // ACCESS VALIDATION
    // =====================================================

    private void validateManager(
            User user
    ) {

        if (
                !isManager(
                        user
                )
        ) {

            throw new AccessDeniedException(
                    "No tienes permisos para crear tareas."
            );
        }
    }


    private void validateCanView(
            User user,
            Task task
    ) {

        if (
                !user
                        .getTenantId()
                        .equals(
                                task.getTenantId()
                        )
        ) {

            throw new ResourceNotFoundException(
                    "La tarea solicitada no existe."
            );
        }


        if (
                isManager(user)
                        ||
                        isCreator(
                                user,
                                task
                        )
                        ||
                        isAssigned(
                                user,
                                task
                        )
        ) {

            return;
        }


        throw new AccessDeniedException(
                "No tienes permisos para consultar esta tarea."
        );
    }


    // =====================================================
    // ENTITIES
    // =====================================================

    private Task getTaskEntity(
            Long taskId
    ) {

        return taskRepository
                .findByIdAndTenant_Id(
                        taskId,

                        currentTenantService
                                .getCurrentTenantId()
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "La tarea solicitada no existe."
                                )
                );
    }


    private User getCurrentUser(
            Long userId
    ) {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        return userRepository
                .findByIdAndTenant_Id(
                        userId,
                        tenantId
                )
                .orElseThrow(
                        () ->
                                new AccessDeniedException(
                                        "La sesión no corresponde a un usuario válido."
                                )
                );
    }


    private List<User> getUsers(
            List<Long> ids
    ) {

        if (
                ids == null
                        ||
                        ids.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Selecciona al menos una persona."
            );
        }


        Set<Long> uniqueIds =
                new LinkedHashSet<>(
                        ids
                );


        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        List<User> users =
                userRepository
                        .findAllByTenant_IdAndIdIn(
                                tenantId,
                                uniqueIds
                        );


        if (
                users.size()
                        !=
                        uniqueIds.size()
        ) {

            throw new ResourceNotFoundException(
                    "Uno o más usuarios asignados no existen."
            );
        }


        return users;
    }


    // =====================================================
    // NORMALIZATION
    // =====================================================

    private String normalizeDescription(
            String description
    ) {

        if (
                description == null
                        ||
                        description.isBlank()
        ) {

            return null;
        }


        return description.trim();
    }
}