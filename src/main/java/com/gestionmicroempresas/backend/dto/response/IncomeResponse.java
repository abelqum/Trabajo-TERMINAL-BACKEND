package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.Income;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record IncomeResponse(

        Long id,

        @JsonProperty("clienteId")
        Long clientId,

        @JsonProperty("cliente")
        ClientOptionResponse client,

        @JsonProperty("tipoRegistro")
        String recordType,

        @JsonProperty("concepto")
        String concept,

        BigDecimal subtotal,

        @JsonProperty("aplicaIva")
        boolean taxApplied,

        @JsonProperty("iva")
        BigDecimal taxAmount,

        BigDecimal total,

        @JsonProperty("estado")
        String status,

        @JsonProperty("fecha")
        LocalDate recordDate,

        @JsonProperty("fechaPago")
        LocalDate paymentDate,

        @JsonProperty("formaPago")
        String paymentMethod,

        @JsonProperty("creadoEn")
        OffsetDateTime createdAt,

        @JsonProperty("actualizadoEn")
        OffsetDateTime updatedAt

) {

    public static IncomeResponse from(
            Income income
    ) {

        return new IncomeResponse(

                income.getId(),

                income
                        .getClient()
                        .getId(),

                ClientOptionResponse.from(
                        income.getClient()
                ),

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
        );
    }
}