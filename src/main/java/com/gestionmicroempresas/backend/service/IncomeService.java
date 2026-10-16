package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.request.CreateIncomeRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateIncomeRequest;

import com.gestionmicroempresas.backend.dto.response.IncomeReportResponse;
import com.gestionmicroempresas.backend.dto.response.IncomeResponse;
import com.gestionmicroempresas.backend.dto.response.IncomeSummaryResponse;
import com.gestionmicroempresas.backend.dto.response.IncomesPageResponse;

import com.gestionmicroempresas.backend.entity.Client;
import com.gestionmicroempresas.backend.entity.Income;

import com.gestionmicroempresas.backend.enums.IncomeDateReference;
import com.gestionmicroempresas.backend.enums.IncomeRecordType;
import com.gestionmicroempresas.backend.enums.IncomeStatus;

import com.gestionmicroempresas.backend.exception.ResourceNotFoundException;

import com.gestionmicroempresas.backend.repository.ClientRepository;
import com.gestionmicroempresas.backend.repository.IncomeRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.List;
import java.util.Locale;

@Service
public class IncomeService {

    private static final BigDecimal VAT_RATE =
            new BigDecimal("0.16");


    private static final BigDecimal ZERO =
            new BigDecimal("0.00");


    private final IncomeRepository incomeRepository;

    private final ClientRepository clientRepository;

    private final CurrentTenantService currentTenantService;


    public IncomeService(
            IncomeRepository incomeRepository,
            ClientRepository clientRepository,
            CurrentTenantService currentTenantService
    ) {

        this.incomeRepository =
                incomeRepository;

        this.clientRepository =
                clientRepository;

        this.currentTenantService =
                currentTenantService;
    }


    // =====================================================
    // GET INCOMES
    // =====================================================

    @Transactional(readOnly = true)
    public IncomesPageResponse getIncomes(
            int page,
            int size,
            String sort,
            String search,
            Long clientId,
            String recordType,
            String status,
            LocalDate dateFrom,
            LocalDate dateTo,
            String dateReference
    ) {

        validateDateRange(
                dateFrom,
                dateTo
        );


        Specification<Income> specification =
                buildSpecification(
                        search,
                        clientId,
                        recordType,
                        status,
                        dateFrom,
                        dateTo,
                        dateReference
                );


        PageRequest pageable =
                PageRequest.of(

                        Math.max(
                                page,
                                0
                        ),

                        Math.min(
                                Math.max(
                                        size,
                                        1
                                ),
                                100
                        ),

                        resolveSort(
                                sort
                        )
                );


        Page<Income> result =
                incomeRepository.findAll(
                        specification,
                        pageable
                );


        return new IncomesPageResponse(

                result
                        .getContent()
                        .stream()
                        .map(
                                IncomeResponse::from
                        )
                        .toList(),

                result.getNumber(),

                result.getSize(),

                result.getTotalElements(),

                result.getTotalPages(),

                result.isFirst(),

                result.isLast()
        );
    }


    // =====================================================
    // SUMMARY
    // =====================================================

    @Transactional(readOnly = true)
    public IncomeSummaryResponse getSummary(
            String search,
            Long clientId,
            String recordType,
            String status,
            LocalDate dateFrom,
            LocalDate dateTo,
            String dateReference
    ) {

        validateDateRange(
                dateFrom,
                dateTo
        );


        return calculateSummary(

                incomeRepository.findAll(

                        buildSpecification(
                                search,
                                clientId,
                                recordType,
                                status,
                                dateFrom,
                                dateTo,
                                dateReference
                        )
                )
        );
    }


    // =====================================================
    // REPORT
    // =====================================================

    @Transactional(readOnly = true)
    public IncomeReportResponse getReport(
            String search,
            Long clientId,
            String recordType,
            String status,
            LocalDate dateFrom,
            LocalDate dateTo,
            String dateReference
    ) {

        validateDateRange(
                dateFrom,
                dateTo
        );


        List<Income> incomes =
                incomeRepository.findAll(

                        buildSpecification(
                                search,
                                clientId,
                                recordType,
                                status,
                                dateFrom,
                                dateTo,
                                dateReference
                        ),

                        Sort
                                .by(
                                        "recordDate"
                                )
                                .descending()
                                .and(
                                        Sort
                                                .by("id")
                                                .descending()
                                )
                );


        return new IncomeReportResponse(

                incomes
                        .stream()
                        .map(
                                IncomeResponse::from
                        )
                        .toList(),

                calculateSummary(
                        incomes
                ),

                OffsetDateTime.now(
                        ZoneOffset.UTC
                )
        );
    }


    // =====================================================
    // CREATE
    // =====================================================

    @Transactional
    public IncomeResponse createIncome(
            CreateIncomeRequest request
    ) {

        Client client =
                getClient(
                        request.clientId()
                );


        IncomeRecordType recordType =
                IncomeRecordType
                        .fromApiValue(
                                request.recordType()
                        );


        IncomeStatus status =
                resolveCreateStatus(
                        recordType,
                        request.status()
                );


        MoneyAmounts amounts =
                calculateAmounts(
                        request.subtotal(),
                        request.taxApplied()
                );


        PaymentData paymentData =
                resolvePaymentData(

                        recordType,

                        status,

                        request.recordDate(),

                        request.paymentDate(),

                        request.paymentMethod()
                );


        Income income =
                new Income(

                        client,

                        recordType,

                        request
                                .concept()
                                .trim(),

                        amounts.subtotal(),

                        request.taxApplied(),

                        amounts.taxAmount(),

                        amounts.total(),

                        status,

                        request.recordDate(),

                        paymentData.paymentDate(),

                        paymentData.paymentMethod()
                );


        return IncomeResponse.from(

                incomeRepository.saveAndFlush(
                        income
                )
        );
    }


    // =====================================================
    // UPDATE
    // =====================================================

    @Transactional
    public IncomeResponse updateIncome(
            Long incomeId,
            UpdateIncomeRequest request
    ) {

        Income income =
                getIncomeById(
                        incomeId
                );


        Client client =
                getClient(
                        request.clientId()
                );


        IncomeRecordType recordType =
                IncomeRecordType
                        .fromApiValue(
                                request.recordType()
                        );


        IncomeStatus status =
                resolveUpdateStatus(

                        recordType,

                        request.status(),

                        income.getStatus()
                );


        MoneyAmounts amounts =
                calculateAmounts(
                        request.subtotal(),
                        request.taxApplied()
                );


        PaymentData paymentData =
                resolvePaymentData(

                        recordType,

                        status,

                        request.recordDate(),

                        request.paymentDate(),

                        request.paymentMethod()
                );


        income.setClient(
                client
        );


        income.setRecordType(
                recordType
        );


        income.setConcept(
                request
                        .concept()
                        .trim()
        );


        income.setSubtotal(
                amounts.subtotal()
        );


        income.setTaxApplied(
                request.taxApplied()
        );


        income.setTaxAmount(
                amounts.taxAmount()
        );


        income.setTotal(
                amounts.total()
        );


        income.setStatus(
                status
        );


        income.setRecordDate(
                request.recordDate()
        );


        income.setPaymentDate(
                paymentData.paymentDate()
        );


        income.setPaymentMethod(
                paymentData.paymentMethod()
        );


        return IncomeResponse.from(

                incomeRepository.saveAndFlush(
                        income
                )
        );
    }


    // =====================================================
    // DELETE
    // =====================================================

    @Transactional
    public void deleteIncome(
            Long incomeId
    ) {

        Income income =
                getIncomeById(
                        incomeId
                );


        incomeRepository.delete(
                income
        );


        incomeRepository.flush();
    }


    // =====================================================
    // GET BY ID
    // =====================================================

    @Transactional(readOnly = true)
    public Income getIncomeById(
            Long incomeId
    ) {

        return incomeRepository
                .findByIdAndTenant_Id(

                        incomeId,

                        currentTenantService
                                .getCurrentTenantId()
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "El ingreso solicitado no existe."
                                )
                );
    }


    // =====================================================
    // CLIENT
    // =====================================================

    private Client getClient(
            Long clientId
    ) {

        return clientRepository
                .findByIdAndTenant_Id(

                        clientId,

                        currentTenantService
                                .getCurrentTenantId()
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "El cliente seleccionado no existe."
                                )
                );
    }


    // =====================================================
    // STATUS
    // =====================================================

    private IncomeStatus resolveCreateStatus(
            IncomeRecordType recordType,
            String requestedStatus
    ) {

        if (
                recordType ==
                        IncomeRecordType.INCOME
        ) {

            return IncomeStatus.PAID;
        }


        if (
                requestedStatus == null
                        ||
                        requestedStatus.isBlank()
        ) {

            return IncomeStatus.PENDING;
        }


        return IncomeStatus
                .fromApiValue(
                        requestedStatus
                );
    }


    private IncomeStatus resolveUpdateStatus(
            IncomeRecordType recordType,
            String requestedStatus,
            IncomeStatus currentStatus
    ) {

        if (
                recordType ==
                        IncomeRecordType.INCOME
        ) {

            return IncomeStatus.PAID;
        }


        if (
                requestedStatus == null
                        ||
                        requestedStatus.isBlank()
        ) {

            return currentStatus;
        }


        return IncomeStatus
                .fromApiValue(
                        requestedStatus
                );
    }


    // =====================================================
    // PAYMENT
    // =====================================================

    private PaymentData resolvePaymentData(
            IncomeRecordType recordType,
            IncomeStatus status,
            LocalDate recordDate,
            LocalDate requestedPaymentDate,
            String requestedPaymentMethod
    ) {

        if (
                status !=
                        IncomeStatus.PAID
        ) {

            return new PaymentData(
                    null,
                    null
            );
        }


        LocalDate paymentDate =
                requestedPaymentDate;


        if (
                paymentDate == null
                        &&
                        recordType ==
                                IncomeRecordType.INCOME
        ) {

            paymentDate =
                    recordDate;
        }


        if (paymentDate == null) {

            throw new IllegalArgumentException(
                    "La fecha de pago es obligatoria para un registro pagado."
            );
        }


        if (
                paymentDate.isBefore(
                        recordDate
                )
        ) {

            throw new IllegalArgumentException(
                    "La fecha de pago no puede ser anterior a la fecha del registro."
            );
        }


        if (
                requestedPaymentMethod == null
                        ||
                        requestedPaymentMethod.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "La forma de pago es obligatoria para un registro pagado."
            );
        }


        return new PaymentData(

                paymentDate,

                requestedPaymentMethod.trim()
        );
    }


    // =====================================================
    // MONEY
    // =====================================================

    private MoneyAmounts calculateAmounts(
            BigDecimal requestedSubtotal,
            boolean taxApplied
    ) {

        BigDecimal subtotal =
                requestedSubtotal
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        BigDecimal taxAmount =
                taxApplied
                        ?
                        subtotal
                                .multiply(
                                        VAT_RATE
                                )
                                .setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        :
                        ZERO;


        BigDecimal total =
                subtotal
                        .add(
                                taxAmount
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        return new MoneyAmounts(
                subtotal,
                taxAmount,
                total
        );
    }


    // =====================================================
    // SUMMARY
    // =====================================================

    private IncomeSummaryResponse calculateSummary(
            List<Income> incomes
    ) {

        BigDecimal collectedTotal =
                ZERO;


        BigDecimal pendingTotal =
                ZERO;


        BigDecimal canceledTotal =
                ZERO;


        long paidRecords = 0;

        long pendingInvoices = 0;

        long canceledInvoices = 0;

        long directIncomes = 0;


        for (Income income : incomes) {

            if (
                    income.getRecordType() ==
                            IncomeRecordType.INCOME
            ) {

                directIncomes++;
            }


            switch (
                    income.getStatus()
            ) {

                case PAID -> {

                    collectedTotal =
                            collectedTotal.add(
                                    income.getTotal()
                            );

                    paidRecords++;
                }


                case PENDING -> {

                    pendingTotal =
                            pendingTotal.add(
                                    income.getTotal()
                            );

                    pendingInvoices++;
                }


                case CANCELED -> {

                    canceledTotal =
                            canceledTotal.add(
                                    income.getTotal()
                            );

                    canceledInvoices++;
                }
            }
        }


        return new IncomeSummaryResponse(

                money(
                        collectedTotal
                ),

                money(
                        pendingTotal
                ),

                money(
                        canceledTotal
                ),

                incomes.size(),

                paidRecords,

                pendingInvoices,

                canceledInvoices,

                directIncomes
        );
    }


    // =====================================================
    // SPECIFICATION
    // =====================================================

    private Specification<Income> buildSpecification(
            String search,
            Long clientId,
            String recordType,
            String status,
            LocalDate dateFrom,
            LocalDate dateTo,
            String dateReference
    ) {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        Specification<Income> specification =
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


        // -------------------------------------------------
        // SEARCH
        // -------------------------------------------------

        if (
                search != null
                        &&
                        !search.isBlank()
        ) {

            String searchPattern =
                    "%"
                            +
                            search
                                    .trim()
                                    .toLowerCase(
                                            Locale.ROOT
                                    )
                            +
                            "%";


            specification =
                    specification.and(

                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder.or(

                                            criteriaBuilder.like(

                                                    criteriaBuilder.lower(
                                                            root.get(
                                                                    "concept"
                                                            )
                                                    ),

                                                    searchPattern
                                            ),

                                            criteriaBuilder.like(

                                                    criteriaBuilder.lower(

                                                            root
                                                                    .get(
                                                                            "client"
                                                                    )
                                                                    .get(
                                                                            "businessName"
                                                                    )
                                                    ),

                                                    searchPattern
                                            ),

                                            criteriaBuilder.like(

                                                    criteriaBuilder.lower(

                                                            root
                                                                    .get(
                                                                            "client"
                                                                    )
                                                                    .get(
                                                                            "rfc"
                                                                    )
                                                    ),

                                                    searchPattern
                                            ),

                                            criteriaBuilder.like(

                                                    criteriaBuilder.lower(
                                                            root.get(
                                                                    "paymentMethod"
                                                            )
                                                    ),

                                                    searchPattern
                                            )
                                    )
                    );
        }


        // -------------------------------------------------
        // CLIENT
        // -------------------------------------------------

        if (clientId != null) {

            specification =
                    specification.and(

                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder.equal(

                                            root
                                                    .get(
                                                            "client"
                                                    )
                                                    .get(
                                                            "id"
                                                    ),

                                            clientId
                                    )
                    );
        }


        // -------------------------------------------------
        // RECORD TYPE
        // -------------------------------------------------

        if (
                recordType != null
                        &&
                        !recordType.isBlank()
        ) {

            IncomeRecordType parsedRecordType =
                    IncomeRecordType
                            .fromApiValue(
                                    recordType
                            );


            specification =
                    specification.and(

                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder.equal(

                                            root.get(
                                                    "recordType"
                                            ),

                                            parsedRecordType
                                    )
                    );
        }


        // -------------------------------------------------
        // STATUS
        // -------------------------------------------------

        if (
                status != null
                        &&
                        !status.isBlank()
        ) {

            IncomeStatus parsedStatus =
                    IncomeStatus
                            .fromApiValue(
                                    status
                            );


            specification =
                    specification.and(

                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder.equal(

                                            root.get(
                                                    "status"
                                            ),

                                            parsedStatus
                                    )
                    );
        }


        // -------------------------------------------------
        // DATE
        // -------------------------------------------------

        IncomeDateReference reference =
                IncomeDateReference
                        .fromApiValue(
                                dateReference
                        );


        String dateProperty =
                reference ==
                        IncomeDateReference.PAYMENT
                        ?
                        "paymentDate"
                        :
                        "recordDate";


        if (dateFrom != null) {

            specification =
                    specification.and(

                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder
                                            .greaterThanOrEqualTo(

                                                    root
                                                            .<LocalDate>get(
                                                                    dateProperty
                                                            ),

                                                    dateFrom
                                            )
                    );
        }


        if (dateTo != null) {

            specification =
                    specification.and(

                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder
                                            .lessThanOrEqualTo(

                                                    root
                                                            .<LocalDate>get(
                                                                    dateProperty
                                                            ),

                                                    dateTo
                                            )
                    );
        }


        return specification;
    }


    // =====================================================
    // DATE RANGE
    // =====================================================

    private void validateDateRange(
            LocalDate dateFrom,
            LocalDate dateTo
    ) {

        if (
                dateFrom != null
                        &&
                        dateTo != null
                        &&
                        dateFrom.isAfter(
                                dateTo
                        )
        ) {

            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }
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
                    "recordDate"
            );
        }


        String[] parts =
                sort.split(",");


        String property =
                switch (
                        parts[0].trim()
                        ) {

                    case "total" ->
                            "total";

                    case "concepto" ->
                            "concept";

                    case "fechaPago" ->
                            "paymentDate";

                    case "cliente" ->
                            "client.businessName";

                    case "fecha" ->
                            "recordDate";

                    default ->
                            "recordDate";
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
    // HELPERS
    // =====================================================

    private BigDecimal money(
            BigDecimal value
    ) {

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }


    private record MoneyAmounts(

            BigDecimal subtotal,

            BigDecimal taxAmount,

            BigDecimal total

    ) {
    }


    private record PaymentData(

            LocalDate paymentDate,

            String paymentMethod

    ) {
    }
}