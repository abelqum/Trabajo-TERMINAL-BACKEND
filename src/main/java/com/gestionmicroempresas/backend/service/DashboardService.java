package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.response.DashboardAnnualPointResponse;
import com.gestionmicroempresas.backend.dto.response.DashboardExpenseCategoriesResponse;
import com.gestionmicroempresas.backend.dto.response.DashboardExpenseCategoryResponse;
import com.gestionmicroempresas.backend.dto.response.DashboardFinancialSummaryResponse;
import com.gestionmicroempresas.backend.dto.response.DashboardMonthlyPointResponse;
import com.gestionmicroempresas.backend.dto.response.DashboardMonthlyResponse;
import com.gestionmicroempresas.backend.dto.response.DashboardTaskResponse;
import com.gestionmicroempresas.backend.dto.response.EmployeeDashboardResponse;
import com.gestionmicroempresas.backend.dto.response.FinancialDashboardResponse;
import com.gestionmicroempresas.backend.dto.response.UserOptionResponse;

import com.gestionmicroempresas.backend.entity.Income;
import com.gestionmicroempresas.backend.entity.ManualExpense;
import com.gestionmicroempresas.backend.entity.Task;
import com.gestionmicroempresas.backend.entity.TravelExpense;
import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.enums.IncomeStatus;
import com.gestionmicroempresas.backend.enums.Role;
import com.gestionmicroempresas.backend.enums.TaskStatus;
import com.gestionmicroempresas.backend.enums.TravelExpenseStatus;

import com.gestionmicroempresas.backend.repository.IncomeRepository;
import com.gestionmicroempresas.backend.repository.ManualExpenseRepository;
import com.gestionmicroempresas.backend.repository.TaskRepository;
import com.gestionmicroempresas.backend.repository.TravelExpenseRepository;
import com.gestionmicroempresas.backend.repository.UserRepository;

import jakarta.persistence.criteria.JoinType;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.security.access.AccessDeniedException;

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
import java.util.Set;
import java.util.TreeSet;

@Service
public class DashboardService {

    private static final BigDecimal ZERO =
            new BigDecimal("0.00");


    private static final String[] MONTH_NAMES = {
            "Ene",
            "Feb",
            "Mar",
            "Abr",
            "May",
            "Jun",
            "Jul",
            "Ago",
            "Sep",
            "Oct",
            "Nov",
            "Dic"
    };


    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    private final IncomeRepository incomeRepository;

    private final ManualExpenseRepository manualExpenseRepository;

    private final TravelExpenseRepository travelExpenseRepository;

    private final CurrentTenantService currentTenantService;


    public DashboardService(
            UserRepository userRepository,
            TaskRepository taskRepository,
            IncomeRepository incomeRepository,
            ManualExpenseRepository manualExpenseRepository,
            TravelExpenseRepository travelExpenseRepository,
            CurrentTenantService currentTenantService
    ) {

        this.userRepository = userRepository;

        this.taskRepository = taskRepository;

        this.incomeRepository = incomeRepository;

        this.manualExpenseRepository = manualExpenseRepository;

        this.travelExpenseRepository = travelExpenseRepository;

        this.currentTenantService = currentTenantService;
    }


    // =====================================================
    // HOME
    // =====================================================

    @Transactional(readOnly = true)
    public Object getHome(
            Long currentUserId
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        if (
                currentUser.getRole()
                        ==
                        Role.EMPLOYEE
        ) {

            return buildEmployeeDashboard(
                    currentUser
            );
        }


        return buildFinancialDashboard(
                currentUser
        );
    }


    // =====================================================
    // EMPLOYEE DASHBOARD
    // =====================================================

    private EmployeeDashboardResponse buildEmployeeDashboard(
            User currentUser
    ) {

        Long tenantId =
                currentUser.getTenantId();


        Specification<Task> specification =
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
                                    JoinType.INNER
                            );


                    return criteriaBuilder.and(

                            criteriaBuilder.equal(
                                    root
                                            .get("tenant")
                                            .get("id"),
                                    tenantId
                            ),

                            criteriaBuilder.equal(
                                    assignees.get("id"),
                                    currentUser.getId()
                            ),

                            criteriaBuilder.notEqual(
                                    root.get("status"),
                                    TaskStatus.COMPLETED
                            )
                    );
                };


        List<Task> tasks =
                taskRepository
                        .findAll(
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
                        )
                        .stream()
                        .limit(6)
                        .toList();


        List<DashboardTaskResponse> pendingTasks =
                tasks
                        .stream()
                        .map(
                                DashboardTaskResponse::from
                        )
                        .toList();


        return new EmployeeDashboardResponse(
                "EMPLEADO",

                UserOptionResponse.from(
                        currentUser
                ),

                pendingTasks
        );
    }


    // =====================================================
    // FINANCIAL DASHBOARD
    // =====================================================

    private FinancialDashboardResponse buildFinancialDashboard(
            User currentUser
    ) {

        validateFinancialAccess(
                currentUser
        );


        FinancialData data =
                loadFinancialData(
                        currentUser.getTenantId()
                );


        int currentYear =
                LocalDate
                        .now()
                        .getYear();


        YearMonth currentMonth =
                YearMonth.now();


        List<Integer> availableYears =
                getAvailableYears(
                        data,
                        currentYear
                );


        List<DashboardAnnualPointResponse> historical =
                buildHistorical(
                        data,
                        availableYears
                );


        DashboardMonthlyResponse monthly =
                buildMonthly(
                        data,
                        currentYear
                );


        DashboardFinancialSummaryResponse periodSummary =
                buildSummary(
                        data,
                        currentMonth,
                        currentMonth
                );


        DashboardExpenseCategoriesResponse expenseCategories =
                buildExpenseCategories(
                        data,
                        currentMonth,
                        currentMonth
                );


        return new FinancialDashboardResponse(
                "FINANCIERA",

                UserOptionResponse.from(
                        currentUser
                ),

                availableYears,

                historical,

                monthly,

                periodSummary,

                expenseCategories
        );
    }


    // =====================================================
    // MONTHLY
    // =====================================================

    @Transactional(readOnly = true)
    public DashboardMonthlyResponse getMonthlyEvolution(
            Long currentUserId,
            int year
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        validateFinancialAccess(
                currentUser
        );


        if (
                year < 2000
                        ||
                        year > 2200
        ) {

            throw new IllegalArgumentException(
                    "El año solicitado no es válido."
            );
        }


        return buildMonthly(
                loadFinancialData(
                        currentUser.getTenantId()
                ),
                year
        );
    }


    // =====================================================
    // PERIOD SUMMARY
    // =====================================================

    @Transactional(readOnly = true)
    public DashboardFinancialSummaryResponse getPeriodSummary(
            Long currentUserId,
            String fromValue,
            String toValue
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        validateFinancialAccess(
                currentUser
        );


        Period period =
                parsePeriod(
                        fromValue,
                        toValue
                );


        return buildSummary(
                loadFinancialData(
                        currentUser.getTenantId()
                ),
                period.from(),
                period.to()
        );
    }


    // =====================================================
    // EXPENSE CATEGORIES
    // =====================================================

    @Transactional(readOnly = true)
    public DashboardExpenseCategoriesResponse getExpenseCategories(
            Long currentUserId,
            String fromValue,
            String toValue
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        validateFinancialAccess(
                currentUser
        );


        Period period =
                parsePeriod(
                        fromValue,
                        toValue
                );


        return buildExpenseCategories(
                loadFinancialData(
                        currentUser.getTenantId()
                ),
                period.from(),
                period.to()
        );
    }


    // =====================================================
    // FINANCIAL DATA
    // =====================================================

    private FinancialData loadFinancialData(
            Long tenantId
    ) {

        List<Income> incomes =
                incomeRepository
                        .findAllByTenant_IdOrderByIdAsc(
                                tenantId
                        );


        List<ManualExpense> manualExpenses =
                manualExpenseRepository
                        .findAllByTenant_IdOrderByIdAsc(
                                tenantId
                        );


        List<TravelExpense> travelExpenses =
                travelExpenseRepository
                        .findAllByTenant_IdOrderByIdAsc(
                                tenantId
                        )
                        .stream()
                        .filter(
                                expense ->
                                        expense.getStatus()
                                                ==
                                                TravelExpenseStatus.APPROVED
                        )
                        .toList();


        return new FinancialData(
                incomes,
                manualExpenses,
                travelExpenses
        );
    }


    // =====================================================
    // HISTORICAL
    // =====================================================

    private List<DashboardAnnualPointResponse> buildHistorical(
            FinancialData data,
            List<Integer> availableYears
    ) {

        List<DashboardAnnualPointResponse> result =
                new ArrayList<>();


        for (
                Integer year :
                availableYears
                        .stream()
                        .sorted()
                        .toList()
        ) {

            BigDecimal incomes =
                    calculatePaidIncomeForYear(
                            data.incomes(),
                            year
                    );


            BigDecimal manualExpenses =
                    calculateManualExpensesForYear(
                            data.manualExpenses(),
                            year
                    );


            BigDecimal travelExpenses =
                    calculateTravelExpensesForYear(
                            data.travelExpenses(),
                            year
                    );


            BigDecimal expenses =
                    money(
                            manualExpenses.add(
                                    travelExpenses
                            )
                    );


            BigDecimal profit =
                    money(
                            incomes.subtract(
                                    expenses
                            )
                    );


            result.add(
                    new DashboardAnnualPointResponse(
                            year,
                            incomes,
                            expenses,
                            profit
                    )
            );
        }


        return result;
    }


    // =====================================================
    // MONTHLY DATA
    // =====================================================

    private DashboardMonthlyResponse buildMonthly(
            FinancialData data,
            int year
    ) {

        List<DashboardMonthlyPointResponse> months =
                new ArrayList<>();


        for (
                int month = 1;
                month <= 12;
                month++
        ) {

            YearMonth period =
                    YearMonth.of(
                            year,
                            month
                    );


            BigDecimal incomes =
                    calculatePaidIncomeForMonth(
                            data.incomes(),
                            period
                    );


            BigDecimal manualExpenses =
                    calculateManualExpensesForMonth(
                            data.manualExpenses(),
                            period
                    );


            BigDecimal travelExpenses =
                    calculateTravelExpensesForMonth(
                            data.travelExpenses(),
                            period
                    );


            BigDecimal expenses =
                    money(
                            manualExpenses.add(
                                    travelExpenses
                            )
                    );


            BigDecimal profit =
                    money(
                            incomes.subtract(
                                    expenses
                            )
                    );


            months.add(
                    new DashboardMonthlyPointResponse(
                            MONTH_NAMES[
                                    month - 1
                                    ],
                            incomes,
                            expenses,
                            profit
                    )
            );
        }


        return new DashboardMonthlyResponse(
                year,
                months
        );
    }


    // =====================================================
    // PERIOD SUMMARY
    // =====================================================

    private DashboardFinancialSummaryResponse buildSummary(
            FinancialData data,
            YearMonth from,
            YearMonth to
    ) {

        LocalDate dateFrom =
                from.atDay(1);


        LocalDate dateTo =
                to.atEndOfMonth();


        BigDecimal incomes =
                ZERO;


        BigDecimal pendingCollection =
                ZERO;


        for (Income income : data.incomes()) {

            if (
                    income.getStatus()
                            ==
                            IncomeStatus.PAID
            ) {

                LocalDate date =
                        resolveIncomePaymentDate(
                                income
                        );


                if (
                        isBetween(
                                date,
                                dateFrom,
                                dateTo
                        )
                ) {

                    incomes =
                            incomes.add(
                                    income.getTotal()
                            );
                }
            }


            if (
                    income.getStatus()
                            ==
                            IncomeStatus.PENDING
                            &&
                            isBetween(
                                    income.getRecordDate(),
                                    dateFrom,
                                    dateTo
                            )
            ) {

                pendingCollection =
                        pendingCollection.add(
                                income.getTotal()
                        );
            }
        }


        BigDecimal registeredExpenses =
                ZERO;


        for (
                ManualExpense expense :
                data.manualExpenses()
        ) {

            if (
                    isBetween(
                            expense.getExpenseDate(),
                            dateFrom,
                            dateTo
                    )
            ) {

                registeredExpenses =
                        registeredExpenses.add(
                                expense.getAmount()
                        );
            }
        }


        BigDecimal travelExpenses =
                ZERO;


        for (
                TravelExpense expense :
                data.travelExpenses()
        ) {

            if (
                    isBetween(
                            expense.getExpenseDate(),
                            dateFrom,
                            dateTo
                    )
            ) {

                travelExpenses =
                        travelExpenses.add(
                                expense.getAmount()
                        );
            }
        }


        incomes =
                money(
                        incomes
                );


        pendingCollection =
                money(
                        pendingCollection
                );


        registeredExpenses =
                money(
                        registeredExpenses
                );


        travelExpenses =
                money(
                        travelExpenses
                );


        BigDecimal totalExpenses =
                money(
                        registeredExpenses.add(
                                travelExpenses
                        )
                );


        BigDecimal profit =
                money(
                        incomes.subtract(
                                totalExpenses
                        )
                );


        BigDecimal margin =
                calculateMargin(
                        incomes,
                        profit
                );


        return new DashboardFinancialSummaryResponse(
                incomes,
                pendingCollection,
                registeredExpenses,
                travelExpenses,
                totalExpenses,
                profit,
                margin,
                from.toString(),
                to.toString()
        );
    }


    // =====================================================
    // EXPENSE CATEGORIES
    // =====================================================

    private DashboardExpenseCategoriesResponse buildExpenseCategories(
            FinancialData data,
            YearMonth from,
            YearMonth to
    ) {

        LocalDate dateFrom =
                from.atDay(1);


        LocalDate dateTo =
                to.atEndOfMonth();


        Map<String, BigDecimal> totals =
                new LinkedHashMap<>();


        for (
                ManualExpense expense :
                data.manualExpenses()
        ) {

            if (
                    !isBetween(
                            expense.getExpenseDate(),
                            dateFrom,
                            dateTo
                    )
            ) {

                continue;
            }


            totals.merge(
                    expense
                            .getCategory()
                            .getApiValue(),

                    expense.getAmount(),

                    BigDecimal::add
            );
        }


        for (
                TravelExpense expense :
                data.travelExpenses()
        ) {

            if (
                    !isBetween(
                            expense.getExpenseDate(),
                            dateFrom,
                            dateTo
                    )
            ) {

                continue;
            }


            totals.merge(
                    expense
                            .getCategory()
                            .getApiValue(),

                    expense.getAmount(),

                    BigDecimal::add
            );
        }


        List<DashboardExpenseCategoryResponse> categories =
                totals
                        .entrySet()
                        .stream()
                        .map(
                                entry ->
                                        new DashboardExpenseCategoryResponse(
                                                entry.getKey(),
                                                money(
                                                        entry.getValue()
                                                )
                                        )
                        )
                        .sorted(
                                Comparator
                                        .comparing(
                                                DashboardExpenseCategoryResponse::total
                                        )
                                        .reversed()
                                        .thenComparing(
                                                DashboardExpenseCategoryResponse::category
                                        )
                        )
                        .toList();


        return new DashboardExpenseCategoriesResponse(
                from.toString(),
                to.toString(),
                categories
        );
    }


    // =====================================================
    // AVAILABLE YEARS
    // =====================================================

    private List<Integer> getAvailableYears(
            FinancialData data,
            int currentYear
    ) {

        Set<Integer> years =
                new TreeSet<>(
                        Comparator.reverseOrder()
                );


        years.add(
                currentYear
        );


        for (Income income : data.incomes()) {

            if (
                    income.getRecordDate()
                            != null
            ) {

                years.add(
                        income
                                .getRecordDate()
                                .getYear()
                );
            }


            if (
                    income.getPaymentDate()
                            != null
            ) {

                years.add(
                        income
                                .getPaymentDate()
                                .getYear()
                );
            }
        }


        for (
                ManualExpense expense :
                data.manualExpenses()
        ) {

            years.add(
                    expense
                            .getExpenseDate()
                            .getYear()
            );
        }


        for (
                TravelExpense expense :
                data.travelExpenses()
        ) {

            years.add(
                    expense
                            .getExpenseDate()
                            .getYear()
            );
        }


        return List.copyOf(
                years
        );
    }


    // =====================================================
    // YEAR CALCULATIONS
    // =====================================================

    private BigDecimal calculatePaidIncomeForYear(
            List<Income> incomes,
            int year
    ) {

        BigDecimal result =
                ZERO;


        for (Income income : incomes) {

            if (
                    income.getStatus()
                            !=
                            IncomeStatus.PAID
            ) {

                continue;
            }


            LocalDate date =
                    resolveIncomePaymentDate(
                            income
                    );


            if (
                    date != null
                            &&
                            date.getYear()
                                    ==
                                    year
            ) {

                result =
                        result.add(
                                income.getTotal()
                        );
            }
        }


        return money(
                result
        );
    }


    private BigDecimal calculateManualExpensesForYear(
            List<ManualExpense> expenses,
            int year
    ) {

        BigDecimal result =
                ZERO;


        for (
                ManualExpense expense :
                expenses
        ) {

            if (
                    expense
                            .getExpenseDate()
                            .getYear()
                            ==
                            year
            ) {

                result =
                        result.add(
                                expense.getAmount()
                        );
            }
        }


        return money(
                result
        );
    }


    private BigDecimal calculateTravelExpensesForYear(
            List<TravelExpense> expenses,
            int year
    ) {

        BigDecimal result =
                ZERO;


        for (
                TravelExpense expense :
                expenses
        ) {

            if (
                    expense
                            .getExpenseDate()
                            .getYear()
                            ==
                            year
            ) {

                result =
                        result.add(
                                expense.getAmount()
                        );
            }
        }


        return money(
                result
        );
    }


    // =====================================================
    // MONTH CALCULATIONS
    // =====================================================

    private BigDecimal calculatePaidIncomeForMonth(
            List<Income> incomes,
            YearMonth period
    ) {

        BigDecimal result =
                ZERO;


        for (Income income : incomes) {

            if (
                    income.getStatus()
                            !=
                            IncomeStatus.PAID
            ) {

                continue;
            }


            LocalDate date =
                    resolveIncomePaymentDate(
                            income
                    );


            if (
                    date != null
                            &&
                            YearMonth
                                    .from(date)
                                    .equals(period)
            ) {

                result =
                        result.add(
                                income.getTotal()
                        );
            }
        }


        return money(
                result
        );
    }


    private BigDecimal calculateManualExpensesForMonth(
            List<ManualExpense> expenses,
            YearMonth period
    ) {

        BigDecimal result =
                ZERO;


        for (
                ManualExpense expense :
                expenses
        ) {

            if (
                    YearMonth
                            .from(
                                    expense.getExpenseDate()
                            )
                            .equals(period)
            ) {

                result =
                        result.add(
                                expense.getAmount()
                        );
            }
        }


        return money(
                result
        );
    }


    private BigDecimal calculateTravelExpensesForMonth(
            List<TravelExpense> expenses,
            YearMonth period
    ) {

        BigDecimal result =
                ZERO;


        for (
                TravelExpense expense :
                expenses
        ) {

            if (
                    YearMonth
                            .from(
                                    expense.getExpenseDate()
                            )
                            .equals(period)
            ) {

                result =
                        result.add(
                                expense.getAmount()
                        );
            }
        }


        return money(
                result
        );
    }


    // =====================================================
    // CURRENT USER
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


    // =====================================================
    // ACCESS
    // =====================================================

    private void validateFinancialAccess(
            User user
    ) {

        if (
                user.getRole()
                        !=
                        Role.ADMIN
                        &&
                        user.getRole()
                                !=
                                Role.EDITOR
        ) {

            throw new AccessDeniedException(
                    "No tienes permisos para consultar información financiera."
            );
        }
    }


    // =====================================================
    // PERIOD
    // =====================================================

    private Period parsePeriod(
            String fromValue,
            String toValue
    ) {

        YearMonth from =
                parseYearMonth(
                        fromValue,
                        "El periodo inicial no es válido."
                );


        YearMonth to =
                parseYearMonth(
                        toValue,
                        "El periodo final no es válido."
                );


        if (
                from.isAfter(to)
        ) {

            throw new IllegalArgumentException(
                    "El periodo inicial no puede ser posterior al periodo final."
            );
        }


        return new Period(
                from,
                to
        );
    }


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
    // HELPERS
    // =====================================================

    private LocalDate resolveIncomePaymentDate(
            Income income
    ) {

        if (
                income.getPaymentDate()
                        != null
        ) {

            return income.getPaymentDate();
        }


        return income.getRecordDate();
    }


    private boolean isBetween(
            LocalDate value,
            LocalDate from,
            LocalDate to
    ) {

        if (value == null) {
            return false;
        }


        return !value.isBefore(from)
                &&
                !value.isAfter(to);
    }


    private BigDecimal calculateMargin(
            BigDecimal incomes,
            BigDecimal profit
    ) {

        if (
                incomes.compareTo(
                        BigDecimal.ZERO
                )
                        ==
                        0
        ) {

            return new BigDecimal(
                    "0.0"
            );
        }


        return profit
                .divide(
                        incomes,
                        4,
                        RoundingMode.HALF_UP
                )
                .multiply(
                        new BigDecimal("100")
                )
                .setScale(
                        1,
                        RoundingMode.HALF_UP
                );
    }


    private BigDecimal money(
            BigDecimal value
    ) {

        if (value == null) {
            return ZERO;
        }


        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }


    private record FinancialData(
            List<Income> incomes,
            List<ManualExpense> manualExpenses,
            List<TravelExpense> travelExpenses
    ) {
    }


    private record Period(
            YearMonth from,
            YearMonth to
    ) {
    }
}