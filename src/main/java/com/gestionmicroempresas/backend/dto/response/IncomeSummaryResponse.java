package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record IncomeSummaryResponse(

        @JsonProperty("ingresos")
        BigDecimal collectedTotal,

        @JsonProperty("pendiente")
        BigDecimal pendingTotal,

        @JsonProperty("cancelado")
        BigDecimal canceledTotal,

        @JsonProperty("totalRegistros")
        long totalRecords,

        @JsonProperty("registrosPagados")
        long paidRecords,

        @JsonProperty("facturasPendientes")
        long pendingInvoices,

        @JsonProperty("facturasCanceladas")
        long canceledInvoices,

        @JsonProperty("ingresosDirectos")
        long directIncomes

) {
}