package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.request.CreateTravelRequest;
import com.gestionmicroempresas.backend.dto.request.TravelBudgetRequest;
import com.gestionmicroempresas.backend.dto.request.TravelExpenseItemRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateTravelExpenseStatusRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateTravelRequest;

import com.gestionmicroempresas.backend.dto.response.TravelBudgetResponse;
import com.gestionmicroempresas.backend.dto.response.TravelExpenseResponse;
import com.gestionmicroempresas.backend.dto.response.TravelPermissionsResponse;
import com.gestionmicroempresas.backend.dto.response.TravelResponse;
import com.gestionmicroempresas.backend.dto.response.TravelSummaryResponse;
import com.gestionmicroempresas.backend.dto.response.TravelsPagePermissionsResponse;
import com.gestionmicroempresas.backend.dto.response.TravelsPageResponse;
import com.gestionmicroempresas.backend.dto.response.UserOptionResponse;

import com.gestionmicroempresas.backend.entity.Travel;
import com.gestionmicroempresas.backend.entity.TravelExpense;
import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.enums.Role;
import com.gestionmicroempresas.backend.enums.TravelExpenseCategory;
import com.gestionmicroempresas.backend.enums.TravelExpenseStatus;
import com.gestionmicroempresas.backend.enums.TravelStatus;

import com.gestionmicroempresas.backend.exception.ConflictException;
import com.gestionmicroempresas.backend.exception.ResourceNotFoundException;

import com.gestionmicroempresas.backend.repository.TravelExpenseRepository;
import com.gestionmicroempresas.backend.repository.TravelRepository;
import com.gestionmicroempresas.backend.repository.UserRepository;

import com.gestionmicroempresas.backend.storage.TravelReceiptStorageService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TravelService {

    private static final BigDecimal ZERO =
            new BigDecimal("0.00");


    private final TravelRepository travelRepository;

    private final TravelExpenseRepository travelExpenseRepository;

    private final UserRepository userRepository;

    private final TravelReceiptStorageService storageService;

    private final CurrentTenantService currentTenantService;


    public TravelService(
            TravelRepository travelRepository,
            TravelExpenseRepository travelExpenseRepository,
            UserRepository userRepository,
            TravelReceiptStorageService storageService,
            CurrentTenantService currentTenantService
    ) {

        this.travelRepository =
                travelRepository;

        this.travelExpenseRepository =
                travelExpenseRepository;

        this.userRepository =
                userRepository;

        this.storageService =
                storageService;

        this.currentTenantService =
                currentTenantService;
    }


    // =====================================================
    // GET TRAVELS
    // =====================================================

    @Transactional(readOnly = true)
    public TravelsPageResponse getTravels(
            Long currentUserId,
            int page,
            int size,
            String sort
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Long tenantId =
                currentUser.getTenantId();


        PageRequest pageable =
                PageRequest.of(
                        Math.max(page, 0),

                        Math.min(
                                Math.max(size, 1),
                                100
                        ),

                        resolveSort(sort)
                );


        Page<Travel> result;


        if (isAdministrator(currentUser)) {

            result =
                    travelRepository.findAllByTenant_Id(
                            tenantId,
                            pageable
                    );

        } else {

            result =
                    travelRepository
                            .findDistinctByTenant_IdAndAssignedUsers_Id(
                                    tenantId,
                                    currentUserId,
                                    pageable
                            );
        }


        List<TravelResponse> travels =
                result
                        .getContent()
                        .stream()
                        .map(
                                travel ->
                                        buildTravelResponse(
                                                currentUser,
                                                travel
                                        )
                        )
                        .toList();


        return new TravelsPageResponse(
                travels,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),

                new TravelsPagePermissionsResponse(
                        isAdministrator(
                                currentUser
                        )
                )
        );
    }


    // =====================================================
    // GET TRAVEL
    // =====================================================

    @Transactional(readOnly = true)
    public TravelResponse getTravel(
            Long currentUserId,
            Long travelId
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Travel travel =
                getTravelEntity(
                        travelId
                );


        validateCanViewTravel(
                currentUser,
                travel
        );


        return buildTravelResponse(
                currentUser,
                travel
        );
    }


    // =====================================================
    // CREATE TRAVEL
    // =====================================================

    @Transactional
    public TravelResponse createTravel(
            Long currentUserId,
            CreateTravelRequest request
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        validateAdministrator(
                currentUser
        );


        validateTravelDates(
                request.startDate(),
                request.endDate()
        );


        List<User> assignedUsers =
                getAssignedUsers(
                        request.userIds()
                );


        Travel travel =
                new Travel(
                        currentUser.getTenant(),

                        request
                                .name()
                                .trim(),

                        request
                                .destination()
                                .trim(),

                        request.startDate(),

                        request.endDate()
                );


        applyBudget(
                travel,
                request.budget()
        );


        travel.replaceAssignedUsers(
                assignedUsers
        );


        Travel savedTravel =
                travelRepository.saveAndFlush(
                        travel
                );


        return buildTravelResponse(
                currentUser,
                savedTravel
        );
    }


    // =====================================================
    // UPDATE TRAVEL
    // =====================================================

    @Transactional
    public TravelResponse updateTravel(
            Long currentUserId,
            Long travelId,
            UpdateTravelRequest request
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        validateAdministrator(
                currentUser
        );


        Travel travel =
                getTravelEntity(
                        travelId
                );


        validateActiveTravel(
                travel
        );


        validateTravelDates(
                request.startDate(),
                request.endDate()
        );


        List<TravelExpense> existingExpenses =
                travelExpenseRepository
                        .findAllByTenant_IdAndTravel_IdOrderByExpenseDateDescIdDesc(
                                currentUser.getTenantId(),
                                travelId
                        );


        for (TravelExpense expense : existingExpenses) {

            if (
                    expense
                            .getExpenseDate()
                            .isBefore(
                                    request.startDate()
                            )
                            ||
                            expense
                                    .getExpenseDate()
                                    .isAfter(
                                            request.endDate()
                                    )
            ) {

                throw new ConflictException(
                        "No puedes cambiar las fechas porque existen gastos fuera del nuevo periodo."
                );
            }
        }


        List<User> assignedUsers =
                getAssignedUsers(
                        request.userIds()
                );


        travel.setName(
                request
                        .name()
                        .trim()
        );


        travel.setDestination(
                request
                        .destination()
                        .trim()
        );


        travel.setStartDate(
                request.startDate()
        );


        travel.setEndDate(
                request.endDate()
        );


        travel.replaceAssignedUsers(
                assignedUsers
        );


        applyBudget(
                travel,
                request.budget()
        );


        Travel updatedTravel =
                travelRepository.saveAndFlush(
                        travel
                );


        return buildTravelResponse(
                currentUser,
                updatedTravel
        );
    }


    // =====================================================
    // DELETE TRAVEL
    // =====================================================

    @Transactional
    public void deleteTravel(
            Long currentUserId,
            Long travelId
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        validateAdministrator(
                currentUser
        );


        Travel travel =
                getTravelEntity(
                        travelId
                );


        validateActiveTravel(
                travel
        );


        List<TravelExpense> expenses =
                travelExpenseRepository
                        .findAllByTenant_IdAndTravel_IdOrderByExpenseDateDescIdDesc(
                                currentUser.getTenantId(),
                                travelId
                        );


        List<String> receiptFiles =
                expenses
                        .stream()
                        .map(
                                TravelExpense::getReceiptFileName
                        )
                        .filter(
                                fileName ->
                                        fileName != null
                                                &&
                                                !fileName.isBlank()
                        )
                        .toList();


        travelRepository.delete(
                travel
        );


        travelRepository.flush();


        for (String fileName : receiptFiles) {

            try {

                storageService.deleteReceipt(
                        fileName
                );

            } catch (RuntimeException ignored) {
            }
        }
    }


    // =====================================================
    // DELETE RECEIPTS
    // =====================================================

    @Transactional
    public void deleteReceipts(
            Long currentUserId,
            Long travelId
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        validateAdministrator(
                currentUser
        );


        Travel travel =
                getTravelEntity(
                        travelId
                );


        validateActiveTravel(
                travel
        );


        List<TravelExpense> expenses =
                travelExpenseRepository
                        .findAllByTenant_IdAndTravel_IdOrderByExpenseDateDescIdDesc(
                                currentUser.getTenantId(),
                                travelId
                        );


        for (TravelExpense expense : expenses) {

            String fileName =
                    expense.getReceiptFileName();


            if (
                    fileName == null
                            ||
                            fileName.isBlank()
            ) {

                continue;
            }


            storageService.deleteReceipt(
                    fileName
            );


            expense.setReceiptFileName(
                    null
            );
        }


        travelExpenseRepository.saveAll(
                expenses
        );


        travelExpenseRepository.flush();
    }


    // =====================================================
    // REGISTER EXPENSES
    // =====================================================

    @Transactional
    public TravelResponse registerExpenses(
            Long currentUserId,
            Long travelId,
            List<TravelExpenseItemRequest> requests,
            List<MultipartFile> receipts
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Travel travel =
                getTravelEntity(
                        travelId
                );


        validateCanViewTravel(
                currentUser,
                travel
        );


        validateActiveTravel(
                travel
        );


        if (
                !isAdministrator(currentUser)
                        &&
                        !isAssigned(
                                currentUser,
                                travel
                        )
        ) {

            throw new AccessDeniedException(
                    "No tienes permisos para registrar gastos en este viaje."
            );
        }


        if (
                requests == null
                        ||
                        requests.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Debes registrar al menos un gasto."
            );
        }


        List<MultipartFile> safeReceipts =
                receipts == null
                        ?
                        List.of()
                        :
                        receipts;


        List<PreparedExpense> preparedExpenses =
                new ArrayList<>();


        Set<Integer> usedReceiptIndexes =
                new HashSet<>();


        for (TravelExpenseItemRequest request : requests) {

            TravelExpenseCategory category =
                    TravelExpenseCategory
                            .fromApiValue(
                                    request.category()
                            );


            if (
                    request.description() == null
                            ||
                            request.description().isBlank()
            ) {

                throw new IllegalArgumentException(
                        "La descripción del gasto es obligatoria."
                );
            }


            if (
                    request
                            .description()
                            .trim()
                            .length()
                            > 300
            ) {

                throw new IllegalArgumentException(
                        "La descripción no puede superar los 300 caracteres."
                );
            }


            BigDecimal amount =
                    normalizePositiveMoney(
                            request.amount(),
                            "El monto del gasto debe ser mayor que cero."
                    );


            LocalDate expenseDate =
                    request.expenseDate();


            if (expenseDate == null) {

                throw new IllegalArgumentException(
                        "La fecha del gasto es obligatoria."
                );
            }


            if (
                    expenseDate.isBefore(
                            travel.getStartDate()
                    )
                            ||
                            expenseDate.isAfter(
                                    travel.getEndDate()
                            )
            ) {

                throw new IllegalArgumentException(
                        "La fecha del gasto debe estar dentro del periodo del viaje."
                );
            }


            Integer receiptIndex =
                    request.receiptIndex();


            if (receiptIndex != null) {

                if (
                        receiptIndex < 0
                                ||
                                receiptIndex >= safeReceipts.size()
                ) {

                    throw new IllegalArgumentException(
                            "Uno de los comprobantes no corresponde con el gasto enviado."
                    );
                }


                if (
                        !usedReceiptIndexes.add(
                                receiptIndex
                        )
                ) {

                    throw new IllegalArgumentException(
                            "Un comprobante no puede asignarse a más de un gasto."
                    );
                }
            }


            preparedExpenses.add(
                    new PreparedExpense(
                            category,

                            request
                                    .description()
                                    .trim(),

                            amount,

                            expenseDate,

                            receiptIndex
                    )
            );
        }


        if (
                usedReceiptIndexes.size()
                        !=
                        safeReceipts.size()
        ) {

            throw new IllegalArgumentException(
                    "Se recibieron comprobantes que no están asociados a ningún gasto."
            );
        }


        List<String> storedFiles =
                new ArrayList<>();


        try {

            List<TravelExpense> expenses =
                    new ArrayList<>();


            for (PreparedExpense prepared : preparedExpenses) {

                TravelExpense expense =
                        new TravelExpense(
                                travel,
                                prepared.category(),
                                prepared.description(),
                                prepared.amount(),
                                prepared.expenseDate(),
                                currentUser
                        );


                if (
                        prepared.receiptIndex()
                                != null
                ) {

                    String fileName =
                            storageService.storeReceipt(
                                    safeReceipts.get(
                                            prepared.receiptIndex()
                                    )
                            );


                    storedFiles.add(
                            fileName
                    );


                    expense.setReceiptFileName(
                            fileName
                    );
                }


                expenses.add(
                        expense
                );
            }


            travelExpenseRepository.saveAll(
                    expenses
            );


            travelExpenseRepository.flush();

        } catch (RuntimeException exception) {

            for (String fileName : storedFiles) {

                try {

                    storageService.deleteReceipt(
                            fileName
                    );

                } catch (RuntimeException ignored) {
                }
            }


            throw exception;
        }


        return buildTravelResponse(
                currentUser,
                travel
        );
    }


    // =====================================================
    // DELETE EXPENSE
    // =====================================================

    @Transactional
    public void deleteExpense(
            Long currentUserId,
            Long travelId,
            Long expenseId
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        Travel travel =
                getTravelEntity(
                        travelId
                );


        validateCanViewTravel(
                currentUser,
                travel
        );


        validateActiveTravel(
                travel
        );


        TravelExpense expense =
                getTravelExpense(
                        expenseId,
                        travelId
                );


        if (
                expense.getStatus()
                        ==
                        TravelExpenseStatus.APPROVED
        ) {

            throw new ConflictException(
                    "Un gasto aprobado no puede eliminarse."
            );
        }


        boolean ownExpense =
                expense
                        .getCreatedBy()
                        .getId()
                        .equals(
                                currentUserId
                        );


        if (
                !isAdministrator(currentUser)
                        &&
                        !ownExpense
        ) {

            throw new AccessDeniedException(
                    "No tienes permisos para eliminar este gasto."
            );
        }


        String receiptFileName =
                expense.getReceiptFileName();


        travelExpenseRepository.delete(
                expense
        );


        travelExpenseRepository.flush();


        try {

            storageService.deleteReceipt(
                    receiptFileName
            );

        } catch (RuntimeException ignored) {
        }
    }


    // =====================================================
    // REVIEW EXPENSE
    // =====================================================

    @Transactional
    public TravelExpenseResponse updateExpenseStatus(
            Long currentUserId,
            Long travelId,
            Long expenseId,
            UpdateTravelExpenseStatusRequest request
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        validateAdministrator(
                currentUser
        );


        Travel travel =
                getTravelEntity(
                        travelId
                );


        validateActiveTravel(
                travel
        );


        TravelExpense expense =
                getTravelExpense(
                        expenseId,
                        travelId
                );


        if (
                expense.getStatus()
                        !=
                        TravelExpenseStatus.PENDING
        ) {

            throw new ConflictException(
                    "Solo los gastos pendientes pueden revisarse."
            );
        }


        TravelExpenseStatus newStatus =
                TravelExpenseStatus
                        .fromApiValue(
                                request.status()
                        );


        if (
                newStatus ==
                        TravelExpenseStatus.PENDING
        ) {

            throw new IllegalArgumentException(
                    "La revisión debe aprobar o rechazar el gasto."
            );
        }


        if (
                newStatus ==
                        TravelExpenseStatus.REJECTED
        ) {

            if (
                    request.rejectionReason() == null
                            ||
                            request
                                    .rejectionReason()
                                    .isBlank()
            ) {

                throw new IllegalArgumentException(
                        "Indica el motivo del rechazo."
                );
            }


            expense.setRejectionReason(
                    request
                            .rejectionReason()
                            .trim()
            );

        } else {

            expense.setRejectionReason(
                    null
            );
        }


        expense.setStatus(
                newStatus
        );


        TravelExpense updatedExpense =
                travelExpenseRepository
                        .saveAndFlush(
                                expense
                        );


        return buildExpenseResponse(
                currentUser,
                travel,
                updatedExpense
        );
    }


    // =====================================================
    // FINISH TRAVEL
    // =====================================================

    @Transactional
    public TravelResponse finishTravel(
            Long currentUserId,
            Long travelId
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        validateAdministrator(
                currentUser
        );


        Travel travel =
                getTravelEntity(
                        travelId
                );


        validateActiveTravel(
                travel
        );


        long pendingExpenses =
                travelExpenseRepository
                        .countByTenant_IdAndTravel_IdAndStatus(
                                currentUser.getTenantId(),
                                travelId,
                                TravelExpenseStatus.PENDING
                        );


        if (pendingExpenses > 0) {

            throw new ConflictException(
                    "Todos los gastos deben estar aprobados o rechazados antes de finalizar."
            );
        }


        travel.setStatus(
                TravelStatus.FINISHED
        );


        Travel finishedTravel =
                travelRepository.saveAndFlush(
                        travel
                );


        return buildTravelResponse(
                currentUser,
                finishedTravel
        );
    }


    // =====================================================
    // RECEIPT ACCESS
    // =====================================================

    @Transactional(readOnly = true)
    public void validateReceiptAccess(
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
                    "El comprobante solicitado no existe."
            );
        }


        TravelExpense expense =
                travelExpenseRepository
                        .findByTenant_IdAndReceiptFileName(
                                currentUser.getTenantId(),
                                fileName
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "El comprobante solicitado no existe."
                                        )
                        );


        validateCanViewTravel(
                currentUser,
                expense.getTravel()
        );
    }


    // =====================================================
    // BUILD RESPONSE
    // =====================================================

    private TravelResponse buildTravelResponse(
            User currentUser,
            Travel travel
    ) {

        List<TravelExpense> expenses =
                travelExpenseRepository
                        .findAllByTenant_IdAndTravel_IdOrderByExpenseDateDescIdDesc(
                                currentUser.getTenantId(),
                                travel.getId()
                        );


        BigDecimal approvedSpent =
                ZERO;


        long pendingExpenses = 0;
        long approvedExpenses = 0;
        long rejectedExpenses = 0;


        for (TravelExpense expense : expenses) {

            switch (expense.getStatus()) {

                case PENDING ->
                        pendingExpenses++;


                case APPROVED -> {

                    approvedExpenses++;

                    approvedSpent =
                            approvedSpent.add(
                                    expense.getAmount()
                            );
                }


                case REJECTED ->
                        rejectedExpenses++;
            }
        }


        approvedSpent =
                approvedSpent.setScale(
                        2,
                        RoundingMode.HALF_UP
                );


        TravelBudgetResponse budget =
                TravelBudgetResponse.from(
                        travel
                );


        BigDecimal balance =
                budget
                        .total()
                        .subtract(
                                approvedSpent
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        TravelSummaryResponse summary =
                new TravelSummaryResponse(
                        approvedSpent,
                        balance,
                        pendingExpenses,
                        approvedExpenses,
                        rejectedExpenses
                );


        boolean administrator =
                isAdministrator(
                        currentUser
                );


        boolean active =
                travel.getStatus()
                        ==
                        TravelStatus.ACTIVE;


        boolean assigned =
                isAssigned(
                        currentUser,
                        travel
                );


        TravelPermissionsResponse permissions =
                new TravelPermissionsResponse(
                        administrator && active,
                        administrator && active,

                        active
                                &&
                                (
                                        administrator
                                                ||
                                                assigned
                                ),

                        administrator && active,
                        administrator && active
                );


        List<UserOptionResponse> users =
                travel
                        .getAssignedUsers()
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


        List<TravelExpenseResponse> expenseResponses =
                expenses
                        .stream()
                        .map(
                                expense ->
                                        buildExpenseResponse(
                                                currentUser,
                                                travel,
                                                expense
                                        )
                        )
                        .toList();


        long days =
                ChronoUnit
                        .DAYS
                        .between(
                                travel.getStartDate(),
                                travel.getEndDate()
                        )
                        +
                        1;


        return new TravelResponse(
                travel.getId(),
                travel.getName(),
                travel.getDestination(),
                travel.getStartDate(),
                travel.getEndDate(),
                days,

                travel
                        .getStatus()
                        .getApiValue(),

                budget,
                users,
                summary,
                expenseResponses,
                permissions
        );
    }


    private TravelExpenseResponse buildExpenseResponse(
            User currentUser,
            Travel travel,
            TravelExpense expense
    ) {

        boolean administrator =
                isAdministrator(
                        currentUser
                );


        boolean ownExpense =
                expense
                        .getCreatedBy()
                        .getId()
                        .equals(
                                currentUser.getId()
                        );


        boolean canDelete =
                travel.getStatus()
                        ==
                        TravelStatus.ACTIVE

                        &&

                        expense.getStatus()
                                !=
                                TravelExpenseStatus.APPROVED

                        &&

                        (
                                administrator
                                        ||
                                        ownExpense
                        );


        String receiptUrl =
                storageService.getPublicUrl(
                        expense.getReceiptFileName()
                );


        return TravelExpenseResponse.from(
                expense,
                receiptUrl,
                canDelete
        );
    }


    // =====================================================
    // USERS
    // =====================================================

    private User getCurrentUser(
            Long currentUserId
    ) {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        return userRepository
                .findByIdAndTenant_Id(
                        currentUserId,
                        tenantId
                )
                .orElseThrow(
                        () ->
                                new AccessDeniedException(
                                        "La sesión no corresponde a un usuario válido."
                                )
                );
    }


    private List<User> getAssignedUsers(
            List<Long> userIds
    ) {

        if (
                userIds == null
                        ||
                        userIds.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Asigna al menos a una persona."
            );
        }


        Set<Long> uniqueIds =
                new LinkedHashSet<>(
                        userIds
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
    // ACCESS
    // =====================================================

    private boolean isAdministrator(
            User user
    ) {

        return user.getRole()
                ==
                Role.ADMIN;
    }


    private boolean isAssigned(
            User user,
            Travel travel
    ) {

        return travel
                .getAssignedUsers()
                .stream()
                .anyMatch(
                        assignedUser ->
                                assignedUser
                                        .getId()
                                        .equals(
                                                user.getId()
                                        )
                );
    }


    private void validateAdministrator(
            User user
    ) {

        if (
                !isAdministrator(
                        user
                )
        ) {

            throw new AccessDeniedException(
                    "No tienes permisos para administrar viáticos."
            );
        }
    }


    private void validateCanViewTravel(
            User user,
            Travel travel
    ) {

        if (
                !user
                        .getTenantId()
                        .equals(
                                travel.getTenantId()
                        )
        ) {

            throw new ResourceNotFoundException(
                    "El viaje solicitado no existe."
            );
        }


        if (
                isAdministrator(user)
                        ||
                        isAssigned(
                                user,
                                travel
                        )
        ) {

            return;
        }


        throw new AccessDeniedException(
                "No tienes permisos para consultar este viaje."
        );
    }


    // =====================================================
    // ENTITIES
    // =====================================================

    private Travel getTravelEntity(
            Long travelId
    ) {

        return travelRepository
                .findByIdAndTenant_Id(
                        travelId,

                        currentTenantService
                                .getCurrentTenantId()
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "El viaje solicitado no existe."
                                )
                );
    }


    private TravelExpense getTravelExpense(
            Long expenseId,
            Long travelId
    ) {

        return travelExpenseRepository
                .findByIdAndTenant_IdAndTravel_Id(
                        expenseId,

                        currentTenantService
                                .getCurrentTenantId(),

                        travelId
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "El gasto solicitado no existe."
                                )
                );
    }


    private void validateActiveTravel(
            Travel travel
    ) {

        if (
                travel.getStatus()
                        !=
                        TravelStatus.ACTIVE
        ) {

            throw new ConflictException(
                    "El viaje ya está finalizado."
            );
        }
    }


    private void validateTravelDates(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (
                startDate == null
                        ||
                        endDate == null
        ) {

            throw new IllegalArgumentException(
                    "Las fechas del viaje son obligatorias."
            );
        }


        if (
                endDate.isBefore(
                        startDate
                )
        ) {

            throw new IllegalArgumentException(
                    "La fecha de retorno no puede ser anterior a la fecha de salida."
            );
        }
    }


    // =====================================================
    // BUDGET
    // =====================================================

    private void applyBudget(
            Travel travel,
            TravelBudgetRequest budget
    ) {

        if (budget == null) {

            throw new IllegalArgumentException(
                    "El presupuesto es obligatorio."
            );
        }


        travel.setFoodBudget(
                normalizeNonNegativeMoney(
                        budget.food()
                )
        );


        travel.setGasolineBudget(
                normalizeNonNegativeMoney(
                        budget.gasoline()
                )
        );


        travel.setTollsBudget(
                normalizeNonNegativeMoney(
                        budget.tolls()
                )
        );


        travel.setLodgingBudget(
                normalizeNonNegativeMoney(
                        budget.lodging()
                )
        );


        travel.setMaterialBudget(
                normalizeNonNegativeMoney(
                        budget.material()
                )
        );


        travel.setOtherBudget(
                normalizeNonNegativeMoney(
                        budget.other()
                )
        );
    }


    private BigDecimal normalizeNonNegativeMoney(
            BigDecimal value
    ) {

        BigDecimal safeValue =
                value == null
                        ?
                        ZERO
                        :
                        value;


        if (
                safeValue.compareTo(
                        BigDecimal.ZERO
                )
                        <
                        0
        ) {

            throw new IllegalArgumentException(
                    "El presupuesto no puede ser negativo."
            );
        }


        return safeValue.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }


    private BigDecimal normalizePositiveMoney(
            BigDecimal value,
            String message
    ) {

        if (
                value == null
                        ||
                        value.compareTo(
                                BigDecimal.ZERO
                        )
                                <=
                                0
        ) {

            throw new IllegalArgumentException(
                    message
            );
        }


        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }


    // =====================================================
    // SORT
    // =====================================================

    private Sort resolveSort(
            String sort
    ) {

        if (
                sort == null
                        ||
                        sort.isBlank()
        ) {

            return Sort.by(
                    Sort.Direction.DESC,
                    "startDate"
            );
        }


        String[] parts =
                sort.split(",");


        String property =
                switch (parts[0].trim()) {

                    case "fechaFin" ->
                            "endDate";

                    case "nombre" ->
                            "name";

                    default ->
                            "startDate";
                };


        Sort.Direction direction =
                Sort.Direction.DESC;


        if (
                parts.length > 1
                        &&
                        "asc".equalsIgnoreCase(
                                parts[1].trim()
                        )
        ) {

            direction =
                    Sort.Direction.ASC;
        }


        return Sort.by(
                direction,
                property
        );
    }


    // =====================================================
    // INTERNAL
    // =====================================================

    private record PreparedExpense(
            TravelExpenseCategory category,
            String description,
            BigDecimal amount,
            LocalDate expenseDate,
            Integer receiptIndex
    ) {
    }
}