package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.request.CreateExpenseRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateExpenseRequest;

import com.gestionmicroempresas.backend.dto.response.ExpenseMonthResponse;
import com.gestionmicroempresas.backend.dto.response.ExpenseResponse;
import com.gestionmicroempresas.backend.dto.response.ExpensesResponse;
import com.gestionmicroempresas.backend.dto.response.ExpensesSummaryResponse;

import com.gestionmicroempresas.backend.entity.ManualExpense;
import com.gestionmicroempresas.backend.entity.TravelExpense;

import com.gestionmicroempresas.backend.enums.ExpenseCategory;
import com.gestionmicroempresas.backend.enums.TravelExpenseStatus;

import com.gestionmicroempresas.backend.exception.ResourceNotFoundException;

import com.gestionmicroempresas.backend.repository.ManualExpenseRepository;
import com.gestionmicroempresas.backend.repository.TravelExpenseRepository;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Service
public class ExpenseService {

    private static final BigDecimal ZERO =
            new BigDecimal("0.00");


    private final ManualExpenseRepository manualExpenseRepository;

    private final TravelExpenseRepository travelExpenseRepository;

    private final CurrentTenantService currentTenantService;


    public ExpenseService(
            ManualExpenseRepository manualExpenseRepository,
            TravelExpenseRepository travelExpenseRepository,
            CurrentTenantService currentTenantService
    ) {

        this.manualExpenseRepository =
                manualExpenseRepository;

        this.travelExpenseRepository =
                travelExpenseRepository;

        this.currentTenantService =
                currentTenantService;
    }


    // =====================================================
    // GET EXPENSES
    // =====================================================

    @Transactional(readOnly = true)
    public ExpensesResponse getExpenses(
            String from,
            String to
    ) {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        YearMonth fromMonth =
                parseYearMonth(
                        from,
                        "El periodo inicial no es válido."
                );


        YearMonth toMonth =
                parseYearMonth(
                        to,
                        "El periodo final no es válido."
                );


        if (
                fromMonth.isAfter(
                        toMonth
                )
        ) {

            throw new IllegalArgumentException(
                    "El periodo inicial no puede ser posterior al periodo final."
            );
        }


        LocalDate dateFrom =
                fromMonth.atDay(1);


        LocalDate dateTo =
                toMonth.atEndOfMonth();


        List<ManualExpense> manualExpenses =
                manualExpenseRepository
                        .findAllByTenant_IdAndExpenseDateBetweenOrderByExpenseDateDescIdDesc(
                                tenantId,
                                dateFrom,
                                dateTo
                        );


        List<TravelExpense> travelExpenses =
                travelExpenseRepository
                        .findAllByTenant_IdAndStatusAndExpenseDateBetweenOrderByExpenseDateDescIdDesc(

                                tenantId,

                                TravelExpenseStatus.APPROVED,

                                dateFrom,

                                dateTo
                        );


        Map<YearMonth, MonthAccumulator> months =
                new LinkedHashMap<>();


        TreeSet<YearMonth> periods =
                new TreeSet<>(
                        Comparator.reverseOrder()
                );


        for (
                ManualExpense expense :
                manualExpenses
        ) {

            periods.add(

                    YearMonth.from(
                            expense.getExpenseDate()
                    )
            );
        }


        for (
                TravelExpense expense :
                travelExpenses
        ) {

            periods.add(

                    YearMonth.from(
                            expense.getExpenseDate()
                    )
            );
        }


        for (
                YearMonth period :
                periods
        ) {

            months.put(

                    period,

                    new MonthAccumulator(
                            period
                    )
            );
        }


        // =================================================
        // MANUAL EXPENSES
        // =================================================

        for (
                ManualExpense expense :
                manualExpenses
        ) {

            YearMonth period =
                    YearMonth.from(
                            expense.getExpenseDate()
                    );


            MonthAccumulator month =
                    months.get(
                            period
                    );


            month.expenses.add(

                    ExpenseResponse
                            .fromManualExpense(
                                    expense
                            )
            );


            month.registeredTotal =
                    month
                            .registeredTotal
                            .add(
                                    expense.getAmount()
                            );


            month.registeredCount++;
        }


        // =================================================
        // TRAVEL EXPENSES
        // =================================================

        for (
                TravelExpense expense :
                travelExpenses
        ) {

            YearMonth period =
                    YearMonth.from(
                            expense.getExpenseDate()
                    );


            MonthAccumulator month =
                    months.get(
                            period
                    );


            month.expenses.add(

                    ExpenseResponse
                            .fromTravelExpense(
                                    expense
                            )
            );


            month.travelTotal =
                    month
                            .travelTotal
                            .add(
                                    expense.getAmount()
                            );


            month.travelCount++;
        }


        Comparator<ExpenseResponse> expenseComparator =
                Comparator
                        .comparing(
                                ExpenseResponse::expenseDate
                        )
                        .reversed()
                        .thenComparing(
                                ExpenseResponse::origin
                        )
                        .thenComparing(
                                ExpenseResponse::id,
                                Comparator.reverseOrder()
                        );


        List<ExpenseMonthResponse> monthResponses =
                new ArrayList<>();


        BigDecimal registeredTotal =
                ZERO;


        BigDecimal travelTotal =
                ZERO;


        long registeredCount = 0;

        long travelCount = 0;


        for (
                MonthAccumulator month :
                months.values()
        ) {

            month.expenses.sort(
                    expenseComparator
            );


            BigDecimal monthRegisteredTotal =
                    money(
                            month.registeredTotal
                    );


            BigDecimal monthTravelTotal =
                    money(
                            month.travelTotal
                    );


            BigDecimal monthTotal =
                    money(

                            monthRegisteredTotal.add(
                                    monthTravelTotal
                            )
                    );


            monthResponses.add(

                    new ExpenseMonthResponse(

                            month.period.toString(),

                            month.period.getYear(),

                            month.period.getMonthValue(),

                            monthRegisteredTotal,

                            monthTravelTotal,

                            monthTotal,

                            month.registeredCount,

                            month.travelCount,

                            List.copyOf(
                                    month.expenses
                            )
                    )
            );


            registeredTotal =
                    registeredTotal.add(
                            monthRegisteredTotal
                    );


            travelTotal =
                    travelTotal.add(
                            monthTravelTotal
                    );


            registeredCount +=
                    month.registeredCount;


            travelCount +=
                    month.travelCount;
        }


        registeredTotal =
                money(
                        registeredTotal
                );


        travelTotal =
                money(
                        travelTotal
                );


        BigDecimal total =
                money(

                        registeredTotal.add(
                                travelTotal
                        )
                );


        ExpensesSummaryResponse summary =
                new ExpensesSummaryResponse(

                        total,

                        registeredTotal,

                        travelTotal,

                        registeredCount,

                        travelCount
                );


        return new ExpensesResponse(

                summary,

                monthResponses
        );
    }


    // =====================================================
    // CREATE
    // =====================================================

    @Transactional
    public ExpenseResponse createExpense(
            CreateExpenseRequest request
    ) {

        ExpenseCategory category =
                ExpenseCategory
                        .fromApiValue(
                                request.category()
                        );


        BigDecimal amount =
                normalizeAmount(
                        request.amount()
                );


        ManualExpense expense =
                new ManualExpense(

                        currentTenantService
                                .getCurrentTenant(),

                        request
                                .concept()
                                .trim(),

                        category,

                        amount,

                        request.expenseDate(),

                        normalizeDescription(
                                request.description()
                        )
                );


        ManualExpense savedExpense =
                manualExpenseRepository
                        .saveAndFlush(
                                expense
                        );


        return ExpenseResponse
                .fromManualExpense(
                        savedExpense
                );
    }


    // =====================================================
    // UPDATE
    // =====================================================

    @Transactional
    public ExpenseResponse updateExpense(
            Long expenseId,
            UpdateExpenseRequest request
    ) {

        ManualExpense expense =
                getManualExpense(
                        expenseId
                );


        ExpenseCategory category =
                ExpenseCategory
                        .fromApiValue(
                                request.category()
                        );


        expense.setConcept(
                request
                        .concept()
                        .trim()
        );


        expense.setCategory(
                category
        );


        expense.setAmount(

                normalizeAmount(
                        request.amount()
                )
        );


        expense.setExpenseDate(
                request.expenseDate()
        );


        expense.setDescription(

                normalizeDescription(
                        request.description()
                )
        );


        ManualExpense updatedExpense =
                manualExpenseRepository
                        .saveAndFlush(
                                expense
                        );


        return ExpenseResponse
                .fromManualExpense(
                        updatedExpense
                );
    }


    // =====================================================
    // DELETE
    // =====================================================

    @Transactional
    public void deleteExpense(
            Long expenseId
    ) {

        ManualExpense expense =
                getManualExpense(
                        expenseId
                );


        manualExpenseRepository.delete(
                expense
        );


        manualExpenseRepository.flush();
    }


    // =====================================================
    // GET MANUAL EXPENSE
    // =====================================================

    private ManualExpense getManualExpense(
            Long expenseId
    ) {

        return manualExpenseRepository
                .findByIdAndTenant_Id(

                        expenseId,

                        currentTenantService
                                .getCurrentTenantId()
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "El egreso solicitado no existe."
                                )
                );
    }


    // =====================================================
    // MONTH
    // =====================================================

    private YearMonth parseYearMonth(
            String value,
            String message
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    message
            );
        }


        try {

            return YearMonth.parse(
                    value.trim()
            );

        } catch (
                DateTimeParseException exception
        ) {

            throw new IllegalArgumentException(
                    message
            );
        }
    }


    // =====================================================
    // NORMALIZATION
    // =====================================================

    private BigDecimal normalizeAmount(
            BigDecimal amount
    ) {

        if (
                amount == null
                        ||
                        amount.compareTo(
                                BigDecimal.ZERO
                        )
                                <=
                                0
        ) {

            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero."
            );
        }


        return money(
                amount
        );
    }


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


    private BigDecimal money(
            BigDecimal value
    ) {

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }


    // =====================================================
    // INTERNAL
    // =====================================================

    private static class MonthAccumulator {

        private final YearMonth period;


        private BigDecimal registeredTotal =
                ZERO;


        private BigDecimal travelTotal =
                ZERO;


        private long registeredCount;

        private long travelCount;


        private final List<ExpenseResponse> expenses =
                new ArrayList<>();


        private MonthAccumulator(
                YearMonth period
        ) {

            this.period =
                    period;
        }
    }
}