package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse;

import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse.BackupData;
import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse.ClientData;
import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse.ExpenseData;
import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse.IncomeData;
import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse.InventoryCatalogData;
import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse.InventoryMovementData;
import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse.InventoryProductData;
import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse.KitComponentData;
import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse.TaskData;
import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse.TaskEvidenceData;
import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse.TravelBudgetData;
import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse.TravelData;
import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse.TravelExpenseData;
import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse.UserData;

import com.gestionmicroempresas.backend.entity.Client;
import com.gestionmicroempresas.backend.entity.Income;
import com.gestionmicroempresas.backend.entity.InventoryCatalogItem;
import com.gestionmicroempresas.backend.entity.InventoryKitComponent;
import com.gestionmicroempresas.backend.entity.InventoryProduct;
import com.gestionmicroempresas.backend.entity.InventoryStockMovement;
import com.gestionmicroempresas.backend.entity.ManualExpense;
import com.gestionmicroempresas.backend.entity.Task;
import com.gestionmicroempresas.backend.entity.TaskEvidence;
import com.gestionmicroempresas.backend.entity.Travel;
import com.gestionmicroempresas.backend.entity.TravelExpense;
import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.enums.InventoryMovementType;
import com.gestionmicroempresas.backend.enums.Role;

import com.gestionmicroempresas.backend.repository.ClientRepository;
import com.gestionmicroempresas.backend.repository.IncomeRepository;
import com.gestionmicroempresas.backend.repository.InventoryCatalogRepository;
import com.gestionmicroempresas.backend.repository.InventoryKitComponentRepository;
import com.gestionmicroempresas.backend.repository.InventoryProductRepository;
import com.gestionmicroempresas.backend.repository.InventoryStockMovementRepository;
import com.gestionmicroempresas.backend.repository.ManualExpenseRepository;
import com.gestionmicroempresas.backend.repository.TaskEvidenceRepository;
import com.gestionmicroempresas.backend.repository.TaskRepository;
import com.gestionmicroempresas.backend.repository.TravelExpenseRepository;
import com.gestionmicroempresas.backend.repository.TravelRepository;
import com.gestionmicroempresas.backend.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.Comparator;
import java.util.List;

@Service
public class SystemBackupService {

    private static final String BACKUP_FORMAT =
            "gestion-microempresas-json";


    private static final int BACKUP_VERSION =
            1;


    private final UserRepository userRepository;

    private final ClientRepository clientRepository;

    private final IncomeRepository incomeRepository;

    private final ManualExpenseRepository manualExpenseRepository;

    private final TravelRepository travelRepository;

    private final TravelExpenseRepository travelExpenseRepository;

    private final TaskRepository taskRepository;

    private final TaskEvidenceRepository taskEvidenceRepository;

    private final InventoryCatalogRepository inventoryCatalogRepository;

    private final InventoryProductRepository inventoryProductRepository;

    private final InventoryStockMovementRepository inventoryStockMovementRepository;

    private final InventoryKitComponentRepository inventoryKitComponentRepository;

    private final CurrentTenantService currentTenantService;


    public SystemBackupService(
            UserRepository userRepository,
            ClientRepository clientRepository,
            IncomeRepository incomeRepository,
            ManualExpenseRepository manualExpenseRepository,
            TravelRepository travelRepository,
            TravelExpenseRepository travelExpenseRepository,
            TaskRepository taskRepository,
            TaskEvidenceRepository taskEvidenceRepository,
            InventoryCatalogRepository inventoryCatalogRepository,
            InventoryProductRepository inventoryProductRepository,
            InventoryStockMovementRepository inventoryStockMovementRepository,
            InventoryKitComponentRepository inventoryKitComponentRepository,
            CurrentTenantService currentTenantService
    ) {

        this.userRepository = userRepository;

        this.clientRepository = clientRepository;

        this.incomeRepository = incomeRepository;

        this.manualExpenseRepository = manualExpenseRepository;

        this.travelRepository = travelRepository;

        this.travelExpenseRepository = travelExpenseRepository;

        this.taskRepository = taskRepository;

        this.taskEvidenceRepository = taskEvidenceRepository;

        this.inventoryCatalogRepository =
                inventoryCatalogRepository;

        this.inventoryProductRepository =
                inventoryProductRepository;

        this.inventoryStockMovementRepository =
                inventoryStockMovementRepository;

        this.inventoryKitComponentRepository =
                inventoryKitComponentRepository;

        this.currentTenantService =
                currentTenantService;
    }


    // =====================================================
    // BACKUP
    // =====================================================

    @Transactional(readOnly = true)
    public SystemBackupResponse generateBackup(
            Long currentUserId
    ) {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        User currentUser =
                userRepository
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


        if (
                currentUser.getRole()
                        !=
                        Role.ADMIN
        ) {

            throw new AccessDeniedException(
                    "No tienes permisos para descargar el respaldo."
            );
        }


        BackupData data =
                new BackupData(
                        getUsers(
                                tenantId
                        ),

                        getClients(
                                tenantId
                        ),

                        getIncomes(
                                tenantId
                        ),

                        getExpenses(
                                tenantId
                        ),

                        getTravels(
                                tenantId
                        ),

                        getTravelExpenses(
                                tenantId
                        ),

                        getTasks(
                                tenantId
                        ),

                        getTaskEvidence(
                                tenantId
                        ),

                        getInventoryCatalogs(
                                tenantId
                        ),

                        getInventoryProducts(
                                tenantId
                        ),

                        getInventoryMovements(
                                tenantId
                        ),

                        getKitComponents(
                                tenantId
                        )
                );


        List<String> warnings =
                List.of(
                        "Por seguridad, el respaldo no incluye contraseñas, hashes de contraseñas, tokens JWT ni secretos de configuración.",

                        "El archivo JSON contiene las referencias de fotografías, comprobantes y evidencias, pero no incluye los archivos binarios almacenados en el servidor.",

                        "El respaldo contiene exclusivamente la información perteneciente a la empresa autenticada."
                );


        return new SystemBackupResponse(
                BACKUP_FORMAT,

                BACKUP_VERSION,

                OffsetDateTime.now(
                        ZoneOffset.UTC
                ),

                false,

                false,

                warnings,

                data
        );
    }


    // =====================================================
    // USERS
    // =====================================================

    private List<UserData> getUsers(
            Long tenantId
    ) {

        return userRepository
                .findAllByTenant_IdOrderByNameAsc(
                        tenantId
                )
                .stream()
                .sorted(
                        Comparator.comparing(
                                User::getId
                        )
                )
                .map(
                        user ->
                                new UserData(
                                        user.getId(),

                                        user.getName(),

                                        user.getEmail(),

                                        user
                                                .getRole()
                                                .getApiValue(),

                                        user.getCreatedAt(),

                                        user.getUpdatedAt()
                                )
                )
                .toList();
    }


    // =====================================================
    // CLIENTS
    // =====================================================

    private List<ClientData> getClients(
            Long tenantId
    ) {

        return clientRepository
                .findAllByTenant_IdOrderByIdAsc(
                        tenantId
                )
                .stream()
                .map(
                        client ->
                                new ClientData(
                                        client.getId(),

                                        client.getBusinessName(),

                                        client.getRfc(),

                                        client
                                                .getPhones()
                                                .stream()
                                                .sorted()
                                                .toList(),

                                        client
                                                .getEmails()
                                                .stream()
                                                .sorted()
                                                .toList(),

                                        client.getCreatedAt(),

                                        client.getUpdatedAt()
                                )
                )
                .toList();
    }


    // =====================================================
    // INCOMES
    // =====================================================

    private List<IncomeData> getIncomes(
            Long tenantId
    ) {

        return incomeRepository
                .findAllByTenant_IdOrderByIdAsc(
                        tenantId
                )
                .stream()
                .map(
                        income ->
                                new IncomeData(
                                        income.getId(),

                                        income
                                                .getClient()
                                                .getId(),

                                        income
                                                .getRecordType()
                                                .getApiValue(),

                                        income.getConcept(),

                                        income.getSubtotal(),

                                        income.isTaxApplied(),

                                        income.getTaxAmount(),

                                        income.getTotal(),

                                        income
                                                .getStatus()
                                                .getApiValue(),

                                        income.getRecordDate(),

                                        income.getPaymentDate(),

                                        income.getPaymentMethod(),

                                        income.getCreatedAt(),

                                        income.getUpdatedAt()
                                )
                )
                .toList();
    }


    // =====================================================
    // EXPENSES
    // =====================================================

    private List<ExpenseData> getExpenses(
            Long tenantId
    ) {

        return manualExpenseRepository
                .findAllByTenant_IdOrderByIdAsc(
                        tenantId
                )
                .stream()
                .map(
                        expense ->
                                new ExpenseData(
                                        expense.getId(),

                                        expense.getConcept(),

                                        expense
                                                .getCategory()
                                                .getApiValue(),

                                        expense.getAmount(),

                                        expense.getExpenseDate(),

                                        expense.getDescription(),

                                        expense.getCreatedAt(),

                                        expense.getUpdatedAt()
                                )
                )
                .toList();
    }


    // =====================================================
    // TRAVELS
    // =====================================================

    private List<TravelData> getTravels(
            Long tenantId
    ) {

        return travelRepository
                .findAllByTenant_IdOrderByIdAsc(
                        tenantId
                )
                .stream()
                .map(
                        travel ->
                                new TravelData(
                                        travel.getId(),

                                        travel.getName(),

                                        travel.getDestination(),

                                        travel.getStartDate(),

                                        travel.getEndDate(),

                                        travel
                                                .getStatus()
                                                .getApiValue(),

                                        new TravelBudgetData(
                                                travel.getFoodBudget(),

                                                travel.getGasolineBudget(),

                                                travel.getTollsBudget(),

                                                travel.getLodgingBudget(),

                                                travel.getMaterialBudget(),

                                                travel.getOtherBudget()
                                        ),

                                        travel
                                                .getAssignedUsers()
                                                .stream()
                                                .map(
                                                        User::getId
                                                )
                                                .sorted()
                                                .toList(),

                                        travel.getCreatedAt(),

                                        travel.getUpdatedAt()
                                )
                )
                .toList();
    }


    // =====================================================
    // TRAVEL EXPENSES
    // =====================================================

    private List<TravelExpenseData> getTravelExpenses(
            Long tenantId
    ) {

        return travelExpenseRepository
                .findAllByTenant_IdOrderByIdAsc(
                        tenantId
                )
                .stream()
                .map(
                        expense ->
                                new TravelExpenseData(
                                        expense.getId(),

                                        expense
                                                .getTravel()
                                                .getId(),

                                        expense
                                                .getCategory()
                                                .getApiValue(),

                                        expense.getDescription(),

                                        expense.getAmount(),

                                        expense.getExpenseDate(),

                                        expense
                                                .getStatus()
                                                .getApiValue(),

                                        expense.getRejectionReason(),

                                        expense.getReceiptFileName(),

                                        expense
                                                .getCreatedBy()
                                                .getId(),

                                        expense.getCreatedAt(),

                                        expense.getUpdatedAt()
                                )
                )
                .toList();
    }


    // =====================================================
    // TASKS
    // =====================================================

    private List<TaskData> getTasks(
            Long tenantId
    ) {

        return taskRepository
                .findAllByTenant_IdOrderByIdAsc(
                        tenantId
                )
                .stream()
                .map(
                        task ->
                                new TaskData(
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

                                        task
                                                .getCreator()
                                                .getId(),

                                        task
                                                .getAssignees()
                                                .stream()
                                                .map(
                                                        User::getId
                                                )
                                                .sorted()
                                                .toList(),

                                        task.getFeedback(),

                                        task.getReviewedAt(),

                                        task.getCompletedAt(),

                                        task.getCreatedAt(),

                                        task.getUpdatedAt()
                                )
                )
                .toList();
    }


    // =====================================================
    // TASK EVIDENCE
    // =====================================================

    private List<TaskEvidenceData> getTaskEvidence(
            Long tenantId
    ) {

        return taskEvidenceRepository
                .findAllByTenant_IdOrderByIdAsc(
                        tenantId
                )
                .stream()
                .map(
                        evidence ->
                                new TaskEvidenceData(
                                        evidence.getId(),

                                        evidence
                                                .getTask()
                                                .getId(),

                                        evidence
                                                .getAuthor()
                                                .getId(),

                                        evidence.getFileName(),

                                        evidence.getOriginalName(),

                                        evidence.getMimeType(),

                                        evidence.getSizeBytes(),

                                        evidence.getCreatedAt()
                                )
                )
                .toList();
    }


    // =====================================================
    // INVENTORY CATALOGS
    // =====================================================

    private List<InventoryCatalogData> getInventoryCatalogs(
            Long tenantId
    ) {

        return inventoryCatalogRepository
                .findAllByTenant_IdOrderByIdAsc(
                        tenantId
                )
                .stream()
                .map(
                        item ->
                                new InventoryCatalogData(
                                        item.getId(),

                                        item
                                                .getType()
                                                .getApiValue(),

                                        item.getName(),

                                        item.getLink(),

                                        item.isAllowPriceUpdate(),

                                        item.getCreatedAt(),

                                        item.getUpdatedAt()
                                )
                )
                .toList();
    }


    // =====================================================
    // INVENTORY PRODUCTS
    // =====================================================

    private List<InventoryProductData> getInventoryProducts(
            Long tenantId
    ) {

        return inventoryProductRepository
                .findAllByTenant_IdOrderByIdAsc(
                        tenantId
                )
                .stream()
                .map(
                        product ->
                                new InventoryProductData(
                                        product.getId(),

                                        product.getDescription(),

                                        product.getModel(),

                                        getCatalogId(
                                                product.getCategory()
                                        ),

                                        getCatalogId(
                                                product.getBrand()
                                        ),

                                        getCatalogId(
                                                product.getUnitMeasure()
                                        ),

                                        getCatalogId(
                                                product.getSupplier()
                                        ),

                                        getCatalogId(
                                                product.getMeasure()
                                        ),

                                        getCatalogId(
                                                product.getWarehouse()
                                        ),

                                        getCatalogId(
                                                product.getCondition()
                                        ),

                                        product.getRowLocation(),

                                        product.getUnitPrice(),

                                        product.getQuantity(),

                                        product.getMinimumStock(),

                                        product.isKit(),

                                        product.getPhotoFileName(),

                                        product.getQrCode(),

                                        product.getCreatedAt(),

                                        product.getUpdatedAt()
                                )
                )
                .toList();
    }


    // =====================================================
    // INVENTORY MOVEMENTS
    // =====================================================

    private List<InventoryMovementData> getInventoryMovements(
            Long tenantId
    ) {

        return inventoryStockMovementRepository
                .findAllByTenant_IdOrderByIdAsc(
                        tenantId
                )
                .stream()
                .map(
                        movement ->
                                new InventoryMovementData(
                                        movement.getId(),

                                        movement
                                                .getProduct()
                                                .getId(),

                                        movement
                                                .getUser()
                                                .getId(),

                                        getMovementApiValue(
                                                movement.getMovementType()
                                        ),

                                        movement.getQuantityChange(),

                                        movement.getResultingQuantity(),

                                        movement.getReason(),

                                        movement.getCreatedAt()
                                )
                )
                .toList();
    }


    // =====================================================
    // KIT COMPONENTS
    // =====================================================

    private List<KitComponentData> getKitComponents(
            Long tenantId
    ) {

        return inventoryKitComponentRepository
                .findAllByTenant_IdOrderByIdAsc(
                        tenantId
                )
                .stream()
                .map(
                        component ->
                                new KitComponentData(
                                        component.getId(),

                                        component
                                                .getKit()
                                                .getId(),

                                        component
                                                .getComponent()
                                                .getId(),

                                        component.getRequiredQuantity(),

                                        component.getCreatedAt()
                                )
                )
                .toList();
    }


    // =====================================================
    // HELPERS
    // =====================================================

    private Long getCatalogId(
            InventoryCatalogItem item
    ) {

        return item == null
                ?
                null
                :
                item.getId();
    }


    private String getMovementApiValue(
            InventoryMovementType type
    ) {

        return switch (type) {

            case INITIAL ->
                    "INICIAL";

            case ENTRY ->
                    "ENTRADA";

            case EXIT ->
                    "SALIDA";

            case KIT_ASSEMBLY ->
                    "ARMADO_KIT";

            case KIT_DISASSEMBLY ->
                    "DESARMADO_KIT";
        };
    }
}