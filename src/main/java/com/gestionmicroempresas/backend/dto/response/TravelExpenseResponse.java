package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.TravelExpense;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TravelExpenseResponse(

        Long id,

        @JsonProperty("categoria")
        String category,

        @JsonProperty("descripcion")
        String description,

        @JsonProperty("monto")
        BigDecimal amount,

        @JsonProperty("fecha")
        LocalDate expenseDate,

        @JsonProperty("estatus")
        String status,

        @JsonProperty("motivoRechazo")
        String rejectionReason,

        @JsonProperty("comprobanteUrl")
        String receiptUrl,

        @JsonProperty("creadoPor")
        UserOptionResponse createdBy,

        @JsonProperty("puedeEliminar")
        boolean canDelete,

        @JsonProperty("creadoEn")
        OffsetDateTime createdAt

) {

    public static TravelExpenseResponse from(
            TravelExpense expense,
            String receiptUrl,
            boolean canDelete
    ) {

        return new TravelExpenseResponse(

                expense.getId(),

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

                receiptUrl,

                UserOptionResponse.from(
                        expense.getCreatedBy()
                ),

                canDelete,

                expense.getCreatedAt()
        );
    }
}