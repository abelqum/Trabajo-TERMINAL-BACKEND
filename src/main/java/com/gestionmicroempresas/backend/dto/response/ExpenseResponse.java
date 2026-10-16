package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.ManualExpense;
import com.gestionmicroempresas.backend.entity.TravelExpense;

import java.math.BigDecimal;

import java.time.LocalDate;

public record ExpenseResponse(

        Long id,

        @JsonProperty("origen")
        String origin,

        @JsonProperty("concepto")
        String concept,

        @JsonProperty("categoria")
        String category,

        @JsonProperty("monto")
        BigDecimal amount,

        @JsonProperty("fecha")
        LocalDate expenseDate,

        @JsonProperty("descripcion")
        String description,

        @JsonProperty("viaje")
        ExpenseTravelResponse travel

) {

    public static ExpenseResponse fromManualExpense(
            ManualExpense expense
    ) {

        return new ExpenseResponse(

                expense.getId(),

                "REGISTRADO",

                expense.getConcept(),

                expense
                        .getCategory()
                        .getApiValue(),

                expense.getAmount(),

                expense.getExpenseDate(),

                expense.getDescription(),

                null
        );
    }


    public static ExpenseResponse fromTravelExpense(
            TravelExpense expense
    ) {

        return new ExpenseResponse(

                expense.getId(),

                "VIATICO",

                null,

                expense
                        .getCategory()
                        .getApiValue(),

                expense.getAmount(),

                expense.getExpenseDate(),

                expense.getDescription(),

                ExpenseTravelResponse.from(
                        expense.getTravel()
                )
        );
    }
}